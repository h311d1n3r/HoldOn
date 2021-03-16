package me.helldiner.holdon.hook;

public interface NetHooksListener {
	
	public void onNetHooksInjection(NetHooksHandler handler);
	public void onPacketInfoReceived(String ip, int port, boolean received);
	public void onPacketBytesReceived(char[] packet);
	
}
