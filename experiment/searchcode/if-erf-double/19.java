/**
 * 
 */
package cmg.bp.pl.migration.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cmg.bp.pl.migration.bean.MemberBean;
import cmg.bp.pl.migration.db.JDBCConnector;

/**
 * @author BachLe
 *
 */
public class CalcFactory {
	
	private long startTime;
	private long calcRunStart;
	private Connection conn = null;
	private MemberBean memberBean;
	
	public static int colNameMaxLength;
	
	String[] calcTypes = {"94", "95", "96", "97", "99"};
	String[] calcNames = {"DC", "WC", "RD", "RR", "CR"};
	
	public CalcFactory(Connection con, MemberBean user, int colNameMaxLength) {
		conn = con;
		memberBean = user;
		this.colNameMaxLength = colNameMaxLength;
	}
	
	public MemberBean runAllCalc(MemberBean user) throws SQLException, Exception {
		java.sql.Date NRD = Utility.getDate(String.valueOf(user.getMemberData("Nrd")));
		java.sql.Date NPD = Utility.getDate(String.valueOf(user.getMemberData("Npd")));
		int CINN91 = Utility.getInt(user.getMemberData("CINN91"));
		int NPA = Utility.getInt(user.getMemberData("Npa"));
		int NRA = Utility.getInt(user.getMemberData("Nra"));
		double fte = Utility.getDouble(user.getMemberData("FTE"));
		
		Date DoC = new Date(System.currentTimeMillis());
		
		calculateAdjust(NRD, CINN91, 0, null, CINN91, fte, DoC);
		
		return memberBean;
	}
	
	
	public void calculateAdjust(Date DoR, int accrual_rate, double cash, 
							    Date overrideAccDate, int overrideAccRate, double fte,
							    Date DoC) throws SQLException, Exception{

		String userid = memberBean.getMemberData(MemberBean.MEMBER_REFNO);//String.valueOf(this.getMemberBean().get(Environment.MEMBER_REFNO));
		userid = userid == null ? new String("") : userid;

		String username = memberBean.getMemberData(JDBCConnector.CONNECTION_USERNAME);
		String password = memberBean.getMemberData(JDBCConnector.CONNECTION_PASSWORD);
		
		memberBean.valueMap.remove(JDBCConnector.CONNECTION_USERNAME);
		memberBean.valueMap.remove(JDBCConnector.CONNECTION_PASSWORD);
		
		String bGroup = memberBean.getMemberData(MemberBean.MEMBER_GROUP);
		String refNo  = userid;

		String agCode = null;//String.valueOf(this.getMemberBean().get(Environment.MEMBER_AGCODE));
		agCode = agCode == null ? new String("BP01") : agCode;
		
		double _accrual_rate = Utility.getDouble(String.valueOf(accrual_rate));
		
		/************************* Checking lock for calc: HUY ***************/
		/***** Checking lock finished ****/	
		
		for (int i=0; i<calcTypes.length; i++) {
			deleteCalOutput(userid, calcTypes[i], bGroup);
			deleteCal(username, userid, calcTypes[i], bGroup); //correct
			deleteCentralSession(username, bGroup);
			
			if ("99".equals(calcTypes[i])) { //CR CALC
				insertCRCalcInput(calcTypes[i], refNo, bGroup, DoR, cash);
				
			} else if ("97".equals(calcTypes[i])) { //RR
				insertRRCalcInput(calcTypes[i], refNo, bGroup, DoC);
				
			} else if ("96".equals(calcTypes[i])) { //RD
				insertRDCalcInput(calcTypes[i], refNo, bGroup, DoC);
				
			} else if ("95".equals(calcTypes[i])) { //WC
				insertWCCalcInput(calcTypes[i], refNo, bGroup);
				
			} else if ("94".equals(calcTypes[i])) { //DC
				insertDCCalcInput(calcTypes[i], refNo, bGroup);
			}
			
			inserIntoSession(username, password, bGroup, agCode); //correct
			
			runProcess(bGroup, username, refNo, calcTypes[i], password, DoR); //working after being corrected
		}
		
		System.out.println("\nPlease wait while processing sql and calculations for user "+refNo+" in group "+bGroup);
		for (int num=0; num<10; num++) {
			System.out.print(". ");
			Thread.sleep(500);
		}
		for (int j=0; j<calcTypes.length; j++) {
			System.out.print(". ");
			saveCalcResult(bGroup, refNo, calcTypes[j], cash, overrideAccDate, overrideAccRate, fte);
		}
	}
	
	/**
	 * @author HUY
	 * Check if a lock has been set for this calculation of given user.
	 * @param bGroup
	 * @param userName
	 * @param refNo
	 * @param calcType
	 * @param agcode
	 * @return STATUS of the lock: 	0 - lock is released; 
	 * 								1 - lock has been used; 
	 * 								-1 - lock has been time out
	 * 								
	 */
	public int getCalcLockStatus(Connection cmsCon, String bGroup, String refNo, String calcType, String agcode) {
		int status = 0;
		String sqlSelect = "Select started from bp_calc_lock where BGroup=? and Refno=?";
		try {
			startTime = System.currentTimeMillis();
			PreparedStatement pstm = cmsCon.prepareStatement(sqlSelect);

			// set all the parameters into the insert query
			/** TODO complete the rest of setting parameters. Be aware of the type */

			pstm.setString(1, bGroup);
			pstm.setString(2, refNo);
			//pstm.setString(3, calcType);

			ResultSet rs = pstm.executeQuery();
			if (rs.next())
			{
				String startedStr = rs.getString("started");
				Long started = new Long(startedStr);
				long now = System.currentTimeMillis();
				Long calcOld = -1L;//new Long (CheckConfigurationKey.getStringValue(Environment.MEMBER_CALCOLD));
				if ((now - started.longValue()) > calcOld.longValue())
				{
					status = -1;
				}
				else
				{
					status = 1;
				}
			}
			else
			{
				status = 0;
			}
			rs.close();
			pstm.close();
		}
		catch (SQLException e){
			status = 0;
		}
		return status;
	}
	
	private void deleteCalOutput( String userid, String calctype, String bgroup) throws SQLException {
		   // Modify by Huy: remove try catch block. Add deleteCalOutputRecord( "CALC_ERRORS", userid, calctype, bgroup);
		deleteCalOutputRecord( "CALC_OUTPUT", userid, calctype, bgroup);
		deleteCalOutputRecord( "CALC_OUTPUT_2", userid, calctype, bgroup);
		deleteCalOutputRecord( "CALC_OUTPUT_3", userid, calctype, bgroup);
		deleteCalOutputRecord( "CALC_OUTPUT_4", userid, calctype, bgroup);
		deleteCalOutputRecord( "CALC_OUTPUT_5", userid, calctype, bgroup);
		deleteCalOutputRecord( "CALC_ERRORS", userid, calctype, bgroup);
	}
	
	private void deleteCalOutputRecord( String tablename, String userid, String calctype, String bgroup) throws SQLException {
		String sqlDelete = "Delete from "+tablename+" where refno =? and  calctype = ? and  Bgroup = ? ";
		PreparedStatement pstm = null;
		  try {
		      conn.setAutoCommit(false);
		      pstm = conn.prepareStatement(sqlDelete);
		      pstm.setString(1, userid);
		      pstm.setString(2, calctype);
		      pstm.setString(3, bgroup);
		      pstm.execute();
		      conn.commit();
		      conn.setAutoCommit(true);
		      
		  } catch (SQLException e) {
			   //releaseLock(bgroup, userid, calctype);
		      try {
		    	  conn.rollback();
		      } catch (SQLException ex) {
		          throw ex;
		      }
		      e.printStackTrace();
		      throw e;
		      
		  } finally {
		      if (conn != null) {
		          try {
		          } catch (Exception e) {
		          }
		      }
		  }
		  
	}
	
	/**
	 * @param userid
	 * @param calctype
	 * @param bgroup
	 *            Delete from calc_input table
	 */
	private void deleteCal(String username, String userid, String calctype, String bgroup) throws SQLException {
		String sqlDelete = "Delete from calc_input where refno =? and  calctype = ? and  BGROUP = ? ";
		String sqlDelSess = "Delete from central_session where userid = ? and  BGROUP = ?";
		PreparedStatement pstm = null;
		PreparedStatement ps = null;
		try {
			conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sqlDelete);
			pstm.setString(1, userid);
			pstm.setString(2, calctype);
			pstm.setString(3, bgroup);				
			pstm.execute();
			
			/*
			ps = conn.prepareStatement(sqlDelSess);
			ps.setString(1, username);
			ps.setString(2, bgroup);
			ps.execute();
			*/
			
			conn.commit();
			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			//releaseLock(bgroup, userid, calctype);
			// TODO: handle exception
			try {
				conn.rollback();
			} catch (SQLException ex) {
				throw ex;
			}
			e.printStackTrace();
			throw e;
		} finally {
			if (conn != null) {
				try {
					
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}
	
	private void deleteCentralSession(String username, String bgroup) throws SQLException {
		String sqlDelSess = "Delete from central_session where userid = ? and  BGROUP = ?";
		PreparedStatement ps = null;
		try {
			conn.setAutoCommit(false);
			
			ps = conn.prepareStatement(sqlDelSess);
			ps.setString(1, username);
			ps.setString(2, bgroup);
			ps.execute();
		
			conn.commit();
			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			// TODO: handle exception
			try {
				conn.rollback();
			} catch (SQLException ex) {
				throw ex;
			}
			e.printStackTrace();
			throw e;
		} 
	}
	
	private void insertRDCalcInput(String calType, String refNo, String bGroup, java.sql.Date rdDate) throws SQLException{
		String sqlInsert = "insert into calc_input(CalcType,Refno,BGroup,CIND01, CINN50) values(?,?,?,?,0) ";
	
		PreparedStatement pstm = null;
		try {
			conn.setAutoCommit(false);

			pstm = conn.prepareStatement(sqlInsert);
			pstm.setString(1, calType);
			pstm.setString(2, refNo);
			pstm.setString(3, bGroup);
			pstm.setDate(4, rdDate);
			pstm.executeUpdate();
			
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			//releaseLock(bGroup, refNo, calType);
			
			try {
				conn.rollback();
				throw e;
			} catch (SQLException ex) {
				throw ex;
			}
		}
	}
	
	private void insertWCCalcInput(String calType, String refNo, String bGroup) throws SQLException {
		String sqlInsert = "insert into calc_input(CalcType,Refno,BGroup,CIND01) " +
							"values(?,?,?,(SELECT TO_CHAR(SYSDATE,'DD-MON-YYYY') systemdate FROM dual))";

		PreparedStatement pstm = null;
		try {
			conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sqlInsert);
			pstm.setString(1, calType);
			pstm.setString(2, refNo);
			pstm.setString(3, bGroup);
			
			pstm.executeUpdate();
			conn.commit();
			conn.setAutoCommit(true);

		} catch (SQLException e) {
			//releaseLock(bGroup, refNo, calType);
			// TODO: handle exception
			try {
				conn.rollback();
				throw e;
			} catch (SQLException ex) {
				throw ex;
			}

		}
	}
	
	private void insertDCCalcInput(String calType, String refNo, String bGroup) throws SQLException {
		String sqlInsert = "insert into calc_input(CalcType,Refno,BGroup,CIND01,CINI01) " +
				"values(?,?,?,(SELECT TO_CHAR(SYSDATE,'DD-MON-YYYY') systemdate FROM dual),"
				+ "?) ";
		PreparedStatement pstm = null;
		try {
			conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sqlInsert);
			pstm.setString(1, calType);
			pstm.setString(2, refNo);
			pstm.setString(3, bGroup);
			pstm.setInt(4, 1); // Number of dependants - will always be 1
	
			pstm.executeUpdate();
			conn.commit();
			conn.setAutoCommit(true);

		} catch (SQLException e) {
			//releaseLock(bGroup, refNo, calType);
			// TODO: handle exception
			try {
				conn.rollback();
				throw e;
				
			} catch (SQLException ex) {
				throw ex;
			}
		}
	}
	
	private void insertRRCalcInput(String calType, String refNo, String bGroup,java.sql.Date rdDate) throws SQLException {
		String sqlInsert = "insert into calc_input(CalcType,Refno,BGroup,CIND01, CINN50, CINN51) " +
							"values(?,?,?,?,0,0) ";
		
		PreparedStatement pstm = null;
		try {
			conn.setAutoCommit(false);

			pstm = conn.prepareStatement(sqlInsert);
			pstm.setString(1, calType);
			pstm.setString(2, refNo);
			pstm.setString(3, bGroup);
			pstm.setDate(4, rdDate);
			pstm.executeUpdate();
			
			conn.setAutoCommit(true);
		} catch (Exception e) {
			
			try {
				conn.rollback();
			} catch (Exception ex) {
				// TODO: handle exception				
			}
		}
	}
	
	private void insertCRCalcInput(String calType, String refNo, String bGroup, Date DoR, double cash) throws SQLException {
		String sqlInsert = "insert into calc_input(CalcType, Refno, BGroup, CINN20, CIND01, CINN50, CINN51) " +
				"values(?, ?, ?, ?, ?, 0, 0) ";
		PreparedStatement pstm = null;
		try {
			conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sqlInsert);
			pstm.setString(1, calType);
			pstm.setString(2, refNo);
			pstm.setString(3, bGroup);
			pstm.setDouble(4, cash);
			pstm.setDate(5, DoR);  //2020-06-30
			
			pstm.executeUpdate();
			conn.commit();
			conn.setAutoCommit(true);
			//System.out.println("insert calc input OK");
		} catch (SQLException sle) {
			//releaseLock(bGroup, refNo, calType);
			// TODO: handle exception
			try {
				conn.rollback();
			} catch (Exception ex) {
				// TODO: handle exception
			}
			
			throw sle;
		} finally {
			if (conn != null) {
				try {
					
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}
	
	/**
	 * @param userid
	 * @param password
	 * @param bGroup
	 * @param agcode
	 *            Insert into centeral_session table
	 */
	private void inserIntoSession(String userid, String password, String bGroup, String agcode) throws SQLException {
		String sqlInsert = "insert into central_session (userid,password,session_number,start_date,start_time, bgroup,logon_id,AGCODE)"
				+ "values (?,?,0,(SELECT TO_CHAR(SYSDATE,'DD-MON-YYYY') systemdate FROM dual),'00:00:00',?,'INTERNAL',?)";
		PreparedStatement pstm = null;
		boolean sessionExist = checkCentralSession(userid, password, bGroup, agcode);
		if (!sessionExist){
		try {
			conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sqlInsert);
			pstm.setString(1, userid);
			pstm.setString(2, password);
			pstm.setString(3, bGroup);
			pstm.setString(4, agcode);
			
			boolean row = pstm.execute();

			conn.commit();
			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
			}
			throw e;
		}
		}
	}
	
	/** check whether Session row has been created */
	private boolean checkCentralSession(String userid, String password, String bGroup, String agcode){
		boolean sessionExist = false;
		
		String sqlInsert = " select * from central_session where userid = '"+userid+"' and password = '"+password+"' and " +
				"bgroup = '"+bGroup+"' and agcode = '"+agcode+"'";
		
		/*
		String sqlInsert = " select * from central_session where userid = '"+userid+"' and " +
		"bgroup = '"+bGroup+"' and agcode = '"+agcode+"'";
		*/
		
		Statement pstm = null;
		try {
			pstm = conn.createStatement();	
			ResultSet rs;
			rs = pstm.executeQuery(sqlInsert);
			
			if (rs.next()) {
				sessionExist = true;
			}

		} catch (SQLException e) {
		} finally {
			if (conn != null) {
				try {
			
				} catch (Exception e) {
				}
			}
		}
		return sessionExist;
	}
	
	public void deleteAdministratorLocks(String refNo, String bGroup) {
		   // delete from lock_table where bgroup = $bgroup and lock_refno = (select crefno from basic where refno = refno and bgroup = bgroup);
		String sqlInsert = "delete from lock_table where bgroup = ? and lock_refno = (select crefno from basic where refno = ? and bgroup = ?)";

		PreparedStatement pstm = null;
		try {
			conn.setAutoCommit(false);
			pstm = conn.prepareStatement(sqlInsert);
			pstm.setString(1, bGroup);
			pstm.setString(2, refNo);
			pstm.setString(3, bGroup);
			pstm.execute();
			conn.commit();
			conn.setAutoCommit(true);

		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (Exception ex) {
			}

		} finally {
			if (conn != null) {
				try {
					
				} catch (Exception e) {
				}	
			}
		}
	}
	
	/**
	 * 
	 * 
	 * @param bGroup
	 * @param userName
	 * @param refNo
	 * @param calcType
	 * @param password
	 * @param status
	 */
	
	private void runProcess(String bGroup, String userName, String refNo,
							String calcType, String password, Date DoC) throws SQLException {
		//System.out.println("start running calc for "+refNo+" in group "+bGroup+ " with username and pass: "+userName+"-"+password);
		CallableStatement cs = null;
		try {
            String procedureQuery = "declare returnStatus NUMBER; errorInfo BP1.pmsapi_err.error_info_tab_type;"
            	 				    +"begin BP1.PMSAPI_CALC.run_process(?, ?, ?,?, ?,returnStatus, errorInfo); end;";			
            conn.setAutoCommit(false);
			
			cs = conn.prepareCall(procedureQuery);
			cs.setString(1, bGroup);
			cs.setString(2, userName);
			cs.setString(3, password);
			cs.setString(4, refNo);
			cs.setString(5, calcType);
					   	
			//LOG.info("begin  BP1.PMSAPI_CALC.run_process("+bGroup+", "+userName+", "+password+", "+refNo+","+calcType+",?,?); end;");			
			cs.execute();
			conn.commit();
			conn.setAutoCommit(true);
			//System.out.println("finish running calc");
			//LOG.debug("runProcess has been done !!!");
		} catch (SQLException sle) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				throw ex;
			}
			System.out.println("Calculation error: "+sle.getMessage());
			throw sle;
			
		} finally {
			if (conn != null) {
				try {
			
				} catch (Exception e) {
				}
			}
		}
	}
	
	public void saveCalcResult(String bGroup, String refNo, String calcType, double cash, 
		    				   Date overrideAccDate, int overrideAccRate, double fte) throws SQLException, Exception{
		//deleteFromSession(username); //correct
		// HUY: Set start time
		calcRunStart = System.currentTimeMillis();
		String calcName = "";
		Long waitTimeout = 60000L;
		
		if ("99".equals(calcType)) {
			// Modify the query to get FPS, DoR, CurrentAccural, ComFactor, from calc_output for adjustment
			String sqlSelect = "SELECT CALC_OUTPUT_2.Bgroup,CALC_OUTPUT_2.refno,CALC_OUTPUT_2.CALCTYPE,CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,CALC_OUTPUT.COUN42  Pension,"
				+ " CALC_OUTPUT.COUN0K PensionToDate,"
				+ "CALC_OUTPUT.COUN91 UnreducedPension,"
				+ "CALC_OUTPUT.COUN1V VeraIndicator,"
				+ "CALC_OUTPUT.COUN42 ReducedPension, CALC_OUTPUT.COUN43 SpousesPension, CALC_OUTPUT_2.CO2N65 CashLumpSum,"
				+ "CALC_OUTPUT_3.CO3N04 MaximumCashLumpSum, CALC_OUTPUT_2.CO2N36 PensionWithChosenCash,"
				+ "CALC_OUTPUT_3.CO3N05 PensionWithMaximumCash, "
				+ "CALC_OUTPUT.COUN41 FPS, " 
				+ "CALC_OUTPUT_5.CO5D14 DoR, " 
				+ "CALC_OUTPUT_5.CO5C03 CurrentAccural, "
				+ "CALC_OUTPUT.COUN1N ERF1, CALC_OUTPUT.COUN1O ERF2, "
				+ "CALC_OUTPUT_4.CO4N03 ComFactor " 
				+ "FROM CALC_OUTPUT,CALC_OUTPUT_2,CALC_OUTPUT_3,CALC_OUTPUT_4,CALC_OUTPUT_5 "
				+ "WHERE CALC_OUTPUT.Bgroup=CALC_OUTPUT_2.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE"
				+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_3.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE"
				+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_4.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_4.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_4.CALCTYPE"
				+ "  AND CALC_OUTPUT.Bgroup=CALC_OUTPUT_5.Bgroup  AND  CALC_OUTPUT.REFNO=CALC_OUTPUT_5.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_5.CALCTYPE"
				+ "  AND CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";

			PreparedStatement pstm = null;
			ResultSet rs = null;
			
			calcName = "_"+calcNames[4]+calcType+"@CALC";
			try {
				// set as a string
				String overfundIndicator = "false";
				String veraIndicator = "false";
				double veraIndicatorValue = 0;
				double unreducedPensionValue = 0;
				double reducedPensionValue = 0;
				
				pstm = conn.prepareStatement(sqlSelect);
				pstm.setString(1, bGroup);
				pstm.setString(2, refNo);
				pstm.setString(3, calcType);
				rs = pstm.executeQuery();
				
				//Initialise:
				/*
				memberBean.putCalcVal(MemberBean.Pension+calcName, " ");
				memberBean.putCalcVal(MemberBean.UnreducedPension+calcName," ");
				memberBean.putCalcVal(MemberBean.ReducedPension+calcName," ");
				memberBean.putCalcVal(MemberBean.SpousesPension+calcName," ");
				memberBean.putCalcVal(MemberBean.CashLumpSum+calcName, " ");
				memberBean.putCalcVal(MemberBean.CashLumpSumCurrency+calcName, " ");
				memberBean.putCalcVal(MemberBean.MaximumCashLumpSum+calcName," ");
				memberBean.putCalcVal(MemberBean.MaximumCashLumpSumExact+calcName," ");
				memberBean.putCalcVal(MemberBean.PensionWithChosenCash+calcName," ");
				memberBean.putCalcVal(MemberBean.PensionWithMaximumCash+calcName," ");
				*/
				//end initialise
				
				boolean calcResultFound = false;
				String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
				PreparedStatement pstmError = null;
				ResultSet rsError = null;			
				pstmError = conn.prepareStatement(sqlSelectError);
				pstmError.setString(1, bGroup);
				pstmError.setString(2, refNo);
				pstmError.setString(3, calcType);
				rsError = pstmError.executeQuery();			

				
				while (!calcResultFound){
					if (rs.next()) {
						//memberBean.putCalcVal(MemberBean.CALC_NAME, calcNames[2]+calcType+"@CALC");
						// return the reduced Pension as a double
						reducedPensionValue = rs.getDouble("REDUCEDPENSION");
						// return the unreduced Pension as a double
						unreducedPensionValue = rs.getDouble("UNREDUCEDPENSION");					
						/** Adjust Aquilla calculation results **/
						Date dateOfRetirement = rs.getDate("DoR");
						// get FPS
						double fps = rs.getDouble("FPS");
						// get current accural
						int currentAccural = rs.getInt("CurrentAccural") % 100;
						
						// detect ERF
						double erf1 = rs.getDouble("ERF1");
						double erf2 = rs.getDouble("ERF2");
						double erf = (erf1 > erf2) ? erf1: erf2;

						// get ComFactor
						double comFactor = rs.getDouble("ComFactor");
						
						// calculate service of years from overideAccDate: If input overrideAccDate = null
						// calculate it from the start day of next month
						float serviceYears = 0f;					
						
						// Calculate the amount of service years from accuralDate to DoR as float
						if (overrideAccDate == null){
							java.util.Date firstDayOfNextMonth = Utility.getFirstDayOfNextMonth();						
							serviceYears = Utility.getYearsBetweenAsFloat(firstDayOfNextMonth, dateOfRetirement);
						} else {
							serviceYears = Utility.getYearsBetweenAsFloat(overrideAccDate, dateOfRetirement);					
						}
						
						//	Adjust service Years for part time member
						serviceYears = serviceYears * (float)fte;
						
						// calculate the uplift
						double uplift = (serviceYears * fps * erf) * ((double)1/overrideAccRate - (double)1/currentAccural);
						
						// calculate new reduced pension
						double newReducedPension = reducedPensionValue + uplift;
						
						// calculate ltaLimit
						double ltaLimit = (2 * fps) / 3;
						
						// compare newReducedPension with ltaLimit to cap the reduced pension
						newReducedPension = (newReducedPension < ltaLimit) ? newReducedPension : ltaLimit;										
						
						// calculate new spouse pension: = 2/3 reducedPension
						double newSpousesPension = (newReducedPension * 2) / 3;
					
						// calculate scheme cash
						double schemeCash = (newReducedPension * 20)/(3 + (double)20/comFactor);
						
						// get LTA from calc output
						double lta = Utility.getDouble(memberBean.getMemberData("Lta"));
						
						// compare schemeCash with LTA/4 to get max scheme cash
						double newMaxSchemeCash = (schemeCash < (lta * 0.25)) ? schemeCash : (lta * 0.25);
						
						// calculate residual a max cash
						double residualMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						
						// calculate residual pension
						double residualPension = newReducedPension - (cash / comFactor);
						
						// calculate pension at max cash (in fact it is residualMaxCash
						double pensionMaxCash = newReducedPension - (newMaxSchemeCash / comFactor);
						
						// update new value to memberBean object
						memberBean.putCalcVal(MemberBean.Pension+calcName, Utility.getString((rs.getString("PENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.Pension, memberBean.get(MemberBean.Pension));
						
						memberBean.putCalcVal(MemberBean.UnreducedPension+calcName, Utility.getString(Utility.toLowestPound(rs.getDouble("UNREDUCEDPENSION"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.UnreducedPension, memberBean.get(MemberBean.UnreducedPension));
						
						memberBean.putCalcVal(MemberBean.ReducedPension+calcName, Utility.getString(Utility.toLowestPound(newReducedPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.ReducedPension, memberBean.get(MemberBean.ReducedPension));					
						
						memberBean.putCalcVal(MemberBean.SpousesPension+calcName, Utility.getString(Utility.toLowestPound(newSpousesPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.SpousesPension, memberBean.get(MemberBean.SpousesPension));
		
						memberBean.putCalcVal(MemberBean.CashLumpSum+calcName, Utility.getString(Utility.toNearestOne(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.CashLumpSum, memberBean.get(MemberBean.CashLumpSum));
						
						memberBean.putCalcVal(MemberBean.CashLumpSumCurrency+calcName, Utility.getString(Utility.toNearestPound(rs.getDouble("CASHLUMPSUM"))));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.CashLumpSumCurrency, memberBean.get(MemberBean.CashLumpSumCurrency));
		
						memberBean.putCalcVal(MemberBean.MaximumCashLumpSum+calcName, Utility.getString(Utility.toLowestThousand(newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.MaximumCashLumpSum, memberBean.get(MemberBean.MaximumCashLumpSum));
						
						if (this.colNameMaxLength < MemberBean.MaximumCashLumpSumExact.length()) {
							colNameMaxLength = MemberBean.MaximumCashLumpSumExact.length();
						}
						
						memberBean.putCalcVal(MemberBean.MaximumCashLumpSumExact+calcName, Utility.getString(Utility.toLowestPound(newMaxSchemeCash)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.MaximumCashLumpSumExact, memberBean.get(MemberBean.MaximumCashLumpSumExact));
						
						memberBean.putCalcVal(MemberBean.PensionWithChosenCash+calcName, Utility.getString(Utility.toLowestPound(residualPension)));
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.PensionWithChosenCash, memberBean.get(MemberBean.PensionWithChosenCash));
		
						memberBean.putCalcVal(MemberBean.PensionWithMaximumCash+calcName, Utility.getString(Utility.toLowestPound(pensionMaxCash)));					
						
						// check the Vera indicator and set to memberBean
						veraIndicatorValue = rs.getDouble("VeraIndicator");
						if (veraIndicatorValue == 1){
							veraIndicator = "true";
						}
						memberBean.putCalcVal(MemberBean.veraIndicator+calcName, veraIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.veraIndicator, memberBean.get(MemberBean.veraIndicator));
						
						// check the indicator and set to memberBean
						if (unreducedPensionValue > reducedPensionValue){
							overfundIndicator = "true";
						}
						memberBean.putCalcVal(MemberBean.overfundIndicator+calcName, overfundIndicator);
						//memDebug("BpAcCrConsumer.Calculate, resultSet>0", this, MemberBean.overfundIndicator, memberBean.get(MemberBean.overfundIndicator));
						
						//double bPs=NumberUtil.getDouble(String.valueOf(memberDao.get(MemberBean.BasicPs)));
						
						// UnreducedPension will return the tag Pension
						//double reducedPension = NumberUtil.getDouble(memberBean.get(MemberBean.Pension)); 
						
						double reducedPensionvsSalary = Utility.DEFAULT_DOUBLEVALUE;
						
						 // calculating the UnreducedPensionVsSalary for either NRA or NPA
						if (newReducedPension > Utility.DEFAULT_DOUBLEZEROVALUE && fps > Utility.DEFAULT_DOUBLEZEROVALUE) 
						{
							reducedPensionvsSalary = 100 * (newReducedPension / fps);
						}
							
						if (reducedPensionvsSalary > Utility.DEFAULT_DOUBLEZEROVALUE) 
						{

							memberBean.putCalcVal(MemberBean.ReducedPensionVsSalary+calcName, Utility.to2DpPercentage(reducedPensionvsSalary));
						} 
						else 
						{
							memberBean.putCalcVal(MemberBean.ReducedPensionVsSalary+calcName, " ");
						}	
						calcResultFound = true;
						
					
					} else if (rsError.next()){
						//TODO:
						String message = rsError.getString("message");
						calcResultFound = true;
						memberBean.putCalcVal("JDBC Error"+calcName, message);
						
					} else {					
						long now = System.currentTimeMillis();
						if ((now - calcRunStart) > waitTimeout.longValue()) {
							//LOG.info("BpAcCrConsumer.calculate: No data in result file -> run again");					
							memberBean.putCalcVal(MemberBean.veraIndicator+calcName, veraIndicator);
							//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberBean.veraIndicator, memberBean.get(MemberBean.veraIndicator));					
							memberBean.putCalcVal(MemberBean.overfundIndicator+calcName, overfundIndicator);
							//memDebug("BpAcCrConsumer.Calculate, resultSet<=0", this, MemberBean.overfundIndicator, memberBean.get(MemberBean.overfundIndicator));						
							break;
						}
						
						rs = pstm.executeQuery();
						rsError = pstmError.executeQuery();
					}
				}
				
				pstm.close();
				rs.close();
				rsError.close();
				pstmError.close();
				
				// calculation ends here
			} catch (SQLException sle) {
				System.out.println("Calculation error: "+sle.getMessage());
				throw sle;
				
			} finally {
				// HUY: Release lock when finish
				//releaseLock(bGroup, refNo, calType);
			}
			
		} else if ("94".equals(calcType)) {
			
			String sqlSelect = "SELECT CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,"
				+ " CALC_OUTPUT.COUN44 DeathInServiceCash,"
				+ "CALC_OUTPUT.COUN43 DeathInServicePension FROM CALC_OUTPUT "
				+ "WHERE CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";
			
			PreparedStatement pstm = null;
			ResultSet rs = null;
			
			try {
				pstm = conn.prepareStatement(sqlSelect);
				pstm.setString(1, bGroup);
				pstm.setString(2, refNo);
				pstm.setString(3, calcType);
				rs = pstm.executeQuery();
				
				boolean calcResultFound = false;
				String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
				PreparedStatement pstmError = null;
				ResultSet rsError = null;			
				pstmError = conn.prepareStatement(sqlSelectError);
				pstmError.setString(1, bGroup);
				pstmError.setString(2, refNo);
				pstmError.setString(3, calcType);
				rsError = pstmError.executeQuery();				
				
				calcName = "_"+calcNames[0]+calcType+"@CALC";
				while (!calcResultFound) { 			
					if (rs.next()) {
						//memberBean.putCalcVal(MemberBean.CALC_NAME, calcNames[0]+calcType+"@CALC");
						memberBean.putCalcVal(MemberBean.DeathInServiceCash+calcName, Utility
								.getString(Utility.toLowestPound(rs.getDouble("DeathInServiceCash"))));
		
						if (colNameMaxLength < MemberBean.DeathInServicePension.length()) {
							colNameMaxLength = MemberBean.DeathInServicePension.length();
						}
						memberBean.putCalcVal(MemberBean.DeathInServicePension+calcName, Utility
								.getString(Utility.toLowestPound(rs.getDouble("DeathInServicePension"))));
						
						calcResultFound = true;
					} 
					else if (rsError.next()) {
						memberBean.putCalcVal(MemberBean.DeathInServiceCash+calcName, Utility.EMPTY_STRING);
						memberBean.putCalcVal(MemberBean.DeathInServicePension+calcName, Utility.EMPTY_STRING);					
						calcResultFound = true;
						
						String message = rsError.getString("message");				
					}				
					else {
						long now = System.currentTimeMillis();
						
						if ((now - calcRunStart) > waitTimeout.longValue()) {
							memberBean.putCalcVal(MemberBean.DeathInServiceCash+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.DeathInServicePension+calcName, Utility.EMPTY_STRING);
													
							break;
						}
						
						rs = pstm.executeQuery();
						rsError = pstmError.executeQuery();
					}
				}
				pstm.close();
				rs.close();
				rsError.close();
				pstmError.close();		
				
			} catch (SQLException e) {
				// TODO: handle exception
				System.out.println("Calculation error: "+e.getMessage());
				throw e;
			}
			
		} else if ("95".equals(calcType)) {
			String sqlSelect = "SELECT CALC_OUTPUT.Bgroup,CALC_OUTPUT.refno,CALC_OUTPUT.CALCTYPE,"
				+ "CALC_OUTPUT.COUN0E PensionToDate FROM CALC_OUTPUT "
				+ "WHERE CALC_OUTPUT.Bgroup=? AND CALC_OUTPUT.Refno=? AND CALC_OUTPUT.CALCTYPE=? ";
	           
			PreparedStatement pstm = null;
			ResultSet rs = null;
			
			try {
				pstm = conn.prepareStatement(sqlSelect);
				pstm.setString(1, bGroup);
				pstm.setString(2, refNo);
				pstm.setString(3, calcType);
				rs = pstm.executeQuery();
						
				// PensionToDate value
				double p2date = 0;//Utility.getDouble(memberTemp.get(MemberBean.PensionToDate));
				boolean calcResultFound = false;
				String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
				PreparedStatement pstmError = null;
				ResultSet rsError = null;			
				pstmError = conn.prepareStatement(sqlSelectError);
				pstmError.setString(1, bGroup);
				pstmError.setString(2, refNo);
				pstmError.setString(3, calcType);
				rsError = pstmError.executeQuery();	
				
				calcName = "_"+calcNames[1]+calcType+"@CALC";
				while (!calcResultFound) {			
					if (rs.next()) {
						//memberBean.putCalcVal(MemberBean.CALC_NAME, calcNames[1]+calcType+"@CALC");
						p2date = rs.getDouble("PensionToDate");			
						memberBean.putCalcVal(MemberBean.PensionToDate+calcName, Utility.toLowestPound(p2date));
						
						calcResultFound = true;
					} 
					else if (rsError.next()) {
						memberBean.putCalcVal(MemberBean.PensionToDate+calcName, Utility.EMPTY_STRING);
						calcResultFound = true;
						String message = rsError.getString("message");				
					}				
					else {					
						long now = System.currentTimeMillis();
						if ((now - calcRunStart) > waitTimeout.longValue()) {
							memberBean.putCalcVal(MemberBean.PensionToDate+calcName, Utility.EMPTY_STRING);						
							break;
						}
						
						rs = pstm.executeQuery();
						rsError = pstmError.executeQuery();				
					}
				}
				pstm.close();
				rs.close();
				rsError.close();
				pstmError.close();			
				
				// FPS value
				double fps=Utility.getDouble(String.valueOf(memberBean.getMemberData(MemberBean.Fps)));
			
				// PensionVs Salary return the percentage
				double pensionVsSalary = Utility.DEFAULT_DOUBLEZEROVALUE;
				
				double pensionVsLta = Utility.DEFAULT_DOUBLEZEROVALUE;
							
				double totalVsLta = Utility.DEFAULT_DOUBLEZEROVALUE;
				
				double totalAvcVsLta = Utility.DEFAULT_DOUBLEZEROVALUE;
				
				// Life Time allowance value
				double lta = Utility.getDouble(memberBean.getMemberData("Lta"));
	           	
				/* TODO We will need to calculate the Total AVC by adding up all AVC sections (AVC, AVC transferred in
				* and bonus waiver. For now, we will set it to 0
				*/
				double totalAvc = Utility.DEFAULT_DOUBLEZEROVALUE; 
				
				String totalAvcString = memberBean.getMemberData(MemberBean.totalAvc);
				double totalAvcTagValue = Utility.getDouble(totalAvcString); //can be -1
				if (totalAvcTagValue > Utility.DEFAULT_DOUBLEZEROVALUE){
					totalAvc = totalAvcTagValue;
				}
					
			    
	            // calculating the PensionVsSalary and set to MemberBean object
				if (p2date > Utility.DEFAULT_DOUBLEZEROVALUE && fps > Utility.DEFAULT_DOUBLEZEROVALUE) {
					pensionVsSalary = 100 * (p2date / fps);
				}
				
				if (colNameMaxLength < MemberBean.PensionableSalary.length()) {
					colNameMaxLength = MemberBean.PensionableSalary.length();
				}
				if (pensionVsSalary > Utility.DEFAULT_DOUBLEZEROVALUE) {
					memberBean.putCalcVal(MemberBean.PensionVsSalary+calcName, Utility.to2DpPercentage(pensionVsSalary));
				} else {
					memberBean.putCalcVal(MemberBean.PensionVsSalary+calcName, Utility.EMPTY_STRING);
				}
				// calculation ends here
				// calculating the PensionVsLTA and set to MemberBean object
				if (p2date > Utility.DEFAULT_DOUBLEZEROVALUE	&& lta > Utility.DEFAULT_DOUBLEZEROVALUE) {
					pensionVsLta = 100 * (p2date * Utility.YEARS_OF_SERVICE / lta);
				}
				if (pensionVsLta > Utility.DEFAULT_DOUBLEZEROVALUE) {
					memberBean.putCalcVal(MemberBean.PensionVsLta+calcName, Utility.to2DpPercentage(pensionVsLta));
				} else {
					memberBean.putCalcVal(MemberBean.PensionVsLta+calcName, Utility.EMPTY_STRING);
				}
				// calculation ends here
				
				
				// calculating the AvcVsLTA and set to MemberBean object
				if (lta > Utility.DEFAULT_DOUBLEZEROVALUE) { // totalAvc may be 0
					totalAvcVsLta = 100 * (totalAvc / lta);
				}
					// the value of AvcVsLta may be 0
					memberBean.putCalcVal(MemberBean.AvcVsLta+calcName, Utility.to2DpPercentage(totalAvcVsLta));
				// calculation ends here
								
				// calculating the TotalVsLTA and set to MemberBean object
					// totalPercentage = pensionVsLta + totalAvcVsLta 
					totalVsLta	= pensionVsLta + totalAvcVsLta;
					memberBean.putCalcVal(MemberBean.TotalVsLta+calcName, Utility.to2DpPercentage(totalVsLta));
				// calculation ends here
					
			} catch (SQLException e) {
				System.out.println("Calculation error: "+e.getMessage());
			    throw e;
			} 
			
		} else if ("96".equals(calcType)) { 
			String sqlSelect = " SELECT CALC_OUTPUT.COUN1L ACCRUEDPENSION, CALC_OUTPUT.COUN0N EGPCASH," +
			"CALC_OUTPUT.COUN0M SRPCASH,CALC_OUTPUT.COUN0S TAXFREECASH,CALC_OUTPUT.COUN0O TAXABLECASH," +
			"CALC_OUTPUT.COUN0R TAXPAYABLE FROM CALC_OUTPUT" +
			" WHERE CALC_OUTPUT.BGROUP=? AND CALC_OUTPUT.REFNO=? AND CALC_OUTPUT.CALCTYPE=?";
			
			ResultSet rs = null;
			PreparedStatement pstm = null;
			calcName = "_"+calcNames[2]+calcType+"@CALC";
			try {
				pstm = conn.prepareStatement(sqlSelect);
				pstm.setString(1, bGroup);
				pstm.setString(2, refNo);
				pstm.setString(3, calcType);
				rs = pstm.executeQuery();
				
				//Initialise
				/*
				memberTemp.set(MemberDao.AccruedPension, StringUtil.EMPTY_STRING);
				memberTemp.set(MemberDao.EgpCash, StringUtil.EMPTY_STRING);
				memberTemp.set(MemberDao.SrpCash, StringUtil.EMPTY_STRING);
				memberTemp.set(MemberDao.TaxFreeCash, StringUtil.EMPTY_STRING);
				memberTemp.set(MemberDao.TaxableCash, StringUtil.EMPTY_STRING);
				memberTemp.set(MemberDao.TaxPayable, StringUtil.EMPTY_STRING);
				memberTemp.set(MemberDao.RrReducedPension, StringUtil.EMPTY_STRING);
				memberTemp.set(MemberDao.RrSpousesPension, StringUtil.EMPTY_STRING);
				memberTemp.set(MemberDao.RrMaxLumpSum, StringUtil.EMPTY_STRING);
				memberTemp.set(MemberDao.RrResidualPension, StringUtil.EMPTY_STRING);
				*/
							
				boolean calcResultFound = false;
				String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
				PreparedStatement pstmError = null;
				ResultSet rsError = null;			
				pstmError = conn.prepareStatement(sqlSelectError);
				pstmError.setString(1, bGroup);
				pstmError.setString(2, refNo);
				pstmError.setString(3, calcType);
				rsError = pstmError.executeQuery();	
				
				while (!calcResultFound)
				{			
					if (rs.next()){// if calculate have record set it in to memberDao attribute
						memberBean.putCalcVal(MemberBean.AccruedPension+calcName, Utility.getString(rs.getString("ACCRUEDPENSION")));
						memberBean.putCalcVal(MemberBean.EgpCash+calcName, Utility.toLowestPound(rs.getDouble("EGPCASH")));
						memberBean.putCalcVal(MemberBean.SrpCash+calcName, Utility.toLowestPound(rs.getDouble("SRPCASH")));
						memberBean.putCalcVal(MemberBean.TaxFreeCash+calcName, Utility.toLowestPound(rs.getDouble("TAXFREECASH")));
						memberBean.putCalcVal(MemberBean.TaxableCash+calcName,Utility.toLowestPound(rs.getDouble("TAXABLECASH")));
						memberBean.putCalcVal(MemberBean.TaxPayable+calcName, Utility.toLowestPound(rs.getDouble("TAXPAYABLE")));
						
						calcResultFound = true;
					}//if no results then do nothing.
					else if (rsError.next()) {
						//TODO:
						calcResultFound = true;
						String message = rsError.getString("message");	
						memberBean.putCalcVal("JDBC Error"+calcName, message);
					}				
					else
					{
						long now = System.currentTimeMillis();
						
						if ((now - calcRunStart) > waitTimeout.longValue())
						{						
							break;
						}
						
						rs = pstm.executeQuery();	
						rsError = pstmError.executeQuery();
					}
				}
				rs.close();
				pstm.close();
				rsError.close();
				pstmError.close();			
				
			} catch (SQLException e) {
				System.out.println("Calculation error: "+e.getMessage());
				throw e;
			}
			
		} else if ("97".equals(calcType)) {
			String sqlSelect = " SELECT CALC_OUTPUT.COUN91 ACCRUEDPENSION, CALC_OUTPUT.COUN0N EGPCASH," +
			"CALC_OUTPUT.COUN0M SRPCASH,CALC_OUTPUT.COUN0S TAXFREECASH,CALC_OUTPUT.COUN0O TAXABLECASH," +
			"CALC_OUTPUT.COUN0R TAXPAYABLE, CALC_OUTPUT.COUN42 RrReducedPension, " +
			"CALC_OUTPUT.COUN43 RRSPOUSESPENSI0N, CALC_OUTPUT_2.CO2N65 RRMAXLUMPSUM, "
			+ " CALC_OUTPUT_3.CO3N05 RRRESIDUALPENSION FROM CALC_OUTPUT,CALC_OUTPUT_2, CALC_OUTPUT_3" +
			" WHERE CALC_OUTPUT.BGROUP=CALC_OUTPUT_2.BGROUP AND CALC_OUTPUT.REFNO=CALC_OUTPUT_2.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_2.CALCTYPE " +
			"AND CALC_OUTPUT.BGROUP=CALC_OUTPUT_3.BGROUP AND CALC_OUTPUT.REFNO=CALC_OUTPUT_3.REFNO AND CALC_OUTPUT.CALCTYPE=CALC_OUTPUT_3.CALCTYPE " +
			"AND CALC_OUTPUT.BGROUP=? AND CALC_OUTPUT.REFNO=? AND CALC_OUTPUT.CALCTYPE=?";
			
			ResultSet rs = null;
			PreparedStatement pstm = null;
			calcName = "_"+calcNames[3]+calcType+"@CALC";
			try {
				pstm = conn.prepareStatement(sqlSelect);
				pstm.setString(1, bGroup);
				pstm.setString(2, refNo);
				pstm.setString(3, calcType);
				rs = pstm.executeQuery();
				
				boolean calcResultFound = false;
				String sqlSelectError = "SELECT message FROM CALC_ERRORS WHERE bgroup=? and refno=? and calctype=? and errtype='F'";
				PreparedStatement pstmError = null;
				ResultSet rsError = null;			
				pstmError = conn.prepareStatement(sqlSelectError);
				pstmError.setString(1, bGroup);
				pstmError.setString(2, refNo);
				pstmError.setString(3, calcType);
				rsError = pstmError.executeQuery();	
				
				while (!calcResultFound) {			
					if (rs.next()){// if calculate have record set it in to memberDao attribute
						
						memberBean.putCalcVal(MemberBean.AccruedPension+calcName, Utility.getString(rs.getString("ACCRUEDPENSION")));
						memberBean.putCalcVal(MemberBean.EgpCash+calcName, Utility.toLowestPound(rs.getDouble("EGPCASH")));
						memberBean.putCalcVal(MemberBean.SrpCash+calcName, Utility.toLowestPound(rs.getDouble("SRPCASH")));
						memberBean.putCalcVal(MemberBean.TaxFreeCash+calcName, Utility.toLowestPound(rs.getDouble("TAXFREECASH")));
						memberBean.putCalcVal(MemberBean.TaxableCash+calcName,Utility.toLowestPound(rs.getDouble("TAXABLECASH")));
						memberBean.putCalcVal(MemberBean.TaxPayable+calcName, Utility.toLowestPound(rs.getDouble("TAXPAYABLE")));
						memberBean.putCalcVal(MemberBean.RrReducedPension+calcName, Utility.toLowestPound(rs.getDouble("RrReducedPension")));
						memberBean.putCalcVal(MemberBean.RrSpousesPension+calcName, Utility.toLowestPound(rs.getDouble("RRSPOUSESPENSI0N")));
						memberBean.putCalcVal(MemberBean.RrMaxLumpSum+calcName, Utility.toLowestPound(rs.getDouble("RRMAXLUMPSUM")));
						memberBean.putCalcVal(MemberBean.RrResidualPension+calcName, Utility.toLowestPound(rs.getDouble("RRRESIDUALPENSION")));
						
						calcResultFound = true;
						
					} else if (rsError.next()) {				
						calcResultFound = true;
						String message = rsError.getString("message");
						memberBean.putCalcVal("JDBC Error"+calcName, message);				
					
					} else {// if calculate have no record putCalcVal memberDao attribute with StringUtil.EMPTY_STRING					
						long now = System.currentTimeMillis();
						
						if ((now - calcRunStart) > waitTimeout.longValue()) {
							memberBean.putCalcVal(MemberBean.AccruedPension+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.EgpCash+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.SrpCash+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.TaxFreeCash+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.TaxableCash+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.TaxPayable+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.RrReducedPension+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.RrSpousesPension+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.RrMaxLumpSum+calcName, Utility.EMPTY_STRING);
							memberBean.putCalcVal(MemberBean.RrResidualPension+calcName, Utility.EMPTY_STRING);
							break;
						}
							
						
						rs = pstm.executeQuery();
						rsError = pstmError.executeQuery();
					}
				}
				
				rs.close();
				pstm.close();
				rsError.close();
				pstmError.close();
				
			} catch (SQLException e) {
				System.out.println("Calculation error: "+e.getMessage());
				throw e;
			}
		}
	}
}

