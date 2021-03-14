package me.helldiner.holdon.hook;

public class NetHooksHandler extends Thread {
	
	private PipeHandler pipe;
	
	public NetHooksHandler() {
		this.pipe = new PipeHandler();
	}
	
	public boolean connectPipe() {
		if(this.pipe.connect()) {
			this.start();
			return true;
		}
		return false;
	}
	
	public void receivePacketInfo(int id, String ip, int port) {
		System.out.println(id+" "+ip+" "+port);
	}
	
	public void receivePacketBytes(int id, char[] bytes) {
		
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
		public native void sendPacketBytes(int id);
		
	}
	
}