#pragma once

class PipeServer {
public:
	PipeServer(bool& success);
	~PipeServer();
	bool sendData(char* buf, ULONG len);
	int readData(char* buf);
private:
	HANDLE hPipe = NULL;
	LPCTSTR pipeName = TEXT("\\\\.\\pipe\\holdon_pipe");
	bool initPipe();
};