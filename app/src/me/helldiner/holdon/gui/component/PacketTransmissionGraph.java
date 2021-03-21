package me.helldiner.holdon.gui.component;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;

import javax.swing.JPanel;

import me.helldiner.holdon.gui.ScreenListener;

public class PacketTransmissionGraph extends JPanel implements ScreenListener {

	private static final long serialVersionUID = 1L;
	private static final int MAX_SECONDS = 60;
	private static final int Y_OFF = 3;
	
	private Container parent;
	private int[] packets = new int[MAX_SECONDS];
	private int packetsAmount = 0;
	private long lastTime = 0L;

	public PacketTransmissionGraph(Container parent) {
		this.parent = parent;
		this.setSize(parent.getWidth()/6, (parent.getHeight()-52)/3);
	}
	
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
		g.setColor(new Color(30,35,40));
		for(int i = 0; i < 5; i++) {
			g.drawLine((getWidth()/6)*(i+1), 0, (getWidth()/6)*(i+1), getHeight());
		}
		for(int i = 0; i < 5; i++) {
			g.drawLine(0, (int)((getHeight()-getHeight()/4)/5D*(i+1)), getWidth(), (int)((getHeight()-getHeight()/4)/5D*(i+1)));
		}
		g.setColor(Color.GREEN);
		for(int i = 0; i < this.packetsAmount; i++) {
			int y = getHeight()-getHeight()/4-(Y_OFF*this.packets[i]);
			int oldY = i-1>=0?getHeight()-getHeight()/4-(Y_OFF*this.packets[i-1]):y;
			g.drawLine((int)((i-1>=0?i-1:i)*(getWidth()/(double)(this.packets.length-1))),
					oldY,
					(int)(i*(getWidth()/(double)(this.packets.length-1))),
					y);
		}
		this.repaint();
	}
	
	@Override
	public void onScreenResized() {
		this.setSize(this.parent.getWidth()/15*4, (this.parent.getHeight()-52)/2);
	}

}