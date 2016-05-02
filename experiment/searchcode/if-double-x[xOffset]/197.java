package roboflight.ui.etc;

import java.awt.Color;
import java.awt.Graphics2D;

public class Text2D {
	public static final int LEFT_ALIGN = 1;
	public static final int CENTER_ALIGN = 0;
	public static final int RIGHT_ALIGN = 2;
	public static final int BOTTOM_ALIGN = 1;
	public static final int TOP_ALIGN = 2;
	public double x, y;
	public int xoffset, yoffset;
	public int vAlign, hAlign;
	public Color color;
	public String text;

	public Text2D() {
		color = Color.WHITE;
		x = y = 0;
		vAlign = hAlign = 0;
		xoffset = yoffset = 0;
	}

	public Text2D(String text) {
		color = Color.WHITE;
		x = y = 0;
		vAlign = hAlign = 0;
		xoffset = yoffset = 0;
		this.text = text;
	}

	public Text2D(String text, double[] pos) {
		color = Color.WHITE;
		setLocation(pos);
		vAlign = hAlign = 0;
		xoffset = yoffset = 0;
		this.text = text;
	}

	public void setLocation(double[] pos) {
		x = pos[0];
		y = pos[1];
	}

	public void setAlignment(int horizontal, int vertical) {
		hAlign = horizontal;
		vAlign = vertical;
	}
	
	public void draw(Graphics2D g, int sw, int sh) {
		double x, y;
		x = y = 0;
		x = this.x;
		y = sh - this.y;
		
		g.setColor(color);
		double w = g.getFontMetrics().stringWidth(text);
		double h = g.getFontMetrics().getHeight();
		
		if(hAlign == RIGHT_ALIGN)
    		x -= w;
    	else if(hAlign == CENTER_ALIGN)
    		x -= w / 2.0;
    
    	if(hAlign == TOP_ALIGN)
    		y -= h;
    	else if(hAlign == CENTER_ALIGN)
    		y -= h / 2.0;
    	
    	g.drawString(text, (float)x + xoffset, (float)y - yoffset);
	}
}

