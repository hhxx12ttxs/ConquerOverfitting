package com.jsm.chloride.helper;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DecimalNumberFilter extends DocumentFilter {
	int numDecimals = 0;

	@Override
	public void replace(FilterBypass fb, int offset, int length, String text,
			AttributeSet attrs) {
		System.out.println(text);
		if (text.matches("[0-9]") || (text.contains(".") && numDecimals < 1)) {
			try {
				if (text.contains(".")) {
					numDecimals++;
				}
				super.replace(fb, offset, length, text, attrs);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

}

