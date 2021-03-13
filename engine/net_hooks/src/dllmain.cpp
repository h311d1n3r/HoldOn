#include "pch.h"
#include "hook.h"
#include "pipe_client.h"

#ifdef DBG
#include <iostream>
#endif

SIZE_T maxLen;
LPWSABUF lpBuffers;
DWORD dwBufferCount;
LPDWORD lpNumberOfBytesRecvd;
PipeClient *pipe = NULL;

void hookWSARecvStart(SIZE_T* stack) {
    maxLen = 0;
    lpBuffers = (LPWSABUF) stack[1];
    dwBufferCount = stack[2];
    for (int i(0); i < dwBufferCount; i++) {
        WSABUF wsaBuf = lpBuffers[i];
        maxLen += wsaBuf.len;
    }
    lpNumberOfBytesRecvd = (LPDWORD) stack[3];
}

void hookWSARecvEnd() {
    if (lpNumberOfBytesRecvd) {
        DWORD len = *lpNumberOfBytesRecvd;
        if (len <= maxLen) {
            //printf("%x ", len);
            int totalLen = 0;
            for (int i(0); i < dwBufferCount && totalLen < len; i++) {
                WSABUF wsaBuf = lpBuffers[i];
                for (int i2(0); i2 < wsaBuf.len && totalLen < len; i2++) {
                    char c = wsaBuf.buf[i2];
                    totalLen++;
                    printf("%x", c);
                }
            }
        }
    }
}

void hookSend(SIZE_T* stack) {

}

void initPipe() {
    bool pipeSuccess;
    pipe = new PipeClient(pipeSuccess);
    if (pipeSuccess) {
        char msg[BUFF_LEN];
        int timeout = 0;
        while (pipe->readData(msg) <= 0) {
            Sleep(100);
            timeout++;
            if (timeout >= 20) {
                #ifdef DBG
                HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
                SetConsoleTextAttribute(hConsole, 12);
                cout << "Pipe synchronization failed due to timeout..." << endl;
                SetConsoleTextAttribute(hConsole, 8);
                #endif
                return;
            }
        }
        if (msg) {
            if (msg[0] == SYNC) {
                const char sync[] = { SYNC };
                pipe->sendData((char*)sync, 1);
            }
        }
    }
    else {
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 12);
        cout << "An error occured while enabling the pipe..." << endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
    }
}

void initHooks() {
    HMODULE module = GetModuleHandleA("WS2_32.dll");
    if (module) {
        SIZE_T wsarecvStartAddr = (SIZE_T)module + 0x10500;
        HookInjector injector(wsarecvStartAddr, 15, &hookWSARecvStart);
        injector.inject();
        SIZE_T wsarecvEndAddr = (SIZE_T)module + 0x10679;
        injector = HookInjector(wsarecvEndAddr, 16, &hookWSARecvEnd);
        injector.inject();
        SIZE_T sendAddr = (SIZE_T)module + 0x2320;
        injector = HookInjector(sendAddr, 15, &hookSend);
        injector.inject();
    }
    else {
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 12);
        cout << "Module WS2_32.dll couldn't be found in process..." << endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
    }
}

BOOL APIENTRY DllMain(HMODULE hModule, DWORD ul_reason_for_call, LPVOID lpReserved) {
    switch (ul_reason_for_call) {
    case DLL_PROCESS_ATTACH:
        initPipe();
        initHooks();
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}