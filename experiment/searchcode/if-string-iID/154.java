package h4hdb.data;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import h4hdb.data.Address;
import h4hdb.data.Comment;
import h4hdb.data.Event;
import h4hdb.data.FamilyPartner;
import h4hdb.data.Person;
import h4hdb.data.Expression.MatchOperator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *Represents a inspection report as modeled by the database
 * 
 * @author daniel
 */
public class Inspection {
	
	public static final String FIELD_ID = "IID";
	public static final String FIELD_FAMILY_PARTNER_ID = "FID";
	public static final String FIELD_DATE = "Date";
	public static final String FIELD_LOUNGE_INSPECTION_ID = "LIID";
	public static final String FIELD_DINING_INSPECTION_ID = "DIID";
	public static final String FIELD_KITCHEN_INSPECTION_ID = "KIID";
	public static final String FIELD_BEDROOM1_INSPECTION_ID = "BR1IID";
	public static final String FIELD_BEDROOM2_INSPECTION_ID = "BR2IID";
	public static final String FIELD_BEDROOM3_INSPECTION_ID = "BR3IID";
	public static final String FIELD_BEDROOM4_INSPECTION_ID = "BR4IID";
	public static final String FIELD_BEDROOM5_INSPECTION_ID = "BR5IID";
	public static final String FIELD_BATHROOM_INSPECTION_ID = "BIID";
	public static final String FIELD_TOILET_INSPECTION_ID = "TIID";
	public static final String FIELD_LAUNDRY_INSPECTION_ID = "LdIID";
	public static final String FIELD_EXTERIOR_INSPECTION_ID = "EIID";
	public static final String FIELD_INSPECTOR_ID = "Inspector";
	public static final String FIELD_Occupant1_ID = "Occupant1";
	public static final String FIELD_Occupant2_ID = "Occupant2";
	
	
	private int IID; // inspection id, which is generated when a inspection is created on the database
    private FamilyPartner familyPartner; // family partnership
    private List<Person> familyMembers; 
    private Date dateOfInspection;
    private LoungeInspection lounge; // lounge inspection
    private DiningInspection dining; // dining inspection
    private KitchenInspection kitchen; // kitchen inspection
    private BedroomInspection bedroom1; // bedroom 1 inspection
    private BedroomInspection bedroom2; // bedroom 2 inspection
    private BedroomInspection bedroom3; // bedroom 3 inspection
    private BedroomInspection bedroom4; // bedroom 4 inspection
    private BedroomInspection bedroom5; // bedroom 5 inspection
	private BathroomInspection bathroom; // bathroom inspection
	private ToiletInspection toilet; // toilet inspection
	private LaundryInspection laundry; // laundry inspection
	private ExteriorInspection exterior; // exterior inspection
    private List<Comment> comments;
    private Person inspector; 
    private Person occupant1; 
    private Person occupant2; 
    private DB databaseAdapter; 
	
    /**
     * Base class for Inspection
     * 
     * @author daniel
     */
	private class InspectionTemplate {
		private int ID; 
		private String walls; 
		private String floors; 
		private String curtains; 
		private String light; 
		private String windows; 
	    private List<Comment> comments;
		/**
		 * Getter for ID
		 */
		public int getID(){return ID;}
		/**
		 * Setter for ID
		 */
		public InspectionTemplate setID(int ID){this.ID = ID; return this; } 
		/**
		 * Get walls info
		 */
		public String getWalls(){return walls;}
		/**
		 * Setter for Walls
		 */
		public InspectionTemplate setWalls(String walls){this.walls = walls; return this; } 
		/**
		 * Get floors
		 */
		public String getFloors(){return floors;}
		/**
		 * Setter for Floors
		 */
		public InspectionTemplate setFloors(String floors){this.floors = floors; return this; } 
		/**
		 * Get curtains
		 */
		public String getCurtains(){return curtains;}
		/**
		 * Setter for Curtains
		 */
		public InspectionTemplate setCurtains(String curtains){this.curtains = curtains; return this; } 
		/**
		 * Get light
		 */
		public String getLight(){return light;}
		/**
		 * Setter for light
		 */
		public InspectionTemplate setLight(String light){this.light = light; return this; } 
		/**
		 * Get windowsauthPort
		 */
		public String getWindows(){return windows;}
		/**
		 * Setter for windows
		 */
		public InspectionTemplate setWindows(String windows){this.windows = windows; return this; } 
		/**
		 * Get comments
		 */
		public List<Comment> getComments(){return comments;}
		/**
		 * Setter for comments
		 */
		public InspectionTemplate setComments(List<Comment> comments){this.comments = comments; return this; } 
	}
	
	/**
	 * Lounge Inspection
	 * 
	 * @author daniel
	 */
	private class LoungeInspection extends InspectionTemplate{
		private String moisture; 
		private String heating; 
		/**
		 * Get moisture
		 */
		public String getMoisture(){return moisture;}
		/**
		 * Setter for moisture
		 */
		public LoungeInspection setMoisture(String moisture){this.moisture = moisture; return this; } 
		/**
		 * Get heating
		 */
		public String getHeating(){return heating;}
		/**
		 * Setter for heating
		 */
		public LoungeInspection setHeating(String heating){this.heating = heating; return this; } 
	}
	
	/**
	 * Dining Inspection
	 * 
	 * @author daniel
	 */
	private class DiningInspection extends InspectionTemplate{
	}
	
	/**
	 * Kitchen Inspection
	 * 
	 * @author daniel
	 */
	private class KitchenInspection extends InspectionTemplate{
		private String cupboards; 
		private String benchTops; 
		private String sink; 
		private String taps; 
		private String oven; 
		private String exhaustFan; 
		/**
		 * Get cupboards
		 */
		public String getCupboards(){return cupboards;}
		/**
		 * Setter for cupboards
		 */
		public KitchenInspection setCupboards(String cupboards){this.cupboards = cupboards; return this; } 
		/**
		 * Get benchTops
		 */
		public String getBenchTops(){return benchTops;}
		/**
		 * Setter for benchTops
		 */
		public KitchenInspection setBenchTops(String benchTops){this.benchTops = benchTops; return this; } 
		/**
		 * Get sink
		 */
		public String getSink(){return sink;}
		/**
		 * Setter for sink
		 */
		public KitchenInspection setSink(String sink){this.sink = sink; return this; } 
		/**
		 * Get taps
		 */
		public String getTaps(){return taps;}
		/**
		 * Setter for taps
		 */
		public KitchenInspection setTaps(String taps){this.taps = taps; return this; } 
		/**
		 * Get oven
		 */
		public String getOven(){return oven;}
		/**
		 * Setter for oven
		 */
		public KitchenInspection setOven(String oven){this.oven = oven; return this; } 
		/**
		 * Get exhaustFan
		 */
		public String getExhaustFan(){return exhaustFan;}
		/**
		 * Setter for exhaustFan
		 */
		public KitchenInspection setExhaustFan(String exhaustFan){this.exhaustFan = exhaustFan; return this; } 
	}
	
	/**
	 * Bedroom Inspection
	 * 
	 * @author daniel
	 */
	private class BedroomInspection extends InspectionTemplate{
		private String wardrobes;
		/**
		 * Get wardrobes
		 */
		public String getWardrobes(){return wardrobes;}
		/**
		 * Setter for wardrobes
		 */
		public BedroomInspection setWardrobe(String wardrobes){this.wardrobes = wardrobes; return this; } 
	}
	
	/**
	 * Bathroom Inspection
	 * 
	 * @author daniel
	 */
	private class BathroomInspection extends InspectionTemplate{
		private String bath;
		private String shower; 
		private String washBasin; 
		private String tiling; 
		private String mirror; 
		private String towel; 
		private String toilet; 
		/**
		 * Get bath
		 */
		public String getBath(){return bath;}
		/**
		 * Setter for bath
		 */
		public BathroomInspection setBath(String bath){this.bath = bath; return this; } 
		/**
		 * Get shower
		 */
		public String getShower(){return shower;}
		/**
		 * Setter for shower
		 */
		public BathroomInspection setShower(String shower){this.shower = shower; return this; } 
		/**
		 * Get washBasin
		 */
		public String getWashBasin(){return washBasin;}
		/**
		 * Setter for washBasin
		 */
		public BathroomInspection setWashBasin(String washBasin){this.washBasin = washBasin; return this; } 
		/**
		 * Get tiling
		 */
		public String getTiling(){return tiling;}
		/**
		 * Setter for tiling
		 */
		public BathroomInspection setTiling(String tiling){this.tiling = tiling; return this; } 
		/**
		 * Get mirror
		 */
		public String getMirror(){return mirror;}
		/**
		 * Setter for mirror
		 */
		public BathroomInspection setMirror(String mirror){this.mirror = mirror; return this; } 
		/**
		 * Get towel
		 */
		public String getTowel(){return towel;}
		/**
		 * Setter for towel
		 */
		public BathroomInspection setTowel(String towel){this.towel = towel; return this; } 
		/**
		 * Get toilet
		 */
		public String getToilet(){return toilet;}
		/**
		 * Setter for toilet
		 */
		public BathroomInspection setToilet(String toilet){this.toilet = toilet; return this; } 
	}
	
	/**
	 * Toilet Inspection
	 * 
	 * @author daniel
	 */
	private class ToiletInspection extends InspectionTemplate{
		private String toilet;
		/**
		 * Get toilet
		 */
		public String getToilet(){return toilet;}
		/**
		 * Setter for toilet
		 */
		public ToiletInspection setToilet(String toilet){this.toilet = toilet; return this; } 
	}
	
	/**
	 * Laundry Inspection
	 * 
	 * @author daniel
	 */
	private class LaundryInspection extends InspectionTemplate{
		private String washTub;
		/**
		 * Get washTub
		 */
		public String getWashTub(){return washTub;}
		/**
		 * Setter for washTub
		 */
		public LaundryInspection setWashTub(String washTub){this.washTub = washTub; return this; } 
	}
	
	/**
	 * Exterior Inspection
	 * 
	 * @author daniel
	 */
	private class ExteriorInspection{
		private int ID; 
		private String exteriorWalls;
		private String lawns; 
		private String roof; 
		private String garage; 
		private String driveway; 
		private String clothesLine; 
		private String fences; 
		private String letterBox; 
		private String decking; 
		private String outsideLights; 
		private List<Comment> comments; 
		/**
		 * Getter for ID
		 */
		public int getID(){return ID;}
		/**
		 * Setter for ID
		 */
		public ExteriorInspection setID(int ID){this.ID = ID; return this; }
		/**
		 * Get exteriorWalls
		 */
		public String getExteriorWalls(){return exteriorWalls;}
		/**
		 * Setter for exteriorWalls
		 */
		public ExteriorInspection setExteriorWalls(String exteriorWalls){this.exteriorWalls = exteriorWalls; return this; } 
		/**
		 * Get lawns
		 */
		public String getLawns(){return lawns;}
		/**
		 * Setter for lawns
		 */
		public ExteriorInspection setLawns(String lawns){this.lawns = lawns; return this; } 
		/**
		 * Get roof
		 */
		public String getRoof(){return roof;}
		/**
		 * Setter for roof
		 */
		public ExteriorInspection setRoof(String roof){this.roof = roof; return this; } 
		/**
		 * Get garage
		 */
		public String getGarage(){return garage;}
		/**
		 * Setter for garage
		 */
		public ExteriorInspection setGarage(String garage){this.garage = garage; return this; } 
		/**
		 * Get driveway
		 */
		public String getDriveway(){return driveway;}
		/**
		 * Setter for driveway
		 */
		public ExteriorInspection setDriveway(String driveway){this.driveway = driveway; return this; } 
		/**
		 * Get clothesLine
		 */
		public String getClothesLine(){return clothesLine;}
		/**
		 * Setter for clothesLine
		 */
		public ExteriorInspection setClothesLine(String clothesLine){this.clothesLine = clothesLine; return this; } 
		/**
		 * Get fences
		 */
		public String getFences(){return fences;}
		/**
		 * Setter for fences
		 */
		public ExteriorInspection setFences(String fences){this.fences = fences; return this; } 
		/**
		 * Get letterBox
		 */
		public String getLetterBox(){return letterBox;}
		/**
		 * Setter for letterBox
		 */
		public ExteriorInspection setLetterBox(String letterBox){this.letterBox = letterBox; return this; } 
		/**
		 * Get decking
		 */
		public String getDecking(){return decking;}
		/**
		 * Setter for decking
		 */
		public ExteriorInspection setDecking(String decking){this.decking = decking; return this; } 
		/**
		 * Get outsideLights
		 */
		public String getOutsideLights(){return outsideLights;}
		/**
		 * Setter for outsideLights
		 */
		public ExteriorInspection setOutsideLights(String outsideLights){this.outsideLights = outsideLights; return this; }
		/**
		 * Get comments
		 */
		public List<Comment> getComments(){return comments;}
		/**
		 * Setter for comments
		 */
		public ExteriorInspection setComments(List<Comment> comments){this.comments = comments; return this; }  
	}

    /**
     * Construct a inspection object with the respective details before an inspection.
     * 
     * @param IID 			--- int Inspection ID, primary key
     * @param fp			--- family partner object for the address
     * @param familyMembers	--- list of members within this family partner
     * @param doi 			--- date of inspection
     * @param comments 
     * @param inspector		--- person object of the member who inspects the family partner
     * @param occupant1		--- person object of the occupant who lives within the family partner
     * @param occupant2		--- person object of the occupant who lives within the family partner
     */
    public Inspection(int IID, FamilyPartner fp, List<Person> familyMembers, Date doi, List<Comment> comments, Person inspector, Person occupant1, Person occupant2) {
        this.IID = IID;
        this.familyPartner = fp; 
        this.familyMembers = familyMembers; 
        this.dateOfInspection = doi;
        this.comments = comments;
        this.inspector = inspector; 
        this.occupant1 = occupant1; 
        this.occupant2 = occupant2; 
    }

    /**
     * Construct a inspection object with the respective details before an inspection.
     * 
     * @param addr1
     * @param addr2
     * @param suburb
     * @param city
     * @param region
     * @param country
     * @param postcode
     * @param familyMembers
     * @param doi
     * @param inspector_fName
     * @param inspector_lName
     * @param inspector_contact
     * @param occupant1_fName
     * @param occupant1_lName
     * @param occupant1_contact
     * @param occupant2_fName
     * @param occupant2_lName
     * @param occupant2_contact
     * @param con
     * @throws SQLException 
     */
    public Inspection(String addr1, String addr2, String suburb, String city, String region, String country, String postcode, 
    		List<Person> familyMembers, Date doi, String inspector_fName, String inspector_lName, String inspector_contact, 
    		String occupant1_fName, String occupant1_lName, String occupant1_contact, String occupant2_fName, String occupant2_lName, 
    		String occupant2_contact, Connection con) throws SQLException {
    	List<Expression> address_conditions = new ArrayList<Expression>(); 
    	address_conditions.add(new Expression(FamilyPartner.FIELD_ADDR1, MatchOperator.EQUALS, addr1)); 
    	address_conditions.add(new Expression(FamilyPartner.FIELD_ADDR2, MatchOperator.EQUALS, addr2)); 
    	address_conditions.add(new Expression(FamilyPartner.FIELD_SUBURB, MatchOperator.EQUALS, suburb)); 
    	address_conditions.add(new Expression(FamilyPartner.FIELD_CITY, MatchOperator.EQUALS, city)); 
    	address_conditions.add(new Expression(FamilyPartner.FIELD_REGION, MatchOperator.EQUALS, region)); 
    	address_conditions.add(new Expression(FamilyPartner.FIELD_COUNTRY, MatchOperator.EQUALS, country)); 
    	address_conditions.add(new Expression(FamilyPartner.FIELD_POSTCODE, MatchOperator.EQUALS, postcode)); 
    	List<FamilyPartner> fpList = null; 
		fpList = DB.searchFamPartner(address_conditions, con);
    	if(fpList == null) {throw new SQLException(); }
    	else if(fpList.size() == 1) {this.familyPartner = fpList.get(0); }
    	else {throw new SQLException("There is more than 1 result. "); }
    	
    	// Inspector search
    	// Go through person name list to gather people with the exact first and last name
    	List<Expression> person_conditions = new ArrayList<Expression>(); 
    	person_conditions.add(new Expression(Person.FIELD_FIRST_NAME, MatchOperator.EQUALS, inspector_fName)); 
    	person_conditions.add(new Expression(Person.FIELD_LAST_NAME, MatchOperator.EQUALS, inspector_lName)); 
    	List<Person> pNameList = null; 
		pNameList = DB.searchPerson(person_conditions, con);
    	if(pNameList == null) {throw new SQLException(); }
		
		person_conditions.clear(); 

    	// Go through person name list to gather people with the exact contact
    	person_conditions.add(new Expression(Person.FIELD_EMAIL, MatchOperator.EQUALS, inspector_contact)); 
    	person_conditions.add(new Expression(Person.FIELD_LAND_LINE, MatchOperator.EQUALS, inspector_contact)); 
    	person_conditions.add(new Expression(Person.FIELD_WORK_PHONE, MatchOperator.EQUALS, inspector_contact)); 
    	person_conditions.add(new Expression(Person.FIELD_MOBILE, MatchOperator.EQUALS, inspector_contact)); 
    	List<Person> pContactList = null; 
    	pContactList = DB.searchPerson(person_conditions, con);
    	if(pContactList == null) {throw new SQLException(); }
    	
    	// Now sort out inspector with the return result set from name list and contact list
    	for(Person p1:pNameList) {
    		for(Person p2:pContactList) {
    			if((p1.getContacts().getEmail() != null) && (p2.getContacts().getEmail() != null)) {
    				if(p1.getContacts().getEmail().equals(p2.getContacts().getEmail())){this.inspector = p1; break;}
    			}
    			if((p1.getContacts().getcPhone() != null) && (p2.getContacts().getcPhone() != null)) {
    				if(p1.getContacts().getcPhone().equals(p2.getContacts().getcPhone())){this.inspector = p1; break;}
    			}
    			if((p1.getContacts().gethPhone() != null) && (p2.getContacts().gethPhone() != null)) {
    				if(p1.getContacts().gethPhone().equals(p2.getContacts().gethPhone())){this.inspector = p1; break;}
    			}
    			if((p1.getContacts().getwPhone() != null) && (p2.getContacts().getwPhone() != null)) {
    				if(p1.getContacts().getwPhone().equals(p2.getContacts().getwPhone())){this.inspector = p1; break;}
    			}
    		}
    		if(this.inspector != null) {break;}
    	}
		
		person_conditions.clear(); 
		
		// Occupant1 search
    	// Go through person name list to gather people with the exact first and last name
    	person_conditions.add(new Expression(Person.FIELD_FIRST_NAME, MatchOperator.EQUALS, occupant1_fName)); 
    	person_conditions.add(new Expression(Person.FIELD_LAST_NAME, MatchOperator.EQUALS, occupant1_lName)); 
    	pNameList = null; 
		pNameList = DB.searchPerson(person_conditions, con);
    	if(pNameList == null) {throw new SQLException(); }
		
		person_conditions.clear(); 

    	// Go through person name list to gather people with the exact contact
    	person_conditions.add(new Expression(Person.FIELD_EMAIL, MatchOperator.EQUALS, occupant1_contact)); 
    	person_conditions.add(new Expression(Person.FIELD_LAND_LINE, MatchOperator.EQUALS, occupant1_contact)); 
    	person_conditions.add(new Expression(Person.FIELD_WORK_PHONE, MatchOperator.EQUALS, occupant1_contact)); 
    	person_conditions.add(new Expression(Person.FIELD_MOBILE, MatchOperator.EQUALS, occupant1_contact)); 
    	pContactList = null; 
    	pContactList = DB.searchPerson(person_conditions, con);
    	if(pContactList == null) {throw new SQLException(); }
    	
    	// Now sort out inspector with the return result set from name list and contact list
    	for(Person p1:pNameList) {
    		for(Person p2:pContactList) {
    			if((p1.getContacts().getEmail() != null) && (p2.getContacts().getEmail() != null)) {
    				if(p1.getContacts().getEmail().equals(p2.getContacts().getEmail())){this.occupant1 = p1; break;}
    			}
    			if((p1.getContacts().getcPhone() != null) && (p2.getContacts().getcPhone() != null)) {
    				if(p1.getContacts().getcPhone().equals(p2.getContacts().getcPhone())){this.occupant1 = p1; break;}
    			}
    			if((p1.getContacts().gethPhone() != null) && (p2.getContacts().gethPhone() != null)) {
    				if(p1.getContacts().gethPhone().equals(p2.getContacts().gethPhone())){this.occupant1 = p1; break;}
    			}
    			if((p1.getContacts().getwPhone() != null) && (p2.getContacts().getwPhone() != null)) {
    				if(p1.getContacts().getwPhone().equals(p2.getContacts().getwPhone())){this.occupant1 = p1; break;}
    			}
    		}
    		if(this.occupant1 != null) {break;}
    	}
		
		person_conditions.clear(); 
		
		// Occupant2 search
    	// Go through person name list to gather people with the exact first and last name
    	person_conditions.add(new Expression(Person.FIELD_FIRST_NAME, MatchOperator.EQUALS, occupant2_fName)); 
    	person_conditions.add(new Expression(Person.FIELD_LAST_NAME, MatchOperator.EQUALS, occupant2_lName)); 
    	pNameList = null; 
		pNameList = DB.searchPerson(person_conditions, con);
    	if(pNameList == null) {throw new SQLException(); }
		
		person_conditions.clear(); 

    	// Go through person name list to gather people with the exact contact
    	person_conditions.add(new Expression(Person.FIELD_EMAIL, MatchOperator.EQUALS, occupant2_contact)); 
    	person_conditions.add(new Expression(Person.FIELD_LAND_LINE, MatchOperator.EQUALS, occupant2_contact)); 
    	person_conditions.add(new Expression(Person.FIELD_WORK_PHONE, MatchOperator.EQUALS, occupant2_contact)); 
    	person_conditions.add(new Expression(Person.FIELD_MOBILE, MatchOperator.EQUALS, occupant2_contact)); 
    	pContactList = null; 
    	pContactList = DB.searchPerson(person_conditions, con);
    	if(pContactList == null) {throw new SQLException(); }
    	
    	// Now sort out inspector with the return result set from name list and contact list
    	for(Person p1:pNameList) {
    		for(Person p2:pContactList) {
    			if((p1.getContacts().getEmail() != null) && (p2.getContacts().getEmail() != null)) {
    				if(p1.getContacts().getEmail().equals(p2.getContacts().getEmail())){this.occupant2 = p1; break;}
    			}
    			if((p1.getContacts().getcPhone() != null) && (p2.getContacts().getcPhone() != null)) {
    				if(p1.getContacts().getcPhone().equals(p2.getContacts().getcPhone())){this.occupant2 = p1; break;}
    			}
    			if((p1.getContacts().gethPhone() != null) && (p2.getContacts().gethPhone() != null)) {
    				if(p1.getContacts().gethPhone().equals(p2.getContacts().gethPhone())){this.occupant2 = p1; break;}
    			}
    			if((p1.getContacts().getwPhone() != null) && (p2.getContacts().getwPhone() != null)) {
    				if(p1.getContacts().getwPhone().equals(p2.getContacts().getwPhone())){this.occupant2 = p1; break;}
    			}
    		}
    		if(this.occupant2 != null) {break;}
    	}
    	
    	this.dateOfInspection = doi; 
    	
    	// TODO List of members living in the family partnership
    }
//
//    /**
//     * Construct a inspection object with result set return from a database
//     *
//     * @param rs --- result set return from a database
//     */
//    public Inspection(ResultSet rs, Connection con) throws SQLException {
//    	this.databaseAdapter = new DB(con); 
//    	List<Expression> search_conditions = new ArrayList<Expression>(); 
//    	
//    	this.IID = rs.getInt(FIELD_ID); 
//    	
//    	search_conditions.add(new Expression(FamilyPartner.FIELD_ID, MatchOperator.EQUALS, rs.getInt(FIELD_FAMILY_PARTNER_ID))); 
//    	this.familyPartner = this.databaseAdapter.searchFamPartner(search_conditions).get(0); 
//    	
//    	this.dateOfInspection = rs.getDate(FIELD_DATE); 
//    	
//    	search_conditions.clear(); 
//    	search_conditions.add(new Expression(FIELD_LOUNGE_INSPECTION_ID, MatchOperator.EQUALS, rs.getInt(FIELD_LOUNGE_INSPECTION_ID))); 
//    	this.lounge = this.databaseAdapter. // TODO search database for each inspection table
//	}
//
//    public List<Comment> getComments() {
//        return this.comments;
//    }
//
//    public void setComments(List<Comment> comments) {
//        this.comments = comments;
//    }
//
//    public void addComment(Comment c) {
//        this.comments.add(c);
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final Event other = (Event) obj;
//        if (this.EID != other.EID) {
//            return false;
//        }
//        if (this.name != other.name) {
//        	return false; 
//        }
//        if (!this.address.equals(other.address)) {
//        	return false; 
//        }
//        if (this.doe != other.doe) {
//        	return false; 
//        }
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 83 * hash + this.EID;
//        return hash;
//    }
//
//    @Override
//    public String toString() {
//        return name;
//    }
//    
//    /**
//     * Preparing an sql statement
//     * 
//     * @param preparedStatement
//     * @throws SQLException
//     */
//	private void addValuesToSqlStatement(PreparedStatement preparedStatement)
//			throws SQLException {
//		preparedStatement.setString(1, this.name); 
//		preparedStatement.setString(2, address.getAddr1());
//		preparedStatement.setString(3, address.getAddr2());
//		preparedStatement.setString(4, address.getSuburb());
//		preparedStatement.setString(5, address.getCity());
//		preparedStatement.setString(6, address.getRegion());
//		preparedStatement.setString(7, address.getCountry());
//		preparedStatement.setString(8, address.getPostCode());
//		preparedStatement.setDate(9, this.doe);
//	}
//
//    /**
//     * Writes a new event into the database will not attempt to insert an
//     * existing event.
//     *
//     * @param con --- Database connection to write to
//     * @return --- True if write succeeded
//     * @throws SQLException
//     */
//    public boolean insertIntoDB(Connection con) throws SQLException {
//        if (this.EID != 0) {
//            return false;
//        }
//        
//        int rs = -1;
//		PreparedStatement s = con
//				.prepareStatement(
//						"INSERT INTO Event"
//								+ "(Name,Address1,Address2,Suburb,City,Region,Country,PostCode,"
//								+ "EventDate)"
//								+ "VALUES(?,?,?,?,?,?,?,?,?)",
//						Statement.RETURN_GENERATED_KEYS);
//		addValuesToSqlStatement(s);
//		rs = s.executeUpdate();
//		ResultSet r = s.getGeneratedKeys();
//		r.next();
//		this.EID = r.getInt("EID");
//		r.close();
//		s.close();
//		return rs == 1;
//		// TODO Comments
//        /*for (Comment c : this.comments) {
//            s.executeUpdate("INSERT INTO Comments VALUES (DEFAULT, " + EID + ", NULL,"
//                    + " NULL, " + c.getDate() + ", NULL," + sqlSanitizer.sanitizeString(c.getText()) + ");");
//            r = s.executeQuery("SELECT LAST_INSERT_ID();");
//            r.next();
//            c.setCID(r.getInt(1));
//            r.close();
//        }
//        s.close();
//        return rs == 1;*/
//    }
//
//    /**
//     * Writes updates to event into the database will not attempt to insert a new event.
//     *
//     * @param con --- Database connection to write to
//     * @return --- True if write succeeded
//     * @throws SQLException
//     */
//    public boolean updateIntoDB(Connection con) throws SQLException {
//        if (this.EID == 0) {
//            return false;
//        }
//        
//        PreparedStatement s = con.prepareStatement("UPDATE Event SET Name = ?,"
//				+ "Address1 = ?," + "Address2 = ?," + "Suburb = ?,"
//				+ "City = ?," + "Region = ?," + "Country = ?,"
//				+ "PostCode = ?," + "EndDate = ?" + "WHERE EID = ?");
//		addValuesToSqlStatement(s);
//		s.setInt(10, this.EID);
//		s.executeUpdate();
//		s.close();
//		return true;
//		//TODO Comments
//        /*for (Comment c : this.comments) {
//            if (c.getCID() == 0) {
//                s.executeUpdate("INSERT INTO Comments VALUES (DEFAULT, " + EID + ", NULL,"
//                        + " NULL, " + c.getDate() + ", NULL," + sqlSanitizer.sanitizeString(c.getText()) + ");");
//                r = s.executeQuery("SELECT LAST_INSERT_ID();");
//                r.next();
//                c.setCID(r.getInt(1));
//                r.close();
//            }
//        }
//        return rs == 1;*/
//    }
//
//    /**
//     * Delete this event from the database. 
//     * 
//     * @param con
//     * @throws SQLException
//     */
//    public void deleteFromDB(Connection con) throws SQLException{
//    	PreparedStatement s;
//    	s = con.prepareStatement(
//    			"DELETE FROM Event where eid = ?",
//    			Statement.RETURN_GENERATED_KEYS); 
//    	s.setInt(1, this.EID);
//    	s.executeUpdate();
//    	s.close();
//    }
//
//    /**
//     * Used to generate an SQL statement to select from Event all event which
//     * match the fields set in this event Must not be called on an empty
//     * Event.
//     *
//     * @return String --- SQL Query to select event based on set fields.
//     */
//    public String createSelectQuery() {
//        StringBuilder query = new StringBuilder("SELECT * FROM Event WHERE "); //header is constant, now build the where clause.
//        if (this.name != null && !this.name.equals("")) {
//            query.append("Name = ");
//            query.append(sqlSanitizer.sanitizeString(name));
//            query.append(" AND ");
//        }
//        if (this.address != null) {
//            if (this.address.getAddr1() != null && !this.address.getAddr1().equals("NULL")) {
//                query.append("Address1 = ");
//                query.append(this.address.getAddr1());
//                query.append(" AND ");
//            }
//            if (this.address.getAddr2() != null && !this.address.getAddr2().equals("NULL")) {
//                query.append("Address2 = ");
//                query.append(this.address.getAddr2());
//                query.append(" AND ");
//            }
//            if (this.address.getSuburb() != null && !this.address.getSuburb().equals("NULL")) {
//                query.append("Suburb = ");
//                query.append(this.address.getSuburb());
//                query.append(" AND ");
//            }
//            if (this.address.getCity() != null && !this.address.getCity().equals("NULL")) {
//                query.append("City = ");
//                query.append(this.address.getCity());
//                query.append(" AND ");
//            }
//            if (this.address.getRegion() != null && !this.address.getRegion().equals("NULL")) {
//                query.append("Region = ");
//                query.append(this.address.getRegion());
//                query.append(" AND ");
//            }
//            if (this.address.getCountry() != null && !this.address.getCountry().equals("NULL")) {
//                query.append("Country = ");
//                query.append(this.address.getCountry());
//                query.append(" AND ");
//            }
//            if (this.address.getPostCode() != null && !this.address.getPostCode().equals("NULL")) {
//                query.append("PostCode = ");
//                query.append(this.address.getPostCode());
//                query.append(" AND ");
//            }
//        }
//        if (this.doe != null) {
//            query.append("EventDate = ");
//            query.append(this.doe.toString());
//            query.append(" AND ");
//        }
//
//        query.delete(query.length() - 5, query.length());
//        return query.toString().trim();
//    }
}

