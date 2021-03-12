#pragma once
#include <windows.h>
#include "pipe_constants.h"

class PipeClient {
public:
	PipeClient(bool& success);
	~PipeClient();
	bool sendData(char* buf, ULONG len);
	int readData(char* buf);
private:
	HANDLE hPipe = NULL;
	LPCTSTR pipeName = TEXT("\\\\.\\pipe\\holdon_pipe");
	bool initPipe();
};