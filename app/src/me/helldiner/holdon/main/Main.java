package me.helldiner.holdon.main;

import me.helldiner.holdon.gui.IWindow;
import me.helldiner.holdon.gui.Window;
import me.helldiner.holdon.load.AppLoader;

public class Main {

	public static final String APP_NAME = "HoldOn";
	public static final String APP_VERSION = "1.0";
	public static final String AUTHOR_NAME = "HellDiner";
	
	public static final String LIB_DIR = "./lib/";
	
	public static void main(String[] args) {
		IWindow window = new Window();
		new AppLoader(window);
	}

}