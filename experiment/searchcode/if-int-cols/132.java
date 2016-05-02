/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.plexiViewer.converter;

import ij.ImagePlus;

import java.io.File;

import org.nrg.pipeline.xmlbeans.xnat.AbstractResource;
import org.nrg.pipeline.xmlbeans.xnat.ImageResource;
import org.nrg.pipeline.xmlbeans.xnat.PETSessionDocument;
import org.nrg.pipeline.xmlbeans.xnat.PetSessionData;
import org.nrg.pipeline.xmlreader.XmlReader;
import org.nrg.plexiViewer.io.IOHelper;
import org.nrg.plexiViewer.io.PlexiFileOpener;
import org.nrg.plexiViewer.lite.io.PlexiFileSaver;
import org.nrg.plexiViewer.lite.io.PlexiImageFile;
import org.nrg.plexiViewer.utils.FileUtils;
import org.nrg.plexiViewer.utils.LUTApplier;
import org.nrg.plexiViewer.utils.Transform.PlexiImageOrientor;
import org.nrg.plexiViewer.utils.Transform.PlexiMontageMaker;
import org.nrg.xnattools.xml.XMLSearch;

public class PIBQCImageCreator {
    
    String session, xnatId;
    String host;
    String user;
    String pwd;
    boolean radiologic = false;
    int exitStatus = 0;
    String lkupfile;
    String petimageContent;
    String mrimageContent;
    String archiveDir;
    Double minThreshold, maxThreshold;

    public PIBQCImageCreator(String args[]) {
        for(int i=0; i<args.length; i++){
            if (args[i].equalsIgnoreCase("-session") ) {
                if (i+1 < args.length) {
                    session=args[i+1];
                }
            }else  if (args[i].equalsIgnoreCase("-xnatId") ) {
                if (i+1 < args.length) {
                    xnatId=args[i+1];
                }
            }  else if (args[i].equalsIgnoreCase("-host") ) {
                if (i+1 < args.length) {
                    host=args[i+1];
                }
            }else if (args[i].equalsIgnoreCase("-u") ) {
                if (i+1 < args.length) {
                    user=args[i+1];
                }
            }else if (args[i].equalsIgnoreCase("-pwd") ) {
                if (i+1 < args.length) {
                    pwd=args[i+1];
                }
            }else if (args[i].equalsIgnoreCase("-r") ) {
                radiologic = true; 
            }else if (args[i].equalsIgnoreCase("-lut") ) {
                lkupfile = args[i+1]; 
            }else if (args[i].equalsIgnoreCase("-pc") ) {
                petimageContent = args[i+1]; 
            }else if (args[i].equalsIgnoreCase("-mc") ) {
                mrimageContent = args[i+1]; 
            }else if (args[i].equalsIgnoreCase("-a") ) {
                archiveDir = args[i+1]; 
            }else if (args[i].equalsIgnoreCase("-mtmin") ) {
                minThreshold = Double.valueOf(args[i+1]); 
            }else if (args[i].equalsIgnoreCase("-mtmax") ) {
                maxThreshold = Double.valueOf(args[i+1]); 
            }    
        }
        if (session == null || host == null || user == null || pwd == null || lkupfile == null || petimageContent == null || mrimageContent == null || archiveDir == null) {
            handleError();
        }
    }
    
    public void createQCRawImages() {
        File qcFolder = null;
        try {
            String createdFile = new XMLSearch(host, user, pwd).searchFirst("xnat:petSessionData.ID",xnatId, "=","xnat:petSessionData",FileUtils.getTempFolder());
            //Bind the instance to the generated XMLBeans types.
            PETSessionDocument petSession = (PETSessionDocument)new XmlReader().read(createdFile, true);
            /*int indexOfSlash = ((ImageResource)petSession.getPETSession().getScans().getScanArray(0).getFileArray(0).changeType(ImageResource.type)).getURI().lastIndexOf("/RAW/");
            String sessionPath = null;
            if (indexOfSlash != -1) {
            	String uri = ((ImageResource)petSession.getPETSession().getScans().getScanArray(0).getFileArray(0).changeType(ImageResource.type)).getURI();
            	if (uri.startsWith("/")) {
            		sessionPath =  ((ImageResource)petSession.getPETSession().getScans().getScanArray(0).getFileArray(0).changeType(ImageResource.type)).getURI().substring(0,indexOfSlash);
            	}else {
            		int indexofSession = uri.indexOf(petSession.getPETSession().getLabel());
            		if (indexofSession != -1) {
                		String sessionPostfix = uri.substring(indexofSession,indexOfSlash);
                        sessionPath = archiveDir + File.separator + sessionPostfix;
            		}
            	}
                if (!sessionPath.endsWith(File.separator)) sessionPath += File.separator; 
                qcFolder = new File(sessionPath + "QC");
                if (!qcFolder.exists())
                    qcFolder.mkdir();
            }else {
                throw new Exception("Couldnt find the session path");
            }*/
            String sessionPath = archiveDir + File.separator + petSession.getPETSession().getLabel();
            qcFolder = new File(sessionPath + File.separator + "QC");
            if (!qcFolder.exists())
                qcFolder.mkdir();
            
            ImageResource rsc = getImage(petSession,petimageContent);
            ImageResource mrrsc = getImage(petSession,mrimageContent);
            if (!rsc.getURI().startsWith("/")) 
            	rsc.setURI(getPath(petSession,rsc.getURI()));
            if (!mrrsc.getURI().startsWith("/"))
            	mrrsc.setURI(getPath(petSession,mrrsc.getURI()));
            PlexiImageFile pf = IOHelper.getPlexiImageFileFromImageResource(rsc);
            ImagePlus petimg = PlexiFileOpener.openBaseFile(pf, radiologic);
            setMinValueToZero(petimg);
            LUTApplier lut = new LUTApplier(lkupfile);
            petimg = lut.applyLUT(petimg);
            pf = IOHelper.getPlexiImageFileFromImageResource(mrrsc);
            ImagePlus mrimg = PlexiFileOpener.openBaseFile(pf, radiologic);
            threshold(mrimg);
            String filename = qcFolder + File.separator + petSession.getPETSession().getLabel() + "_QC";
            createMontage(filename, petimg,mrimg, pf.getOrientation());
        }catch(Exception e) {
            e.printStackTrace();
            exitStatus = 1;
        }
        if (exitStatus == 0) {
            System.out.println("Created QC file in " + qcFolder);
            //addQCOutFileToXml
        }
    }
    
    private void threshold(ImagePlus img) {
        //ThresholdAdjuster tAdj = new ThresholdAdjuster(minThreshold,maxThreshold);
        //tAdj.applyThresold(img);
        //double min = img.getStatistics().min;
        //double max = img.getStatistics().max;
        //if (m)
        img.getProcessor().setMinAndMax(150, 1400);
    }
    
    private void setMinValueToZero(ImagePlus img) {
        double max = img.getProcessor().getMax();
        setMinValue(img,0,max);
    }
    
    private void setMinValue(ImagePlus img, double min, double max) {
        img.getProcessor().setMinAndMax(min,max);
    }
    
    private void createMontage(String rootfilename,  ImagePlus petimage, ImagePlus mrimage, String acquiredOrientation) {
        //Hashtable attribs = ImageUtils.getSliceIncrement(petimage, 35);
        int cols = 7; int rows = 4; 
        //int increment = ((Integer)attribs.get("increment")).intValue();
        int increment = 2;
        int startslice = 10;
        for (int i = 0; i < rows; i++) {
            int end = startslice + (cols - 1)*increment ;
            createJpeg(rootfilename + "_p" + i + "_t.jpg", petimage,cols,startslice,end,increment);
            createJpeg(rootfilename + "_m" + i + "_t.jpg", mrimage,cols,startslice,end,increment);
            startslice = end + increment;
        }
        
        PlexiImageOrientor pio=new PlexiImageOrientor(petimage,"IFH");
        ImagePlus petimage1 = pio.getImage(acquiredOrientation,"sagittal"+"F");
        pio=new PlexiImageOrientor(mrimage,"IFH");
        ImagePlus mrimage1 = pio.getImage(acquiredOrientation,"sagittal"+"F");
        //int increment = ((Integer)attribs.get("increment")).intValue();
        cols = 6; rows = 1;
        increment = 4;
        startslice = 54;
        for (int i = 0; i < rows; i++) {
            int end = startslice + (cols - 1)*increment ;
            createJpeg(rootfilename + "_ps" + i + "_t.jpg", petimage1,cols,startslice,end,increment);
            createJpeg(rootfilename + "_ms" + i + "_t.jpg", mrimage1,cols,startslice,end,increment);
            startslice = end + increment;
        }

        
    }
    
    private String getPath(PETSessionDocument petSession, String uri) {
    	String rtn = null;
		int indexofSession = uri.indexOf(petSession.getPETSession().getLabel());
		int indexOfProcessed = uri.indexOf("/PROCESSED/");
		if (indexofSession != -1) {
    		String sessionPostfix = uri.substring(indexofSession,indexOfProcessed);
            rtn = archiveDir + File.separator + sessionPostfix + uri.substring(indexOfProcessed);
		}
		return rtn;
    }
    
    private void createJpeg(String filename, ImagePlus img, int cols, int startslice, int endslice, int increment ) {
        PlexiMontageMaker mm = new PlexiMontageMaker();
        ImagePlus montage = mm.makeMontage(img,cols,1,1,startslice,endslice,increment,true,false);
        PlexiFileSaver fs =  new PlexiFileSaver();
        //fs.saveAsJpeg(img,filename+".jpg", 100);
        //StackProcessor tbproc = new StackProcessor(montage.getStack(), montage.getProcessor());
        //ImageStack tb = tbproc.resize((int)montage.getWidth()/2,(int)montage.getHeight()/2);
        //montage.setStack("",tb);    
        fs =  new PlexiFileSaver();
        fs.saveAsJpeg(montage,filename, 100);
    }
    
    private ImageResource getImage(PETSessionDocument petSession, String imageContent) throws Exception {
        PetSessionData petSessionData = petSession.getPETSession();
        ImageResource rtn = null;
        if (petSessionData.isSetReconstructions()) {
            for (int i = 0; i < petSessionData.getReconstructions().getReconstructedImageArray(0).getOut().sizeOfFileArray(); i++) {
                AbstractResource outFile = petSessionData.getReconstructions().getReconstructedImageArray(0).getOut().getFileArray(i);
                ImageResource rsc = (ImageResource)outFile.changeType(ImageResource.type);
                if (rsc.getContent().equalsIgnoreCase(imageContent)) {
                    rtn = rsc;
                    break;
                }
            }
        }
        return rtn;
    }
    
    
    private void handleException(String methodName, String msg, Exception e) {
        System.out.println(" PIBQCImageCreator." + methodName +" encountered problem " + (e == null?"":e.getMessage()) + " \n MSG: " + msg);
        System.exit(exitStatus);
    }
    
    /**
     * @return Returns the exitStatus.
     */
    public int getExitStatus() {
        return exitStatus;
    }
    
    
    private void handleError() {
        System.out.println("Insufficient arguments");
        printUsage();
        System.exit(1);
        
    }
    
    protected void printUsage() {
        System.out.println("PIBQCImageCreator OPTIONS:"); 
        System.out.println("\t\t-session <pet-session label>");
        System.out.println("\t\t-xnatId <pet-session xnat id>");
        System.out.println("\t\t-host <xnat host>");
        System.out.println("\t\t-u <xnat username>");
        System.out.println("\t\t-pwd <xnat password>");
        System.out.println("\t\t-lut <lkup file>");
        System.out.println("\t\t-pc <PET Image Content Code>");
        System.out.println("\t\t-mc <MR Image Content Code>");
        System.out.println("\t\t-a <Root archive path>");
        System.out.println("\t\t-mtmin <Min threshold for MR Image>");
        System.out.println("\t\t-mtmax <Max threshold for MR Image>");
    }
    
    public static void main(String args[]) {
        // System.setProperty("java.awt.headless","true");
         PIBQCImageCreator qc = new PIBQCImageCreator(args);
         qc.createQCRawImages();
         System.out.println("All done");
         System.exit(qc.getExitStatus());
     }
}

