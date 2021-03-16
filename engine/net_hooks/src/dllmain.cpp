#include "pch.h"
#include "hook.h"
#include "pipe_client.h"
#include "ws2tcpip.h"

#ifdef DBG
#include <iostream>
#endif

SIZE_T maxLen;
LPWSABUF lpBuffers;
DWORD dwBufferCount;
LPDWORD lpNumberOfBytesRecvd;
char* buf;
int len;
PipeClient *pipe = NULL;
bool pauseThread = false;
HANDLE pausedThread;
bool singleStep = false;

void receiverThread() {
    while (true) {
        char msg[BUFF_LEN];
        int len;
        if (pipe->waitingData()) {
            while ((len = pipe->readData(msg)) <= 0);
            if (len == 1) {
                switch (msg[0]) {
                case PACKET_CONTENT:
                    if (pipe->readData(msg) == sizeof(int)) {
                        int packetLen = 0;
                        memcpy(&packetLen, msg, sizeof(int));
                        char* packet = (char*)malloc(packetLen);
                        if (packet) {
                            int i(0);
                            while (i < packetLen) {
                                int bufferLen = pipe->readData(msg);
                                memcpy(&packet[i], msg, bufferLen);
                                i += bufferLen;
                            }
                            memcpy(buf,packet,packetLen);
                        }
                    }
                    break;
                case PAUSE_THREAD:
                    pauseThread = true;
                    break;
                case CONTINUE_THREAD:
                    if (pausedThread && pauseThread) ResumeThread(pausedThread);
                    break;
                case SINGLE_STEP:
                    singleStep = true;
                    if (pausedThread && pauseThread) ResumeThread(pausedThread);
                }
            }
        }
    }
}

void hookWSARecvStart(SIZE_T* stack) {
    maxLen = 0;
    SOCKET s = stack[0];
    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    int nameLen = sizeof(addr);
    if (!getpeername(s, (struct sockaddr*) &addr, &nameLen)) {
        char ip[16];
        inet_ntop(AF_INET, &addr.sin_addr, ip, sizeof(ip));
        unsigned int port = ntohs(addr.sin_port);
        lpBuffers = (LPWSABUF)stack[1];
        dwBufferCount = stack[2];
        for (int i(0); i < dwBufferCount; i++) {
            WSABUF wsaBuf = lpBuffers[i];
            maxLen += wsaBuf.len;
        }
        lpNumberOfBytesRecvd = (LPDWORD)stack[3];
        const char packet_info[] = { PACKET_INFO };
        char* portArr = reinterpret_cast<char*>(&port);
        const char received[] = { true };
        pipe->sendData((char*)packet_info, 1);
        pipe->sendData((char*)received, sizeof(bool));
        pipe->sendData(ip, 16);
        pipe->sendData(portArr, sizeof(unsigned int));
    }
    else {
        lpNumberOfBytesRecvd = NULL;
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 12);
        cout << "Couldn't resolve socket address, error : " << WSAGetLastError() << endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
    }
}

void hookWSARecvEnd() {
    if (lpNumberOfBytesRecvd) {
        DWORD len = *lpNumberOfBytesRecvd;
        if (len <= maxLen && pauseThread) {
            const char packetContentCode[] = { PACKET_CONTENT };
            pipe->sendData((char*) packetContentCode, 1);
            char* lenArr = reinterpret_cast<char*>(&len);
            pipe->sendData(lenArr, sizeof(int));
            int totalLen = 0;
            for (int i(0); i < dwBufferCount && totalLen < len; i++) {
                WSABUF wsaBuf = lpBuffers[i];
                int i2(0);
                while (i2 < wsaBuf.len && totalLen < len) {
                    int sendLen = BUFF_LEN;
                    if (wsaBuf.len - i2 < sendLen) sendLen = wsaBuf.len - i2;
                    if (len - totalLen < sendLen) sendLen = len - totalLen;
                    char* sendArr = (char*) malloc(sendLen);
                    if (sendArr) {
                        memcpy(sendArr, &wsaBuf.buf[i2], sendLen);
                        pipe->sendData(sendArr, sendLen);
                    }
                    i2 += sendLen;
                    totalLen += sendLen;
                }
            }
        }
        pausedThread = OpenThread(THREAD_ALL_ACCESS, FALSE, GetCurrentThreadId());
        if (pausedThread) SuspendThread(pausedThread);
        pauseThread = false;
        if (singleStep) {
            pauseThread = true;
            singleStep = false;
        }
    }
}

void hookRecvStart(SIZE_T* stack) {
    SOCKET s = stack[0];
    buf = (char*) stack[1];
    len = stack[2];
    struct sockaddr_in6 addr;
    memset(&addr, 0, sizeof(addr));
    int nameLen = sizeof(addr);
    if (!getpeername(s, (struct sockaddr*)&addr, &nameLen)) {
        char ip[16];
        inet_ntop(AF_INET, &addr.sin6_addr, ip, sizeof(ip));
        unsigned int port = ntohs(addr.sin6_port);
        const char packet_info[] = { PACKET_INFO };
        char* portArr = reinterpret_cast<char*>(&port);
        const char received[] = { true };
        pipe->sendData((char*)packet_info, 1);
        pipe->sendData((char*)received, sizeof(bool));
        pipe->sendData(ip, 16);
        pipe->sendData(portArr, sizeof(unsigned int));
    }
    else {
        lpNumberOfBytesRecvd = NULL;
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 12);
        cout << "Couldn't resolve socket address, error : " << WSAGetLastError() << endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
    }
}

void hookRecvEnd() {
    if (buf && pauseThread) {
        const char packetContentCode[] = { PACKET_CONTENT };
        pipe->sendData((char*)packetContentCode, 1);
        char* lenArr = reinterpret_cast<char*>(&len);
        pipe->sendData(lenArr, sizeof(int));
        int i(0);
        while (i < len) {
            int sendLen = BUFF_LEN;
            if (len - i < sendLen) sendLen = len - i;
            char* sendArr = (char*)malloc(sendLen);
            if (sendArr) {
                memcpy(sendArr, &buf[i], sendLen);
                pipe->sendData(sendArr, sendLen);
            }
            i += sendLen;
        }
        pausedThread = OpenThread(THREAD_ALL_ACCESS, FALSE, GetCurrentThreadId());
        if (pausedThread) SuspendThread(pausedThread);
        pauseThread = false;
        if (singleStep) {
            pauseThread = true;
            singleStep = false;
        }
    }
}

void hookWSASend(SIZE_T* stack) {
    SOCKET s = stack[0];
    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    int nameLen = sizeof(addr);
    if (!getpeername(s, (struct sockaddr*)&addr, &nameLen)) {
        char ip[16];
        inet_ntop(AF_INET, &addr.sin_addr, ip, sizeof(ip));
        unsigned int port = ntohs(addr.sin_port);
        const char packet_info[] = { PACKET_INFO };
        char* portArr = reinterpret_cast<char*>(&port);
        const char received[] = { false };
        pipe->sendData((char*)packet_info, 1);
        pipe->sendData((char*)received, sizeof(bool));
        pipe->sendData(ip, 16);
        pipe->sendData(portArr, sizeof(unsigned int));
    }
    else {
        lpNumberOfBytesRecvd = NULL;
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 12);
        cout << "Couldn't resolve socket address, error : " << WSAGetLastError() << endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
    }
}

void hookSend(SIZE_T* stack) {
    SOCKET s = stack[0];
    buf = (char*) stack[1];
    len = (int)stack[2];
    struct sockaddr_in6 addr;
    memset(&addr, 0, sizeof(addr));
    int nameLen = sizeof(addr);
    if (!getpeername(s, (struct sockaddr*)&addr, &nameLen)) {
        char ip[16];
        inet_ntop(AF_INET, &addr.sin6_addr, ip, sizeof(ip));
        unsigned int port = ntohs(addr.sin6_port);
        const char packet_info[] = { PACKET_INFO };
        char* portArr = reinterpret_cast<char*>(&port);
        const char received[] = { false };
        pipe->sendData((char*)packet_info, 1);
        pipe->sendData((char*)received, sizeof(bool));
        pipe->sendData(ip, 16);
        pipe->sendData(portArr, sizeof(unsigned int));
        if (pauseThread) {
            const char packetContentCode[] = { PACKET_CONTENT };
            pipe->sendData((char*)packetContentCode, 1);
            char* lenArr = reinterpret_cast<char*>(&len);
            pipe->sendData(lenArr, sizeof(int));
            int i(0);
            while (i < len) {
                int sendLen = BUFF_LEN;
                if (len - i < sendLen) sendLen = len - i;
                char* sendArr = (char*)malloc(sendLen);
                if (sendArr) {
                    memcpy(sendArr, &buf[i], sendLen);
                    pipe->sendData(sendArr, sendLen);
                }
                i += sendLen;
            }
            pausedThread = OpenThread(THREAD_ALL_ACCESS, FALSE, GetCurrentThreadId());
            if (pausedThread) SuspendThread(pausedThread);
            pauseThread = false;
            if (singleStep) {
                pauseThread = true;
                singleStep = false;
            }
        }
    }
    else {
        lpNumberOfBytesRecvd = NULL;
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 12);
        cout << "Couldn't resolve socket address, error : " << WSAGetLastError() << endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
    }
}

bool initPipe() {
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
                return false;
            }
        }
        if (msg) {
            if (msg[0] == SYNC) {
                const char sync[] = { SYNC };
                pipe->sendData((char*)sync, 1);
                return true;
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
    return false;
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
        SIZE_T recvStartAddr = (SIZE_T)module + 0x11D90;
        injector = HookInjector(recvStartAddr, 15, &hookRecvStart);
        injector.inject();
        SIZE_T recvEndAddr = (SIZE_T)module + 0x11E7F;
        injector = HookInjector(recvEndAddr, 14, &hookRecvEnd);
        injector.inject();
        SIZE_T wsasendAddr = (SIZE_T)module + 0x1F60;
        injector = HookInjector(wsasendAddr, 15, &hookWSASend);
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
        if (initPipe()) {
            initHooks();
            CreateThread(NULL, NULL, (LPTHREAD_START_ROUTINE)&receiverThread, NULL, NULL, NULL);
        }
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}