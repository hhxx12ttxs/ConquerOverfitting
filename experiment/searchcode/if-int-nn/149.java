package jdbctest;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.Statement;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class HW2 {

	private static final String DBUSER = "scott";
	private static final String DBDRIVER = "oracle.jdbc.driver.OracleDriver";
	private static final String DBPASSWORD = "tiger";
	private static final String DBNAME = "jdbc:oracle:thin:@localhost:1521:orcl";

	public static void main(String[] args) {
		System.out.println("Executing HW2.java");
		try {
			Connection con = HW2.getConnectionInstance();
			if (con != null) {
				String query = objType(args[1], args[0]);
				if(query == null){
					System.out.println("INVALID OBJECT TYPE ARGUMENT");
					try {
						HW2.closeConnection(con);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.exit(0);
				}
				// System.out.println("QUERY BUILT:" + query);
				if (args[0].equalsIgnoreCase("window")) {
					// System.out.println("WINDOW QUERY");
					int x1 = Integer.parseInt(args[2].trim());
					int x2 = Integer.parseInt(args[3].trim());
					int y1 = Integer.parseInt(args[4].trim());
					int y2 = Integer.parseInt(args[5].trim());
					// System.out.println(x1+" "+x2+" "+y1+" "+y2+" " );
					query = query
							+ "WHERE SDO_INSIDE(shape, SDO_GEOMETRY(2003, NULL, NULL,SDO_ELEM_INFO_ARRAY(1,1003,3), SDO_ORDINATE_ARRAY(?, ?, ?, ?))) = 'TRUE')";

					PreparedStatement psmt = con.prepareStatement(query);
					psmt.setInt(1, x1);
					psmt.setInt(2, x2);
					psmt.setInt(3, y1);
					psmt.setInt(4, y2);
					// System.out.println("QUERY BUILT:" + query);
					String psmtStr = psmt.toString();
					// System.out.println(psmtStr);

					ResultSet rs = psmt.executeQuery();
					System.out.println("Building ID ");
					int loop = 0;
					while (rs.next()) {
						loop++;
						String BIDwin = rs.getString("BID");
						System.out.println(BIDwin);
					}
					if (loop == 0) {
						System.out.println("NULL");
					}

				} else if (args[0].equalsIgnoreCase("within")) {
					// System.out.println("WITHIN QUERY");
					String BName = args[2].trim();
					int radius = Integer.parseInt(args[3].trim());

					String innerQ = "select SHAPE from buildings where BNAME = '"
							+ BName + "'";
					// System.out.println("Inner QUERY BUILT:" + innerQ);
					PreparedStatement Innerpsmt = con.prepareStatement(innerQ);

					// System.out.println("Inner QUERY BUILT:" + innerQ);

					String InnerpsmtStr = Innerpsmt.toString();
					ResultSet Innerrs = Innerpsmt.executeQuery();

					while (Innerrs.next()) {

						// System.out.println("Outer Query");

						STRUCT st = (oracle.sql.STRUCT) Innerrs
								.getObject("SHAPE");
						// convert STRUCT into geometry
						// JGeometry j_geom = JGeometry.load(st);
						// System.out.println(st);
						// System.out.println(j_geom);

						query = query
								+ " H WHERE SDO_WITHIN_DISTANCE(H.SHAPE, ?, 'distance="
								+ radius + "') = 'TRUE')";
						System.out.println("QUERY BUILT:" + query);

						PreparedStatement psmt = con.prepareStatement(query);
						psmt.setObject(1, st);

						String psmtStr = psmt.toString();
						ResultSet rs = psmt.executeQuery();
						System.out.println("Building ID ");
						int loop = 0;
						while (rs.next()) {
							loop++;
							String BIDin = rs.getString("BID");
							System.out.println(BIDin);
						}
						if (loop == 0) {
							System.out.println("NULL");
						}
						// System.out.println(rs);
					}
				} else if (args[0].equalsIgnoreCase("nn")) {
					// System.out.println("NN QUERY");
					String BID = args[2].trim();
					int nn = Integer.parseInt(args[3].trim());
					query = query
							+ "WHERE SDO_NN(SHAPE, (select shape from buildings where BID = ?), 'sdo_num_res ="
							+ nn + "') = 'TRUE')";
					// System.out.println("QUERY BUILT:" + query);
					PreparedStatement psmt = con.prepareStatement(query);
					psmt.setString(1, BID);
					// psmt.setInt(2, nn);

					String psmtStr = psmt.toString();
					ResultSet rs = psmt.executeQuery();
					System.out.println("Building ID ");
					int loop = 0;
					while (rs.next()) {
						loop++;
						String BIDnn = rs.getString("BID");
						System.out.println(BIDnn);
					}
					if (loop == 0) {
						System.out.println("NULL");
					}
					// System.out.println(rs);
				} else if (args[0].equalsIgnoreCase("demo")) {
					// System.out.println("DEMO");
					if (args[1].equalsIgnoreCase("1")) {
						String demoQ = "Select DISTINCT B.BNAME From Buildings B, FIREB F Where B.BNAME LIKE 'S%' MINUS SELECT F.BNAME FROM FIREB F";
						Statement stmt = null;
						stmt = con.createStatement();
						ResultSet rsDemo = stmt.executeQuery(demoQ);
						System.out.println("Building Name");

						int loop = 0;
						while (rsDemo.next()) {
							loop++;
							String BName = rsDemo.getString(1);
							System.out.println(BName);
						}
						if (loop == 0) {
							System.out.println("NULL");
						}
					} else if (args[1].equalsIgnoreCase("2")) {
						String demoQ = "SELECT F.BNAME, H.BID FROM HYDRANT H, FIREB F WHERE SDO_NN(H.SHAPE, F.SHAPE, 'sdo_num_res =5') = 'TRUE'";
						Statement stmt = null;
						stmt = con.createStatement();
						ResultSet rsDemo = stmt.executeQuery(demoQ);
						System.out.println("BuildingName HydrantID");
						int loop = 0;
						while (rsDemo.next()) {
							loop++;
							String BName = rsDemo.getString(1);
							String BID = rsDemo.getString(2);
							System.out.println(BName + "\t\t" + BID);
						}
						if (loop == 0) {
							System.out.println("NULL");
						}
					} else if (args[1].equalsIgnoreCase("3")) {
						String demoQ = "SELECT H.BID, COUNT(B.BID) as NUMB FROM BUILDINGS B, HYDRANT H WHERE SDO_WITHIN_DISTANCE(B.SHAPE, H.SHAPE, 'distance=120') = 'TRUE' GROUP BY h.bid HAVING COUNT(B.BID) = (SELECT MAX(NUMB) FROM (SELECT H.BID, COUNT(B.BID) as NUMB FROM BUILDINGS B, HYDRANT H WHERE SDO_WITHIN_DISTANCE(B.SHAPE, H.SHAPE, 'distance=120') = 'TRUE' GROUP BY h.bid))";
						Statement stmt = null;
						stmt = con.createStatement();
						ResultSet rsDemo = stmt.executeQuery(demoQ);
						System.out.println("BID   Count");
						int loop = 0;
						while (rsDemo.next()) {
							loop++;
							String BID = rsDemo.getString(1);
							String Count = rsDemo.getString(2);
							System.out.println(BID + "\t" + Count);
						}
						if (loop == 0) {
							System.out.println("NULL");
						}
					} else if (args[1].equalsIgnoreCase("4")) {
						String demoQ = "SELECT * FROM (SELECT H.BID, COUNT(*) FROM BUILDINGS B, HYDRANT H WHERE SDO_NN(H.SHAPE, B.SHAPE, 'SDO_NUM_RES=1')='TRUE' GROUP BY H.BID ORDER BY COUNT(*) DESC) WHERE ROWNUM<=5";
						Statement stmt = null;
						stmt = con.createStatement();
						ResultSet rsDemo = stmt.executeQuery(demoQ);
						System.out.println("FireHydrantID Count");
						int loop = 0;
						while (rsDemo.next()) {
							loop++;
							String BID = rsDemo.getString(1);
							String Count = rsDemo.getString(2);
							System.out.println(BID + "\t\t" + Count);
						}
						if (loop == 0) {
							System.out.println("NULL");
						}
					} else if (args[1].equalsIgnoreCase("5")) {
						String demoQ = "select rownum coord_seq, column_value coord from table(SELECT SDO_AGGR_MBR(B.SHAPE).sdo_ordinates FROM BUILDINGS B WHERE B.BNAME LIKE '%HE')";
						Statement stmt = null;
						stmt = con.createStatement();
						ResultSet rsDemo = stmt.executeQuery(demoQ);

						int loop = 0;
						while (rsDemo.next()) {
							loop++;
							String BID = rsDemo.getString(1);
							String Coor = rsDemo.getString(2);
							System.out.println(Coor);
						}
						if (loop == 0) {
							System.out.println("NULL");
						}
					}
					else{
						System.out.println("INVALID DEMO NUMBER");
						try {
							HW2.closeConnection(con);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.exit(0);
					}
					
				}
				else{
					System.out.println("INVALID QUERY TYPE ARGUMENT");
					try {
						HW2.closeConnection(con);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.exit(0);
				}
					
			}
			HW2.closeConnection(con);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public static String objType(String objtype, String arg0) {

		String selectObj = null;

		if (objtype.equalsIgnoreCase("building")) {
			selectObj = new String("(select bid from buildings ");
		} else if (objtype.equalsIgnoreCase("firehydrant")) {
			selectObj = new String("(select bid from hydrant ");
		} else if (objtype.equalsIgnoreCase("firebuilding")) {
			selectObj = new String("(select bid from fireb ");
		} else if (arg0.equalsIgnoreCase("demo")) {
			selectObj = new String("demo");
		} 
		// System.out.println("SELECT STATEMENT" + selectObj);
		return selectObj;

	}

	public static Connection getConnectionInstance() throws Exception {
		try {
			Class.forName(DBDRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Connection con = null;
		try {
			System.out.println("Establishing connection to DB");
			con = DriverManager.getConnection(DBNAME, DBUSER, DBPASSWORD);
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return con;
	}

	public static void closeConnection(Connection connection) throws Exception {
		try {
			System.out.println("Closing connection to DB");
			connection.close();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
}

