/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.nrrd;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.measure.Calibration;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import org.nrg.plexiViewer.Reader.ImageReader;
import org.nrg.plexiViewer.Reader.ReaderUtils;
import org.nrg.plexiViewer.utils.FileUtils;
import org.nrg.plexiViewer.utils.UnzipFile;
import org.nrg.plexiViewer.utils.Transform.PlexiImageOrientor;


public class NRRDReader implements ImageReader{

	public final String uint8Types="uchar, unsigned char, uint8, uint8_t";
	public final String int16Types="short, short int, signed short, signed short int, int16, int16_t";
	public final String uint16Types="ushort, unsigned short, unsigned short int, uint16, uint16_t";
	public final String int32Types="int, signed int, int32, int32_t";
	public final String uint32Types="uint, unsigned int, uint32, uint32_t";
	private String notes = "";
	
	private boolean detachedHeader=false;
	private String headerPath=null;
	private String imagePath=null;
	private String imageName=null;
	private String  fileName, directory;
    private NrrdFileInfo fi;
	private Calibration spatialCal;
	private boolean zipped = false;
	private String zext = ".gz";
	
	public NRRDReader(String dir, String filename) {
		directory = dir; this.fileName = filename;
	}

	public NRRDReader(String filePath) {
		int i = filePath.lastIndexOf(File.separator);
		if (i != -1) {
			directory = filePath.substring(0, i);
			fileName = filePath.substring(i+1);
		}else {
			fileName = filePath;
			directory = "";
		}
	}
	
	  public boolean isZipped() {
	    	return zipped;
	    }

	
	public ImagePlus getImagePlus() {

		ImagePlus imp = load();

		if (imp==null) return null;  // failed to load the file		
		if (imageName!=null) {
			// set the name of the image to the name found inside the load method
			// TOFIX - what should the name be?  There could be several
			// image files referenced in a single detached .nhdr
			imp.setStack(imageName, imp.getStack());
		} else {
			imp.setStack(fileName, imp.getStack());
		}
				
		if (!notes.equals(""))
			imp.setProperty("Info", notes);
		// bring over the calibration information as well
		imp.copyScale(imp);
		return imp;
	}

	
	 public void clearTempFolder() {
	    	if (zipped) {
	    		FileUtils.deleteFile(directory, true);
	    	}
	    }
	 
	public ImagePlus load() {    
		if (!directory.endsWith(File.separator)) directory += File.separator;
		if ((fileName == null) || (fileName == "") ) return null;
        if (fileName.endsWith(zext)) {
            zipped = true;
        }
        unzip();
		
		try {
			getHeaderInfo();
		}
		catch (IOException e) { 
			IJ.write("readHeader: "+ e.getMessage()); 
			return null;
		}
		
		
		ImagePlus imp; FlexibleFileOpener gzfo;
		
		if(fi.encoding.equals("gzip") && detachedHeader) {
			// call my nice gzip opener plugin which has had the 
			// createInputStream method overloaded.
			gzfo = new FlexibleFileOpener(fi,FlexibleFileOpener.GZIP);
			imp = gzfo.open(false);
		} else if(fi.encoding.equals("gzip")) {
			long preOffset=fi.longOffset>0?fi.longOffset:fi.offset;
			fi.offset=0;fi.longOffset=0;
			gzfo= new FlexibleFileOpener(fi,FlexibleFileOpener.GZIP,preOffset);
			if (IJ.debugMode) IJ.log("gzfo:"+gzfo);
			imp = gzfo.open(false);			
		} else {
			FileOpener fo = new FileOpener(fi);  
			imp = fo.open(false);
		}
		if(imp==null) return null;
		
		// Copy over the spatial scale info which we found in readHeader
		// nb the first we don't just overwrite the current calibration 
		// because this may have density calibration for signed images 
		Calibration cal = imp.getCalibration();
		cal.pixelWidth=spatialCal.pixelWidth;
		cal.pixelHeight=spatialCal.pixelHeight;        
		cal.pixelDepth=spatialCal.pixelDepth;
		cal.setUnit(spatialCal.getUnit());
		cal.xOrigin=spatialCal.xOrigin;		
		cal.yOrigin=spatialCal.yOrigin;		
		cal.zOrigin=spatialCal.zOrigin;
		imp.setCalibration(cal);		
		//if (zipped) FileUtils.deleteFile(directory); Now handled by FileOpener
		return imp; 
	}
	
	public FileInfo getFileInfo() {
		return (FileInfo)fi;
	}
	
	public String getOrientation() {
		return PlexiImageOrientor.AS_ACQUIRED_TXT;
	}
	
	public int getOrientationForWriter() {
		return ReaderUtils.getOrientationAsInt(getOrientation(),false);
   }
	
	 public int getVolumes() {
	        return 1;
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
                    new UnzipFile().gunzip(directory + File.separator + fileName, dir.getPath());
	                directory = dir.getPath();
	            }catch (IOException ioe) {System.out.println("DicomSequence:: Unable to create temporary directory");}
	        }
	        //System.out.println("Directory " + directory);

	    }
	
	 
	public NrrdFileInfo getHeaderInfo() throws IOException {

		if (IJ.debugMode) IJ.log("Entering Nrrd_Reader.readHeader():");
		fi = new NrrdFileInfo();
		fi.directory=directory; fi.fileName=fileName;
		spatialCal = new Calibration();
		
		// NB Need RAF in order to ensure that we know file offset
		RandomAccessFile input = new RandomAccessFile(fi.directory+fi.fileName,"r");

		String thisLine,noteType,noteValue, noteValuelc;

		fi.fileType = FileInfo.GRAY8;  // just assume this for the mo
		spatialCal.setUnit("micron");  // just assume this for the mo    
		fi.fileFormat = FileInfo.RAW;
		fi.nImages = 1;

		// parse the header file, until reach an empty line//	boolean keepReading=true;
		while(true) {
			thisLine=input.readLine();
			if(thisLine==null || thisLine.equals("")) {
				if(!detachedHeader) fi.longOffset = input.getFilePointer();
				break;
			}		
			notes+=thisLine+"\n";

			if(thisLine.indexOf("#")==0) continue; // ignore comments

			noteType=getFieldPart(thisLine,0).toLowerCase(); // case irrelevant
			noteValue=getFieldPart(thisLine,1);
			noteValuelc=noteValue.toLowerCase();
			String firstNoteValue=getSubField(thisLine,0);
//			String firstNoteValuelc=firstNoteValue.toLowerCase();

			if (IJ.debugMode) IJ.log("NoteType:"+noteType+", noteValue:"+noteValue);

			if (noteType.equals("data file")||noteType.equals("datafile")) {
				// This is a detached header file
				// There are 3 kinds of specification for the data files
				// 	1.	data file: <filename>
				//	2.	data file: <format> <min> <max> <step> [<subdim>]
				//	3.	data file: LIST [<subdim>]
				if(firstNoteValue.equals("LIST")) {
					// TOFIX - type 3
					throw new IOException("Nrrd_Reader: not yet able to handle datafile: LIST specifications");
				} else if(!getSubField(thisLine,1).equals("")) {
					// TOFIX - type 2
					throw new IOException("Nrrd_Reader: not yet able to handle datafile: sprintf file specifications");
				} else {
					// Type 1 specification
					File imageFile;
					// Relative or absolute
					if(noteValue.indexOf("/")==0) {
						// absolute
						imageFile=new File(noteValue);
						// TOFIX could also check local directory if absolute path given
						// but dir does not exist
					} else {
						//IJ.log("fi.directory = "+fi.directory);					
						imageFile=new File(fi.directory,noteValue);
					}
					//IJ.log("image file ="+imageFile);

					if(imageFile.exists()) {
						fi.directory=imageFile.getParent();
						fi.fileName=imageFile.getName();
						imagePath=imageFile.getPath();
						detachedHeader=true;
					} else {
						throw new IOException("Unable to find image file ="+imageFile.getPath());
					}
				}										
			}

			if (noteType.equals("dimension")) {
				fi.dimension=Integer.valueOf(noteValue).intValue();
				if(fi.dimension>3) throw new IOException("Nrrd_Reader: Dimension>3 not yet implemented!");
			}
			if (noteType.equals("sizes")) {
				fi.sizes=new int[fi.dimension];
				for(int i=0;i<fi.dimension;i++) {
					fi.sizes[i]=Integer.valueOf(getSubField(thisLine,i)).intValue();
					if(i==0) fi.width=fi.sizes[0];
					if(i==1) fi.height=fi.sizes[1];
					if(i==2) fi.nImages=fi.sizes[2];
				}
			}

			if (noteType.equals("units")) spatialCal.setUnit(firstNoteValue);
			if (noteType.equals("spacings")) {
				double[] spacings=new double[fi.dimension];
				for(int i=0;i<fi.dimension;i++) {
					// TOFIX - this order of allocations is not a given!
					spacings[i]=Double.valueOf(getSubField(thisLine,i)).doubleValue();
					if(i==0) spatialCal.pixelWidth=spacings[0];
					if(i==1) spatialCal.pixelHeight=spacings[1];
					if(i==2) spatialCal.pixelDepth=spacings[2];
				}
			}
			if (noteType.equals("centers") || noteType.equals("centerings")) {
				fi.centers=new String[fi.dimension];
				for(int i=0;i<fi.dimension;i++) {
					// TOFIX - this order of allocations is not a given!
					fi.centers[i]=getSubField(thisLine,i);
				}
			}

			if (noteType.equals("axis mins") || noteType.equals("axismins")) {
				double[] axismins=new double[fi.dimension];
				for(int i=0;i<fi.dimension;i++) {
					// TOFIX - this order of allocations is not a given!
					// NB xOrigin are in pixels, whereas axismins are of course
					// in units; these are converted later
					axismins[i]=Double.valueOf(getSubField(thisLine,i)).doubleValue();
					if(i==0) spatialCal.xOrigin=axismins[0];
					if(i==1) spatialCal.yOrigin=axismins[1];
					if(i==2) spatialCal.zOrigin=axismins[2];
				}
			}
			if (noteType.equals("type")) {
				if (uint8Types.indexOf(noteValuelc)>=0) {
					fi.fileType=FileInfo.GRAY8;
				} else if(uint16Types.indexOf(noteValuelc)>=0) {
					fi.fileType=FileInfo.GRAY16_SIGNED;
				} else if(int16Types.indexOf(noteValuelc)>=0) {
					fi.fileType=FileInfo.GRAY16_UNSIGNED;
				} else if(uint32Types.indexOf(noteValuelc)>=0) {
					fi.fileType=FileInfo.GRAY32_UNSIGNED;
				} else if(int32Types.indexOf(noteValuelc)>=0) {
					fi.fileType=FileInfo.GRAY32_INT;
				} else if(noteValuelc.equals("float")) {
					fi.fileType=FileInfo.GRAY32_FLOAT;
				} else if(noteValuelc.equals("double")) {
					fi.fileType=FileInfo.GRAY64_FLOAT;
				} else {
					throw new IOException("Unimplemented data type ="+noteValue);
				}
			}

			if (noteType.equals("byte skip")||noteType.equals("byteskip")) fi.longOffset=Long.valueOf(noteValue).longValue();
			if (noteType.equals("endian")) {
				if(noteValuelc.equals("little")) {
					fi.intelByteOrder = true;
				} else {
					fi.intelByteOrder = false;
				}
			}

			if (noteType.equals("encoding")) {
				if(noteValuelc.equals("gz")) noteValuelc="gzip";
				fi.encoding=noteValuelc;
			}	
		}


		// Fix axis mins, converting them to pixels
		// if clause is to guard against cases where there is no spatial
		// calibration info leading to Inf
		if(spatialCal.pixelWidth!=0) spatialCal.xOrigin=spatialCal.xOrigin/spatialCal.pixelWidth;
		if(spatialCal.pixelHeight!=0) spatialCal.yOrigin=spatialCal.yOrigin/spatialCal.pixelHeight;
		if(spatialCal.pixelDepth!=0) spatialCal.zOrigin=spatialCal.zOrigin/spatialCal.pixelDepth;

		// Axis min will be the centre of the first pixel if this a "cell" nrrd
		// or at the (top, front, left) if this is a "node" nrrd.
		// ImageJ works on a node basis - that is it treats each voxel as a
		// cube located at its top left corner (or more accurately I think the 
		// corner closer to the coordinate origin); however the image extent
		// displayed is the "bounds" ie spacing*n
		// So to convert a cell based nrrd to a node based ImagePlus, need to
		// shift origin by 1/2 voxel dims in each dimension
		// Since the nrrd specified origin would have been the centre of the
		// voxel we need to SUBTRACT 1/2 voxel dims for ImageJ 
		// See http://teem.sourceforge.net/nrrd/format.html#centers

		if(fi.centers!=null) {
			if(fi.centers[0].equals("cell")) spatialCal.xOrigin-=spatialCal.pixelWidth/2;
			if(fi.centers[1].equals("cell")) spatialCal.yOrigin-=spatialCal.pixelHeight/2;
			if(fi.dimension>2 && fi.centers[2].equals("cell")) spatialCal.zOrigin-=spatialCal.pixelDepth/2;
		}

		if(!detachedHeader) fi.longOffset = input.getFilePointer();
		input.close();
		return (fi);
	}

	// This gets a space delimited field from a nrrd string
	// of the form
	// a long name: space delimited values
	// but note only works with Java >=1.4 Ithink


	String getFieldPart(String str, int fieldIndex) {
		str=str.trim(); // trim the string
		String[] fieldParts=str.split(":\\s+");
		if(fieldParts.length<2) return(fieldParts[0]);
		//IJ.log("field = "+fieldParts[0]+"; value = "+fieldParts[1]+"; fieldIndex = "+fieldIndex);

		if(fieldIndex==0) return fieldParts[0];
		else return fieldParts[1];
	}

	String getSubField(String str, int fieldIndex) {
		String fieldDescriptor=getFieldPart(str,1);
		fieldDescriptor=fieldDescriptor.trim(); // trim the string

		if (IJ.debugMode) IJ.log("fieldDescriptor = "+fieldDescriptor+"; fieldIndex = "+fieldIndex);

		String[] fields_values=fieldDescriptor.split("\\s+");

		if (fieldIndex>=fields_values.length) {
			return "";
		} else {
			String rval=fields_values[fieldIndex];
			if(rval.startsWith("\"")) rval=rval.substring(1);
			if(rval.endsWith("\"")) rval=rval.substring(0, rval.length()-1);
			return rval;
		}
	}
}

	 

