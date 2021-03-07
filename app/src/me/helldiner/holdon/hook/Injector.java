package me.helldiner.holdon.hook;

import java.io.File;

import me.helldiner.holdon.main.Main;

public class Injector {
	
	private static final String HOOK_DIR = Main.LIB_DIR+"hook/";
	private int pid;
	
	public Injector(int pid) {
		this.pid = pid;
	}
	
	public boolean inject(String dllName) {
		File f = new File(HOOK_DIR+dllName+".dll");
		if(!f.exists()) return false;
		return this.inject(f.getAbsolutePath(), this.pid);
	}
	
	private native boolean inject(String dllPath, int pid);
	
}