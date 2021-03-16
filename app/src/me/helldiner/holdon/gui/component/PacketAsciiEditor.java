package me.helldiner.holdon.gui.component;

import java.awt.Color;
import java.awt.Container;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PacketAsciiEditor extends PacketEditor {

	private static final long serialVersionUID = 1L;

	public PacketAsciiEditor(Container parent) {
		super("ASCII", parent);
		LimitedTextAreaDoc doc = new LimitedTextAreaDoc();
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
		super.textArea.setForeground(Color.MAGENTA);
		super.textArea.setCaretColor(Color.MAGENTA);
	}

	@Override
	public void setPacket(char[] packet) {
		String text = "";
		if(packet != null) text = new String(packet);
		LimitedTextAreaDoc doc = (LimitedTextAreaDoc) super.textArea.getDocument();
		doc.setLimit(text.length());
		super.textArea.setText(text);
	}
	
	@Override
	public char[] getPacket() {
		String text = super.textArea.getText();
		char[] packet = null;
		if(text != null) {
			return text.toCharArray();
		}
		return packet;
	}
	
}
