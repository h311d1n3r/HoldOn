#include "pch.h"
#include "pipe_server.h"
#include "pipe_constants.h"
#include "pipe_handler.h"

#ifdef DBG
#include <iostream>
#endif

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