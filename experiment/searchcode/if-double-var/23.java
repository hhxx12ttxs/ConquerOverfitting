package me.samkio.levelcraft.SamToolbox;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.samkio.levelcraft.Levelcraft;


public class DataSqlite {
	private  Connection connection;
	private  final Logger log = Logger.getLogger("Minecraft");
	public  Levelcraft plugin;
	public DataSqlite(Levelcraft instance) {
		plugin = instance;
	}


	public  synchronized Connection getConnection() {
		if (connection == null) {
			connection = createConnection();
		}
		return connection;
	}

	private  Connection createConnection() {

		try {

			Class.forName("org.sqlite.JDBC");
			Connection ret = DriverManager.getConnection("jdbc:sqlite:"
					+ plugin.maindirectory + plugin.datadirectory
					+ "Experience.sqlite");
			ret.setAutoCommit(false);
			return ret;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public  void PrepareDB() {
		Connection conn = null;
		Statement st = null;
		int maxcolumns = 9;                //Always update this when added new experience tree
		try {
			conn = getConnection();
			st = conn.createStatement();
			st.executeUpdate("CREATE TABLE IF NOT EXISTS 'ExperienceTable' ('PlayerName' VARCHAR, 'WoodcuttingExp' INT ( 255 ) NOT NULL DEFAULT 0," +
					"'MiningExp' INT ( 255 ) NOT NULL DEFAULT 0,'SlayingExp' INT ( 255 ) NOT NULL DEFAULT 0," +
					"'RangingExp' INT ( 255 ) NOT NULL DEFAULT 0,'FisticuffsExp' INT ( 255 ) NOT NULL DEFAULT 0," +
					"'ArcheryExp' INT ( 255 ) NOT NULL DEFAULT 0,'DiggingExp' INT ( 255 ) NOT NULL DEFAULT 0,'ForgeExp' INT ( 255 ) NOT NULL DEFAULT 0 ); CREATE INDEX playerIndex on ExperienceTable (PlayerName);");
			ResultSet rs = st.executeQuery("SELECT * FROM 'ExperienceTable';");
		    ResultSetMetaData rsmd = rs.getMetaData();
		    int numColumns = rsmd.getColumnCount();
		    if (!(numColumns == maxcolumns)){
		    	//database is old we need to add the new columns
		    	if(numColumns==4){
		    		st.executeUpdate("ALTER TABLE 'ExperienceTable' ADD COLUMN 'RangingExp' INT ( 255 )  NOT NULL DEFAULT 0;");
		    		st.executeUpdate("ALTER TABLE 'ExperienceTable' ADD COLUMN 'FisticuffsExp' INT ( 255 )  NOT NULL DEFAULT 0;");
		    		st.executeUpdate("ALTER TABLE 'ExperienceTable' ADD COLUMN 'ArcheryExp' INT ( 255 )  NOT NULL DEFAULT 0;");
		    	}
		    	if(numColumns==5){
		    		st.executeUpdate("ALTER TABLE 'ExperienceTable' ADD COLUMN 'FisticuffsExp' INT ( 255 )  NOT NULL DEFAULT 0;");
		    		st.executeUpdate("ALTER TABLE 'ExperienceTable' ADD COLUMN 'ArcheryExp' INT ( 255 )  NOT NULL DEFAULT 0;");
		    	}
		    	if(numColumns==6){
		    		st.executeUpdate("ALTER TABLE 'ExperienceTable' ADD COLUMN 'ArcheryExp' INT ( 255 )  NOT NULL DEFAULT 0;");
		    	}
		    	if(numColumns==7){
					st.executeUpdate("ALTER TABLE 'ExperienceTable' ADD COLUMN 'DiggingExp' INT ( 255 ) NOT NULL DEFAULT 0;");
				}
		    	if(numColumns==8){
					st.executeUpdate("ALTER TABLE 'ExperienceTable' ADD COLUMN 'ForgeExp' INT ( 255 ) NOT NULL DEFAULT 0;");
				}
		    }
		    conn.commit();
		} catch (SQLException e) {
			log.severe("[Levelcraft] Unable to prepare database");
		}
	}

	public  void NewPlayer(CommandSender sender, double var) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Connection conn = null;
			Statement st = null;
			String p = player.getName();
			try {
				conn = getConnection();
				st = conn.createStatement();
				st.executeUpdate("INSERT INTO ExperienceTable (PlayerName,WoodcuttingExp,MiningExp,SlayingExp,RangingExp,FisticuffsExp,ArcheryExp,DiggingExp,ForgeExp) VALUES ('"
						+ p + "'," + var + "," + var + "," + var +"," + var +"," + var +"," + var +"," + var + "," + var + ")");
				conn.commit();
			} catch (SQLException e) {
				log.severe("[Levelcraft] Unable to add row database" + e);
			}
		} else {
			sender.sendMessage("Error: Cannot create new player!");
		}
	}

	public  void DelRow(String databasetable, String value) {
		Connection conn = null;
		Statement st = null;
		try {
			conn = getConnection();
			st = conn.createStatement();
			st.executeUpdate("DELETE FROM Players WHERE PlayerName=('" + value
					+ "')");
			conn.commit();
		} catch (SQLException e) {
			log.severe("[Levelcraft] Unable to delete row database" + e);
		}
	}

	public  void update(CommandSender sender, String value,double newvalue) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Connection conn = null;
			Statement st = null;
			String p = player.getName();
			try {
				conn = getConnection();
				st = conn.createStatement();
				st.executeUpdate("UPDATE ExperienceTable set "+value+" = '"+newvalue+"' WHERE PlayerName='"+p+"'");
				conn.commit();
			} catch (SQLException e) {
				log.severe("[Levelcraft] Unable to update row database" + e);
			}
		} else {
			sender.sendMessage("Error: Player could not be updated!");
		}
	}

	public  String GetRow(String databasetable, String value) {
		Connection conn = null;
		Statement st = null;
		String name = "NULL";
		try {
			conn = getConnection();
			st = conn.createStatement();
			ResultSet rs = st
			.executeQuery("SELECT PlayerName FROM Players WHERE PlayerName=('"
					+ value + "')");
			while (rs.next()) {
				name = rs.getString("PlayerName");
			}
			conn.commit();
			return name;
		} catch (SQLException e) {
			log.severe("[Levelcraft] Unable to get row database" + e);
		}
		return name;
	}

	public  double getExp(CommandSender sender, String value) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Connection conn = null;
			Statement st = null;
			String p = player.getName();
			double level = 0;
			try {
				conn = getConnection();
				st = conn.createStatement();
				ResultSet rs = st.executeQuery("SELECT " + value
						+ " FROM ExperienceTable WHERE PlayerName=('" + p + "')");
				while (rs.next()) {
					level = rs.getDouble(value);
				}
				conn.commit();
				return level;
			} catch (SQLException e) {
				log.severe("[Levelcraft] Unable to get row database" + e);
			}
			return level;
		} else {
			sender.sendMessage("Error: Could not retrieve experience value!");
			return 0;
		}
	}

	public  int getLevel(CommandSender sender, String value) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			int level = 0;
			double exp = getExp(player, value);
			double constant = plugin.Settings.Constant;
			constant = constant / 100;
			for (int i = 1; i <= 1000; i++) {
				double levelAti = (100 * (i * (i * constant)));
				if (levelAti >= exp) {
					level = i;
					break;
				}
			}
			return level;
		} else {
			sender.sendMessage("Error: Could not retrieve level value!");
			return 0;
		}
	}

	public  double getExpLeft(CommandSender sender, String value) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			double exp = getExp(player, value);
			double getExpUp = 0;
			double constant = plugin.Settings.Constant;
			constant = constant / 100;
			for (int i = 1; i <= 1000; i++) {
				double levelAti = (100 * (i * (i * constant)));
				if (levelAti >= exp) {
					getExpUp = levelAti;
					break;
				}
			}
			double leftExp = (getExpUp - exp);
			double leftExp2 = plugin.Toolbox.roundTwoDecimals(leftExp);
			return leftExp2;
		} else {
			sender.sendMessage("Error: Could not retrieve experience value!");
			return 0;
		}
	}

	public  boolean PlayerExsists(CommandSender sender) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Connection conn = null;
			Statement st = null;
			String p = player.getName();
			boolean isTrue = false;
			try {
				conn = getConnection();
				st = conn.createStatement();
				ResultSet rs = st
				.executeQuery("SELECT PlayerName FROM ExperienceTable WHERE PlayerName=('"
						+ p + "')");
				while (rs.next()) {
					isTrue = true;
				}
				conn.commit();
				return isTrue;
			} catch (SQLException e) {
				log.severe("[Levelcraft] Unable to get row database" + e);
			}
			return isTrue;
		} else {
			sender.sendMessage("Error: Player does not exist!");
			return false;
		}
	}
}


