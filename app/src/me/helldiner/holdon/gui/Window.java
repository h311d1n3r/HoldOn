package me.helldiner.holdon.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import me.helldiner.holdon.main.Main;
import me.helldiner.holdon.utils.Utils;

public class Window extends JFrame implements IWindow, WindowListener {
	
	private static final long serialVersionUID = 1L;
	private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
	
	private List<WindowStateListener> stateListeners = new ArrayList<WindowStateListener>();

	public Window() {
		this.init();
	}
	
	private void init() {
		UIManager.put("PopupMenu.border", new EmptyBorder(0,0,0,0));
		UIManager.put("MenuItem.selectionBackground", new Color(5,100,200));
		UIManager.put("MenuItem.selectionForeground", Color.WHITE);
		UIManager.put("Button.select", new Color(55,60,65));
		this.setTitle(Main.APP_NAME+" v"+Main.APP_VERSION+" - "+Main.AUTHOR_NAME);
		try {
			this.setIconImage(ImageIO.read(Utils.loadResource("./res/img/logo.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.addWindowListener(this);
		this.setVisible(true);
	}

	@Override
	public void addStateListener(WindowStateListener listener) {
		this.stateListeners.add(listener);
	}
	
	@Override
	public void setScreen(Container screen) {
		this.getContentPane().removeAll();
		this.setContentPane(screen);
		this.revalidate();
		this.repaint();
	}

	@Override
	public void setSize(float wFactor, float hFactor, boolean resizable) {
		if(wFactor == 1 && hFactor == 1) this.setExtendedState(MAXIMIZED_BOTH);
		else {
			this.setSize((int)(SCREEN_SIZE.getWidth()*wFactor), (int)(SCREEN_SIZE.getHeight()*hFactor));
			this.setLocationRelativeTo(null);
		}
		this.setResizable(resizable);
	}
	
	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		for(WindowStateListener listener : this.stateListeners) listener.onWindowClosing();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}
	
}