/*
 * Copyright (C) 2008-2011 by Simon Hefti.
 * All rights reserved.
 * 
 * Licensed under the EPL 1.0 (Eclipse Public License).
 * (see http://www.eclipse.org/legal/epl-v10.html)
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 */
package ch.heftix.mailxel.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.sqlite.SQLiteJDBCLoader;

import ch.heftix.mailxel.client.ListUtil;
import ch.heftix.mailxel.client.to.AddressTO;
import ch.heftix.mailxel.client.to.AttachedCategoryTO;
import ch.heftix.mailxel.client.to.AttachmentTO;
import ch.heftix.mailxel.client.to.Category;
import ch.heftix.mailxel.client.to.ConfigTO;
import ch.heftix.mailxel.client.to.Constants;
import ch.heftix.mailxel.client.to.Envelope;
import ch.heftix.mailxel.client.to.IconTO;
import ch.heftix.mailxel.client.to.MailTO;
import ch.heftix.mailxel.client.to.MessageQueryTO;
import ch.heftix.mailxel.client.to.MessageSearchTO;
import ch.heftix.mailxel.client.to.PersonTO;
import ch.heftix.mailxel.client.to.StatisticsTO;
import ch.heftix.xel.db.PrepStmt;
import ch.heftix.xel.db.PrepStmtCache;
import ch.heftix.xel.db.ResultSetHandler;
import ch.heftix.xel.db.TransactionBody;
import ch.heftix.xel.log.LOG;
import ch.heftix.xel.util.Util;

public class MailDB implements MailDBAccessAPI {

	private Connection conn = null;
	private PrepStmtCache prepStmtCache = null;
	private File attachmentRoot = null;
	protected DBUtil dbUtil = null; /* protected, not private: for testing */
	private MailSaveQueue mailStoreQueue = null;
	private MailSaveWorker mailStoreWorker = null;
	public File endOfQueueMarker = null;

	public MailDB(final String path) throws Exception {
		this(path, "mailxel.sql");
	}

	protected MailDB(final String path, final String sqlCreateName)
			throws Exception {

		Class.forName("org.sqlite.JDBC");

		if (!SQLiteJDBCLoader.isNativeMode()) {
			LOG.warn("cannot use sqlite native mode, falling back to (slower) pure-java mode");
		}

		attachmentRoot = new File(path, "attachments");
		attachmentRoot.mkdirs();

		try {
			// create a database connection
			conn = DriverManager.getConnection("jdbc:sqlite:" + path
					+ "/mailxel.db");

			Statement stmt = conn.createStatement();
			stmt.execute("PRAGMA cache_size=100000");
			stmt.close();
			stmt = null;

			prepStmtCache = new PrepStmtCache(conn);
			dbUtil = new DBUtil(prepStmtCache);

			checkCreateSchema(sqlCreateName);

			// use explicit transactions
			conn.setAutoCommit(false);
			LOG.debug("initDB: autocommit set to false");

		} catch (SQLException e) {
			LOG.warn("cannot connect to DB: %s", e.getMessage(), e);
		}

		mailStoreQueue = new MailSaveQueue();
	}

	public void destroy() {
		if (null != conn) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				LOG.warn("MailDB destroy: cannot close connection: "
						+ e.getMessage());
			}
		}
	}

	protected synchronized void enqueueLast() {

		LOG.debug("enqueueLast");
		mailStoreQueue.enqueue(endOfQueueMarker);
	}

	protected synchronized boolean isLast(File file) {
		if (file == endOfQueueMarker) {
			return true;
		}
		return false;
	}

	protected synchronized void enqueue(File file) {

		LOG.debug("enqueue: " + file);
		if (mailStoreQueue.isEmpty()) {
			try {
				endOfQueueMarker = File.createTempFile("mailxel-end-of-queue-",
						".dat");
				endOfQueueMarker.deleteOnExit();
			} catch (IOException e) {
				// ignore
			}
		}
		mailStoreQueue.enqueue(file);

	}

	protected synchronized File dequeue() {

		File res = null;
		res = mailStoreQueue.dequeue();
		LOG.debug("dequeue: " + res);
		return res;

	}

	protected synchronized void startMailSaveWorker() {

		if (null != mailStoreWorker && mailStoreWorker.isAlive()
				&& !mailStoreWorker.isHalted()) {
			LOG.debug("ignoring mail store worker start command");
			return;
		}

		if (null == mailStoreWorker || mailStoreWorker.isHalted()) {
			LOG.debug("starting new mail store worker");
			mailStoreWorker = new MailSaveWorker(this);
			mailStoreWorker.start();
		}
	}

	/**
	 * stores given mail in DB. Assumes that the caller has assured that the
	 * UUID is not yet stored in the DB. Assumes that method is called within a
	 * transaction.
	 * 
	 * @category requires surrounding transaction
	 */
	protected synchronized int storeMail(final Mail mail) {

		LOG.debug("storeMail: %s:%s.", mail.uuid, mail.subject);

		// 2) store all users
		String[] from = mail.getFrom();
		int[] fromIDs = createAddress(from);
		String[] to = mail.getTo();
		int[] toIDs = createAddress(to);
		String[] cc = mail.getCc();
		int[] ccIDs = createAddress(cc);
		String[] bcc = mail.getBcc();
		int[] bccIDs = createAddress(bcc);

		// 3) store mail
		int msgid = dbUtil.poorMansSequence("message");

		String dateStr = Util.format("yyyy-MM-dd HH:mm:ss", mail.date);

		dbUtil.updateAndBringBack(
				"insert into message (id,date,deleted,count,curcatid,fme,tme) values (?,?,0,0,990,0,0)",
				msgid, dateStr);

		// body
		dbUtil.updateAndBringBack(
				"insert into messagetext (docid,subject,body) values (?,?,?)",
				msgid, mail.subject, mail.body);

		// 4) cross-link users and mail
		crosslinkAdressXMessage(conn, msgid, MailAPIUtil.FROM, fromIDs);
		crosslinkAdressXMessage(conn, msgid, MailAPIUtil.TO, toIDs);
		crosslinkAdressXMessage(conn, msgid, MailAPIUtil.CC, ccIDs);
		crosslinkAdressXMessage(conn, msgid, MailAPIUtil.BCC, bccIDs);

		// ... and update last seen dates
		updateLastSeenDate(conn, fromIDs, mail.date);
		updateLastSeenDate(conn, toIDs, mail.date);
		updateLastSeenDate(conn, ccIDs, mail.date);
		updateLastSeenDate(conn, bccIDs, mail.date);

		// 5) cross-link attachment and mail
		AttachmentDescriptor ad = mail.getAttachmentDescriptor();
		if (null != ad) {
			AttachmentTO[] attachmentNames = ad.getAttachmentNames();
			if (null != attachmentNames && attachmentNames.length > 0
					&& null != ad.zipFile) {

				// update attachment bookkeeping
				for (int j = 0; j < attachmentNames.length; j++) {

					int attachmentId = dbUtil.poorMansSequence("attachment");

					dbUtil.updateAndBringBack(
							"insert into attachment (id,messageid,name,md5) values (?,?,?,?)",
							attachmentId, msgid, attachmentNames[j].name,
							attachmentNames[j].md5);
				}

				final int[] counts = new int[1];
				ResultSetHandler rsHandler = new ResultSetHandler() {

					public void handle(ResultSet rs) throws SQLException {
						if (rs.next()) {
							counts[0] = rs.getInt(1);
						}
					}
				};

				// save disk space by removing zip entries with same MD5
				List<String> attachmentAlreadyStored = new ArrayList<String>();
				for (int j = 0; j < attachmentNames.length; j++) {
					counts[0] = 0;

					dbUtil.executeAndBringBack(rsHandler,
							"select count(*) from attachment where md5=?",
							attachmentNames[j].md5);

					if (counts[0] > 1) {
						attachmentAlreadyStored.add(attachmentNames[j].name);
					}
				}

				// move attachments to final zip file
				File dest = createPermanentStoragePath(attachmentRoot,
						mail.date, msgid, mail.subject);
				FileUtil.removeFromZip(ad.zipFile, dest,
						attachmentAlreadyStored);

				// make sure that attachments are not deleted accidentally
				dest.setReadOnly();

				ad.zipFile.delete();

				// write path to DB
				dbUtil.updateAndBringBack(
						"update message set azip=?,nattach=? where id=?",
						dest.getAbsolutePath(), attachmentNames.length, msgid);
			}

			// add attachment name to search fields
			String sql = "update messagetext set a=(select group_concat(name) from attachment where messageid=?) where docid=?";
			dbUtil.updateAndBringBack(sql, msgid, msgid);
		}

		// 6) update address fields
		updateAddressFields(msgid);

		// 7) set "to me" flag
		updateFromMeToMeFlag(MailAPIUtil.FROM, msgid);
		updateFromMeToMeFlag(MailAPIUtil.TO, msgid);

		// 8) set inreplyto, if any
		if (null != mail.inReplyTo) {
			dbUtil.updateAndBringBack(
					"update message set inreplyto=? where id=?",
					mail.inReplyTo, msgid);
		}

		// 9) cross-link messages, if references are present
		String[] refs = mail.getReferences();
		for (int i = 0; i < refs.length; i++) {
			dbUtil.updateAndBringBack(
					"insert into msgxmsg (srcid,refid) values (?,?)",
					mail.uuid, refs[i]);
		}

		// 10) finally: update uuid (signals: storage complete)
		dbUtil.updateAndBringBack("update message set uuid=? where id=?",
				mail.uuid, msgid);

		return msgid;
	}

	protected synchronized boolean uuidExists(final String uuid) {

		final boolean[] res = new boolean[1];
		res[0] = false;

		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					// already stored - skip
					res[0] = true;
				}
			}
		};

		dbUtil.executeAndBringBack(rsHandler,
				"select id from message where uuid=?", uuid);

		return res[0];
	}

	private void crosslinkAdressXMessage(final Connection conn,
			final int msgid, final String type, final int[] ids) {

		for (int i = 0; i < ids.length; i++) {

			dbUtil.updateAndBringBack(
					"insert into addressxmessage (addressid,messageid,type) values (?,?,?)",
					ids[i], msgid, type);
		}
	}

	/** @category requires transaction */
	private void updateLastSeenDate(final Connection conn,
			final int[] addressIds, final Date sentDate) {

		for (int i = 0; i < addressIds.length; i++) {
			String sql1 = String
					.format("select id from address where id=%d union select id from address where shortname is not null and shortname=(select shortname from address where id=%d)",
							addressIds[i], addressIds[i]);
			String sql = String
					.format("update address set lastseen='%s',count=count+1 where id in (%s)",
							Util.format("yyyy-MM-dd HH:mm:ss", sentDate), sql1);
			dbUtil.updateAndBringBack(sql);
		}
	}

	public void saveAddresses(final List<AddressTO> addresses) {

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {

				for (AddressTO addressTO : addresses) {

					// find id (or create a new one)
					AddressTO tmpATO = createAddress(addressTO.address);

					if (null != tmpATO && Constants.UNDEFINED_ID != tmpATO.id) {

						addressTO.id = tmpATO.id;

						// add values (if any)
						updateAddress(addressTO);

						// see if we need to update strings in message table
						if (addressTO.nameDirty || addressTO.shortNameDirty) {

							int[] tmp = dbUtil
									.getInts(
											"select docid from messagetext where r match ?",
											addressTO.address);

							for (int i = 0; i < tmp.length; i++) {
								updateAddressFields(tmp[i]);
							}
						}
					}
				}
			}
		};
		transaction(tb);
	}

	/**
	 * @category requires surrounding transaction
	 * @return newly created or existing AddressTO, or null
	 */
	protected AddressTO createAddress(final String address) {

		AddressTO res = getAddress(address);

		if (Constants.UNDEFINED_ID == res.id) {
			// address does not exist; create
			int nextid = dbUtil.poorMansSequence("address");
			StringBuffer sb = new StringBuffer(1024);
			VarArgs args = new VarArgs();
			sb.append("insert into address (id");
			args.add(nextid);
			sb.append(",address");
			args.add(address);
			sb.append(",isvalid,ispreferred,count) values (");
			args.add(true);
			args.add(true);
			args.add(0);

			Object[] arg2 = args.toArray();
			for (int i = 0; i < arg2.length; i++) {
				if (i != 0) {
					sb.append(",");
				}
				sb.append("?");
			}

			sb.append(")");
			String sql = sb.toString();

			dbUtil.updateAndBringBack(sql, arg2);
			res = getAddress(address);
		}
		return res;
	}

	/**
	 * lightweight version of {@link createAddress}
	 * 
	 * @category requires surrounding transaction
	 * @return ids of newly created or existing address record
	 */
	private int[] createAddress(final String[] addresses) {

		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < addresses.length; i++) {

			int nid = createAddressGetId(addresses[i]);
			ids.add(new Integer(nid));
		}
		return ListUtil.asIntArray(ids);
	}

	/**
	 * lightweight version of {@link createAddress}
	 * 
	 * @category requires surrounding transaction
	 * @return id of newly created or existing address record
	 */
	private int createAddressGetId(final String address) {

		String sql = "select id from address where address=?";
		final int[] res = { Constants.UNDEFINED_ID };
		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					res[0] = rs.getInt(1);
				}
			}
		};
		dbUtil.executeAndBringBack(rsHandler, sql, address);
		if (Constants.UNDEFINED_ID == res[0]) {
			// not known, create new
			int nextid = dbUtil.poorMansSequence("address");
			StringBuffer sb = new StringBuffer(256);
			sb.append("insert into address (id,address,isvalid,ispreferred,count)");
			sb.append(" values(?,?,?,?,?)");
			VarArgs args = new VarArgs();
			args.add(nextid);
			args.add(address);
			args.add(true);
			args.add(true);
			args.add(0);
			Object[] arg2 = args.toArray();
			sql = sb.toString();
			dbUtil.updateAndBringBack(sql, arg2);
			res[0] = nextid;
		}
		return res[0];
	}

	/**
	 * @category requires surrounding transaction
	 */
	protected void updateAddress(final AddressTO aTO) {

		StringBuffer sb = new StringBuffer(1024);
		sb.append("update address");

		VarArgs args = new VarArgs();

		if (aTO.shortNameDirty) {
			sb.append(" set shortname=?");
			args.add(aTO.shortname);
		}
		if (aTO.nameDirty) {
			if (args.size() > 0) {
				sb.append(",");
			} else {
				sb.append(" set");
			}
			sb.append(" name=?");
			args.add(aTO.name);
		}
		if (aTO.validDirty) {
			if (args.size() > 0) {
				sb.append(",");
			} else {
				sb.append(" set");
			}
			sb.append(" isvalid=?");
			args.add(aTO.isValid);
		}
		if (aTO.preferredDirty) {
			if (args.size() > 0) {
				sb.append(",");
			} else {
				sb.append(" set");
			}
			sb.append(" ispreferred=?");
			args.add(aTO.isPreferred);
		}
		sb.append(" where id=?");
		args.add(aTO.id);

		if (args.size() > 1) {

			Object[] arg2 = args.toArray();
			String sql = sb.toString();

			dbUtil.updateAndBringBack(sql, arg2);
		}
	}

	/**
   */
	protected MessageQueryTO updateMessageQuery(final MessageQueryTO mqTO) {

		// create if not yet exists
		if (Constants.UNDEFINED_ID == mqTO.id) {
			MessageQueryTO newto = createMessageQuery(mqTO.shortname,
					mqTO.name, mqTO.sql);
			return newto;
		}

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {
				StringBuffer sb = new StringBuffer(512);
				VarArgs args = new VarArgs();
				sb.append("update msgquery");
				if (mqTO.shortNameDirty) {
					sb.append(" set shortname=?");
					args.add(mqTO.shortname);
				}
				if (mqTO.nameDirty) {
					if (args.size() > 0) {
						sb.append(",");
					}
					sb.append(" set name=?");
					args.add(mqTO.name);
				}
				if (mqTO.sqlDirty) {
					if (args.size() > 0) {
						sb.append(",");
					}
					sb.append(" set sql=?");
					args.add(mqTO.sql);
				}
				sb.append(" where id=?");
				args.add(mqTO.id);

				if (args.size() > 1) {

					Object[] arg2 = args.toArray();
					String sql = sb.toString();

					dbUtil.updateAndBringBack(sql, arg2);
				}
			}
		};

		transaction(tb);

		MessageQueryTO res = getMessageQuery(mqTO.id);
		return res;
	}

	private void runSQLScript(final Statement stmt, final String filename)
			throws IOException, SQLException {

		LOG.debug("running DB script: " + filename);

		InputStream is = MailDB.class.getResourceAsStream(filename);
		BufferedReader in = new BufferedReader(new InputStreamReader(is));

		String str;
		StringBuffer sql = new StringBuffer(256);

		while ((str = in.readLine()) != null) {
			str = str.trim();
			// line may contain comment
			int cmtPos = str.indexOf("--");
			if (cmtPos >= 0) {
				str = str.substring(0, cmtPos);
			}
			if (str.length() > 0) {
				// after cutting comment, we still have some text
				sql.append(" ");
				sql.append(str);
				if (str.endsWith(";")) {
					// end of statement.execute
					String tmp = sql.toString();
					tmp = tmp.trim();
					LOG.debug("sql: '" + tmp + "'");
					stmt.execute(tmp);
					sql = new StringBuffer(256);
				}
			}
		}
		in.close();

		is.close();

	}

	/**
	 * run update schema update script and return new DB version as queried from
	 * updated DB
	 */
	protected String updateSchema(final String updateScript)
			throws SQLException, IOException {

		boolean ac = conn.getAutoCommit();

		conn.setAutoCommit(true);

		Statement stmt = conn.createStatement();
		runSQLScript(stmt, updateScript);

		stmt.close();
		stmt = null;

		conn.setAutoCommit(ac);

		return getVersion();
	}

	protected void checkCreateSchema(final String sqlCreateName)
			throws SQLException, IOException {

		Statement stmt = null;

		// check if table exists
		String[] test = dbUtil
				.getStrings("select name from sequence where name='message'");

		if (test.length <= 0) {
			// schema does not exist; create it
			stmt = conn.createStatement();
			// runSQLScript(stmt, "mailxel.sql");
			runSQLScript(stmt, sqlCreateName);
			stmt.close();
			stmt = null;
		}

		String currentVersion = getVersion();

		if ("0.5.2".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.5.2-0.6.0.sql");
		}

		if ("0.6.0".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.6.0-0.6.1.sql");
		}

		if ("0.6.1".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.6.1-0.6.4.sql");
		}

		if ("0.6.4".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.6.4-0.7.0.sql");
		}

		if ("0.7.0".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.7.0-0.7.1.sql");
		}

		if ("0.7.1".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.7.1-0.7.3.sql");
		}

		if ("0.7.3".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.7.3-0.7.4.sql");
		}

		if ("0.7.4".equals(currentVersion)) {
			LOG.info("updating DB to version 0.7.6. This may take a while (depending on DB size)");
			currentVersion = updateSchema("mailxel-update-0.7.4-0.7.6.sql");
		}
		if ("0.7.6".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.7.6-0.7.7.sql");
		}
		if ("0.7.7".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.7.7-0.8.0.sql");
		}
		if ("0.8.0".equals(currentVersion)) {
			LOG.info("*** starting long-running process to full-text index DB in 2 sec");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// ignore
			}
			LOG.info("starting full-text indexing");
			currentVersion = updateSchema("mailxel-update-0.8.0-0.8.1.sql");
		}
		if ("0.8.1".equals(currentVersion)) {
			currentVersion = updateSchema("mailxel-update-0.8.1-0.9.3.sql");
		}
	}

	protected String getVersion() {
		// check schema version
		String res = getConfig("mailxel-db-version");
		if (null == res) {
			res = "0.5.2";
		}
		return res;
	}

	private File createPermanentStoragePath(final File root, final Date date,
			final int msgid, final String subject) {

		String yyyymmdd = Util.formatFolderName(date);
		File yyyymmddDir = new File(root, yyyymmdd);
		yyyymmddDir.mkdirs();

		String fn = null;
		if (null == subject) {
			fn = Integer.toString(msgid) + ".zip";
		} else {
			fn = subject + "_" + Integer.toString(msgid) + ".zip";
		}
		fn = Util.conformify(fn);

		File res = new File(yyyymmddDir, fn);

		return res;
	}

	// private void filecopy(final File src, final File dest) {
	// try {
	// FileChannel srcChannel = new FileInputStream(src).getChannel();
	// FileChannel dstChannel = new FileOutputStream(dest).getChannel();
	// dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
	//
	// srcChannel.close();
	// dstChannel.close();
	// } catch (IOException e) {
	// LOG.warn(
	// "MailDB: error while copying temp attachment zip to permanent location:",
	// e);
	// }
	//
	// }

	protected void updateShortname(final String[] addresses,
			final String shortname) {

		for (int i = 0; i < addresses.length; i++) {
			AddressTO aTO = createAddress(addresses[i]);
			aTO.shortname = shortname;
			aTO.shortNameDirty = true;
			updateAddress(aTO);
		}

	}

	/** */
	protected void setInitialSeenCount() {

		String sql = "select addressid, count(messageid) c from addressxmessage group by addressid order by c";
		final List<Integer> res = new ArrayList<Integer>();

		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				while (rs.next()) {
					res.add(rs.getInt(1));
					res.add(rs.getInt(2));
				}
			}
		};
		dbUtil.executeAndBringBack(rsHandler, sql);

		int len = res.size();
		for (int i = 0; i < len; i = i + 2) {
			Integer count = res.get(i + 1);
			if (count > 1) {
				Integer id = res.get(i);
				sql = "update address set count=? where id=?";
				dbUtil.updateAndBringBack(sql, count, id);
			}
		}
	}

	/**
	 * update the short-access fields in table message for the specified address
	 * 
	 * @param addressId
	 */
	protected void invalidateShortname(final int[] addressIds) {

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {
				for (int i = 0; i < addressIds.length; i++) {
					dbUtil.updateAndBringBack(
							"update address set shortname=null where id=?",
							addressIds[i]);
				}
			}
		};
		transaction(tb);
	}

	/**
	 * update the short-access fields in table message for the specified address
	 * 
	 * @param addressId
	 */
	protected void updateAddressFieldsForAddress(int addressId) {

		final int[] messages = getMessagesByAddress(addressId);
		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {
				for (int i = 0; i < messages.length; i++) {
					updateAddressFields(messages[i]);
				}
			}
		};
		transaction(tb);
	}

	/**
	 * update the short-access fields in table message for the specified address
	 * 
	 * @param addressId
	 */
	protected void setToMeFlag(final int msgId, final String myAddress) {

		StringBuffer sb = new StringBuffer(1024);
		sb.append("update message set __fme__=(");
		sb.append("select count(addressid) from addressxmessage");
		sb.append(" where messageid=? and type=? and addressid in");
		sb.append(" (select id from address where isvalid=1 and shortname=");
		sb.append(" (select shortname from address where address=?))");
		sb.append(" ) where id=?");

		String tmp = sb.toString();

		String sqlF = tmp.replaceAll("__fme__", "fme");
		String sqlT = tmp.replaceAll("__tme__", "tme");

		dbUtil.executeAndBringBack(null, sqlF, msgId, "F", myAddress, msgId);
		dbUtil.executeAndBringBack(null, sqlT, msgId, "T", myAddress, msgId);
	}

	/**
	 * @deprecated use @see select(int,MessageSearchTO,int) instead
	 */
	public List<Envelope> select(final int page, final String from,
			final String to, final String fromOrTo, final String cc,
			final String bcc, final String dateFrom, final int daysRange,
			final String gtd, final String subject, final int maxRows) {

		MessageSearchTO msTO = new MessageSearchTO();
		msTO.from = from;
		msTO.to = to;
		msTO.fromOrTo = fromOrTo;
		msTO.cc = cc;
		msTO.bcc = bcc;
		msTO.dateFrom = dateFrom;
		msTO.daysRange = daysRange;
		msTO.gtd = gtd;
		msTO.subject = subject;

		ConfigTO cfg = new ConfigTO();
		cfg.maxSearchRows = maxRows;
		cfg.explcitWildcards = false;
		return select(page, msTO, cfg);

	}

	public List<Envelope> executeMessageQuery(final int page,
			final int maxRows, final MessageQueryTO mqTO) {

		updateMessageQueryCount(mqTO.id);

		String sql = "select sql from msgquery where id=?";
		sql = dbUtil.getString(sql, mqTO.id);

		StringBuffer sqlb = new StringBuffer(1024);
		sqlb.append("select m.id,mt.f,mt.t,strftime(\"%Y-%m-%d\",m.date),c.shortname,m.nattach,mt.subject,");
		sqlb.append(" strftime(\"%H:%M\",m.date),");
		sqlb.append(" m.curcatid,");
		sqlb.append(" julianday('now') - julianday(m.date),");
		sqlb.append(" m.fme, m.tme, m.count");
		sqlb.append(" from message m, messagetext mt, category c");
		sqlb.append(" where m.id in (");
		sqlb.append(sql);
		sqlb.append(")");
		sqlb.append(" and mt.docid=m.id and m.curcatid=c.id");
		sqlb.append(" order by m.date desc, mt.f, mt.t");

		sql = sqlb.toString();
		List<Envelope> res = select(page, maxRows, sql);

		return res;
	}

	public Long count(final MessageQueryTO mqTO) {

		updateMessageQueryCount(mqTO.id);

		String sql = "select sql from msgquery where id=?";
		sql = dbUtil.getString(sql, mqTO.id);

		int cnt = dbUtil.getInt(sql, (Object[]) null);

		return new Long(cnt);
	}

	public List<Envelope> selectRecent(final int page, final ConfigTO cfg) {

		MessageQueryTO t = new MessageQueryTO();
		t.id = 995;
		return executeMessageQuery(page, cfg.maxSearchRows, t);

	}

	public List<Envelope> selectFTS(final int page, final MessageSearchTO msTO,
			final ConfigTO cfg) {

		String fts = msTO.fts;

		// empty query?
		if (null == fts || fts.length() < 1) {
			return selectRecent(page, cfg);
		}

		// rewrite query string if necessary
		fts = msTO.fts;
		if (fts.contains("s:")) {
			fts = fts.replace("s:", "subject:");
		}
		if (fts.contains("b:")) {
			fts = fts.replace("b:", "body:");
		}
		String date = null;
		String op = null;
		if (fts.contains("d:") || fts.contains("d>") || fts.contains("d<")) {
			Pattern p = Pattern
					.compile(".*d([<>:])([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]).*");
			Matcher m = p.matcher(fts);
			if (m.matches()) {
				op = m.group(1);
				date = m.group(2);
				String tmp = fts.substring(0, m.start(1) - 1)
						+ fts.substring(m.end(2));
				fts = tmp;
			}
		}
		if (null != op && op.equals(":")) {
			op = "=";
		}
		fts = fts.trim();

		// d:2010-05-01
		boolean dateOnly = false;
		if (null != date && fts.length() < 1) {
			// date only query
			dateOnly = true;
		}

		// create query
		StringBuffer sqlb = new StringBuffer(1024);
		// sqlb.append("select m.id,mt.f,mt.t,strftime(\"%Y-%m-%d\",m.date),m.gtd,m.nattach,mt.subject,");
		sqlb.append("select m.id,mt.f,mt.t,strftime(\"%Y-%m-%d\",m.date),c.shortname,m.nattach,mt.subject,");
		sqlb.append(" strftime(\"%H:%M\",m.date),");
		sqlb.append(" m.curcatid,");
		sqlb.append(" julianday('now') - julianday(m.date),");
		sqlb.append(" fme,tme,m.count");
		sqlb.append(" from message m, messagetext mt, category c");
		sqlb.append(" where m.id=mt.docid");
		if (!dateOnly) {
			sqlb.append(" and messagetext match ?");
		}
		sqlb.append(" and m.deleted=0");
		sqlb.append(" and m.curcatid=c.id");
		if (null != date) {
			sqlb.append(" and round(julianday(m.date),0) ");
			sqlb.append(op);
			sqlb.append(" round(julianday(?),0)");
		}

		sqlb.append(" order by m.date desc, mt.f, mt.t");

		String sql = sqlb.toString();

		List<Envelope> res = null;
		if (dateOnly) {
			res = select(page, cfg.maxSearchRows, sql, date);
		} else {
			if (null == date) {
				// normal
				res = select(page, cfg.maxSearchRows, sql, fts);
			} else {
				// fts and date
				res = select(page, cfg.maxSearchRows, sql, fts, date);
			}
		}

		return res;
	}

	private List<Envelope> select(final int page, final int maxRows,
			final String sql, Object... args) {

		final List<Envelope> res = new ArrayList<Envelope>();
		final int[] rowCnts = { 0 };

		ResultSetHandler rsh = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {

				int intRowCnt = 0;

				int firstRelevantRow = maxRows * page;
				int firstBreakRow = maxRows * (page + 1);

				while (rs.next()) {

					if (intRowCnt < firstRelevantRow) {
						intRowCnt++;
						continue;
					}

					if (intRowCnt > firstBreakRow) {
						break;
					}

					Envelope env = new Envelope();

					env.id = rs.getInt(1);
					env.from = rs.getString(2);
					env.to = rs.getString(3);
					env.date = rs.getString(4);
					env.GTD = rs.getString(5);
					env.nattach = rs.getInt(6);
					env.subject = rs.getString(7);
					env.time = rs.getString(8);
					env.curcatid = rs.getInt(9);
					int age = rs.getInt(10);
					env.fromMe = Util.i2b(rs.getInt(11));
					env.toMe = Util.i2b(rs.getInt(12));
					env.count = rs.getInt(13);

					// set urgency based on catid and age
					// 997,'DON', 994,'IGN', 993,'TON'
					if (env.toMe && !env.fromMe) {
						if (age > 60) {
							// ignore
						} else {
							if (env.curcatid == 997 || env.curcatid == 994
									|| env.curcatid == 993) {
								env.urgency = 0;
							} else {
								if (age > 20) {
									env.urgency = 4;
								} else if (age > 10) {
									env.urgency = 3;
								} else if (age > 5) {
									env.urgency = 2;
								} else if (age > 1) {
									env.urgency = 1;
								} else {
									env.urgency = 0;
								}
							}
						}
					}
					res.add(env);

					intRowCnt++;
				}
				rowCnts[0] = intRowCnt;
			}
		};

		dbUtil.executeAndBringBack(rsh, sql, args);

		return res;
	}

	public List<AttachmentTO> getAttachments(final int mailId) {

		final List<AttachmentTO> tmp = new ArrayList<AttachmentTO>();

		ResultSetHandler rshAttachmentNames = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				while (rs.next()) {
					AttachmentTO aTO = new AttachmentTO();
					aTO.id = rs.getInt(1);
					aTO.messageid = rs.getInt(2);
					aTO.name = rs.getString(3);
					aTO.md5 = rs.getString(4);
					tmp.add(aTO);
				}
			}
		};
		dbUtil.executeAndBringBack(
				rshAttachmentNames,
				"select id,messageid,name,md5 from attachment where messageid=?",
				mailId);

		return tmp;
	}

	/**
	 * mailxel stores attachments with the same MD5 hash just once; this method
	 * gives back the attachment descriptor of the _first_ attachment seen with
	 * the MD5 of the selected attachment id
	 * 
	 * @param attachmentId
	 *            requested (but not necessarily returned) attachment id
	 * @return attachment information of the first stored attachment with the
	 *         same MD5
	 */
	public AttachmentTO getAttachment(final int attachmentId) {

		final AttachmentTO[] res = new AttachmentTO[1];

		ResultSetHandler rsh = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					res[0] = new AttachmentTO();
					res[0].id = rs.getInt(1);
					res[0].messageid = rs.getInt(2);
					res[0].name = rs.getString(3);
					res[0].md5 = rs.getString(4);
				}
			}
		};
		dbUtil.executeAndBringBack(
				rsh,
				"select id,messageid,name,md5 from attachment where md5=(select md5 from attachment where id=?) order by id",
				attachmentId);
		return res[0];
	}

	/**
	 * accesses attachment data as string; intended to be used for text
	 * attachments
	 * 
	 * @param attachmentId
	 * @return attachment content as string
	 */
	public String getAttachmentData(final int attachmentId) {

		// from attachment id: find corresponding message id (for zip name)
		AttachmentTO aTO = getAttachment(attachmentId);

		// stream from zip file to memory
		ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
		streamAttachmentFromZipfile(aTO, baos);

		String res = baos.toString();
		baos = null;

		return res;
	}

	/**
   */
	public void streamIcon(final int iconId, final OutputStream outputStream)
			throws IOException {

		ResultSetHandler rsh = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					byte[] data = rs.getBytes(1);
					try {
						outputStream.write(data);
					} catch (IOException e) {
						throw new SQLException(e);
					}
				}
			}
		};
		dbUtil.executeAndBringBack(rsh, "select data from icon where id=?",
				iconId);
	}

	/**
   */
	public BlobTO getIcon(final int iconId) {

		final BlobTO[] res = new BlobTO[1];

		ResultSetHandler rsh = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					res[0] = new BlobTO();
					int id = rs.getInt(1);
					String name = rs.getString(2);
					byte[] data = rs.getBytes(3);
					res[0].id = id;
					res[0].name = name;
					res[0].data = data;
				}
			}
		};
		dbUtil.executeAndBringBack(rsh,
				"select id,name,data from icon where id=?", iconId);
		return res[0];
	}

	public MailTO get(final int id) {

		final MailTO[] res = new MailTO[1];

		ResultSetHandler rsh = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					res[0] = new MailTO();
					res[0].id = rs.getInt(1);
					res[0].from = rs.getString(2);
					res[0].to = rs.getString(3);
					res[0].cc = rs.getString(4);
					res[0].bcc = rs.getString(5);
					res[0].date = rs.getString(6);
					res[0].subject = rs.getString(7);
					res[0].body = rs.getString(8);
					res[0].GTD = rs.getString(9);
					res[0].nattach = rs.getInt(10);
					res[0].azip = rs.getString(11);
					res[0].time = rs.getString(12);
					res[0].curcatid = rs.getInt(13);
					res[0].count = rs.getInt(14);
				}
			}
		};
		dbUtil.executeAndBringBack(
				rsh,
				"select m.id,mt.f,mt.t,mt.c,mt.b,strftime(\"%Y-%m-%d\",m.date),mt.subject,mt.body,c.shortname,nattach,azip,strftime(\"%H:%M\",m.date),curcatid,m.count from message m,messagetext mt, category c where m.id=? and mt.docid=m.id and m.curcatid=c.id",
				id);

		ResultSetHandler rshCats = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				while (rs.next()) {
					Category cat = new Category();
					cat.id = rs.getInt(1);
					cat.shortname = rs.getString(2);
					cat.name = rs.getString(3);
					cat.deleted = false;
					int itmp = rs.getInt(4);
					if (itmp != 0) {
						cat.deleted = true;
					}
					AttachedCategoryTO attachedCategoryTO = new AttachedCategoryTO();
					attachedCategoryTO.category = cat;
					attachedCategoryTO.date = rs.getString(5);
					res[0].categories.add(attachedCategoryTO);
				}
			}
		};
		dbUtil.executeAndBringBack(
				rshCats,
				"select c.id,c.shortname,c.name,c.deleted,strftime(\"%Y-%m-%d\",cxm.date) from category c, categoryxmessage cxm where cxm.messageid=? and cxm.categoryid=c.id order by cxm.date desc",
				id);

		res[0].attachments = getAttachments(id);
		//
		// ResultSetHandler rshAttachmentNames = new ResultSetHandler() {
		//
		// public void handle(ResultSet rs) throws SQLException {
		// while (rs.next()) {
		// AttachmentTO aTO = new AttachmentTO();
		// aTO.id = rs.getInt(1);
		// aTO.messageid = rs.getInt(2);
		// aTO.name = rs.getString(3);
		// aTO.md5 = rs.getString(4);
		// res[0].attachments.add(aTO);
		// }
		// }
		// };
		// dbUtil.executeAndBringBack(rshAttachmentNames,
		// "select id,messageid,name,md5 from attachment where messageid=?",
		// id);

		ResultSetHandler rshAddresses = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				while (rs.next()) {
					int aid = rs.getInt(1);
					String type = rs.getString(2);
					AddressTO aTO = getAddress(aid);
					if ("F".equals(type)) {
						res[0].fromATO = aTO;
					} else if ("T".equals(type)) {
						res[0].toATOs.add(aTO);
					} else if ("C".equals(type)) {
						res[0].ccATOs.add(aTO);
					} else if ("B".equals(type)) {
						res[0].bccATOs.add(aTO);
					}
				}
			}
		};
		dbUtil.executeAndBringBack(
				rshAddresses,
				"select addressid, type from addressxmessage where messageid=?",
				id);

		updateMessageAccessCount(id);

		return res[0];
	}

	/**
	 * @param msgId
	 */
	protected void updateAddressFields(int msgId) {

		StringBuffer sb = new StringBuffer(1024);
		sb.append("update messagetext set _from_=");
		sb.append("  (select group_concat(ifnull(shortname, address))");
		sb.append("   from addressxmessage, address");
		sb.append("   where messageid=? and addressid=id and type=?)");
		sb.append("where docid=?");
		String sql = sb.toString();

		Integer ID = new Integer(msgId);

		String sql1 = sql.replaceAll("_from_", "f");
		dbUtil.updateAndBringBack(sql1, ID, "F", ID);

		sql1 = sql.replaceAll("_from_", "t");
		dbUtil.updateAndBringBack(sql1, ID, "T", ID);

		sql1 = sql.replaceAll("_from_", "c");
		dbUtil.updateAndBringBack(sql1, ID, "C", ID);

		sql1 = sql.replaceAll("_from_", "b");
		dbUtil.updateAndBringBack(sql1, ID, "B", ID);

		sb = new StringBuffer(1024);
		sb.append("update messagetext set r=");
		sb.append("  (select");
		sb.append(" group_concat(ifnull(a.shortname,'') || ' ' || ifnull(a.name,'') || ' ' || a.address)");
		sb.append(" from addressxmessage axm, address a");
		sb.append(" where axm.messageid=? and axm.addressid=a.id)");
		sb.append(" where docid=?");
		sql = sb.toString();
		dbUtil.updateAndBringBack(sql, ID, ID);

	}

	protected void updateCategoryField(int id) {

		StringBuffer sb = new StringBuffer(1024);
		sb.append("update messagetext set GTD=");
		sb.append("  (select group_concat(ifnull(shortname,name))");
		sb.append("     from categoryxmessage, category");
		sb.append("     where messageid=? and categoryid=id)");
		sb.append("where docid=?");
		String sql = sb.toString();

		Integer ID = new Integer(id);
		dbUtil.updateAndBringBack(sql, ID, ID);
	}

	public List<AddressTO> suggestAddress(final String query,
			final int maxAddressSuggestions) {

		final List<AddressTO> res = new ArrayList<AddressTO>();

		if (null == query) {
			return res;
		}

		String sql = null;
		String q = query;
		if (query.length() <= 3 && query.matches("[A-Z]{2,3}")) {
			sql = "select id,shortname,name,address from address where shortname=? and isvalid=1 order by count desc, ispreferred desc, address";
		} else {
			sql = "select id,shortname,name,address from address where address like ? and isvalid=1 order by count desc, ispreferred desc, address";
			q = "%" + query + "%";
		}

		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				int cntRows = 0;

				while (rs.next()) {
					cntRows++;
					if (maxAddressSuggestions > 0
							&& cntRows > maxAddressSuggestions) {
						break;
					}
					AddressTO tmp = new AddressTO();
					tmp.id = rs.getInt(1);
					tmp.shortname = rs.getString(2);
					tmp.name = rs.getString(3);
					tmp.address = rs.getString(4);
					res.add(tmp);
				}
			}
		};

		dbUtil.executeAndBringBack(rsHandler, sql, q);
		return res;
	}

	public List<AddressTO> searchAddresses(final String shortname,
			final String address) {

		// create query
		StringBuffer sqlb = new StringBuffer(1024);
		sqlb.append("select id,shortname,name,address,isvalid,ispreferred,strftime(\"%Y-%m-%d\",lastseen),count from address");
		boolean hasWhere = false;

		final int maxRows = 100;

		VarArgs varargs = new VarArgs();

		if (null != shortname) {
			if (false == hasWhere) {
				sqlb.append(" where");
				hasWhere = true;
			}
			sqlb.append(" shortname like ?");
			varargs.add(shortname);
		}

		if (null != address) {
			if (false == hasWhere) {
				sqlb.append(" where");
				hasWhere = true;
			} else {
				sqlb.append(" and");
			}
			sqlb.append(" address like ?");
			varargs.add(address);
		}

		sqlb.append(" order by count desc, address, shortname");

		String sql = sqlb.toString();

		final List<AddressTO> res = new ArrayList<AddressTO>();

		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				int rowCnt = 0;
				while (rs.next()) {
					if (rowCnt > maxRows) {
						break;
					}
					AddressTO env = new AddressTO();
					env.id = rs.getInt(1);
					env.shortname = rs.getString(2);
					env.name = rs.getString(3);
					env.address = rs.getString(4);
					int tmp = rs.getInt(5);
					env.isValid = Util.i2b(tmp);
					tmp = rs.getInt(6);
					env.isPreferred = Util.i2b(tmp);
					env.lastSeen = rs.getString(7);
					env.count = rs.getInt(8);
					res.add(env);
					rowCnt++;
				}
			}
		};

		Object[] args = varargs.toArray();
		dbUtil.executeAndBringBack(rsHandler, sql, args);

		return res;
	}

	/**
	 * @param id
	 *            record id
	 * @return {@link AddressTO}
	 */
	public AddressTO getAddress(final int id) {

		String sql = "select id,shortname,name,address,isvalid,ispreferred,strftime(\"%Y-%m-%d\",lastseen),count from address where id=?";
		final AddressTO res = new AddressTO();
		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					res.id = rs.getInt(1);
					res.shortname = rs.getString(2);
					res.name = rs.getString(3);
					res.address = rs.getString(4);
					int t = rs.getInt(5);
					res.isValid = Util.i2b(t);
					t = rs.getInt(6);
					res.isPreferred = Util.i2b(t);
					res.lastSeen = rs.getString(7);
					res.count = rs.getInt(8);
				}
			}
		};
		dbUtil.executeAndBringBack(rsHandler, sql, id);
		return res;
	}

	/**
	 * @param address
	 *            , like foo.bar@foobar.com
	 * @return {@link AddressTO}
	 */
	public AddressTO getAddress(final String address) {

		String sql = "select id,shortname,name,address,isvalid,ispreferred,strftime(\"%Y-%m-%d\",lastseen),count from address where address=?";
		final AddressTO res = new AddressTO();
		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					res.id = rs.getInt(1);
					res.shortname = rs.getString(2);
					res.name = rs.getString(3);
					res.address = rs.getString(4);
					int t = rs.getInt(5);
					res.isValid = Util.i2b(t);
					t = rs.getInt(6);
					res.isPreferred = Util.i2b(t);
					res.lastSeen = rs.getString(7);
					res.count = rs.getInt(8);
				}
			}
		};
		dbUtil.executeAndBringBack(rsHandler, sql, address);
		return res;
	}

	protected String recalculateAttachementZipName(final int msgid) {

		String res = null;

		String sql = "select count(id) from attachment where messageid=?";
		int[] tmp = dbUtil.getInts(sql, msgid);
		if (null != tmp && tmp.length > 0 && tmp[0] > 0) {
			// has attachements
			sql = "select strftime(\"%Y/%m/%d\",m.date),mt.subject from message m, messagetext mt where m.id=? and mt.docid=m.id";
			PrepStmt ps = prepStmtCache.prepare(sql, msgid);
			int[] indexes = { 1, 2 };
			int[] types = { Types.VARCHAR, Types.VARCHAR };
			List<Object[]> rows = JDBCUtil.get(ps, types, indexes);
			if (null != rows && rows.size() > 0) {
				Object[] row = rows.get(0);
				String zDate = (String) row[0];
				Date date = Util.toDate(zDate);
				String subject = (String) row[1];
				File dest = createPermanentStoragePath(attachmentRoot, date,
						msgid, subject);
				res = FileUtil.truncateRoot(dest, attachmentRoot);
				sql = "update message set azip=?,nattach=? where id=?";
				dbUtil.updateAndBringBack(sql, res, tmp[0], msgid);
			}
		}
		return res;
	}

	protected int[] getMessagesByDate(final String date) {

		int[] ids = dbUtil.getInts("select id from message where date(date)=?",
				date);
		return ids;
	}

	/**
	 * @param addressId
	 * @return ids of messages where this address occurs
	 */
	protected int[] getMessagesByAddress(final int addressId) {

		int[] ids = dbUtil.getInts(
				"select messageid from addressxmessage where addressid=?",
				addressId);
		return ids;
	}

	/**
	 * @return ids of addresses which have a shortname of length 0
	 */
	protected int[] getUndefinedShortnames() {

		int[] ids = dbUtil
				.getInts(" select id from address where shortname is not null and length(shortname)=0");
		return ids;
	}

	/**
	 * @return address ids which carry same shortname as given address
	 */
	protected int[] getAddressIdsWithSameShortname(final String address) {

		StringBuffer sb = new StringBuffer(1024);
		sb.append("select id from address where isvalid=1 and shortname=");
		sb.append("(select shortname from address where address=?)");
		String sql = sb.toString();

		int[] ids = dbUtil.getInts(sql, address);
		return ids;
	};

	/**
	 * return id(s) of valid address
	 */
	protected int[] getAddressId(final String address) {

		StringBuffer sb = new StringBuffer(1024);
		sb.append("select id from address where isvalid=1 and address=?");
		String sql = sb.toString();

		int[] ids = dbUtil.getInts(sql, address);
		return ids;
	};

	public void saveCategories(final List<Category> categories) {

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {

				for (Category category : categories) {
					findOrStoreCategory(category.id, category.shortname,
							category.name);
				}
			}
		};
		transaction(tb);
	}

	private int findOrStoreCategory(final int id, final String shortname,
			final String name) {

		int res = -1;

		int[] tmp = dbUtil.getInts("select id from category where id=?", id);
		if (tmp.length > 0) {
			res = tmp[0];
			dbUtil.updateAndBringBack(
					"update address set shortname=?, name=? where id=?",
					shortname, name, res);
		} else {
			// int nextid = JDBCUtil.poorMansSequence(conn, "category");
			int nextid = dbUtil.poorMansSequence("category");
			dbUtil.updateAndBringBack(
					"insert into category (id,shortname,name,deleted) values (?,?,?,0)",
					nextid, shortname, name);
			res = nextid;
		}

		// now update all mails which reference to this address
		tmp = dbUtil.getInts(
				"select messageid from categoryxmessage where categoryid=?",
				res);
		for (int i = 0; i < tmp.length; i++) {
			updateCategoryField(tmp[i]);
		}

		return res;
	}

	public List<Category> searchCategories(final String name) {

		// create query
		StringBuffer sqlb = new StringBuffer(1024);
		sqlb.append("select id,name from category");
		boolean hasWhere = false;

		VarArgs args = new VarArgs();

		if (null != name) {
			if (false == hasWhere) {
				sqlb.append(" where");
				hasWhere = true;
			}
			sqlb.append(" name like ?");
			if (name.length() < 1) {
				args.add("%");
			} else {
				args.add(name);
			}
		}

		sqlb.append(" order by count desc, name");

		String sql = sqlb.toString();

		// PrepStmt ps = prepStmtCache.prepareNonNull(sql, name);

		final List<Category> res = new ArrayList<Category>();

		final int maxRows = 100;
		final int[] rowCnts = { 0 };

		ResultSetHandler rsh = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				while (rs.next()) {
					if (rowCnts[0] > maxRows) {
						break;
					}
					Category env = new Category();
					env.id = rs.getInt(1);
					env.name = rs.getString(2);
					res.add(env);
					rowCnts[0]++;
				}
			}
		};
		Object[] tmp = args.toArray();
		dbUtil.executeAndBringBack(rsh, sql, tmp);

		return res;
	}

	public List<Category> suggestCategory(final String query) {

		if (null == query) {
			return new ArrayList<Category>();
		}

		String sql = "select id,shortname,name from category where deleted<>1 and name like ? order by count desc, name";
		String q = "%" + query + "%";

		final List<Category> res = new ArrayList<Category>();

		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				while (rs.next()) {
					Category env = new Category();
					env.id = rs.getInt(1);
					env.shortname = rs.getString(2);
					env.name = rs.getString(3);
					res.add(env);
				}
			}

		};

		dbUtil.executeAndBringBack(rsHandler, sql, q);

		return res;
	}

	public void markAsDeleted(final List<Integer> ids) {

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {
				String zIds = ListUtil.asComaSeparted(ids);
				String sql = "update message set deleted=1 where id in ("
						+ zIds + ")";
				// PrepStmt ps = prepStmtCache.borrowAndPrepare(sql);
				dbUtil.updateAndBringBack(sql);
				// JDBCUtil.execute(ps);
			}
		};

		transaction(tb);
	}

	public void updateCategories(final List<Integer> ids,
			final String categories) {

		if (null == categories || categories.length() < 1) {
			return;
		}

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {

				// find ids of categories
				String[] cats = ListUtil.getFromComaSeparated(categories);
				String sql = "select id from category where shortname=? or name=?";
				List<Integer> catIds = new ArrayList<Integer>();
				for (int i = 0; i < cats.length; i++) {
					String tmp = cats[i].trim();
					int[] id = dbUtil.getInts(sql, tmp, tmp);
					if (id.length > 0) {
						catIds.add(id[0]);
					}
				}

				for (Integer mId : ids) {
					for (Integer catId : catIds) {
						// check if exists
						sql = "select messageid from categoryxmessage where categoryid=? and messageid=?";
						int[] t2 = dbUtil.getInts(sql, catId, mId);
						if (t2.length < 1) {
							categorizeMessage(mId, catId);
						}
					}
					updateCategoryField(mId);
				}
			}
		};
		transaction(tb);
	}

	protected void categorizeMessage(final int msgId, final int catId) {
		dbUtil.updateAndBringBack("update message set curcatid=? where id=?",
				catId, msgId);
		String sql = "insert into categoryxmessage (categoryid,messageid,date) values (?,?,date('now'))";
		dbUtil.updateAndBringBack(sql, catId, msgId);
		// update category use count
		dbUtil.updateAndBringBack(
				"update category set count=count+1 where id=?", catId);
	}

	public void transaction(final TransactionBody transactionBody) {

		prepStmtCache.transaction(transactionBody);
	}

	public boolean verifySchema() {

		boolean res = false;

		return res;

	}

	protected DatabaseMetaData getMetaData() {
		DatabaseMetaData metaData = null;
		try {
			metaData = conn.getMetaData();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
		return metaData;
	}

	public void markStatus(final int msgId, final int categoryId) {

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {
				categorizeMessage(msgId, categoryId);
				updateCategoryField(msgId);
			}
		};
		transaction(tb);
	}

	public void streamAttachmentFromZipfile(final AttachmentTO aTO,
			final OutputStream outputStream) {

		ZipInputStream in = null;

		try {

			File file = getAttachmentZipfile(aTO);
			in = new ZipInputStream(new FileInputStream(file));

			boolean found = false;
			ZipEntry entry = in.getNextEntry();
			while (!found) {
				if (null != entry && null != entry.getName()
						&& entry.getName().equals(aTO.name)) {
					found = true;
					break;
				}
				in.closeEntry();
				entry = in.getNextEntry();
			}

			if (null == entry) {
				LOG.debug("attachment not found in zip file");
				return;
			}
			// Transfer bytes from the ZIP file to the output file
			int bufSize = (int) 65536;
			byte[] buf = new byte[bufSize];
			int len = 0;
			while ((len = in.read(buf)) > 0) {
				outputStream.write(buf, 0, len);
			}

		} catch (IOException e) {
			LOG.debug("error extracting attachment from zip file", e);
		} finally {

			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.warn("error closing zip", e);
				}
				in = null;
			}
		}

	}

	public File getAttachmentZipfile(final AttachmentTO aTO) {
		String zipName = recalculateAttachementZipName(aTO.messageid);

		// Open the ZIP file
		File file = new File(attachmentRoot, zipName);

		return file;
	}

	/** used for performance tuning */
	protected void genericQuery(final String sql) {

		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				ResultSetMetaData md = rs.getMetaData();
				int cols = md.getColumnCount();
				int rowcnt = 0;
				while (rs.next()) {
					if (rowcnt > 3) {
						break;
					}
					StringBuffer sb = new StringBuffer(1024);
					for (int i = 1; i <= cols; i++) {
						if (i > 1) {
							sb.append(" ");
						}
						sb.append(rs.getString(i));
					}
					System.out.println(sb.toString());
					rowcnt++;
				}
			}
		};

		Object[] args = null;

		dbUtil.executeAndBringBack(rsHandler, sql, args);
	}

	public void setTiming(final boolean doTime) {
		prepStmtCache.setTiming(doTime);
	}

	public long getTotalNumMessages() {

		String sql = "select count(*) from message";
		final long[] tmp = new long[1];

		ResultSetHandler rsHandler = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					tmp[0] = rs.getLong(1);
				}
			}
		};
		dbUtil.executeAndBringBack(rsHandler, sql);

		return tmp[0];
	}

	public StatisticsTO calculateStatistics() {

		final StatisticsTO res = new StatisticsTO();

		res.totalLastDay = dbUtil
				.getInt("select count(*) from message where date>date('now','-1 day')");
		res.totalLastWeek = dbUtil
				.getInt("select count(*) from message where date>date('now','-7 days')");
		res.totalLastMonth = dbUtil
				.getInt("select count(*) from message where date>date('now','-30 days')");
		res.totalLastYear = dbUtil
				.getInt("select count(*) from message where date>date('now','-365 days')");

		res.numRecievedLastDay = dbUtil
				.getInt("select count(*) from message where date>date('now','-1 day') and tme=1");
		res.numRecievedLastWeek = dbUtil
				.getInt("select count(*) from message where date>date('now','-7 day') and tme=1");
		res.numRecievedLastMonth = dbUtil
				.getInt("select count(*) from message where date>date('now','-30 day') and tme=1");
		res.numRecievedLastYear = dbUtil
				.getInt("select count(*) from message where date>date('now','-365 day') and tme=1");

		res.numSentLastDay = dbUtil
				.getInt("select count(*) from message where date>date('now','-1 day') and fme=1");
		res.numSentLastWeek = dbUtil
				.getInt("select count(*) from message where date>date('now','-7 day') and fme=1");
		res.numSentLastMonth = dbUtil
				.getInt("select count(*) from message where date>date('now','-30 day') and fme=1");
		res.numSentLastYear = dbUtil
				.getInt("select count(*) from message where date>date('now','-365 day') and fme=1");

		String me = getConfig("me");
		String myDomain = null;
		if (null != me) {
			String[] tmp = me.split("@");
			if (null != tmp && tmp.length > 1) {
				myDomain = "%" + tmp[1];
			}
		}

		StringBuffer sb = new StringBuffer(512);
		sb.append("select count(*) from");
		sb.append(" (select distinct m.id from message m, address a, addressxmessage axm");
		sb.append(" where m.date>date('now',?)");
		sb.append(" and axm.messageid=m.id");
		sb.append(" and axm.addressid=a.id");
		sb.append(" and axm.type=?");
		sb.append(" and a.address like ?)");

		String sql = sb.toString();

		res.numRecievedLastWeekSameDomain = dbUtil.getInt(sql, "-7 day",
				MailAPIUtil.FROM, myDomain);
		res.numSentLastWeekSameDomain = dbUtil.getInt(sql, "-7 day",
				MailAPIUtil.TO, myDomain);

		sb = new StringBuffer(512);
		sb.append("select count(*) from");
		sb.append(" (select distinct m.id from message m, address a, addressxmessage axm");
		sb.append(" where m.date>date('now',?)");
		sb.append(" and axm.messageid=m.id");
		sb.append(" and axm.addressid=a.id");
		sb.append(" and axm.type=?");
		sb.append(" and a.address not like ?)");

		sql = sb.toString();

		res.numRecievedLastWeekOtherDomain = dbUtil.getInt(sql, "-7 day",
				MailAPIUtil.FROM, myDomain);
		res.numSentLastWeekOtherDomain = dbUtil.getInt(sql, "-7 day",
				MailAPIUtil.TO, myDomain);

		return res;
	}

	private void appendSubQueryFromToMe(final StringBuffer sb,
			final boolean withMessageId) {
		sb.append("(select m.id");
		sb.append(" from message m, address a, addressxmessage axm");
		sb.append(" where axm.messageid=m.id");
		if (withMessageId) {
			sb.append(" and m.id=?");
		}
		sb.append(" and axm.type=?");
		sb.append(" and axm.addressid=a.id");
		sb.append(" and a.id in");
		sb.append(" (select id from address");
		sb.append(" where shortname=(select shortname from address where address=");
		sb.append(" (select v from conf where k='me'))))");
	}

	/** @expects-transaction */
	public void updateFromMeToMeFlag(final String type, final int msgId) {
		StringBuffer sb = new StringBuffer(512);
		if (type.equals(MailAPIUtil.FROM)) {
			sb.append("update message set fme=1 where id in");
		} else {
			sb.append("update message set tme=1 where id in");
		}
		appendSubQueryFromToMe(sb, true);

		String sql = sb.toString();
		dbUtil.updateAndBringBack(sql, msgId, type);
	}

	public void initFromMeToMeFlag() {
		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {
				dbUtil.updateAndBringBack("update message set fme=0, tme=0");
			}
		};
		transaction(tb);
	}

	public void updateFromMeToMeFlag(final String type) {

		if (!MailAPIUtil.FROM.equals(type) && !MailAPIUtil.TO.equals(type)) {
			throw new IllegalArgumentException("type must be either 'F' or 'T'");
		}

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {

				StringBuffer sb = new StringBuffer(512);
				if (type.equals(MailAPIUtil.FROM)) {
					sb.append("update message set fme=1 where id in");
				} else {
					sb.append("update message set tme=1 where id in");
				}
				appendSubQueryFromToMe(sb, false);

				String sql = sb.toString();
				dbUtil.updateAndBringBack(sql, type);
			}

		};
		transaction(tb);

	}

	protected void saveConfig(final String key, final String value) {

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {

				dbUtil.updateAndBringBack("delete from conf where k=?", key);
				dbUtil.updateAndBringBack(
						"insert into conf (k,v) values (?,?)", key, value);
			}
		};
		transaction(tb);
	}

	protected String getConfig(final String key) {

		String res = dbUtil.getString("select v from conf where k=?", key);
		return res;
	}

	public List<MessageQueryTO> searchQueries(final String type,
			final String queryName) {

		// create query
		StringBuffer sqlb = new StringBuffer(1024);
		sqlb.append("select id,shortname,name,type,iconid,sql,count from msgquery");

		boolean hasWhere = false;

		VarArgs args = new VarArgs();

		if (null != type && type.length() > 0) {
			if (!hasWhere) {
				sqlb.append(" where");
				hasWhere = true;
			}
			sqlb.append(" type=?");
			args.add(type);
		}

		if (null != queryName) {
			if (!hasWhere) {
				sqlb.append(" where");
				hasWhere = true;
			} else {
				sqlb.append(" and");
			}
			sqlb.append(" name like ?");
			if (queryName.length() < 1) {
				args.add("%");
			} else {
				args.add(queryName);
			}
		}

		sqlb.append(" order by count desc, name");

		String sql = sqlb.toString();

		final List<MessageQueryTO> res = new ArrayList<MessageQueryTO>();

		ResultSetHandler rsh = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				while (rs.next()) {
					MessageQueryTO env = new MessageQueryTO();
					env.id = rs.getInt(1);
					env.shortname = rs.getString(2);
					env.name = rs.getString(3);
					env.type = rs.getString(4);
					env.iconId = rs.getInt(5);
					if (rs.wasNull()) {
						env.iconId = Constants.UNDEFINED_ID;
					}
					env.sql = rs.getString(6);
					env.count = rs.getInt(7);
					res.add(env);
				}
			}
		};
		Object[] tmp = args.toArray();
		dbUtil.executeAndBringBack(rsh, sql, tmp);

		return res;
	}

	public MessageQueryTO getMessageQuery(final int id) {

		// create query
		StringBuffer sqlb = new StringBuffer(256);
		sqlb.append("select id,shortname,name,type,iconid,sql,count from msgquery where id=?");
		String sql = sqlb.toString();

		final MessageQueryTO res = new MessageQueryTO();

		ResultSetHandler rsh = new ResultSetHandler() {

			public void handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					res.id = rs.getInt(1);
					res.shortname = rs.getString(2);
					res.name = rs.getString(3);
					res.type = rs.getString(4);
					res.iconId = rs.getInt(5);
					if (rs.wasNull()) {
						res.iconId = Constants.UNDEFINED_ID;
					}
					res.sql = rs.getString(6);
					res.count = rs.getInt(7);
				}
			}
		};
		dbUtil.executeAndBringBack(rsh, sql, id);

		return res;
	}

	/** increase access count on table msgquery */
	protected void updateMessageQueryCount(final int id) {
		updateCount("update msgquery set count=count+1 where id=?", id);
	}

	/** increase access count on table message */
	protected void updateMessageAccessCount(final int id) {
		updateCount("update message set count=count+1 where id=?", id);
	}

	/** increase count on table category */
	protected void updateCategoryUseCount(final int id) {
		updateCount("update category set count=count+1 where id=?", id);
	}

	/** increase access count on table message */
	private void updateCount(final String sql, final int id) {

		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {
				dbUtil.updateAndBringBack(sql, id);
			}
		};

		transaction(tb);
	}

	/**
	 * @category requires surrounding transaction
	 * @return newly created or existing MessageQuery, or null
	 */
	protected MessageQueryTO createMessageQuery(final String shortName,
			final String name, final String sql) {

		int nextid = dbUtil.poorMansSequence("msgquery");
		StringBuffer sb = new StringBuffer(1024);
		VarArgs args = new VarArgs();
		sb.append("insert into msgquery (id, shortname, name, sql, count, type)");
		sb.append(" values (?,?,?,?,?,'M')");
		args.add(nextid);
		args.add(shortName);
		args.add(name);
		args.add(sql);
		args.add(0);

		Object[] arg2 = args.toArray();
		String sql2 = sb.toString();

		dbUtil.updateAndBringBack(sql2, arg2);

		MessageQueryTO to = new MessageQueryTO();
		to.id = nextid;
		to.shortname = shortName;
		to.name = name;
		to.sql = sql;
		to.count = 0;

		return to;
	}

	// protected void initialImageLoad() {
	//
	//
	// }
	//
	protected void storeIcon(final File icon) {
		TransactionBody tb = new TransactionBody() {

			public void trx() throws SQLException {
				try {
					int nextid = dbUtil.poorMansSequence("icon");
					InputStream is = new FileInputStream(icon);
					ByteArrayOutputStream baos = new ByteArrayOutputStream(
							(int) icon.length());

					int bufSize = (int) 65536;
					byte[] buf = new byte[bufSize];
					int numRead = is.read(buf);
					while (numRead > 0) {
						baos.write(buf, 0, numRead);
						numRead = is.read(buf);
					}
					is.close();
					buf = baos.toByteArray();
					baos = null;

					String sql = "insert into icon (id,name,data) values (?,?,?)";
					String name = icon.getName();
					dbUtil.updateAndBringBack(sql, nextid, name, buf);
					is.close();
				} catch (IOException e) {
					throw new SQLException(e);
				}
			}
		};
		transaction(tb);
	}

	public List<IconTO> searchIcons(final String name) {

		String sql = "select id,name from icon where name like ?";

		final List<IconTO> res = new ArrayList<IconTO>();

		Result
