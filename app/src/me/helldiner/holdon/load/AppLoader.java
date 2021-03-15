package me.helldiner.holdon.load;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	private Font helldinerFont, loadingFont;
	private BufferedImage helldiner,logo;
	private IWindow window;
	
	public AppLoader(IWindow window) {
		this.window = window;
		this.initImages();
		this.initFonts();
		window.setSize(0.7f, 0.8f, false);
		window.setScreen(new LogoScreen());
	}
	
	private void startLoadingPhase() {
		this.screen = new AppLoaderScreen();
		this.window.setScreen(this.screen);
		this.loadNativeLibraries();
		new MainScreen(this.window);
	}
	
	private void initFonts() {
		try {
			InputStream stream = Utils.loadResource("./res/font/Prince Valiant.ttf");
			this.helldinerFont = Font.createFont(Font.TRUETYPE_FONT, stream);
			stream = Utils.loadResource("./res/font/KarmaFuture.ttf");
			this.loadingFont = Font.createFont(Font.TRUETYPE_FONT, stream);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initImages() {
		try {
			this.helldiner = ImageIO.read(Utils.loadResource("./res/img/helldiner.png"));
			this.logo = ImageIO.read(Utils.loadResource("./res/img/logo.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadNativeLibraries() {
		this.loadLibrary("injector");
		this.loadLibrary("pipe_server");
	}
	
	private void loadLibrary(String name) {
		this.screen.setStatusMsg("Loading "+name+".dll");
		System.loadLibrary(Main.LIB_DIR+name);
	}
	
	private class LogoScreen extends JPanel {
		
		private static final long serialVersionUID = 1L;
		private long startTime;
		
		public LogoScreen() {
			this.startTime = System.currentTimeMillis();
		}
		
		@Override
		public void paintComponent(Graphics g) {
			long time = System.currentTimeMillis()-this.startTime;
			if(time < 3000) {
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
				int radius = this.getWidth()/8;
				if(time >= 1500) ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (3000-time)/1500f));
				if(time >= 750) {
					Font f = helldinerFont.deriveFont(this.getHeight()/17f);
					g.setFont(f);
					FontMetrics metrics = g.getFontMetrics();
					final int totalWidth = metrics.stringWidth("Designed by HellDiner");
					final String[] helldinerStrings = new String[]{"Designed ","by ","HellDiner"};
					final Color[] helldinerColors = new Color[]{Color.BLUE,Color.WHITE,Color.RED};
					int xOff = 0;
					for(int i = 0; i < helldinerStrings.length; i++) {
						String helldinerStr = helldinerStrings[i];
						int width = metrics.stringWidth(helldinerStr);
						g.setColor(helldinerColors[i]);
						g.drawString(helldinerStr, getWidth()/2-totalWidth/2+xOff, getHeight()/5*4+metrics.getHeight()/4);
						xOff += width;
					}
					g.drawImage(helldiner, this.getWidth()/2-radius, this.getHeight()/2-radius, radius*2, radius*2, null);
				}
				repaint();
			} else if(time < 3250) {
				float coeff = (time-3000)/250f;
				g.setColor(new Color((int)(20*coeff),(int)(25*coeff),(int)(30*coeff)));
				g.fillRect(0, 0, getWidth(), getHeight());
				repaint();
			} else startLoadingPhase();
		}
		
	}
	
	private class AppLoaderScreen extends JPanel {

		private String statusMsg = "Loading...";
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(new Color(20,25,30));
			g.fillRect(0, 0, getWidth(), getHeight());
			int radius = this.getWidth()/8;
			g.drawImage(logo, this.getWidth()/2-radius, this.getHeight()/2-radius, radius*2, radius*2, null);
			g.setColor(Color.WHITE);
			Font f = loadingFont.deriveFont(this.getHeight()/17f);
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