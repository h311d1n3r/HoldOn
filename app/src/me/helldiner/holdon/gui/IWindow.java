package me.helldiner.holdon.gui;

import java.awt.Container;

public interface IWindow {

	public void addStateListener(WindowStateListener listener);
	public void setScreen(Container screen);
	public void setSize(float wFactor, float hFactor, boolean resizable);
	
}
