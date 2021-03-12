package me.helldiner.holdon.load;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import me.helldiner.holdon.gui.IWindow;
import me.helldiner.holdon.hook.Injector;
import me.helldiner.holdon.hook.NetHooksHandler;
import me.helldiner.holdon.main.Main;

public class AppLoader {
	
	public AppLoader(IWindow window) {
		window.setScreen(new AppLoaderScreen());
		/* TEMP */
		this.loadLibrary("injector");
		Injector injector = new Injector(11108);
		injector.inject("LetsHook");
		injector.inject("net_hooks");
		this.loadLibrary("pipe_server");
		if(new NetHooksHandler().connectPipe()) {
			if(Main.DEBUG) System.out.println("Pipe synchronization : OK");
		} else if(Main.DEBUG) System.out.println("Pipe synchronization : ERROR");
		/********/
	}
	
	private void loadLibrary(String name) {
		System.loadLibrary(Main.LIB_DIR+name);
	}
	
	private class AppLoaderScreen extends JPanel {

		private static final long serialVersionUID = 1L;
		
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
	}
	
}