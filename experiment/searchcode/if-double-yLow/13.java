
/**
	CoordSystem.java
 
	<p>

	Copyright 1998, 1999, 2000, 2001, 2002, 2003, 2004 Patrik Lundin, patrik@lundin.info, 
	http://www.lundin.info
	
	<p>
 
	This file is part of GraphApplet.

	<p>

    GraphApplet is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

	<p>

    GraphApplet is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

	<p>

    You should have received a copy of the GNU General Public License
    along with GraphApplet; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	<p>
	<hr>
	<p>

	This class defines a coordinate system for drawing functions, it extends Canvas
	and can be used as any other AWT component.
 
	<p>
 
	To listen to events this class sends implement the CoordListener
	interface and register with addCoordListener, a CoordEvent is sent
	when the user zooms using the mouse, when the bounds are being changed
	or when an error occurs.
	
	<p>
 
	@author Patrik Lundin, patrik@lundin.info
	@version 1.05
 
*/

import java.awt.*;
import java.awt.event.*;
import com.javathings.math.*;
import java.util.*;
import java.text.*;


public final class CoordSystem extends Canvas {

	private static final long serialVersionUID = 4899646295343695723L;

	// general popumenu we can use
    PopupMenu pop = null;

    // for formatting numbers
    NumberFormat numform = null;

    // for adding listeners 
    Vector<CoordListener> coordlisteners = null;

    // com.javathings.math eval object.
    Eval ev = null;

    // com.javathings.math derive object.
    Derive de = null;

    // background color.
    Color bg = null;

    // default foreground color.
    Color fg = null;

    // hashtable to store functions in.
    Hashtable<String, Function> functions = null;

    // hashtable to put values in.
    Hashtable<String, String> vals = null;

    // current variable allowed.
    String currentvar = "";

    // limits.
    double minX = 0.0;
    double maxX = 0.0;
    double minY = 0.0;
    double maxY = 0.0;
    int step = 0;

    // used to determine if
    // limits have changed.
    double minXold = 0.0;
    double maxXold = 0.0;
    double minYold = 0.0;
    double maxYold = 0.0;
    int stepold = 0;

    // origo offset.
    double offXorigo = 0.0;
    double offYorigo = 0.0;

    // Default font
    Font fo = null;

    // fontmetrics
    FontMetrics fmetrics = null;

    // text for coordinate info.
    String text = "";

    // current startx for zoom rectangle
    int startx = -1;

    // current starty for zoom rectangle
    int starty = -1;

    // initial zoom startx, set by mouseDown
    int downx  = -1;

    // initial zoom startx, set by mouseDown
    int downy  = -1;

    // width on zoom rectangle
    int dragwidth  = 1;

    // height on zoom rectangle
    int dragheight = 1;

    // flag if zoom area is selected
    boolean drag = false;

    // the color if they drag with first mousebutton down
    Color dragcolor = null;

    // used for finding interesting points
    double oldxval = 0;
    double oldyval = 0;

    // the last function label written, so we can draw over it again
    Rectangle oldlbl = null;

    // random object used for generating random colors
    Random rand = null;

    /**
    	Creates a CoordSystem with default colors
    */
    public
    CoordSystem() {
        super();

        minX = -10.0;
        maxX = 10.0;
        minY = -10.0;
        maxY = 10.0;
        step = 250;
        minXold = minX;
        maxXold = maxX;
        minYold = minY;
        maxYold = maxY;
        stepold = step;

        currentvar = "x";

        ev				= new Eval();
        de				= new Derive();
        functions		= new Hashtable<String, Function>( 100 );
        vals			= new Hashtable<String, String>();
        coordlisteners	= new Vector<CoordListener>();

        bg				= Color.black;
        fg				= Color.white;
        dragcolor		= new Color( 0xffff00 );
        fo				= new Font( "TimesRoman", Font.PLAIN, 11 );
        numform		    = NumberFormat.getInstance();
        rand			= new Random();

        numform.setMaximumFractionDigits(9);

        setFont( fo );

        fmetrics = getFontMetrics( fo );

        setBackground( bg );
        setForeground( fg );

        setCursor( new Cursor( Cursor.CROSSHAIR_CURSOR ) );

        addMouseListener( new CoordSystem_MouseListener() );
        addMouseMotionListener( new CoordSystem_MouseMotionListener() );
    }

    /**
    	Creates a CoordSystem with the specified colors.
    */
    public CoordSystem( Color bg , Color fg ) {
        this();

        this.bg	= bg;
        this.fg	= fg;
    }

    /**
    	Adds CoordListeners to receive events from this CoordSystem
     
    	@param listener a object implementing the CoordListener interface
    */
    public void
    addCoordListener( CoordListener listener ) {
        if( listener != null )
            coordlisteners.addElement( listener );
    }

    /**
    	Removes a CoordListener
     
    	@param listener a object implementing the CoordListener interface
    */
    public void
    removeCoordListener( CoordListener listener ) {
        if( listener != null )
            coordlisteners.removeElement( listener );
    }

    /**
    	Adds a popup menu to the CoordSystem.
     
    	@param mi a MenuItem to add to the popup menu
    */
    public void
    addPopupMenuItems( MenuItem mi ) {
        if( pop == null ) {
            pop = new PopupMenu( "PopupTest" );

            add
                ( pop );
        }

        if( mi != null )
            pop.add( mi );
    }

    /**
    	Sets the number of fraction digits to use in the CoordSystem.
     
    	@param fractions the number of digits to use
     
    */
    public void
    setFractionDigits( int fractions ) {
        if( fractions >= 0 )
            numform.setMaximumFractionDigits( fractions );
    }

    /**
    	Overrides paint in Component
     
    	@param g a graphics object
    */
    public void
    paint( Graphics g ) {
        try {
            drawAll( g );
        } catch( Exception e ) {
            postEvent( new CoordEvent( this, e.getMessage(), minX, maxX , minY, maxY ) );
        }
    }

    /**
     
    	Posts CoordEvent's to all registered listeners
     
    	@param c the CoordEvent to dispatch
     
    */
    synchronized void
    postEvent( CoordEvent c ) {
        CoordListener listener;
        Enumeration<CoordListener> en = coordlisteners.elements();

        while( en.hasMoreElements() ) {
            listener = (CoordListener)en.nextElement();
            listener.coordSystemEvent( c );
        }
    }


    /**
    	This version of setBounds is for layout, overrides setBounds(int,int,int,int) in Component
     
    	@see java.awt.Component
    */
    public void
    setBounds( int x, int y, int width, int height ) {
        super.setBounds( x, y, width, height );
    }

    /**
    	This versions of setBounds sets the bounds for the coordsystem<br>
    	setting the bounds will post a CoordEvent.
     
    	@param xmin the lowest value for the x-axis
    	@param xmax the highest value for the x-axis
    	@param ymin the lowest value for the y-axis
    	@param ymax the highest value for the y-axis
    */
    public void
    setBounds( double xmin, double xmax , double ymin, double ymax ) {
        String message = null;

        minXold = this.minX;
        maxXold = this.maxX;
        minYold = this.minY;
        maxYold = this.maxY;

        this.minX = xmin;
        this.maxX = xmax;
        this.minY = ymin;
        this.maxY = ymax;

        if( hasLimitsChanged() ) {
            try {
                if( isShowing() )
                    drawAll();
            } catch( Exception ex ) {
                message = ex.getMessage();
            }
        }

        postEvent( new CoordEvent( this, message, minX, maxX , minY, maxY ) );
    }

    /**
    	Redraws the complete CoordSystem
     
    	@param g a graphics object
    	@exception java.lang.Exception if an error occurs
    */
    void
    drawAll(Graphics g) throws java.lang.Exception {
        g.setColor( this.bg );
        g.fillRect( 0 , 0 , this.getSize().width , this.getSize().height );
        g.setColor( this.fg );
        drawAxis(g);
        drawAllFunctions(g);
    }

    /**
    	Redraws the complete CoordSystem
     
    	@exception java.lang.Exception if an error occurs
    */
    void
    drawAll() throws java.lang.Exception {
        Graphics g = this.getGraphics();
        drawAll(g);
        g.dispose();
    }


    /**
    	Clears all the functions from the CoordSystem
    */
    public final void
    clear() {
        Graphics g = this.getGraphics();
        this.functions.clear();
        g.setColor( this.bg );
        g.fillRect( 0 , 0 , this.getSize().width , this.getSize().height );
        g.setColor( this.fg );
        drawAxis(g);
    }

    /**
    	Erases the specified function
     
    	@param fnk the function to erase
    */
    public final void
    eraseFunction(String fnk) {
        removeFunction( fnk );
        try {
            drawAll();
        } catch(Exception ex) {
            postEvent( new CoordEvent( this, ex.getMessage() , minX, maxX , minY, maxY ) );
        }
    }


    /**
    	Draws the axis for this CoordSystem
    */
    void
    drawAxis() {
        Graphics g = this.getGraphics();
        drawAxis( g );
        g.dispose();
    }

    /**
    	Draws the axis for this CoordSystem
     
    	@param g a graphics object
    */
    void
    drawAxis( Graphics g ) {
        int width2 = this.getSize().width;
        int height2 = this.getSize().height;
        double scalestep = 0;

        offXorigo = -minX / (double)( maxX - minX );
        offYorigo = maxY / (double)( maxY - minY );

        // x-axis
        g.drawLine( 0 , (int)Math.round( height2 * offYorigo ) , width2 , (int)Math.round( height2 * offYorigo ) );
        // y-axis
        g.drawLine( (int)Math.round( width2 * offXorigo ) , 0 , (int)Math.round( width2 * offXorigo ) , height2 );

        // scaling x-axis, offset origo
        scalestep = width2 / (double)Math.abs( maxX - minX );

        for( double i = width2 * offXorigo; i < width2 ; i += scalestep ) {
            g.drawLine( (int)Math.round( i ) , (int)Math.round( height2 * offYorigo ) , (int)Math.round( i ) , (int)Math.round( height2 * offYorigo ) - 2);
        }
        for( double i = width2 * offXorigo; i > 0 ; i -= scalestep ) {
            g.drawLine( (int)Math.round( i ) , (int)Math.round( height2 * offYorigo ) , (int)Math.round( i ) , (int)Math.round( height2 * offYorigo ) - 2);
        }

        // scaling y-axis, offset origo
        scalestep =  height2 / Math.abs( maxY - minY );
        for( double i = height2 * offYorigo ; i < height2 ; i += scalestep ) {
            g.drawLine( (int)Math.round( width2 * offXorigo ) , (int)Math.round( i ) , (int)Math.round( width2 * offXorigo ) + 2 , (int)Math.round( i ) );
        }
        for( double i = height2 * offYorigo ; i > 0 ; i -= scalestep ) {
            g.drawLine( (int)Math.round( width2 * offXorigo ) , (int)Math.round( i ) , (int)Math.round( width2 * offXorigo ) + 2 , (int)Math.round( i ) );
        }
    }



    /**
    	Draws a function.
     
    	@param fnk the fucntion to draw
    	@param g a graphics object to draw on
    	@exception java.lang.Exception if an error occurs
    */
    void
    drawFunction( String fnk, Graphics g ) throws java.lang.Exception {
        boolean currentIsChecked,oldIsChecked;
        double xtemp,temp,oldTemp,tmp,scalestep;
        int xpoints[],ypoints[];
        double rpoints[];
        int width,height,p, r;
        boolean boolok[];

        boolok  = new boolean[ step ];
        xpoints = new int[ step ];
        ypoints = new int[ step ];
        rpoints = new double[ step ];
        height  = getSize().height;
        width   = getSize().width;

        scalestep = ( maxX - minX ) / (double)step;
        currentIsChecked = false;
        oldIsChecked = false;
        oldTemp = 0.0;
        temp = 0.0;
        tmp = 0.0;
        p = 0;
        r = 0;

        if( hasLimitsChanged() )
            drawAll();

        // if empty or stored, ignore.
        if( fnk.equals( "" ) || isStored( fnk ) )
            return;

        try {
            // draw.
            while( p < step ) {
                xpoints[ p ] = (int)Math.round( p * width / (double)step );
                oldTemp = temp;

                oldIsChecked = currentIsChecked;

                // put value into hashtable, faster than with string I hope
                xtemp = minX + p * scalestep;
                vals.put( this.currentvar , String.valueOf( xtemp ) );
                temp = ev.eval( fnk, vals );

                if ( ( Double.isNaN( temp ) ) || ( Double.isInfinite( temp ) ) ) {
                    currentIsChecked = false;
                } else {
                    currentIsChecked = true;
                }

                boolok[ p ] = false;

                if( temp > ( tmp = Math.abs( maxY - minY ) + .5 * ( maxY + minY ) ) ) {
                    ypoints[ p ] = (int)Math.round( height * offYorigo - tmp * height / ( maxY - minY ) );
                    currentIsChecked = false;
                } else if ( temp < ( tmp = -Math.abs( maxY - minY ) + .5 * ( maxY + minY ) ) ) {
                    ypoints[ p ] = (int)Math.round( height * offYorigo - tmp * height / ( maxY - minY ) );
                    currentIsChecked = false;
                } else {
                    ypoints[ p ] = (int)Math.round( height * offYorigo - temp * height / ( maxY - minY ) );
                }

                if( ( p > 0 ) && oldIsChecked && currentIsChecked && noGap( fnk, minX + ( p - 1 ) * scalestep, minX + p * scalestep, oldTemp, temp , 1 ) ) {
                    boolok[ p ] = true;
                }

                // check for interesting points , "roots"
                if( boolok[ p ]  && ( ( oldyval >= 0 && temp < 0 ) || ( temp >= 0 && oldyval < 0 ) ) ) {
                    // save the xvalue so we can calculate the exact point later
                    rpoints[ r ] = xtemp - ( ( Math.abs( xtemp ) - Math.abs( oldxval ) ) / 2 );
                    r++;
                }

                oldxval = xtemp;
                oldyval = temp;

                p++;
            }

            p = 0;

            // redraw line, netscape 3.0 won't draw correctly if put in above loop
            // may be an issue with screen updates.
            while( p < step ) {
                if( p > 0 && boolok[ p ] )
                    g.drawLine( xpoints[p-1], ypoints[p-1], xpoints[p], ypoints[p] );
                p++;
            }

            // store funk
            storeFunction( fnk , xpoints , ypoints , boolok , g.getColor() , r, rpoints );
        } finally {
            
            vals.remove( this.currentvar );
        }
    }


    /**
    	Method used to determine if a function has a gap in it since 
    	we dont want to draw non existing lines.
     
    	@param fnk the function to check
    	@param xlow the lower x value
    	@param xhi the higher x value
    	@param ylow the lower y value
    	@param yhi the higher y value
    	@param depth determines how deep the check goes 
    	@return true if the fucntion has a gap in the specified interval, false otherwise.
    	
    */
    boolean
    noGap( String fnk, double xlow, double xhi, double ylow, double yhi, int depth ) {
        int hiSlopeBound = 8000; // highest allowed slope.
        int loSlopeBound = 4;    // the slope at which this routine begins testing
        double ymid;

        //  If slope is small this returns true and does not check futher.
        //  The lower bound grows exponentially with depth.
        if ( Math.abs( ( yhi - ylow ) / ( xhi - xlow ) ) < loSlopeBound * depth ) {
            return true;
        } else if ( Math.abs( ( yhi - ylow ) / ( xhi - xlow ) ) > hiSlopeBound ) {
            //  If the slope is greater than the high slope bound, we don't graph
            //  for fear that it may be unbounded on the interval.
            return false;
        } else {
            //  If slope was "in between", we split the interval in half, determine the
            //  half with the largest slope, and recursively continue the test.
            vals.put( this.currentvar , String.valueOf( ( xlow + xhi ) / 2 ) );

            try {
                ymid = ev.eval( fnk, vals );
            } catch( Exception e ) {
                return false;
            }

            //   Test to see if the function is defined at the upper endpoint.
            if ( ( Double.isNaN( ymid ) ) || ( Double.isInfinite( ymid ) ) ) {
                return false;
            }

            if ( Math.abs( ( yhi - ymid ) / ( xhi - xlow ) ) < Math.abs( ( ymid - ylow ) / ( xhi - xlow ) ) ) {
                return noGap( fnk, xlow, ( xlow + xhi ) / 2, ylow, ymid, 2 * depth );
            } else {
                return noGap( fnk, ( xlow + xhi ) / 2, xhi, ymid, yhi, 2 * depth );
            }
        }
    }


    /**
    	Returns a random color used when drawing functions.
     
    	@return a random Color
    */
    Color
    randomColor() {
        int i = 0;
        int lim = 100; // limit for how dark red and green can be

        Color c = new Color( rand.nextFloat() , rand.nextFloat() , rand.nextFloat() );

        while( c.getRed() < lim || c.getGreen() < lim || c.getBlue() < lim ) {
            i++;

            c = new Color( rand.nextFloat() , rand.nextFloat() , rand.nextFloat() );

            // sanity check, if loop count is ten just break
            if( i > 10 ) {
                c = fg; // use the foreground color
                break;
            }
        }

        return( c );
    }

    /**
    	Draws a previously stored Function object
     
    	@param fnk a Function object to draw
    	@param g a graphics object
    	@exception java.lang.Exception if an error occurs
    */
    void
    drawFunction( Function fnk, Graphics g ) throws java.lang.Exception {
        int p = 1;

        g.setColor( fnk.color );

        if( fnk.needUpdate( minX, maxX, minY, maxY, step, this.getSize().width, this.getSize().height ) ) {
            removeFunction( fnk.function );
            drawFunction( fnk.function , g );
        } else {
            while( p < fnk.step ) {
                if( p > 0 && fnk.pointok[ p ] ) {
                    g.drawLine( fnk.xpoints[p-1], fnk.ypoints[p-1], fnk.xpoints[p], fnk.ypoints[p] );
                }
                p++;
            }
        }
    }

    /**
    	Draws a function
     
    	@param fnk a function to draw
    	@exception java.lang.Exception if an error occurs
    */
    public final void
    drawFunction(String fnk) throws java.lang.Exception {
        Graphics g = this.getGraphics();
        g.setColor( randomColor() );
        drawFunction(fnk,g);
        g.dispose();
    }

    /**
    	Redraws all stored functions
     
    	@param g a graphics object
    	@exception java.lang.Exception if an error occurs
    */
    void
    drawAllFunctions(Graphics g) throws java.lang.Exception {
        Function fnk;
        Enumeration<Function> en;

        en = this.functions.elements();

        while( en.hasMoreElements() ) {
            fnk = (Function)en.nextElement();
            drawFunction( fnk , g );
        }
    }

    /**
    	Draws the label for the function closest to the specified point in the coordinate system
     
    	@param g a graphics object
    	@param x the x point in the coordinate system, pixelwise
    	@param y the y point in the coordinate system, pixelwise
    	@param width the width in pixels of this coordinate system
    	@param height the height in pixels of this coordinate system
    	@param valx the corresponding x-point "in reality" 
    	@param valy the corresponding y-point "in reality" 
    */
    void
    drawLabel( Graphics g, int x , int y, int width, int height, double valx, double valy ) {
        Enumeration<Function> en;
        Function funk;
        int sw, sh, des;
        double prox;

        // search for closest Function and draw the label
        en = functions.elements();

        if( oldlbl != null ) {
            g.clipRect( oldlbl.x, oldlbl.y, oldlbl.width, oldlbl.height );
            try {
                drawAll( g );
            } catch( Exception ex ) {}
            g.setClip( 0, 0 , width, height );
            oldlbl = null;
        }

        // calculate the proximety to the function we accept
        prox = Math.abs( maxY - minY ) / (double)step;

        while( en.hasMoreElements() ) {
            funk = (Function)en.nextElement();

            try {
                vals.put( this.currentvar , String.valueOf( valx ) );
                if( Math.abs( ev.eval( funk.function , vals ) - valy ) < prox ) {
                    sw  = fmetrics.stringWidth( funk.function );
                    des = fmetrics.getDescent();
                    sh  = fmetrics.getAscent() + des;

                    g.setColor( funk.color );
                    // sw + 2 since we want a bit of padding, 1 pix on each side
                    // x+2, y+2 so we get some offset from the cursor
                    g.fillRect( x + 2, y + 2, sw + 2, sh );
                    g.setColor( fg );
                    g.draw3DRect( x + 2, y + 2, sw + 2, sh, true );
                    g.setColor( bg );
                    // x + 3 so we get 1 pix padding and 2 pix offset from the cursor
                    // y + 3 ... because of cursor offset and 1 pix 3d rect
                    g.drawString( funk.function, x + 3, y + 3 + sh / 2 + des );

                    // save rectangle so we can draw over it next time.
                    // sw + 3 because of the 2 pix padding + 1 pix for the 3d rect,
                    // sh + 1 because of 3d rect
                    // x+2, y+2 so we get some offset from the cursor
                    oldlbl = new Rectangle( x + 2, y + 2, sw + 3, sh + 1 );

                    break;
                }
            } catch( Exception ex ) {}
        }
    }

    /**
    	Redraws all the stored fucntions.
     
    	@exception java.lang.Exception if an error occurs
    */
    void
    drawAllFunctions() throws java.lang.Exception {
        Graphics g = this.getGraphics();
        drawAllFunctions(g);
        g.dispose();
    }

    /**
    	Stores a function together with already calculated points and the specified Color
     
    	@param fnk the function 
    	@param xvals points along the x-axis as pixel values, used when redrawing this function
    	@param yvals points along the y-axis as pixel values, used when redrawing this function
    	@param boolok determines if the function has a gap or not in the corresponding point
    	@param color the Color to draw this function in
    	@param points the number of points stored
    	@param rpoints interesting values, possible "near" roots
    	@see Function
    */
    boolean
    storeFunction( String fnk, int xvals[], int yvals[], boolean boolok[], Color color , int points, double rpoints[] ) {
        Function f = new Function( fnk, xvals, yvals, boolok, color, minX, maxX, minY, maxY, step, this.getSize().width, this.getSize().height, points, rpoints );

        if( ! this.functions.containsKey( fnk ) ) {
            this.functions.put(fnk,f);
            return true;
        }

        return false;
    }


    /**
    	Returns all the stored fucntions as an Enumeration of fucntion objects
     
    	@return an Enumeration of Function objects
    	@see Function
     
    */
    public Enumeration<String>
    getFunctions() {
        return this.functions.keys();
    }

    /**
    	Sets the variable to use as "x-axis"
     
    	@param var the variable to use
    */
    public void
    setVar( String var ) {
        this.currentvar = var;
    }

    /**
    	Removes the specified function from storage, not the coordinate system
     
    	@param fnk the function to remove
    */
    public void
    removeFunction(String fnk) {
        this.functions.remove( fnk );
    }

    /**
    	Determines if this function is stored or not
     
    	@param fnk the fucntion to check
    	@return true if the function is stored, false otherwise
    */
    boolean
    isStored(String fnk) {
        return( this.functions.containsKey(fnk) );
    }

    /**
    	Determines if the bounds have changed
     
    	@return true if the bounds have changed, false otherwise
    */
    boolean
    hasLimitsChanged() {
        if( step != stepold || minX != minXold || maxX != maxXold
                || minY != minYold || maxY != maxYold ) {
            stepold = step;
            minXold = minX;
            maxXold = maxX;
            minYold = minY;
            maxYold = maxY;

            return true;
        } else {
            return false;
        }
    }

    /**
    	Mouse listener class for the CoordSystem
    */
    private class CoordSystem_MouseListener extends MouseAdapter {

        /**
        	Sets the starting point if a "drag" is being made
        */
        public void
        mousePressed(MouseEvent e) {
            if( ! drag ) {
                downx = e.getX();
                downy = e.getY();
                startx = e.getX();
                starty = e.getY();
            }
        }

        /**
        	Sets the new bounds if a "zoom" was made by selecting an area with the mouse<br>
        	or shows the popupmenu if any was added to the coordinate system and the<br>
        	system specific popup trigger was pressed.
        */
        public void
        mouseReleased(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            if( drag ) {
                drag = false;

                int width = getSize().width;
                int height = getSize().height;

                double tmpx = ( maxX-minX );
                double tmpy = ( maxY-minY );

                // ignore if width, height is less than 5 pixel
                if( dragwidth > 5 || dragheight > 5 ) {
                    // calculate bounds
                    maxX = ( minX + x * tmpx / (double)width );
                    minX = ( minX + downx * tmpx / (double)width );
                    minY = ( maxY - y * tmpy / (double)height );
                    maxY = ( maxY - downy * tmpy / (double)height );

                    // swap x values if necessary
                    if( minX > maxX ) {
                        tmpx = maxX;
                        maxX = minX;
                        minX = tmpx;
                    }

                    // swap y values if necessary
                    if( minY > maxY ) {
                        tmpy = maxY;
                        maxY = minY;
                        minY = tmpy;
                    }
                }

                // draw it all with new bounds
                try {
                    drawAll();
                } catch( Exception ex ) {}
                ;

                postEvent( new CoordEvent( this, null, minX, maxX , minY, maxY ) );
            } else if(  e.isPopupTrigger() ) {
                if( pop != null )
                    pop.show( CoordSystem.this, x, y );
            }

        }
    }


    /**
    	Mousemotion listener for the CoordSystem
    */
    private class CoordSystem_MouseMotionListener extends MouseMotionAdapter {
        /**
        	Draws a rectangle if an area is being selected.
        */
        public void
        mouseDragged(MouseEvent evt) {
            Graphics g = getGraphics();

            int x = evt.getX();
            int y = evt.getY();

            // alternate between bg and yellow
            g.setColor( bg );
            g.setXORMode( dragcolor );

            // if area already selected, draw over last rectangle
            if( drag )
                g.drawRect( startx , starty , dragwidth , dragheight );

            // calculate width , height for new rectangle
            dragwidth = Math.abs( x - downx );
            dragheight = Math.abs( y - downy );

            // flip if current point is less than where mouse was pressed
            startx = ( x < downx ? x : startx );
            starty = ( y < downy ? y : starty );

            // draw rect
            g.drawRect( startx , starty , dragwidth , dragheight );

            // flag that a drag has been made, used to check wheter to
            // draw over and to prevent drawAll to be called in mouseup
            // if no area was selected
            drag = true;

            //g.dispose();

            // call mouseMove so coordinates still get written
            mouseMoved( evt );
        }

        /**
        	Draws a rectangle if an area is being selected,<br>
        	draws the x and y values in the lower right corner and<br>
        	draws a label if the mouse is close to a function.
        */
        public void
        mouseMoved( MouseEvent e ) {
            Graphics g;
            int sw, sh, tmp ,pnt;
            int width, height, offset;
            double valx, valy;
            StringBuffer sb;

            int x = e.getX();
            int y = e.getY();

            offset = 8;
            width = getSize().width;
            height = getSize().height;
            sw = fmetrics.stringWidth( text );
            sh = fmetrics.getHeight();
            sb = new StringBuffer( 25 );
            g  = getGraphics();

            valx = minX + x * ( ( Math.abs( maxX-minX ) ) / (double)width );
            valy = maxY - y * ( ( Math.abs( maxY-minY ) ) / (double)height );

            sb.append( "( " );
            sb.append(  numform.format( valx ) );
            sb.append( " ; " ).append( numform.format( valy ) );
            sb.append( " )" );

            text = sb.toString();

            tmp = fmetrics.stringWidth( text );

            pnt = ( tmp < sw ? sw : tmp );

            g.setColor( bg );
            g.fillRect( width - pnt - offset, height - sh - offset, pnt + 2, sh + 2 );

            // set clip rect
            if( sw + 2 - tmp > 0 ) {
                g.clipRect( width - sw - offset, height - sh - offset, sw + 2 - tmp , sh + 2 );
                // draw pieces that may be missing
                try {
                    drawAll( g );
                } catch( Exception ex ) {}
                ;

                // set the clip back again
                g.setClip( 0, 0 , width, height );
            }

            g.setColor( fg );

            // write coordinates
            g.drawString( text , width - tmp - offset, height - offset );

            // draw function label if mouse close to any
            if( ! drag )
                drawLabel( g, x, y, width, height, valx, valy );

            g.dispose();
        }

    }


} // end class CoordSystem.




