#include "pch.h"
#include "injector.h"
#include <string>
#include <filesystem>
#include <Psapi.h>

using namespace std;

HANDLE openProcess(int pid) {
    return OpenProcess(PROCESS_ALL_ACCESS, false, pid);
}

LPVOID allocateMemory(HANDLE process, string dll_path) {
    return VirtualAllocEx(process, NULL, dll_path.size() + 1, MEM_RESERVE | MEM_COMMIT, PAGE_READWRITE);
}

void writeDLLPath(HANDLE process, string dll_path, LPVOID addr) {
    WriteProcessMemory(process, addr, dll_path.c_str(), dll_path.size() + 1, NULL);
}

HANDLE loadDLL(HANDLE process, LPVOID addr) {
    return CreateRemoteThread(process, nullptr, NULL, (LPTHREAD_START_ROUTINE)LoadLibraryA, addr, NULL, nullptr);
}

string dllNameFromPath(const char* path) {
    string pathString = path;
    int nameIndex = pathString.find_last_of("\\") + 1;
    string name = pathString.substr(nameIndex);
    for (auto& c : name) c = toupper(c);
    return name;
}

bool isDllAlreadyLoaded(const char* path, HANDLE process) {
    HMODULE hMods[1024];
    DWORD cbNeeded;
    char szModuleName[MAX_PATH] = "UNKNOWN";
    if (EnumProcessModules(process, hMods, sizeof(hMods), &cbNeeded)) {
        for (int i = 0; i < (cbNeeded / sizeof(HMODULE)); i++) {
            TCHAR szModName[MAX_PATH];
            if (GetModuleFileNameExA(process, hMods[i], szModuleName,
                sizeof(szModuleName) / sizeof(TCHAR))) {
                if(!dllNameFromPath(szModuleName).compare(dllNameFromPath(path))) return true;
            }
        }
    }
    return false;
}

JNIEXPORT jboolean JNICALL Java_me_helldiner_holdon_hook_Injector_inject
(JNIEnv* env, jobject obj, jstring dllPath, jint pid) {
    const char* dllPathStr = env->GetStringUTFChars(dllPath, NULL);
    HANDLE process = openProcess(pid);
    if (!isDllAlreadyLoaded(dllPathStr, process)) {
        if (filesystem::exists(dllPathStr)) {
            LPVOID allocation_start_addr = allocateMemory(process, dllPathStr);
            writeDLLPath(process, dllPathStr, allocation_start_addr);
            env->ReleaseStringUTFChars(dllPath, dllPathStr);
            HANDLE thread = loadDLL(process, allocation_start_addr);
            if (!thread) return false;
        }
        else {
            env->ReleaseStringUTFChars(dllPath, dllPathStr);
            return false;
        }
    } else {
        env->ReleaseStringUTFChars(dllPath, dllPathStr);
        return true;
    }
    return true;
}

BOOL APIENTRY DllMain(HMODULE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
    case DLL_PROCESS_ATTACH:
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}