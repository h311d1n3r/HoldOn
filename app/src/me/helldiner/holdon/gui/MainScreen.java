package me.helldiner.holdon.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import me.helldiner.holdon.gui.component.CenteredJMenu;
import me.helldiner.holdon.gui.popup.ProcessPickerWindow;

public class MainScreen extends JPanel {

	private static final long serialVersionUID = 1L;

	public MainScreen(IWindow window) {
		window.setSize(1f, 1f, true);
		window.setScreen(this);
		UIManager.put("PopupMenu.border", new EmptyBorder(0,0,0,0));
		UIManager.put("MenuItem.selectionBackground", new Color(5,100,200));
		UIManager.put("MenuItem.selectionForeground", Color.WHITE);
		this.setLayout(null);
		this.setBackground(new Color(35,40,45));
		this.init();
	}
	
	private void init() {
		this.initMenuBar();
	}
	
	private void initMenuBar() {
		JMenuBar bar = new JMenuBar();
		bar.setBackground(new Color(30,35,40));
		bar.setBounds(0,0,getWidth(),30);
		bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		bar.add(this.initMenu("File", new String[]{"Attach process..."},135));
		bar.add(this.initMenu("Edit", new String[]{"Settings"},80));
		bar.add(this.initMenu("Packet", new String[]{"Run","Pause","Stop"},66));
		this.add(bar);
	}
	
	private JMenu initMenu(String name, String[] items, int width) {
		final Font font = new Font("Arial",Font.BOLD,15);
		CenteredJMenu menu = new CenteredJMenu(name);
		menu.setBackground(new Color(30,35,40));
		menu.setBorder(null);
		menu.setPreferredSize(new Dimension(70,0));
		menu.setForeground(new Color(200,200,200));
		menu.setFont(font);
		for(String itemText : items) {
			JMenuItem item = new JMenuItem(itemText);
			item.setBackground(new Color(45,50,55));
			item.setFont(font.deriveFont(13f));
			item.setForeground(new Color(200,200,200));
			item.setPreferredSize(new Dimension(width, 30));
			item.setBorder(new EmptyBorder(0,10,0,0));
			item.setName(name+"|"+itemText);
			item.addActionListener(this.menuItemListener);
			menu.add(item);
		}
		return menu;
	}
	
	private ActionListener menuItemListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object component = e.getSource();
			if(component instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem) component;
				String menuItemName = menuItem.getName();
				switch(menuItemName) {
					case "File|Attach process...":
						new ProcessPickerWindow();
					break;
					default:
				}
			}
		}
	};
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.repaintAll(this);
	}
	
	private void repaintAll(Component comp) {
		SwingUtilities.updateComponentTreeUI(this);
		if(comp instanceof Container) {
			for(Component component : ((Container)comp).getComponents()) {
				this.repaintAll(component);
			}
		}
	}
	
}
