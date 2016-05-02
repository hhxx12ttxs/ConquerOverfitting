/*
 * org.nrg.plexiViewer.utils.imageformats.DicomSequence
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 10/4/13 10:50 AM
 */


package org.nrg.plexiViewer.utils.imageformats;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.plugin.DICOM_Sorter;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;

import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.nrg.plexiViewer.Reader.DICOMReader;
import org.nrg.plexiViewer.utils.FileUtils;
import org.nrg.plexiViewer.utils.UnzipFile;

public class DicomSequence {
    
	private boolean convertToGrayscale, convertToRGB;
	private double scale = 100.0;
	private int n, start, increment;
	private String filter;
	private FileInfo fi;
	private String info1;
	private String directory;
	private String orientation=null;
	private String patientOrientation = null;
    private String[] list;
    private String title;
    boolean zipped = false;
    
    String rowAxis;
    String colAxis;
    
	public DicomSequence(String dir) {
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
    
    private void unzip() {
        String zext = ".gz"; 
        if (zipped) {
            String suffix =  "_" + new Random().nextInt();
            File tempDir = new File(FileUtils.getTempFolder());
            try {
                File dir = File.createTempFile( "NRG", suffix,  tempDir);
                if (dir.exists()) dir.delete();
                dir.mkdir();
                //System.out.println("DicomSequence tempdir " + dir.getPath());
                for (int i = 0; i < list.length; i++) {
                    new UnzipFile().gunzip(directory + File.separator + list[i], dir.getPath());
                }    
                directory = dir.getPath();
                list = (new File(directory)).list();
            }catch (IOException ioe) {System.out.println("DicomSequence:: Unable to create temporary directory");}
        }
        //System.out.println("Directory " + directory);

    }

    public DicomSequence(ArrayList fileList) {
        directory = ((File)fileList.get(0)).getParent();
        if (directory.startsWith("file:")) directory = directory.substring(5);
        list = new String[fileList.size()];
        setTitle();
        String zext = ".gz"; 
        for (int i = 0; i < fileList.size(); i++) {
            list[i] = ((File)fileList.get(i)).getName();
            if (!(new File(directory+File.separator+list[i]).exists()) && (new File(directory+File.separator+list[i]+zext).exists())) {
                zipped = true;
            }
        }
        
        unzip();
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
		return getImagePlus(null, true, false);
	}	
	    
    
	public ImagePlus getImagePlus(String filter, boolean convertToGrayscale, boolean convertToRGB ) {
       // System.out.println("DICOMSQN entry ");
		ImagePlus rtn=null;
		this.convertToGrayscale = convertToGrayscale;
		this.convertToRGB = convertToRGB;
		this.filter = filter;
       
		list = sortFileList(list);
		n = list.length;
		start=1; increment=1; scale=100;
		if (IJ.debugMode) IJ.log("DicomSequence: "+directory+" ("+list.length+" files)");
		int width=0,height=0,depth=0,bitDepth=0;
		ImageStack stack = null;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		Calibration cal = null;
		boolean allSameCalibration = true;
		try {
			for (int i=0; i<list.length; i++) {
				if (list[i].endsWith(".txt"))
					continue;
				//IJ.redirectErrorMessages();
				ImagePlus imp = (new Opener()).openImage(directory, list[i]);
				if (imp!=null) {
					width = imp.getWidth();
					height = imp.getHeight();
					bitDepth = imp.getBitDepth();
					fi = imp.getOriginalFileInfo();
				}
			}
			if (width==0) {
				IJ.error("Import Sequence", "This folder does not appear to contain any TIFF,\n"
				+ "JPEG, BMP, DICOM, GIF, FITS or PGM files.");
				return null;
			}
            
			if (filter!=null && (filter.equals("") || filter.equals("*")))
				filter = null;
			if (filter!=null) {
				int filteredImages = 0;
				for (int i=0; i<list.length; i++) {
					if (list[i].indexOf(filter)>=0)
						filteredImages++;
					else
						list[i] = null;
				}
				if (filteredImages==0) {
					IJ.error("None of the "+list.length+" files contain\n the string '"+filter+"' in their name.");
					return null;
				}
				String[] list2 = new String[filteredImages];
				int j = 0;
				for (int i=0; i<list.length; i++) {
					if (list[i]!=null)
						list2[j++] = list[i];
				}
				list = list2;
			}
            
			if (n<1)
				n = list.length;
			if (start<1 || start>list.length)
				start = 1;
			if (start+n-1>list.length)
				n = list.length-start+1;
			int count = 0;
			int counter = 0;
			for (int i=start-1; i<list.length; i++) {
				if (list[i].endsWith(".txt"))
					continue;
				if ((counter++%increment)!=0)
					continue;
				Opener opener = new Opener();
				opener.setSilentMode(true);
				//IJ.redirectErrorMessages();
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
					info1 = (String)imp.getProperty("Info");
					if (orientation==null) setOrientation();
                    if (patientOrientation == null) setPatientOrientation();
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
			if (info1!=null && info1.lastIndexOf("7FE0,0010")>0)
				stack = (new DICOM_Sorter()).sort(stack);
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
        reorientImage(rtn);
        if (zipped) FileUtils.deleteFile(directory);
		return rtn;
	}
	
    
    private ImagePlus reorientImage(ImagePlus img) {
        StackProcessor sp = null;
        ImagePlus rtn = img;
        if (orientation.equals("SAGITTAL")) {
            if (rowAxis.equals("A") && colAxis.equals("H")) {
                sp =  new StackProcessor(img.getStack(), img.getProcessor());
                sp.flipVertical();
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
            }else if (rowAxis.equals("P") && colAxis.equals("F")) {
                sp =  new StackProcessor(img.getStack(), img.getProcessor());
                sp.flipHorizontal();
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
        }else if (orientation.equals("TRANSVERSE")) {
            if (rowAxis.equals("R") && colAxis.equals("A")) {
                sp =  new StackProcessor(img.getStack(), img.getProcessor());
                sp.flipVertical();
                sp.flipHorizontal();
            }else if (rowAxis.equals("R") && colAxis.equals("P")) {
                sp =  new StackProcessor(img.getStack(), img.getProcessor());
                sp.flipHorizontal();
            }else if (rowAxis.equals("L") && colAxis.equals("A")) {
                sp =  new StackProcessor(img.getStack(), img.getProcessor());
                sp.flipVertical();
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
            }else if (rowAxis.equals("R") && colAxis.equals("F")) {
                sp =  new StackProcessor(img.getStack(), img.getProcessor());
                sp.flipHorizontal();
            }else if (rowAxis.equals("L") && colAxis.equals("H")) {
                sp =  new StackProcessor(img.getStack(), img.getProcessor());
                sp.flipVertical();
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
    private void setPatientOrientation() {
        int patientOri= info1.indexOf("0020,0020");
        if (patientOri == -1) return;
        patientOrientation = (((info1.substring(patientOri+10).split("\n"))[0]).split(":"))[1];
    }
    
    public String getPatientOrientation() {
        return patientOrientation;
    }
    
	private void setOrientation() {
		int imagePositionPatient= info1.indexOf("0020,0037");
		String dcs = (((info1.substring(imagePositionPatient+10).split("\n"))[0]).split(":"))[1];
		rowAxis = DICOMReader.getRowAxis(dcs);
        colAxis = DICOMReader.getColumnAxis(dcs);
        orientation = DICOMReader.setOrientation(dcs);
	}
  
    
	String[] sortFileList(String[] list) {
		int listLength = list.length;
		int first = listLength>1?1:0;
		if ((list[first].length()==list[listLength-1].length())&&(list[first].length()==list[listLength/2].length()))
		{ij.util.StringSorter.sort(list); return list;}
		int maxDigits = 15;
		String[] list2 = null;
		char ch;
		for (int i=0; i<listLength; i++) {
			int len = list[i].length();
			String num = "";
			for (int j=0; j<len; j++) {
				ch = list[i].charAt(j);
				if (ch>=48&&ch<=57) num += ch;
			}
			if (list2==null) list2 = new String[listLength];
			num = "000000000000000" + num; // prepend maxDigits leading zeroes
			num = num.substring(num.length()-maxDigits);
			list2[i] = num + list[i];
		}
		if (list2!=null) {
			ij.util.StringSorter.sort(list2);
			for (int i=0; i<listLength; i++)
				list2[i] = list2[i].substring(maxDigits);
			return list2;
		} else {
			ij.util.StringSorter.sort(list);
			return list;
		}
	}
    
    
	/**
	 * @return
	 */
	public String getOrientation() {
		return orientation;
	}

    public static void main(String args[]) {
        String dir = "C:\\Archive\\TEST_DCM\\UNZIPPED";
        DicomSequence dcm = new DicomSequence(dir);
        ImagePlus img = dcm.getImagePlus();
        img.show(); 
        /*String dcs = "0.09976304294434\\0.9860005635372\\-0.1336047303309\\-0.0588482066054\\-0.1281926700582\\-0.9900017817775";
        String rowAxis = DICOMReader.getRowAxis(dcs);
        String colAxis = DICOMReader.getColumnAxis(dcs);
        String orientation = DICOMReader.setOrientation(dcs);
        System.out.println("RowAxis " + rowAxis + " ColAxis " + colAxis + " Orientation " + orientation);

        
        String dcs1 = "0.0350859499659\\0.98504861844578\\0.16866592842967\\0.01977314940912\\0.16805257440613\\-0.9855797049442";
        String rowAxis1 = DICOMReader.getRowAxis(dcs1);
        String colAxis1 = DICOMReader.getColumnAxis(dcs1);
        String orientation1 = DICOMReader.setOrientation(dcs1);
        System.out.println("RowAxis1 " + rowAxis1 + " ColAxis1 " + colAxis1 + " Orientation1 " + orientation1);
     
        String dcs2 = "-0.0405411546091\\0.98851686933126\\0.14557064893204\\-0.0378034114035\\0.14406862472235\\-0.988845353661";
        String rowAxis2 = DICOMReader.getRowAxis(dcs2);
        String colAxis2 = DICOMReader.getColumnAxis(dcs2);
        String orientation2 = DICOMReader.setOrientation(dcs2);
        System.out.println("RowAxis2 " + rowAxis2 + " ColAxis2 " + colAxis2 + " Orientation2 " + orientation2);*/
        
    }
}



