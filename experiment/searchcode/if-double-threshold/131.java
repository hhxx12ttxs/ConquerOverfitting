/*
 * org.nrg.plexiViewer.lite.UserInterfaceContents
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 10/4/13 10:50 AM
 */


package org.nrg.plexiViewer.lite;

/**
 * @author Mohana
 *
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.nrg.plexiViewer.lite.ui.GenericIdentifier;
import org.nrg.plexiViewer.lite.utils.HTTPDetails;
import org.nrg.plexiViewer.lite.utils.LiteFileUtils;
import org.nrg.plexiViewer.utils.Transform.PlexiImageOrientor;
import org.nrg.xdat.bean.XnatAbstractresourceBean;
import org.nrg.xdat.bean.XnatDicomseriesBean;
import org.nrg.xdat.bean.XnatImageassessordataBean;
import org.nrg.xdat.bean.XnatImageresourceBean;
import org.nrg.xdat.bean.XnatImageresourceseriesBean;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatMrscandataBean;
import org.nrg.xdat.bean.XnatReconstructedimagedataBean;
import org.nrg.xdat.bean.XnatResourceBean;
import org.nrg.xdat.bean.XnatResourcecatalogBean;


public class UserInterfaceContents extends java.lang.Object implements java.io.Serializable{
	String sessionId;
	XnatImagesessiondataBean imageSession;
	private static final String RAW_TEXT = "RAW";
	private static final String RECONSTRUCTIONS_TEXT = "RECONSTRUCTIONS";
	private static final String ASSESSORS_TEXT = "ASSESSORS";
	private static final String RESOURCES_TEXT = "RESOURCES";
    private static final String defaultImageViewerClassName ="org.nrg.plexiViewer.lite.viewer.MR.MRImage" ;
    private static final String defaultLoResConverter ="org.nrg.plexiViewer.converter.DefaultConverter" ;
    private static final String defaultThumbnailConverter ="org.nrg.plexiViewer.converter.CNLThumbnailConverter" ;
	private Hashtable orientations;
	
	public UserInterfaceContents(String sessionId) {
		this.sessionId=sessionId;
		orientations = new Hashtable();
		populateSelf(sessionId);
	}

    
	public UserInterfaceContents(String sessionId, XnatImagesessiondataBean imageSession) {
		this.sessionId=sessionId;
		this.imageSession = imageSession;
		orientations = new Hashtable();
	}

	public String getViewerClassName() {
		return defaultImageViewerClassName;
	}

	public String getConverterClassName() {
		return defaultLoResConverter;
	}

	private String sessionLabel;
	
	
	public void populateSelf(String sessionId) {
		String suffix = HTTPDetails.getSuffix("PopulateServlet");
		URL populateServlet = null;
		URLConnection servletConnection;
			try {
				suffix+="?sessionId=" +sessionId;
			
				populateServlet = HTTPDetails.getURL(HTTPDetails.getHost(),suffix);
				servletConnection = HTTPDetails.openConnection(populateServlet);
				//Don't use a cached version of URL connection.
				servletConnection.setUseCaches (false);
				//servletConnection.setDefaultUseCaches (false);
				//servletConnection.setDoInput(true); 
				InputStream is = servletConnection.getInputStream();
				ObjectInputStream inputStreamFromServlet =  new ObjectInputStream(is);
		
				imageSession = (XnatImagesessiondataBean) inputStreamFromServlet.readObject();
				sessionLabel = imageSession.getLabel();
				inputStreamFromServlet.close();
				is.close();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}catch (ClassNotFoundException e) {
				System.out.println("UserInterfaceContents threw this exception");
				System.out.println(e);
			}		
	}
	
	


	
	
	/**
	 * @return
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param string
	 */
	public void setSessionId(String string) {
		sessionId = string;
	}


	/**
	 * @return the sessionLabel
	 */
	public String getSessionLabel() {
		return sessionLabel;
	}

	/**
	 * @param sessionLabel the sessionLabel to set
	 */
	public void setSessionLabel(String sessionLabel) {
		this.sessionLabel = sessionLabel;
	}

	public Vector getValuesForData() {
		Vector rtn = new Vector();
		if (imageSession.getScans_scan() != null && imageSession.getScans_scan().size() > 0) {
			rtn.add(RAW_TEXT);
		}
		if (imageSession.getReconstructions_reconstructedimage() != null && imageSession.getReconstructions_reconstructedimage().size() > 0) {
			rtn.add(RECONSTRUCTIONS_TEXT);
		}
		if (imageSession.getAssessors_assessor() != null && imageSession.getAssessors_assessor().size() > 0) {
			rtn.add(ASSESSORS_TEXT);
		}
		/*if (imageSession.getResources_resource() != null && imageSession.getResources_resource().size() > 0) {
			rtn.add(RESOURCES_TEXT);
		}*/
		return rtn;
	}
	
	public String getProject() {
		return imageSession.getProject();
	}
	
	public Vector getFileList(String dataStr, String typeStr) {
		Vector rtn = new Vector();
		if (RAW_TEXT.equals(dataStr)) {
			List scans = imageSession.getScans_scan();
			if (scans.size() > 0) {
				for (int i = 0; i<scans.size(); i++){
					XnatImagescandataBean scan = (XnatImagescandataBean)scans.get(i);
					if (scan.getType().equals(typeStr)) {
						List files = scan.getFile();
						if (files != null) {
							for (int j=0;j<files.size(); j++) {	
							  XnatAbstractresourceBean 	file = (XnatAbstractresourceBean)files.get(j);
							  if (!isImage(file)) continue;
							  String display = getStringToDisplay(file);
							  if (display.equalsIgnoreCase("SNAPSHOTS")) continue;
							  else if (file.getLabel() != null && file.getLabel().equalsIgnoreCase("SECONDARY")) continue;
							  Vector ori = getOrientations(file, scan);
							  orientations.put(file, ori);
							  GenericIdentifier gid = new GenericIdentifier(scan.getId()+" - "+ display, file, true);
							  rtn.add(gid);
							}
						}
					}
				}
			}
		}
		else if (RECONSTRUCTIONS_TEXT.equals(dataStr)) {
			List recons = imageSession.getReconstructions_reconstructedimage();
			if (recons.size() > 0) {
				for (int i = 0; i<recons.size(); i++){
					XnatReconstructedimagedataBean recon = (XnatReconstructedimagedataBean)recons.get(i);
					if (recon.getType().equals(typeStr)) {
						List files = recon.getOut_file();
						if (files != null && files.size()>0) {
							Hashtable uniqueContent = populateUniques(files);
							for (int j=0;j<files.size(); j++) {	
							  XnatAbstractresourceBean 	file = (XnatAbstractresourceBean)files.get(j);
							  if (!isImage(file)) continue;
							  String display = getStringToDisplay(file);
							  if (((Hashtable)uniqueContent.get(display)).size()>1) {
								  //Adjust the display with file name
								  display += " - " + LiteFileUtils.getFileName(file);
							  }
							  Vector ori = getOrientation(file);
							  orientations.put(file, ori);
							  GenericIdentifier gid = new GenericIdentifier(display, file, true);
							  rtn.add(gid);
							}
						}
					}
				}
			}
		}
		else if (ASSESSORS_TEXT.equals(dataStr)) {
			List assessors = imageSession.getAssessors_assessor();
			if (assessors.size() > 0) {
				for (int i = 0; i<assessors.size(); i++){
					XnatImageassessordataBean assessor = (XnatImageassessordataBean)assessors.get(i);
					if (assessor.getLabel().equals(typeStr)) {
						List files = assessor.getOut_file();
						if (files != null && files.size()>0) {
							Hashtable uniqueContent = populateUniques(files);
							for (int j=0;j<files.size(); j++) {	
							  XnatAbstractresourceBean 	file = (XnatAbstractresourceBean)files.get(j);
							  if (!isImage(file)) continue;
							  String display = getStringToDisplay(file); 
							  if (((Hashtable)uniqueContent.get(display)).size()>1) {
								  //Adjust the display with file name
								  display += " - " + LiteFileUtils.getFileName(file);
							  }
							  Vector ori = getOrientation(file);
							  orientations.put(file, ori);
							  GenericIdentifier gid = new GenericIdentifier(display, file, true);
							  rtn.add(gid);
							}
						}
					}
				}
			}
		}

		return rtn;
	}
	
	private Vector getOrientations(XnatAbstractresourceBean abs, XnatImagescandataBean scan) {
		Vector ori = new Vector();
		if (scan instanceof XnatMrscandataBean) {
			XnatMrscandataBean mrScan =  (XnatMrscandataBean)scan;
			if (isScanIsotropic(mrScan.getParameters_voxelres_x(),mrScan.getParameters_voxelres_y(),mrScan.getParameters_voxelres_z())) {
        		ori = getAcquiredOrientation();
        	}else {
        		ori = getAcquiredOrientation(mrScan.getParameters_orientation());
        	}
		}else {
			ori = getAcquiredOrientation(null);
		}
		return ori;
	}
	
	private Hashtable populateUniques(List files) {
		Hashtable rtn = new Hashtable();
		for (int j=0;j<files.size(); j++) {	
			XnatAbstractresourceBean file = (XnatAbstractresourceBean)files.get(j);
			String display = getStringToDisplay(file); 
			if (rtn.containsKey(display)) {
				Hashtable indices = (Hashtable)rtn.get(display);
				indices.put(new Integer(j),"1");
				rtn.put(display, indices);
			}else {
				Hashtable indices = new Hashtable();
				indices.put(new Integer(j),"1");
				rtn.put(display, indices);
			}
		}
		return rtn;
	}
	
	
	
	private boolean isImage(XnatAbstractresourceBean file) {
		boolean rtn = false;
		if (file instanceof XnatImageresourceBean || file instanceof XnatImageresourceseriesBean || file instanceof XnatDicomseriesBean || file instanceof XnatResourcecatalogBean )
			rtn =true;
		return rtn;
	}
	
	private String getStringToDisplay(XnatAbstractresourceBean file) {
		String rtn = file.getLabel();
		if (rtn != null) return rtn;
		if (file instanceof XnatResourceBean) {
			XnatResourceBean resource = (XnatResourceBean)file;
			rtn = resource.getContent();
		}else if (file instanceof XnatImageresourceBean) {
			XnatImageresourceBean resource = (XnatImageresourceBean)file;
			rtn = resource.getContent();
		}else if (file instanceof XnatImageresourceseriesBean) {
			XnatImageresourceseriesBean resource = (XnatImageresourceseriesBean)file;
			rtn = resource.getContent();
		}else if (file instanceof XnatDicomseriesBean) {
			XnatDicomseriesBean resource = (XnatDicomseriesBean)file;
			rtn = resource.getContent();
		}else if (file instanceof XnatResourcecatalogBean) {
        	XnatResourcecatalogBean resource = (XnatResourcecatalogBean)file;
			rtn = resource.getContent();
		}
		return rtn;
	}
	

	public Vector getOrientations(XnatAbstractresourceBean file) {
		return (Vector)orientations.get(file);
	}
	
	private Vector getOrientation(XnatAbstractresourceBean file) {
		Vector ori = new Vector();
		if (file == null) return ori;
	       if (file instanceof XnatImageresourceBean) {
	        	XnatImageresourceBean resource = (XnatImageresourceBean)file;
	        	Double x = resource.getVoxelres_x();
	        	Double y = resource.getVoxelres_y();
	        	Double z = resource.getVoxelres_z();
	        	if (isScanIsotropic(x,y,z)) {
	        		ori = getAcquiredOrientation();
	        	}else {
	        		ori = getAcquiredOrientation(resource.getOrientation());
	        	}
	        }else if (file instanceof XnatImageresourceseriesBean) {
	        	XnatImageresourceseriesBean resource = (XnatImageresourceseriesBean)file;
	        	Double x = resource.getVoxelres_x();
	        	Double y = resource.getVoxelres_y();
	        	Double z = resource.getVoxelres_z();
	        	if (isScanIsotropic(x,y,z)) {
	        		ori = getAcquiredOrientation();
	        	}else {
	        		ori = getAcquiredOrientation(resource.getOrientation());
	        	}
	        }else if (file instanceof XnatDicomseriesBean) {
	        	XnatDicomseriesBean resource = (XnatDicomseriesBean)file;
	        	Double x = resource.getVoxelres_x();
	        	Double y = resource.getVoxelres_y();
	        	Double z = resource.getVoxelres_z();
	        	if (isScanIsotropic(x,y,z)) {
	        		ori = getAcquiredOrientation();
	        	}else {
	        		ori = getAcquiredOrientation(resource.getOrientation());
	        	}
	        }else if (file instanceof XnatResourcecatalogBean) {
	        	//XnatResourcecatalogBean resource = (XnatResourcecatalogBean)file;
        		ori = getAcquiredOrientation(null);
	        }
	       
	    return ori;    
	}
	
	  private boolean isScanIsotropic(Double d_voxel_x, Double d_voxel_y, Double d_voxel_z) {
	        boolean rtn = false;
	        if (d_voxel_x == null || d_voxel_y == null || d_voxel_z == null) return rtn;
	        double threshold = 0.5;
	        double voxel_x = d_voxel_x.doubleValue();
	        double voxel_y=d_voxel_y.doubleValue();
	        double voxel_z = d_voxel_z.doubleValue();
	        if (Math.abs(voxel_x-voxel_y) < threshold && Math.abs(voxel_x - voxel_z)<threshold) {
	            rtn = true;
	        }else if (Math.abs(voxel_x-voxel_y) < threshold && Math.abs(voxel_y - voxel_z)<threshold) {
	            rtn = true;
	        }else if (Math.abs(voxel_x-voxel_z) < threshold && Math.abs(voxel_z - voxel_y)<threshold) {
	            rtn = true;
	        }
	        return rtn;
	    }
	
	    private Vector getAcquiredOrientation(String ori) {
	        String tra = "Transverse     ";
	        String sag = "Sagittal       ";
	        String cor = "Coronal        ";
	        String acquired = PlexiImageOrientor.AS_ACQUIRED_TXT;
	        if (ori == null) {
	            Vector orientation = new Vector();
	            orientation.add(acquired);
	            return orientation;
	        }else {
	            Vector orientation = new Vector();
	            if (ori.toUpperCase().startsWith("TRA"))
	                orientation.add(tra);
	            else if (ori.toUpperCase().startsWith("SAG")) 
	                orientation.add(sag);
	            else if (ori.toUpperCase().startsWith("COR")) 
	                orientation.add(cor);
	            else 
	            	orientation.add(acquired);
	            return orientation;
	        }
	    }
	    
	    private Vector getAcquiredOrientation() {
	        String tra = "Transverse     ";
	        String sag = "Sagittal       ";
	        String cor = "Coronal        ";
	        Vector orientation = new Vector();
	        orientation.add(tra);
	        orientation.add(sag);
	        orientation.add(cor);
	        return orientation;
	    }

	
	public Vector getTypeValues(String data) {
		Vector rtn = new Vector();
		if (data!=null ) {
			if (data.equals(RAW_TEXT)) {
				List scans = imageSession.getScans_scan();
				Hashtable uniqueScanTypes = new Hashtable();
				if (scans.size() > 0) {
					for (int i = 0; i<scans.size(); i++){
						XnatImagescandataBean scan = (XnatImagescandataBean)scans.get(i);
						if (!uniqueScanTypes.containsKey(scan.getType())) {
							uniqueScanTypes.put(scan.getType(),"1");
							rtn.add(scan.getType());
						}
					}
				}
			}else if (data.equals(RECONSTRUCTIONS_TEXT)) {
				List recons = imageSession.getReconstructions_reconstructedimage();
				Hashtable uniqueReconTypes = new Hashtable();
				if (recons.size() > 0) {
					for (int i = 0; i<recons.size(); i++){
						XnatReconstructedimagedataBean recon = (XnatReconstructedimagedataBean)recons.get(i);
						if (!uniqueReconTypes.containsKey(recon.getType())) {
							uniqueReconTypes.put(recon.getType(),"1");
							rtn.add(recon.getType());
						}
					}
				}
			}else if (data.equals(ASSESSORS_TEXT)) {
				List assessors = imageSession.getAssessors_assessor();
				Hashtable uniqueAssessorTypes = new Hashtable();
				if (assessors.size() > 0) {
					for (int i = 0; i<assessors.size(); i++){
						XnatImageassessordataBean assessor = (XnatImageassessordataBean)assessors.get(i);
						if (!uniqueAssessorTypes.containsKey(assessor.getLabel())) {
							uniqueAssessorTypes.put(assessor.getId(),"1");
							rtn.add(assessor.getLabel());
						}
					}
				}
			}
			
			/*else if (data.equals(RESOURCES_TEXT)) {
				
			}*/
		}
		return rtn;
	}
}

