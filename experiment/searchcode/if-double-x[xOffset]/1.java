/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
package ru.adv.web.captcha.text.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.captcha.text.renderer.WordRenderer;

public class AdvWordRenderer implements WordRenderer {

	private static final List<Color> DEFAULT_COLORS = new ArrayList<Color>();
	private static final List<Font> DEFAULT_FONTS = new ArrayList<Font>();
	private static final float DEFAULT_STROKE_WIDTH = 0.5f;
	// The text will be rendered 25%/5% of the image height/width from the X and Y axes
	private static double YOFFSET = 0.25;
	private static double XOFFSET = 0.05;
	
	private final List<Font> _fonts;
	private final List<Color> _colors;
	private final float _strokeWidth;
	
	static {
		DEFAULT_FONTS.add(new Font("Arial", Font.BOLD, 40));
		DEFAULT_COLORS.add(Color.BLUE);
	}

	public AdvWordRenderer() {
		this(DEFAULT_COLORS, DEFAULT_FONTS, DEFAULT_STROKE_WIDTH);
	}
	
	public AdvWordRenderer(List<Color> colors, List<Font> fonts) {
		this(colors, fonts, DEFAULT_STROKE_WIDTH);
	}
	
	public AdvWordRenderer(List<Color> colors, List<Font> fonts, float strokeWidth) {
		_colors = colors != null ? colors : DEFAULT_COLORS;
		_fonts = fonts != null ? fonts : DEFAULT_FONTS;
		_strokeWidth = strokeWidth < 0 ? DEFAULT_STROKE_WIDTH : strokeWidth;
	}
	
	public void setX (double X) {
		XOFFSET = X;
	}
	
	public void setY (double Y) {
		YOFFSET = Y;
	}

	@Override
	public void render(final String word, BufferedImage image) {
		Graphics2D g = image.createGraphics();
		
        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));
        g.setRenderingHints(hints);

        
		g.setStroke(new BasicStroke(_strokeWidth));
		FontRenderContext frc = g.getFontRenderContext();
        int startPos = (int) Math.round(image.getWidth() * XOFFSET);
		
        for (int i=0,len=word.length();i<len;i++) {
        	Font font = getRandomFont();
            g.setFont(font);
        	g.setColor(getRandomColor());
        	
            GlyphVector gv = font.createGlyphVector(frc, new char[]{word.charAt(i)});
            double charWidth = gv.getVisualBounds().getWidth();
            
            int xBaseline = startPos;
            int yBaseline = image.getHeight() - (int) Math.round(image.getHeight() * YOFFSET);
            
            g.drawChars(new char[]{word.charAt(i)}, 0, 1, xBaseline, yBaseline);
            startPos = xBaseline + (int)charWidth;
        }
        
	}
	
	private Color getRandomColor() {
		return (Color) getRandomObject(_colors);
	}
	
	private Font getRandomFont() {
		return (Font) getRandomObject(_fonts);
	}
	
	private Object getRandomObject(List<? extends Object> objs) {
		if (objs.size() == 1) {
			return objs.get(0);
		}
		
		Random gen = new SecureRandom();
		int i = gen.nextInt(objs.size());
		return objs.get(i);
	}
}



