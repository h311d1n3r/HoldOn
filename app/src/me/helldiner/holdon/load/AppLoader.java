package me.helldiner.holdon.load;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import me.helldiner.holdon.gui.IWindow;
import me.helldiner.holdon.gui.MainScreen;
import me.helldiner.holdon.main.Main;
import me.helldiner.holdon.utils.Utils;

public class AppLoader {
	
	private AppLoaderScreen screen;
	private Font font = this.initFont();
	private BufferedImage logo = this.initLogo();
	
	public AppLoader(IWindow window) {
		window.setSize(0.7f, 0.8f, false);
		this.screen = new AppLoaderScreen();
		window.setScreen(this.screen);
		this.init();
		new MainScreen(window);
	}
	
	private Font initFont() {
		try {
			InputStream stream = Utils.loadResource("./res/font/KarmaFuture.ttf");
			Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
			return font;
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private BufferedImage initLogo() {
		try {
			return ImageIO.read(Utils.loadResource("./res/img/logo.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void init() {
		this.loadLibrary("injector");
		this.loadLibrary("pipe_server");
	}
	
	private void loadLibrary(String name) {
		this.screen.setStatusMsg("Loading "+name+".dll");
		System.loadLibrary(Main.LIB_DIR+name);
	}
	
	private class AppLoaderScreen extends JPanel {

		private String statusMsg = "Loading...";
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(new Color(30,35,40));
			g.fillRect(0, 0, getWidth(), getHeight());
			int radius = this.getWidth()/8;
			g.drawImage(logo, this.getWidth()/2-radius, this.getHeight()/2-radius, radius*2, radius*2, null);
			g.setColor(Color.WHITE);
			Font f = font.deriveFont(this.getHeight()/17f);
			g.setFont(f);
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(this.statusMsg, this.getWidth()/2-metrics.stringWidth(this.statusMsg)/2, this.getHeight()/8*7+metrics.getHeight()/4);
		}
		
		public void setStatusMsg(String statusMsg) {
			this.statusMsg = statusMsg;
			this.repaint();
		}
		
	}
	
}