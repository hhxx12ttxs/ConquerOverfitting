package org.nrg.hcp.importer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xdat.om.HcprestrictedHcprestrictedids;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import au.com.bytecode.opencsv.CSVReader;


/**
 * Generates HCP Phase II subject records from STRATA subject export file  
 * @author Mike Hodge <hodgem@mir.wustl.edu>
 *
 */
/*
 * 2012/07/06 - modification to eliminate "update" parameter.  Per meeting with Rade, we will always
 *              update everything except zygosity.  Only zygosity will be user-modifiable.
 */
public class HCPStrataImporter extends ImporterHandlerA implements Callable<List<String>> {

	static Logger logger = Logger.getLogger(HCPStrataImporter.class);

	private final FileWriterWrapperI fw;
	private final XDATUser user;
	final Map<String,Object> params;
   	private XnatProjectdata proj;
   	//private boolean updateExisting = false;
   	private boolean verboseOutput = false;
   	private ArrayList<String> csvPublicIds = new ArrayList<String>();
   	private ArrayList<String> dbSubjectLbls = new ArrayList<String>();
   	private ArrayList<String> newSubjectLbls = new ArrayList<String>();
   	private ArrayList<String> returnList = new ArrayList<String>();
   	
   	// Value to append to subjectID in EXP_LBL for Restricted Demographics Assessor
   	private static final String rDemogAppend = "_RstID";
   	
   	private static final int publicIdField = 0;
   	private static final int ageField = 1;
   	private static final int genderField = 2;
   	private static final int individIdField = 3;
   	private static final int momIdField = 4;
   	private static final int dadIdField = 5;
   	private static final int twinStatusField = 6;
   	private static final int unusedField = 7;
   	private static final int strataGroupIdField = 8;
   	private static final int strataRelationshipIdField = 9;
   	private static final int flagToExportField = 10;
   	
   	private static final String publicIdValid = "..\\d{4}";
   	private static final String ageValid = "[23]\\d";
   	private static final String genderValid = "[01]";
   	private static final String individIdValid = "\\d{5}";
   	private static final String momIdValid = "\\d{5}";
   	private static final String dadIdValid = "\\d{5}";
   	private static final String twinStatusValid = "[012]";
   	private static final String strataGroupIdValid = "\\d{5}";
   	private static final String strataRelationshipIdValid = "\\d{5}";
   	private static final String flagToExportValid = "[1]";
   	
   	private static final String csvGenderMale = "M";
   	private static final String csvGenderFemale = "F";
   	
   	private static final String dbGenderMale = "male";
   	private static final String dbGenderFemale = "female";
   	private static final String dbGenderOther = "other";
   	private static final String dbGenderUnknown = "unknown";
   	
	/**
	 * 
	 * @param listenerControl
	 * @param u
	 * @param session
	 * @param overwrite:   'append' means overwrite, but preserve un-modified content (don't delete anything)
	 *                      'delete' means delete the pre-existing content.
	 * @param additionalValues: should include project (subject and experiment are expected to be found in the archive)
	 */
	public HCPStrataImporter(Object listenerControl, XDATUser u, FileWriterWrapperI fw, Map<String, Object> params) {
		super(listenerControl, u, fw, params);
		this.user=u;
		this.fw=fw;
		this.params=params;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<String> call() throws ClientException, ServerException {
		verifyProject();
		//if (params.get("update")!=null && params.get("update").toString().equalsIgnoreCase("true")) {
		//	updateExisting = true;
		//}
		if (params.get("verbose")!=null && params.get("verbose").toString().equalsIgnoreCase("true")) {
			verboseOutput = true;
		}
		try {
			final List<String> returnList = saveAndProcessCsvFile();
			this.completed("Successfully imported STRATA CSV");
			return returnList;
		} catch (ClientException e) {
			logger.error("",e);
			this.failed(e.getMessage());
			throw e;
		} catch (ServerException e) {
			logger.error("",e);
			this.failed(e.getMessage());
			throw e;
		} catch (Throwable e) {
			logger.error("",e);
			throw new ServerException(e.getMessage(),new Exception());
		}
	}

	private void verifyProject() throws ClientException {
		if (params.get("project") == null) {
			clientFailed("ERROR:  project parameter must be supplied for import");
		}
		String projID=params.get("project").toString();
		proj=AutoXnatProjectdata.getXnatProjectdatasById(projID, user, false);
		if (proj == null) {
			clientFailed("ERROR:  Project specified is invalid or user does not have access to project");
		}
	}
	

	@SuppressWarnings("deprecation")
	private void clientFailed(String fmsg) throws ClientException {
		this.failed(fmsg);
		throw new ClientException(fmsg,new Exception());
	}

	private List<String> saveAndProcessCsvFile() throws ClientException,ServerException {
		
		String cachePath = ArcSpecManager.GetInstance().getGlobalCachePath();
		Date d = Calendar.getInstance().getTime();
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("yyyyMMdd_HHmmss");
		String uploadID = formatter.format(d);
		
		// Save input file to cache space
		cachePath+="user_uploads/"+user.getXdatUserId() + "/" + uploadID + "/";
		final File cacheLoc = new File(cachePath);
		cacheLoc.mkdirs();
		
		final String fileName = fw.getName();
		//String writeout = null;
		File cacheFile = new File(cacheLoc,fileName);
		try {
			//StringWriter writer = new StringWriter();
			FileWriter writer = new FileWriter(cacheFile);
			IOUtils.copy(fw.getInputStream(), writer);
			writer.close();
		} catch (IOException e) {
			throw new ServerException("Could not save CSV file",e);
		}
		
		// Iterate over CSV file, optionally updating records and saving new ones
		parseAndProcessCsvFile(cacheFile);
		
		return returnList;
		
	}
	
	private void parseAndProcessCsvFile(File cacheFile) throws ClientException, ServerException {
		//List<Map<String,String>> csvRep = null;
		List<String[]> csvRep;
		
		try {
			csvRep = new CSVReader(new FileReader(cacheFile)).readAll();
		} catch (IOException e) {
			throw new ServerException("Could not parse input CSV file",e);
		}
		if (csvRep==null || csvRep.size()<1) {
			throw new ClientException("Uploaded file contains no data");
		}
		
		// Obtain list of current subjects
		populateSubjectLblList();
	
		// Loop over rows, creating items
		int rowCount = 0;
		
		for (String[] row : csvRep) {
			rowCount++;
			// Pull PublicID
			String publicID = null;
			if (checkColumn(row,publicIdField)) {
				publicID = row[publicIdField];
				// Check row against list of current subjects
				if (csvPublicIds.contains(publicID)) {
					throw new ClientException("ERROR:  CSV file contains multiple records for " + publicID + ".");
				} else {
					csvPublicIds.add(publicID);
				}
			} 
			// MODIFICATION HERE 2012/07/06.  Now always doing update, but not updating zygosity information from CSV
			// file.  We'll keep information from the prior record	
			// Process Data
			createOrUpdateSubject(row,publicID,rowCount);
			/*
			//if (dbSubjectLbls.contains(publicID) && !updateExisting) {
			//	if (verboseOutput) {
			//		returnList.add("Subject " + publicID + " already exists in database and update=false.  Skipping...");
			//	}
			//} else {
			//	createOrUpdateSubject(row,publicID,rowCount);
			//}
			*/
			
		}
		
	}

	private boolean checkColumn(String[] row, int col) throws ClientException {
		if (row.length<col) {
				throw new ClientException("Invalid CSV file format.  One or more rows is missing columns");
		}
		if (row[col]!=null) {
			return true;
		}
		return false;
	}

	private void populateSubjectLblList() {
        CriteriaCollection cc;
        cc=new CriteriaCollection("OR");
        cc.addClause(XnatSubjectdata.SCHEMA_ELEMENT_NAME + "/project", proj.getId());
        cc.addClause(XnatSubjectdata.SCHEMA_ELEMENT_NAME + "/sharing/share/project", proj.getId());
		ArrayList<XnatSubjectdata> subjectRecords=XnatSubjectdata.getXnatSubjectdatasByField(cc, user, false);
		for (XnatSubjectdata record : subjectRecords) {
			String thisId = record.getLabel();
			if (dbSubjectLbls.contains(thisId)) {
				// This shouldn't happen
				returnList.add("WARNING:  Subject data contains multiple records for " + thisId);
			} else {
				dbSubjectLbls.add(thisId);
			}
		}
		subjectRecords = null;
	}

	private void createOrUpdateSubject(String[] row, String subjID, int rowCount) throws ServerException, ClientException {
		
		//////////////////////////////////////////////////////////
		// Create and save Subject and Restricted Data assessor //
		//////////////////////////////////////////////////////////
		try {
			
			XFTItem demoI=XFTItem.NewItem("xnat:demographicData",user);
			
			XnatSubjectdata currSubject = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(), subjID, user, false);
			XnatSubjectdata subject = (currSubject!=null) ? currSubject : new XnatSubjectdata((UserI)user);
			
			// Set subject record label/ID values
			subject.setProject(proj.getId());
			subject.setLabel(subjID);
			subject.setDemographics(demoI);
			if (currSubject!=null) {
				subject.setId(currSubject.getId());
			} else {
				subject.setId(XnatSubjectdata.CreateNewID());
			}
			
			// Set restricted demographics label/ID values 
			XnatExperimentdata currRestrict = HcprestrictedHcprestrictedids.GetExptByProjectIdentifier(proj.getId(), subject.getLabel() + rDemogAppend, user, false);
			if (!(currRestrict==null || currRestrict instanceof HcprestrictedHcprestrictedids)) {
				throw new ServerException("Could not save Resctricted Demographics record - Experiment label " + currRestrict.getLabel() +
						" exists but is not a restricted demographics record type.  (SUBJECT=" + subjID + ")");
			}
			HcprestrictedHcprestrictedids restricted = (currRestrict!=null) ? (HcprestrictedHcprestrictedids) currRestrict : new HcprestrictedHcprestrictedids((UserI)user);
			restricted.setProject(proj.getId());
			restricted.setSubjectId(subject.getId());
			restricted.setLabel(subject.getLabel() + rDemogAppend);
			if (currRestrict!=null) {
				restricted.setId(currRestrict.getId());
				restricted.setDate(currRestrict.getDate());
			} else {
				restricted.setId(XnatExperimentdata.CreateNewID());
				restricted.setDate(new Date());
			}
			
			// Don't continue if FLAG_TO_EXPORT is not valid (currently equals 1) - These may be from earlier study phases
			if (checkColumn(row,flagToExportField)) {
				String flagToExport = row[flagToExportField];
				if (!flagToExport.matches(flagToExportValid)) {
					returnList.add("WARNING:  FLAG_TO_EXPORT value invalid (possibly earlier phase record) - (VALUE=" + row[flagToExportField] + ",SUBJECT=" + subjID + ")");
					return;
				}
			} 
			
			assignRowValuesToObjects(row,subject,restricted,demoI,subjID);
			
			// Save subject record -- If record already exists, use existing ID
			try {
				if (currSubject==null) {
					
					if (verboseOutput) 
						returnList.add("Saving new Subject record for " + subjID);
					final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, subject.getItem(),
							EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.STORE_XML, EventUtils.CREATE_VIA_WEB_SERVICE, null, null));
					final EventMetaI ci = wrk.buildEvent();
					if (SaveItemHelper.authorizedSave(subject,user,false,true,ci)) {
						PersistentWorkflowUtils.complete(wrk, ci);
					} else {
						PersistentWorkflowUtils.fail(wrk,ci);
						if (verboseOutput) 
							throw new ServerException("Could not save Subject record - (SUBJECT=" + subjID + ")");
					}
					
				// MODIFICATION HERE 2012/07/06.  Now always doing update, but not updating zygosity information from CSV
				// file.  We'll keep information from the prior record	
				} else {
					
					if (verboseOutput)
						returnList.add("Updating Subject record for " + subjID);
					final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, subject.getItem(),
							EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.STORE_XML, EventUtils.MODIFY_VIA_WEB_SERVICE, null, null));
					final EventMetaI ci = wrk.buildEvent();
					if (SaveItemHelper.authorizedSave(subject,user,false,true,ci)) {
						PersistentWorkflowUtils.complete(wrk, ci);
					} else {
						PersistentWorkflowUtils.fail(wrk,ci);
						if (verboseOutput) 
							returnList.add("No change from pre-existing Subject record - modification not required - " + subjID);
					}
					
				} 
				/*	
				} else if (updateExisting && currSubject!=null) {
					if (verboseOutput)
						returnList.add("Updating Subject record for " + subjID);
					if (!SaveItemHelper.authorizedSave(subject,user,false,true)) {
						if (verboseOutput) 
							returnList.add("No change from pre-existing Subject record - modification not required - " + subjID);
					}
				} else if (verboseOutput && !updateExisting && currSubject!=null) {
					// This condition should never be met in practice - should already be skipped
					returnList.add("Subject " + subjID + " already exists in database and update=false.  Skipping...");
				}
				*/	
			} catch (Exception e) {
				throw new ServerException("Could not save Subject record - (SUBJECT=" + subjID + ")",e);
			}
			
			// Save Restricted Demographics record -- If record already exists, use existing ID (and date)
			try {
				if (currRestrict==null) {
					if (verboseOutput) 
						returnList.add("Saving new Restricted Demographics record for " + subjID);
					
					final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, restricted.getItem(),
							EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.STORE_XML, EventUtils.MODIFY_VIA_WEB_SERVICE, null, null));
					final EventMetaI ci = wrk.buildEvent();
					if (SaveItemHelper.authorizedSave(restricted,user,false,true,ci)) {
						PersistentWorkflowUtils.complete(wrk, ci);
					} else {
						PersistentWorkflowUtils.fail(wrk,ci);
						throw new ServerException("Could not save Restricted Demographics record - (SUBJECT=" + subjID + ")");
					}
					
				// MODIFICATION HERE 2012/07/06.  Now always doing update, but not updating zygosity information from CSV
				// file.  We'll keep information from the prior record	
				} else {
					if (verboseOutput) 
						returnList.add("Updating Restricted Demographics record for " + subjID);
					final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, restricted.getItem(),
							EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.STORE_XML, EventUtils.MODIFY_VIA_WEB_SERVICE, null, null));
					final EventMetaI ci = wrk.buildEvent();
					if (SaveItemHelper.authorizedSave(restricted,user,false,true,ci)) {
						PersistentWorkflowUtils.complete(wrk, ci);
					} else {
						PersistentWorkflowUtils.fail(wrk,ci);
						if (verboseOutput) 
							returnList.add("No change from pre-existing Restricted Demographics record - modification not required - " + subjID);
					}
				} 
				/*
				} else  if (updateExisting && (currRestrict!=null)) {
					if (verboseOutput) 
						returnList.add("Updating Restricted Demographics record for " + subjID);
					if (!SaveItemHelper.authorizedSave(restricted,user,false,true)) {
						if (verboseOutput) 
							returnList.add("No change from pre-existing Restricted Demographics record - modification not required - " + subjID);
					}
				} else if (verboseOutput && !updateExisting && currRestrict!=null) {
					// This condition should never be met in practice - should already be skipped
					returnList.add("Subject " + subjID + " already exists in Restricted Demographics database and update=false.  Skipping...");
				}
				*/
			} catch (Exception e) {
				throw new ServerException("Could not save Resctricted Demographics record - (SUBJECT=" + subjID + ")",e);
			} 
			
			newSubjectLbls.add(subjID);
		
		} catch (Exception e) {
			throw new ServerException("Could not save Subject and Restricted Demographics record - (SUBJECT=" + subjID + ")",e);
		}
		
			
	}

	private void assignRowValuesToObjects(String[] row, XnatSubjectdata subject,
			HcprestrictedHcprestrictedids restricted, XFTItem demoI, String subjID) throws XFTInitException, ElementNotFoundException, FieldNotFoundException, InvalidValueException, ClientException {
			
			// Age (in restricted assessor and rounded in subject assessor) 
			if (checkColumn(row,ageField)) {
				try {	
					int ageV = Integer.parseInt(row[ageField]);
					restricted.setAgeinyrs(ageV);
					// Round age values at subject level
					if (demoI!=null) {
						//demoI.setProperty("age", Integer.toString(((int)(ageV/ageRound))*ageRound));
						int ageCat=-1;
						if (ageV<22) ageCat=1;
						else if (ageV<=25) ageCat=22;
						else if (ageV<=30) ageCat=26;
						else if (ageV<=35) ageCat=31;
						else if (ageV>35) ageCat=99;
						demoI.setProperty("age", Integer.toString(ageCat));
					}
					if (!row[ageField].matches(ageValid)) {
						returnList.add("WARNING:  Import file contains missing/invalid value for Age - (VALUE=" + row[ageField] + ",SUBJECT=" + subjID + ")");
					}
				} catch (NumberFormatException e) {
					returnList.add("WARNING:  Import file contains missing invalid integer value for age - (VALUE=" + row[ageField] + ",SUBJECT=" + subjID + ")");
				}
			}
			
			// Gender (subject assessor)
			if (checkColumn(row,genderField)) {
				try {	
					String genderV = row[genderField];
					if (genderV.equalsIgnoreCase(csvGenderMale)) {
							demoI.setProperty("gender", dbGenderMale);
					} else  if (genderV.equalsIgnoreCase(csvGenderFemale)) {
							demoI.setProperty("gender", dbGenderFemale);
					} else {
							returnList.add("WARNING:  Import file contains missing/invalid integer value for gender - (VALUE=" + row[genderField] + ",SUBJECT=" + subjID + ")");
							demoI.setProperty("gender", dbGenderUnknown);
					}
				} catch (NumberFormatException e) {
					returnList.add("WARNING:  Import file contains missing/invalid integer value for gender - (VALUE=" + row[genderField] + ",SUBJECT=" + subjID + ")");
				}
			}
			
			// IndividualID (restricted assessor)
			if (checkColumn(row,individIdField)) {
				String individID = row[individIdField];
				restricted.setIndividualid(individID);
				if (!individID.matches(individIdValid)) {
					returnList.add("WARNING:  Import file contains missing/invalid value for IndividualID - (VALUE=" + individID + ",SUBJECT=" + subjID + ")");
				}
			} else {
				returnList.add("WARNING:  Import file contains missing value for IndividualID - (SUBJECT=" + subjID + ")");
			}
			
			// MotherID (restricted assessor)
			if (checkColumn(row,momIdField)) {
				String momID = row[momIdField];
				restricted.setMotherid(momID);
				if (!momID.matches(momIdValid)) {
					returnList.add("WARNING:  Import file contains missing/invalid value for MotherID - (VALUE=" + momID + ",SUBJECT=" + subjID + ")");
				}
			} else {
				returnList.add("WARNING:  Import file contains missing value for MotherID - (SUBJECT=" + subjID + ")");
			}
			
			// FatherID (restricted assessor)
			if (checkColumn(row,dadIdField)) {
				String dadID = row[dadIdField];
				restricted.setFatherid(dadID);
				if (!dadID.matches(dadIdValid)) {
					returnList.add("WARNING:  Import file contains missing/invalid value for FatherID - (VALUE=" + dadID + ",SUBJECT=" + subjID + ")");
				}
			} else {
				returnList.add("WARNING:  Import file contains missing value for FatherID - (SUBJECT=" + subjID + ")");
			}
			
			// MODIFICATION HERE 2012/07/06.  Now always doing update, but not updating zygosity information from CSV
			// file.  We'll keep information from the prior record.	 Set zygosity only if it's missing in current record.
			// Zygosity (restricted assessor) - currently based on twinStatus
			if (checkColumn(row,twinStatusField)) {
				int twinStatus = Integer.parseInt(row[twinStatusField]);
				switch(twinStatus) {
					case 0: 
						// set zygosity to 0=Not a twin (2012/07/06 - only setting if not already set)
						if (restricted.getZygosity()==null) {
							restricted.setZygosity(0);
						}
						break;
					case 1: 
						// set zygosity to 9=Don't know (2012/07/06 - only setting if not already set)
						if (restricted.getZygosity()==null) {
							restricted.setZygosity(9);
						}
						break;
					default:
						returnList.add("WARNING:  Import file contains missing/invalid value for TwinStatus - (VALUE=" + twinStatus + ",SUBJECT=" + subjID + ")");
						break;
				}
			} else {
				returnList.add("WARNING:  Import file contains missing value for TwinStatus - (SUBJECT=" + subjID + ")");
			}
			
	}

}


