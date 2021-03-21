#include "pch.h"
#include "injector.h"
#include "psapi.h"
#include <string>

JNIEXPORT jobjectArray JNICALL Java_me_helldiner_holdon_hook_Injector_listProcesses
(JNIEnv* env, jclass cls) {
    DWORD processes[1024], cbNeeded;
    if (!EnumProcesses(processes, sizeof(processes), &cbNeeded)) {
        return NULL;
    }
    DWORD amount = cbNeeded / sizeof(DWORD);
	jobjectArray processesArr = env->NewObjectArray(amount, env->FindClass("java/lang/String"), env->NewStringUTF(""));
    for (int i(0); i < amount; i++) {
        DWORD pid = processes[i];
        char szProcessName[MAX_PATH] = "UNKNOWN";
        std::string processName;
        HANDLE process = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, pid);
        if (process != NULL) {
            HMODULE hMod;
            DWORD cbNeeded;
            if (EnumProcessModules(process, &hMod, sizeof(hMod), &cbNeeded)) {
                GetModuleFileNameExA(process, hMod, (LPSTR)szProcessName, sizeof(szProcessName));
                processName = szProcessName;
                processName += "|";
                processName += std::to_string(pid);
            }
        }
        env->SetObjectArrayElement(processesArr, i, env->NewStringUTF(processName.c_str()));
    }
	return processesArr;
}