/**
 * TalkTalkServer: Project that accompanies the TalkTalk mobile
 * application instant messaging service which sends messages
 * over the Internet.
 */
package com.aidansmoker.talktalk.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Aidan Smoker / R00118206 / aidan.smoker@mycit.ie
 * 
 */
public class DatabaseHandler
{
	// Objects for making DB connection and executing statements
	private Connection			connect;
	private Statement			statement;
	private PreparedStatement	preparedStatement;
	private ResultSet			resultSet;

	// Host connection parameters
	private String				hostname;
	private String				port;
	private String				database;
	private String				username;
	private String				password;

	public DatabaseHandler(String hostname, String port, String database, String username, String password)
	{
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
	}

	/**
	 * Add a message to the database
	 * 
	 * @param message
	 *            New message object
	 * @throws SQLException
	 */
	public void addMessage(Message message) throws SQLException
	{
		try
		{
			// Create the database connection
			createConnection();

			// Create a Statement to allow issuing SQL queries to the database
			String statementString = "INSERT INTO messages VALUES (default, ?, ?, ?, ?, ?)";
			preparedStatement = connect.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setInt(1, message.getFromUser());
			preparedStatement.setInt(2, message.getToUser());
			preparedStatement.setString(3, message.getContent());
			preparedStatement.setTimestamp(4, new Timestamp(message.getTimeStamp().getTime()));
			preparedStatement.setBoolean(5, message.isRead());
			int rowCount = preparedStatement.executeUpdate();

			// Check that query occurred
			if (rowCount != 1) throw new SQLException();
			resultSet = preparedStatement.getGeneratedKeys();
			resultSet.next();

			// Update the message's I.D.
			message.setId(resultSet.getInt(1));
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}
	}

	/**
	 * Add a user to database
	 * 
	 * @param user
	 *            New app user
	 * @throws SQLException
	 */
	public void addUser(User user) throws SQLException
	{
		try
		{
			// Create the database connection
			createConnection();

			// Create a Statement to allow issuing SQL queries to the database
			String statementString = "INSERT INTO users VALUES (default, ?, ?, ?, ?, ?, ?, ?, ?)";
			preparedStatement = connect.prepareStatement(statementString, Statement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, user.getFirstName());
			preparedStatement.setString(2, user.getLastName());
			preparedStatement.setString(3, user.getUserName());
			preparedStatement.setString(4, user.getPassWord());
			preparedStatement.setString(5, user.getEmail());
			preparedStatement.setString(6, user.getPhone());
			preparedStatement.setDate(7, new java.sql.Date(user.getDateOfBirth().getTime()));
			preparedStatement.setString(8, user.getGender());
			int rowCount = preparedStatement.executeUpdate();

			// Check that query occurred
			if (rowCount != 1) throw new SQLException();
			resultSet = preparedStatement.getGeneratedKeys();
			resultSet.next();

			// Update the user's I.D.
			user.setId(resultSet.getInt(1));
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}
	}

	/**
	 * Close all resources
	 */
	private void close()
	{
		try
		{
			if (resultSet != null) resultSet.close();
			if (statement != null) statement.close();
			if (preparedStatement != null) preparedStatement.close();
			if (connect != null) connect.close();
		}
		catch (SQLException ignore)
		{
		}
	}

	/**
	 * Create connection
	 * 
	 * @throws SQLException
	 */
	private void createConnection() throws SQLException
	{
		// Load the MySQL driver
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e)
		{
			throw new SQLException(e);
		}

		// Create the connection to the DB
		connect = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username,
				password);
	}

	/**
	 * Load all messages in database into the server message list
	 * 
	 * @param server
	 * @throws SQLException
	 */
	public void loadMessages(Server server) throws SQLException
	{
		try
		{
			// Create the database connection
			createConnection();

			// Create a Statement to allow issuing SQL queries to the database
			statement = connect.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM messages");

			// Get the results of the SQL query
			while (resultSet.next())
			{
				int id = resultSet.getInt("id");
				int fromUser = resultSet.getInt("from_user");
				int toUser = resultSet.getInt("to_user");
				String content = resultSet.getString("content");
				Date timeStamp = parseDate(resultSet.getTimestamp("timestamp").toString(), "yyyy-MM-dd hh:mm:ss");
				boolean read = resultSet.getBoolean("message_read");

				// Create and add new message object to server list
				server.addMessage(new Message(id, fromUser, toUser, content, timeStamp, read));
			}
		}
		catch (SQLException sqle)
		{
			throw sqle;
		}
		finally
		{
			close();
		}
	}

	/**
	 * Load all users from db tables to server list
	 * 
	 * @param server
	 * @throws SQLException
	 */
	public void loadUsers(Server server) throws SQLException
	{
		try
		{
			// Create the database connection
			createConnection();

			// Create a Statement to allow issuing SQL queries to the database
			statement = connect.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM users");

			// Get the results of the SQL query
			while (resultSet.next())
			{
				int id = resultSet.getInt("id");
				String firstName = resultSet.getString("firstName");
				String lastName = resultSet.getString("lastName");
				String userName = resultSet.getString("userName");
				String passWord = resultSet.getString("passWord");
				String email = resultSet.getString("email");
				String phone = resultSet.getString("phone");
				Date dateOfBirth = parseDate(resultSet.getDate("date_of_birth").toString(), "yyyy-MM-dd");
				String gender = resultSet.getString("gender");

				// Create new user object and add to db list
				server.addUser(new User(id, firstName, lastName, userName, passWord, email, phone, dateOfBirth, gender));
			}
		}
		catch (SQLException sqle)
		{
			throw sqle;
		}
		finally
		{
			close();
		}
	}

	/**
	 * Get a date/time from DB string
	 * 
	 * @param date
	 *            Date/time as DB string
	 * @param format
	 *            Format of string
	 * @return Date object
	 * @throws SQLException
	 */
	public Date parseDate(String date, String format) throws SQLException
	{
		Date parsedDate = new Date();
		try
		{
			parsedDate = new SimpleDateFormat(format, Locale.ENGLISH).parse(date);
		}
		catch (ParseException e)
		{
			throw new SQLException();
		}
		return parsedDate;
	}

	/**
	 * Set message as read
	 * 
	 * @param message
	 * @throws SQLException
	 */
	public void updateMessage(Message message) throws SQLException
	{
		try
		{
			// Create the database connection
			createConnection();

			// Create a Statement to allow issuing SQL queries to the database
			String statementString = "UPDATE messages SET message_read=? WHERE id=?";
			preparedStatement = connect.prepareStatement(statementString);
			preparedStatement.setBoolean(1, message.isRead());
			preparedStatement.setInt(2, message.getId());
			int rowCount = preparedStatement.executeUpdate();
			if (rowCount != 1) throw new SQLException(); // Verify query
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			close();
		}
	}
}

