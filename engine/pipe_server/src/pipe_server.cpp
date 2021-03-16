#include "pch.h"
#include "pipe_server.h"
#include "pipe_constants.h"
#include "pipe_handler.h"
#include <iostream>

PipeServer::PipeServer(bool& success) {
	success = this->initPipe();
    if (success) {
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 10);
        std::cout << "Pipe server initialized !" << std::endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
    }
}

PipeServer::~PipeServer() {
    if (this->hPipe) {
        const char disconnect[] = { DISCONNECT };
        this->sendData((char*)disconnect, 1);
        CloseHandle(hPipe);
    }
}

bool PipeServer::initPipe() {
    this->hPipe = CreateNamedPipe(
        PIPE_NAME,
        PIPE_ACCESS_DUPLEX,
        PIPE_TYPE_MESSAGE |
        PIPE_READMODE_MESSAGE |
        PIPE_WAIT,
        1,
        BUFF_LEN,
        BUFF_LEN,
        0,
        NULL);
    COMMTIMEOUTS timeouts = { 10, 0, 0, 0, 0 };
    SetCommTimeouts(hPipe, &timeouts);
    if (!(ConnectNamedPipe(this->hPipe, NULL) ? true : (GetLastError() == ERROR_PIPE_CONNECTED))) {
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 12);
        std::cout << "Client pipe couldn't connect..." << std::endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
        CloseHandle(hPipe);
        return false;
    }
    if (this->hPipe == NULL) {
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 12);
        std::cout << "An error occured while trying to enable pipe : " << GetLastError() << std::endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
        return false;
    }
    return true;
}

bool PipeServer::sendData(char* buf, ULONG len) {
    ULONG writtenLen;
    if (WriteFile(this->hPipe, buf, len, &writtenLen, NULL)) {
        if (writtenLen == len) return true;
        return false;
    }
    #ifdef DBG
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    SetConsoleTextAttribute(hConsole, 12);
    std::cout << "An error occured while trying to write to pipe : " << GetLastError() << std::endl;
    SetConsoleTextAttribute(hConsole, 8);
    #endif
    return false;
}

int PipeServer::readData(char* buf) {
    ULONG readLen;
    if (ReadFile(this->hPipe, buf, BUFF_LEN, &readLen, NULL)) {
        return readLen;
    }
    #ifdef DBG
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    SetConsoleTextAttribute(hConsole, 12);
    std::cout << "An error occured while trying to read from pipe : " << GetLastError() << std::endl;
    SetConsoleTextAttribute(hConsole, 8);
    #endif
    return -1;
}

bool PipeServer::waitingData() {
    DWORD total_available_bytes;
    if (PeekNamedPipe(hPipe, 0, 0, 0, &total_available_bytes, 0)) {
        if (total_available_bytes > 0) {
            return true;
        }
    }
    return false;
}

PipeServer* pipe = NULL;

JNIEXPORT jboolean JNICALL Java_me_helldiner_holdon_hook_NetHooksHandler_00024PipeHandler_connect(JNIEnv* env, jobject obj) {
    bool success;
    pipe = new PipeServer(success);
    if (!success) {
        #ifdef DBG
        HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
        SetConsoleTextAttribute(hConsole, 12);
        std::cout << "An error occured while enabling the pipe server..." << std::endl;
        SetConsoleTextAttribute(hConsole, 8);
        #endif
        return false;
    }
    const char sync[] = { SYNC };
    pipe->sendData((char*)sync, 1);
    char msg[BUFF_LEN];
    int timeout = 0;
    while (pipe->readData(msg) <= 0) {
        Sleep(100);
        timeout++;
        if (timeout >= 20) { //2 seconds
            #ifdef DBG
            HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
            SetConsoleTextAttribute(hConsole, 12);
            std::cout << "Pipe synchronization failed due to timeout..." << std::endl;
            SetConsoleTextAttribute(hConsole, 8);
            #endif
            return false;
        }
    }
    if (msg) {
        if (msg[0] == SYNC) return true;
    }
    return false;
}

JNIEXPORT void JNICALL Java_me_helldiner_holdon_hook_NetHooksHandler_00024PipeHandler_tick(JNIEnv* env, jobject obj, jobject callbackHandler, jbyte message) {
    char msg[BUFF_LEN];
    if (pipe->waitingData()) {
        if (pipe->readData(msg) == 1) {
            jclass net_hooks_handler_class = env->GetObjectClass(callbackHandler);
            switch (msg[0]) {
            case PACKET_INFO:
                if (pipe->readData(msg) == sizeof(bool)) {
                    bool received = false;
                    memcpy(&received, msg, sizeof(bool));
                    if (pipe->readData(msg) == 16) {
                        char ip[16];
                        memcpy(ip, msg, 16);
                        if (pipe->readData(msg) == sizeof(unsigned int)) {
                            unsigned int port = 0;
                            memcpy(&port, msg, sizeof(unsigned int));
                            jmethodID receive_packet_info = env->GetMethodID(net_hooks_handler_class, "receivePacketInfo", "(Ljava/lang/String;IZ)V");
                            env->CallVoidMethod(callbackHandler, receive_packet_info, env->NewStringUTF(ip), port, received);
                        }
                    }
                }
                break;
            case PACKET_CONTENT:
                if (pipe->readData(msg) == sizeof(int)) {
                    int packetLen = 0;
                    memcpy(&packetLen, msg, sizeof(int));
                    char* packet = (char*)malloc(packetLen);
                    if (packet) {
                        int i(0);
                        while (i < packetLen) {
                            int buffLen = pipe->readData(msg);
                            memcpy(&packet[i], msg, buffLen);
                            i += buffLen;
                        }
                        jchar* jPacket = (jchar*)calloc(sizeof(jchar), packetLen);
                        if (jPacket) {
                            for (int i = 0; i < packetLen; i++) {
                                jPacket[i] = (jchar)packet[i];
                            }
                            jcharArray packetBytes = env->NewCharArray(packetLen);
                            env->SetCharArrayRegion(packetBytes, 0, packetLen, jPacket);
                            jmethodID receive_packet_bytes = env->GetMethodID(net_hooks_handler_class, "receivePacketBytes", "([C)V");
                            env->CallVoidMethod(callbackHandler, receive_packet_bytes, packetBytes);
                        }
                    }
                }
                break;
            }
        }
    }
    const char pauseThread[] = { PAUSE_THREAD };
    const char continueThread[] = { CONTINUE_THREAD };
    const char singleStep[] = { SINGLE_STEP };
    switch (message) {
    case 1:
        pipe->sendData((char*)pauseThread, 1);
        break;
    case 2:
        pipe->sendData((char*)continueThread, 1);
        break;
    case 3:
        pipe->sendData((char*)singleStep, 1);
        break;
    }
}

JNIEXPORT void JNICALL Java_me_helldiner_holdon_hook_NetHooksHandler_00024PipeHandler_sendPacketBytes(JNIEnv* env, jobject obj, jcharArray arr) {
    int len = env->GetArrayLength(arr);
    jchar* buf = (jchar*)calloc(sizeof(jchar), len);
    if (buf) {
        env->GetCharArrayRegion(arr, 0, len, buf);
        char* packet = (char*)malloc(len);
        if (packet) {
            for (int i(0); i < len; i++) {
                packet[i] = (char)buf[i];
            }
            const char packetContentCode[] = { PACKET_CONTENT };
            pipe->sendData((char*) packetContentCode, 1);
            char* packetLen = reinterpret_cast<char*>(&len);
            pipe->sendData(packetLen, sizeof(int));
            int i(0);
            while (i < len) {
                int sendLen = BUFF_LEN;
                if (len - i < sendLen) sendLen = len - i;
                char* sendArr = (char*)malloc(sendLen);
                if (sendArr) {
                    memcpy(sendArr, &packet[i], sendLen);
                    pipe->sendData(sendArr, sendLen);
                }
                i += sendLen;
            }
        }
    }
}