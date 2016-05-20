package com.acnc.mm.web.messenger;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


import java.util.List;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import javax.sql.rowset.serial.SerialException;



import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.RowEditEvent;
import com.acnc.mm.domain.messenger.BusinessRecord;
import com.acnc.mm.domain.messenger.CandidateInfo;

import com.acnc.mm.domain.messenger.Category;
import com.acnc.mm.domain.messenger.CertResume;
import com.acnc.mm.domain.messenger.ContractRecord;
import com.acnc.mm.domain.messenger.DeleteBusiness;
import com.acnc.mm.domain.messenger.DeleteCandidate;
import com.acnc.mm.domain.messenger.DeleteContract;
import com.acnc.mm.domain.messenger.EducationResume;
import com.acnc.mm.domain.messenger.EmailRepo;
import com.acnc.mm.domain.messenger.EmailRepo2;
import com.acnc.mm.domain.messenger.EmployResume;

import com.acnc.mm.domain.messenger.FlatFile;
import com.acnc.mm.domain.messenger.JobRecord;
import com.acnc.mm.domain.messenger.UpdateBusiness;
import com.acnc.mm.domain.messenger.UpdateCandidate;
import com.acnc.mm.domain.messenger.UpdateCandidateInfo;
import com.acnc.mm.domain.messenger.UpdateContract;
import com.acnc.mm.services.mail.MailService;
import com.acnc.mm.services.messenger.MessengerService;
import com.acnc.mm.util.MessengerConstants;
import com.acnc.mm.web.BaseUIBean;


public class ViewMessengerData<updateCandidate>  extends BaseUIBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1865449671609587246L;

	private static Logger log = Logger.getLogger(ViewMessengerData.class.getName());
	
	 private int currentLevel = 1;  

	private boolean skip;  
	
	private int number = 0;
	
	private String userId;
	
	private String password;
	
	private String newNameFile;
	
	private String fNameFile;
	
	private String candinfoFirstName;
	
    private String candinfoLastName;
	
    private String candinfoMiddle;
	
    private String candinfoEmail;
	
    private String candinfoQuestone;
	
    private String candinfoQuesttwo;
	
    private String candinfoQuesthree; 
	
    private String candinfoQuestfour;
	
	private String candinfoQuestfive;
	
	private String candinfoSkills;
	
	private Long candinfo_id;
	
	public Long edID;
	
	private String degree;
	
	private String schoolName;
	
	private String major;
	
	private Date compYear;
	
	public Long cerID;
	
	private String certName;
	
	private Date certYear;
	
	public Long employID;
	
    private String compName;
    
	private String title;
	
	private byte[] clobFile;
	
	private String projectRole;
	
	private String projDescpt; 
	
	private Date fromMonth;
	
	private String fromYear;
	
	private String toMonth;
	
	private Date toYear;
	
	private Date projfromMonth;
	
	private String projfromYear;
	
	private String projtoMonth;
	
	private Date projtoYear;
	
	private String catName;
	
	private String catDescrp;
	
	private Date cateDate;
	
	private Blob fileClob; 
	
	private String lilb;
	
	private String newName;
	
	private String oldName;
	
	private Date dateCreated;
	
    private boolean valueOne;
	  
    private boolean valueTwo;	
		
	private MessengerService messengerService;	
	
	private MailService mailService;
	
	private List<JobRecord> allJobPosted;
	
	private List<EducationResume> allEducationPosted;
	
	private List<EducationResume> allSelectedEducation;
	
	private List<EmployResume> allSelectedEmploy;
	
	private List<EmployResume> allEmployPosted;
	
	private List<CertResume> allCertPosted;
	
	private List<CertResume> allSelectedCerts;
	
	private List<CertResume> selectedCerts;
	
	private List<CertResume> allQueryCertPosted;
	
	private List<CandidateInfo> allQueryRecord;
		
	private List<ContractRecord> allContractPosted;
	
	private List<ContractRecord> cr;
	
	private List<BusinessRecord> br;
	
	private List<EmailRepo> allEmailPosted;
	
	private List<EmailRepo2> allEmailPosted2;
	
	private List<CandidateInfo> allCandidateInfoPosted;
	
	private List<EducationResume> empEdu;
	
	private List<EmployResume> mainER;
	
	private List<CandidateInfo> cnd;
	
	private List<CertResume> cert;
	
	private List<Category> cat;
	
	private List<FlatFile> flat;
	
	private JobRecord jobRecord;
	
	private Category category;
	
	private FlatFile flatfile;
	
	private CertResume certResume;
			
	private CandidateInfo  candidateInfo;
	
	private UpdateCandidate updateCandidate;
	
	private UpdateCandidateInfo updateCandidateinfo;
	
	private UpdateBusiness updateBusiness;
	
	private UpdateContract updateContract;
	
	private DeleteCandidate deleteCandidate;
	
	private ContractRecord contractRecord;
	
	private BusinessRecord businessRecord;
	
	private DeleteContract deleteContract;
	
	private DeleteBusiness deleteBusiness;
	
	private EducationResume educationResume;
	
	private EmployResume employResume;
	
	private String candidateFirstName;
	
	private String candidateLastName;

	private String candidateEmail;

	private String jobTitle;
	
	private String jobDescription;
	
	private Date jobDate;
	
	private Date jobStatusChange;
	
	private Long jobRequest;
	
	private String contractTitle;
	
	private String contractAgency;
	
	private Date contractDate;
	
	private String businessFirstName;
	
	private String businessLastName;
	
	private String businessPhoneNumber;
	
	private	String businessEmail;
	
	private Boolean showEmailPushOption = Boolean.FALSE;
	
	private Boolean showMainData = Boolean.TRUE;
	
	private Boolean showJobDataInput = Boolean.FALSE;

	private Boolean showCanDataInput = Boolean.FALSE;

	private Boolean showSelectedLst = Boolean.FALSE;
	
	private Boolean showSelectedLstDisable = Boolean.TRUE;
	
    private Boolean showSelectedLst2 = Boolean.FALSE;
	
	private Boolean showSelectedLstDisable2 = Boolean.TRUE;

	private Boolean addingEducation = Boolean.FALSE;
	
	private Boolean showFileUpLoaderDisable = Boolean.FALSE;
	
	private Boolean showEditButtonDisable = Boolean.FALSE;
	
	private Boolean canFileUpLoaderDisable = Boolean.FALSE;
	
	private Boolean showContractUpLoaderDisable = Boolean.FALSE;
	
	private Boolean addingEmployment = Boolean.FALSE;
	
	private Boolean addingCertification = Boolean.FALSE;
	
	private Boolean addingResume = Boolean.FALSE;
	
	private BusinessRecord selectedBusiness;
	
	private JobRecord selectedJobs;
	
	private ContractRecord selectedContract;
	
	private CandidateInfo selectedCandidateInfo;
	
	private CertResume selectedCertResume;
	
	private List<CandidateInfo> allSelectedCandidateInfo;
	
	private List<JobRecord> allSelectedJobs;
	
	private List<ContractRecord> allSelectedContracts;
	
	private List<BusinessRecord> allSelectedBusiness;
	
	private List<EmailRepo> allAddedEmailsInfo;
	
	private List<EmailRepo2> allAddedEmailsInfo2;
	
	private List<BusinessRecord> allBusinessPosted;

	private JobRecord selectedJob;
		
	private ContractRecord selectedContracts;
	
	private Long passID;	
	
	private EmailRepo lpAEMP;
	
	private EmailRepo2 lpAEMP2;
	
	private String destination = "\\Users\\Shared\\dumpTmp\\";
	
	public String getBeanName() {
		return "viewMessengerData";
	}
	
	public ViewMessengerData() {
		init();
		
	}
	
	private void init() {

	messengerService = (MessengerService) getService(MessengerConstants.SERVICE_MESSENGER_DATA);

	mailService = (MailService) getService(MessengerConstants.SERVICE_MAIL_DATA);
	
	allJobPosted = new ArrayList<JobRecord>();
	
	/*allJobPosted = messengerService.getAllJobs();
	*/
	allEducationPosted = new ArrayList<EducationResume>();
    
    /*allEducationPosted = messengerService.getAllEducation();*/
	
	allEmployPosted = new ArrayList<EmployResume>();
	
	/*allEmployPosted = messengerService.getAllEmploy();*/
	
	allCertPosted = new ArrayList<CertResume>();
	
	/*allCertPosted = messengerService.getAllCert();*/
	
	allSelectedCerts = new ArrayList<CertResume>();
	
	selectedCerts = new ArrayList<CertResume>();
	
    allSelectedEducation = new ArrayList<EducationResume>();
	
    allSelectedEmploy = new ArrayList<EmployResume>();
	
	cnd = new ArrayList<CandidateInfo>();
	
	cr = new ArrayList<ContractRecord>();
	
	br = new ArrayList<BusinessRecord>();
	
	cat = new ArrayList<Category>();
	
	flat = new ArrayList<FlatFile>();
	
	category = new Category();
	
	flatfile = new FlatFile();
	
	candidateInfo = new CandidateInfo();
	
	cert = new ArrayList<CertResume>();
	
	certResume = new CertResume();
	
	mainER = new ArrayList<EmployResume>();
	
	employResume = new EmployResume();
	
    empEdu = new ArrayList<EducationResume>();
	
	educationResume = new EducationResume();
	
	allContractPosted = new ArrayList<ContractRecord>();
	
	/*allContractPosted = messengerService.getAllContracts();*/
	
	allCandidateInfoPosted = new ArrayList<CandidateInfo>();
	
	/*allCandidateInfoPosted = messengerService.getAllCandidateInfo();*/
	
    allEducationPosted = new ArrayList<EducationResume>();
    
    /*allEducationPosted = messengerService.getAllEducation();*/
	
	allEmployPosted = new ArrayList<EmployResume>();
	
	/*allEmployPosted = messengerService.getAllEmploy();*/
	
	allCertPosted = new ArrayList<CertResume>();
	
	/*allCertPosted = messengerService.getAllCert();*/
	
    allQueryRecord = new ArrayList<CandidateInfo>();
	
	/*allQueryRecord = messengerService.getAllQueryRecord();*/
	
	allBusinessPosted = new ArrayList<BusinessRecord>();
	
	/*allBusinessPosted = messengerService.getAllBusiness();*/
	
	allEmailPosted = new ArrayList<EmailRepo>();
	
	/*allEmailPosted = messengerService.getAllEmailRepos();*/
	
	allEmailPosted2 = new ArrayList<EmailRepo2>();
	
	/*allEmailPosted2 = messengerService.getAllEmailRepos2();*/
	
	jobRecord = new JobRecord();
	
	candidateFirstName = null;
	
	candidateLastName = null;
	
	candidateEmail = null;
	
	jobDescription = null;
	
	jobTitle = null;
		
	contractTitle = null;
	
	allSelectedJobs = new ArrayList<JobRecord>();
		
	allSelectedContracts = new ArrayList<ContractRecord>();
	
	allSelectedBusiness = new ArrayList<BusinessRecord>();
	
	allSelectedCandidateInfo = new ArrayList<CandidateInfo>();
	
	allAddedEmailsInfo = new ArrayList<EmailRepo>();
	
	allAddedEmailsInfo2 = new ArrayList<EmailRepo2>();
	
	lpAEMP = new EmailRepo();
	
	lpAEMP2 = new EmailRepo2();
	
	showSelectedLst = Boolean.FALSE;
	
	showSelectedLstDisable = Boolean.TRUE;
	
	showFileUpLoaderDisable = Boolean.FALSE;
	
	showEditButtonDisable = Boolean.FALSE;
	
	canFileUpLoaderDisable = Boolean.FALSE;
	
	showContractUpLoaderDisable = Boolean.FALSE;
	
    showSelectedLst2 = Boolean.FALSE;
	
	showSelectedLstDisable2 = Boolean.TRUE;

	addingEducation = Boolean.FALSE;
	
	addingEmployment = Boolean.FALSE;
	
	addingCertification = Boolean.FALSE;
	
	addingResume = Boolean.FALSE;
	
	updateCandidate = new UpdateCandidate();
	
	updateCandidateinfo = new UpdateCandidateInfo();
	
	updateBusiness = new UpdateBusiness();
	
	deleteCandidate = new DeleteCandidate();
	
	contractRecord = new ContractRecord();
	
	businessRecord = new BusinessRecord();
	
	updateContract = new UpdateContract(); 
	
	deleteContract = new DeleteContract();
	
	deleteBusiness = new DeleteBusiness();
	
	degree = null;
	
	schoolName = null;
	
	major = null;
	
	compYear = null;
	
	certName = null;
	
	certYear = null;
	
	compName = null;
	
	title = null;
	
	projectRole = null;
	
	projDescpt = null; 
	
	fromMonth = null;
	
	fromYear = null;
	
	toMonth = null;
	
	toYear = null;
	
	projfromMonth = null;
	
	projfromYear = null;
	
	projtoMonth = null;
	
	projtoYear = null;
	
    candinfoFirstName = null;
	
    candinfoLastName = null;
	
    candinfoMiddle = null;
	
    candinfoEmail = null;
	
    candinfoQuestone = null;
	
    candinfoQuesttwo = null;
	
    candinfoQuesthree = null; 
	
    candinfoQuestfour = null;
	
	candinfoQuestfive = null;
	
	candinfoSkills = null;
	
	fNameFile = null;	
	
	contractDate = null;
	
    catName = null;
	
	catDescrp = null;
	
	cateDate = null;
	
	fileClob = null; 
	
	newName = null;
	
	oldName = null;
	
	dateCreated = null;
	
	newNameFile = null;
	
	userId = null;
	
	password = null;
	
	}
	
	
	public void shwJobFrm(){
		log.info("showing job form");
		/*jobRecord = new JobRecord();*/
	}
	
		
	public void shwConFrm(){
		log.info("showing contract form");
		/*contractRecord = new ContractRecord();*/
	}
	
	
	public void upCandfrm(){
		log.info("showing updated candidate info");
		/*updateCandidateinfo = new UpdateCandidateInfo();*/
	}
	 
	public void upBusfrm(){
		log.info("showing business form");
		/*updateBusiness = new UpdateBusiness();*/
	}
	
	public void showCANfrm(){
		log.info("Showing candidates");
		/*candidateInfo = new CandidateInfo();*/
	}
	
 	public String deleteCandidate(){
		log.info("Delete Canddiate");
		updateCandidateinfo.setCandinfo_id(selectedCandidateInfo.getCandinfo_id());
		messengerService.deleteCandidate(updateCandidateinfo);
		addMessage("Successfully Deleted: "+ selectedCandidateInfo.getCandinfoFirstName() + " record!" );
		clearValues();
		return null;
	}
	
	//Add populate button	
   public String saveCandidateData(){
		log.info("saving the following data");
		log.info(candinfoFirstName);
		log.info(candinfoLastName);
		log.info(candinfoEmail);

		candidateInfo.setCandinfoFirstName(candinfoFirstName);
		candidateInfo.setCandinfoLastName(candinfoLastName);
		candidateInfo.setCandinfoEmail(candinfoEmail);
		candidateInfo.setCandinfoMiddle("Blank");
		candidateInfo.setCandinfoQuestone("NO");
		candidateInfo.setCandinfoQuesttwo("NO");
		candidateInfo.setCandinfoQuesthree("NO");
		candidateInfo.setCandinfoQuestfour("NO");
		candidateInfo.setCandinfoQuestfive("NO");
		candidateInfo.setCandinfoSkills("JAVA");
		
		// Dummy data for the other tables Education Certs and EMployment..
		educationResume.setDegree("BA");
		educationResume.setSchoolName("UMUC");
		educationResume.setMajor("CMIS");
		educationResume.setCompYear(new Date());		
		
		certResume.setCertName("NAHO");
		certResume.setCertYear(new Date());
		
		employResume.setCompName("ACMC");
		employResume.setTitle("DEVELPOER");
		employResume.setFromMonth(new Date());
		employResume.setToYear(new Date());
		employResume.setProjectRole("Tech Lead");
		employResume.setProjDescpt("Responsible for the entire project");
		employResume.setProjfromMonth(new Date());
		employResume.setProjtoYear(new Date());   			
				
		messengerService.saveCandidate(candidateInfo);
		messengerService.sveducationinfo(educationResume);
		messengerService.svcertresume(certResume);
		messengerService.svemployresume(employResume);
		log.info("new candidate data candidate");
		addMessage("Successfully Saved: "+ candidateInfo.getCandinfoFirstName() + " record!" );
		clearValues();
		return null;

	}
	
	public String updateBusiness(){
        log.info("update Business Info");
		
        log.info(selectedBusiness.getBusinessID().toString());
		log.info(selectedBusiness.businessFirstName);
		log.info(selectedBusiness.businessLastName);
		log.info(selectedBusiness.businessPhoneNumber);
		log.info(selectedBusiness.businessEmail);
		
		updateBusiness.setBusinessID(selectedBusiness.getBusinessID());
		updateBusiness.setBusinessFirstName(selectedBusiness.getBusinessFirstName());
		updateBusiness.setBusinessLastName(selectedBusiness.getBusinessLastName());
		updateBusiness.setBusinessPhoneNumber(selectedBusiness.getBusinessPhoneNumber());
		updateBusiness.setBusinessEmail(selectedBusiness.getBusinessEmail());				
		messengerService.updateBusiness(updateBusiness);
		addMessage("Successfully Updated: "+ selectedBusiness.getBusinessFirstName() + " record!");
		clearValues();
		return null;
	
	}
	
	public String updateContract(){
		log.info("update Contract info");
		log.info(selectedContracts.contractTitle);
		log.info(selectedContracts.contractAgency);
		updateContract.setContractID(selectedContracts.getContractID());
		updateContract.setContractTitle(selectedContracts.getContractTitle());
		updateContract.setContractAgency(selectedContracts.getContractAgency());
		messengerService.updateContract(updateContract);
		addMessage("Successfully Updated: "+ selectedContracts.getContractTitle() + " record!");
		clearValues();
		return null;
		
	}
	
	public String updateJob(){
		log.info("Updating Job info.");
		jobRecord.setJobID(selectedJobs.getJobID());
		jobRecord.setJobTitle(selectedJobs.getJobTitle());
		jobRecord.setJobDescription(selectedJobs.getJobDescription());
		messengerService.updateJob(jobRecord);
		addMessage("Successfully Updated: "+ jobRecord.getJobTitle() + " record!" );
		clearValues();
		return null;
	}
	
	public String deleteContract(){
		log.info("deleting Contract");
		contractRecord.setContractID(selectedContracts.getContractID());		
		messengerService.deleteContract(contractRecord);
		addMessage("Successfully Deleted: "+ selectedContracts.getContractTitle() + " record!");
		clearValues();
		return null;
	}
	
	public String deleteJob(){
		log.info("deleting Job");
		jobRecord.setJobID(selectedJobs.getJobID());
		messengerService.deleteJob(jobRecord);
		addMessage("Successfully Deleted: "+ jobRecord.getJobTitle() + " record!");
		clearValues();
		return null;
	}
	
	public String deleteBusiness(){ 
		log.info("deleting Business");
		businessRecord.setBusinessID(selectedBusiness.getBusinessID());
		messengerService.deleteBusiness(businessRecord);
		addMessage("Successfully Deleted: "+ businessRecord.getBusinessFirstName() + " record!");
		clearValues();
		return null;
	}
	
	
	public String saveJobData(){
		log.info("saving the following data");
		log.info(jobTitle);
		log.info(jobDescription);

		jobRecord.setJobTitle(jobTitle);
		jobRecord.setJobDescription(jobDescription);
		
		messengerService.saveJob(jobRecord);
		log.info(" new job data saved");
		addMessage("Successfully Saved: "+ jobRecord.getJobTitle() + " record!" );
		clearValues();
		return null;
	}
	
	public String saveBusinessData() {
		log.info("saving the following data");

		log.info(businessFirstName);
		log.info(businessLastName);
		log.info(businessEmail);

		businessRecord.setBusinessFirstName(businessFirstName);
		businessRecord.setBusinessLastName(businessLastName);
		businessRecord.setBusinessPhoneNumber(businessPhoneNumber);
		businessRecord.setBusinessEmail(businessEmail);
		
		messengerService.saveBusiness(businessRecord);
		log.info("new business data candidate");
		addMessage("Successfully Saved: "+ businessRecord.getBusinessFirstName() + " record!" );
		clearValues();
		return null;
		
	}
	
	//Resume wizard candidate
	public String saveCandidateInfo() {
		log.info("Resume Candinfo General Info");		
		candidateInfo.setCandinfoFirstName(candinfoFirstName);
		candidateInfo.setCandinfoLastName(candinfoLastName);
		candidateInfo.setCandinfoMiddle(candinfoMiddle);
		candidateInfo.setCandinfoEmail(candinfoEmail);
		candidateInfo.setCandinfoQuestone(candinfoQuestone);
		candidateInfo.setCandinfoQuesttwo(candinfoQuesttwo);
		candidateInfo.setCandinfoQuesthree(candinfoQuesthree);
		candidateInfo.setCandinfoQuestfour(candinfoQuestfour);
		candidateInfo.setCandinfoQuestfive(candinfoQuestfive);
		candidateInfo.setCandinfoSkills(candinfoSkills);
		log.info(candinfoFirstName);
		messengerService.saveCandidate(candidateInfo);
		cnd.add(candidateInfo);
       
		clearValues();
		return null;

	}
   //Resume Education Wizard
	public String saveEducationResume() {
		educationResume.setDegree(degree);
		educationResume.setSchoolName(schoolName);
		educationResume.setMajor(major);
		educationResume.setCompYear(compYear);
      
		messengerService.sveducationinfo(educationResume);
		empEdu.add(educationResume);
		for(EducationResume ed: empEdu){
        	System.out.println("stored vaules first : " + ed.getDegree());
        }        

		clearValues();
		return null;

	}
   //Resume Certs Wizard
	public String saveCertResume() {
		certResume.setCertName(certName);
		certResume.setCertYear(certYear);
        messengerService.svcertresume(certResume);
		cert.add(certResume);

		clearValues();
		return null;

	}
  //Resume Employment Wizard
	public String saveEmployResumeinfo() {
		employResume.setCompName(compName);
		employResume.setTitle(title);
		employResume.setFromMonth(fromMonth);
		employResume.setToYear(toYear);
		employResume.setProjectRole(projectRole);
		employResume.setProjDescpt(projDescpt);
		employResume.setProjfromMonth(projfromMonth);
		employResume.setProjtoYear(projtoYear);        
		messengerService.svemployresume(employResume);
		mainER.add(employResume);
       
		clearValues();
		return null;

	}
	
	public String getIDNUmber(){
		passID = selectedCandidateInfo.getCandinfo_id();
		System.out.println(selectedCandidateInfo.getCandinfo_id());
			
	    allSelectedCerts = new ArrayList<CertResume>();
		/*allSelectedCerts = messengerService.getSelectedCertsInfo(passID);*/
	
	    allSelectedEducation = new ArrayList<EducationResume>();
	    /*allSelectedEducation = messengerService.getSelectedEducationInfo(passID);*/
		
	    allSelectedEmploy = new ArrayList<EmployResume>();
	    /*allSelectedEmploy = messengerService.getSelectedEmployInfo(passID);*/
	    
	    //showEditButtonDisable = Boolean.TRUE;
		
		return null;
	}
	
		
	public void updateViewCand(){
		candidateInfo.setCandinfo_id(selectedCandidateInfo.getCandinfo_id());
		candidateInfo.setCandinfoFirstName(selectedCandidateInfo.candinfoFirstName);
		candidateInfo.setCandinfoLastName(selectedCandidateInfo.candinfoLastName);
		candidateInfo.setCandinfoMiddle(selectedCandidateInfo.candinfoMiddle);
		candidateInfo.setCandinfoEmail(selectedCandidateInfo.candinfoEmail);
		candidateInfo.setCandinfoQuestone(selectedCandidateInfo.candinfoQuestone);
		candidateInfo.setCandinfoQuesttwo(selectedCandidateInfo.candinfoQuesttwo);
		candidateInfo.setCandinfoQuesthree(selectedCandidateInfo.candinfoQuesthree);
		candidateInfo.setCandinfoQuestfour(selectedCandidateInfo.candinfoQuestfour);
		candidateInfo.setCandinfoQuestfive(selectedCandidateInfo.candinfoQuestfive);
		candidateInfo.setCandinfoSkills(selectedCandidateInfo.candinfoSkills);
		
		messengerService.upDateCandidate(candidateInfo);
		
		
	}
	
	//Resume  view wizard employment edit
	public void rowEditEmploy(RowEditEvent event){
		Long num = (Long)((EmployResume) event.getObject()).getEmployID();
		String emp = (String) ((EmployResume) event.getObject()).getCompName();
		String tt = (String) ((EmployResume) event.getObject()).getTitle();
		
		System.out.printf("Company Name " + emp + " Title " + tt + " With ID NUm : " + num);
		System.out.println("\n");
		 
		 messengerService.updateEmploy(((EmployResume)event.getObject()));
	     FacesMessage msg = new FacesMessage("Employ Edited", (String) ((EmployResume) event.getObject()).getCompName());  
	  
	     FacesContext.getCurrentInstance().addMessage(null, msg); 
		
	}
	//Resume  view employment wizard cancel
	public void cancelEmploy(RowEditEvent event){
		Long num = (Long)((EmployResume) event.getObject()).getEmployID();
		String emp = (String) ((EmployResume) event.getObject()).getCompName();
		String tt = (String) ((EmployResume) event.getObject()).getTitle();
		
		System.out.printf("Company Name " + emp + " Title " + tt + " With ID NUm : " + num);
		System.out.println("\n");
	    FacesMessage msg = new FacesMessage("Employ cancel", (String) ((EmployResume) event.getObject()).getCompName());  
	  
	    FacesContext.getCurrentInstance().addMessage(null, msg); 
		
	}

	//Resume  view wizard education edit
	public void rowEditEducation(RowEditEvent event){
		Long num = (Long)((EducationResume) event.getObject()).getEdID();
		String scname = (String) ((EducationResume) event.getObject()).getSchoolName();
		String mj = (String) ((EducationResume) event.getObject()).getMajor();
		String dg = (String) ((EducationResume) event.getObject()).getDegree();
		System.out.printf("Degree " + dg + " school name " + scname + " Major " + mj + " With ID NUm : " + num);
		System.out.println("\n");
		messengerService.updateEducation(((EducationResume) event.getObject()));
	    FacesMessage msg = new FacesMessage("education Edited", ((EducationResume) event.getObject()).getDegree());  
	  
	    FacesContext.getCurrentInstance().addMessage(null, msg);  
		
	}
	//Resume  view wizard cancel education
	public void cancelEducation(RowEditEvent event){
		
		Long num = (Long)((EducationResume) event.getObject()).getEdID();
		String scname = (String) ((EducationResume) event.getObject()).getSchoolName();
		String mj = (String) ((EducationResume) event.getObject()).getMajor();
		String dg = (String) ((EducationResume) event.getObject()).getDegree();
		System.out.printf("Degree " + dg + " school name " + scname + " Major " + mj + " With ID NUm : " + num);
		System.out.println("\n");
	    FacesMessage msg = new FacesMessage("Education Cancelled", ((EducationResume) event.getObject()).getDegree());  
	  
	    FacesContext.getCurrentInstance().addMessage(null, msg);  
	}
	////Resume  view wizard Certs edit
	 public void rowEditCerts(RowEditEvent event) {  
		   Long num = (Long)((CertResume) event.getObject()).getCerID();
		   String limitName =  (String) ((CertResume) event.getObject()).getCertName();
		   Date dt = (Date) ((CertResume) event.getObject()).getCertYear();
		   System.out.printf("Cert Name Edited " + limitName + " Cert Date " + dt + " With ID NUm : " + num);
		   System.out.println("\n");
		   messengerService.updateCert(((CertResume) event.getObject()));
	       FacesMessage msg = new FacesMessage("Cert Edited", ((CertResume) event.getObject()).getCertName());  
	  
	       FacesContext.getCurrentInstance().addMessage(null, msg);  
	    } 
	 ////Resume  view wizard Certs cancel
	 public void cancelCerts(RowEditEvent event) { 
		 Long num = (Long)((CertResume) event.getObject()).getCerID();
		 String limit = (String) ((CertResume) event.getObject()).getCertName();
		 System.out.printf("Car Cancelled " + limit + " With ID NUm : " + num);
		 System.out.println("\n");
		  
		 
	     FacesMessage msg = new FacesMessage("Car Cancelled", ((CertResume) event.getObject()).getCertName());  
	  
	     FacesContext.getCurrentInstance().addMessage(null, msg);  
	    } 
	 
	 //UpLoad event Process 
	 private static void log(String message)
	  {
	          System.out.println(message);
	  }
	
	private static String GetFileExtension(String fname2)
	  {
	      String fileName = fname2;
	      String fname="";
	      String ext="";
	      int mid= fileName.lastIndexOf(".");
	      fname=fileName.substring(0,mid);
	      ext=fileName.substring(mid+1,fileName.length());
	      return ext;
	  }

	public void handleFileUpload(FileUploadEvent event) throws NoSuchAlgorithmException, SerialException, SQLException {  
        FacesMessage msg = new FacesMessage("Success! ", event.getFile().getFileName() + " is uploaded.");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        newNameFile = event.getFile().getFileName();
        
        // Do what you want with the file        
        try {
            copyFile(event.getFile().getFileName(), event.getFile().getInputstream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String fileExtn = GetFileExtension(event.getFile().getFileName());
          Workbook wb = null;
          Sheet sheet = null;
          try {
			     
			    FileInputStream file = new FileInputStream(new File(destination + event.getFile().getFileName()));
			     
			    		    
		    if (fileExtn.equalsIgnoreCase("xlsx"))
			      {
				       wb = new XSSFWorkbook(file);
				      log("xlsx="+wb.getSheetName(0));
				      sheet = wb.getSheetAt(0);
			      }
		    else if (fileExtn.equalsIgnoreCase("xls"))
			      {
				  POIFSFileSystem fs = new POIFSFileSystem(file);
			    	  wb = new HSSFWorkbook(fs);
			    	  log("xls="+wb.getSheetName(0));
			    	  sheet = wb.getSheetAt(0);
			      }
			      int sheets = wb.getNumberOfSheets();			     
				     for(int i =0; i< sheets; i++) {
				      
				    	 sheet = wb.getSheetAt(i);
			     
			    //Iterate through each rows from first sheet
			    Iterator<Row> rowIterator = sheet.iterator();
			    while(rowIterator.hasNext()) {
			    	
			        Row row = rowIterator.next();
			        
			        Double num = 0.0;			        
			        String Column1 = "";
			        String Column2 = "";
			        String Column3 = "";
			        String Column4 = "";
			        String Column5 = "";
			        String Column6 = "";
			        String Column7 = "";
			        String Column8 = "";
			        String Column9 = "";
			        String Column10 = "";
			        String Column11 = "";
			        String Column12 = "";
			        String Column13 = "";
			        String Column14 = "";
				    String Column15 = "";
				    String Column16 = "";
				    String Column17 = "";
				    String Column18 = "";
				    String Column19 = "";
				    String Column20 = "";
				    String Column21 = "";
				    String Column22 = "";
				    String Column23 = "";
				    String Column24 = "";
				    String Column25 = "";
				    String Column26 = "";
				    String Column27 = "";
				    String Column28 = "";
				    
			         
			        //For each row, iterate through each columns
			        Iterator<Cell> cellIterator = row.cellIterator();
			        while(cellIterator.hasNext()) {
			             
			            Cell cell = cellIterator.next();
			             
			            switch(cell.getCellType()) {
			                case Cell.CELL_TYPE_BOOLEAN:
			                    System.out.print(cell.getBooleanCellValue() + "\t\t");
			                    break;
			                case Cell.CELL_TYPE_NUMERIC:
			                	if (DateUtil.isCellDateFormatted(cell)) {
			                		
			                		 FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
			                		   CellValue evaluate = evaluator.evaluate(cell);
			                		   Date date = DateUtil.getJavaDate(evaluate.getNumberValue());
			                		   System.out.println(date);
			                		jobDate = cell.getDateCellValue();
			                		//jobRecord.setJobDate(jobDate);
			                		System.out.println(jobDate + " The Date ");
			                		jobDate = null;
			                	} else {
			                    	jobRequest = (long) cell.getNumericCellValue();
			                       // jobRecord.setJobRequest(jobRequest);
			                        System.out.println(jobRequest + " The Job Request");
			                        
			                    }
			                    	//System.out.println(cell.getDateCellValue());
			                   			              
			                   // System.out.print(" Value: from numeric " + cell.getNumericCellValue() + "\t\t");
			                    
			                    break;
			                case Cell.CELL_TYPE_STRING:
			                	if(Column1.equalsIgnoreCase("")){
			                		Column1 = cell.getStringCellValue().trim();
			                	}else if (Column2.equalsIgnoreCase("")){
			                		Column2 = cell.getStringCellValue().trim();
			                	}else if (Column3.equalsIgnoreCase("")){
			                		Column3 = cell.getStringCellValue().trim();
			                	}else if (Column4.equalsIgnoreCase("")){
			                		Column4 = cell.getStringCellValue().trim();
			                	}else if (Column5.equalsIgnoreCase("")){
			                		Column5 = cell.getStringCellValue().trim();
			                	}else if (Column6.equalsIgnoreCase("")){
			                		Column6 = cell.getStringCellValue().trim();
			                	}else if (Column7.equalsIgnoreCase("")){
			                		Column7 = cell.getStringCellValue().trim();
			                	}else if (Column8.equalsIgnoreCase("")){
			                		Column8 = cell.getStringCellValue().trim();
			                	}else if (Column9.equalsIgnoreCase("")){
			                		Column9 = cell.getStringCellValue().trim();
			                	}else if (Column10.equalsIgnoreCase("")){
			                		Column10 = cell.getStringCellValue().trim();
			                	}else if (Column11.equalsIgnoreCase("")){
			                		Column11 = cell.getStringCellValue().trim();
			                	}else if (Column12.equalsIgnoreCase("")){
			                		Column12 = cell.getStringCellValue().trim();
			                	}else if (Column13.equalsIgnoreCase("")){
			                		Column13 = cell.getStringCellValue().trim();
			                	}else if (Column14.equalsIgnoreCase("")){
			                		Column14 = cell.getStringCellValue().trim();
			                	}else if (Column15.equalsIgnoreCase("")){
			                		Column15 = cell.getStringCellValue().trim();
			                	}else if (Column16.equalsIgnoreCase("")){
			                		Column16 = cell.getStringCellValue().trim();
			                	}else if (Column17.equalsIgnoreCase("")){
			                		Column17 = cell.getStringCellValue().trim();
			                	}else if (Column18.equalsIgnoreCase("")){
			                		Column18 = cell.getStringCellValue().trim();
			                	}else if (Column19.equalsIgnoreCase("")){
			                		Column19 = cell.getStringCellValue().trim();
			                	}else if (Column20.equalsIgnoreCase("")){
			                		Column20 = cell.getStringCellValue().trim();
			                	}else if (Column21.equalsIgnoreCase("")){
			                		Column21 = cell.getStringCellValue().trim();
			                	}else if (Column22.equalsIgnoreCase("")){
			                		Column22 = cell.getStringCellValue().trim();
			                	}else if (Column23.equalsIgnoreCase("")){
			                		Column23 = cell.getStringCellValue().trim();
			                	}else if (Column24.equalsIgnoreCase("")){
			                		Column24 = cell.getStringCellValue().trim();
			                	}else if (Column25.equalsIgnoreCase("")){
			                		Column25 = cell.getStringCellValue().trim();
			                	}else if (Column26.equalsIgnoreCase("")){
			                		Column26 = cell.getStringCellValue().trim();
			                	}else if (Column27.equalsIgnoreCase("")){
			                		Column27 = cell.getStringCellValue().trim();
			                	}else if (Column28.equalsIgnoreCase("")){		                			
			                			Column28 = cell.getStringCellValue().trim();
			                	}else{
		                    System.out.print("Random STuff::" + cell.getStringCellValue() + "\t\t");
			                	}
			                    break;
			            }
			        }
			        jobRecord = new JobRecord();
			        System.out.println("");
			             System.out.println(" Begin " + Column1 + " " + Column2 + " " + Column3 + " " + Column4 + " " + Column5 + " " + Column6);              
			    } 
			 }// sheets loop
				     
			    file.close();
			    FileOutputStream out = 
			        new FileOutputStream(new File(event.getFile().getFileName()).getAbsoluteFile());
			    wb.write(out);
			    out.close();
		       
			} catch (FileNotFoundException e) {
			    e.printStackTrace();
			} catch (IOException e) {
			    e.printStackTrace();
			}
          try {
				FileFlat();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           
    }  

    public void copyFile(String fileName, InputStream in) throws NoSuchAlgorithmException, SerialException, SQLException {
    	       MessageDigest md = MessageDigest.getInstance("MD5");
           try {
                             
                // write the inputStream to a FileOutputStream
                OutputStream out = new FileOutputStream(destination + fileName);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();               
                System.out.println(destination + fileName);
                System.out.println("This is the size of the file " + fileName.length());
                System.out.println("The size is " + in.available());
                int read = 0;
                byte[] bytes = new byte[1024];
             
                while ((read = in.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                    bos.write(bytes);   
                }
                bytes = md.digest();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < bytes.length; i++) {
                 sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                 
                }
                
                lilb = sb.toString();
                clobFile = bos.toByteArray();
                System.out.println("Digest(in hex format):: " + sb.toString());
                System.out.println(clobFile); 
                System.out.println(clobFile.equals(bos));
                System.out.println(bos.size());
                in.close();
                out.flush();
                out.close();
             
                System.out.println("New file created!");
                } catch (IOException e) {
                System.out.println(e.getMessage());
                }
           
         
    } 
    
    public void contFileUpload(FileUploadEvent event) throws NoSuchAlgorithmException, SerialException, SQLException{
    	    log.info("Contract upload files");
		    
	        FacesMessage msg = new FacesMessage("Succesful Contract File " + event.getFile().getFileName() + " is uploaded.");  
		    FacesContext.getCurrentInstance().addMessage(null, msg);
		    newNameFile = event.getFile().getFileName();
		    try {
	            copyFile(event.getFile().getFileName(), event.getFile().getInputstream());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		    
		     String fileExtn = GetFileExtension(event.getFile().getFileName());
	          Workbook wb = null; //Declare Workbook
	          Sheet sheet = null; //Declare Sheets
	          
	          try {
	        	   FileInputStream file = new FileInputStream(destination + event.getFile().getFileName());
				     
				    if(fileExtn.equalsIgnoreCase("xlsx")){
						   wb = new XSSFWorkbook(file);
						   log("xlsx="+wb.getSheetName(0));
					   }else if(fileExtn.equalsIgnoreCase("xls")){
						 POIFSFileSystem fs = new POIFSFileSystem(file);
						 wb = new HSSFWorkbook(fs);
						 log("xls="+wb.getSheetName(0));
					   }
					     int sheets = wb.getNumberOfSheets();			     
					     for(int i =0; i< sheets; i++) {
					      
					    	 sheet = wb.getSheetAt(i);
				   
			   				     
				    //Iterate through each rows from first sheet
					    	 Iterator<Row> rowIterator = sheet.iterator();
							    while(rowIterator.hasNext()) {
							        Row row = rowIterator.next();
							          
							          String Column1 = "";
								      String Column2 = "";
								      String Column3 = "";
								      String Column4 = "";
								      String Column5 = "";
							          String Column6 = "";
							          							         
							        //For each row, iterate through each columns
							        Iterator<Cell> cellIterator = row.cellIterator();
							        while(cellIterator.hasNext()) {
							             
							            Cell cell = cellIterator.next();
							             
							            switch(cell.getCellType()) {
							                case Cell.CELL_TYPE_BOOLEAN:
							                    System.out.print(cell.getBooleanCellValue() + "\t\t");
							                    break;
							                case Cell.CELL_TYPE_NUMERIC:
							                	if (DateUtil.isCellDateFormatted(cell)) {
							                		contractDate = cell.getDateCellValue();
							                		contractRecord.setContractDate(contractDate);
							                        System.out.println("");
							                    } else {
							                        System.out.println(cell.getNumericCellValue());
							                    }
							              
							                    break;
							                case Cell.CELL_TYPE_STRING:
							                	if(Column1.equalsIgnoreCase("")){
							                		Column1 = cell.getStringCellValue().trim();
							                		System.out.print(Column1);
							                		contractRecord.setContractTitle(Column1);
							                	}
							                	else if(Column2.equalsIgnoreCase("")){
							                		Column2 = cell.getStringCellValue().trim();
							                		System.out.print(Column2);
							                		contractRecord.setContractAgency(Column2);
							                	}
							                	else if(Column3.equalsIgnoreCase("")){
							                		 Column3 = cell.getStringCellValue().trim();
							                		 businessRecord.setBusinessFirstName(Column3);
							                	}
							                	else if(Column4.equalsIgnoreCase("")){
							                		Column4 = cell.getStringCellValue().trim();
							                		businessRecord.setBusinessEmail(Column4);
							                	}
							                	else if (Column5.equalsIgnoreCase("")){
							                		Column5 = cell.getStringCellValue().trim();
							                	}
							                	else if(Column6.equalsIgnoreCase("")){
							                		Column6 = cell.getStringCellValue().trim();
							                	}
							                	else {
						                        System.out.print(" Random stuff : " + cell.getStringCellValue() + "\t\t");
							                	}
							                    break;
							            }
							        } 
							        br.add(businessRecord);
							        cr.add(contractRecord);	
							        contractRecord = new ContractRecord();
							        businessRecord = new BusinessRecord();
							        
							       }
							    
					     } 
					     /*for (ContractRecord r : cr){
					    	 System.out.println("My contents : " + r.getContractTitle() + " " + r.getContractAgency() + " " + r.getContractDate());
					     }
					      for (BusinessRecord b : br){
					    	  System.out.print(b.getBusinessFirstName() + " " + b.getBusinessEmail());
					      }*/
					       System.out.println(" ");
					      
				    file.close();
				    FileOutputStream out = 
				        new FileOutputStream(new File(event.getFile().getFileName()).getAbsoluteFile());
				    wb.write(out);
				    out.close();
				     
				} catch (FileNotFoundException e) {
				    e.printStackTrace();
				} catch (IOException e) {
				    e.printStackTrace();
				}
	          		      
	           try {
				FileFlat();
				messengerService.saveBusinessFile(br);
		        messengerService.saveContractFile(cr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     

    }// end contFileUploader
    
     public void FileFlat() throws Exception{ 
    	  flatfile = new FlatFile();
    	  newName = lilb;    		
    	  oldName = newNameFile;
    	  flatfile.setClobFile(clobFile);
    	  flatfile.setNewName(newName);
    	  flatfile.setOldName(oldName);
    	  flat.add(flatfile);    	  
    		
    	  System.out.println("the flat file: " + clobFile +  " : " + newName + " :: " + oldName); 
    	//  messengerService.saveFlatFile(flat);
     }
	
	 
	 public void candFileUpload(FileUploadEvent event) throws IOException{
		   log.info("I get Called to upload files");
		    
	        FacesMessage msg = new FacesMessage("Succesful FILE " + event.getFile().getFileName() + " is uploaded.");  
		    FacesContext.getCurrentInstance().addMessage(null, msg);
	       
		    try {
		    	File targetFolder = new File(destination + event.getFile().getFileName());
		    	InputStream inputStream = event.getFile().getInputstream();
		    	OutputStream out = new FileOutputStream(new File(targetFolder,
		    	event.getFile().getFileName()));
		    	int read = 0;
		    	byte[] bytes = new byte[1024];
		    	while ((read = inputStream.read(bytes)) != -1) {
		    	out.write(bytes, 0, read);
		    	}
		    	inputStream.close();
		       	out.flush();
		    	out.close();
		    	} catch (IOException e) {
		    	e.printStackTrace();
		    	}
		    	
	 }    
	      
	  public void radiobutton(){
		 		  
		  switch (number){
		  case 0:
			  break;
		  case 1: showFileUpLoaderDisable = Boolean.TRUE;
		          canFileUpLoaderDisable = Boolean.FALSE; 
		          showContractUpLoaderDisable = Boolean.FALSE;
		          Long t = (long) 1.0;
		          category.setCatID(t);
		          messengerService.passID(category);
			  break;
		  case 2: canFileUpLoaderDisable = Boolean.TRUE;
		          showFileUpLoaderDisable = Boolean.FALSE;
		          showContractUpLoaderDisable = Boolean.FALSE;
		          long tt = (long) 2.0;
		          category.setCatID(tt);
		          messengerService.passID(category);
		      break;
		  case 3: showContractUpLoaderDisable = Boolean.TRUE;  
		          showFileUpLoaderDisable = Boolean.FALSE;
		          canFileUpLoaderDisable = Boolean.FALSE;
		          long ttt = (long) 3.0;
		          category.setCatID(ttt);
		          messengerService.passID(category);
			  break;
		  }
	  }
		  
	// Delete this method only a test
	  
	public void TestOne(){
	//	System.out.println("Yes I get triggered");
	}
	
	
	//Login Method
	public void Login(ActionEvent e) throws java.io.IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect("/massMessenger/j_spring_security_check?j_username=" + userId + "&j_password=" + password);
		 System.out.println("DO I get Called");
         System.out.println("The User name is: " + userId + " password : " + password);
        
    }
	 
			
	 public void save(ActionEvent actionEvent) {  
	        //Persist user  
		    candidateInfo = new CandidateInfo();
		  
		    FacesMessage msg = new FacesMessage("Successful", "Welcome :"  );  
	        FacesContext.getCurrentInstance().addMessage(null, msg);  
	
	        currentLevel = 1; 
	        clearValues();
	    }  
	 
	public String saveContractData(){
		log.info("saving the following data");
		log.info(contractTitle);
		log.info(contractAgency);

		contractRecord.setContractTitle(contractTitle);
		contractRecord.setContractAgency(contractAgency);
		
		messengerService.saveContract(contractRecord);
		log.info(" new contract data saved");
		addMessage("Successfully Saved: "+ selectedContract.getContractTitle() + " record!");
		clearValues();
		return null;
	}
	// Delete if not being used
	 public String onFlowProcess(FlowEvent event) { 
	    	log.info("Current wizard step:" + event.getOldStep());  
	        log.info("Next step:" + event.getNewStep());  
	          
	        if(skip) {  
	            skip = false;   //reset in case user goes back  
	            return "confirm";  
	        }  
	        else {  
	            return event.getNewStep();  
	        }  
	    }  
	
	
	// Delete if not being used..
     public void save() { 		  
	        FacesContext context = FacesContext.getCurrentInstance(); 	       
	        context.addMessage(null, new FacesMessage("Successfully Updated: "+ selectedContract.getContractTitle() + " record!" ));  
	         
	    }  
	
    	  
     public void addMessage(String summary) {  
	        //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null);  
	        FacesContext context = FacesContext.getCurrentInstance();
	        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary,  null));  
	    }  

	public String addLinkedCanandJobs(){
		
		lpAEMP = new EmailRepo();
		
		for(CandidateInfo cr : allSelectedCandidateInfo){
			lpAEMP = new EmailRepo();
			
			lpAEMP.setJobID(selectedJob.getJobID());
			log.info ("Job ID: " + lpAEMP.getJobID());
			lpAEMP.setJobTitle(selectedJob.getJobTitle());
			log.info ("Job Title: " + lpAEMP.getJobTitle());
			lpAEMP.setJobDescription(selectedJob.getJobDescription());
			log.info ("Job Description: " + lpAEMP.getJobDescription());
			lpAEMP.setCandidateEmail(cr.getCandinfoEmail());
			log.info ("Candidate Email: " + lpAEMP.getCandidateEmail());
			lpAEMP.setCandidateFirstName(cr.getCandinfoFirstName());
			log.info ("Candidate First Name: " + lpAEMP.getCandidateFirstName());
			lpAEMP.setCandidateLastName(cr.getCandinfoLastName());
			log.info ("Candidate Last Name: " + lpAEMP.getCandidateLastName());
			lpAEMP.setCandidateID(cr.getCandinfo_id());
			log.info ("Candidate ID: " + lpAEMP.getCandidateID());
			
		allAddedEmailsInfo.add(lpAEMP);
			
		}
		
		
		allSelectedCandidateInfo = new ArrayList<CandidateInfo>();
		selectedJob = new JobRecord();
		showSelectedLst = Boolean.TRUE;
		showSelectedLstDisable = Boolean.FALSE;
		
		addMessage("Selected Data stored! Please click on the Selected Data tab to continue with mass email.");
		
		return null;
	}
	
	public String sendToEmailService(){
		
		
		
		for(EmailRepo er : allAddedEmailsInfo){
		
		mailService.sendMail("careers@acnc-md.com", er.getCandidateEmail(), "Urgent Job Opening for " + er.getJobTitle(), 
				" Dear " + er.getCandidateFirstName() +" " + er.getCandidateLastName() + ",\n\n" +
						" I am a representative of Advanced Computer and Network\n" +
						" Concepts, LLC, our company is currently looking for talented,\n" +
						" dynamic, and motivated " + er.getJobTitle() + " to become part of our\n" +
						" technical team. After reviewing your resume on clearancejobs.com\n" +
						" we were very pleased with your skill set and your experience.\n\n" +
						" As part of our initial screening process, we would like for\n" +
						" you to provide the following information:\n\n" +
						" - what is your clearance level?\n" +
						" - are you still seeking new opportunities, if so what is your\n" +
						" availability?\n" +
						" - provide us with your most current resume.\n" +
						" - provide a good contact number and time to best reach you at.\n" +
						" - provide a desired salary range and/or hourly contracting 1099 rate.\n\n" +
		" We look forward to speaking with you further about potential\n" +
		" opportunities.\n" +
		" ACNC L.L.C. is a steadily growing small business that\n" +
		" specializes in intelligence, engineering, and information\n" +
		" technology. As a government contracting company, we have\n" +
		" subcontracts supporting clients throughout the Intelligence\n" +
		" Community in the MD, DC, and VA area. We maintain a small-\n" +
		"company feel while also offering benefits and opportunities\n" +
		" competitive with larger companies in the industry. A preview\n" +
		" of the offered benefits:\n\n " +
		"Medical insurance is the choice of a PPO plan or an Open\n" +
		" Access HMO plan with a health savings account funded 100% by\n" +
		" ACNC L.L.C..\n" +
		" ACNC L.L.C. pays 100% of employee health coverage.\n" +
		" ACNC L.L.C. matches your 401(K) up to 5% contribution after\n" +
		" inital 90 day start date.\n" +
		" ACNC L.L.C. pays up to $4,500 a year for any training and\n" +
		" education you want to receive.\n" +
		" 15 days paid vacation, 5 days sick leave, 24 hours of facility\n" +
		" closing paid leave (natural disasters and storms), and 10\n" +
		" holidays a year- all paid leave time.\n" +
		" Health insurance, life insurance, short and long term\n" +
		" disability plans.\n\n" +
		" Vr\n" +
		" Human Resource Representative\n" +
		" Advanced Computer and Network Concepts, L.L.C.\n" + 
		" 5707 Calverton St, Suite 2B\n" +
		" Catonsville, MD 21228\n" +
		" Voice:  410.294.8369 | 410.298.0887\n");
		
		//mailService.sendAlertMail("Exception occurred");

				
		}
		
		messengerService.saveEmailQueue(allAddedEmailsInfo);
		allAddedEmailsInfo = new ArrayList<EmailRepo>();
		allEmailPosted = new ArrayList<EmailRepo>();		
		/*allEmailPosted = messengerService.getAllEmailRepos();*/
		showSelectedLst = Boolean.FALSE;
		showSelectedLstDisable = Boolean.TRUE;
		addMessage("Selected Data sent out to the email service and sotred to reposiotry auto emails!");

		return null;
		
	}
	
	public String addLinkedConandBDD(){
		
		
		lpAEMP2 = new EmailRepo2();
		
		for(BusinessRecord br : allSelectedBusiness){
			lpAEMP2 = new EmailRepo2();
			
			lpAEMP2.setContractID(selectedContract.getContractID());
			log.info ("Contract ID: " + lpAEMP2.getContractID());
			lpAEMP2.setContractTitle(selectedContract.getContractTitle());
			log.info ("Contract Title: " + lpAEMP2.getContractTitle());
			lpAEMP2.setContractAgency(selectedContract.getContractAgency());
			log.info ("Contract Agency: " + lpAEMP2.getContractAgency());
			lpAEMP2.setBusinessEmail(br.getBusinessEmail());
			log.info ("Business Email: " + lpAEMP2.getBusinessEmail());
			lpAEMP2.setBusinessFirstName(br.getBusinessFirstName());
			log.info ("Business First Name: " + lpAEMP2.getBusinessFirstName());
			lpAEMP2.setBusinessLastName(br.getBusinessLastName());
			log.info ("Business Last Name: " + lpAEMP2.getBusinessLastName());
			lpAEMP2.setBusinessID(br.getBusinessID());
			log.info ("Business ID: " + lpAEMP2.getBusinessID());
			
		allAddedEmailsInfo2.add(lpAEMP2);
			
		}
		
		allSelectedBusiness = new ArrayList<BusinessRecord>();
		
		selectedContract = new ContractRecord();
		setShowSelectedLst2(Boolean.TRUE);
		setShowSelectedLstDisable2(Boolean.FALSE);
		
		addMessage("Selected Data stored! Please click on the Selected Data tab to continue with mass email.");
		
		return null;
	}
	
    public String sendToEmailService2(){
		
		
		
		for(EmailRepo2 er : allAddedEmailsInfo2){
		
		mailService.sendMail("careers@acnc-md.com", er.getBusinessEmail(), "Urgent Job Opening for " + er.getContractTitle(), 
				" Hello " + er.getBusinessFirstName() +" " + er.getBusinessLastName() + ",\n\n" +
						" I would like to take this opportunity to introduce myself. My name is " +
						"Kunta A. Little and I represent a small minority veteran " +
						"owned company called Advanced Computer and Network " +
						"Concepts (ACNC), L.L.C.  My counterparts and I would like to " +
						"discuss teaming opportunities with your company for services we can " +
						"render to assist you on your new " + er.getContractTitle() + " contract.  " +
						"We are eager to discuss any current and future " + er.getBusinessFirstName()  + " teaming " +
						"opportunities with your company.  Thank you for your time and we look " +
						"forward to partnering with you and your company." +
						"Attached you will find a copy of Advanced Computer and Network " +
						"Concepts (ACNC), L.L.C. Capability Statement.\n\n " +
						
		
		" Vr\n" +
		" Human Resource Representative\n" +
		" Advanced Computer and Network Concepts, L.L.C.\n" + 
		" 5707 Calverton St, Suite 2B\n" +
		" Catonsville, MD 21228\n" +
		" Voice:  410.294.8369 | 410.298.0887\n");
		
		//mailService.sendAlertMail("Exception occurred");

				
		}
		
		messengerService.saveEmailQueue2(allAddedEmailsInfo2);
		allAddedEmailsInfo2 = new ArrayList<EmailRepo2>();
		allEmailPosted2 = new ArrayList<EmailRepo2>();		
		/*allEmailPosted2 = messengerService.getAllEmailRepos2();*/
		showSelectedLst2 = Boolean.FALSE;
		showSelectedLstDisable2 = Boolean.TRUE;
		addMessage("Selected Data sent out to the email service and sotred to reposiotry auto emails!");

		return null;
		
	}
	
	public void resetValues() {	
		
		log.info ("Resetting values");
		
		allJobPosted = new ArrayList<JobRecord>();
		
		/*allJobPosted = messengerService.getAllJobs();
		*/
		allContractPosted = new ArrayList<ContractRecord>();
		
		/*allContractPosted = messengerService.getAllContracts();*/
		
		allEducationPosted = new ArrayList<EducationResume>();
	    
	   /* allEducationPosted = messengerService.getAllEducation();*/
		
		allEmployPosted = new ArrayList<EmployResume>();
		
		/*allEmployPosted = messengerService.getAllEmploy();*/
		
		allCertPosted = new ArrayList<CertResume>();
		
		/*allCertPosted = messengerService.getAllCert();*/
		
		allSelectedCerts = new ArrayList<CertResume>();
		
		/*allSelectedCerts = messengerService.getSelectedCertsInfo(passID);*/
		
		selectedCerts = new ArrayList<CertResume>();
		
		allBusinessPosted = new ArrayList<BusinessRecord>();
		
		/*allBusinessPosted = messengerService.getAllBusiness();*/
		
        allQueryRecord = new ArrayList<CandidateInfo>();
		
		/*allQueryRecord = messengerService.getAllQueryRecord();	*/	
		
		allCandidateInfoPosted = new ArrayList<CandidateInfo>();
		
		/*allCandidateInfoPosted = messengerService.getAllCandidateInfo();*/
		
		educationResume = new EducationResume();
		
		certResume = new CertResume();
		
		employResume = new EmployResume();
		
		candidateInfo = new CandidateInfo();
		
		selectedJob = new JobRecord();
		
		selectedBusiness = new BusinessRecord();
		
		selectedContract = new ContractRecord();
		
		selectedCandidateInfo = new CandidateInfo();
		
		selectedCertResume = new CertResume();
		
		jobRecord = new JobRecord();
		
		contractRecord = new ContractRecord();
		
		businessRecord = new BusinessRecord();
		
		updateContract = new UpdateContract(); 	
		
		candidateFirstName = null;
		
		candidateLastName = null;
		
		candidateEmail = null;
		
		jobDescription = null;
		
		jobTitle = null;
		
		degree = null;
		
		schoolName = null;
		
		major = null;
		
		compYear = null;
		
		certName = null;
		
		certYear = null;
		
		compName = null;
		
		title = null;
		
		projectRole = null;
		
		projDescpt = null; 
		
		fromMonth = null;
		
		fromYear = null;
		
		toMonth = null;
		
		toYear = null;
		
		projfromMonth = null;
		
		projfromYear = null;
		
		projtoMonth = null;
		
		projtoYear = null;
		
        candinfoFirstName = null;
		
	    candinfoLastName = null;
		
	    candinfoMiddle = null;
		
	    candinfoEmail = null;
		
	    candinfoQuestone = null;
		
	    candinfoQuesttwo = null;
		
	    candinfoQuesthree = null; 
		
	    candinfoQuestfour = null;
		
		candinfoQuestfive = null;
		
		candinfoSkills = null;
		
		catName = null;
		
		catDescrp = null;
		
		cateDate = null;
		
		fileClob = null; 
		
		newName = null;
		
		oldName = null;
		
		dateCreated = null;
		
		log.info ("Successfully reset Records object.");
	}
	
	public String clearValues() {
		resetValues();
		return "next";
	}
	
	
	
	public String getCatName() {
		return catName;
	}

	public void setCatName(String catName) {
		this.catName = catName;
	}

	public String getCatDescrp() {
		return catDescrp;
	}

	public void setCatDescrp(String catDescrp) {
		this.catDescrp = catDescrp;
	}

	public Date getCateDate() {
		return cateDate;
	}

	public void setCateDate(Date cateDate) {
		this.cateDate = cateDate;
	}

	public Blob getFileClob() {
		return fileClob;
	}

	public void setFileClob(Blob fileClob) {
		this.fileClob = fileClob;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public MessengerService getMessengerService() {
		return messengerService;
	}

	public void setMessengerService(MessengerService messengerService) {
		this.messengerService = messengerService;
	}

	public List<JobRecord> getAllJobPosted() {
		return allJobPosted;
	}

	public void setAllJobPosted(List<JobRecord> allJobPosted) {
		this.allJobPosted = allJobPosted;
	}
	
	public JobRecord getJobRecord() {
		return jobRecord;
	}

	public void setJobRecord(JobRecord jobRecord) {
		this.jobRecord = jobRecord;
	}

	public Boolean getShowEmailPushOption() {
		return showEmailPushOption;
	}

	public void setShowEmailPushOption(Boolean showEmailPushOption) {
		this.showEmailPushOption = showEmailPushOption;
	}

	public Boolean getShowMainData() {
		return showMainData;
	}

	public void setShowMainData(Boolean showMainData) {
		this.showMainData = showMainData;
	}

	public Boolean getShowJobDataInput() {
		return showJobDataInput;
	}

	public void setShowJobDataInput(Boolean showJobDataInput) {
		this.showJobDataInput = showJobDataInput;
	}

	public Boolean getShowCanDataInput() {
		return showCanDataInput;
	}

	public void setShowCanDataInput(Boolean showCanDataInput) {
		this.showCanDataInput = showCanDataInput;
	}

	public String getCandidateFirstName() {
		return candidateFirstName;
	}

	public void setCandidateFirstName(String candidateFirstName) {
		this.candidateFirstName = candidateFirstName;
	}

	public String getCandidateLastName() {
		return candidateLastName;
	}

	public void setCandidateLastName(String candidateLastName) {
		this.candidateLastName = candidateLastName;
	}

	public String getCandidateEmail() {
		return candidateEmail;
	}

	public void setCandidateEmail(String candidateEmail) {
		this.candidateEmail = candidateEmail;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	public List<EmailRepo> getAllEmailPosted() {
		return allEmailPosted;
	}

	public void setAllEmailPosted(List<EmailRepo> allEmailPosted) {
		this.allEmailPosted = allEmailPosted;
	}

	public List<EmailRepo> getAllAddedEmailsInfo() {
		return allAddedEmailsInfo;
	}

	public void setAllAddedEmailsInfo(List<EmailRepo> allAddedEmailsInfo) {
		this.allAddedEmailsInfo = allAddedEmailsInfo;
	}
	
	public List<EmailRepo2> getAllEmailPosted2() {
		return allEmailPosted2;
	}

	public void setAllEmailPosted2(List<EmailRepo2> allEmailPosted2) {
		this.allEmailPosted2 = allEmailPosted2;
	}

	public List<EmailRepo2> getAllAddedEmailsInfo2() {
		return allAddedEmailsInfo2;
	}

	public void setAllAddedEmailsInfo2(List<EmailRepo2> allAddedEmailsInfo2) {
		this.allAddedEmailsInfo2 = allAddedEmailsInfo2;
	}

	public EmailRepo2 getLpAEMP2() {
		return lpAEMP2;
	}

	public void setLpAEMP2(EmailRepo2 lpAEMP2) {
		this.lpAEMP2 = lpAEMP2;
	}
	
	public List<JobRecord> getAllSelectedJobs() {
		return allSelectedJobs;
	}

	public void setAllSelectedJobs(List<JobRecord> allSelectedJobs) {
		this.allSelectedJobs = allSelectedJobs;
	}

	public JobRecord getSelectedJob() {
		return selectedJob;
	}

	public void setSelectedJob(JobRecord selectedJob) {
		this.selectedJob = selectedJob;
	}

	public EmailRepo getLpAEMP() {
		return lpAEMP;
	}

	public void setLpAEMP(EmailRepo lpAEMP) {
		this.lpAEMP = lpAEMP;
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public Boolean getShowSelectedLst() {
		return showSelectedLst;
	}

	public void setShowSelectedLst(Boolean showSelectedLst) {
		this.showSelectedLst = showSelectedLst;
	}

	public Boolean getShowSelectedLstDisable() {
		return showSelectedLstDisable;
	}

	public void setShowSelectedLstDisable(Boolean showSelectedLstDisable) {
		this.showSelectedLstDisable = showSelectedLstDisable;
	}
	
	
	public UpdateCandidateInfo getUpdateCandidateinfo() {
		return updateCandidateinfo;
	}

	public void setUpdateCandidateinfo(UpdateCandidateInfo updateCandidateinfo) {
		this.updateCandidateinfo = updateCandidateinfo;
	}

	public Boolean getShowFileUpLoaderDisable() {
		return showFileUpLoaderDisable;
	}

	public void setShowFileUpLoaderDisable(Boolean showFileUpLoaderDisable) {
		this.showFileUpLoaderDisable = showFileUpLoaderDisable;
	}
	
	
	public Boolean getCanFileUpLoaderDisable() {
		return canFileUpLoaderDisable;
	}

	public void setCanFileUpLoaderDisable(Boolean canFileUpLoaderDisable) {
		this.canFileUpLoaderDisable = canFileUpLoaderDisable;
	}
	
	

	public Boolean getShowContractUpLoaderDisable() {
		return showContractUpLoaderDisable;
	}

	public void setShowContractUpLoaderDisable(Boolean showContractUpLoaderDisable) {
		this.showContractUpLoaderDisable = showContractUpLoaderDisable;
	}

	public JobRecord getSelectedJobs() {
		return selectedJobs;
	}

	public void setSelectedJobs(JobRecord selectedJobs) {
		this.selectedJobs = selectedJobs;
	}

	public List<ContractRecord> getAllContractPosted() {
		return allContractPosted;
	}

	public void setAllContractPosted(List<ContractRecord> allContractPosted) {
		this.allContractPosted = allContractPosted;
	}

	public List<ContractRecord> getAllSelectedContracts() {
		return allSelectedContracts;
	}

	public void setAllSelectedContracts(List<ContractRecord> allSelectedContracts) {
		this.allSelectedContracts = allSelectedContracts;
	}
	
	
	public String getContractTitle() {
		return contractTitle;
	}

	public void setContractTitle(String contractTitle) {
		this.contractTitle = contractTitle;
	}

	public BusinessRecord getBusinessRecord() {
		return businessRecord;
	}

	public void setBusinessRecord(BusinessRecord businessRecord) {
		this.businessRecord = businessRecord;
	}

	public String getBusinessFirstName() {
		return businessFirstName;
	}

	public void setBusinessFirstName(String businessFirstName) {
		this.businessFirstName = businessFirstName;
	}

	public String getBusinessLastName() {
		return businessLastName;
	}

	public void setBusinessLastName(String businessLastName) {
		this.businessLastName = businessLastName;
	}

	public String getBusinessEmail() {
		return businessEmail;
	}

	public void setBusinessEmail(String businessEmail) {
		this.businessEmail = businessEmail;
	}

	public List<BusinessRecord> getAllBusinessPosted() {
		return allBusinessPosted;
	}

	public void setAllBusinessPosted(List<BusinessRecord> allBusinessPosted) {
		this.allBusinessPosted = allBusinessPosted;
	}

	public UpdateCandidate getUpdateCandidate() {
		return updateCandidate;
	}

	public void setUpdateCandidate(UpdateCandidate updateCandidate) {
		this.updateCandidate = updateCandidate;
	}

	public DeleteCandidate getDeleteCandidate() {
		return deleteCandidate;
	}

	public void setDeleteCandidate(DeleteCandidate deleteCandidate) {
		this.deleteCandidate = deleteCandidate;
	}

	public List<BusinessRecord> getAllSelectedBusiness() {
		return allSelectedBusiness;
	}

	public void setAllSelectedBusiness(List<BusinessRecord> allSelectedBusiness) {
		this.allSelectedBusiness = allSelectedBusiness;
	}

	public String getBusinessPhoneNumber() {
		return businessPhoneNumber;
	}

	public void setBusinessPhoneNumber(String businessPhoneNumber) {
		this.businessPhoneNumber = businessPhoneNumber;
	}

	public BusinessRecord getSelectedBusiness() {
		return selectedBusiness;
	}

	public void setSelectedBusiness(BusinessRecord selectedBusiness) {
		this.selectedBusiness = selectedBusiness;
	}

	public UpdateContract getUpdateContract() {
		return updateContract;
	}

	public void setUpdateContract(UpdateContract updateContract) {
		this.updateContract = updateContract;
	}

	public ContractRecord getSelectedContract() {
		return selectedContract;
	}

	public void setSelectedContract(ContractRecord selectedContract) {
		this.selectedContract = selectedContract;
	}

	public UpdateBusiness getUpdateBusiness() {
		return updateBusiness;
	}

	public void setUpdateBusiness(UpdateBusiness updateBusiness) {
		this.updateBusiness = updateBusiness;
	}

	public DeleteContract getDeleteContract() {
		return deleteContract;
	}

	public void setDeleteContract(DeleteContract deleteContract) {
		this.deleteContract = deleteContract;
	}

	public DeleteBusiness getDeleteBusiness() {
		return deleteBusiness;
	}

	public void setDeleteBusiness(DeleteBusiness deleteBusiness) {
		this.deleteBusiness = deleteBusiness;
	}

	public ContractRecord getContractRecord() {
		return contractRecord;
	}

	public void setContractRecord(ContractRecord contractRecord) {
		this.contractRecord = contractRecord;
	}

	public String getContractAgency() {
		return contractAgency;
	}

	public void setContractAgency(String contractAgency) {
		this.contractAgency = contractAgency;
	}

	public ContractRecord getSelectedContracts() {
		return selectedContracts;
	}

	public void setSelectedContracts(ContractRecord selectedContracts) {
		this.selectedContracts = selectedContracts;
	}

	public Boolean getShowSelectedLst2() {
		return showSelectedLst2;
	}

	public void setShowSelectedLst2(Boolean showSelectedLst2) {
		this.showSelectedLst2 = showSelectedLst2;
	}

	public Boolean getShowSelectedLstDisable2() {
		return showSelectedLstDisable2;
	}

	public void setShowSelectedLstDisable2(Boolean showSelectedLstDisable2) {
		this.showSelectedLstDisable2 = showSelectedLstDisable2;
	}

	public CandidateInfo getCandidateInfo() {
		return candidateInfo;
	}

	public void setCandidateInfo(CandidateInfo candidateInfo) {
		this.candidateInfo = candidateInfo;
	}

	
	public EducationResume getEducationResume() {
		return educationResume;
	}

	public void setEducationResume(EducationResume educationResume) {
		this.educationResume = educationResume;
	}

	public EmployResume getEmployResume() {
		return employResume;
	}

	public void setEmployResume(EmployResume employResume) {
		this.employResume = employResume;
	}

			
	public Boolean getAddingEducation() {
		return addingEducation;
	}

	public void setAddingEducation(Boolean addingEducation) {
		this.addingEducation = addingEducation;
	}

	public List<CandidateInfo> getCnd() {
		return cnd;
	}

	public void setCnd(List<CandidateInfo> cnd) {
		this.cnd = cnd;
	}

	public Boolean getAddingResume() {
		return addingResume;
	}

	public void setAddingResume(Boolean addingResume) {
		this.addingResume = addingResume;
	}

	public List<CertResume> getCert() {
		return cert;
	}

	public void setCert(List<CertResume> cert) {
		this.cert = (ArrayList<CertResume>) cert;
	}

	public CertResume getCertResume() {
		return certResume;
	}

	public void setCertResume(CertResume certResume) {
		this.certResume = certResume;
	}

	
	public Boolean getAddingEmployment() {
		return addingEmployment;
	}

	public void setAddingEmployment(Boolean addingEmployment) {
		this.addingEmployment = addingEmployment;
	}

	public Boolean getAddingCertification() {
		return addingCertification;
	}

	public void setAddingCertification(Boolean addingCertification) {
		this.addingCertification = addingCertification;
	}

	public List<EmployResume> getMainER() {
		return mainER;
	}

	public void setMainER(List<EmployResume> mainER) {
		this.mainER = mainER;
	}

	public List<CandidateInfo> getAllCandidateInfoPosted() {
		return allCandidateInfoPosted;
	}

	public void setAllCandidateInfoPosted(List<CandidateInfo> allCandidateInfoPosted) {
		this.allCandidateInfoPosted = allCandidateInfoPosted;
	}

	public List<EducationResume> getEmpEdu() {
		return empEdu;
	}

	public void setEmpEdu(List<EducationResume> empEdu) {
		this.empEdu = empEdu;
	}

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public String getSchoolName() {
		return schoolName;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public Date getCompYear() {
		return compYear;
	}

	public void setCompYear(Date compYear) {
		this.compYear = compYear;
	}

	public String getCertName() {
		return certName;
	}

	public void setCertName(String certName) {
		this.certName = certName;
	}

	public Date getCertYear() {
		return certYear;
	}

	public void setCertYear(Date certYear) {
		this.certYear = certYear;
	}

	public String getCompName() {
		return compName;
	}

	public void setCompName(String compName) {
		this.compName = compName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getProjectRole() {
		return projectRole;
	}

	public void setProjectRole(String projectRole) {
		this.projectRole = projectRole;
	}

	public String getProjDescpt() {
		return projDescpt;
	}

	public void setProjDescpt(String projDescpt) {
		this.projDescpt = projDescpt;
	}

	public Date getFromMonth() {
		return fromMonth;
	}

	public void setFromMonth(Date fromMonth) {
		this.fromMonth = fromMonth;
	}

	public String getFromYear() {
		return fromYear;
	}

	public void setFromYear(String fromYear) {
		this.fromYear = fromYear;
	}

	public String getToMonth() {
		return toMonth;
	}

	public void setToMonth(String toMonth) {
		this.toMonth = toMonth;
	}

	public Date getToYear() {
		return toYear;
	}

	public void setToYear(Date toYear) {
		this.toYear = toYear;
	}

	public Date getProjfromMonth() {
		return projfromMonth;
	}

	public void setProjfromMonth(Date projfromMonth) {
		this.projfromMonth = projfromMonth;
	}

	public String getProjfromYear() {
		return projfromYear;
	}

	public void setProjfromYear(String projfromYear) {
		this.projfromYear = projfromYear;
	}

	public String getProjtoMonth() {
		return projtoMonth;
	}

	public void setProjtoMonth(String projtoMonth) {
		this.projtoMonth = projtoMonth;
	}

	public Date getProjtoYear() {
		return projtoYear;
	}

	public void setProjtoYear(Date projtoYear) {
		this.projtoYear = projtoYear;
	}

	public String getCandinfoFirstName() {
		return candinfoFirstName;
	}

	public void setCandinfoFirstName(String candinfoFirstName) {
		this.candinfoFirstName = candinfoFirstName;
	}

	public String getCandinfoLastName() {
		return candinfoLastName;
	}

	public void setCandinfoLastName(String candinfoLastName) {
		this.candinfoLastName = candinfoLastName;
	}

	public String getCandinfoMiddle() {
		return candinfoMiddle;
	}

	public void setCandinfoMiddle(String candinfoMiddle) {
		this.candinfoMiddle = candinfoMiddle;
	}

	public String getCandinfoEmail() {
		return candinfoEmail;
	}

	public void setCandinfoEmail(String candinfoEmail) {
		this.candinfoEmail = candinfoEmail;
	}

	public String getCandinfoQuestone() {
		return candinfoQuestone;
	}

	public void setCandinfoQuestone(String candinfoQuestone) {
		this.candinfoQuestone = candinfoQuestone;
	}

	public String getCandinfoQuesttwo() {
		return candinfoQuesttwo;
	}

	public void setCandinfoQuesttwo(String candinfoQuesttwo) {
		this.candinfoQuesttwo = candinfoQuesttwo;
	}

	public String getCandinfoQuesthree() {
		return candinfoQuesthree;
	}

	public void setCandinfoQuesthree(String candinfoQuesthree) {
		this.candinfoQuesthree = candinfoQuesthree;
	}

	public String getCandinfoQuestfour() {
		return candinfoQuestfour;
	}

	public void setCandinfoQuestfour(String candinfoQuestfour) {
		this.candinfoQuestfour = candinfoQuestfour;
	}

	public String getCandinfoQuestfive() {
		return candinfoQuestfive;
	}

	public void setCandinfoQuestfive(String candinfoQuestfive) {
		this.candinfoQuestfive = candinfoQuestfive;
	}

	public String getCandinfoSkills() {
		return candinfoSkills;
	}

	public void setCandinfoSkills(String candinfoSkills) {
		this.candinfoSkills = candinfoSkills;
	}

	public int getCurrentLevel() {  
        return currentLevel;  
    }  
  
    public void setCurrentLevel(int currentLevel) {  
        this.currentLevel = currentLevel;  
    }

	public CandidateInfo getSelectedCandidateInfo() {
		return selectedCandidateInfo;
	}

	public void setSelectedCandidateInfo(CandidateInfo selectedCandidateInfo) {
		this.selectedCandidateInfo = selectedCandidateInfo;
	}
	
	public CertResume getSelectedCertResume() {
		return selectedCertResume;
	}

	public void setSelectedCertResume(CertResume selectedCertResume) {
		this.selectedCertResume = selectedCertResume;
	}

	public List<CandidateInfo> getAllSelectedCandidateInfo() {
		return allSelectedCandidateInfo;
	}

	public void setAllSelectedCandidateInfo(
			List<CandidateInfo> allSelectedCandidateInfo) {
		this.allSelectedCandidateInfo = allSelectedCandidateInfo;
	}

	public List<EducationResume> getAllEducationPosted() {
		return allEducationPosted;
	}

	public void setAllEducationPosted(List<EducationResume> allEducationPosted) {
		this.allEducationPosted = allEducationPosted;
	}

	public List<EmployResume> getAllEmployPosted() {
		return allEmployPosted;
	}

	public void setAllEmployPosted(List<EmployResume> allEmployPosted) {
		this.allEmployPosted = allEmployPosted;
	}

	public List<CertResume> getAllCertPosted() {
		return allCertPosted;
	}

	public void setAllCertPosted(List<CertResume> allCertPosted) {
		this.allCertPosted = allCertPosted;
	}
	
	
	public List<CertResume> getAllSelectedCerts() {
		return allSelectedCerts;
	}

	public void setAllSelectedCerts(List<CertResume> allSelectedCerts) {
		this.allSelectedCerts = allSelectedCerts;
	}	
	

	public List<CertResume> getSelectedCerts() {
		return selectedCerts;
	}

	public void setSelectedCerts(List<CertResume> selectedCerts) {
		this.selectedCerts = selectedCerts;
	}

	public List<CertResume> getAllQueryCertPosted() {
		return allQueryCertPosted;
	}

	public void setAllQueryCertPosted(List<CertResume> allQueryCertPosted) {
		this.allQueryCertPosted = allQueryCertPosted;
	}

	public List<CandidateInfo> getAllQueryRecord() {
		return allQueryRecord;
	}

	public void setAllQueryRecord(List<CandidateInfo> allQueryRecord) {
		this.allQueryRecord = allQueryRecord;
	}

	public Long getCandinfo_id() {
		return candinfo_id;
	}

	public void setCandinfo_id(Long candinfo_id) {
		this.candinfo_id = candinfo_id;
	}

	public Long getEdID() {
		return edID;
	}

	public void setEdID(Long edID) {
		this.edID = edID;
	}

	public Long getCerID() {
		return cerID;
	}

	public void setCerID(Long cerID) {
		this.cerID = cerID;
	}

	public Long getEmployID() {
		return employID;
	}

	public void setEmployID(Long employID) {
		this.employID = employID;
	}

	public Long getPassID() {
		return passID;
	}

	public void setPassID(Long passID) {
		this.passID = passID;
	}

	public List<EducationResume> getAllSelectedEducation() {
		return allSelectedEducation;
	}

	public void setAllSelectedEducation(List<EducationResume> allSelectedEducation) {
		this.allSelectedEducation = allSelectedEducation;
	}

	public List<EmployResume> getAllSelectedEmploy() {
		return allSelectedEmploy;
	}

	public void setAllSelectedEmploy(List<EmployResume> allSelectedEmploy) {
		this.allSelectedEmploy = allSelectedEmploy;
	}

	public boolean isValueOne() {
		return valueOne;
	}

	public void setValueOne(boolean valueOne) {
		this.valueOne = valueOne;
	}

	public boolean isValueTwo() {
		return valueTwo;
	}

	public void setValueTwo(boolean valueTwo) {
		this.valueTwo = valueTwo;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getfNameFile() {
		return fNameFile;
	}

	public void setfNameFile(String fNameFile) {
		this.fNameFile = fNameFile;
	}

	public Date getContractDate() {
		return contractDate;
	}

	public void setContractDate(Date contractDate) {
		this.contractDate = contractDate;
	}

	public List<ContractRecord> getCr() {
		return cr;
	}

	public void setCr(List<ContractRecord> cr) {
		this.cr = cr;
	}

	public List<BusinessRecord> getBr() {
		return br;
	}

	public void setBr(List<BusinessRecord> br) {
		this.br = br;
	}

	public List<Category> getCat() {
		return cat;
	}

	public void setCat(List<Category> cat) {
		this.cat = cat;
	}

	public List<FlatFile> getFlat() {
		return flat;
	}

	public void setFlat(List<FlatFile> flat) {
		this.flat = flat;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public FlatFile getFlatfile() {
		return flatfile;
	}

	public void setFlatfile(FlatFile flatfile) {
		this.flatfile = flatfile;
	}

	
	public byte[] getClobFile() {
		return clobFile;
	}

	public void setClobFile(byte[] clobFile) {
		this.clobFile = clobFile;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public Date getJobDate() {
		return jobDate;
	}

	public void setJobDate(Date jobDate) {
		this.jobDate = jobDate;
	}

	public Date getJobStatusChange() {
		return jobStatusChange;
	}

	public void setJobStatusChange(Date jobStatusChange) {
		this.jobStatusChange = jobStatusChange;
	}

	public Long getJobRequest() {
		return jobRequest;
	}

	public void setJobRequest(Long jobRequest) {
		this.jobRequest = jobRequest;
	}

	public Boolean getShowEditButtonDisable() {
		return showEditButtonDisable;
	}

	public void setShowEditButtonDisable(Boolean showEditButtonDisable) {
		this.showEditButtonDisable = showEditButtonDisable;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	
	
	
}

