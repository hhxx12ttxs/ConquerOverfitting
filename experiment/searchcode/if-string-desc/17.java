package cmuHCI.WalkyScotty;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import cmuHCI.WalkyScotty.entities.*;
import cmuHCI.WalkyScotty.util.*;

public class DBAdapter extends SQLiteOpenHelper {

	private static String DB_PATH = "/data/data/cmuHCI.WalkyScotty/databases/";
    private static String DB_NAME = "chewbacca";
    private SQLiteDatabase myDataBase; 
    private final Context myContext;
    
    /* Image array..array index = loc_id */
    private static int[] images = 
	{ 
    	R.drawable.blank, //loc_id = 0 (this is a dummy entry...you can't actually have loc_id = 0)
		R.drawable.bakerhall, //loc_id = 1
		R.drawable.ghc, //loc_id = 2 
		R.drawable.si_senor1, //loc_id = 3
		R.drawable.blank,
		R.drawable.ph,
		R.drawable.cfa,
		R.drawable.cic,
		R.drawable.dh,
		R.drawable.uc,
		R.drawable.hbh, //10
		R.drawable.hh,
		R.drawable.hunt_lib_1,
		R.drawable.blank,
		R.drawable.taste_of_india1,
		R.drawable.spice_it_up_grill1,
		R.drawable.underground1,
		R.drawable.zebra_lounge1,
		R.drawable.tartans_pavilion1,
		R.drawable.downtown_deli1,
		R.drawable.blank,
		R.drawable.the_exchange1, //20
		R.drawable.maggie_murph1,
		R.drawable.gingers_express1,
		R.drawable.resnik,
		R.drawable.city_grill1,
		R.drawable.evgefstos1,
		R.drawable.kosher_korner1,
		R.drawable.pasta_villaggio1,
		R.drawable.quik_piks1,
		R.drawable.schatz1,
		R.drawable.schatz1, //30
		R.drawable.schatz1,
		R.drawable.skibo_cafe1,
		R.drawable.spinning_salads1,
		R.drawable.take_comfort1,
		R.drawable.totally_juiced1,
		R.drawable.asiana1,
		R.drawable.heinz_cafe1,
		R.drawable.la_prima1,
		R.drawable.stephanies1,
		R.drawable.tazza1, //40
		R.drawable.carnegie_mellon_cafe1,
		R.drawable.souper_soup1,
		R.drawable.sushi_too1,
		R.drawable.take_comfort1,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.wh,
		R.drawable.mellon,
		R.drawable.blank,
		R.drawable.blank, //50
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.purnell,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.skibo2,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,	
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.cic,
		R.drawable.blank,
		R.drawable.cyert,
		R.drawable.warner_hall,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank,
		R.drawable.blank
	};
    
    public static int getImage(int locID){
    	return images[locID % images.length];
    }
	
	public DBAdapter(Context context) {
		//super(context, name, factory, version);
		super(context, DB_NAME, null, 1);
        this.myContext = context;
	}
	
	/**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
 
    	boolean dbExist = checkDataBase();
 dbExist = false;
    	if(dbExist){
    		//do nothing - database already exist
    	}else{
 
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try {
 
    			copyDataBase();
    			System.out.println("copying db");
 
    		} catch (IOException e) {
 
        		throw new Error("Error copying database");
 
        	}
    	}
 
    }
    
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;

    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
 
    		//database does't exist yet.
 
    	}
 
    	if(checkDB != null){
 
    		checkDB.close();
 
    	}
 
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
 
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
    	
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
 
    public void openDataBase() throws SQLException{
 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        //myDataBase = this.getReadableDatabase();
        
    }
 
    @Override
	public synchronized void close() {
 
    	    if(myDataBase != null)
    		    myDataBase.close();
 
    	    super.close();
 
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}
	
	//Data retrieval
	
	public ArrayList<Building> getBuildings(){
		ArrayList<Building> buildings = new ArrayList<Building>();
		
		Cursor c = myDataBase.query(true, "buildings JOIN locations ON (buildings.location_id = locations._id)", new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick"}, null, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					
					buildings.add(new Building(id, name, desc, img, abbr));
				}
				while(c.moveToNext());
			}
		}
		
		return buildings;
	}
	
	public Room getRoom(int locationID){
		Cursor c = myDataBase.query(true, "rooms JOIN locations ON (rooms.location_id = locations._id)", new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick"}, "locations._id = " + locationID, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					
					
					return new Room(id, name, desc, img, abbr);
				}
				while(c.moveToNext());
			}
		}
		
		return null;
	}
	
	public ArrayList<Room> getRooms(){
		ArrayList<Room> rooms = new ArrayList<Room>();
		
		Cursor c = myDataBase.query(true, "rooms JOIN locations ON (rooms.location_id = locations._id)", new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick"}, null, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					
					
					rooms.add(new Room(id, name, desc, img, abbr));
				}
				while(c.moveToNext());
			}
		}
		
		return rooms;
	}
	
	public ArrayList<Room> getRooms(Building building){
		ArrayList<Room> rooms = new ArrayList<Room>();
		
		Cursor c = myDataBase.query(true, "rooms JOIN locations ON (rooms.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick"}, "rooms.building_id = " + building.getId(), null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					
					rooms.add(new Room(id, name, desc, img, abbr));
				}
				while(c.moveToNext());
			}
		}
		
		return rooms;
	}
	
	public Building getBuilding(int locationID){
		Cursor c = myDataBase.query(true, "buildings JOIN locations ON (buildings.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick"}, "locations._id = " + locationID, null, null, null, "locations.name", null);


		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		
		if(c!=null){
			if(c.isFirst()){
				int id = c.getInt(idCol);
				String name = c.getString(nameCol);
				String desc = c.getString(descCol);
				String img = c.getString(imgCol);
				String abbr = c.getString(abbrCol);
				
				return new Building(id, name, desc, img, abbr);
			}
		}
		
		return null;
	}
	
	public Building getBuildingForRoom(int locationID){
		
		int buildingID = -1;
		
		Cursor c = myDataBase.query(true, "buildings JOIN rooms ON (rooms.building_id = buildings._id) JOIN locations ON (rooms.location_id = locations._id) ", new String[]{"buildings.location_id"}, "locations._id = " + locationID, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("location_id");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					buildingID = c.getInt(idCol);
				}
				while(c.moveToNext());
			}
			return getBuilding(buildingID);
		}
		
		return null;		
	}
	
	public Restaurant getRestaurant(int locationID){
		Cursor c = myDataBase.query(true, "restaurants JOIN locations ON (restaurants.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick", "restaurants.hours", "restaurants.menu"}, "locations._id = " + locationID, null, null, null, "locations.name", null);


		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		int hourCol = c.getColumnIndex("hours");
		int menuCol = c.getColumnIndex("menu"); 
		
		if(c!=null){
			if(c.isFirst()){
				int id = c.getInt(idCol);
				String name = c.getString(nameCol);
				String desc = c.getString(descCol);
				String img = c.getString(imgCol);
				String abbr = c.getString(abbrCol);
				String hours = c.getString(hourCol);
				String menu = c.getString(menuCol);
				
				return new Restaurant(id, name, desc, img, abbr, hours, menu);
			}
		}
		
		return null;
	}
	
	public ArrayList<Restaurant> getRestaurants(){
		ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
		
		Cursor c = myDataBase.query(true, "restaurants JOIN locations ON (restaurants.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick",
				"restaurants.hours", "restaurants.menu"}, null, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		int hourCol = c.getColumnIndex("hours");
		int menuCol = c.getColumnIndex("menu");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					String hours = c.getString(hourCol);
					String menu = c.getString(menuCol);
					
					restaurants.add(new Restaurant(id, name, desc, img, abbr,hours, menu));
				}
				while(c.moveToNext());
			}
		}
		
		return restaurants;
	}
	
	public Service getService(int locationID){
		Cursor c = myDataBase.query(true, "services JOIN locations ON (services.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick", "services.tel"}, "locations._id = " + locationID, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		int telCol = c.getColumnIndex("tel");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					int tel = c.getInt(telCol);
					
					return new Service(id, name, desc, img, abbr, tel);
				}
				while(c.moveToNext());
			}
		}
		
		return null;
	}
	
	public ArrayList<Service> getServices(){
		ArrayList<Service> services = new ArrayList<Service>();
		
		Cursor c = myDataBase.query(true, "services JOIN locations ON (services.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick", "services.tel"}, null, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		int telCol = c.getColumnIndex("tel");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					int tel = c.getInt(telCol);
					
					services.add(new Service(id, name, desc, img, abbr, tel));
				}
				while(c.moveToNext());
			}
		}
		
		return services;
	}
	
	public Shuttle getShuttle(int locationID){
		Cursor c = myDataBase.query(true, "shuttles JOIN locations ON (shuttles.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick",
				"shuttles.hours", "shuttles.stops"}, "locations._id = " + locationID, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		int hourCol = c.getColumnIndex("hours");
		int stopCol = c.getColumnIndex("stops");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					String stops = c.getString(stopCol);
					String hours = c.getString(hourCol);
					
					return new Shuttle(id, name, desc, img, abbr,stops, hours);
				}
				while(c.moveToNext());
			}
		}
		
		return null;
	}
	
	public ArrayList<Shuttle> getShuttles(){
		ArrayList<Shuttle> shuttles = new ArrayList<Shuttle>();
		
		Cursor c = myDataBase.query(true, "shuttles JOIN locations ON (shuttles.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick",
				"shuttles.hours", "shuttles.stops"}, null, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		int hourCol = c.getColumnIndex("hours");
		int stopCol = c.getColumnIndex("stops");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					String stops = c.getString(stopCol);
					String hours = c.getString(hourCol);
					
					shuttles.add(new Shuttle(id, name, desc, img, abbr,stops, hours));
				}
				while(c.moveToNext());
			}
		}
		
		return shuttles;
	}
	
	public ArrayList<Escort> getEscorts(){
		ArrayList<Escort> escorts = new ArrayList<Escort>();
		
		Cursor c = myDataBase.query(true, "escorts JOIN locations ON (escorts.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick",
				"escorts.hours", "escorts.stops"}, null, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		int hourCol = c.getColumnIndex("hours");
		int stopCol = c.getColumnIndex("stops");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					String stops = c.getString(stopCol);
					String hours = c.getString(hourCol);
					
					escorts.add(new Escort(id, name, desc, img, abbr, stops, hours));
				}
				while(c.moveToNext());
			}
		}
		
		return escorts;
	}
	
	public Escort getEscort(int locationID){
		Cursor c = myDataBase.query(true, "escorts JOIN locations ON (escorts.location_id = locations._id)", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick",
				"escorts.hours", "escorts.stops"}, "locations._id = " + locationID, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int abbrCol = c.getColumnIndex("nick");
		int hourCol = c.getColumnIndex("hours");
		int stopCol = c.getColumnIndex("stops");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String abbr = c.getString(abbrCol);
					String stops = c.getString(stopCol);
					String hours = c.getString(hourCol);
					
					return new Escort(id, name, desc, img, abbr, stops, hours);
				}
				while(c.moveToNext());
			}
		}
		
		return null;
	}
	
	public ArrayList<Location> getOther(){
		ArrayList<Location> locations = new ArrayList<Location>();
		
		locations.addAll(getShuttles());
		locations.addAll(getEscorts());
		
		Collections.sort(locations, new AlphaComparator());
		
		return locations;
	}
	
	public Location getLocation(int locID){
		
		Cursor c = myDataBase.query(true, "locations", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick", "locations.loc_type"}, "" +
						"locations._id = " + locID, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int typeCol = c.getColumnIndex("loc_type");
		int abbrCol = c.getColumnIndex("nick");
		
		if(c!=null){
			if(c.isFirst()){
				int id = c.getInt(idCol);
				String name = c.getString(nameCol);
				String desc = c.getString(descCol);
				String img = c.getString(imgCol);
				String locType = c.getString(typeCol);
				String abbr = c.getString(abbrCol);
				
				Location l = new Location(id, name, desc, img, abbr);
				l.setlType(locType);
				
				return l;
			}
		}
		
		return null;
	}
	
	public ArrayList<Location> getAllLocations(){
		ArrayList<Location> locations = new ArrayList<Location>();
		
		Cursor c = myDataBase.query(true, "locations", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick", "locations.loc_type"}, null, null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int typeCol = c.getColumnIndex("loc_type");
		int abbrCol = c.getColumnIndex("nick");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					String name = c.getString(nameCol);
					String desc = c.getString(descCol);
					String img = c.getString(imgCol);
					String locType = c.getString(typeCol);
					String abbr = c.getString(abbrCol);
					
					Location l = new Location(id, name, desc, img, abbr);
					l.setlType(locType);
					
					locations.add(l);
				}
				while(c.moveToNext());
			}
		}
		
		return locations;
	}
	
	public Location getLocation(String loc){
		
		Cursor c = myDataBase.query(true, "locations", 
				new String[]{"locations._id", "locations.name","locations.description","locations.image", "locations.nick", "locations.loc_type"}, "" +
						"locations.name = '" + loc + "'", null, null, null, "locations.name", null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int nameCol = c.getColumnIndex("name");
		int descCol = c.getColumnIndex("description");
		int imgCol = c.getColumnIndex("image");
		int typeCol = c.getColumnIndex("loc_type");
		int abbrCol = c.getColumnIndex("nick");
		
		if(c!=null){
			if(c.isFirst()){
				int id = c.getInt(idCol);
				String name = c.getString(nameCol);
				String desc = c.getString(descCol);
				String img = c.getString(imgCol);
				String locType = c.getString(typeCol);
				String abbr = c.getString(abbrCol);
				
				Location l = new Location(id, name, desc, img, abbr);
				l.setlType(locType);
				return l;
			}
		}
		
		return null;
	}
	
	public DirectionList getDirections(Location from, Location to){
		DirectionList directions;
		
		return null;
	
	}
	
	public MyDirectedGraph<Location, WeightedEdge<Location>> getGraph(){
		ArrayList<WeightedEdge<Location>> edges = new ArrayList<WeightedEdge<Location>>();
		HashSet<Location> locs = new HashSet<Location>();
		MyDirectedGraph<Location, WeightedEdge<Location>> graph = new MyDirectedGraph<Location, WeightedEdge<Location>>();
		
		Cursor c = myDataBase.query(true, "nearby", 
				new String[]{"nearby._id", "nearby.loc_1","nearby.loc_2","nearby.dist"}, null, null, null, null, null, null);

		c.moveToFirst();
		
		int idCol = c.getColumnIndex("_id");
		int locCol = c.getColumnIndex("loc_1");
		int loc2Col = c.getColumnIndex("loc_2");
		int distCol = c.getColumnIndex("dist");
		
		if(c!=null){
			if(c.isFirst()){
				do{
					int id = c.getInt(idCol);
					int loc_1 = c.getInt(locCol);
					int loc_2 = c.getInt(loc2Col);
					int dist = c.getInt(distCol);
					
					Location l1 = new Location(loc_1, null, null, null, null);
					Location l2 = new Location(loc_2, null, null, null, null);
					
					graph.addVertex(l1);
					graph.addVertex(l2);

					graph.addEdge(new WeightedEdge<Location>(l1, l2, dist));
					graph.addEdge(new WeightedEdge<Location>(l2, l1, dist));
				}
				while(c.moveToNext());
			}
		}	
		return graph;
	}
	
}

