package me.helldiner.holdon.gui.popup;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileSystemView;

import me.helldiner.holdon.hook.Injector;
import me.helldiner.holdon.hook.NetHooksHandler;
import me.helldiner.holdon.hook.NetHooksListener;
import me.helldiner.holdon.main.Main;
import me.helldiner.holdon.utils.Utils;

public class ProcessPickerWindow extends JFrame implements WindowListener {
	
	private static final long serialVersionUID = 1L;
	
	private static boolean Open = false;
	private JLabel clicked = null;
	private NetHooksListener hooksListener;

	public ProcessPickerWindow(NetHooksListener hooksListener) {
		if(!Open) {
			this.hooksListener = hooksListener;
			this.init();
			Open = true;
		}
	}
	
	private void init() {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setTitle("Attach process...");
		try {
			this.setIconImage(ImageIO.read(Utils.loadResource("./res/img/logo.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(new Dimension((int)(screenSize.getWidth()/7),(int)(screenSize.getHeight()/3)));
		this.setResizable(false);
		this.setAlwaysOnTop(true);
		this.setLocationRelativeTo(null);
		this.addWindowListener(this);
		this.setContentPane(this.initComponents());
		this.setVisible(true);
	}
	
	private JPanel initComponents() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(new Color(15,20,25));
		JLabel title = new JLabel("Pick a process :");
		title.setForeground(Color.WHITE);
		title.setFont(new Font("Arial",Font.PLAIN,16));
		title.setBounds(0, 0, getWidth(), getHeight()/10);
		title.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setVerticalAlignment(JLabel.CENTER);
		panel.add(title);
		this.initProcessList(panel);
		JButton button = new JButton("Attach");
		button.setForeground(Color.GREEN);
		button.setBounds(getWidth()/8, getHeight()-getHeight()/5-getHeight()/20, getWidth()-getWidth()/4, getHeight()/5-getHeight()/15);
		button.setBackground(new Color(35,40,45));
		button.setFont(new Font("Arial",Font.PLAIN,16));
		button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		button.setFocusPainted(false);
		button.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(clicked != null) {
					dispose();
					int pid = Integer.parseInt(clicked.getName());
					Injector injector = new Injector(pid);
					injector.inject("LetsHook");
					injector.inject("net_hooks");
					if(new NetHooksHandler(hooksListener).connectPipe()) {
						if(Main.DEBUG) System.out.println("Pipe synchronization : OK");
					} else if(Main.DEBUG) System.out.println("Pipe synchronization : ERROR");
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}
			
		});
		panel.add(button);
		return panel;
	}
	
	private void initProcessList(JPanel panel) {
		JPanel scrollContainer = new JPanel();
		scrollContainer.setLayout(new BoxLayout(scrollContainer,BoxLayout.Y_AXIS));
		this.displayProcesses(scrollContainer);
		JScrollPane scrollPane = new JScrollPane(scrollContainer);
		JScrollBar vertical = scrollPane.getVerticalScrollBar();
		vertical.addAdjustmentListener(new AdjustmentListener() {
			@Override
		    public void adjustmentValueChanged(AdjustmentEvent e) {  
		        e.getAdjustable().setValue(e.getAdjustable().getMaximum());
		        vertical.removeAdjustmentListener(this);
		    }
		});
		vertical.setUnitIncrement(20);
		scrollPane.setBounds(getWidth()/8, getHeight()/8, getWidth()-getWidth()/4, (int)(getHeight()-getHeight()/2.5));
		scrollContainer.setBackground(new Color(35,40,45));
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		panel.add(scrollPane);
	}
	
	private void displayProcesses(JPanel scrollContainer) {
		final MouseListener labelListener = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Component component = e.getComponent();
				if(component instanceof JLabel) {
					JLabel label = (JLabel) component;
					if(clicked == null || !clicked.getName().equals(label.getName())) {
						if(clicked != null) {
							clicked.setOpaque(false);
							clicked.repaint();
						}
						clicked = label;
						label.setOpaque(true);
						label.setBackground(new Color(5,100,200));
						label.repaint();
					}
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
		};
		for(String processStr : Injector.listProcesses()) {
			if(processStr.length() != 0) {
				if(processStr.contains("|")) {
					String processPath = processStr.substring(0,processStr.indexOf("|"));
					File file = new File(processPath);
					Icon icon = null;
					if(file.exists() && file.canRead()) {
						icon = FileSystemView.getFileSystemView().getSystemIcon(file);
					}
					if(processPath.contains("\\")) {
						String processName = processPath.substring(processPath.lastIndexOf("\\")+1);
						int pid = Integer.parseInt(processStr.substring(processStr.lastIndexOf("|")+1));
						JLabel label = new JLabel(processName+" - "+pid);
						label.setName(""+pid);
						label.setPreferredSize(new Dimension(scrollContainer.getWidth(),20));
						if(icon != null) label.setIcon(icon);
						label.setForeground(Color.WHITE);
						label.setFont(new Font("Calibri",Font.PLAIN,16));
						label.addMouseListener(labelListener);
						scrollContainer.add(label);
					}
				}
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {
		Open = false;
	}

	@Override
	public void windowClosing(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}
	
}
