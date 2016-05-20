// (c) MIT 2003.  All rights reserved.

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// AUTHOR:      Tevfik Metin Sezgin                                           //
//              Massachusetts Institute of Technology                         //
//              Department of Electrical Engineering and Computer Science     //
//              Artificial Intelligence Laboratory                            //
//                                                                            //
// E-MAIL:        mtsezgin@ai.mit.edu, mtsezgin@mit.edu                       //
//                                                                            //
// COPYRIGHT:   Tevfik Metin Sezgin                                           //
//              All rights reserved. This code can not be copied, modified,   //
//              or distributed in whole or partially without the written      //
//              permission of the author. Also see the COPYRIGHT file.        //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
package edu.mit.sketch.util;


/** 
  * 
  * See the end of the file for the log of changes.
  * 
  * $Author: moltmans $
  * $Date: 2003/03/06 01:08:54 $   
  * $Revision: 1.2 $
  * $Headers$
  * $Id: GraphicsUtil.java,v 1.2 2003/03/06 01:08:54 moltmans Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /projects/drg/CVSROOT/drg/code/src/edu/mit/sketch/util/GraphicsUtil.java,v $
  *  
  **/



import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;


/**
  *
  * Implements some methods not found in jdk 1.1
  *
  **/
public class
GraphicsUtil
{
    /**
    *
    * Plot a thick line from (x1, y1) to (x2,y2) with radius r
    *
    **/
    public static void
    drawThickLine( int r, Graphics g, int x1, int y1, int x2, int y2 ) 
    {
        if ( r == 1 ) {
            g.drawLine( x1, y1, x2, y2 );
            return;
        }
        int iterations = (int)Math.sqrt( (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) );
        for ( int i=0; i<iterations; i++ ) {
            g.fillOval( ( x1*i + x2*(iterations-i) )/iterations,
                        ( y1*i + y2*(iterations-i) )/iterations,
                        r,
                        r);
        }
    }
    
    /**
    *
    * Plot a thick line from (x1, y1) to (x2,y2) with radius r
    *
    **/
    public static void
    drawThickLine( double r, 
                   Graphics g, 
                   double x1, 
                   double y1, 
                   double x2, 
                   double y2 ) 
    {
        if ( r == 1 ) {
            g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
            return;
        }
        int iterations = (int)Math.sqrt( (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) );
        for ( int i=0; i<iterations; i++ ) {
            g.fillOval( (int)(( x1*i + x2*(iterations-i) )/iterations),
                        (int)(( y1*i + y2*(iterations-i) )/iterations),
                        (int)r,
                        (int)r );
        }
    }
    
    
    /**
    *
    * Plot a thick line from (x1, y1) to (x2,y2) with radius r
    *
    **/
    public static void
    drawThickOval( int r,  Graphics g, int x, int y, int r1, int r2 )
    {   
        if ( r == 1 ) {
            g.drawOval( x, y, r1, r2 );
            return;
        }     
        for ( int i=0; i<r; i++ ) {
            g.drawOval( x+i,
                         y+i,
                         r1-i*2,
                         r2-i*2);
        }
    }
    
    
    /**
    *
    * Set the constraints of the input component using the arguments.
    *
    **/
    public static void
    setConstraints( GridBagLayout layout, 
                    Component     component,
                    int           x, 
                    int           y, 
                    int           column_span,
                    int           row_span,
                    double        weightx,
                    double        weighty,
                    int           fill,
                    int           anchor,
                    Insets        insets )
    {   
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx      = x;
        gbc.gridy      = y;
        gbc.gridwidth  = column_span;
        gbc.gridheight = row_span;
        gbc.weightx    = weightx;
        gbc.weighty    = weighty;
        gbc.fill       = fill;
        gbc.anchor     = anchor;
        gbc.insets     = insets;
        
        layout.setConstraints( component, gbc );
    }


    /**
    *
    * Clear the component
    *
    **/
    public static void
    clearComponent( Component component ) 
    {
        Graphics  g = component.getGraphics();
        g.setColor( Color.white );
        g.fillRect( 0, 
                    0, 
                    component.getWidth(),
                    component.getHeight() );
    }
}


/** 
  * 
  * $Log: GraphicsUtil.java,v $
  * Revision 1.2  2003/03/06 01:08:54  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.1  2001/11/23 03:24:32  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.1.1.1  2001/03/29 16:25:01  moltmans
  * Initial directories for DRG
  *
  * Revision 1.10  2000/09/06 22:40:37  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.4  2000/06/03 01:52:33  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.3  2000/04/28 04:45:03  mtsezgin
  *
  * Now each GeometricObject keeps the mouse input that was previously
  * discarded. User can switch between seeing the recognized mode and
  * the raw mode. setDataPoints( Polygon points ) and getDataPoints()
  * are added to GeometricObject, and all the implementors are modified
  * accordingly.
  *
  * Revision 1.2  2000/04/25 22:15:29  mtsezgin
  *
  * Added more utility functions.
  *
  * Revision 1.1.1.1  2000/04/01 03:07:07  mtsezgin
  * Imported sources
  *
  * Revision 1.2  2000/03/31 22:41:03  mtsezgin
  *
  * Started Log tracking.
  *
  *  
  **/

