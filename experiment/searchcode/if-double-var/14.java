package me.samkio.levelcraft.SamToolbox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.samkio.levelcraft.Levelcraft;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

public class DataMySql {
	private  final Logger log = Logger.getLogger("Minecraft");
	public  Levelcraft plugin;
	public DataMySql(Levelcraft instance) {
		plugin = instance;
	}

	private  Connection createConnection() throws ClassNotFoundException,
			SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://"+plugin.Settings.MySqlDir+"",""+plugin.Settings.MySqlUser+"", ""+plugin.Settings.MySqlPass+"");
	}

	public  void PrepareDB() {
		Connection conn = null;
		Statement st = null;
		int maxcolumns = 10;                //Always update this when added new experience tree
		try {
			conn = createConnection();
			st = (Statement) conn.createStatement();
			st.executeUpdate("CREATE TABLE IF NOT EXISTS `ExperienceTable` ( `id` INT( 255 ) NOT NULL AUTO_INCREMENT," +
					" `PlayerName` TEXT NOT NULL ,`WoodcuttingExp` DOUBLE(10,2) NOT NULL DEFAULT 0," +
					"`SlayingExp` DOUBLE(10,2) NOT NULL DEFAULT 0,`MiningExp` DOUBLE(10,2) NOT NULL DEFAULT 0," +
					"`RangingExp` DOUBLE(10,2) NOT NULL DEFAULT 0,`FisticuffsExp` DOUBLE(10,2) NOT NULL v," +
					"`ArcheryExp` DOUBLE(10,2) NOT NULL DEFAULT 0,`DiggingExp` DOUBLE(10,2) NOT NULL DEFAULT 0,`ForgeExp` DOUBLE(10,2) NOT NULL DEFAULT 0,PRIMARY KEY ( `id` )) ENGINE = MYISAM;");
			ResultSet rs = st.executeQuery("SELECT * FROM `ExperienceTable`;");
			ResultSetMetaData rsMetaData = (ResultSetMetaData) rs.getMetaData();
			int numcolumns = rsMetaData.getColumnCount();
			if(!(numcolumns==maxcolumns)){
				if(numcolumns==5){
					st.executeUpdate("ALTER TABLE ExperienceTable ADD RangingExp DOUBLE(10,2) NOT NULL DEFAULT 0;");
					st.executeUpdate("ALTER TABLE ExperienceTable ADD FisticuffsExp DOUBLE(10,2) NOT NULL DEFAULT 0;");
					st.executeUpdate("ALTER TABLE ExperienceTable ADD ArcheryExp DOUBLE(10,2) NOT NULL DEFAULT 0;");
				}
				if(numcolumns==6){
					st.executeUpdate("ALTER TABLE ExperienceTable ADD FisticuffsExp DOUBLE(10,2) NOT NULL DEFAULT 0;");
					st.executeUpdate("ALTER TABLE ExperienceTable ADD ArcheryExp DOUBLE(10,2) NOT NULL DEFAULT 0;");
				}
				if(numcolumns==7){
					st.executeUpdate("ALTER TABLE ExperienceTable ADD ArcheryExp DOUBLE(10,2) NOT NULL DEFAULT 0;");
				}
				if(numcolumns==8){
					st.executeUpdate("ALTER TABLE ExperienceTable ADD DiggingExp DOUBLE(10,2) NOT NULL DEFAULT 0;");
				}
				if(numcolumns==9){
					st.executeUpdate("ALTER TABLE ExperienceTable ADD ForgeExp DOUBLE(10,2) NOT NULL DEFAULT 0;");
				}
			}
		} catch (SQLException ex) {
			log.severe("[Levelcraft]: Could not create table for mysql" + ": "
					+ ex);
			return;
		} catch (ClassNotFoundException e) {
			log.severe("[Levelcraft]: Database connector not found for mysql"
					+ ": " + e);
			return;
		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				log.severe("[Levelcraft]: Failed to close connection");
			}
		}
	}

	public  void NewPlayer(CommandSender sender, double var) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Connection conn = null;
			Statement st = null;
			String p = player.getName();
			try {
				conn = createConnection();
				st = (Statement) conn.createStatement();
				st.executeUpdate("INSERT INTO ExperienceTable (PlayerName,WoodcuttingExp,MiningExp,SlayingExp,RangingExp,FisticuffsExp,ArcheryExp,DiggingExp,ForgeExp) VALUES ('"
						+ p + "'," + var + "," + var + "," + var + "," + var + "," + var + "," + var + "," + var + "," + var + ")");
			} catch (SQLException ex) {
				log.severe("[Levelcraft]: Could not insert row for mysql" + ": "
						+ ex);
				return;
			} catch (ClassNotFoundException e) {
				log.severe("[Levelcraft]: Database connector not found for mysql"
						+ ": " + e);
				return;
			} finally {
				try {
					if (st != null) {
						st.close();
					}
					if (conn != null)
						conn.close();
				} catch (SQLException ex) {
					log.severe("[Levelcraft]: Failed to close connection");
				}
			}
		} else {
			sender.sendMessage("Error: Could not create player!");
		}
	}

	public  double getExp(CommandSender sender, String value) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Connection conn = null;
			Statement st = null;
			String p = player.getName();
			double exp = 0;
			try {

				conn = createConnection();

				st = (Statement) conn.createStatement();
				ResultSet rs = st.executeQuery("SELECT " + value
						+ " FROM ExperienceTable WHERE PlayerName=('" + p + "')");
				while (rs.next()) {
					exp = rs.getDouble(value);
				}
				return exp;
			} catch (SQLException e) {
				log.severe("[Levelcraft] Unable to getExp player row database" + e);
			} catch (ClassNotFoundException e) {
				log.severe("[Levelcraft] Unable to getExp player row database" + e);
			} finally {
				try {
					if (st != null) {
						st.close();
					}
					if (conn != null)
						conn.close();
				} catch (SQLException ex) {
					log.severe("[Levelcraft]: Failed to close connection");
				}
			}
			return exp;
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

				conn = createConnection();

				st = (Statement) conn.createStatement();
				ResultSet rs = st
				.executeQuery("SELECT PlayerName FROM ExperienceTable WHERE PlayerName=('"
						+ p + "')");
				while (rs.next()) {
					isTrue = true;
				}
				return isTrue;
			} catch (SQLException e) {
				log.severe("[Levelcraft] Unable to check player row database" + e);
			} catch (ClassNotFoundException e) {
				log.severe("[Levelcraft] Unable to check player row database" + e);
			} finally {
				try {
					if (st != null) {
						st.close();
					}
					if (conn != null)
						conn.close();
				} catch (SQLException ex) {
					log.severe("[Levelcraft]: Failed to close connection");
				}
			}
			return isTrue;
		} else {
			sender.sendMessage("Error: Player does not exist!");
			return false;
		}
	}

	public  void update(CommandSender sender, String value,double newvalue) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			Connection conn = null;
			Statement st = null;
			String p = player.getName();
			try {
				conn = createConnection();
				st = (Statement) conn.createStatement();

				st.executeUpdate("UPDATE ExperienceTable set "+value+" = '"+newvalue+"' WHERE PlayerName='"+p+"'");
			} catch (SQLException ex) {
				log.severe("[Levelcraft]: Could not delete row for mysql" + ": " + ex);
				return;
			} catch (ClassNotFoundException e) {
				log.severe("[Levelcraft]: Database connector not found for mysql" + ": "
						+ e);
				return;
			} finally {
				try {
					if (st != null) {
						st.close();
					}
					if (conn != null)
						conn.close();
				} catch (SQLException ex) {
					log.severe("[Levelcraft]: Failed to close connection");
				}
			}
		} else {
			sender.sendMessage("Error: Could not update player!");
		}
	}
	public  void UpdateRow() {
		Connection conn = null;
		Statement st = null;
		try {
			conn = createConnection();
			st = (Statement) conn.createStatement();

			st.executeUpdate("UPDATE Players SET player='Noob' WHERE id='2'");
		} catch (SQLException ex) {
			log.severe("[MySql]: Could not update row for mysql" + ": " + ex);
			return;
		} catch (ClassNotFoundException e) {
			log.severe("[MySql]: Database connector not found for mysql" + ": "
					+ e);
			return;
		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				log.severe("[MySql]: Failed to close connection");
			}
		}
	}

	public  String GetRow() {
		Connection conn = null;
		Statement st = null;
		String name = "NULL";
		try {
			conn = createConnection();
			st = (Statement) conn.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT player FROM Players WHERE id='2'");
			while (rs.next()) {
				name = rs.getString("player");
			}
		} catch (SQLException ex) {
			log.severe("[MySql]: Could not update row for mysql" + ": " + ex);

		} catch (ClassNotFoundException e) {
			log.severe("[MySql]: Database connector not found for mysql" + ": "
					+ e);

		} finally {
			try {
				if (st != null) {
					st.close();
				}
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				log.severe("[MySql]: Failed to close connection");
			}
		}
		return name;
	}
}

