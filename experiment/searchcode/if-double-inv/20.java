/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utility;

import april.jmat.LinAlg;
import detect.Constants;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 *
 * @author jrpeterson
 */
public class ImageProc {
    
    // converts image to single matrix of intensity values
    public static double[][] imgtoGrey(BufferedImage img) {
        double[][] dataD = new double[img.getHeight()][img.getWidth()];
        if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) { // type 5
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();
            int i, v;
            for (int y = 0; y < img.getHeight(); y++) {
                i = 3 * (y * img.getWidth());
                for (int x = 0; x < img.getWidth(); x++) {
                    // inline for more speed!!
                    v = (int) data[i] & 0xff;       // blue
                    v += (int) data[i + 1] & 0xff;  // green 
                    v += (int) data[i + 2] & 0xff;  // red
                    dataD[y][x] = (double) v;
                    //dataD[y][x] /= 3.0;
                    i += 3;
                }
            }

            return dataD;
        } else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) { // type 6
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k, v;
            for (int y = 0; y < img.getHeight(); y++) {
                k = 4 * (y * img.getWidth());
                for (int x = 0; x < img.getWidth(); x++) {
                    // inline for more speed!!
                    v = (int) data[k + 1] & 0xff;       // blue
                    v += (int) data[k + 2] & 0xff;      // green
                    v +=  (int) data[k + 3] & 0xff;     // red
                    dataD[y][x] = (double) v;
                    //dataD[y][x] /= 3.0;
                    k += 4;
                }
            }
            return dataD;
        } else if (img.getType() == BufferedImage.TYPE_INT_RGB) { // type 1
            int data[] = ((DataBufferInt) (img.getRaster().getDataBuffer())).getData();

            int i, v;
            int rgb;
            for (int y = 0; y < img.getHeight(); y++) {
                i = y*img.getWidth();
                for (int x = 0; x < img.getWidth(); x++) {
                    rgb = data[i + x];
                    // inline for more speed!!
                    v = ((rgb & 0xff0000) >> 16); // red
                    v += ((rgb & 0xff00) >> 8);  // green
                    v += (rgb & 0xff); // blue
                    //dataD[y][x] /= 3.0;
                    dataD[y][x] = (double) v;
                }
            }

            return dataD;
        } else {
            System.out.println("Unsupported image format! = " + img.getType());
            assert false;
            return null;
        }

    }

    public static BufferedImage greytoImg(double[][] grey) {
	return greytoImg(grey, 1.0);
    }

    // converts single matrix of intensity values to an image
	public static BufferedImage greytoImg(double[][] grey, double scale) {
	BufferedImage img = new BufferedImage(grey[0].length, grey.length, BufferedImage.TYPE_INT_RGB);
	//byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

	for(int y = 0; y < img.getHeight(); ++y){
	    for(int x = 0; x < img.getWidth(); ++x){
		double v = grey[y][x]/3.0/scale;
		int val = ((int)(v))&0xff;
		img.setRGB(x, y, val<<16 | val<<8 | val);
		//int i = y*img.getWidth() + x;
		//data[i] = (byte)(((int)(grey[y][x]/3.0))&0xff);
	    }
	}

	return img;
    }
        
        
    public static double[] extractRGB(BufferedImage img) {
        double red = 0;
        double green = 0;
        double blue = 0;
        int total = 0;
        //System.out.println("image types " + BufferedImage.TYPE_4BYTE_ABGR + ", " + BufferedImage.TYPE_INT_ARGB);
        if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) { // type 5
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    k = 3 * (i * img.getWidth() + j);
                    // inline for more speed!!
                    blue += (double) ((int) data[k] & 0xff);
                    green += (double) ((int) data[k + 1] & 0xff);
                    red += (double) ((int) data[k + 2] & 0xff);
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) { // type 6
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    k = 4 * (i * img.getWidth() + j);
                    // inline for more speed!!
                    blue += (double) ((int) data[k + 1] & 0xff);
                    green += (double) ((int) data[k + 2] & 0xff);
                    red += (double) ((int) data[k + 3] & 0xff);
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_INT_RGB) { // type 1
            int data[] = ((DataBufferInt) (img.getRaster().getDataBuffer())).getData();

            int rgb;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    rgb = data[i*img.getWidth() + j];
                    // inline for more speed!!
                    red += (double) ((rgb & 0xff0000) >> 16);
                    green += (double) ((rgb & 0xff00) >> 8);
                    blue += (double) (rgb & 0xff);
                    total++;
                }
            }
        } else {
            System.out.println("Unsupported image format! = " + img.getType());
            assert false;
            return null;
        }
        //double totalvalue = red + green + blue; // lets try to normalize by ratio of color
        return new double[] {red/total, green/total, blue/total};
        //return new double[] {red/total, green/total, blue/total};
    }
    // extracts average RGB value from a window 
    public static double[] extractRGB(BufferedImage img, int row, int column, double scale) {
        double red = 0;
        double green = 0;
        double blue = 0;
        int total = 0; // total pixels averaged over
        int s2 = (int) Math.round(scale/2.0);
        
        int istart,iend,jstart,jend;
        if (row < s2) {
            istart = 0;
        } else {
            istart = row - s2;
        }
        
        if (column < s2) {
            jstart = 0;
        } else {
            jstart = column - s2;
        }
        
        if ((row+s2) >= img.getHeight()) {
            iend = img.getHeight();
        } else {
            iend = row+s2;
        } 
        
        if ((column+s2) >= img.getWidth()) {
            jend = img.getWidth();
        } else {
            jend = column+s2;
        }
        
        if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) { // type 5
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            for (int i = istart; i < iend; i++) {
                for (int j = jstart; j < jend; j++) {
                    k = 3 * (i * img.getWidth() + j);
                    blue += (double) ((int) data[k] & 0xff);
                    green += (double) ((int) data[k + 1] & 0xff);
                    red += (double) ((int) data[k + 2] & 0xff);
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) { // type 6
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            for (int i = istart; i < iend; i++) {
                for (int j = jstart; j < jend; j++) {
                    k = 4 * (i * img.getWidth() + j);
                    blue += (double) ((int) data[k + 1] & 0xff);
                    green += (double) ((int) data[k + 2] & 0xff);
                    red += (double) ((int) data[k + 3] & 0xff);
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_INT_RGB) { // type 1
            int data[] = ((DataBufferInt) (img.getRaster().getDataBuffer())).getData();

            int k;
            int rgb;
            for (int i = istart; i < iend; i++) {
                for (int j = jstart; j < jend; j++) {
                    rgb = data[i*img.getWidth() + j];
                    red += (double) ((rgb & 0xff0000) >> 16);
                    green += (double) ((rgb & 0xff00) >> 8);
                    blue += (double) (rgb & 0xff);
                    total++;
                }
            }
        } else {
            System.out.println("Unsupported image format! = " + img.getType());
            assert false;
            return null;
        }
        //double totalvalue = red + green + blue; // lets try to normalize by ratio of color
        return new double[] {red/total, green/total, blue/total};
    }
    
    public static double[] extractRGBHistogram(BufferedImage img) {
        double[] rgbhist = new double[3*Constants.NUMBUCKETS];
        // precompute bin separations for speed
        int[] binSep = new int[Constants.NUMBUCKETS]; // each is the upper bound of each bucket
        
        for (int i = 0; i < Constants.NUMBUCKETS; i++) {
            binSep[i] = (int) (Math.round((255.0/Constants.NUMBUCKETS)*(i+1)));
        }
        
        
        int total = 0;
        int intrgb[] = new int[3];
        //System.out.println("image types " + BufferedImage.TYPE_4BYTE_ABGR + ", " + BufferedImage.TYPE_INT_ARGB);
        if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) { // type 5
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            byte r, g, b;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    k = 3 * (i * img.getWidth() + j);
                    // inline for more speed!!
                    intrgb[2] =  ((int) data[k] & 0xff);
                    intrgb[1] =  ((int) data[k + 1] & 0xff);
                    intrgb[0] = ((int) data[k + 2] & 0xff);
                    
                    for (int c = 0; c < 3; c++) { // for each color
                        // figure out which bucket to go into
                        int bu = 0;
                        while (intrgb[c] > binSep[bu]) {
                            bu++;
                        }
                        rgbhist[c*Constants.NUMBUCKETS+bu] += 1.0;
                    }
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) { // type 6
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            byte r, g, b;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    k = 4 * (i * img.getWidth() + j);
                    // inline for more speed!!
                    intrgb[2] = ((int) data[k + 1] & 0xff);
                    intrgb[1] = ((int) data[k + 2] & 0xff);
                    intrgb[0] = ((int) data[k + 3] & 0xff);
                    
                    for (int c = 0; c < 3; c++) { // for each color
                        // figure out which bucket to go into
                        int bu = 0;
                        while (intrgb[c] > binSep[bu]) {
                            bu++;
                        }
                        rgbhist[c*Constants.NUMBUCKETS+bu] += 1.0;
                    }
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_INT_RGB) { // type 1
            int data[] = ((DataBufferInt) (img.getRaster().getDataBuffer())).getData();

            int k;
            int rgb;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    rgb = data[i*img.getWidth() + j];
                    intrgb[0] = (rgb & 0xff0000) >> 16;
                    intrgb[1] = (rgb & 0xff00) >> 8;
                    intrgb[2] = (rgb & 0xff);
                    
                    for (int c = 0; c < 3; c++) { // for each color
                        // figure out which bucket to go into
                        int bu = 0;
                        while (intrgb[c] > binSep[bu]) {
                            bu++;
                        }
                        rgbhist[c*Constants.NUMBUCKETS+bu] += 1.0;
                    }
                    total++;
                }
            }
        } else {
            System.out.println("Unsupported image format! = " + img.getType());
            assert false;
            return null;
        }
        // normalize the histogram
        LinAlg.scaleEquals(rgbhist, 1.0/total);
        //double totalvalue = red + green + blue; // lets try to normalize by ratio of color
        return rgbhist;
        //return new double[] {red/total, green/total, blue/total};
    }
    
     public static double[] extractRGBHistogram(BufferedImage img, int row, int column, double scale) {
        double[] rgbhist = new double[3*Constants.NUMBUCKETS];
        // precompute bin separations for speed
        int[] binSep = new int[Constants.NUMBUCKETS]; // each is the upper bound of each bucket
        
        for (int i = 0; i < Constants.NUMBUCKETS; i++) {
            binSep[i] = (int) (Math.round((255.0/Constants.NUMBUCKETS)*(i+1)));
        }
        
        int s2 = (int) Math.round(scale/2.0);
        
        int istart,iend,jstart,jend;
        if (row < s2) {
            istart = 0;
        } else {
            istart = row - s2;
        }
        
        if (column < s2) {
            jstart = 0;
        } else {
            jstart = column - s2;
        }
        
        if ((row+s2) >= img.getHeight()) {
            iend = img.getHeight();
        } else {
            iend = row+s2;
        } 
        
        if ((column+s2) >= img.getWidth()) {
            jend = img.getWidth();
        } else {
            jend = column+s2;
        }
        
        int total = 0;
        int intrgb[] = new int[3];
        //System.out.println("image types " + BufferedImage.TYPE_4BYTE_ABGR + ", " + BufferedImage.TYPE_INT_ARGB);
        if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) { // type 5
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            byte r, g, b;
            for (int i = istart; i < iend; i++) {
                for (int j = jstart; j < jend; j++) {
                    k = 3 * (i * img.getWidth() + j);
                    //  more speed
                    intrgb[2] =  ((int) data[k] & 0xff);
                    intrgb[1] =  ((int) data[k + 1] & 0xff);
                    intrgb[0] = ((int) data[k + 2] & 0xff);
                    
                    for (int c = 0; c < 3; c++) { // for each color
                        // figure out which bucket to go into
                        int bu = 0;
                        while (intrgb[c] > binSep[bu]) {
                            bu++;
                        }
                        rgbhist[c*Constants.NUMBUCKETS+bu] += 1.0;
                    }
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) { // type 6
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            byte r, g, b;
            for (int i = istart; i < iend; i++) {
                for (int j = jstart; j < jend; j++) {
                    k = 4 * (i * img.getWidth() + j);
                    // more speed
                    intrgb[2] = ((int) data[k + 1] & 0xff);
                    intrgb[1] = ((int) data[k + 2] & 0xff);
                    intrgb[0] = ((int) data[k + 3] & 0xff);
                    
                    for (int c = 0; c < 3; c++) { // for each color
                        // figure out which bucket to go into
                        int bu = 0;
                        while (intrgb[c] > binSep[bu]) {
                            bu++;
                        }
                        rgbhist[c*Constants.NUMBUCKETS+bu] += 1.0;
                    }
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_INT_RGB) { // type 1
            int data[] = ((DataBufferInt) (img.getRaster().getDataBuffer())).getData();

            int k;
            int rgb;
            for (int i = istart; i < iend; i++) {
                for (int j = jstart; j < jend; j++) {
                    rgb = data[i*img.getWidth() + j];
                    intrgb[0] = (rgb & 0xff0000) >> 16;
                    intrgb[1] = (rgb & 0xff00) >> 8;
                    intrgb[2] = (rgb & 0xff);
                    
                    for (int c = 0; c < 3; c++) { // for each color
                        // figure out which bucket to go into
                        int bu = 0;
                        while (intrgb[c] > binSep[bu]) {
                            bu++;
                        }
                        rgbhist[c*Constants.NUMBUCKETS+bu] += 1.0;
                    }
                    total++;
                }
            }
        } else {
            System.out.println("Unsupported image format! = " + img.getType());
            assert false;
            return null;
        }
        // normalize the histogram
        LinAlg.scaleEquals(rgbhist, 1.0/total);
        //double totalvalue = red + green + blue; // lets try to normalize by ratio of color
        return rgbhist;
        //return new double[] {red/total, green/total, blue/total};
    }
    
     public static double[] extractHSHistogram(BufferedImage img) {
        double[] hshist = new double[2*Constants.NUMBUCKETS];
        // precompute bin separations for speed
        double[] HbinSep = new double[Constants.NUMBUCKETS]; // each is the upper bound of each bucket
        double[] SbinSep = new double[Constants.NUMBUCKETS];
        
        for (int i = 0; i < Constants.NUMBUCKETS; i++) {
            HbinSep[i] = (360.0/Constants.NUMBUCKETS)*(i+1);
            SbinSep[i] = (1.0/Constants.NUMBUCKETS)*(i+1);
        }
        
        int total = 0;
        int intrgb[] = new int[3];
        double[] hsv = new double[3];
        
        //System.out.println("image types " + BufferedImage.TYPE_4BYTE_ABGR + ", " + BufferedImage.TYPE_INT_ARGB);
        if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) { // type 5
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            byte r, g, b;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    k = 3 * (i * img.getWidth() + j);
                    intrgb[2] =  ((int) data[k] & 0xff);
                    intrgb[1] =  ((int) data[k + 1] & 0xff);
                    intrgb[0] = ((int) data[k + 2] & 0xff);
                    
                    RGBtoHSV(intrgb, hsv);
                    
                    // hue part
                    int bu = 0;
                    while (hsv[0] > HbinSep[bu]) {
                        bu++;
                    }
                    hshist[bu] += 1.0;
                    
                    // saturation part
                    bu = 0;
                    while(hsv[1] > SbinSep[bu]) {
                        bu++;
                    }
                    hshist[Constants.NUMBUCKETS + bu] += 1.0;
                    
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) { // type 6
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            byte r, g, b;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    k = 4 * (i * img.getWidth() + j);
                    intrgb[2] = ((int) data[k + 1] & 0xff);
                    intrgb[1] = ((int) data[k + 2] & 0xff);
                    intrgb[0] = ((int) data[k + 3] & 0xff);
                    
                    RGBtoHSV(intrgb, hsv);
                    
                    // hue part
                    int bu = 0;
                    while (hsv[0] > HbinSep[bu]) {
                        bu++;
                    }
                    hshist[bu] += 1.0;
                    
                    // saturation part
                    bu = 0;
                    while(hsv[1] > SbinSep[bu]) {
                        bu++;
                    }
                    hshist[Constants.NUMBUCKETS + bu] += 1.0;
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_INT_RGB) { // type 1
            int data[] = ((DataBufferInt) (img.getRaster().getDataBuffer())).getData();

            int k;
            int rgb;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    rgb = data[i*img.getWidth() + j];
                    intrgb[0] = (rgb & 0xff0000) >> 16;
                    intrgb[1] = (rgb & 0xff00) >> 8;
                    intrgb[2] = (rgb & 0xff);
                    
                    RGBtoHSV(intrgb, hsv);
                    
                    // hue part
                    int bu = 0;
                    while (hsv[0] > HbinSep[bu]) {
                        bu++;
                    }
                    hshist[bu] += 1.0;
                    
                    // saturation part
                    bu = 0;
                    while(hsv[1] > SbinSep[bu]) {
                        bu++;
                    }
                    hshist[Constants.NUMBUCKETS + bu] += 1.0;
                    total++;
                }
            }
        } else {
            System.out.println("Unsupported image format! = " + img.getType());
            assert false;
            return null;
        }
        // normalize the histogram
        LinAlg.scaleEquals(hshist, 1.0/total);
        //double totalvalue = red + green + blue; // lets try to normalize by ratio of color
        return hshist;
        //return new double[] {red/total, green/total, blue/total};
    }
     
     
     public static double[] extractHSHistogram(BufferedImage img, int row, int column, double scale) {
         double[] hshist = new double[2*Constants.NUMBUCKETS];
        // precompute bin separations for speed
        double[] HbinSep = new double[Constants.NUMBUCKETS]; // each is the upper bound of each bucket
        double[] SbinSep = new double[Constants.NUMBUCKETS];
        
        for (int i = 0; i < Constants.NUMBUCKETS; i++) {
            HbinSep[i] = (360.0/Constants.NUMBUCKETS)*(i+1);
            SbinSep[i] = (1.0/Constants.NUMBUCKETS)*(i+1);
        }
        
        int s2 = (int) Math.round(scale/2.0);
        
        int istart,iend,jstart,jend;
        if (row < s2) {
            istart = 0;
        } else {
            istart = row - s2;
        }
        
        if (column < s2) {
            jstart = 0;
        } else {
            jstart = column - s2;
        }
        
        if ((row+s2) >= img.getHeight()) {
            iend = img.getHeight();
        } else {
            iend = row+s2;
        } 
        
        if ((column+s2) >= img.getWidth()) {
            jend = img.getWidth();
        } else {
            jend = column+s2;
        }
        
        int total = 0;
        int intrgb[] = new int[3];
        double[] hsv = new double[3];
        //System.out.println("image types " + BufferedImage.TYPE_4BYTE_ABGR + ", " + BufferedImage.TYPE_INT_ARGB);
        if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) { // type 5
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            byte r, g, b;
            for (int i = istart; i < iend; i++) {
                for (int j = jstart; j < jend; j++) {
                    k = 3 * (i * img.getWidth() + j);
                    intrgb[2] =  ((int) data[k] & 0xff);
                    intrgb[1] =  ((int) data[k + 1] & 0xff);
                    intrgb[0] = ((int) data[k + 2] & 0xff);
                    
                    RGBtoHSV(intrgb, hsv);
                    
                    // hue part
                    int bu = 0;
                    while (hsv[0] > HbinSep[bu]) {
                        bu++;
                    }
                    hshist[bu] += 1.0;
                    
                    // saturation part
                    bu = 0;
                    while(hsv[1] > SbinSep[bu]) {
                        bu++;
                    }
                    hshist[Constants.NUMBUCKETS + bu] += 1.0;
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) { // type 6
            byte data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();

            int k;
            byte r, g, b;
            for (int i = istart; i < iend; i++) {
                for (int j = jstart; j < jend; j++) {
                    k = 4 * (i * img.getWidth() + j);
                    // more speed
                    intrgb[2] = ((int) data[k + 1] & 0xff);
                    intrgb[1] = ((int) data[k + 2] & 0xff);
                    intrgb[0] = ((int) data[k + 3] & 0xff);
                    
                    RGBtoHSV(intrgb, hsv);
                    
                    // hue part
                    int bu = 0;
                    while (hsv[0] > HbinSep[bu]) {
                        bu++;
                    }
                    hshist[bu] += 1.0;
                    
                    // saturation part
                    bu = 0;
                    while(hsv[1] > SbinSep[bu]) {
                        bu++;
                    }
                    hshist[Constants.NUMBUCKETS + bu] += 1.0;
                    total++;
                }
            }
        } else if (img.getType() == BufferedImage.TYPE_INT_RGB) { // type 1
            int data[] = ((DataBufferInt) (img.getRaster().getDataBuffer())).getData();

            int k;
            int rgb;
            for (int i = istart; i < iend; i++) {
                for (int j = jstart; j < jend; j++) {
                    rgb = data[i*img.getWidth() + j];
                    intrgb[0] = (rgb & 0xff0000) >> 16;
                    intrgb[1] = (rgb & 0xff00) >> 8;
                    intrgb[2] = (rgb & 0xff);
                    
                    RGBtoHSV(intrgb, hsv);
                    
                    // hue part
                    int bu = 0;
                    while (hsv[0] > HbinSep[bu]) {
                        bu++;
                    }
                    hshist[bu] += 1.0;
                    
                    // saturation part
                    bu = 0;
                    while(hsv[1] > SbinSep[bu]) {
                        bu++;
                    }
                    hshist[Constants.NUMBUCKETS + bu] += 1.0;
                    total++;
                }
            }
        } else {
            System.out.println("Unsupported image format! = " + img.getType());
            assert false;
            return null;
        }
        // normalize the histogram
        LinAlg.scaleEquals(hshist, 1.0/total);
        //double totalvalue = red + green + blue; // lets try to normalize by ratio of color
        return hshist;
        //return new double[] {red/total, green/total, blue/total};
    }
     
     // convert RGB to HSV
    private static void RGBtoHSV(int[] rgb, double[] hsv) {
        //double[] hsv = new double[3];
        //Get individual values for convenience
        double red = rgb[0];
        double green = rgb[1];
        double blue = rgb[2];

        //Compute the max, min and delta values in RGB space
        double min = red < green ? (red < blue ? red : blue) : (green < blue ? green : blue);
        double max = red > green ? (red > blue ? red : blue) : (green > blue ? green : blue);
        double delta = max - min;

        //The "Value" of HSV is simply the max RGB value
        hsv[2] = max;

        //Compute the saturation
        if (max != 0.0) {
            hsv[1] = delta / max;
        } else {
            //This only happens when the RGB values are {0, 0, 0}
            //In this case, Saturation is 0 and the Hue is undefined
            hsv[1] = 0;
            hsv[0] = -1;
        }

        //Compute the hue
        if (red == max) {
            //When r is the max, the hue is somewhere between yellow and magenta
            hsv[0] = (green - blue) / delta;
        } else if (green == max) {
            //When g is the max, the hue is somewhere between cyan and yellow
            hsv[0] = 2 + (blue - red) / delta;
        } else {
            //When b is the max, the hue is somewhere between magenta and cyan
            hsv[0] = 4 + (red - green) / delta;
        }
        hsv[0] *= 60;

        //Make sure hue is between 0 and 360 degrees
        if (hsv[0] < 0) {
            hsv[0] += 360;
        }
        
        //return hsv;
    }
    
    public static double[][] invert(double[][] imgI) {
        double[][] inv = new double[imgI.length][imgI[0].length];
        for (int i = 0; i < imgI.length; i++) {
            for (int j = 0; j < imgI[0].length; j++) {
                if (imgI[i][j] != 0) {
                    inv[i][j] = 1.0/imgI[i][j];
                } else {
                    inv[i][j] = 0; // blown highlight
                }
            }

        }
        return inv;
    }

    public static BufferedImage cropImage(BufferedImage img, int x0, int y0, int x1, int y1){
	if(img == null)
	    throw new RuntimeException("ImageProc.cropImage was passed img==null");

	int dx = x1 - x0;
	int dy = y0 - y1;
	if(x0 < 0 || y0 < 0 || x0 >= img.getWidth() || y0 >= img.getHeight() ||
	   x1 < 0 || y1 < 0 || x1 >= img.getWidth() || y1 >= img.getHeight() ||
	   dx <= 0 || dy <= 0)
	    throw new RuntimeException("ImageProc.cropImage was passed an invalid region");

	BufferedImage new_img = new BufferedImage(dx, dy, img.getType());
	for(int y = 0; y < dy; ++y){
	    for(int x = 0; x < dx; ++x){
		new_img.setRGB(x, y, img.getRGB(x+x0, y+(img.getHeight()-y0)));
	    }
	}
	return new_img;
    }

    public static BufferedImage cloneImage(BufferedImage img) {
	assert(img != null);
	BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
	byte img_data[] = ((DataBufferByte) (img.getRaster().getDataBuffer())).getData();
	byte ret_data[] = ((DataBufferByte) (ret.getRaster().getDataBuffer())).getData();

	for(int i = 0; i < img_data.length; ++i)
	    ret_data[i] = img_data[i];
	
	return ret;
    }

}

