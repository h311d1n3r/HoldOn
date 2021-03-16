package me.helldiner.holdon.hook;

import java.util.concurrent.atomic.AtomicInteger;

public class NetHooksHandler extends Thread {
	
	private PipeHandler pipe;
	private NetHooksListener listener;
	private AtomicInteger message = new AtomicInteger(0);
	
	public NetHooksHandler(NetHooksListener listener) {
		this.listener = listener;
		this.pipe = new PipeHandler();
		this.listener.onNetHooksInjection(this);
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
		this.listener.onPacketBytesReceived(bytes);
	}
	
	public void pauseThread() {
		this.message.set(1);
	}
	
	public void continueThread() {
		this.message.set(2);
	}
	
	public void singleStep() {
		this.message.set(3);
	}
	
	public void setPacketBytes(char[] packet) {
		this.pipe.sendPacketBytes(packet);
	}
	
	@Override
	public void run() {
		while(true) {
			byte msg = (byte)this.message.get();
			pipe.tick(NetHooksHandler.this, msg);
			if(msg != 0) this.message.set(0);
		}
	}
	
	private class PipeHandler {
		
		public native boolean connect();
		public native void tick(NetHooksHandler callbackHandler, byte message);
		public native void sendPacketBytes(char[] bytes);
		
	}
	
}