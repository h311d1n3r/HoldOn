package me.helldiner.holdon.gui.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import me.helldiner.holdon.gui.ScreenListener;
import me.helldiner.holdon.hook.NetHooksHandler;
import me.helldiner.holdon.utils.Utils;

public class PacketConsole extends JPanel implements ScreenListener {
	
	private static final long serialVersionUID = 1L;
	
	private Container parent;
	private JPanel packetsLoggerContainer;
	private NetHooksHandler netHooksHandler;
	private PacketEditor editor;
	private boolean isThreadPaused = false;
	private boolean areButtonsEnabled = true;

	public PacketConsole(Container parent) {
		this.parent = parent;
		this.setSize(parent.getWidth()/6, parent.getHeight()-52);
		this.setLayout(null);
		this.setBackground(new Color(30,35,40));
		this.initActionBar();
		this.initPacketLogger();
	}
	
	public void addPacket(String ip, boolean received) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int count = packetsLoggerContainer.getComponentCount();
				JPanel container = new JPanel();
				container.setBackground(null);
				container.setLayout(new BorderLayout());
				container.setBounds(0,count*30,packetsLoggerContainer.getWidth(), 30);
				JLabel ipLabel = new JLabel(ip);
				ipLabel.setForeground(Color.WHITE);
				ipLabel.setSize(container.getWidth()/2, getHeight());
				ipLabel.setFont(new Font("Arial", Font.BOLD, 20));
				ipLabel.setBorder(new EmptyBorder(5,10,2,0));
				container.add(ipLabel,BorderLayout.WEST);
				JLabel idLabel = new JLabel(""+(received?"RECEIVED":"SENT"));
				idLabel.setForeground(received?Color.RED:Color.GREEN);
				idLabel.setSize(container.getWidth()/2, getHeight());
				idLabel.setBorder(new EmptyBorder(5,0,2,10));
				idLabel.setFont(new Font("Arial", Font.BOLD, 20));
				container.add(idLabel,BorderLayout.EAST);
				packetsLoggerContainer.add(container);
				if((count+1) * container.getHeight() > packetsLoggerContainer.getHeight()) {
					packetsLoggerContainer.remove(0);
					for(Component component : packetsLoggerContainer.getComponents()) {
						component.setLocation(component.getX(), component.getY()-30);
					}
				}
				packetsLoggerContainer.revalidate();
				packetsLoggerContainer.repaint();
			}
		});
	}
	
	private void initActionBar() {
		JPanel bar = new JPanel();
		bar.setLayout(null);
		bar.setBounds(1, 1, this.getWidth()-2, 24);
		bar.setBorder(new MatteBorder(0,0,1,0,Color.BLACK));
		bar.setBackground(new Color(20,25,30));
		JButton playButton = this.initActionBarButton("play");
		playButton.setLocation(10, 2);
		bar.add(playButton);
		JButton stepButton = this.initActionBarButton("single_step");
		stepButton.setLocation(40, 2);
		bar.add(stepButton);
		JButton pauseButton = this.initActionBarButton("pause");
		pauseButton.setLocation(70, 2);
		bar.add(pauseButton);
		JButton stopButton = this.initActionBarButton("close");
		stopButton.setLocation(100, 2);
		bar.add(stopButton);
		this.add(bar);
	}
	
	private ActionListener actionBarButtonListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object component = e.getSource();
			if(component instanceof JButton && areButtonsEnabled) {
				JButton button = (JButton) component;
				switch(button.getName()) {
					case "play":
						if(netHooksHandler != null && editor != null && isThreadPaused) {
							if(editor.getPacket() != null) netHooksHandler.setPacketBytes(editor.getPacket());
							editor.setPacket(null);
							netHooksHandler.continueThread();
							isThreadPaused = false;
						}
						break;
					case "single_step":
						if(netHooksHandler != null && editor != null && isThreadPaused) {
							if(editor.getPacket() != null) netHooksHandler.setPacketBytes(editor.getPacket());
							editor.setPacket(null);
							netHooksHandler.singleStep();
							editor.setPacket("Loading packet...".toCharArray());
							isThreadPaused = true;
							areButtonsEnabled = false;
						}
						break;
					case "pause":
						if(netHooksHandler != null && !isThreadPaused) {
							netHooksHandler.pauseThread();
							editor.setPacket("Loading packet...".toCharArray());
							isThreadPaused = true;
							areButtonsEnabled = false;
						}
						break;
					case "close":
						break;
				}
			}
		}
	};
	
	public void onPacketReceived() {
		this.areButtonsEnabled = true;
	}
	
	private JButton initActionBarButton(String name) {
		String path = "./res/img/"+name+".png";
		Image icon = null;
		try {
			icon = new ImageIcon(ImageIO.read(Utils.loadResource(path))).getImage();
			icon = icon.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JButton button = new JButton(new ImageIcon(icon));
		button.setBorder(new EmptyBorder(0,0,0,0));
		button.setSize(20,20);
		button.setBackground(null);
		button.setFocusPainted(false);
		button.setName(name);
		button.addActionListener(this.actionBarButtonListener);
		return button;
	}
	
	private void initPacketLogger() {
		this.packetsLoggerContainer = new JPanel();
		this.packetsLoggerContainer.setBackground(new Color(12,20,25));
		this.packetsLoggerContainer.setLayout(null);
		this.packetsLoggerContainer.setBounds(1, 25, getWidth()-2, getHeight()-26);
		this.packetsLoggerContainer.setBorder(new EmptyBorder(0,0,0,0));
		this.add(this.packetsLoggerContainer);
	}

	public void setNetHooksHandler(NetHooksHandler handler) {
		this.netHooksHandler = handler;
	}
	
	@Override
	public void onScreenResized() {
		this.setSize(this.getWidth(), this.parent.getHeight()-52);
	}
	
	public void setPacketEditor(PacketEditor editor) {
		this.editor = editor;
	}
	
}
