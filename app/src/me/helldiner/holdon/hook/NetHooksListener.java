package me.helldiner.holdon.hook;

public interface NetHooksListener {
	
	public void onPacketInfoReceived(String ip, int port, boolean received);
	
}
