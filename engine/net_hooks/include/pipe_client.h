#pragma once
#include <windows.h>
class PipeClient {
public:
	void sendPacket(char* buf, ULONG len);
};