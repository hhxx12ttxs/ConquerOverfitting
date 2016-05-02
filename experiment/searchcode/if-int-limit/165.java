package com.twitstreet.session;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.twitstreet.config.ConfigMgr;
import com.twitstreet.db.base.DBConstants;
import com.twitstreet.db.base.DBMgr;
import com.twitstreet.db.base.DBMgrImpl;
import com.twitstreet.db.data.Group;
import com.twitstreet.db.data.RankingHistoryData;
import com.twitstreet.db.data.User;
import com.twitstreet.task.StockUpdateTask;
import com.twitstreet.util.Util;

public class UserMgrImpl implements UserMgr {

	@Inject
	DBMgr dbMgr;
	@Inject ConfigMgr configMgr;
	@Inject GroupMgr groupMgr;
	
	private static int MAX_RECORD_PER_PAGE = 20;
	private static Logger logger = Logger.getLogger(UserMgrImpl.class);

	
	private static String SEASON_START = "2012-03-01 00:35:00";
	
	private static String SELECT_FROM_USERS_RANKING = "select " + 
			"id, " + 
			"userName, " + 
			"lastLogin, " + 
			"firstLogin, " + 
			"users.cash as cash, " + 
			"lastIp, " + 
			"oauthToken, " +
			"oauthTokenSecret, " + 
			"user_profit(users.id) as changePerHour," +
			"rank, " +
			"oldRank, " + 
			"direction, " + 
			"pictureUrl, " + 
			"portfolio_value(id) as portfolio " + 
			"from users,ranking ";
	
	private static String SELECT_FROM_USERS_JOIN_RANKING = SELECT_FROM_USERS_RANKING + " where ranking.user_id = users.id ";
	
	
	public User getUserById(long id) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User userDO = null;
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement(SELECT_FROM_USERS_JOIN_RANKING+ " and users.id = ?");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				userDO = new User();
				userDO.getDataFromResultSet(rs);
			}
			
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
		return userDO;
	}
	public ArrayList<User> getUsersByGroup(Group group) {
		ArrayList<User> users = new ArrayList<User>();
		if(group.getId()<1 && group.getName() != null && group.getName().length()>0){
			group = groupMgr.getGroup(group.getName());		
		}
		
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User userDO = null;
		try {
			connection = dbMgr.getConnection();
			ps = connection.prepareStatement(SELECT_FROM_USERS_RANKING 
							+" , user_group where ranking.user_id = users.id and user_group.user_id = users.id and user_group.group_id = ? ");
			ps.setLong(1, group.getId());
			rs = ps.executeQuery();
			while (rs.next()) {
				userDO = new User();
				userDO.getDataFromResultSet(rs);
				users.add(userDO);
			}
			
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
		return users;
	}
	
	
	public void assignInitialRankToUser(User userDO){

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = dbMgr.getConnection();
			
			ps = connection.prepareStatement(" select (count(*)+1) as newrank from ranking,users " +
					" where ranking.user_id = users.id and " +
					" ( " +
					" (portfolio + ranking.cash) > ? or " +
					" (ranking.portfolio + ranking.cash = ? and username <?) " +
					" ) ");
			
			ps.setDouble(1, userDO.getCash());
			ps.setDouble(2, userDO.getCash());
			ps.setString(3, userDO.getUserName());
			rs = ps.executeQuery();
			int newRank = 999999;
			if(rs.next()){
			  newRank = rs.getInt("newrank");
			}
			
			rs.close();
			ps.close();
			
			ps = connection.prepareStatement("insert into ranking(user_id, cash,portfolio,rank,oldRank,direction,lastUpdate)" + " values(?,?,?,?,?,?,NOW())");
			ps.setLong(1, userDO.getId());
			ps.setDouble(2, userDO.getCash());
			ps.setDouble(3, 0);
			ps.setInt(4, newRank);
			ps.setInt(5, newRank);
			ps.setInt(6, 0);

			ps.executeUpdate();
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (MySQLIntegrityConstraintViolationException e) {
			logger.warn("DB: User already exists in ranking - UserId:" + userDO.getId()
					+ " User Name: " + userDO.getUserName() + " - "
					+ e.getMessage());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
	
		
	}

	public void saveUser(User userDO) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = dbMgr.getConnection();
			
		
			
			ps = connection
					.prepareStatement("insert into users(id, userName, "
							+ "lastLogin, firstLogin, "
							+ "cash, lastIp, oauthToken, oauthTokenSecret, pictureUrl) "
							+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setLong(1, userDO.getId());
			ps.setString(2, userDO.getUserName());
			ps.setDate(3, Util.toSqlDate(userDO.getLastLogin()));
			ps.setDate(4, Util.toSqlDate(userDO.getFirstLogin()));
			ps.setDouble(5, userDO.getCash());
			ps.setString(6, userDO.getLastIp());
			ps.setString(7, userDO.getOauthToken());
			ps.setString(8, userDO.getOauthTokenSecret());
			ps.setString(9, userDO.getPictureUrl());

			ps.executeUpdate();
			
			
			
			
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (MySQLIntegrityConstraintViolationException e) {
			logger.warn("DB: User already exist - UserId:" + userDO.getId()
					+ " User Name: " + userDO.getUserName() + " - "
					+ e.getMessage());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, null);
		}
		
		assignInitialRankToUser(userDO);
		groupMgr.addUserToDefaultGroup(userDO);
	}


	@Override
	public void updateUser(User user) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement("update users set userName = ?, "
							+ "lastLogin = ?, "
							+ "lastIp = ?, oauthToken = ?, oauthTokenSecret = ?, pictureUrl = ? where id = ?");
			ps.setString(1, user.getUserName());
			ps.setDate(2, Util.toSqlDate(user.getLastLogin()));
			ps.setString(3, user.getLastIp());
			ps.setString(4, user.getOauthToken());
			ps.setString(5, user.getOauthTokenSecret());
			ps.setLong(6, user.getId());
			ps.setString(7, user.getPictureUrl());

			ps.executeUpdate();
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, null);
		}
	}

	@Override
	public User random() {
		Connection connection = null;
		Statement stmt = null;
		User user = null;
		ResultSet rs = null;
		try {
			connection = dbMgr.getConnection();
			stmt = connection.createStatement();
			rs = stmt
					.executeQuery(SELECT_FROM_USERS_JOIN_RANKING+" and users.id >= (select floor( max(id) * rand()) from users ) " + 
							"order by users.id limit 1");
			if (rs.next()) {
				user = new User();
				user.getDataFromResultSet(rs);

			} else {
				logger.error("DB: Random user selection query is not working properly");
			}
		} catch (SQLException e) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + stmt.toString(), e);
		} finally {
			dbMgr.closeResources(connection, stmt, rs);

		}
		return user;
	}

	@Override
	public void increaseCash(long userId, double cash) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement("update users set cash = (cash + ?) where id = ?");
			ps.setDouble(1, cash);
			ps.setLong(2, userId);

			ps.executeUpdate();
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, null);
		}
	}

	@Override
	public void updateCash(long userId, double amount) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
		connection = dbMgr.getConnection();
		ps = connection
				.prepareStatement("update users set cash = (cash - ?) where id = ?");
		ps.setDouble(1, amount);
		ps.setLong(2, userId);
		
			ps.executeUpdate();
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, null);
		}
	}

	@Override
	public ArrayList<User> getTopRank(int pageNumber) {
		
		// i.e limit : 17, 17
		int maxRank = getRecordPerPage();
		String limit = ((pageNumber - 1) *  maxRank) + ", " + maxRank;
		
		ArrayList<User> userList = new ArrayList<User>(100);
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User userDO = null;
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement(SELECT_FROM_USERS_JOIN_RANKING
							+" order by rank asc limit " + limit);
			rs = ps.executeQuery();
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
			while (rs.next()) {
				userDO = new User();
				userDO.getDataFromResultSet(rs);
				userList.add(userDO);
			}
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
		return userList;
	}
	
	@Override
	public void rerank() {
		Connection connection = null;
		CallableStatement cs = null;
		try {
			connection = dbMgr.getConnection();
			cs = connection.prepareCall("{call rerank()}");
			cs.execute();
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + cs.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + cs.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, cs, null);
		}
	}
	
	@Override
	public void updateRankingHistory(){
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement("insert ignore into ranking_history(user_id, cash, portfolio, lastUpdate, rank) " +
											" select user_id, cash, portfolio,  lastUpdate, rank from ranking where " +
											"ranking.user_id in" +
											 "(" +
											    "select distinct user_id from ranking r where " +
											       " 15< TIMESTAMPDIFF(minute,( " +
											           " select distinct rh.lastUpdate from ranking_history rh where rh.user_id=r.user_id order by rh.lastUpdate desc limit 1" +
											         "   ), now())" +
											" )");
											
	
		   int rowChanged=	ps.executeUpdate();
				
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		}
		catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, null);
		}
		
		
	}

	
	@Override
	public RankingHistoryData getRankingHistoryForUser(long id, String since) {
	
		RankingHistoryData rhd = new RankingHistoryData();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String sinceStr =" TIMESTAMP('"+SEASON_START+"') " ;
		if(since!=null){
			
			sinceStr =" TIMESTAMP('" + since+"') " ;
			
		}
		
	
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement(" select " +
							" rh.user_id as user_id, " +
							" rh.cash as cash, " +
							" rh.portfolio as portfolio, " +
							" rh.rank as rank, " +
							" rh.lastUpdate as lastUpdate " +
							" from ranking_history rh " + 
							"  where user_id = ? " +
							" and rh.lastUpdate > " +sinceStr +
							" order by lastUpdate desc ");
			ps.setLong(1, id);
			rs = ps.executeQuery();
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		
			rhd.getDataFromResultSet(rs);
			
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
		return rhd;
	}
	
	@Override
	public int count() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		int count = 0;
		try {
			connection = dbMgr.getConnection();
			ps = connection.prepareStatement("SELECT count(*) FROM users");
			rs = ps.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException exception) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), exception);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
		return count;
	}
	@Override
	public int getRecordPerPage() {
		return MAX_RECORD_PER_PAGE;
	}
	@Override
	public ArrayList<User> getUsersByIdList(ArrayList<Long> idList) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User userDO = null;
		ArrayList<User> userList = new ArrayList<User>();
		
		String listStr = DBMgrImpl.getIdListAsCommaSeparatedString(idList);
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement(SELECT_FROM_USERS_JOIN_RANKING+" and users.id in ("+listStr+")");
			

			rs = ps.executeQuery();
			if (rs.next()) {
				userDO = new User();
				userDO.getDataFromResultSet(rs);
				userList.add(userDO);
			}
			
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
		return userList;
	}
	
	@Override
	public ArrayList<User> searchUser(String searchText) {

		searchText = searchText.replace(" ", "");

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User userDO = null;
		ArrayList<User> userList = new ArrayList<User>();
		if (searchText.length() > 0) {
			try {
				connection = dbMgr.getConnection();
				ps = connection.prepareStatement(SELECT_FROM_USERS_JOIN_RANKING+ " and userName LIKE ? ");

				ps.setString(1, "%" + searchText + "%");

				rs = ps.executeQuery();
				while (rs.next()) {
					userDO = new User();
					userDO.getDataFromResultSet(rs);
					userList.add(userDO);
				}

				logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
			} catch (SQLException ex) {
				logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
			} finally {
				dbMgr.closeResources(connection, ps, rs);
			}

		}
		return userList;
	}
	@Override
	public int getPageOfRank(int rank) {
		
		int rpp = getRecordPerPage();
		
		int a = (rank+rpp)/rpp;
		
		return a;
	}
	@Override
	public List<User> getAll() {
		List<User> userList = new ArrayList<User>();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User userDO = null;
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement(SELECT_FROM_USERS_JOIN_RANKING);
			rs = ps.executeQuery();
			while (rs.next()) {
				userDO = new User();
				userDO.getDataFromResultSet(rs);
				userList.add(userDO);
			}
			
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
		return userList;
	}
	@Override
	public void updateTwitterData(User user) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement("update users set userName = ?, pictureUrl = ? where id = ?");
			ps.setString(1, user.getUserName());
			ps.setString(2, user.getPictureUrl());
			ps.setLong(3, user.getId());

			ps.executeUpdate();
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, null);
		}
	}
	@Override
	public List<User> getUserListByServerId(int serverId) {
		List<User> userList = new ArrayList<User>();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User userDO = null;
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement(SELECT_FROM_USERS_JOIN_RANKING + " and mod(id, ?) = ?");
			ps.setInt(1, configMgr.getServerCount());
			ps.setInt(2, configMgr.getServerId());
			rs = ps.executeQuery();
			while (rs.next()) {
				userDO = new User();
				userDO.getDataFromResultSet(rs);
				userList.add(userDO);
			}
			
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
		return userList;
	}
	@Override
	public ArrayList<User> getTopGrossingUsers(int limit){
		
		ArrayList<User> userList = new ArrayList<User>();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User userDO = null;
		try {
			connection = dbMgr.getConnection();
			ps = connection
					.prepareStatement(SELECT_FROM_USERS_JOIN_RANKING + " order by ranking.profit/(ranking.cash+ranking.portfolio) desc limit ? ");
			ps.setInt(1, limit);
		
			rs = ps.executeQuery();
			while (rs.next()) {
				userDO = new User();
				userDO.getDataFromResultSet(rs);
				userList.add(userDO);
			}
			
			logger.debug(DBConstants.QUERY_EXECUTION_SUCC + ps.toString());
		} catch (SQLException ex) {
			logger.error(DBConstants.QUERY_EXECUTION_FAIL + ps.toString(), ex);
		} finally {
			dbMgr.closeResources(connection, ps, rs);
		}
		return userList;
		
		
		
		
	}
}

