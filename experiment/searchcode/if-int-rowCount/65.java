package org.nrg.hcp.importer;

import java.io.File;
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
import org.nrg.hcp.utils.ASRScoringUtil;
import org.nrg.hcp.utils.CsvParser;
import org.nrg.xdat.om.GurAsrrawdata;
import org.nrg.xdat.om.GurGurscoringdata;
import org.nrg.xdat.om.GurNeorawdata;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

/*
 * NOTES:  Per e-mail chain with Deanna on 01/13/2012, we are expecting only ONE entry per subject in the CSV file and only ONE entry
 * saved in the database per subject.  This program is based on that assumption.  Per Deanna: 
 * 
 *    "So, datasetid, siteid and famid are going to be assigned by gur and are basically irrelevant to us.  The HCP numbr 
 *     will be input into subid.  The way it SHOULD work is that there will ever only be oe record per person.  If the person
 *     has to quit and start again, we should be able to go right back into their same record.  That is my understanding of 
 *     how that will work.   By backup, we just meant that they might have to finish it later, but it should still be only
 *     one record per subject."
 *     
 * If things change and there could be multiple CSV and/or database records, this program will require modifications to handle 
 * this.    
 *
 */

/**
 * Imports HCP GUR data and populates database.
 * @author Mike Hodge <hodgem@mir.wustl.edu>
 *
 */
public class HCPBehavioralImporter extends ImporterHandlerA implements Callable<List<String>> {

	static Logger logger = Logger.getLogger(HCPBehavioralImporter.class);

	private final FileWriterWrapperI fw;
	private final XDATUser user;
	final Map<String,Object> params;
   	private XnatProjectdata proj;
   	private boolean updateExisting = false;
   	private boolean verboseOutput = false;
   	private ArrayList<String> csvSubjLbls = new ArrayList<String>();
   	private ArrayList<String> dbAsrSubjLbls = new ArrayList<String>();
   	private ArrayList<String> dbNeoSubjLbls = new ArrayList<String>();
   	private ArrayList<String> dbGurSubjLbls = new ArrayList<String>();
   	private ArrayList<String> newAsrSubjLbls = new ArrayList<String>();
   	private ArrayList<String> newNeoSubjLbls = new ArrayList<String>();
   	private ArrayList<String> newGurSubjLbls = new ArrayList<String>();
   	private ArrayList<String> returnList = new ArrayList<String>();
   	
   	// value prepended to ASR fields in CSV file  
   	private static final String asr_prepend = "ASRVIII.";
   	private static final String neo_prepend = "NEO.";
	
	/**
	 * 
	 * @param listenerControl
	 * @param u
	 * @param session
	 * @param overwrite:   'append' means overwrite, but preserve un-modified content (don't delete anything)
	 *                      'delete' means delete the pre-existing content.
	 * @param additionalValues: should include project (subject and experiment are expected to be found in the archive)
	 */
	public HCPBehavioralImporter(Object listenerControl, XDATUser u, FileWriterWrapperI fw, Map<String, Object> params) {
		super(listenerControl, u, fw, params);
		this.user=u;
		this.fw=fw;
		this.params=params;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<String> call() throws ClientException, ServerException {
		verifyProject();
		if (params.get("update")!=null && params.get("update").toString().equalsIgnoreCase("true")) {
			updateExisting = true;
		}
		if (params.get("verbose")!=null && params.get("verbose").toString().equalsIgnoreCase("true")) {
			verboseOutput = true;
		}
		try {
			final List<String> returnList = saveAndProcessCsvFile();
			this.completed("Successfully imported behavioral CSV");
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
		List<Map<String,String>> csvRep = null;
		try {
			csvRep = CsvParser.parseCSV(cacheFile);
		} catch (IOException e) {
			throw new ServerException("Could not parse input CSV file",e);
		}
		if (csvRep==null || csvRep.size()<1) {
			throw new ClientException("Uploaded file contains no data");
		}
		
		populateAsrIdList();
		populateNeoIdList();
		populateGurIdList();
	
		// Loop over rows, creating items
		int rowCount = 0;
		
		for (Map<String,String> row : csvRep) {
			rowCount++;
			// Pull SubjectID
			String subjLbl = null;
			if (row.containsKey("subid")) {
				subjLbl = row.get("subid");
				if (csvSubjLbls.contains(subjLbl)) {
					returnList.add("WARNING:  CSV file contains multiple records for " + subjLbl + ".  The last record will replace any information saved by the former.");
				} else {
					csvSubjLbls.add(subjLbl);
				}
			} else {
				throw new ClientException("Invalid CSV file format.  Column \"subjLbl\", containing subject ID, is not found");
			}
			// Pull Experiment Date
			String dateStr = null;
			Date dateVar = null;
			if (row.containsKey("testdate")) {
				dateStr = row.get("testdate");
				String[] dateArr = dateStr.split("[.\\-/]");
				try {
					Calendar cal = Calendar.getInstance();
					if (Integer.parseInt(dateArr[0])>1900) {
						cal.set(Integer.parseInt(dateArr[0]),Integer.parseInt(dateArr[1])-1,Integer.parseInt(dateArr[2]),0,0,0);
					} else if (Integer.parseInt(dateArr[2])>1900) {
						cal.set(Integer.parseInt(dateArr[2]),Integer.parseInt(dateArr[0])-1,Integer.parseInt(dateArr[1]),0,0,0);
					}
					dateVar = cal.getTime();
				} catch(Exception e) {
					returnList.add("WARNING:  Could not set experiment date (SUBJECT=" + subjLbl + ").");
				}
			} 
			if (dateVar == null) {
				throw new ClientException("Invalid CSV file format.  Column \"testdate\", containing date of experiment, is not found");
			}
			// Process Data
			
			XnatSubjectdata sub = XnatSubjectdata.GetSubjectByProjectIdentifier(proj.getId(),subjLbl,user, false);
			if (sub == null) {
				// Modified Per Cindy 2012-08-25 (File sometimes has Phase I subjects and should not fail when subject not found);
				//throw new ClientException("Could not save ASR record - Subject " + subjLbl + " not found under project " + proj.getId());
				returnList.add("WARNING:  Subject " + subjLbl + " not found under project " + proj.getId() + " - could not save record.");
				continue;
			}
			
			if (dbAsrSubjLbls.contains(subjLbl) && !updateExisting) {
				if (verboseOutput) {
					returnList.add("Subject " + subjLbl + " already exists in ASR database and update=false.  Skipping...");
				}
			} else {
				populateAndSaveOrUpdateAsr(row,sub,rowCount,dateVar);
			}
			
			if (dbNeoSubjLbls.contains(subjLbl) && !updateExisting) {
				if (verboseOutput) {
					returnList.add("Subject " + subjLbl + " already exists in NEO database and update=false.  Skipping...");
				}
			} else {
				populateAndSaveOrUpdateNeo(row,sub,rowCount,dateVar);
			}
			
			if (dbGurSubjLbls.contains(subjLbl) && !updateExisting) {
				if (verboseOutput) {
					returnList.add("Subject " + subjLbl + " already exists in GUR database and update=false.  Skipping...");
				}
			} else {
				populateAndSaveOrUpdateGur(row,sub,rowCount,dateVar);
			}
		}
		
	}

	private void populateAsrIdList() {
        CriteriaCollection cc;
        cc=new CriteriaCollection("OR");
        cc.addClause(GurAsrrawdata.SCHEMA_ELEMENT_NAME + "/project", proj.getId());
        cc.addClause(GurAsrrawdata.SCHEMA_ELEMENT_NAME + "/sharing/share/project", proj.getId());
		ArrayList<GurAsrrawdata> asrRawRecords=GurAsrrawdata.getGurAsrrawdatasByField(cc, user, false);
		for (GurAsrrawdata record : asrRawRecords) {
			String thisId = record.getSubjectData().getLabel();
			if (dbAsrSubjLbls.contains(thisId)) {
				returnList.add("WARNING:  ASR Raw data contains multiple records for " + thisId);
			} else {
				dbAsrSubjLbls.add(thisId);
			}
		}
		asrRawRecords = null;
	}

	private void populateNeoIdList() {
        CriteriaCollection cc;
        cc=new CriteriaCollection("OR");
        cc.addClause(GurNeorawdata.SCHEMA_ELEMENT_NAME + "/project", proj.getId());
        cc.addClause(GurNeorawdata.SCHEMA_ELEMENT_NAME + "/sharing/share/project", proj.getId());
		ArrayList<GurNeorawdata> neoRawRecords=GurNeorawdata.getGurNeorawdatasByField(cc, user, false);
		for (GurNeorawdata record : neoRawRecords) {
			String thisId = record.getSubjectData().getLabel();
			if (dbNeoSubjLbls.contains(thisId)) {
				returnList.add("WARNING:  NEO Raw data contains multiple records for " + thisId);
			} else {
				dbNeoSubjLbls.add(thisId);
			}
		}
		neoRawRecords = null;
	}

	private void populateGurIdList() {
        CriteriaCollection cc;
        cc=new CriteriaCollection("OR");
        cc.addClause(GurGurscoringdata.SCHEMA_ELEMENT_NAME + "/project", proj.getId());
        cc.addClause(GurGurscoringdata.SCHEMA_ELEMENT_NAME + "/sharing/share/project", proj.getId());
		ArrayList<GurGurscoringdata> gurScoringRecords=GurGurscoringdata.getGurGurscoringdatasByField(cc, user, false);
		for (GurGurscoringdata record : gurScoringRecords) {
			String thisId = record.getSubjectData().getLabel();
			if (dbGurSubjLbls.contains(thisId)) {
				returnList.add("WARNING:  GUR Scoring data contains multiple records for " + thisId);
			} else {
				dbGurSubjLbls.add(thisId);
			}
		}
		gurScoringRecords = null;
	}

	@SuppressWarnings({ "static-access", "unchecked" })
	private void populateAndSaveOrUpdateAsr(Map<String, String> row, XnatSubjectdata sub, int rowCount, Date dateVar) throws ServerException, ClientException {
		////////////////////////////////////////////
		// Create and save ASR RawData assessment //
		////////////////////////////////////////////
		
		try {
			// NOTE:  Assumption of one record per subject very important here.
			CriteriaCollection cc=new CriteriaCollection("OR");
			cc.addClause("gur:ASRRawData/subject_ID", sub.getId());
			ArrayList<GurAsrrawdata> currList = GurAsrrawdata.getGurAsrrawdatasByField(cc, user, false);
			GurAsrrawdata currentAsr = (currList.size()>0) ? currList.get(0) : null;
			GurAsrrawdata asrRaw = (currentAsr!=null) ? currentAsr : new GurAsrrawdata((UserI)user);
		
			// The following need not be assigned for pre-existing records
			if (currentAsr==null) {
				asrRaw.setLabel(sub.getLabel() + "_ASRRaw");
				asrRaw.setId(XnatExperimentdata.CreateNewID());
				asrRaw.setProject(proj.getId());
				asrRaw.setSubjectId(sub.getId());
			} 
		
			asrRaw.setDate(dateVar);
			for (GenericWrapperField field : (ArrayList<GenericWrapperField>)asrRaw.getItem().getGenericSchemaElement().getAllFields()) {
				String rowField = asr_prepend + field.getName();
				if (row.containsKey(rowField)) {
					try {
						asrRaw.setProperty(asrRaw.SCHEMA_ELEMENT_NAME + "/" + field.getXMLPathString(), row.get(rowField));
					} catch (XFTInitException e) {
						// Do nothing for now
					} catch (FieldNotFoundException e) {
						// Do nothing for now
					} catch (InvalidValueException e) {
						// Do nothing for now
					}
				}
			}
			try {
				String actionV = null;
				if (currentAsr!=null) {
					if (verboseOutput) 
						returnList.add("Updating ASR record for " + sub.getLabel());
					actionV = EventUtils.MODIFY_VIA_WEB_SERVICE;
				} else {
					if (verboseOutput) 
						returnList.add("Saving new ASR record for " + sub.getLabel());
					actionV = EventUtils.CREATE_VIA_WEB_SERVICE;
				}
				final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, asrRaw.getItem(),
							EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, actionV, null, null));
				final EventMetaI ci = wrk.buildEvent();
				if (SaveItemHelper.authorizedSave(asrRaw,user,false,true,ci)) {
					PersistentWorkflowUtils.complete(wrk, ci);
				} else {
					PersistentWorkflowUtils.fail(wrk,ci);
					if (verboseOutput) 
						returnList.add("No change from pre-existing ASR record - modification not required - (SUBJECT=" + sub.getLabel() + ")");
				}
				newAsrSubjLbls.add(sub.getLabel());
			} catch (Exception e) {
				throw new ServerException("Could not save ASR record - (SUBJECT=" + sub.getLabel() + ")",e);
			}
		} catch (Exception e) {
			throw new ServerException("Could not process ASR record - (SUBJECT=" + sub.getLabel() + ")",e);
		}
		
	}

	@SuppressWarnings({ "static-access", "unchecked" })
	private void populateAndSaveOrUpdateNeo(Map<String, String> row, XnatSubjectdata sub, int rowCount, Date dateVar) throws ClientException, ServerException {
		////////////////////////////////////////////
		// Create and save NEO RawData assessment //
		////////////////////////////////////////////
		try {
			
			// NOTE:  Assumption of one record per subject very important here.
			CriteriaCollection cc=new CriteriaCollection("OR");
			cc.addClause("gur:NEORawData/subject_ID", sub.getId());
			ArrayList<GurNeorawdata> currList = GurNeorawdata.getGurNeorawdatasByField(cc, user, false);
			GurNeorawdata currentNeo = (currList.size()>0) ? currList.get(0) : null;
			GurNeorawdata neoRaw = (currentNeo!=null) ? currentNeo : new GurNeorawdata((UserI)user);
	
			// The following need not be assigned for pre-existing records
			if (currentNeo==null) {
				neoRaw.setLabel(sub.getLabel() + "_NEORaw");
				neoRaw.setId(XnatExperimentdata.CreateNewID());
				neoRaw.setProject(proj.getId());
				neoRaw.setSubjectId(sub.getId());
			} 
		
			neoRaw.setDate(dateVar);
			for (GenericWrapperField field : (ArrayList<GenericWrapperField>)neoRaw.getItem().getGenericSchemaElement().getAllFields()) {
				String rowField = neo_prepend + field.getName();
				if (row.containsKey(rowField)) {
					try {
						neoRaw.setProperty(neoRaw.SCHEMA_ELEMENT_NAME + "/" + field.getXMLPathString(), row.get(rowField));
					} catch (XFTInitException e) {
						// Do nothing for now
					} catch (FieldNotFoundException e) {
						// Do nothing for now
					} catch (InvalidValueException e) {
						// Do nothing for now
					}
				}
			}
			
			try {
				String actionV = null;
				if (currentNeo!=null) {
					if (verboseOutput) 
						returnList.add("Updating NEO record for " + sub.getLabel());
					actionV = EventUtils.MODIFY_VIA_WEB_SERVICE;
				} else {
					if (verboseOutput) 
						returnList.add("Saving new NEO record for " + sub.getLabel());
					actionV = EventUtils.CREATE_VIA_WEB_SERVICE;
				}
				final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, neoRaw.getItem(),
							EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.WEB_SERVICE, actionV, null, null));
				final EventMetaI ci = wrk.buildEvent();
				
				if (SaveItemHelper.authorizedSave(neoRaw,user,false,true,ci)) {
					PersistentWorkflowUtils.complete(wrk, ci);
				} else {
					PersistentWorkflowUtils.fail(wrk,ci);
					if (verboseOutput) 
						returnList.add("No change from pre-existing NEO record - modification not required - (SUBJECT=" + sub.getLabel() + ")");
				}
				
				newNeoSubjLbls.add(sub.getLabel());
			} catch (Exception e) {
				throw new ServerException("Could not save NEO record - (SUBJECT=" + sub.getLabel() + ")",e);
			}
		} catch (Exception e) {
			throw new ServerException("Could not process NEO record - (SUBJECT=" + sub.getLabel() + ")",e);
		}
		
	}

	@SuppressWarnings({ "static-access", "unchecked" })
	private void populateAndSaveOrUpdateGur(Map<String, String> row, XnatSubjectdata sub, int rowCount, Date dateVar) throws ClientException, ServerException {
		////////////////////////////////////////////
		// Create and save GUR Scoring assessment //
		////////////////////////////////////////////
		
		try {
			
			// NOTE:  Assumption of one record per subject very important here.
			CriteriaCollection cc=new CriteriaCollection("OR");
			cc.addClause("gur:GURScoringData/subject_ID", sub.getId());
			ArrayList<GurGurscoringdata> currList = GurGurscoringdata.getGurGurscoringdatasByField(cc, user, false);
			GurGurscoringdata currentGur = (currList.size()>0) ? currList.get(0) : null;
			GurGurscoringdata gurRaw = (currentGur!=null) ? currentGur : new GurGurscoringdata((UserI)user);
		
			// The following need not be assigned for pre-existing records
			if (currentGur==null) {
				gurRaw.setLabel(sub.getLabel() + "_GURScoring");
				gurRaw.setId(XnatExperimentdata.CreateNewID());
				gurRaw.setProject(proj.getId());
				gurRaw.setSubjectId(sub.getId());
			} 
		
			gurRaw.setDate(dateVar);
			// Generate asr score values
			Map<String,String> asrScores = ASRScoringUtil.computeScores(row);
			// Loop over fields
			fieldLoop:
			for (GenericWrapperField field : (ArrayList<GenericWrapperField>)gurRaw.getItem().getGenericSchemaElement().getAllFields()) {
				String rowField = "." + field.getName();
				// Assign non-ASR values
				for (String keyField : row.keySet()) {
					if (keyField.endsWith(rowField)) {
						try {
							gurRaw.setProperty(gurRaw.SCHEMA_ELEMENT_NAME + "/" + field.getXMLPathString(), row.get(keyField));
						} catch (XFTInitException e) {
							// Do nothing for now
						} catch (FieldNotFoundException e) {
							// Do nothing for now
						} catch (InvalidValueException e) {
							// Do nothing for now
						}
						continue fieldLoop;
					}
				}
				// Assign ASR values
				for (String scoreField : asrScores.keySet()) {
					if (rowField.endsWith(scoreField)) {
						try {
							gurRaw.setProperty(gurRaw.SCHEMA_ELEMENT_NAME + "/" + field.getXMLPathString(), asrScores.get(scoreField));
						} catch (XFTInitException e) {
							// Do nothing for now
						} catch (FieldNotFoundException e) {
							// Do nothing for now
						} catch (InvalidValueException e) {
							// Do nothing for now
						}
						continue fieldLoop;
					}
				}
			}
			try {
				String actionV = null;
				if (currentGur!=null) {
					if (verboseOutput) 
						returnList.add("Updating GUR Scoring record for " + sub.getLabel());
					actionV = EventUtils.MODIFY_VIA_WEB_SERVICE;
				} else {
					if (verboseOutput) 
						returnList.add("Saving new GUR Scoring record for " + sub.getLabel());
					actionV = EventUtils.CREATE_VIA_WEB_SERVICE;
				}
				final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, gurRaw.getItem(),
							EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.STORE_XML, actionV, null, null));
				final EventMetaI ci = wrk.buildEvent();
				if (SaveItemHelper.authorizedSave(gurRaw,user,false,true,ci)) {
					PersistentWorkflowUtils.complete(wrk, ci);
				} else {
					PersistentWorkflowUtils.fail(wrk,ci);
					if (verboseOutput) 
						returnList.add("No change from pre-existing GUR Scoring record - modification not required - (SUBJECT=" + sub.getLabel() + ")");
				}
				
				newGurSubjLbls.add(sub.getLabel());
			} catch (Exception e) {
				throw new ServerException("Could not save GUR Scoring record - (SUBJECT=" + sub.getLabel() + ")",e);
			}
		} catch (Exception e) {
			throw new ServerException("Could not process GUR Scoring record - (SUBJECT=" + sub.getLabel() + ")",e);
		}
		
	}

}


