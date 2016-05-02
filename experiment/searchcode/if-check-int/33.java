package server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.History;
import common.Profile;
import common.ReverseIntegerComparator;
import common.History.historyEntry;


public class DBLogic {
	
	DBAccess DBA;
	
	public static final String DELIMITER = ";;";
	
	public DBLogic() {
		DBA = new DBAccess();
	}
	
	public boolean InitDBConnection() {
		return DBA.InitDBConnection();
	}
	
	public void ShutDBConnection() {
		DBA.ShutDBConnection();
	}
	
	public boolean addNewProfile(Profile profile) {
		int check = DBA.WriteToDB("INSERT INTO USERS (USERNAME, PASSWORD, RATING, EMAIL) " +
			 "VALUES ('"+profile.getUserName()+"', '"+profile.getPassword()+"', '"+profile.getRating()+"', '"+profile.getEmail()+"')");
		if (check!=1) return false;
		for(int i=0;i<profile.getHistory().getNumOfHistories();i++) {
			if (!insertHistoryEntryToDB(profile.getUserName(),profile.getHistory().getEntry(i)))
				return false;
		}
		profile.resetDirtyBits();
		return true;
	}
	
	public boolean updateProfile(Profile profile) {
		//Update profile fields:
		if (profile.isDirty()) {
			int check = DBA.WriteToDB("UPDATE USERS SET PASSWORD='"+profile.getPassword()+"', RATING='"+profile.getRating()+"', EMAIL='"+profile.getEmail()+"' " +
					"WHERE USERNAME='"+profile.getUserName()+"'");			
			if (check!=1)
				return false;
		}
		//Update histories:
		History history = profile.getAddedHistories();
		for(int i=0;i<history.getNumOfHistories();i++) {
			if (!insertHistoryEntryToDB(profile.getUserName(),history.getEntry(i)))
				return false;
		}
		profile.resetDirtyBits();
		return true;
	}
	
	private boolean insertHistoryEntryToDB(String userName,historyEntry entry) {
		String opponents = new String(entry.getRivals().get(0));
		for (int i=1;i<entry.getRivals().size();i++) {
			opponents += DELIMITER;
			opponents +=entry.getRivals().get(i);
		}
		int check = DBA.WriteToDB("INSERT INTO GAME_HISTORY (USERNAME, OPPONENTS, RATING_CHANGE, GAME_DATE) " +
				 "VALUES ('"+userName+"', '"+opponents+"', '"+entry.getScore()+"', '"+entry.getDate()+"')");
		return (check==1);
	}

	public Profile queryProfile(String userName) throws SQLException {
		ResultSet rs = DBA.ReadFromDB("SELECT * FROM USERS WHERE USERNAME = \"" + userName +"\"");
		if (!rs.next()) 
			return null;
		Profile result = new Profile(userName,rs.getString("PASSWORD"),rs.getString("EMAIL"),rs.getInt("RATING"));
		rs = DBA.ReadFromDB("SELECT * FROM GAME_HISTORY WHERE USERNAME = \"" + userName +"\"");
		while (rs.next()) {
			result.addHistoryEntry(rs.getDate("GAME_DATE"), parseRivals(rs.getString("OPPONENTS")), rs.getInt("RATING_CHANGE"));
		}
		result.resetDirtyBits();
		return result;
	}
	
	private List<String> parseRivals(String rivals) {
		String result[] = rivals.split(DELIMITER);
		return Arrays.asList(result);
	}
	
	public int deleteProfile(String userName) throws SQLException {
		int check = DBA.WriteToDB("DELETE FROM GAME_HISTORY WHERE USERNAME = \"" + userName +"\"");
		if (check == -1) return -1;
		check = DBA.WriteToDB("DELETE FROM USERS WHERE USERNAME = \"" + userName +"\"");
		return check;
	}
	
	public Map<Integer,List<String>> getHallOfFame() throws SQLException {
	    ReverseIntegerComparator comparator = new ReverseIntegerComparator();
	    Map<Integer, List<String>> hallOfFame = new TreeMap<Integer, List<String>>(comparator);
	    ResultSet rs = DBA.ReadFromDB("SELECT * FROM HALL_OF_FAME");
	    while (rs.next()) {
	        if (hallOfFame.containsKey(rs.getInt("SCORE"))) {
	            hallOfFame.get(rs.getInt("SCORE")).add(rs.getString("USERNAME"));
	        } else {
	            List<String> names = new ArrayList<String>();
	            names.add(rs.getString("USERNAME"));
	            hallOfFame.put(rs.getInt("SCORE"), names);
	        }
	    }
	    return hallOfFame;
	}
	
	public boolean updateHallOfFame(Map<Integer, List<String>> hallOfFame) {
       String hof_query = "CREATE TABLE HALL_OF_FAME (USERNAME VARCHAR(50) NOT NULL," +
       "SCORE INT UNSIGNED NOT NULL)";
	    
	    DBA.WriteToDB("DROP TABLE HALL_OF_FAME;");
	    int check = DBA.WriteToDB(hof_query);
	    if (check == -1) {
	        return false;
	    }
	    
	    for (int currentScore : hallOfFame.keySet()) {
	        for (String currentName : hallOfFame.get(currentScore)) {
	            check = DBA.WriteToDB("INSERT INTO HALL_OF_FAME (USERNAME, SCORE) " +
	                    "VALUES ('" + currentName + "' , '" + currentScore + "')");
	            if (check == -1) {
	                return false;
	            }
	        }
	    }
	    
	    return true;
	}
}

