package me.helldiner.holdon.hook;

public class NetHooksHandler extends Thread {
	
	private PipeHandler pipe;
	private NetHooksListener listener;
	
	public NetHooksHandler(NetHooksListener listener) {
		this.listener = listener;
		this.pipe = new PipeHandler();
	}
	
	public boolean connectPipe() {
		if(this.pipe.connect()) {
			this.start();
			return true;
		}
		return false;
	}
	
	public void receivePacketInfo(String ip, int port, boolean received) {
		this.listener.onPacketInfoReceived(ip, port, received);
	}
	
	public void receivePacketBytes(char[] bytes) {
		
	}
	
	@Override
	public void run() {
		while(true) {
			pipe.tick(NetHooksHandler.this);
		}
	}
	
	private class PipeHandler {
		
		public native boolean connect();
		public native void tick(NetHooksHandler callbackHandler);
		public native void sendPacketBytes(char[] bytes);
		
	}
	
}