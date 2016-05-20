//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.plexiViewer.utils.Transform;

import ij.*;
import ij.process.*;
import ij.measure.*;
import ij.gui.*;
import java.awt.*;



public class PlexiSlicer  {

	private double outputZSpacing = 1.0;
	private int outputSlices = 1;
	private boolean isLine;	
	private boolean noRoi;
	private boolean rgb;
	private boolean goCount;


	public ImagePlus doSlice(ImagePlus imp, String startLocation, boolean goCount,  boolean showProgress) {
		//System.out.println("Slicer...." + imp.getWidth() + "\t" + imp.getHeight() + "\t" + imp.getStackSize());
		int stackSize = imp.getStackSize();
		if (stackSize<2) {
			System.out.println("Unable to reslice Stack required");
			return null;
		}
		this.goCount = goCount;
		//Section of Show Dialog
		Calibration cal = imp.getCalibration();
		double outputSpacing = cal.pixelDepth;

		String units = cal.getUnits();
		if (cal.pixelWidth==0.0)
			cal.pixelWidth = 1.0;
		if (goCount) {
			outputZSpacing = 1;
			cal.pixelDepth = 1.0;
		} else {
			cal.pixelDepth = outputSpacing;
			outputZSpacing = outputSpacing/cal.pixelWidth;
		}

		ImagePlus imp2 = null;
 	    rgb = imp.getType()==ImagePlus.COLOR_RGB;
		//System.out.println("Slicer ....RGB " + rgb);
		imp2 = reslice(imp, startLocation, showProgress);
		if (imp2==null)
		  return null;
		//System.out.println("Slicer....Return 1 " + imp2.getWidth() + "\t" + imp2.getHeight() + "\t" + imp2.getStackSize());
		imp2.setCalibration(imp.getCalibration());
		cal = imp2.getCalibration();
		cal.pixelDepth = outputZSpacing*cal.pixelWidth;
		
		//System.out.println("Slicer....Return 2 " + imp2.getWidth() + "\t" + imp2.getHeight() + "\t" + imp2.getStackSize());
		return imp2;
	}

	
	private ImagePlus reslice(ImagePlus imp, String startLocation,  boolean showProgress) {
		Roi roi = imp.getRoi();
		int roiType = roi!=null?roi.getType():0;
		if (roi==null || roiType==Roi.RECTANGLE || roiType==Roi.LINE) {
				//System.out.println("Slicer .... ROI " + roiType );
				return resliceRectOrLine(imp, startLocation, showProgress);
		} else {
			System.out.println("Unable to Reslice...Line or rectangular selection required");
			return null;
		}
	}


	ImagePlus resliceRectOrLine(ImagePlus imp, String startLocation,  boolean showProgress) {
		double x1 = 0.0;
		double y1 = 0.0;
		double x2 = 0.0;
		double y2 = 0.0;
		double xInc = 0.0;
		double yInc = 0.0;
		noRoi = false;

		Roi roi = imp.getRoi();
		if (roi==null) {
			noRoi = true;
			imp.setRoi(0, 0, imp.getWidth(), imp.getHeight());
			roi = imp.getRoi();
		}
		//System.out.println("Slicer .... ROI TYPE " + roi.getType());
		if (roi.getType()==Roi.RECTANGLE) {
			Rectangle r = roi.getBoundingRect();
			//System.out.println("Bounding Rect " + r.x + "\t " + r.y + "\t" + r.width + "\t" + r.height);
			if (startLocation.equalsIgnoreCase("TOP")) { // top
				x1 = r.x;
				y1 = r.y;
				x2 = r.x + r.width;
				y2 = r.y;
				xInc = 0.0;
				yInc = outputZSpacing;
				outputSlices =  (int)(r.height/outputZSpacing);
				//System.out.println("Output Slices " + outputSlices);     
		   } else if (startLocation.equalsIgnoreCase("LEFT")) { // left
				x1 = r.x;
				y1 = r.y;
				x2 = r.x;
				y2 = r.y + r.height;
				xInc = outputZSpacing;
				yInc = 0.0;
				outputSlices =  (int)(r.width/outputZSpacing);      
			} else if (startLocation.equalsIgnoreCase("BOTTOM")) { // bottom
				x1 = r.x;
				y1 = r.y + r.height;
				x2 = r.x + r.width;
				y2 = r.y + r.height;
				xInc = 0.0;
				yInc = -outputZSpacing;
				outputSlices =  (int)(r.height/outputZSpacing);     
			} else if (startLocation.equalsIgnoreCase("RIGHT")) { // right
				x1 = r.x + r.width;
				y1 = r.y;
				x2 = r.x + r.width;
				y2 = r.y + r.height;
				xInc = -outputZSpacing;
				yInc = 0.0;
				outputSlices =  (int)(r.width/outputZSpacing);      
			}
		} else if (roi.getType()==Roi.LINE) {
				Line line = (Line)roi;
				x1 = line.x1;
				y1 = line.y1;
				x2 = line.x2;
				y2 = line.y2;
				double dx = x2 - x1;
				double dy = y2 - y1;
				double nrm = Math.sqrt(dx*dx + dy*dy)/outputZSpacing;
				xInc = -(dy/nrm);
				yInc = (dx/nrm);
	   } else
			return null;

		if (outputSlices==0) {
			  System.out.println("Reslicer...Output Z spacing ("+IJ.d2s(outputZSpacing,0)+" pixels) is too large.");
			  return null;
		}
		ImageStack stack=null;
		for (int i=0; i<outputSlices; i++)  {
			
			ImageProcessor ip = getSlice(imp, x1, y1, x2, y2, null);
			//System.out.println("IP " + ip.getWidth() + "\t" + ip.getHeight() + "\t");
			if (stack==null)
					stack = new ImageStack(ip.getWidth(), ip.getHeight());
					stack.addSlice(null, ip);
					x1 += xInc;
					x2 += xInc;
					y1 += yInc;
					y2 += yInc;
				if (showProgress) {
					double percent = (double) i/outputSlices;
					//PlexiGuiManager.showProgress(percent);
				}
	
		}
			//if (showProgress)
				//PlexiGuiManager.showProgress(1.0);
        
			return new ImagePlus(imp.getTitle(), stack);
	}

	ImageProcessor getSlice(ImagePlus imp, double x1, double y1, double x2, double y2, String status) {
		  Roi roi = imp.getRoi();
		  int roiType = roi!=null?roi.getType():0;
		  ImageStack stack = imp.getStack();
		  int stackSize = stack.getSize();
		  ImageProcessor ip,ip2=null;
		  float[] line = null;
		  for (int i=0; i<stackSize; i++) {
			  ip = stack.getProcessor(i+1);
			  line = getLine(ip, x1, y1, x2, y2, line);
			  if (i==0) ip2 = ip.createProcessor(line.length, stackSize);
			  putRow(ip2, 0, i, line, line.length);
		  }
		  Calibration cal = imp.getCalibration();
		  double zSpacing = cal.pixelDepth/cal.pixelWidth;
		  
	   	  if (goCount) zSpacing=1.0; //do not want any Interpolation. Modification 
		  if (zSpacing!=1.0) {
			  ip2.setInterpolate(true);
			  ip2 = ip2.resize(line.length, (int)(stackSize*zSpacing));
		  }   
	  
		  return ip2;
	  }

	  public void putRow(ImageProcessor ip, int x, int y, float[] data, int length) {
		  if (rgb) {
			  for (int i=0; i<length; i++)
				  ip.putPixel(x++, y, Float.floatToIntBits(data[i]));
		  } else {
			  for (int i=0; i<length; i++)
				 ip.putPixelValue(x++, y, data[i]);
		  }
	  }



	private float[] getLine(ImageProcessor ip, double x1, double y1, double x2, double y2, float[] data) {
		  double dx = x2-x1;
		  double dy = y2-y1;
		  int n = (int)Math.round(Math.sqrt(dx*dx + dy*dy));
		  if (data==null)
			  data = new float[n];
		  double xinc = dx/n;
		  double yinc = dy/n;
		  double rx = x1;
		  double ry = y1;
		  for (int i=0; i<n; i++) {
				 data[i] = (float)ip.getInterpolatedValue(rx, ry);
			  rx += xinc;
			  ry += yinc;
		  }
		  return data;
	  }

}

