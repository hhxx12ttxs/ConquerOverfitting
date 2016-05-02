/*
 *  Fractal generating program.
 *  Copyright (C) 2010  Tomas Witzany
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 *  
 *  This program comes with ABSOLUTELY NO WARRANTY.
 *  This is free software, and you are welcome to redistribute it
 *  under certain conditions. See the GNU General Public License 
 *  for more details.
 */
package cz.witzany.fractal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * The Class FractalImage renders a fractal on a BufferedImage.
 */
public class FractalImage extends BufferedImage{
	
	/** The density of the points in the fractal. */
	private double density[][];
	
	/** The min_y. */
	private double max_x=0,min_x=0,max_y=0,min_y=0;
	
	/** The maximum density of the fractal. */
	private double max_c=0;
	
	/** The last generated point. */
	private Point2D lastPoint;
	
	/** The coloring gradient of the fractal. */
	private ColorFormula colorFormula;
	
	/** The the scale and position of the fractal. */
	private AffineTransform lastPosition;
	
	/** The default WIDTH of the BufferedImage. */
	private final static int WIDTH=1400;

	/** The default HEIGHT of the BufferedImage. */
	private final static int HEIGHT=1400;
	
	/**
	 * Instantiates a new fractal image with the default width and height.
	 */
	public FractalImage()
	{
		this(WIDTH,HEIGHT);
	}
	
	/**
	 * Instantiates a new fractal image with a custom width and height.
	 * 
	 * @param width
	 *            the width of the image
	 * @param height
	 *            the height of the image
	 */
	public FractalImage(int width,int height)
	{
		super(width,height, BufferedImage.TYPE_INT_RGB);

		density=new double[width][height];
		for(int i=0; i<density.length; i++)for(int j=0; j<density[i].length; j++)density[i][j]=0;

		Graphics g = getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		
		lastPosition=new AffineTransform(400,0,0,400,width/2,height/2);
		
		colorFormula=new ColorFormula();
	}
	
	/**
	 * Recalculate coloring smooths the coloring of the fractal after generation.
	 * This is handy if you paint the fractal during the generation, this should be called after the generation is done.
	 */
	public synchronized void recalculateColoring()
	{
		Graphics2D g = (Graphics2D) getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for(int x=0;x<density.length;x++)
			for(int y=0;y<density[x].length;y++)
				drawPoint(x,y,g,0); //add 0 density to the point but draw it
	}
	
	/**
	 * Gets the histogram of the fractal density, useful for color gradient generation.
	 * 
	 * @return the histogram of the fractal density
	 */
	public BufferedImage getHistogram()
	{
		int[] histrogram = new int[(int)Math.ceil(max_c)+1];
		for(int x=0;x<density.length;x++)
			for(int y=0;y<density[x].length;y++)
				histrogram[(int)Math.round(density[x][y])]++; //add one for each point close to a density
		
		int max_histro = 0;
		//calculate the maximum value of the histogram
		for(int i=1; i<histrogram.length; i++)
			if(histrogram[i]>max_histro)
				max_histro=histrogram[i];
		
		//fill the image with data
		int height = 100;
		int barWidth = 3;
		BufferedImage histrogramImage = new BufferedImage((histrogram.length-1)*barWidth,height,BufferedImage.TYPE_INT_RGB);
		Graphics g = histrogramImage.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, histrogramImage.getWidth(), height);
		g.setColor(Color.RED);
		for(int i=0;i<histrogram.length-1;i++)
			g.fillRect(i*barWidth, 20, barWidth, (int)(height*histrogram[i+1]/(double)max_histro));
		return histrogramImage;
	}
	
	/**
	 * Advances the generation by iter points. 
	 * Before calling this, call prepare, otherwise, the generation will be broken.
	 * After calling this several times, call recalculateColoring to smoothen the collage of the fractal.
	 * 
	 * @param iter
	 *            the number of points to generate - note that large amounts will be duplicate, this is used for generating fractal density
	 * @param position
	 *            the scale and position of the fractal
	 * @param transform
	 *            the TransformationSet defines the fractal attractor alias its shape
	 * @param drawPoint
	 * 			  specifies if to draw the point on the screen or only to generate it and save it
	 * @param mrwa
	 * 			  if set to true, the advance function will use the modified random walk algorithm to generate points, experimental
	 */
	public void advance(int iter, AffineTransform position, TransformationSet transform, boolean drawPoint, boolean mrwa)
	{
		if(mrwa)
		{
			Point2D draw = new Point2D.Double(0,0);
			int iterations=0;
			Graphics2D g = (Graphics2D) getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Object[] everything = transform.getTransforms().toArray();
			
			while(iterations<iter)
			{
				for(int i=0; i<everything.length; i++)
				{
					((AffineTransform)everything[i]).transform(lastPoint, draw);
					//scale and translate the point so the user can see it
					position.transform(draw, draw);
					if(drawPoint)
						drawPoint(draw,g,1,0);
				}
				//transform the point by a random transformation
				((AffineTransform)everything[TransformationSet.r.nextInt(everything.length)]).transform(lastPoint, lastPoint);
				//scale and translate the point so the user can see it
				position.transform(lastPoint, draw);
				//draw the point with a density 1
				if(drawPoint)
					drawPoint(draw,g,1,0);
				iterations++;
			}
		}else
		{
			Point2D draw = new Point2D.Double(0,0);
			int iterations=0;
			Graphics2D g = (Graphics2D) getGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			while(iterations<iter)
			{
				//transform the point by a random transformation
				transform.chooseRandom().transform(lastPoint, lastPoint);
				//scale and translate the point so the user can see it
				position.transform(lastPoint, draw);
				//draw the point with a density 1
				if(drawPoint)
					drawPoint(draw,g,1,0);
				iterations++;
			}
		}
	}
	
	/**
	 * Prepares for fractal generation.
	 * 
	 * @param position
	 *            the scale and position of the fractal
	 */
	public void prepare(AffineTransform position)
	{
		//initialize some variables to default values
		lastPoint = new Point2D.Double(0,0);
		min_x=min_y=Math.pow(10, 30); //bit of a hack, should use a boolean switch for each variable if it is set
		max_x=max_y=-min_x;
		max_c=10f;
		lastPosition=position;
		
		for(int i=0; i<density.length; i++)for(int j=0; j<density[i].length; j++)density[i][j]=0;

		//clears the canvas
		Graphics2D g = (Graphics2D) getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
	
	/**
	 * Generates a fractal and draws it on an BufferedImage. This takes a while (if you want to watch something while generating, use prepare+advance).
	 * 
	 * @param maxiter
	 *            the number of points to generate - note that large amounts will be duplicate, this is used for generating fractal density
	 * @param position
	 *            the scale and position of the fractal
	 * @param transform
	 *            the TransformationSet defines the fractal attractor alias its shape
	 */
	public void generate(int maxiter, AffineTransform position, TransformationSet transform)
	{
		prepare(position);
		advance(100, position, transform,false,false); //get the lastPoint into the attractor
		advance(maxiter, position, transform,true,false);
		recalculateColoring();
	}
	
	/**
	 * Saves the BufferedImage to a file, does nothing if a exception occurs.
	 * 
	 * @param file
	 *            the file to save to, if it exists, it is rewritten
	 */
	public void save(File file)
	{
		try {
			String path = file.getAbsolutePath();
			String extension = "png";
			if(path.lastIndexOf(".")!=-1)
			{
				extension = path.substring(path.lastIndexOf(".")+1);
				if(!extension.equals("png")&&!extension.equals("jpg")&&!extension.equals("gif"))
				{
					extension="png";
					file = new File(path+".png");
				}
			}else
				file = new File(path+".png");
			ImageIO.write(this, extension, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Draws a point with a Graphics context.
	 * 
	 * @param xn
	 *            the x position of the point
	 * @param yn
	 *            the y position of the point
	 * @param g
	 *            the Graphics context
	 * @param weight
	 *            the density to add to the fractal
	 */
	private void drawPoint(int xn, int yn, Graphics g, double weight)
	{
		//if the point is in the image, add it to the fractal
        if(xn<density.length&&xn>=0&&yn<density[xn].length&&yn>=0)
        {
        	density[xn][yn]+=weight;
        	if(density[xn][yn]>max_c)max_c=density[xn][yn];
        	g.setColor(getColor(density[xn][yn]));
            g.fillRect(xn,yn, 1, 1);
            if(weight==0||density[xn][yn]==0)
            	return;
        }
        //this is needed to calculate the suggested scale and translation
        if(xn>max_x)max_x=xn;
        if(xn<min_x)min_x=xn;
        if(yn>max_y)max_y=yn;
        if(yn<min_y)min_y=yn;
	}
	
	/**
	 * Draws a point with a Graphics context and leaks out to neighbor pixels with the given ratio.
	 * 
	 * @param draw
	 *            the point to draw
	 * @param g
	 *            the Graphics context
	 * @param weight
	 *            the density to add to the fractal
	 * @param leakRatio
	 *            the leak ratio
	 */
	private void drawPoint(Point2D draw, Graphics g, double weight, double leakRatio)
	{
		double x = draw.getX();
		double y = draw.getY();
		int xn = (int)Math.floor(x);
		int yn = (int)Math.floor(y);
		if(leakRatio!=0)
		{
			double next = 0;
			double total = 0;
			double dx = x-xn-0.5;
			double dy = y-yn-0.5;
			xn+=dx>0?1:-1;
			next=(Math.abs(dx)-Math.abs(dx*dy))*leakRatio;
			total+=next;
			drawPoint(xn,yn,g,weight*next);
			yn+=dy>0?1:-1;
			next=Math.abs(dx*dy)*leakRatio;
			total+=next;
			drawPoint(xn,yn,g,weight*next);
			xn+=dx>0?-1:1;
			next=(Math.abs(dy)-Math.abs(dx*dy))*leakRatio;
			total+=next;
			drawPoint(xn,yn,g,weight*next);
			yn+=dy>0?-1:1;
			drawPoint(xn,yn,g,weight*(1-total));
		}else 
			drawPoint(xn,yn,g,weight);
	}
	
	/**
	 * Gets the color of a density.
	 * 
	 * @param density
	 *            the density
	 * 
	 * @return the color
	 */
	private Color getColor(double density)
	{
		return colorFormula.getColor(density, max_c);
	}
	
	/**
	 * Sets the color gradient of the fractal.
	 * 
	 * @param formula
	 *            the new color gradient
	 */
	public void setColorFormula(ColorFormula formula)
	{
		colorFormula=formula;
	}
	
	/**
	 * Gets the suggested scale and translation.
	 * This is useful when you generate a fractal with a low amount of points just to find out this transformation
	 * 
	 * @param fit the ratio of the size of the fractal to size of the screen; 1 normally e.g. 0.95 to fit the screen better
	 * @return the suggested scale and translation in the form of an AffineTransform
	 */
	public AffineTransform getSuggestedTransformation(double fit)
	{
		if(max_x==min_x||max_y==min_y)
			return lastPosition;
		if(fit>1)
			fit=1;
		if(fit<=0)
			fit=0.001;
		
		//scale the last affine transform so that the size of the fractal is the size of the image
		double scalecoef = Math.min(fit*getWidth()/Math.abs(max_x-min_x),fit*getHeight()/Math.abs(max_y-min_y));
		lastPosition.scale(scalecoef, scalecoef);
		
		//scale the saved bounds
		double minx = min_x*scalecoef;
		double maxx = max_x*scalecoef;
		double miny = min_y*scalecoef;
		double maxy = max_y*scalecoef;
		
		//translate the fractal to the middle of the screen
		AffineTransform translate = AffineTransform.getTranslateInstance((getWidth()-maxx-minx)/2, (getHeight()-maxy-miny)/2);
		lastPosition.preConcatenate(translate);
		
		return lastPosition;
	}
}

