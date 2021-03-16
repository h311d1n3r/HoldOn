package me.helldiner.holdon.gui.component;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

public class HexTextAreaDoc extends LimitedTextAreaDoc {

	private static final long serialVersionUID = 1L;
	private boolean filterApplied = true;
	
	@Override
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		if(!this.filterApplied) super.insertString(offset, str, attr);
		else if (str != null) {
		    if (str.matches("[0-9a-fA-F]")) {
		      super.insertString(offset, str.toLowerCase(), attr);
		    }
	    }
	}
	
	@Override
	public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
		if(!this.filterApplied) super.replace(offset, length, text, attrs);
		else {
			String oldTxt = this.getText(offset, length);
			if(oldTxt != null) {
				if(!oldTxt.contains(" ")) {
					super.replace(offset, length, text, attrs);
				}
			}
		}
	}
	
	@Override
	public void remove(int offs, int len) throws BadLocationException {
		if(!this.filterApplied) super.remove(offs, len);
		else {
			String oldText = this.getText(offs, len);
			if(oldText != null) {
				if(!oldText.contains(" ")) {
					super.remove(offs, len);
				}
			}
		}
	}
	
	public void setFilterApplied(boolean applied) {
		this.filterApplied = applied;
	}
	
}
