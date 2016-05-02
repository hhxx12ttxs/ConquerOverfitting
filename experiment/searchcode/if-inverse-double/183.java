/**
 * Copyright (C) 2010  Steve Coleman-Williams
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.scw.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.BoundedRangeModel;
import javax.swing.JProgressBar;

public class Slider extends JProgressBar {
	private boolean positive = true;
	
	private Color borderColour = Color.BLACK;
	private Color colour = Color.GREEN;
	private Color neutralColour = borderColour;
	private Color fadeColour = this.getBackground();
	
	private boolean fade = false;

	public Slider(boolean positive) {
		super();
		this.positive = positive;
	}

	public Slider(BoundedRangeModel newModel, boolean positive) {
		super(newModel);
		this.positive = positive;
	}

	public Slider(int orient, int min, int max, boolean positive) {
		super(orient, min, max);
		this.positive = positive;
	}

	public Slider(int min, int max, boolean positive) {
		super(min, max);
		this.positive = positive;
	}

	public Slider(int orient, boolean positive) {
		super(orient);
		this.positive = positive;
	}

	public void paint(Graphics g) {		
		if(this.getOrientation() == JProgressBar.VERTICAL) {
			if(this.positive) {
				// bottom to top				
				if(this.getValue() > this.getMinimum() && this.getValue() <= this.getMaximum()) {
					int range = this.getMaximum() - this.getMinimum();
					double percent = (Math.abs(this.getValue())/(double)range);		
					int y = (int)(this.getHeight() * percent);
					int height = this.getHeight();
					int barHeight = this.getHeight() - y;
					
					g.setColor(this.colour);
					if(!fade) {						
						g.fillRect(0, barHeight, this.getWidth(), this.getHeight() - barHeight);
					}
					else {
						fade(g, 0, barHeight, true, true);
					}
				}
				else {
					g.setColor(this.neutralColour);
					g.drawLine(0, this.getHeight() -1, this.getWidth(), this.getHeight() -1);
				}
				
				g.setColor(this.borderColour);
				g.drawRect(0, 0, this.getWidth() - 1, this.getHeight());
			}
			else {
				// top to bottom
				if(this.getValue() >= this.getMinimum() && this.getValue() < this.getMaximum()) {					
					int range = this.getMaximum() - this.getMinimum();
					double percent = (Math.abs(this.getValue())/(double)range);		
					int y = (int)(this.getHeight() * percent);
					int height = this.getHeight();
					int barHeight = this.getHeight() - y;
					
					g.setColor(this.colour);
					if(!fade) {
						g.fillRect(0, 0, this.getWidth(), this.getHeight());
						
						g.setColor(this.getForeground());
						g.fillRect(0, this.getHeight() - barHeight, this.getWidth() - 1, barHeight);
					}
					else {
						fade(g, 0, barHeight, true, false);
					}
				}
				else {
					g.setColor(this.neutralColour);
					g.drawLine(0, 0, this.getWidth(), 0);
				}
				
				g.setColor(this.borderColour);
				g.drawRect(0, -1, this.getWidth() - 1, this.getHeight() -1);
			}
		}
		else {
			if(this.positive) {
				// left to right
				if(this.getValue() > this.getMinimum() && this.getValue() <= this.getMaximum()) {
					int range = this.getMaximum() - this.getMinimum();
					double percent = (Math.abs(this.getValue())/(double)range);		
					int x = (int)(this.getWidth() * percent);
					int width = this.getWidth();
					int barWidth = x;
					
					g.setColor(this.colour);
					if(!fade) {
						g.fillRect(0, 0, barWidth, this.getHeight());
					}
					else {
						fade(g, 0, barWidth, false, true);
					}
				}
				else {
					g.setColor(this.neutralColour);
					g.drawLine(0, 0, 0, this.getHeight());
				}
				
				g.setColor(this.borderColour);
				g.drawRect(-1, 0, this.getWidth() -1, this.getHeight() -1);
			}
			else {
				// right to left				
				if(this.getValue() >= this.getMinimum() && this.getValue() < this.getMaximum()) {
					int range = this.getMaximum() - this.getMinimum();
					double percent = (Math.abs(this.getValue())/(double)range);		
					int x = (int)(this.getWidth() * percent);
					int width = this.getWidth();
					int barWidth = this.getWidth() - x;
					
					g.setColor(this.colour);
					if(!fade) {
						g.fillRect(0, 0, width, this.getHeight());
						
						g.setColor(this.getForeground());
						g.fillRect(0, 0, barWidth, this.getHeight());
					}
					else {
						fade(g, 0, barWidth, false, false);
					}
				}
				else {
					g.setColor(this.neutralColour);
					g.drawLine(this.getWidth() -1, 0, this.getWidth() -1, this.getHeight());
				}
				
				g.setColor(this.borderColour);
				g.drawRect(0, 0, this.getWidth(), this.getHeight()-1);
			}
		}
	}
	
	private void fade(Graphics g, int start, int end, boolean vertical, boolean inverse) {
		if(vertical) {
			if(inverse) {
				Graphics2D g2 = (Graphics2D)g;
			    Rectangle2D bar = new Rectangle2D.Double(0, end, this.getWidth(), this.getHeight() - end);
			    
			    GradientPaint gp = new GradientPaint(0, (int)(this.getWidth() / 2), this.fadeColour, (int)(this.getWidth() / 2), (int)this.getHeight(), g.getColor());
		        g2.setPaint(gp);
		        g2.fill(bar);


				//g.fillRect(0, end, this.getWidth(), this.getHeight() - end);
			}
			else {
				Graphics2D g2 = (Graphics2D)g;
				Rectangle2D bar = new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight() - end);
			    
			    GradientPaint gp = new GradientPaint(0, (int)(this.getWidth() / 2), g.getColor(), (int)(this.getWidth() / 2), (int)this.getHeight(), this.fadeColour);
		        g2.setPaint(gp);
		        g2.fill(bar);
			}
		}
		else {
			if(inverse) {
				Graphics2D g2 = (Graphics2D)g;
				Rectangle2D bar = new Rectangle2D.Double(0, 0, end, this.getHeight());
			    
			    GradientPaint gp = new GradientPaint(0, (int)(this.getHeight() / 2), g.getColor(), (int)this.getWidth(), (int)(this.getHeight() / 2), this.fadeColour);
		        g2.setPaint(gp);
		        g2.fill(bar);
			}
			else {
				Graphics2D g2 = (Graphics2D)g;
				Rectangle2D bar = new Rectangle2D.Double(end, 0, this.getWidth() - 1, this.getHeight());
			    
			    GradientPaint gp = new GradientPaint(this.getWidth(), (int)(this.getHeight() / 2), g.getColor(), 0, (int)(this.getHeight() / 2), this.fadeColour);
		        g2.setPaint(gp);
		        g2.fill(bar);
			}
		}
	}

	public Color getBorderColour() {
		return borderColour;
	}

	public void setBorderColour(Color borderColour) {
		this.borderColour = borderColour;
	}

	public Color getColour() {
		return colour;
	}

	public void setColour(Color colour) {
		this.colour = colour;
	}
	
	public Color getNeutralColour() {
		return neutralColour;
	}

	public void setNeutralColour(Color neutralColour) {
		this.neutralColour = neutralColour;
	}
	
	public void setFade(boolean fade) {
		this.fade = fade;
	}

	public Color getFadeColour() {
		return fadeColour;
	}

	public void setFadeColour(Color fadeColour) {
		this.fadeColour = fadeColour;
	}

}
