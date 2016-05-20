//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.plexiViewer.lite.viewer.Seg;
import ij.process.*;
import java.awt.*;

public class SegByteProcessor extends ByteProcessor {
    
    public SegByteProcessor(int width, int height){
        super(width,height);
    }
    
    public SegByteProcessor(Image img) {
        super(img);
    }
    
    /** Uses bilinear interpolation to find the pixel value at real coordinates (x,y). */
    /*private final double getBetterInterpolatedPixel(double x, double y, byte[] pixels) {
        int xbase = (int)x;
        int ybase = (int)y;
        double xFraction = x - xbase;
        double yFraction = y - ybase;
        int offset = ybase * width + xbase;
        int lowerLeft = pixels[offset]&255;
        if ((xbase>=(width-1))||(ybase>=(height-1)))
            return lowerLeft;
        int lowerRight = pixels[offset + 1]&255;
        int upperRight = pixels[offset + width + 1]&255;
        int upperLeft = pixels[offset + width]&255;
        //double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
        //double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);
        //return lowerAverage + yFraction * (upperAverage - lowerAverage);
        double upperAverage = (upperLeft * (1.0 - xFraction)) + (upperRight * xFraction);
        double lowerAverage = (lowerLeft * (1.0 - xFraction)) + (lowerRight * xFraction);
        return (lowerAverage * (1-yFraction)) + (upperAverage * yFraction);
    }*/
    
    /** Creates a new ByteProcessor containing a scaled copy of this image or selection.
     * @see ij.process.ImageProcessor#setInterpolate
     */
   /* public ImageProcessor resize(int dstWidth, int dstHeight) {
        if (roiWidth==dstWidth && roiHeight==dstHeight)
            return crop();
        double srcCenterX = roiX + roiWidth/2.0;
        double srcCenterY = roiY + roiHeight/2.0;
        double dstCenterX = dstWidth/2.0;
        double dstCenterY = dstHeight/2.0;
        double xScale = (double)dstWidth/roiWidth;
        double yScale = (double)dstHeight/roiHeight;
        ImageProcessor ip2 = createProcessor(dstWidth, dstHeight);
        byte[] pixels2 = (byte[])ip2.getPixels();
        double xs, ys;
        int index1, index2;
        for (int y=0; y<=dstHeight-1; y++) {
            ys = (y-dstCenterY)/yScale + srcCenterY;
            index1 = width*(int)ys;
            index2 = y*dstWidth;
            for (int x=0; x<=dstWidth-1; x++) {
                xs = (x-dstCenterX)/xScale + srcCenterX;
                if (interpolate)
                    pixels2[index2++] = (byte)((int)(getBetterInterpolatedPixel(xs, ys, pixels)+0.5)&255);
                else
                    pixels2[index2++] = pixels[index1+(int)xs];
            }
            if (y%20==0)
                showProgress((double)y/dstHeight);
        }
        hideProgress();
        return ip2;
    }
    
    */
    
    private final double getBetterInterpolatedPixel(double x, double y, byte[] pixels) {
        int xbase = (int)x;
        int ybase = (int)y;
        double xFraction = x - xbase;
        double yFraction = y - ybase;
        int offset = ybase * width + xbase;
        int lowerLeft = pixels[offset]&255;
        //if ((xbase>=(width-1))||(ybase>=(height-1)))
        //	return lowerLeft;
        int lowerRight = pixels[offset + 1]&255;
        int upperRight = pixels[offset + width + 1]&255;
        int upperLeft = pixels[offset + width]&255;
        double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
        double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);
        return lowerAverage + yFraction * (upperAverage - lowerAverage);
    }
    
    public ImageProcessor resize(int dstWidth, int dstHeight) {
        if (roiWidth==dstWidth && roiHeight==dstHeight)
            return crop();
        double srcCenterX = roiX + roiWidth/2.0;
        double srcCenterY = roiY + roiHeight/2.0;
        double dstCenterX = dstWidth/2.0;
        double dstCenterY = dstHeight/2.0;
        double xScale = (double)dstWidth/roiWidth;
        double yScale = (double)dstHeight/roiHeight;
        if (interpolate) {
            dstCenterX += xScale/2.0;
            dstCenterY += yScale/2.0;
        }
        ImageProcessor ip2 = createProcessor(dstWidth, dstHeight);
        byte[] pixels2 = (byte[])ip2.getPixels();
        double xs, ys;
        double xlimit = width-1.0, xlimit2 = width-1.001;
        double ylimit = height-1.0, ylimit2 = height-1.001;
        int index1, index2;
        for (int y=0; y<=dstHeight-1; y++) {
            ys = (y-dstCenterY)/yScale + srcCenterY;
            if (interpolate) {
                if (ys<0.0) ys = 0.0;
                if (ys>=ylimit) ys = ylimit2;
            }
            index1 = width*(int)ys;
            index2 = y*dstWidth;
            for (int x=0; x<=dstWidth-1; x++) {
                xs = (x-dstCenterX)/xScale + srcCenterX;
                if (interpolate) {
                    if (xs<0.0) xs = 0.0;
                    if (xs>=xlimit) xs = xlimit2;
                    pixels2[index2++] = (byte)((int)(getBetterInterpolatedPixel(xs, ys, pixels)+0.5)&255);
                } else
                    pixels2[index2++] = pixels[index1+(int)xs];
            }
            if (y%20==0)
                showProgress((double)y/dstHeight);
        }
        hideProgress();
        return ip2;
    }
    
    
}

