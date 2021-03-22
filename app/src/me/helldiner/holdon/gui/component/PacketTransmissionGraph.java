package me.helldiner.holdon.gui.component;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import me.helldiner.holdon.gui.ScreenListener;

public class PacketTransmissionGraph extends JPanel implements ScreenListener {

	private static final long serialVersionUID = 1L;
	
	private Container parent;
	private PacketTransmissionGraphPanel panel;
	
	public PacketTransmissionGraph(Container parent) {
		this.parent = parent;
		this.setLayout(null);
		this.setSize(parent.getWidth()/6, (parent.getHeight()-52)/3);
		this.setBackground(new Color(30,35,40));
		this.initTitleBar();
		this.initPacketTransmissionGraphPanel();
	}
	
	private void initTitleBar() {
		JLabel bar = new JLabel("Packets transmitted");
		bar.setBounds(1, 1, this.getWidth()-2, 24);
		bar.setBorder(new EmptyBorder(3,5,0,0));
		bar.setBackground(new Color(20,25,30));
		bar.setForeground(Color.WHITE);
		bar.setFont(new Font("Calibri", Font.PLAIN, 15));
		this.add(bar);
	}
	
	private void initPacketTransmissionGraphPanel() {
		this.panel = new PacketTransmissionGraphPanel();
		this.panel.setBounds(1, 25, this.getWidth()-2, this.getHeight()-26);
		this.panel.setBorder(new MatteBorder(1,0,0,0,Color.BLACK));
		this.add(panel);
	}
	
	public void onPacketReceived() {
		this.panel.onPacketReceived();
	}
	
	@Override
	public void onScreenResized() {
		this.setSize(this.getWidth(), (this.parent.getHeight()-52)/3);
	}

	private class PacketTransmissionGraphPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		private static final int MAX_SECONDS = 60;
		private static final int Y_OFF = 3;
	
		private int[] packets = new int[MAX_SECONDS];
		private int packetsAmount = 0;
		private long lastTime = 0L;
		
		private void updateTime(boolean packetReceived) {
			long currentTime = System.currentTimeMillis();
			long currentSec = currentTime/1000;
			long lastSec = this.lastTime/1000;
			if(this.packetsAmount == 0) {
				if(packetReceived) {
					this.packetsAmount++;
					this.lastTime = currentTime;
				}
			} else {
				int secDiff = (int)(currentSec - lastSec);
				this.packetsAmount += secDiff;
				if(this.packetsAmount >= this.packets.length) {
					int[] newPackets = new int[this.packets.length];
					if(secDiff < this.packets.length) System.arraycopy(this.packets, secDiff, newPackets, 
							0, this.packets.length-secDiff);
					this.packets = newPackets;
					this.packetsAmount = this.packets.length;
				}
				this.lastTime+=secDiff*1000;
			}
		}
		
		public void onPacketReceived() {
			this.updateTime(true);
			this.packets[this.packetsAmount-1]++;
		}
		
		@Override
		public void paintComponent(Graphics g) {
			this.updateTime(false);
			g.setColor(new Color(12,20,25));
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setFont(new Font("Calibri",Font.PLAIN,15));
			FontMetrics metrics = g.getFontMetrics();
			g.setColor(new Color(30,35,40));
			for(int i = 0; i < 5; i++) {
				g.drawLine((getWidth()/6)*(i+1), 0, (getWidth()/6)*(i+1), getHeight());
				g.drawLine(0, (int)((getHeight()-getHeight()/6)/5D*(i+1)), getWidth(), (int)((getHeight()-getHeight()/6)/5D*(i+1)));
			}
			g.setColor(Color.GREEN);
			for(int i = 0; i < this.packetsAmount; i++) {
				int y = getHeight()-getHeight()/6-(Y_OFF*this.packets[i]);
				int oldY = i-1>=0?getHeight()-getHeight()/6-(Y_OFF*this.packets[i-1]):y;
				g.drawLine((int)((i-1>=0?i-1:i)*(getWidth()/(double)(this.packets.length-1))),
						oldY,
						(int)(i*(getWidth()/(double)(this.packets.length-1))),
						y);
			}
			g.setColor(Color.WHITE);
			for(int i = 0; i < 5; i++) {
				g.drawString(""+((i+1)*this.packets.length/6), 4+(getWidth()/6)*(i+1), getHeight()-getHeight()/6+metrics.getHeight()-5);
				g.drawString(""+((4-i)*getHeight()/6/Y_OFF), 2, (getHeight()/6)*(i+1)-2);
			}
			this.repaint();
		}
	}
	
}