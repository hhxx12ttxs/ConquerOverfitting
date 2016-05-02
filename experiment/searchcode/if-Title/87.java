/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.plexiViewer.utils.imageformats;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import ij.process.StackProcessor;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.nrg.plexiViewer.Reader.DICOMReader;
import org.nrg.plexiViewer.utils.FileUtils;
import org.nrg.plexiViewer.utils.UnzipFile;

public class DicomFileOpener {
    
    private boolean convertToGrayscale, convertToRGB;
    private double scale = 100.0;
    private int n, start, increment;
    private FileInfo fi;
    private String info1;
    private String directory;
    private String orientation=null;
    private String patientOrientation = null;
    private String[] list;
    private String title;
    boolean zipped = false;
    
    public static final String ORIENTATION_AS_ACQUIRED= "As Acquired";
    
    String rowAxis;
    String colAxis;
    int width=0,height=0,depth=0,bitDepth=0;
    
    public DicomFileOpener(String dir) {
        directory = dir;
        setTitle();
        list = (new File(directory)).list();
        String zext = ".gz"; 
        for (int i = 0; i < list.length; i++) {
            if (list[i].endsWith(zext)) {
                zipped = true;
            }
        }
        unzip();
    }

    public DicomFileOpener(ArrayList fileList) {
        directory = ((File)fileList.get(0)).getParent();
        if (directory.startsWith("file:")) directory = directory.substring(5);
        list = new String[fileList.size()];
        setTitle();
        String zext = ".gz"; 
        for (int i = 0; i < fileList.size(); i++) {
            list[i] = ((File)fileList.get(i)).getName();
            if (!(new File(directory+File.separator+list[i]).exists()) && (new File(directory+File.separator+list[i]+zext).exists())) {
                zipped = true;
            }else if (list[i].endsWith(".gz")) {
                zipped = true;
            }
            //if (zipped) break;
        }
        unzip();
    }

    
    private void unzip() {
        String zext = ".gz"; 
        if (zipped) {
            String suffix =  "_" + new Random().nextInt();
            File tempDir = new File(FileUtils.getTempFolder());
            try {
                File dir = File.createTempFile( "NRG", suffix,  tempDir);
                if (dir.exists()) dir.delete();
                System.out.println("DicomSequence tempdir " + dir.getPath());
                
                boolean success = dir.mkdir();
                System.out.println("DicomSequence tempdir " + dir.getPath() + " success " + success) ;
                for (int i = 0; i < list.length; i++) {
                    new UnzipFile().gunzip(directory + File.separator + list[i], dir.getPath());
                }    
                directory = dir.getPath();
                list = (new File(directory)).list();
            }catch (IOException ioe) {
            	System.out.println("DicomSequence:: Unable to create temporary directory " + ioe.getMessage());
            }catch(Exception ee) {
            	ee.printStackTrace();
            }
            }
        //System.out.println("Directory " + directory);

    }
    
    private void setTitle() {
        title = directory;
        if (title.endsWith(File.separator))
            title = title.substring(0, title.length()-1);
        int index = title.lastIndexOf(File.separatorChar);
        if (index!=-1) title = title.substring(index + 1);
        if (title.endsWith(":"))
            title = title.substring(0, title.length()-1);
    }
    
    public ImagePlus getImagePlus() {
        //return getImagePlus(null, true, false);
    	return getImagePlus(null);
    }   

    public ImagePlus getImagePlus(String filter, boolean convertToGrayscale, boolean convertToRGB ) {
            ImagePlus rtn=null;
            this.convertToGrayscale = convertToGrayscale;
            this.convertToRGB = convertToRGB;
            try {
                list = sortFileList();
            }catch(Exception e) {
                e.printStackTrace();
                return null;
            }
            n = list.length;
            start=0; increment=1; scale=100;
            if (IJ.debugMode) IJ.log("DicomSequence: "+directory+" ("+list.length+" files)");
            ImageStack stack = null;
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            Calibration cal = null;
            boolean allSameCalibration = true;
            int count = 0;
            try {
                for (int i=start; i<list.length; i++) {
                    Opener opener = new Opener();
                    opener.setSilentMode(true);
                    ImagePlus imp = opener.openImage(directory, list[i]);
                    if (imp!=null && stack==null) {
                        width = imp.getWidth();
                        height = imp.getHeight();
                        depth = imp.getStackSize();
                        bitDepth = imp.getBitDepth();
                        cal = imp.getCalibration();
                        if (convertToRGB) bitDepth = 24;
                        if (convertToGrayscale) bitDepth = 8;
                        ColorModel cm = imp.getProcessor().getColorModel();
                        if (scale<100.0)
                            stack = new ImageStack((int)(width*scale/100.0), (int)(height*scale/100.0), cm);
                        else
                            stack = new ImageStack(width, height, cm);
                    }
                    if (imp==null) {
                        if (!list[i].startsWith("."))
                            IJ.log(list[i] + ": unable to open");
                        continue;
                    }
               
                    if (imp.getWidth()!=width || imp.getHeight()!=height) {
                        IJ.log(list[i] + ": wrong size; "+width+"x"+height+" expected, "+imp.getWidth()+"x"+imp.getHeight()+" found");
                        continue;
                    }
               
                    //Convert image to 8bit color
          /*          if  (imp.getType()==ImagePlus.COLOR_RGB) {
                      int nColors = 256;	
                      System.out.println("Stack size " + stack.getSize());
                      if (stack.getSize() > 2) { //Not greater than 1 as StackConverter needs a stack size of 2 or more
                    	  new StackConverter(imp).convertToIndexedColor(nColors);
                      }else {
                    	  ImageConverter ic = new ImageConverter(imp);
                    	  ic.convertRGBtoIndexedColor(nColors);
                      }
                    }*/
                    String label = imp.getTitle();
                    if (depth==1) {
                        String info = (String)imp.getProperty("Info");
                        if (info!=null)
                            label += "\n" + info;
                    }
                    if (imp.getCalibration().pixelWidth!=cal.pixelWidth)
                        allSameCalibration = false;
                    ImageStack inputStack = imp.getStack();
                    for (int slice=1; slice<=inputStack.getSize(); slice++) {
                        ImageProcessor ip = inputStack.getProcessor(slice);
                        int bitDepth2 = imp.getBitDepth();
                        if (convertToRGB) {
                            ip = ip.convertToRGB();
                            bitDepth2 = 24;
                        } else if(convertToGrayscale) {
                            ip = ip.convertToByte(true);
                            bitDepth2 = 8;
                        }
                        if (bitDepth2!=bitDepth) {
                            if (bitDepth==8) {
                                ip = ip.convertToByte(true);
                                bitDepth2 = 8;
                            } else if (bitDepth==24) {
                                ip = ip.convertToRGB();
                                bitDepth2 = 24;
                            }
                        }
                        if (bitDepth2!=bitDepth) {
                            IJ.log(list[i] + ": wrong bit depth; "+bitDepth+" expected, "+bitDepth2+" found");
                            break;
                        }
                        if (slice==1) count++;
                        //IJ.showStatus(count+"/"+n);
                        //IJ.showProgress(count, n);
                        if (scale<100.0)
                            ip = ip.resize((int)(width*scale/100.0), (int)(height*scale/100.0));
                        if (ip.getMin()<min) min = ip.getMin();
                        if (ip.getMax()>max) max = ip.getMax();
                        String label2 = label;
                        if (depth>1) label2 = ""+slice;
                        stack.addSlice(label2, ip);
                    }
                    if (count>=n)
                        break;
                    //if (IJ.escapePressed())
                    //{IJ.beep(); break;}
                    //System.gc();
                }
            } catch(OutOfMemoryError e) {
                //IJ.outOfMemory("Import_Dicom_Sequence");
                if (stack!=null) stack.trim();
            }
            if (stack!=null && stack.getSize()>0) {
                ImagePlus imp2 = new ImagePlus(title, stack);
                if (imp2.getType()==ImagePlus.GRAY16 || imp2.getType()==ImagePlus.GRAY32)
                    imp2.getProcessor().setMinAndMax(min, max);
                imp2.setFileInfo(fi); // saves FileInfo of the first image
                if (allSameCalibration)
                    imp2.setCalibration(cal); // use calibration from first image
                if (imp2.getStackSize()==1 && info1!=null)
                    imp2.setProperty("Info", info1);
                rtn=imp2;
            }
            rtn = reorientImage(rtn);
            if (zipped) FileUtils.deleteFile(directory, true);
            return rtn;
        }
    
    public ImagePlus getImagePlus(String filter) {
        ImagePlus rtn=null;
        try {
            list = sortFileList();
        }catch(Exception e) {
            e.printStackTrace();
        	   if (zipped) FileUtils.deleteFile(directory, true);
        	   
            return null;
        }
        n = list.length;
        start=0; increment=1; scale=100;
        if (IJ.debugMode) IJ.log("DicomSequence: "+directory+" ("+list.length+" files)");
        ImageStack stack = null;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        Calibration cal = null;
        boolean allSameCalibration = true;
        int count = 0;
        try {
            for (int i=start; i<list.length; i++) {
                Opener opener = new Opener();
                opener.setSilentMode(true);
                ImagePlus imp = opener.openImage(directory, list[i]);
                if (imp!=null && stack==null) {
                    width = imp.getWidth();
                    height = imp.getHeight();
                    depth = imp.getStackSize();
                    bitDepth = imp.getBitDepth();
                    cal = imp.getCalibration();
                    ColorModel cm = imp.getProcessor().getColorModel();
                    if (scale<100.0)
                        stack = new ImageStack((int)(width*scale/100.0), (int)(height*scale/100.0), cm);
                    else
                        stack = new ImageStack(width, height, cm);
                }
                if (imp==null) {
                    if (!list[i].startsWith("."))
                        IJ.log(directory + File.separator + list[i] + ": unable to open");
                    continue;
                }
           
                if (imp.getWidth()!=width || imp.getHeight()!=height) {
                    IJ.log(list[i] + ": wrong size; "+width+"x"+height+" expected, "+imp.getWidth()+"x"+imp.getHeight()+" found");
                    continue;
                }
           
                //Convert image to 8bit color
      /*          if  (imp.getType()==ImagePlus.COLOR_RGB) {
                  int nColors = 256;	
                  System.out.println("Stack size " + stack.getSize());
                  if (stack.getSize() > 2) { //Not greater than 1 as StackConverter needs a stack size of 2 or more
                	  new StackConverter(imp).convertToIndexedColor(nColors);
                  }else {
                	  ImageConverter ic = new ImageConverter(imp);
                	  ic.convertRGBtoIndexedColor(nColors);
                  }
                }*/
                String label = imp.getTitle();
                if (depth==1) {
                    String info = (String)imp.getProperty("Info");
                    if (info!=null)
                        label += "\n" + info;
                }
                if (imp.getCalibration().pixelWidth!=cal.pixelWidth)
                    allSameCalibration = false;
                ImageStack inputStack = imp.getStack();
                for (int slice=1; slice<=inputStack.getSize(); slice++) {
                    ImageProcessor ip = inputStack.getProcessor(slice);
                    if (slice==1) count++;
                    //IJ.showStatus(count+"/"+n);
                    //IJ.showProgress(count, n);
                    if (scale<100.0)
                        ip = ip.resize((int)(width*scale/100.0), (int)(height*scale/100.0));
                    if (ip.getMin()<min) min = ip.getMin();
                    if (ip.getMax()>max) max = ip.getMax();
                    String label2 = label;
                    if (depth>1) label2 = ""+slice;
                    stack.addSlice(label2, ip);
                }
                if (count>=n)
                    break;
                //if (IJ.escapePressed())
                //{IJ.beep(); break;}
                //System.gc();
            }
            if (stack!=null && stack.getSize()>0) {
                ImagePlus imp2 = new ImagePlus(title, stack);
                if (imp2.getType()==ImagePlus.GRAY16 || imp2.getType()==ImagePlus.GRAY32)
                    imp2.getProcessor().setMinAndMax(min, max);
                imp2.setFileInfo(fi); // saves FileInfo of the first image
                if (allSameCalibration)
                    imp2.setCalibration(cal); // use calibration from first image
                if (imp2.getStackSize()==1 && info1!=null)
                    imp2.setProperty("Info", info1);
                rtn=imp2;
            }
            rtn = reorientImage(rtn);
        } catch(OutOfMemoryError e) {
            //IJ.outOfMemory("Import_Dicom_Sequence");
        }finally {
            //if (stack!=null) stack.trim();
            if (zipped) FileUtils.deleteFile(directory, true);
        }
        return rtn;
    }
        
    private ImagePlus reorientImage(ImagePlus img) {
        StackProcessor sp = null;
        ImagePlus rtn = img;
        if (orientation.equals(ORIENTATION_AS_ACQUIRED) ) {
        	return rtn;
        }else {
	        if (orientation.equals("SAGITTAL")) {
	            if (rowAxis.equals("A") && colAxis.equals("H")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                sp.flipVertical();
	                rtn = new ImagePlus(img.getTitle(), img.getStack());
	            }else if (rowAxis.equals("F") && colAxis.equals("A")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateLeft();
	                rtn = new ImagePlus(img.getTitle(), stack);
	                sp =  new StackProcessor(rtn.getStack(), rtn.getProcessor());
	                sp.flipVertical();
	            }else if (rowAxis.equals("H") && colAxis.equals("A")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateLeft();
	                rtn = new ImagePlus(img.getTitle(), stack);
	            }else if (rowAxis.equals("P") && colAxis.equals("H")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                sp.flipHorizontal();
	                sp.flipVertical();
	                rtn = new ImagePlus(img.getTitle(), img.getStack());
	            }else if (rowAxis.equals("P") && colAxis.equals("F")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                sp.flipHorizontal();
	                rtn = new ImagePlus(img.getTitle(), img.getStack());
	            }else if (rowAxis.equals("H") && colAxis.equals("P")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateLeft();
	                rtn = new ImagePlus(img.getTitle(), stack);
	                sp =  new StackProcessor(rtn.getStack(), rtn.getProcessor());
	                sp.flipHorizontal();
	            }else if (rowAxis.equals("F") && colAxis.equals("P")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateRight();
	                rtn = new ImagePlus(img.getTitle(), stack);
	            }
	            rtn = new ImagePlus(rtn.getTitle(), reverseStack(rtn.getStack(), rtn));
	        }else if (orientation.equals("TRANSVERSE")) {
	            if (rowAxis.equals("R") && colAxis.equals("A")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                sp.flipVertical();
	                sp.flipHorizontal();
	                rtn = new ImagePlus(img.getTitle(), img.getStack());
	            }else if (rowAxis.equals("R") && colAxis.equals("P")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                sp.flipHorizontal();
	                rtn = new ImagePlus(img.getTitle(), img.getStack());
	            }else if (rowAxis.equals("L") && colAxis.equals("A")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                sp.flipVertical();
	                rtn = new ImagePlus(img.getTitle(), img.getStack());
	            }else if (rowAxis.equals("A") && colAxis.equals("R")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateRight();
	                rtn = new ImagePlus(img.getTitle(), stack);
	                sp =  new StackProcessor(rtn.getStack(), rtn.getProcessor());
	                sp.flipVertical();
	            }else if (rowAxis.equals("A") && colAxis.equals("L")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateLeft();
	                rtn = new ImagePlus(img.getTitle(), stack);
	            }else if (rowAxis.equals("P") && colAxis.equals("R")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateRight();
	                rtn = new ImagePlus(img.getTitle(), stack);
	            }else if (rowAxis.equals("P") && colAxis.equals("L")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateRight();
	                rtn = new ImagePlus(img.getTitle(), stack);
	                sp =  new StackProcessor(rtn.getStack(), rtn.getProcessor());
	                sp.flipHorizontal();
	            }
	        }else if (orientation.equals("CORONAL")) {
	            if (rowAxis.equals("R") && colAxis.equals("H")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                sp.flipVertical();
	                sp.flipHorizontal();
	                rtn = new ImagePlus(img.getTitle(), img.getStack());
	            }else if (rowAxis.equals("R") && colAxis.equals("F")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                sp.flipHorizontal();
	                rtn = new ImagePlus(img.getTitle(), img.getStack());
	            }else if (rowAxis.equals("L") && colAxis.equals("H")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                sp.flipVertical();
	                rtn = new ImagePlus(img.getTitle(), img.getStack());
	            }else if (rowAxis.equals("H") && colAxis.equals("R")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateRight();
	                rtn = new ImagePlus(img.getTitle(), stack);
	                sp =  new StackProcessor(rtn.getStack(), rtn.getProcessor());
	                sp.flipVertical();
	            }else if (rowAxis.equals("H") && colAxis.equals("L")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateLeft();
	                rtn = new ImagePlus(img.getTitle(), stack);
	            }else if (rowAxis.equals("F") && colAxis.equals("R")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateRight();
	                rtn = new ImagePlus(img.getTitle(), stack);
	            }else if (rowAxis.equals("F") && colAxis.equals("L")) {
	                sp =  new StackProcessor(img.getStack(), img.getProcessor());
	                ImageStack stack = sp.rotateLeft();
	                rtn = new ImagePlus(img.getTitle(), stack);
	                sp =  new StackProcessor(rtn.getStack(), rtn.getProcessor());
	                sp.flipVertical();
	            }
	            rtn = new ImagePlus(rtn.getTitle(), reverseStack(rtn.getStack(), rtn));
	        }
	        orientation += "F";
	     }
        return rtn;
    }
    
    private ImageStack reverseStack(ImageStack stack, ImagePlus imp) {
        int n;
        ImageStack stack2 = imp.createEmptyStack();
        while ((n=stack.getSize())>0) {
            stack2.addSlice(stack.getSliceLabel(n), stack.getProcessor(n));
            stack.deleteLastSlice();
        }
        return stack2;
   }

    
    /*
     * Find the distnace of the fixtvoxel on each slice along the normal to the slice plane
     * Order the slices based on this distnace rather than the Instance Number tag
     */
    private String[] sortFileList() throws Exception {
        float normal[] = new float[3];
        float cosines[] = new float[6];
        DicomImage dcmImage[] = new DicomImage[list.length];
        String[] rtn = new String[list.length];
        boolean secondaryImage = false;
        for (int i=0; i<list.length; i++) {
            ImagePlus imp = (new Opener()).openImage(directory, list[i]);
            if (imp!=null) {
                String taginfo = (String)imp.getProperty("Info");
                float ipp[] = new float[3];
                if (i==0) {
                    width = imp.getWidth();
                    height = imp.getHeight();
                    depth = imp.getStackSize();
                    bitDepth = imp.getBitDepth();
                    info1 = taginfo;
                    setOrientation();
                    setPatientOrientation();
                    int imageOrientationPatient= info1.indexOf("0020,0037");
                    if (imageOrientationPatient != -1) {
	                    String dcs = (((info1.substring(imageOrientationPatient+10).split("\n"))[0]).split(":"))[1];
	                    String[] inDCS= dcs.split("\\\\");
	                    for (int k = 0; k < 3; k++) { 
	                        cosines[k] = Float.parseFloat(inDCS[k]);
	                        cosines[k+3] = Float.parseFloat(inDCS[k+3]);
	                    }
	                    normal[0] = cosines[1]*cosines[5] - cosines[2]*cosines[4];
	                    normal[1] = cosines[2]*cosines[3] - cosines[0]*cosines[5];
	                    normal[2] = cosines[0]*cosines[4] - cosines[1]*cosines[3];
                    }
                    else secondaryImage = true;
                }
                int imagePositionPatient= taginfo.indexOf("0020,0032");
                if (imagePositionPatient != -1) {
                    String ippStr = (((taginfo.substring(imagePositionPatient+10).split("\n"))[0]).split(":"))[1];
                    int instanceNumber = taginfo.indexOf("0020,0013");
                    if (instanceNumber != -1) {
	                    String instanceNo = (((taginfo.substring(instanceNumber+10).split("\n"))[0]).split(":"))[1];
	                    String[] ippStrs= ippStr.split("\\\\");
	                    for (int j = 0; j < 3; j++)
	                        ipp[j] = Float.parseFloat(ippStrs[j]);
	                    float dist = 0;
	                    for (int j = 0; j < 3; j++)
	                        dist +=  normal[j]*ipp[j];
	                    dcmImage[i] = new DicomImage(list[i],instanceNo,dist);
                    }else secondaryImage = true;
                }else secondaryImage = true;
            }
       }
        if (secondaryImage) {
        	  for (int j =0; j < list.length; j++) {
  	            rtn[j] = list[j];
  	        }
        }else {
	        Arrays.sort(dcmImage);
	        for (int j =0; j < dcmImage.length; j++) {
	            rtn[j] = dcmImage[j].getFilePath();
	            dcmImage[j]=null;
	        }
        }
        return rtn;
    }
    
    private void setPatientOrientation() {
      try {
    	int patientOri= info1.indexOf("0020,0020");
        if (patientOri == -1) return;
        patientOrientation = (((info1.substring(patientOri+10).split("\n"))[0]).split(":"))[1];
      }catch(Exception e) {
    	  
      }
    }
    
    public String getPatientOrientation() {
        return patientOrientation;
    }
    
    public String getOrientation() {
        return orientation;
    }
    
    private void setOrientation() {
        int imagePositionPatient= info1.indexOf("0020,0037");
        if (imagePositionPatient == -1) {
        	orientation = ORIENTATION_AS_ACQUIRED;
        	return;
        }
        String dcs = (((info1.substring(imagePositionPatient+10).split("\n"))[0]).split(":"))[1];
       try {
    	   rowAxis = DICOMReader.getRowAxis(dcs);
           colAxis = DICOMReader.getColumnAxis(dcs);
           orientation = DICOMReader.setOrientation(dcs);
       }catch(Exception e) {
    	   orientation = ORIENTATION_AS_ACQUIRED;
       }
    }
    
    public static void main(String args[]) {
        //String dir = "C:\\Archive\\TEST_DCM";
        String dir = args[0];
        DicomFileOpener dcm = new DicomFileOpener(dir);
        ImagePlus img1 = dcm.getImagePlus(null);
        ImageStack stack = img1.getImageStack();
        
        if  (img1.getType()==ImagePlus.COLOR_RGB) {
                    int nColors = 256;	
                    if (stack.getSize() > 2) { //Not greater than 1 as StackConverter needs a stack size of 2 or more
                  	  new StackConverter(img1).convertToIndexedColor(nColors);
                    }else {
                  	  ImageConverter ic = new ImageConverter(img1);
                  	  ic.convertRGBtoIndexedColor(nColors);
                    }
                  }

          img1.show();
        System.out.println("All done");
      //  System.exit(0);
        //img.show(); 
        /*PlexiImageOrientor pio = new PlexiImageOrientor(img);
        ImagePlus i2= pio.getImage(dcm.getOrientation(),"TRANSVERSEF");
        i2.show();

        PlexiImageOrientor pio2 = new PlexiImageOrientor(img);
        ImagePlus i4 =  pio2.getImage(dcm.getOrientation(),"CORONALF");
        i4.show(); 

        
        String dir1 = "C:\\temp\\070419_TC24190\\TEST_MPRAGE";
        DicomFileOpener dcm1 = new DicomFileOpener(dir1);
        ImagePlus img1 = dcm1.getImagePlus();
        img1.show();
        PlexiImageOrientor pio1 = new PlexiImageOrientor(img1);
        ImagePlus i3= pio1.getImage(dcm.getOrientation(),"TRANSVERSEF");
        i3.show();
        
        PlexiImageOrientor pio3 = new PlexiImageOrientor(img1);
        ImagePlus i5 =  pio3.getImage(dcm.getOrientation(),"CORONALF");
        i5.show(); */
        
        
    }
    
}

