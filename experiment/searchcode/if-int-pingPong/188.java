package com.pingpong.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.pingpong.jdbc.JdbcUtils;
import com.pingpong.model.BallFriend;

public class ShowFriendDao {

	private static final String SQL_QUERY = "select * from ballfriend where friendId = ? or friendPhone = ?";
	private static final String SQL_QUERY_ALL = "select * from ballfriend where friendId in "
			+ "(select followerId from friendfollow where friendId=?) or friendId in "
			+ "(select friendId from friendfollow where followerId=?)";

	private static final String SQL_QUERY_BATCH = "select * from ballfriend where friendId in ";

	private static final String SQL_UPDATE_FANS = "update ballfriend set friendFans = friendFans + 1 where friendId = ?";
	private static final String SQL_UPDATE_PLAY = "update ballfriend set friendPlay = friendPlay + 1 where friendId = ?";
	private static final String SQL_UPDATE_HALL = "update ballfriend set friendHall = friendHall + 1 where friendId = ?";
	private static final String SQL_UPDATE_LIKE = "update ballfriend set friendLike = friendLike + 1 where friendId = ?";

	private JdbcUtils jdbcUtils;
	private QueryRunner queryRunner;

	public ShowFriendDao() {
		jdbcUtils = new JdbcUtils();
		queryRunner = new QueryRunner();
	}

	public BallFriend query(List<Object> params) {
		List<BallFriend> friends = doQuery(SQL_QUERY, params);
		if (friends != null && friends.size() > 0) {
			return friends.get(0);
		}
		return null;
	}

	/**
	 * 查询好友
	 * @param params
	 * @return
	 */
	public List<BallFriend> queryAll(List<Object> params) {
		return doQuery(SQL_QUERY_ALL, params);
	}

	private List<BallFriend> doQuery(String sql, List<Object> params) {
		List<BallFriend> ballFriends = null;
		Connection conn = jdbcUtils.openConnection();
		try {
			ballFriends = queryRunner.query(conn, sql,
					new BeanListHandler<BallFriend>(BallFriend.class),
					params.toArray());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			jdbcUtils.closeConnection();
		}
		return ballFriends;
	}

	/**
	 * 根据系列ID查询Friend
	 * 
	 * @param params
	 * @return
	 */
	public List<BallFriend> queryBatch(List<Object> params) {
		if(params == null || params.size()==0){
			return null;
		}
		Connection conn = jdbcUtils.openConnection();
		try {
			StringBuilder sb = new StringBuilder(SQL_QUERY_BATCH);
			sb.append("(");
			for (Object obj : params) {
				sb.append(obj);
				sb.append(",");
			}
			sb.replace(sb.length() - 1, sb.length(), ")");
			return queryRunner.query(conn, sb.toString(),
					new BeanListHandler<BallFriend>(BallFriend.class));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			jdbcUtils.closeConnection();
		}
		return null;
	}

	public int updateFans(List<Object> params) {
		return doUpdate(SQL_UPDATE_FANS, params);
	}

	public int updatePlay(List<Object> params) {
		return doUpdate(SQL_UPDATE_PLAY, params);
	}

	public int updateHall(List<Object> params) {
		return doUpdate(SQL_UPDATE_HALL, params);
	}

	public int updateLike(List<Object> params) {
		return doUpdate(SQL_UPDATE_LIKE, params);
	}

	private int doUpdate(String sql, List<Object> params) {
		Connection conn = jdbcUtils.openConnection();
		try {
			return queryRunner.update(conn, sql, params.toArray());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			jdbcUtils.closeConnection();
		}
		return 0;
	}
}

