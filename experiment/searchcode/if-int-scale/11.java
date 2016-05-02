//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.plexiViewer.lite.viewer.MR;
/**
 *
 * @author  danm
 * @version
 */

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.nrg.plexiViewer.lite.manager.PlexiManager;

public class MRCanvas extends ij.gui.ImageCanvas {
    
    MRImage mrimage;
    int startX, startY;
    int xhandled, yhandled;
    private boolean syncFlag;
    int crosshairX = -1;
    int crosshairY = -1;
    Image buffer;
    Graphics bufferGraphics;
    
        
    /** Creates new MRCanvas */
    public MRCanvas(MRImage mrimage) {
        super(mrimage.getImagePlus());
        this.mrimage = mrimage;
        int scale = mrimage.getScale();
        int height = mrimage.getImagePlus().getHeight();
        int width = mrimage.getImagePlus().getWidth();
        super.setDrawingSize(width*scale,height*scale );
        setSize(width*scale,height*scale);
        System.out.println("Montage Scale is " + scale);
    }
    
    
    
    public  void paint(Graphics g) {
        String s;
        
        if (mrimage.getOrientation().equalsIgnoreCase("sagittal"))
            s = "";
        else if (mrimage.getRadiologic())
            s = "L";
        else
            s = "R";

        Rectangle r = super.getSrcRect();
        int scale = mrimage.getScale();
		ImagePlus ip = mrimage.getImagePlus();
		if (ip==null) {
			return;
		}
        
        if (bufferGraphics == null){
            buffer = this.createImage(r.width*scale, r.height*scale);
            bufferGraphics = buffer.getGraphics();
        }
        
        //fill in the background...
        bufferGraphics.setColor(Color.black);
        bufferGraphics.fillRect(0,0,r.width*scale,r.height*scale);
		
		ip.updateImage();
		Image img = ip.getImage();
		if (img!=null){
		  Point dst2 = new Point((int) (r.width* scale), (int) (r.height* scale));
		  Point src1 = new Point( r.x, r.y);
		  Point src2 = new Point( r.x + r.width , r.y + r.height );
		  bufferGraphics.drawImage(img, 0, 0, dst2.x, dst2.y, src1.x, src1.y, src2.x, src2.y, null);
		}
		
     	   //draw overlaid text and crosshairs...
			try {
				ImageWindow window = ip.getWindow();
				MRStackWindow mrstack = null;
				MRMontageWindow montageWindow = null;
				if (window instanceof MRStackWindow) {
					mrstack = (MRStackWindow)window;
				}else if (window instanceof MRMontageWindow) {
					montageWindow = (MRMontageWindow)window;
				}
				if ((mrstack != null && mrstack.markRight() ) || (montageWindow != null && montageWindow.markRight()))  {
			        bufferGraphics.setColor(new Color(50,200,25));
			        bufferGraphics.drawString(s,r.width*scale - 20, r.height*scale - 10);
				}
				if ((mrstack != null && mrstack.showRuler() ) || (montageWindow != null && montageWindow.showRuler()) )  {
			        bufferGraphics.setColor(new Color(225,225,225));
			        bufferGraphics.drawLine(10*scale,r.height*scale - 10, 60*scale, r.height*scale - 10);
			        bufferGraphics.drawLine(10*scale,r.height*scale - 10, 10*scale, r.height*scale - 15);
			        bufferGraphics.drawLine(15*scale,r.height*scale - 10, 15*scale, r.height*scale - 12);
			        bufferGraphics.drawLine(20*scale,r.height*scale - 10, 20*scale, r.height*scale - 15);
			        bufferGraphics.drawLine(25*scale,r.height*scale - 10, 25*scale, r.height*scale - 12);
			        bufferGraphics.drawLine(30*scale,r.height*scale - 10, 30*scale, r.height*scale - 15);
			        bufferGraphics.drawLine(35*scale,r.height*scale - 10, 35*scale, r.height*scale - 12);
			        bufferGraphics.drawLine(40*scale,r.height*scale - 10, 40*scale, r.height*scale - 15);
			        bufferGraphics.drawLine(45*scale,r.height*scale - 10, 45*scale, r.height*scale - 12);
			        bufferGraphics.drawLine(50*scale,r.height*scale - 10, 50*scale, r.height*scale - 15);
			        bufferGraphics.drawLine(55*scale,r.height*scale - 10, 55*scale, r.height*scale - 12);
			        bufferGraphics.drawLine(60*scale,r.height*scale - 10, 60*scale, r.height*scale - 15);
				}        
				if ((mrstack != null && mrstack.showCrossHair() ) || (montageWindow != null && montageWindow.showCrossHair()))  {
					//draw crosshairs at the mousepoint
					bufferGraphics.setColor(new Color(100, 255, 50));
					double space = 5;
					bufferGraphics.drawLine(0, crosshairY, crosshairX-(int)(space),crosshairY);
					bufferGraphics.drawLine(crosshairX+(int)(space), crosshairY, (getBounds().width*scale),crosshairY);
					bufferGraphics.drawLine(crosshairX,0, crosshairX,crosshairY - (int)(space));
					bufferGraphics.drawLine(crosshairX, crosshairY + (int)(space), crosshairX, (scale*getBounds().height));
		        }

			}catch(ClassCastException e){
				System.out.println("Exception thrown "+e);
			}
		
        g.drawImage(buffer, 0, 0, this);
        Roi roi = imp.getRoi();
        if (roi != null) roi.draw(g);
    }
    
    public Dimension getPreferredSize() {
        int scale = mrimage.getScale();
        int height = mrimage.getImagePlus().getHeight();
        int width = mrimage.getImagePlus().getWidth();
    	return new Dimension(width*scale,height*scale );
    }
    
	public void mousePressed(MouseEvent e){
		if (!PlexiManager.controlKeys) {
			super.mousePressed(e);
		}else {
			 Point p = e.getPoint();
			 startX = p.x;
			 startY = p.y;
			 xhandled = 0;
			 yhandled = 0;
		}
	 }

	   public void mouseClicked(MouseEvent e){
		   //TODO
			if (!PlexiManager.controlKeys) {
				super.mouseClicked(e);
			}else {
				   Point p = e.getPoint();
		 			if (mrimage != null){
						mrimage.setCoords(p.x, p.y);
						mrimage.printCoords();
						mrimage.syncEvent(p.x, p.y,true);
					}
			}
	   }	
		public void mouseReleased(MouseEvent e){
			if (!PlexiManager.controlKeys) {
				super.mouseReleased(e);
			}else {
				mrimage.printCoords();
			}
		 }
		 
	public void mouseMoved(MouseEvent e){
		if (!PlexiManager.controlKeys) {
			super.mouseMoved(e);
		}	
	 }
		 
    
  public void mouseDragged(MouseEvent e) {
		if (!PlexiManager.controlKeys) {
			super.mouseDragged(e);
		}else {
		  //if it's the magnifier or control key is NOT pressed, use super's routine
			 ImagePlus imp = mrimage.getImagePlus();
			 if (imp.lockSilently())
			 	return;	
			 Point p = e.getPoint();
			 ImageProcessor ip = imp.getProcessor();
	        
			 int xdist = p.x - startX;
			 int xincr = xdist - xhandled;
			 xhandled = xdist;
			 int ydist = p.y - startY;
			 int yincr = ydist - yhandled;
			 yhandled = ydist;
	        
			 adjustBrightness(ip,xincr);
			 adjustContrast(ip,yincr);
			 
			 imp.updateAndDraw();	
			 MRWindow win = (MRWindow)imp.getWindow();
			 win.setMessage("Range: ",(int)ip.getMin() + " to " + (int)ip.getMax());
			 win.message();
		}
	 }
    
	 void adjustBrightness(ImageProcessor ip, double bvalue) {
		 //bvalue should range from 0-255
		 double min = ip.getMin();
		 double max = ip.getMax();
		 ip.setMinAndMax(min-bvalue,max-bvalue);
		 if (min==max)
			 setThreshold(ip);
	 }
    
	 void adjustContrast(ImageProcessor ip, int cvalue) {
        
		 double min = ip.getMin();
		 double max = ip.getMax();
		 double center = (min+max)/2;
		 double range = max - min;
		 range = range + 4*cvalue;
		 if (range < 2)
			 range = 2.0;
		 ip.setMinAndMax(center - (range/2), center + (range/2));
	 }
    
	 void setThreshold(ImageProcessor ip) {
		 if (!(ip instanceof ByteProcessor))
			 return;
		 if (((ByteProcessor)ip).isInvertedLut())
			 ip.setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
		 else
			 ip.setThreshold(0, 255, ImageProcessor.NO_LUT_UPDATE);
	 }
    
	 void setCrosshairPosition(int x, int y){
		 crosshairX = x;
		 crosshairY = y;
	 }
      
    public void setCursor(int x, int y){
        //at this point, we want to ignore these cursor calls
        //so that we can have a wait cursor.
    }
}

