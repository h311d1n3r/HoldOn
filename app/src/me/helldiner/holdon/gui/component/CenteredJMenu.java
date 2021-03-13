package me.helldiner.holdon.gui.component;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class CenteredJMenu extends JMenu implements MenuListener, MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	private Color defaultBackground = Color.WHITE, clickBackground = Color.WHITE;
	private boolean selected = false;

	public CenteredJMenu(String name) {
		super(name);
		this.addMenuListener(this);
		this.addMouseListener(this);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(this.getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(this.getForeground());
		g.setFont(this.getFont());
		FontMetrics metrics = g.getFontMetrics();
		g.drawString(this.getText(), this.getWidth()/2-metrics.stringWidth(this.getText())/2, this.getHeight()/2+metrics.getHeight()/4+3);
	}
	
	public void setBackground(Color color) {
		this.defaultBackground = color;
		super.setBackground(color);
		int r = color.getRed()+15, g = color.getGreen()+15, b = color.getBlue()+15;
		if(r > 255) r = 255;
		if(g > 255) g = 255;
		if(b > 255) b = 255;
		this.clickBackground = new Color(r,g,b);
	}

	@Override
	public void menuCanceled(MenuEvent e) {
		super.setBackground(this.defaultBackground);
		this.selected = false;
	}

	@Override
	public void menuDeselected(MenuEvent e) {
		super.setBackground(this.defaultBackground);
		this.selected = false;
	}

	@Override
	public void menuSelected(MenuEvent e) {
		super.setBackground(this.clickBackground);
		this.selected = true;
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {
		if(!this.selected) super.setBackground(this.clickBackground);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if(!this.selected) super.setBackground(this.defaultBackground);
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
	
}