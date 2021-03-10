#include <pch.h>
#include <pipe_client.h>

PipeClient::PipeClient(bool& success) {
    success = this->initPipe();
}

PipeClient::~PipeClient() {
    if (this->hPipe) CloseHandle(this->hPipe);
}

bool PipeClient::initPipe() {
    while (true) {
        this->hPipe = CreateFile(
            this->pipeName,
            GENERIC_READ | GENERIC_WRITE,
            0,
            NULL,
            OPEN_EXISTING,
            0,
            NULL);
        if (this->hPipe != INVALID_HANDLE_VALUE) break;
        if (GetLastError() != ERROR_PIPE_BUSY) {
            /*DBG*/
            return false;
        }
        if (!WaitNamedPipe(this->pipeName, 20000)) {
            /*DBG*/
            return false;
        }
    }
    DWORD dwMode = PIPE_READMODE_MESSAGE;
    if (!SetNamedPipeHandleState(this->hPipe, &dwMode, NULL, NULL)) {
        /*DBG*/
        return false;
    }
    return true;
}

bool PipeClient::sendData(char* buf, ULONG len) {
    ULONG writtenLen;
    if (WriteFile(this->hPipe, buf, len, &writtenLen, NULL)) {
        if(writtenLen == len) return true;
        return false;
    }
    /*DBG*/
    return false;
}

int PipeClient::readData(char* buf, int bufSize) {
    ULONG readLen;
    if (ReadFile(this->hPipe, buf, bufSize, &readLen, NULL)) {
        return readLen;
    }
    /*DBG*/
    //impl must check if ERROR_MORE_DATA
    return -1;
}