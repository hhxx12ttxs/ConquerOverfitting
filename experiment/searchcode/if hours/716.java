package pl.projektzespolowy.srp.db;

public class OpeningHours {

	public static String[] initial_days = new String[]{"1","2","3","4","5","6"};
	public static String[] initial_open = new String[]{"9:00","9:00","9:00","9:00","9:00","9:00"};
	public static String[] initial_close = new String[]{"18:00","18:00","18:00","18:00","18:00","18:00"};
	
	public static final String
		HOURS_TABLE = "Hours",
		HOURS_ID = "_ID",
		HOURS_DAY = "DayType",
		HOURS_OPEN = "Open",
		HOURS_CLOSE = "Close";

	public static final String
		TAG_TABLE = "godz",
		TAG_DAY = "DzienTygodnia",
		TAG_OPEN = "GodzinaOtwarcia",
		TAG_CLOSE = "GodzinaZamkniecia";
	
	public static final String CREATE_TABLE_HOURS = "CREATE TABLE "+HOURS_TABLE+" ("
			+HOURS_ID+" integer primary key, "
			+HOURS_DAY+" text, "
			+HOURS_OPEN+" text, "
			+HOURS_CLOSE+" text ); ";
		
	public static final String INSERT_HOURS = "INSERT INTO "+HOURS_TABLE+" VALUES ";
		
	public static final String DELETE_TABLE_HOURS = "DROP TABLE IF EXISTS "+HOURS_TABLE;
	
	public static String update(int id, String dayType, String open, String close)
	{
		return "UPDATE "+HOURS_TABLE+" SET "
					+HOURS_DAY+"="+dayType+", "
					+HOURS_OPEN+"="+open+", "
					+HOURS_CLOSE+"="+close+" "
					+" WHERE "+HOURS_ID+"="+id;
	}
	
	public static String insert(String day, String open, String close)
	{
		return INSERT_HOURS+" (null, "+day+", "+open+", "+close+");";
	}
	
	public static String initialInsert()
	{
		return insert(initial_days,initial_open,initial_close);
	}
	
	public static String insert(String[] day, String[] open, String[] close)
	{
		String out = INSERT_HOURS;
		for(int i = 0; i < day.length-1; i++)
		{
			out += " (null, \""+day[i]+"\", \""+open[i]+"\", \""+close[i]+"\"),";
		}
		int i = day.length-1;
		out += "(null, \""+day[i]+"\", \""+open[i]+"\", \""+close[i]+"\");";
		return out;
	}
}

