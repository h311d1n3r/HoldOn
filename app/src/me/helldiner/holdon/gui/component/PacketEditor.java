package me.helldiner.holdon.gui.component;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import me.helldiner.holdon.gui.ScreenListener;

abstract class PacketEditor extends JPanel implements ScreenListener, ITextAreaListener {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Container parent;
	protected JTextArea textArea;
	protected ITextAreaListener listener;
	protected boolean updateListener = false;
	
	public PacketEditor(String name, Container parent) {
		this.name = name;
		this.parent = parent;
		this.setSize(parent.getWidth()/15*4, parent.getHeight()-52);
		this.setLayout(null);
		this.setBackground(new Color(30,35,40));
		this.initTitleBar();
		this.initTextField();
	}
	
	private void initTitleBar() {
		JLabel bar = new JLabel(this.name);
		bar.setBounds(1, 1, this.getWidth()-2, 24);
		bar.setBorder(new EmptyBorder(3,5,0,0));
		bar.setBackground(new Color(20,25,30));
		bar.setForeground(Color.WHITE);
		bar.setFont(new Font("Calibri", Font.PLAIN, 15));
		this.add(bar);
	}
	
	private void initTextField() {
		this.textArea = new JTextArea();
		this.textArea.setBackground(new Color(12,20,25));
		this.textArea.setBorder(new EmptyBorder(3,3,3,3));
		this.textArea.setFont(new Font("Courier New", Font.PLAIN, 20));
		this.textArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(this.textArea);
		scrollPane.setBounds(1, 25, getWidth()-2, getHeight()-26);
		scrollPane.setBorder(new MatteBorder(1,0,0,0,Color.BLACK));
		this.add(scrollPane);
	}
	
	public void setTextAreaListener(ITextAreaListener listener) {
		this.listener = listener;
	}
	
	public abstract void setPacket(char[] packet);
	
	public abstract char[] getPacket();
	
	@Override
	public void onScreenResized() {
		this.setSize(this.getWidth(), this.parent.getHeight()-52);
	}
	
	public void onPacketChanged(char[] packet) {
		if(!updateListener) this.setPacket(packet);
	}
	
}