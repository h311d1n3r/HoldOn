package me.helldiner.holdon.gui.component;

import java.awt.Color;
import java.awt.Container;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PacketHexEditor extends PacketEditor {

	private static final long serialVersionUID = 1L;

	public PacketHexEditor(Container parent) {
		super("HEX", parent);
		HexTextAreaDoc doc = new HexTextAreaDoc();
		super.textArea.setDocument(doc);
		doc.addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				this.dispatchUpdate();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				this.dispatchUpdate();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				this.dispatchUpdate();
			}
			private void dispatchUpdate() {
				if(listener != null) {
					updateListener = true;
					listener.onPacketChanged(getPacket());
					updateListener = false;
				}
			}
		});
		super.textArea.setForeground(Color.CYAN);
		super.textArea.setCaretColor(Color.CYAN);
	}

	@Override
	public void setPacket(char[] packet) {
		String hexCode = "";
		for(int i = 0; i < packet.length; i++) {
			hexCode += Integer.toHexString(packet[i]);
			if(i != packet.length-1) hexCode += " ";
		}
		HexTextAreaDoc doc = (HexTextAreaDoc) super.textArea.getDocument();
		doc.setLimit(hexCode.length());
		doc.setFilterApplied(false);
		super.textArea.setText(hexCode);
		doc.setFilterApplied(true);
	}
	
	@Override
	public char[] getPacket() {
		String text = super.textArea.getText();
		char[] packet = null;
		if(text != null && !text.equals("")) {
			String[] codes = text.split(" ");
			packet = new char[codes.length];
			for(int i = 0; i < codes.length; i++) {
				if(!codes[i].equals("")) packet[i] = (char)Integer.parseInt(codes[i], 16);
				else packet[i] = 0;
			}
		}
		return packet;
	}

}
