package com.lab111.labworkS4L2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

/**
 * Class responsible for drawing a diagram, Realizes Strategy pattern
 * @author wizzardich
 *
 */
public class BarDiagramDrawer implements Drawer {
	//Fields
	private Dimension canvasSize;
	private String[][] data;
	public double scale = Double.NaN;
	/**
	 * Constructor. Specifies the size of canvas
	 */
	public BarDiagramDrawer(int x,int y){
		canvasSize = new Dimension(x,y);
	}
	/**
	 * Implemented method from Drawer interface. Draws the Diagram
	 * @param g graphical context
	 */
	public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Arial", Font.BOLD, 13));
		double width = canvasSize.width/(data.length+1);
		
		//Looking for max element among data and parsing data to doubles
		double[] sizes = new double[data.length];	
		double max = 0;
		for(int i = 0;i<data.length;i++){
			double s = 0;
			s = Double.parseDouble(data[i][1]);
			if (s>max) max = s;
			sizes[i] = s;
		}
		
		//Drawing the grid
		max = createGrid(g2,max);
		
		//Cooking the pattern for filling
		GradientPaint fillingColor = new GradientPaint(0,0,Color.DARK_GRAY,canvasSize.width, 0,Color.LIGHT_GRAY);
		g2.setColor(Color.BLACK);
		
		//Drawing bar diagram itself
		for(int i = 0;i<sizes.length;i++){
			sizes[i] = sizes[i]/max;
			
			//Drawing rectangle
			Rectangle barOne = new Rectangle((int)(width*(i+0.5)+3),(int)((1-sizes[i])*canvasSize.height),(int)(width-3),(int)((sizes[i])*canvasSize.height));
			g2.draw(barOne);
			
			//Filling it with gradient paint
			g2.setPaint(fillingColor);
			g2.fill(barOne);
			
			//Returning back to the actual color
			g2.setColor(Color.BLACK);
			
			//Finding out where should we put the names of the bars and, actually, doing it
			double y = canvasSize.height;
			if (sizes[i]<0.5){
				y = (1-sizes[i])*canvasSize.height;
			}
			drawStringUp(g2, data[i][0], ((width)*(i+1)+3), y-2);
		}
	}
	
	/**
	 * Writes message vertically 
	 * @param g2 graphical context
	 * @param message a string to draw
	 * @param x x-coordinate of origin
	 * @param y y-coordinate of origin
	 */
	private void drawStringUp(Graphics2D g2, String message, double x, double y){
		//Cooking the Affine transform to rotate damn text
		AffineTransform at = new AffineTransform();
		at.rotate(-Math.PI/2);
		
		//Saving the old font and cooking new one
		Font in = g2.getFont();
		Font vertical = in.deriveFont(at);
		
		//Drawing and returning to the actual font
		g2.setFont(vertical);
		g2.drawString(message, (float)x, (float)y);
		g2.setFont(in);
	}
	/**
	 * Creates grid with selected scale
	 * @param g2 context graphical
	 * @param max maximum element of dah array
	 * @return
	 */
	private double createGrid(Graphics2D g2, double max){
		//Incrementing max (so we see the top) and saving old font
		max += 0.1*max;
		autoScale(max);
		Font in = g2.getFont();
		
		//Defining how much lines will be in the grid; their distance and length
		double n = (max/scale);
		double height = canvasSize.height/n;
		double width = canvasSize.width;
		
		//Drawing the lines and subscribing them
		for(double i = n ; i >= 0;i--){
			//Actually drawing the line
			g2.drawLine(0, (int)(i*height), (int)(width*1.2), (int)(i*height));
			
			//And subscribing it
			String s = Double.toString(max*(1 - i/n));
			s = s.indexOf(".")>=2 ? s.substring(0, s.indexOf(".")):s.substring(0, s.indexOf(".")+2);
			int fontSize = (height<12)?(int)height:12;
			g2.setFont(new Font(g2.getFont().getFontName(),Font.BOLD,fontSize));
			g2.drawString(s,3,(float)(i*height - 2));
		}
		g2.setFont(in);
		return max;
	}
	/**
	 * Data setter
	 */
	@Override
	public void setData(String[][] data) {
		this.data = data.clone();
	}
	/**
	 * Sets the size of canvas. To know how much paint we can use on it:)
	 */
	@Override
	public void setSize(Dimension d) {
		canvasSize = d;
		canvasSize.height-= 0.05*canvasSize.height;
		canvasSize.width -= 0.1*canvasSize.width;
	}
	/**
	 * AutoScaling
	 * @param max maximal element
	 */
	private void autoScale(double max){
		this.scale = (int)(max/10);
		if(this.scale == 0){
			this.scale = 1;
		}
	}
	/**
	 * Gets the barnumber we should redraw
	 * @param x Coordinate on canvas
	 * @return number of bar this coordinate represents
	 */
	@Override
	public int getBar(int x) {
		double width = canvasSize.width/(data.length+1);
		x = x-(int)(0.5*width);
		double d = (x)/width;
		int bar = (int)Math.round(d);
		if((d-bar)<0){
			bar--;
		}
		if (bar<0){bar = 0;}
		if (bar>=data.length){bar = data.length-1;};
		return bar;
	}
}
