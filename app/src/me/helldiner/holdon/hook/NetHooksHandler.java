package me.helldiner.holdon.hook;

public class NetHooksHandler {
	
	private PipeHandler pipe;
	
	public NetHooksHandler() {
		this.pipe = new PipeHandler();
	}
	
	public boolean connectPipe() {
		return this.pipe.connect();
	}
	
	public void receivePacketInfo(int id, String info) {
		
	}
	
	public void receivePacketBytes(int id, char[] bytes) {
		
	}
	
	private class PipeHandler {
		
		public native boolean connect();
		public native void sendSharedDir(String dir);
		public native void sendPacketBytes(int id);
		
	}
	
}