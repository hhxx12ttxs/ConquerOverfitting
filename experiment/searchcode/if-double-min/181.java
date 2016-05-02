/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.plexiViewer.utils.Transform;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;

import org.nrg.plexiViewer.io.PlexiFileOpener;
import org.nrg.plexiViewer.lite.io.PlexiImageFile;
import org.nrg.plexiViewer.utils.PlexiConstants;

public class IntensitySetter {
    private ImagePlus image;
    private boolean reset;
    double min, max;
    boolean RGBImage;
	static final int AUTO_THRESHOLD = 5000;
	int channels = 7; // RGB
	int autoThreshold = 0;
	double defaultMin=0, defaultMax=255;

    public IntensitySetter(ImagePlus img, boolean reset) {
        image = img;
		int type = image.getType();
        RGBImage = type==ImagePlus.COLOR_RGB;
        ImageProcessor ip = image.getProcessor();
        if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
			image.resetDisplayRange();
			defaultMin = ip.getMin();
			defaultMax = ip.getMax();

		} else {
			defaultMin = 0;
			defaultMax = 255;
		}

        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        this.reset = reset;
    }
    
    
    
	void setMinAndMax(ImagePlus imp, double min, double max) {
		if (channels!=7 && imp.getType()==ImagePlus.COLOR_RGB)
			imp.setDisplayRange(min, max, channels);
		else
			imp.setDisplayRange(min, max);
	}

    
	public void autoAdjust(ImagePlus imp, ImageProcessor ip) {
 		if (RGBImage)
			ip.reset();
		Calibration cal = imp.getCalibration();
		imp.setCalibration(null);
		ImageStatistics stats = imp.getStatistics(); // get uncalibrated stats
		imp.setCalibration(cal);
		int limit = stats.pixelCount/10;
		int[] histogram = stats.histogram;
		if (autoThreshold<10)
			autoThreshold = AUTO_THRESHOLD;
		else
			autoThreshold /= 2;
		int threshold = stats.pixelCount/autoThreshold;
		int i = -1;
		boolean found = false;
		int count;
		do {
			i++;
			count = histogram[i];
			if (count>limit) count = 0;
			found = count> threshold;
		} while (!found && i<255);
		int hmin = i;
		i = 256;
		do {
			i--;
			count = histogram[i];
			if (count>limit) count = 0;
			found = count > threshold;
		} while (!found && i>0);
		int hmax = i;
		Roi roi = imp.getRoi();
		if (hmax>=hmin) {
			if (RGBImage) imp.killRoi();
			min = stats.histMin+hmin*stats.binSize;
			max = stats.histMin+hmax*stats.binSize;
			if (min==max)
				{min=stats.min; max=stats.max;}
			setMinAndMax(imp, min, max);
			if (RGBImage && roi!=null) imp.setRoi(roi);
		} else {
			reset(imp, ip);
			return;
		}
		if (roi!=null) {
			ImageProcessor mask = roi.getMask();
			if (mask!=null)
				ip.reset(mask);
		}
	}

	void reset(ImagePlus imp, ImageProcessor ip) {
 		if (RGBImage)
			ip.reset();
		if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
			imp.resetDisplayRange();
			defaultMin = ip.getMin();
			defaultMax = ip.getMax();
		}
		min = defaultMin;
		max = defaultMax;
		setMinAndMax(imp, min, max);
		autoThreshold = 0;
	}

	
	/** Restore image outside non-rectangular roi. */
  	void doMasking(ImagePlus imp, ImageProcessor ip) {
		ImageProcessor mask = imp.getMask();
		if (mask!=null)
			ip.reset(mask);
	}

	
	void setMinAndMax(ImagePlus imp, ImageProcessor ip) {
		min = ip.getMin();
		max = ip.getMax();
		Calibration cal = imp.getCalibration();
		int digits = (ip instanceof FloatProcessor)||cal.calibrated()?2:0;
		double minValue = cal.getCValue(min);
		double maxValue = cal.getCValue(max);
		minValue = cal.getRawValue(minValue);
		maxValue = cal.getRawValue(maxValue);
		if (maxValue>=minValue) {
			min = minValue;
			max = maxValue;
			setMinAndMax(imp, min, max);
			if (RGBImage) doMasking(imp, ip);
		}
	}
    
    public static void main(String args[]) {
        try {
    	PlexiImageFile pf = new PlexiImageFile();
        pf.setURIAsString("file:/C:/data/archive/FTest2/arc001/061121_tc22923/PROCESSED/MPRAGE/T88_111/061121_tc22923_mpr_n2_111_t88_gfc.4dfp");
        pf.setXsiType(PlexiConstants.XNAT_IMAGERESOURCE);
        PlexiFileOpener pfo = new PlexiFileOpener("ANALYZE",pf);
 
		ImageJ ij = IJ.getInstance();
	     if (ij == null || !ij.quitting()) {	// initialize IJ and make a window
	    	 new ImageJ().exitWhenQuitting(false);
	    	 ij = IJ.getInstance();
	    	 ij.setVisible(true);
	     }

        
        ImagePlus img = pfo.getImagePlus();
        IntensitySetter i = new IntensitySetter(img, true);
        i.autoAdjust(img, img.getProcessor());
        
        PlexiImageOrientor pio=new PlexiImageOrientor(img,pf.getFormat());
        ImagePlus img1 = pio.getImage(pfo.getOrientation(),"CORONAL"+"F");


//        BitConverter converter = new BitConverter();
//        converter.convertToGrayscale(img);


        BitConverter converter = new BitConverter();
        converter.convertToGrayscale(img1);

        img1.show();

        
        //        System.out.println("Image 8 bit");
//        i.setMinMax(img);

        System.out.println(img.getProcessor().getMin() + "   " + img.getProcessor().getMax());

        //img.show();
        }catch(Exception e) {e.printStackTrace();}
    }
}   

