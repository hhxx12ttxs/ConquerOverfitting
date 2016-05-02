//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.plexiViewer.lite.viewer.MiriadMrxSeg;
import ij.*;
import ij.process.*;
import java.awt.*;
import java.awt.event.*;
import org.nrg.plexiViewer.lite.manager.PlexiManager;


public class SegCanvas extends ij.gui.ImageCanvas {
    
	SegImage _image;
	int startX, startY;
	int xhandled, yhandled;
	private boolean syncFlag;
	int crosshairX = -1;
	int crosshairY = -1;
	Image buffer;
	Graphics bufferGraphics;
	boolean mouseDown = false;
	SegColorModel cm;
	int[] volCount=null;
	int currentRegion ;
    
	/** Creates new MRCanvas */
	public SegCanvas(SegImage image) {
		super(image.getStrImage());
		_image = image;
		int scale = image.getScale();
		super.setMagnification(scale);
	   // System.out.println("Super " + getMagnification());
		int height = image.getStrImage().getHeight();
		int width = image.getStrImage().getWidth();
		super.setDrawingSize(width*scale,height*scale );
		setSize(width*scale,height*scale);
		currentRegion=((Integer)PlexiManager.getSetting("SegImage.regionID")).intValue();
	}
 
    
	private void buildColorModel(){
 		if (cm == null)
			cm = new SegColorModel();
		cm.setRegion(currentRegion);
 		cm.makeDefaultLUT();
		_image.getAsegImage().getProcessor().setColorModel(cm.getCm() );
	}
   
    
    public void updateAndDraw() {
		buildColorModel();
    }
    
	public void setCmRegion(int region){
		   currentRegion = region;	
		   buildColorModel();
		   this.repaint();
	}
    
	public void paint(Graphics g) {
		//System.out.println("Magnification is " + super.getMagnification());
		String s;
		if (_image.getOrientation().equalsIgnoreCase("sagittal"))
			s = "";
		else if (_image.getRadiologic())
			s = "L";
		else
			s = "R";
        
		Rectangle r = super.getSrcRect();
		int scale = _image.getScale();
        
		if (bufferGraphics == null){
			buffer = this.createImage(r.width*scale, r.height*scale);
			bufferGraphics = buffer.getGraphics();
		}
        
		//fill in the background...
		bufferGraphics.setColor(Color.black);
		bufferGraphics.fillRect(0,0,r.width*scale,r.height*scale);
        
		ImagePlus str = _image.getStrImage();
		ImagePlus seg = _image.getAsegImage();
        
		seg.setSlice(str.getCurrentSlice() );
		str.updateImage();
		seg.updateImage();
		Image img = str.getImage();
		if (img!=null){
		  Point dst2 = new Point((int) (r.width* scale), (int) (r.height* scale));
		  Point src1 = new Point( r.x, r.y);
		  Point src2 = new Point( r.x + r.width , r.y + r.height );
		  bufferGraphics.drawImage(img, 0, 0, dst2.x, dst2.y, src1.x, src1.y, src2.x, src2.y, null);
		}

		img = seg.getImage();
		if (img!=null){
		  Point dst2 = new Point((int) (r.width* scale), (int) (r.height* scale));
		  Point src1 = new Point( r.x, r.y);
		  Point src2 = new Point( r.x + r.width , r.y + r.height );
		  bufferGraphics.drawImage(img, 0, 0, dst2.x, dst2.y, src1.x, src1.y, src2.x, src2.y, null);
		}
        if (str.getWindow()!=null) {
			 if (((SegStackWindow)str.getWindow()).markRight()) {
			//draw overlaid text and crosshairs...	
			 bufferGraphics.setColor(new Color(50,200,25));
			 bufferGraphics.drawString(s,r.width*scale - 20, r.height*scale - 10);
			}
        }
		if (str.getWindow()!=null) {
			if (((SegStackWindow)str.getWindow()).showCrossHair()) {
				//draw crosshairs at the mousepoint
				bufferGraphics.setColor(new Color(100, 255, 50));
				double space = 5;
				bufferGraphics.drawLine(0, crosshairY, crosshairX-(int)(space),crosshairY);
				bufferGraphics.drawLine(crosshairX+(int)(space), crosshairY, (getBounds().width*scale),crosshairY);
				bufferGraphics.drawLine(crosshairX,0, crosshairX,crosshairY - (int)(space));
				bufferGraphics.drawLine(crosshairX, crosshairY + (int)(space), crosshairX, (scale*getBounds().height));
			}
		}	
		g.drawImage(buffer, 0, 0, this);
	}
    
	   public Dimension getPreferredSize() {
	        int scale = _image.getScale();
	        int height = _image.getImagePlus().getHeight();
	        int width = _image.getImagePlus().getWidth();
	    	return new Dimension(width*scale,height*scale );
	    }
    
	public void mouseEntered(MouseEvent e) {
	}
    
	public void mousePressed(MouseEvent e){
		Point p = e.getPoint();
		startX = p.x;
		startY = p.y;
		xhandled = 0;
		yhandled = 0;
	}
    
	public void mouseClicked(MouseEvent e){
		Point p = e.getPoint();
		 _image.syncEvent(p.x,p.y,true); 
	}
    
    
	public void mouseReleased(MouseEvent e){
		  super.mouseReleased(e);
	}
    
	public void setCursor(int x, int y){
		//at this point, we want to ignore these cursor calls
		//so that we can have a wait cursor.
	}
    
	public void mouseDragged(MouseEvent e) {
        
       
		 //if it's the magnifier or control key is NOT pressed, use super's routine
		int flags = e.getModifiers();
		ImagePlus imp = _image.getStrImage();
		Point p = e.getPoint();
		ImageProcessor ip = imp.getProcessor();
        
		int xdist = p.x - startX;
		int xincr = xdist - xhandled;
		xhandled = xdist;
		//System.out.println("startX: " + startX + "   p.x: " + p.x + "   xhandled: " + xhandled + "   xincr: " + xincr);
		int ydist = p.y - startY;
		int yincr = ydist - yhandled;
		yhandled = ydist;
		//System.out.println("startY: " + startY + "   p.y: " + p.y + "   yhandled: " + yhandled + "   yincr: " + yincr);
        
	   // if (flags == ){
		//    adjustBrightness(ip,xincr);
		//    adjustContrast(ip,yincr);
	   // } else {
		adjustAlpha(xincr);
		buildColorModel();
		imp.updateAndDraw();  
        
	}
    
    
	public void update(Graphics g) {
		   paint(g);
	}

	void adjustAlpha(double bvalue) {
		//bvalue should range from 0-255
		int a = cm.getAlpha();
		a = a + ( (int)bvalue * 10 );
		if (a > 255) a = 255;
		if (a < 0 ) a = 0;
		cm.setAlpha(a);
		buildColorModel();
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
    
	public String getVolumeCount(int index) {
		String rtn="";
		if (_image.getDisplay().equalsIgnoreCase("MONTAGE"))
			rtn = "Req. Stack display";
		if (volCount!=null && 0<=index && index<=volCount.length)
			rtn = ""+volCount[index];
		System.out.println("getVolumeCount retn " + index +  " " + rtn);	
		return rtn;	 
	}
    
    public void setVolumeCount(int[] count) {
    	volCount = count;
    }
}

