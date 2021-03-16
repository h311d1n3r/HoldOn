package me.helldiner.holdon.gui.component;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitedTextAreaDoc extends PlainDocument {
	
	private int limit = 0;

	private static final long serialVersionUID = 1L;

	@Override
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
	    if (str == null)
	      return;
	    if ((this.getLength() + str.length()) <= this.limit) {
	      super.insertString(offset, str, attr);
	    }
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
}
