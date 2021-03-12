#include "pch.h"
#include "pipe_server.h"
#include "pipe_constants.h"
#include "pipe_handler.h"

#include <iostream>

PipeServer::PipeServer(bool& success) {
	success = this->initPipe();
}

PipeServer::~PipeServer() {
	//TODO : tell client to disconnect
	if (this->hPipe) CloseHandle(hPipe);
}

bool PipeServer::initPipe() {
    this->hPipe = CreateNamedPipe(
        this->pipeName,
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
        /*DBG*/
        return false;
    }
    if (this->hPipe == NULL) {
        /*DBG*/
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
    /*DBG*/
    return false;
}

int PipeServer::readData(char* buf) {
    ULONG readLen;
    if (ReadFile(this->hPipe, buf, BUFF_LEN, &readLen, NULL)) {
        return readLen;
    }
    /*DBG*/
    //impl must check for ERROR_BROKEN_PIPE and ERROR_MORE_DATA
    return -1;
}

PipeServer* pipe = NULL;

JNIEXPORT jboolean JNICALL Java_me_helldiner_holdon_hook_NetHooksHandler_00024PipeHandler_connect(JNIEnv* env, jobject obj) {
    bool success;
    pipe = new PipeServer(success);
    if (!success) {
        /*DBG*/
        return false;
    }
    char msg[BUFF_LEN];
    int timeout = 0;
    while (pipe->readData(msg) <= 0) {
        Sleep(100);
        timeout++;
        if (timeout >= 20) return false; //2 seconds
    }
    if (msg) {
        if (msg[0] == SYNC) return true;
    }
    return false;
}