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
        if (len <= maxLen) {
            //printf("%x ", len);
            int totalLen = 0;
            for (int i(0); i < dwBufferCount && totalLen < len; i++) {
                WSABUF wsaBuf = lpBuffers[i];
                for (int i2(0); i2 < wsaBuf.len && totalLen < len; i2++) {
                    char c = wsaBuf.buf[i2];
                    totalLen++;
                    //printf("%x", c);
                }
            }
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
    if (buf) {
        
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
        if(initPipe()) initHooks();
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}