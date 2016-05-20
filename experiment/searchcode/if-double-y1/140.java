package frond.fractals;

import frond.maths.*;
import frond.colourschemes.*;

import java.lang.Thread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public abstract class Fractal extends JPanel {
	
	private BufferedImage output;
	private Double tX, tY, bX, bY;
	private Dimension size = new Dimension(500, 500);
	public Dimension boxStart, boxEnd;
	public boolean selectionBox = false;
	protected int itterations = 200;
	protected ColourScheme colourScheme;
	private double log2 = Math.log(2);
	private String name;
	
	/**
	* rendering thread class
	*/
	private class RenderingThread implements Runnable {
		
		Fractal fractal;
		BufferedImage image;
		Complex start;
		Complex end;
		
		/**
		* rendering thread constructor
		*/
		public RenderingThread(Fractal fractal, BufferedImage image, Complex start, Complex end) {
			this.fractal = fractal;
			this.image = image;
			this.start = start;
			this.end = end;
		}
		
		/**
		* thread main method
		*/
		public void run() {
			fractal.drawFractal(image, start, end);
			fractal.repaint();
		}
	}
	
	
	public Fractal(String name) {
		
		this.addComponentListener(
			new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					Fractal f = (Fractal)e.getComponent();
					f.update();
				}	
			}
		);
		
		setBackground(Color.black);
		colourScheme = new StandardColourScheme();
		tX = -1.0;
		tY = 1.0;
		bX = 1.0;
		bY = -1.0;
		
		this.name = name;
		//update();
	}
	
	public void update() {
		Dimension size = new Dimension();
		getSize(size);
		if (size.width > 2 && size.height > 2) {
			output = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
			
			Complex tL = new Complex(tX, tY);
			Complex bM = new Complex(tX + ((bX - tX) / 2), bY);
			Complex tM = new Complex(tX + ((bX - tX) / 2), tY);
			Complex bR = new Complex(bX, bY);
			
			BufferedImage leftSection = output.getSubimage(0, 0, output.getWidth() / 2, output.getHeight());
			new Thread(new RenderingThread(this, leftSection, tL, bM)).start();
			
			BufferedImage rightSection = output.getSubimage(output.getWidth() / 2, 0, output.getWidth()/2, output.getHeight());
			new Thread(new RenderingThread(this, rightSection, tM , bR)).start();
			
		}
		this.repaint();
	}
	
	public void setColourScheme(ColourScheme c) {
		colourScheme = c;
		update();
	}
	
	// Getter and Setter for itterations
	public void setItterations(int i) {
		itterations = i;
		update();
	}
	
	public int getItterations() {
		return itterations;
	}
	
	// Getter and Setter for size
	public void setPreferredSize(Dimension d) {
		super.setPreferredSize(d);
		update();
	}
	
	public void setSize(Dimension d) {
		this.setPreferredSize(d);
		this.size = d;
		update();
	}
	
	public Dimension getSize() {
		return getSize(new Dimension());
		//return this.size;
	}
	
	public double getSizeX() {
		return this.size.getWidth();
	}
	
	public double getSizeY() {
		return this.size.getHeight();
	}
	
	// Getter and Setter for coordinates
	public void setCoordinates(double x1, double y1, double x2, double y2) {
		this.tX = x1;
		this.tY = y1;
		this.bX = x2;
		this.bY = y2;
		update();
	}
	
	public double[] getCoordinates() {
		double[] c = {
			tX, tY, bX, bY
		};
		return c;
	}
	
	public double getCoordinateWidth() {
		return bX - tX;
	}
	
	public double getCoordinateHeight() {
		return tY - bY;
	}
	
	public void translate(double xDist, double yDist) {
		tX = tX + xDist;
		tY = tY + yDist;
		bX = bX + xDist;
		bY = bY + yDist;
		update();
	}
	
	
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(output, 0, 0, Color.red, null);
		if (selectionBox) {
	        Graphics2D g2 = (Graphics2D) g;
	        
	        Double x1, x2, y1, y2;
	        if (boxStart.getWidth() < boxEnd.getWidth()) {
	        	x1 = boxStart.getWidth();
	        	x2 = boxEnd.getWidth() - boxStart.getWidth();
	        } else {
	        	x1 = boxEnd.getWidth();
	        	x2 = boxStart.getWidth() - boxEnd.getWidth();
	        }
	        if (boxStart.getHeight() < boxEnd.getHeight()) {
	        	y1 = boxStart.getHeight();
	        	y2 = boxEnd.getHeight() - boxStart.getHeight();
	        } else {
	        	y1 = boxEnd.getHeight();
	        	y2 = boxStart.getHeight() - boxEnd.getHeight();
	        }
	        g2.setColor(new Color(255, 255, 255, 70));
	        g2.fill(new Rectangle.Double(x1, y1, x2, y2));
	        g2.setColor(Color.white);
	        g2.draw(new Rectangle.Double(x1, y1, x2, y2));
		}
		paintChildren(g);
	}
	
	/**
	* Tendency generation function
	* 
	* returns a number between 0 and 1 representing how close the point is to the set.
	* 0 means the point instantly reaces infinity and 1 means it is part of the set
	* 
	* @param count the number of iterations carried out before bailout
	* @param zn, set this parameter if you would like the tendency adjusted with the normalised itteration count
	*/
	protected double getTendency(int count, Complex zn) {
		
		if (zn == null) {
			
			return (double) count / (double) itterations;
		} else {
			
			if (count == itterations) {
				return 1.0d;
			} else {
				
				double zx = zn.getReal();
				double zy = zn.getImaginary();
				return ((count + ( (Math.log(Math.log(4)) - Math.log(Math.log(Math.sqrt((zx*zx + zy*zy))))) / log2)) / itterations) % 1;
			}
		}
	}
	
	/**
	* Template FRactal Drawer funtion
	*
	* @param x size of the fractal
	* @param y size of the fractal
	*/
	protected abstract void drawFractal(BufferedImage image, Complex start, Complex end);
	
	/**
	* Name Accessor method
	*
	* returns the name of the fractal
	*
	* @return name of the fractal
	*/
	public String getName() {
		return name;
	}
}

