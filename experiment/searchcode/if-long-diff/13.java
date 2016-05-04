/* $Id: MonitorModel.java 124 2010-09-23 16:25:36Z altdotua@gmail.com $ */

package db;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;

import djudge.acmcontester.server.ContestServer;
import djudge.acmcontester.structures.MonitorData;
import djudge.acmcontester.structures.MonitorUserStatus;
import djudge.acmcontester.structures.ProblemData;
import djudge.acmcontester.structures.ProblemStatus;
import djudge.acmcontester.structures.UserData;
import djudge.acmcontester.structures.UserProblemStatusACM;
import djudge.acmcontester.structures.UserProblemStatusIOI;

public class MonitorModel
{
	public static void sortMonitor(MonitorData monitor, Comparator<MonitorUserStatus> viewComparator, Comparator<MonitorUserStatus> placesComparator)
	{
		Arrays.sort(monitor.teams, viewComparator);
		int place = 0;
		for (int i = 0; i < monitor.teams.length; i++)
		{
			if (i == 0 || placesComparator.compare(monitor.teams[i], monitor.teams[i-1]) != 0)
				place++;
			monitor.teams[i].place = place;
		}
	}
	
	public MonitorData getMonitorACM(long contestTime)
	{
		UsersDataModel udm = new UsersDataModel();
		udm.setWhere(" `role` = 'TEAM'");
		udm.updateData();
		MonitorData monitor = getMonitor(contestTime, udm.getRows().toArray(new UserData[0]));
		sortMonitor(monitor, new ACMViewComparator(), new ACMPlacesComparator());
		return monitor;
	}
	
	public MonitorData getMonitorIOI(long contestTime)
	{
		UsersDataModel udm = new UsersDataModel();
		udm.setWhere(" `role` = 'TEAM'");
		udm.updateData();
		MonitorData monitor = getMonitor(contestTime, udm.getRows().toArray(new UserData[0]));
		sortMonitor(monitor, new IOIViewComparator(), new IOIPlacesComparator());
		return monitor;
	}
	
	private UserProblemStatusACM getUserProblemStatisticsACM(long contestTime, String userID, String problemID)
	{
		UserProblemStatusACM res = new UserProblemStatusACM();
		String sqlFirstAc = "SELECT * FROM `submissions` WHERE active > 0 AND `contest_time` <= '"
				+ contestTime
				+ "' AND `user_id` = '"
				+ userID
				+ "' AND `problem_id` = '"
				+ problemID
				+ "' AND djudge_flag > 0 AND judgement = 'AC' ORDER BY id asc LIMIT 1";
		Connection conn = Settings.getConnection();
		Statement st;
		synchronized (AbstractTableDataModel.dbMutex)
		{
			try
			{
				String firstAcId = null;
				int acTime = -1;
				st = conn.createStatement();
				ResultSet rs = st.executeQuery(sqlFirstAc);
				if (rs.next())
				{
					firstAcId = rs.getString("id");
					acTime = rs.getInt("contest_time") / 60 / 1000;
				}
				else
				{
					firstAcId = "10000000";
				}
				rs.close();
				st.close();
				String allBeforeAc = "SELECT * FROM `submissions` WHERE active > 0 AND `id` < '"
						+ firstAcId
						+ "' AND `user_id` = '"
						+ userID
						+ "' AND `problem_id` = '"
						+ problemID
						+ "' AND djudge_flag > 0 AND id < '"
						+ firstAcId
						+ "' AND contest_time <= "
						+ contestTime
						+ " AND judgement != 'CE' ORDER BY id ASC";
				st = conn.createStatement();
				rs = st.executeQuery(allBeforeAc);
				int waCnt = 0;
				int lastWaTime = -1;
				while (rs.next())
				{
					waCnt++;
					lastWaTime = rs.getInt("contest_time") / 60 / 1000;
				}
				rs.close();
				st.close();
				if (acTime >= 0)
				{
					res.wasSolved = true;
					res.penaltyTime = acTime + waCnt * 20; 
					res.lastSubmitTime = acTime;
					res.isPending = false;
				}
				else
				{
					res.wasSolved = false;
					res.lastSubmitTime = lastWaTime;
					res.penaltyTime = 0;
					String pending = "SELECT id FROM `submissions` WHERE active > 0 AND `user_id` = '"
							+ userID
							+ "' AND `problem_id` = '"
							+ problemID
							+ "' AND djudge_flag <= 0 AND contest_time <= "
							+ contestTime + " LIMIT 1";
					st = conn.createStatement();
					rs = st.executeQuery(pending);
					if (rs.next())
					{
						res.isPending = true;
					}
					else
					{
						res.isPending = false;
					}
					rs.close();
					st.close();
				}
				res.wrongTryes = waCnt;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}
	
	private boolean getUserProblemStatusPendingIOI(long contestTime, String userID, String problemID)
	{
		Connection conn = Settings.getConnection();
		Statement st;
		ResultSet rs;
		boolean res = false;
		synchronized (AbstractTableDataModel.dbMutex)
		{
			try
			{
				String sqtPending = "SELECT id FROM `submissions` WHERE active > 0 AND `user_id` = '"
						+ userID
						+ "' AND `problem_id` = '"
						+ problemID
						+ "' AND djudge_flag <= 0 AND contest_time <= "
						+ contestTime + " LIMIT 1";
				st = conn.createStatement();
				rs = st.executeQuery(sqtPending);
				if (rs.next())
				{
					res = true;
				}
				rs.close();
				st.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}
	
	private UserProblemStatusIOI getUserProblemStatisticsIOI(long contestTime, String userID, String problemID)
	{
		UserProblemStatusIOI res = new UserProblemStatusIOI();
		Statement st;
		Connection conn = Settings.getConnection();
		ResultSet rs;
		synchronized (AbstractTableDataModel.dbMutex)
		{
			try
			{
				// searching for max score
				String sqlMaxScore = "SELECT * FROM `submissions` WHERE active > 0 AND `contest_time` <= '" + contestTime + "' AND `user_id` = '" + userID + "' AND `problem_id` = '" + problemID + "' AND djudge_flag > 0 ORDER BY score DESC, contest_time ASC LIMIT 1";
				st = conn.createStatement();
				rs = st.executeQuery(sqlMaxScore);
				// some score present
				if (rs.next())
				{
					int maxScore = rs.getInt("score");
					long maxScoreTime = rs.getLong("contest_time");
					String judgement = rs.getString("judgement");
					res.maxScoreFirstTime = maxScoreTime / 60 / 1000;
					res.score = maxScore;
					if ("AC".equalsIgnoreCase(judgement))
					{
						res.isFullScore = true;
					}
					rs.close();
					st.close();
					String sqlTotalCount = "SELECT COUNT(*), MAX(contest_time) FROM `submissions` WHERE active > 0 AND `contest_time` <= '" + contestTime + "' AND `user_id` = '" + userID + "' AND `problem_id` = '" + problemID + "' AND djudge_flag > 0";
					st = conn.createStatement();
					rs = st.executeQuery(sqlTotalCount);
					if (rs.next())
					{
						res.lastSubmitTime = rs.getLong("MAX(contest_time)") / 60 / 1000;
						res.submissionsTotal = rs.getInt("COUNT(*)");
					}
					else
						throw new Exception("Shit happens");
					rs.close();
					st.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return res;
	}
	
	private MonitorData getMonitor(long contestTime, UserData[] users)
	{
		MonitorData md = new MonitorData();
		md.contestTime = contestTime;
		md.teams = new MonitorUserStatus[users.length];
		ProblemData[] pr = ContestServer.getCore().getProblemsModel().getRows().toArray(new ProblemData[0]);
		md.problems = new ProblemStatus[pr.length];
		for (int i = 0; i < pr.length; i++)
		{
			md.problems[i] = new ProblemStatus(pr[i].sid);
		}
		for (int i = 0; i < users.length; i++)
		{
			MonitorUserStatus mr = new MonitorUserStatus();
			mr.userID = users[i].id;
			mr.username = users[i].username;
			mr.acmData = new UserProblemStatusACM[pr.length];
			mr.ioiData = new UserProblemStatusIOI[pr.length];
			for (int ipr = 0; ipr < pr.length; ipr++)
			{
				UserProblemStatusACM acmStat = getUserProblemStatisticsACM(contestTime, users[i].id, pr[ipr].id);
				UserProblemStatusIOI ioiStat = getUserProblemStatisticsIOI(contestTime, users[i].id, pr[ipr].id);
				ioiStat.isPending = getUserProblemStatusPendingIOI(contestTime, users[i].id, pr[ipr].id);
				mr.acmData[ipr] = acmStat;
				mr.ioiData[ipr] = ioiStat;
				if (acmStat.wasSolved)
				{
					mr.totalAttempts++;
					mr.totalSolved++;
					mr.totalTime += acmStat.penaltyTime;
					mr.totalScoredAttempts += acmStat.wrongTryes + 1;
				}
				mr.totalScore += ioiStat.score;
				mr.totalAttempts += acmStat.wrongTryes;
				md.problems[ipr].addUser(acmStat);
			}
			md.teams[i] = mr;
		}
		for (int i = 0; i < pr.length; i++)
		{
			md.totalAC += md.problems[i].totalACCount;
			md.totalSubmitted += md.problems[i].totalSubmissionsCount;
		}
		return md;
	}
	
	public class IOIPlacesComparator implements Comparator<MonitorUserStatus>
	{
		@Override
		public int compare(MonitorUserStatus a, MonitorUserStatus t)
		{
			return t.totalScore - a.totalScore;
		}		
	}
	
	public class IOIViewComparator implements Comparator<MonitorUserStatus>
	{
		@Override
		public int compare(MonitorUserStatus a, MonitorUserStatus t)
		{
			if (a.totalScore != t.totalScore)
				return t.totalScore - a.totalScore;
			if (a.totalSolved != t.totalSolved)
				return t.totalSolved - a.totalSolved;
			return t.totalAttempts - a.totalAttempts; 
		}		
	}
	
	public class ACMPlacesComparator implements Comparator<MonitorUserStatus>
	{
		@Override
		public int compare(MonitorUserStatus a, MonitorUserStatus t)
		{
			if (t.totalSolved != a.totalSolved)
				return t.totalSolved - a.totalSolved;
			long diff = a.totalTime - t.totalTime;
			return diff > 0 ? 1 : diff < 0 ? -1 : 0; 
		}		
	}
	
	public class ACMViewComparator implements Comparator<MonitorUserStatus>
	{
		@Override
		public int compare(MonitorUserStatus a, MonitorUserStatus t)
		{
			if (t.totalSolved != a.totalSolved)
				return t.totalSolved - a.totalSolved;
			if (a.totalTime != t.totalTime)
			{
				long diff = a.totalTime - t.totalTime;
				return diff > 0 ? 1 : -1; 
			}
			long diff = t.totalAttempts - a.totalAttempts;
			return diff > 0 ? 1 : diff < 0 ? -1 : 0;
		}		
	}
}

