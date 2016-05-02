/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.plexiViewer.utils.Transform;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.awt.image.ColorModel;

import org.nrg.plexiViewer.io.PlexiFileOpener;
import org.nrg.plexiViewer.lite.io.PlexiImageFile;

/* Trilinear Interpolation */

public class AffineTransformer {
    
    public static final double ACCEPTABLE_PIXEL_SIZE_ERR = 0.0001;
    private double pixel_smallest;
    private double scale = 1.0;
    
    double[][] affineMatrix = new double[][] {
            {1.0, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.0, 0.0},
            {0.0, 0.0, 1.0, 0.0},
            {0.0, 0.0, 0.0, 1.0}
        };
    
    public ImagePlus  transform(ImagePlus imgIn) {
        ImagePlus imgOut = null;
        if (imgIn == null) return null;
        FileInfo fi = imgIn.getFileInfo();
        if (isIsotropic(fi)) return imgIn;
        System.out.println("IN IMG DIMS ARE ");
        System.out.println("Size: " + fi.pixelWidth + " " + fi.pixelHeight + " " + fi.pixelDepth);
        System.out.println("Dims: " + imgIn.getWidth() + " " + imgIn.getHeight() + " " + imgIn.getStackSize());
        pixel_smallest = imgIn.getCalibration().pixelWidth;
        if (imgIn.getCalibration().pixelHeight < pixel_smallest) pixel_smallest = imgIn.getCalibration().pixelHeight;
        if (imgIn.getCalibration().pixelDepth < pixel_smallest) pixel_smallest = imgIn.getCalibration().pixelDepth;
        FileInfo fiOut = getTransformedFileInfo(fi);
        System.out.println("OUT IMG DIMS ARE ");
        System.out.println("Size: " + fiOut.pixelWidth + " " + fiOut.pixelHeight + " " + fiOut.pixelDepth);
        System.out.println("Dims: " + fiOut.width + " " + fiOut.height + " " + fiOut.nImages);
        imgOut = transformLinear(imgIn, fiOut);
        System.out.println("Image Made Isotropic " );
        return imgOut;
    }
    
    private boolean isIsotropic(FileInfo fi) {
        boolean rtn = false;
        if ((fi.pixelWidth == fi.pixelHeight) && (fi.pixelWidth == fi.pixelDepth)) {
            rtn = true;
        }
        System.out.println("Image (" + fi.pixelWidth + "x" + fi.pixelHeight + "x" + fi.pixelDepth +") "+ (rtn?"isotropic":"anisotropic"));
        return rtn;
    }
    
    private ImagePlus transformLinear(ImagePlus imgIn, FileInfo fiOut) {
        ImagePlus imgOut = null;
        FileInfo fi = imgIn.getOriginalFileInfo();
        double x_max1 = fi.width -1;
        double y_max1 = fi.height -1;
        double z_max1 = fi.nImages -1 ;
        
        System.out.println("X_MAX " + x_max1 + " " + y_max1 + " " + z_max1);
        
        double m00 = affineMatrix[0][0]; 
        double m01 = affineMatrix[0][1];
        double m02 = affineMatrix[0][2];
        double m10 = affineMatrix[1][0];
        double m11 = affineMatrix[1][1];
        double m12 = affineMatrix[1][2];
        double m20 = affineMatrix[2][0];
        double m21 = affineMatrix[2][1];
        double m22 = affineMatrix[2][2];
        double m30 = affineMatrix[3][0];
        double m31 = affineMatrix[3][1];
        double m32 = affineMatrix[3][2];
        
        ImageStack stackOut = new ImageStack(fiOut.width, fiOut.height);
        double x_k = m30, y_k = m31, z_k = m32;
        double a,b,c,d,e,f;
        int x_up, x_down, y_up, y_down, z_up, z_down;
        int middleSlice = (int)(imgIn.getStackSize()/2); 
        imgIn.setSlice(middleSlice);
        long startTime1;
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        for (int k = 1; k <= fiOut.nImages; k++, x_k+=m20, y_k+=m21, z_k+=m22) {
            startTime1 = System.currentTimeMillis();
            ImageProcessor ipOut = imgIn.getProcessor().createProcessor(fiOut.width, fiOut.height);
            ipOut.setValue(0.0);
            stackOut.addSlice("",ipOut);
            //endTime = System.currentTimeMillis();
            //System.out.println("Time to set the image processor for the stack  " + (endTime - startTime1) + " ms");
            double x_j = x_k, y_j = y_k, z_j = z_k;
            double min = 0; double max = 0;
            for (int j = 0; j <fiOut.height; j++,x_j+=m10,y_j+=m11,z_j+=m12 ) {
                boolean x_in = false;
                double x_i = x_j, y_i = y_j, z_i = z_j;
                for (int i = 0; i<fiOut.width; i++,x_i+=m00,y_i+=m01,z_i+=m02) {
                    if(x_i>=0.0 && x_i<=x_max1){
                        if(y_i>=0.0 && y_i<=y_max1){
                            if(z_i>=0.0 && z_i<=z_max1){
                                    x_up=(int)Math.ceil(x_i);
                                    x_down=(int)Math.floor(x_i);
                                    y_up=(int)Math.ceil(y_i);
                                    y_down=(int)Math.floor(y_i);
                                    z_up=(int)Math.ceil(z_i);
                                    z_down=(int)Math.floor(z_i);
                                x_in=true;

                                if(x_up==x_down){
                                    a=0.0;
                                    d=1.0;
                                }
                                else{
                                    a=x_i-x_down;
                                    d=x_up-x_i;
                                }
                                if(y_up==y_down){
                                    b=0.0;
                                    e=1.0;
                                }
                                else{
                                    b=y_i-y_down;
                                    e=y_up-y_i;
                                }
                                if(z_up==z_down){
                                    c=0.0;
                                    f=1.0;
                                }
                                else{
                                    c=z_i-z_down;
                                    f=z_up-z_i;
                                }
                                int k2 = z_down+1;
                                int j2 = y_down;
                                int i2 = x_down;
                                    double total = imgIn.getStack().getProcessor(k2).getPixelValue(i2,j2)*d*e*f;
                                    if (x_up!=x_down){
                                        i2++;
                                        total+=imgIn.getStack().getProcessor(k2).getPixelValue(i2,j2)*a*e*f;
                                    }
                                    if (y_up!=y_down){
                                        j2++; 
                                        total+=imgIn.getStack().getProcessor(k2).getPixelValue(i2,j2)*d*b*f;
                                        if (x_up!=x_down){
                                            i2++;
                                            total+=imgIn.getStack().getProcessor(k2).getPixelValue(i2,j2)*a*b*f;
                                        }
                                    }

                                    if (z_up!=z_down){
                                        k2++;
                                        total+=imgIn.getStack().getProcessor(k2).getPixelValue(i2,j2)*d*e*c;
                                        if (x_up!=x_down){
                                            i2++;
                                            total+=imgIn.getStack().getProcessor(k2).getPixelValue(i2,j2)*a*e*c;
                                        }
                                        if (y_up!=y_down){
                                            j2++;
                                            total+=imgIn.getStack().getProcessor(k2).getPixelValue(i2,j2)*d*b*c;
                                            if (x_up!=x_down){
                                                i2++;
                                                total+=imgIn.getStack().getProcessor(k2).getPixelValue(i2,j2)*a*b*c;
                                            }
                                        }
                                    }
                                    total*=scale;
                                    total+=.5;
                                    ipOut.putPixelValue(i,j,total);
                                    if (max < total) max = total;
                                    if (min > total) min = total;
                            } else if(x_in) break;
                        } else if(x_in) break;
                    } else if(x_in) break;
                }
            }
            ipOut.setMinAndMax(min,max);
        }
        endTime = System.currentTimeMillis();
        System.out.println("Time for end of loop " + (endTime - startTime) + " ms");
        imgOut = new ImagePlus(imgIn.getTitle(),stackOut);
        Calibration cal = imgIn.getCalibration().copy();
        cal.pixelWidth = fiOut.pixelWidth;
        cal.pixelHeight = fiOut.pixelHeight;
        cal.pixelDepth = fiOut.pixelDepth;
        imgOut.setCalibration(cal);
        final ImageProcessor ip = imgIn.getProcessor();
        final ColorModel cm = ip.getColorModel();
        imgOut.getStack().setColorModel(cm);
        endTime = System.currentTimeMillis();
        System.out.println("Time for interpolation " + (endTime - startTime) + " ms");
        return imgOut;
    }
    
/*    private FileInfo getTransformedFileInfo(ImagePlus img) {
        FileInfo fiOut = new FileInfo();
        if (Math.abs(img.pixelWidth - pixel_smallest) > ACCEPTABLE_PIXEL_SIZE_ERR) {
            fiOut.pixelWidth = pixel_smallest;
            fiOut.width = (int)Math.floor(img.getWidth() *(img.pixelWidth/pixel_smallest)  + Double.MIN_VALUE );
            affineMatrix[0][0] = pixel_smallest/img.pixelWidth;
        }else {
            fiOut.pixelWidth = img.pixelWidth;
            fiOut.width = img.getWidth();
        }
        
        if (Math.abs(img.pixelHeight - pixel_smallest) > ACCEPTABLE_PIXEL_SIZE_ERR) {
            fiOut.pixelHeight = pixel_smallest;
            fiOut.height = (int)Math.floor(img.getHeight()*(img.pixelHeight/pixel_smallest) + Double.MIN_VALUE );
            affineMatrix[1][1] = pixel_smallest/img.pixelHeight;
        }else {
            fiOut.pixelHeight = img.pixelHeight;
            fiOut.height = img.getHeight();
        }

        if (Math.abs(img.getCalibration().pixelDepth - pixel_smallest) > ACCEPTABLE_PIXEL_SIZE_ERR) {
            fiOut.pixelDepth = pixel_smallest;
            fiOut.nImages = (int)Math.floor(img.getStackSize() *(img.getCalibration().pixelDepth/pixel_smallest)  + Double.MIN_VALUE );
            affineMatrix[2][2] = pixel_smallest/img.getCalibration().pixelDepth;
        }else {
            fiOut.pixelDepth = img.getCalibration().pixelDepth;
            fiOut.nImages = img.getStackSize();
        }
        return fiOut;
    }
*/
    
    private FileInfo getTransformedFileInfo(ImagePlus img) {
        FileInfo fiOut = new FileInfo();
        if (Math.abs(img.getCalibration().pixelWidth - pixel_smallest) > ACCEPTABLE_PIXEL_SIZE_ERR) {
            fiOut.pixelWidth = pixel_smallest;
            fiOut.width = (int)Math.floor(img.getWidth() *(img.getCalibration().pixelWidth/pixel_smallest)  + Double.MIN_VALUE );
            affineMatrix[0][0] = pixel_smallest/img.getCalibration().pixelWidth;
        }else {
            fiOut.pixelWidth = img.getCalibration().pixelWidth;
            fiOut.width = img.getWidth();
        }
        
        if (Math.abs(img.getCalibration().pixelHeight - pixel_smallest) > ACCEPTABLE_PIXEL_SIZE_ERR) {
            fiOut.pixelHeight = pixel_smallest;
            fiOut.height = (int)Math.floor(img.getHeight()*(img.getCalibration().pixelHeight/pixel_smallest) + Double.MIN_VALUE );
            affineMatrix[1][1] = pixel_smallest/img.getCalibration().pixelHeight;
        }else {
            fiOut.pixelHeight = img.getCalibration().pixelHeight;
            fiOut.height = img.getHeight();
        }

        if (Math.abs(img.getCalibration().pixelDepth - pixel_smallest) > ACCEPTABLE_PIXEL_SIZE_ERR) {
            fiOut.pixelDepth = pixel_smallest;
            fiOut.nImages = (int)Math.floor(img.getStackSize() *(img.getCalibration().pixelDepth/pixel_smallest)  + Double.MIN_VALUE );
            affineMatrix[2][2] = pixel_smallest/img.getCalibration().pixelDepth;
        }else {
            fiOut.pixelDepth = img.getCalibration().pixelDepth;
            fiOut.nImages = img.getStackSize();
        }
        return fiOut;
    }

    private FileInfo getTransformedFileInfo(FileInfo fi) {
        FileInfo fiOut = new FileInfo();
        if (Math.abs(fi.pixelWidth - pixel_smallest) > ACCEPTABLE_PIXEL_SIZE_ERR) {
            fiOut.pixelWidth = pixel_smallest;
            fiOut.width = (int)Math.floor(fi.width *(fi.pixelWidth/pixel_smallest)  + Double.MIN_VALUE );
            affineMatrix[0][0] = pixel_smallest/fi.pixelWidth;
        }else {
            fiOut.pixelWidth = fi.pixelWidth;
            fiOut.width = fi.width;
        }
        
        if (Math.abs(fi.pixelHeight - pixel_smallest) > ACCEPTABLE_PIXEL_SIZE_ERR) {
            fiOut.pixelHeight = pixel_smallest;
            fiOut.height = (int)Math.floor(fi.height*(fi.pixelHeight/pixel_smallest) + Double.MIN_VALUE );
            affineMatrix[1][1] = pixel_smallest/fi.pixelHeight;
        }else {
            fiOut.pixelHeight = fi.pixelHeight;
            fiOut.height = fi.height;
        }

        if (Math.abs(fi.pixelDepth - pixel_smallest) > ACCEPTABLE_PIXEL_SIZE_ERR) {
            fiOut.pixelDepth = pixel_smallest;
            fiOut.nImages = (int)Math.floor(fi.nImages *(fi.pixelDepth/pixel_smallest)  + Double.MIN_VALUE );
            affineMatrix[2][2] = pixel_smallest/fi.pixelDepth;
        }else {
            fiOut.pixelDepth = fi.pixelDepth;
            fiOut.nImages = fi.nImages;
        }
        return fiOut;
    }
    
    public static void main(String argv[]) {
        try {
            PlexiImageFile pf = new PlexiImageFile();
            pf.setURIAsString("file:/Y:/data2/WORK/PIPELINE_TEST/060628_846802/1548.4dfp.img");
            //pf.setURI("srb://dmarcus@wustl-nrg-gpop.nbirn.net/home/dmarcus.wustl-nrg/oasis/set1/disc1/OAS1_0001_MR1/RAW/OAS1_0001_MR1_mpr-4_anon.img");
            PlexiFileOpener pfo = new PlexiFileOpener("ANALYZE",pf);
            ImagePlus img = pfo.getImagePlus();
            AffineTransformer affineTransformer = new AffineTransformer();
            ImagePlus inter = affineTransformer.transform(img);
            new FileSaver(inter).saveAsRawStack("inter.img");
            System.out.println("Image Saved");
        }catch(Exception e) {
            e.printStackTrace();
        }

    }
}

