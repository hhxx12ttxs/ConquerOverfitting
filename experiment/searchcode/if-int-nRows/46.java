//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.plexiViewer.utils.Transform;

import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;
import ij.measure.Calibration;

 
public class PlexiMontageMaker {
    
    private  int columns, rows, first, last, inc;
    private  double scale;
    private  boolean label=false, borders=false;
    private  int saveID;
    
    public ImagePlus makeMontage(ImagePlus imp) {
        
        if (imp==null || imp.getStackSize()==1)
        {IJ.error("Stack required"); return null;}
        
        int nSlices = imp.getStackSize();
        if (columns==0 || imp.getID()!=saveID) {
            columns = (int)Math.sqrt(nSlices);
            rows = columns;
            int n = nSlices - columns*rows;
            if (n>0) columns += (int)Math.ceil((double)n/rows);
            scale = 1.0;
            if (imp.getWidth()*columns>800)
                scale = 0.5;
            if (imp.getWidth()*columns>1600)
                scale = 0.25;
            inc = 1;
            first = 1;
            last = nSlices;
        }
        
        GenericDialog gd = new GenericDialog("Make Montage", IJ.getInstance());
        gd.addNumericField("Columns:", columns, 0);
        gd.addNumericField("Rows:", rows, 0);
        gd.addNumericField("Scale Factor:", scale, 2);
        gd.addNumericField("First Slice:", first, 0);
        gd.addNumericField("Last Slice:", last, 0);
        gd.addNumericField("Increment:", inc, 0);
        gd.addCheckbox("Label Slices", label);
        gd.addCheckbox("Borders", borders);
        gd.showDialog();
        if (gd.wasCanceled())
            return null;
        columns = (int)gd.getNextNumber();
        rows = (int)gd.getNextNumber();
        scale = gd.getNextNumber();
        first = (int)gd.getNextNumber();
        last = (int)gd.getNextNumber();
        inc = (int)gd.getNextNumber();
        if (first<1) first = 1;
        if (last>nSlices) last = nSlices;
        if (inc<1) inc = 1;
        if (gd.invalidNumber()) {
            IJ.error("Invalid number");
            return null;
        }
        label = gd.getNextBoolean();
        borders = gd.getNextBoolean();
        return makeMontage(imp, columns, rows, scale, first, last, inc, label, borders);
    }
    
    public ImagePlus makeMontage(ImagePlus imp, int columns, int rows, double scale, int first, int last, int inc, boolean labels, boolean borders) {
        
  //      if (imp==null || imp.getStackSize()==1)
    //    {IJ.error("Stack required"); return null;}
        
        int stackWidth = imp.getWidth();
        int stackHeight = imp.getHeight();
        int nSlices = imp.getStackSize();
        int width = (int)(stackWidth*scale);
        int height = (int)(stackHeight*scale);
        int montageWidth = width*columns;
        int montageHeight = height*rows;
        ImageProcessor montage = imp.getProcessor().createProcessor(montageWidth, montageHeight);
        Color fgColor=Color.white;
        Color bgColor = Color.black;

        boolean whiteBackground = false;
        if ( (imp.getProcessor() instanceof ByteProcessor) || (imp.getProcessor() instanceof ColorProcessor)) {
            ImageStatistics is = imp.getStatistics();
            whiteBackground = is.mode>=200;
            if (imp.isInvertedLut())
                whiteBackground = !whiteBackground;
        }
        if (whiteBackground) {
            fgColor=Color.black;
            bgColor = Color.white;
        }
        montage.setColor(bgColor);
        montage.fill();
        montage.setColor(fgColor);

        ImageStack stack = imp.getStack();
        int x = 0;
        int y = 0;
        ImageProcessor aSlice;
        int slice = first;
        while (slice<=last) {
            aSlice = stack.getProcessor(slice);
			if (scale!=1.0)
			   aSlice = aSlice.resize(width, height);
            montage.insert(aSlice, x, y);
            if (borders) drawBorder(montage, x, y, width, height);
            if (labels) drawLabel(montage, slice, x, y, width, height);
            x += width;
            if (x>=montageWidth) {
                x = 0;
                y += height;
                if (y>=montageHeight)
                    break;
            }
            //IJ.showProgress((double)(slice-first)/(last-first));
            slice += inc;
        }
        if (borders) drawBorder(montage, 0, 0, montageWidth-1, montageHeight-1);
        //IJ.showProgress(1.0);
        ImagePlus montageImage =  new ImagePlus("", montage);
        montageImage.getProcessor().setColorModel(imp.getProcessor().getColorModel());
		montageImage.setCalibration(imp.getCalibration());
		Calibration cal = montageImage.getCalibration();
		if (cal.scaled()) {
			cal.pixelWidth /= scale;
			cal.pixelHeight /= scale;
		}
		return montageImage;
    }
    
    void drawBorder(ImageProcessor montage, int x, int y, int width, int height) {
        montage.moveTo(x, y);
        montage.lineTo(x+width, y);
        montage.lineTo(x+width, y+height);
        montage.lineTo(x, y+height);
        montage.lineTo(x, y);
    }
    
    void drawLabel(ImageProcessor montage, int slice, int x, int y, int width, int height) {
        String s = ""+slice;
        //int swidth = montage.getStringWidth(s);
        //x += width/2 - swidth/2;
        //y += height;
        y += 15 ;
        montage.moveTo(x, y);
        montage.drawString(s);
    }
    
    public static int getColumns(int noSlices, double aspect) {
    	int nColumns = Math.round((float)Math.sqrt(noSlices*aspect));
    	int nRows = (int)Math.ceil(noSlices /(float) nColumns);
    	int temp;
    	if (nColumns < nRows && nRows <= 6) {
    		nColumns = nRows;
    	}
    	return nColumns;	 
    }
    
    public static void main(String[] args) {
		int noSlices = ((216 - 97)) / 7 + 1;
		System.out.println("NO of Slices " + noSlices);
		System.out.println(PlexiMontageMaker.getColumns(noSlices,0.65));
    }
}



