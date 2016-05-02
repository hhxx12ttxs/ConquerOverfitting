package tankds;

/**
 * PositionGraph.java
 * This object displays the current position of a Projectile.
 * Andrew Dolgert for Michael Fowler 12 August 1998
 */

import java.applet.*;
import java.awt.*;
import java.awt.image.*; // for the label business far below.
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.*;
import java.text.ParseException;

// There won't be any threading here.  The only trick in this graph panel is
// double buffering.

// This graph extends Panel so it can be a component of the main applet.
// By implementing Projectile.ProjectileListener, it is able to accept messages
// from Projectile.
public class PositionGraph extends Panel implements Projectile.ProjectileListener {
	Projectile dart; // We record the projectile we are supposed to work with.
	Image buffer; // The buffer and its Graphics component, bufferGraphics, are our back buffer.
	Dimension size; // The current size of the panel.  Saved so we know when the size has changed.
	Graphics bufferGraphics;
	ResourceBundle rb;
    String sDistance, sHeight, sVelocity, sTime, sMeters, sMPS, sSec;
    String sXAxis, sYAxis;
	// a DrawableAxis is a home brew object that draws an axis.
	DrawableAxis ordinate, abscissa;
	// This is the pixel location of the origin of the axes.
	Point origin;
	// How long the x-axis needs to be.  This changes when you change the max velocity.
	double maxDistance = 280.0;  // -v0^2/g
	// The changeSize() method decides the length of arms of the axes.
	int graphLengthX, graphLengthY;
	// This scales between distance in meters and distance in pixels.
	double scaleDistance;
	// The current position of the projectile.
	int dartX, dartY;
	double realX, realY; // The current position of the projectile in meters.
	// The next place we will draw projectile (in pixels).
	int newDartX, newDartY;
	// This rectangle is the white area inside the axes.  We record this for
	// redraws.  If the area we need to redraw is outside the main area of the
	// graph, then we need to take time to redraw the axes.
	Rectangle graphArea;
	boolean exists; // Whether there is any projectile.  It is false before the first shot.
	boolean bPleaseRedraw; // Whether to redraw the whole screen.
    boolean bWantTrails; // Whether we want trails for the next shot.
	boolean bTrails; // Whether to draw trails behind balls for this shot.
	boolean bStatistics; // At the end of a shot and before the next shot, we put shot stats in the corner.
	// These are those shot stats.
	double endDistance, maxHeight, endVelocity, endTime;
	// Each shot gets a new color from this list.
	static final Color[] colorList = {Color.red, Color.blue, Color.magenta, Color.orange, Color.green};
	// currentColor is just that.  newColor is the next color we will use.  They need to be different.
	// The newColor gets chosen after a shot hits the ground b/c you can interrupt a shot in midair
	// with another shot, and that other shot should be the same color.  But we don't change the currentColor
	// until the next shot is fired b/c we still need to draw the projectile on the ground in its correct
	// color.
	int currentColor, newColor;
	//  This vector will hold information about every shot whose trail we want to show.
	//  We save the last maxShots trails.  We save this list in addition to the pixel positions
	// of the trails themselves in case the window gets resized.
	Vector shotList;
	static final int maxShots = 5;

	// Initializer
	public PositionGraph(Projectile vomit, ResourceBundle rBundle) {
		// There is an implicit call to super() here to initialize the Panel.
		dart = vomit;
		bWantTrails = true;
		changeSize();  // every time our size may change, we re-initialize.
		dart.addListener(this); // Tell the Projectile dart to inform us of shots fired, postions, and hits.
		exists = false; // There is no projectile until the first shot.
		bStatistics = false; // There are no statistics until a shot hits the ground.
		currentColor = newColor = 0; // The first color is red.
		shotList = new Vector(maxShots); // This shotList is available to save past shots for trails.
	    rb = rBundle;
        if (rb != null) {
            sDistance = rb.getString("distance");
            sHeight = rb.getString("height");
            sVelocity = rb.getString("endvelocity");
            sTime = rb.getString("time");
            sMeters = rb.getString("meters");
            sMPS = rb.getString("meterspersecond");
            sSec = rb.getString("seconds");
            sXAxis = rb.getString("xaxis");
            sYAxis = rb.getString("yaxis");
        } else {
            sDistance = "Max distance: ";
            sHeight = "Max height: ";
            sVelocity = "End velocity: ";
            sTime = "Total time: ";
            sMeters = "m";
            sMPS = "m/s";
            sSec = "s";
            sXAxis = "Distance [m]";
            sYAxis = "Height [m]";
        }
	}

	// This is where we draw it all.  We draw first onto the back buffer, bufferGraphics, then
	// copy that buffer to the screen.
	public void paint(Graphics g) {
		// If the size of the panel has changed, re-initialize with a method below.
		// size is a Dimension storing the previously known size.
		if (!size.equals(this.getSize())) changeSize();
		// Set our basic font for the axes.
		//Font f = new Font("SansSerif",Font.PLAIN,10);
		//bufferGraphics.setFont(f);
		// When the system calls paint, it sets a clipping rectangle which contains only the
		// part of the panel it thinks needs to be updated.  Our drawing operations in the
		// background buffer should be faster if we set the clipping rectangle in the background
		// buffer to be the same as that of the main panel.
		Rectangle trect = g.getClipBounds();
		bufferGraphics.setClip(trect);
		bufferGraphics.setColor(Color.white);
		// graphArea is a rectangle that goes from the origin of the axes to the upper right corner
		// of the panel.  If the lower left point of the clipping rectangle is inside the graphArea,
		// we don't need to do a bunch of time consuming tasks like draw the axes and labels.
		if (!graphArea.contains(trect.x,trect.y+trect.height)) {
			// To redraw the panel, first clear it, then draw the axes.
			bufferGraphics.fillRect(0,0,size.width,size.height);
			bufferGraphics.setColor(Color.black);
			ordinate.draw(bufferGraphics,origin.x,origin.y);
			abscissa.draw(bufferGraphics,origin.x, origin.y);
		} else {
			bufferGraphics.fillRect(graphArea.x,graphArea.y,graphArea.width,graphArea.height);
		}
		if (bTrails) {
			// If there are trails, loop through the saved shots and draw them on the screen.
			shotSummary sS;
			for (int j=0; j<shotList.size(); j++) {
				// from the shotList we find the color of the trail.
				sS = (shotSummary) shotList.elementAt(j);
				bufferGraphics.setColor(sS.currentColor);
				// draw all the points stored through the given (x,y) coordinates.
                for (int k=0; k<sS.iPoints; k++)
    				bufferGraphics.fillOval(sS.shotPoints[k][0]-2,sS.shotPoints[k][1]-2,5,5);
			}
		}
		// if a projectile was ever fired, draw one.
		if (exists) {
			dartX = newDartX;  dartY = newDartY;
			bufferGraphics.setColor(colorList[currentColor]);
			bufferGraphics.fillOval(dartX-2,dartY-2,5,5);
		}
		// bStatistics is true only if a shot hit and the stats on it returned.
		if (bStatistics) {
			// This numberformat stuff is awful.  The point is to print digits to two signifigs.
			NumberFormat numform = NumberFormat.getInstance(Locale.US);
			if (numform instanceof DecimalFormat) {
				DecimalFormat decform = (DecimalFormat) numform;
				decform.applyPattern("0.##");
				String s1 = new String(sDistance+" "+decform.format(endDistance)+" "+sMeters);
				String s2 = new String(sHeight+" "+decform.format(maxHeight)+" "+sMeters);
				String s3 = new String(sVelocity+" "+decform.format(endVelocity)+" "+sMPS);
				String s4 = new String(sTime+" "+decform.format(endTime)+" "+sSec);
				Font f = new Font("SansSerif",Font.PLAIN,12);
				bufferGraphics.setFont(f);
				FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(f);
				int ascent = fm.getAscent();
				int spacing = ascent + fm.getLeading();
				int xloc = size.width-fm.stringWidth(s1)-20;
				int it = size.width-fm.stringWidth(s2)-20;
				if (it<xloc) xloc = it;
				it = size.width-fm.stringWidth(s3)-20;
				if (it<xloc) xloc = it;
				it = size.width-fm.stringWidth(s4)-20;
				if (it<xloc) xloc = it;
				int yloc = 10+spacing;
				bufferGraphics.setColor(Color.blue);
				bufferGraphics.drawString(s1,xloc,yloc);
				yloc += spacing;
				bufferGraphics.drawString(s2,xloc,yloc);
				yloc += spacing;
				bufferGraphics.drawString(s3,xloc,yloc);
				yloc += spacing;
				bufferGraphics.drawString(s4,xloc,yloc);
			}
		}
		// Remember that bufferGraphics is the Graphics object of the Image, buffer.
		// In order to copy the back buffer to the Panel's Graphics, we copy Image buffer.
		g.drawImage(buffer,0,0,this);
	}

	// The difference between update and paint is that the system calls update when it it
	// wants to clear the screen before a paint.  Panel has a method update() that does just that,
	// clear the screen and call paint().  With all our attention to painting, we don't
	// need to clear the screen first, so we override Panel's update.
	public void update(Graphics g) {
		paint(g);
	}

	// The next three methods constitute our implementation of the interface
	// Projectile.ProjectileListener.

	// Projectile calls this at the start of a shot.  We use it to estimate sizes for the axes.
	public void beginFiring(double velocity, double angle,double[] shotStats)
	{
		exists = true;  // When a shot starts, we have a projectile to draw.
		dartX = dartY = 0; // Initial projectile position is the origin.
		bStatistics = false; // If there were stats from the last shot, erase them b/c they no
		// longer apply.
		// The next shot will be a new color.
		currentColor = currentColor+1;
		// There are maxShots colors.
		if (currentColor>maxShots-1) currentColor = 0;

		setPosition(dartX,dartY,0,0,0,false);
        if (bWantTrails) bTrails = true; else bTrails = false;
		// Erase any trails just in case someone just turned off trails.
		if (!bTrails) shotList.setSize(0);
		else {
			if (shotList.size() > maxShots-1) {
				shotList.removeElementAt(0);
			}
			shotList.addElement(new shotSummary(shotStats,colorList[currentColor]));
		}

		double firemax = velocity*velocity/9.81;
		firemax = 1.1*firemax;
		// Check whether the new size is much different from the current one.
		if (firemax>maxDistance || firemax<0.4*maxDistance) {
			maxDistance = firemax;
			// Here we change the axis size.  changeSize automatically repaints.
			changeSize();
		} else {
			// Repaint the whole screen for the new shot b/c the axes may have changed.
			bPleaseRedraw = true;
			repaint();
		}
	}

	// Projectile calls this every time it moves the projectile.
	// We will also call this when the screen is resized in order to make sure
	// the pixel position of the ball agrees with new axes.
    // mark tells us whether this is an update for trails of balls.  Time is the current time.
	public void setPosition(double x, double y, double vx, double vy,double time,boolean mark) {
        if (mark) {
            int tx, ty;
            if (!bTrails) return;
            // System.out.println("mark at "+x+", "+y+" t "+time);
    		tx = (int) (x*scaleDistance+origin.x);
    		ty = (int) (origin.y-y*scaleDistance);
            ((shotSummary) shotList.lastElement()).addPoint(tx,ty,time);
            return;
        }
		// We need to know what part of the screen to erase, so first save
		// the previous location of the ball.
		Rectangle oldRect = new Rectangle(dartX-3,dartY-3,9,9);
		realX = x; realY = y; // These are the new ball location in meters.
		// Convert from meters to pixel location for the current graph.
		newDartX = (int) (x*scaleDistance+origin.x);
		newDartY = (int) (origin.y-y*scaleDistance);
		// Find what pixels are affected.
		Rectangle newRect = new Rectangle(newDartX-3,newDartY-3,9,9);
		// The clipping rectangle will be the union of pixels to destroy an
		// pixels to draw.  Setting this clipping rectangle will speed
		// drawing considerably.
		Rectangle theRect = oldRect.union(newRect);
		this.repaint(theRect.x,theRect.y,theRect.width,theRect.height);
	}

	// Projectile calls this when a shot hits.  Look at projectile to see what these
	// things are.
	public void endFiring(double[] endStats)
	{
		endDistance = endStats[0];
		endVelocity = endStats[1];
		maxHeight = endStats[2];
		endTime = endStats[3];
		bStatistics = true; // We have stats.  Tell paint() to display them.

		bPleaseRedraw = true;
		repaint(); // repaint the whole screen.
	}

	// This is a subclass of PositionGraph.  It is a new feature in java 1.1.  I could make
	// it another class, but it is private to this graph, which is good.
	private class shotSummary {
		// Shotstats is a length 5 vector with (kappa,gamma, vxScale, vyScale, tScale).
		public double[] shotStats;
		public Color currentColor;
		public int[][] shotPoints; // array of trail points
        public double[] shotTimes; // array of times of those points (for reconstruction if need be).
        public int iPoints; // current number of points.
        private int maxPoints; // max allocation available.
        
		public shotSummary(double[] sS, Color c) {
			shotStats = sS;
			currentColor = c;
			maxPoints = 10; // why not start with ten?
			shotPoints = new int[maxPoints][2];
            shotTimes = new double[maxPoints];
			iPoints = 0;
		}

        // x and y are the current pixel positions and t is the real time in m/s.
		public void addPoint(int x, int y, double t) {
		    if (iPoints>maxPoints-1) {
                maxPoints += 10;
		        int[][] tp = new int[maxPoints][2];
                double[] tt = new double[maxPoints];
		        for (int i=0; i<iPoints; i++)
		            for (int j=0; j<2; j++) {
		                tp[i][j] = shotPoints[i][j];
		                tt[i] = shotTimes[i];
		            }
		        shotPoints = tp;
		        shotTimes = tt;
		    }
		    shotPoints[iPoints][0] = x;
		    shotPoints[iPoints][1] = y;
		    shotTimes[iPoints] = t;
		    iPoints++;
		}
	}
		

	public void setTrails(boolean on)
	{
		bWantTrails = on;
	}

	// Overriding these methods from Panel helps layout managers add us to the applet.
	public Dimension getPreferredSize() {
		return new Dimension(200,100);
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(200,100);
	}

	// Every time dimensions of the panel or numerical bounds on the domain or range change,
	// we call this.
	private void changeSize()
	{
		// Store the current size in the class varialbe size.
		size = this.getSize();
		// Make a new background buffer for drawing.
		buffer = this.createImage(size.width,size.height);
		// if our size was zero, the buffer will be null.  This actually happens sometimes
		// when the constructor of PositionGraph calls us.
		if (buffer!=null) {
			bufferGraphics = buffer.getGraphics();
		} else return;

		// Given the new size, choose an origin for the graph and record the area inside the graph.
		origin = new Point((int) (size.width*0.2),(int) (size.height*0.8));
		graphArea = new Rectangle(origin.x+1,0,size.width-origin.x,origin.y-1);
		// These are arm lengths of the axes.
		graphLengthX = (int) (size.width*0.7);
		graphLengthY = (int) (size.height*0.7);
		// maxDistance is the maxDistance this shot can travel.  It happens that if a shot can travel
		// maxDistance in the x direction, it can travel x/2 in the y direction.  (Physics, go figure.)
		// We make sure that, whatever the Panel dimensions, there is enough room in either direction.
		if (graphLengthX > graphLengthY*2)
			scaleDistance = 2*graphLengthY/maxDistance;
		else
			scaleDistance = graphLengthX/maxDistance;

		// Create a new axis.  DrawableAxis is my own routine.
		Font f = new Font("SansSerif",Font.PLAIN,10);
		ordinate = new DrawableAxis(graphLengthX,DrawableAxis.HORIZONTAL,
				0,graphLengthX/scaleDistance,f);
	    ordinate.setLabel(sXAxis, new Font("SansSerif",Font.BOLD,12));
		abscissa = new DrawableAxis(graphLengthY,DrawableAxis.VERTICAL,
				0,graphLengthY/scaleDistance,f);
	    makeLabelForAxis(abscissa,sYAxis,new Font("SansSerif",Font.BOLD,12));
		// Set the position of the Projectile if it exists.
		if (exists) setPosition(realX,realY,0,0,0,false);
		// Calculate the new pixel positions of trails from their shotList descriptions.
		if (bTrails) calculateTrails();
		bPleaseRedraw = true;
		repaint(); // Paint it all.
	}

    // This subroutine makes an image for the drawableaxis to use as a label.
	// We do it here instead of inside the axis because the axis won't allow
	// me to create an image.  Only components with a nonzero clipping region
	// can create an image, and the DrawableAxis is never officially shown on the
	// screen.  The real solution is to make the DrawableAxis a lightweight
	// component (and the labels, and the floating statistics bar, and the
	// graph points themselves) and put it all in its own GraphLayoutManager.
	// It's a battle for another day.
	private void makeLabelForAxis(DrawableAxis da,String sLabel,Font labelFont) {
        FontMetrics lfm = getToolkit().getFontMetrics(labelFont);
	    int width = lfm.stringWidth(sLabel);
	    //System.out.println("width "+width+" height "+lfm.getLeading());
        // the height should be getHeight()+getLeading(), but getLeading is 0.
        Image ti = createImage(width,lfm.getHeight()+5);
        Graphics tb = ti.getGraphics();
        tb.setFont(labelFont);
        tb.drawString(sLabel,0,lfm.getAscent());
        // Is the ImageProducer this Canvas?
        ImageProducer ip = ti.getSource();
        // The RotFilter is mine.  It rotates an image 90 degrees.
        RotFilter rf = new RotFilter();
        ip = new FilteredImageSource(ip,rf);
        Image rotatedImage = Toolkit.getDefaultToolkit().createImage(ip);
        da.setLabel(rotatedImage);
	}

	// shotList has initial value information for past shots.  here we recalculate
	// pixel locations along the trails for the current coordinate system.
	private void calculateTrails()
	{
		double t,ttotal, kap,gam,vx0,vy0,t0;
		double[] retLocs;
		shotSummary tSum;
		// loop over all shots whose trails we stored.
		for (int j=0; j<shotList.size(); j++) {
			// Pull initial value info from shotList.
			tSum = (shotSummary) shotList.elementAt(j);

			// Put that info into variables to send to the calculation method.
            // this is written out so I know what is going on.  It is ugly because
			// kappa, gamma, t0, vx0, and vy0 are all really internal variables from
			// Piston.class.  Here I ask Piston.class to recalculate the locations
			// of trail points given the time at which the trail point occurred.
			t=0; kap = tSum.shotStats[0]; gam = tSum.shotStats[1];
			t0 = tSum.shotStats[4]; vx0 = tSum.shotStats[2];
			vy0 = tSum.shotStats[3];
            //System.out.println("kap "+kap+" gam "+gam+" t0 "+t0+" vx0 "+vx0+" vy0 "+vy0);
			for (int i=0; i<tSum.iPoints; i++) {
				t = tSum.shotTimes[i];
				// Ask a projectile method to tell us the current x and y values.
				retLocs = dart.calcPosition(kap,gam,t,vx0,vy0,t0);
				tSum.shotPoints[i][0] = (int) (retLocs[0]*scaleDistance+origin.x);
				tSum.shotPoints[i][1] = (int) (origin.y-retLocs[1]*scaleDistance);
                //System.out.println("recalc mark at x "+retLocs[0]+" y "+retLocs[1]+" t "+t);
			}
		}
	}

}


