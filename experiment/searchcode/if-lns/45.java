package com.agendary.sync;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import lotus.domino.*;

public class LotusReader {

	public static final int DAYS_PAST = -7;
	public static final int DAYS_FUTURE = +14;
	
	private Settings mSettings;
	private DateFormat dateFormat;
	private Date startDate;
	private Date endDate;
	private Database db;

	public LotusReader(Settings settings) {
		mSettings = settings;
	}

	public ArrayList<LotusNotesCalendarEntry> getCalendarEntries() throws Exception {

		boolean notesThreadInitialized = false;
		LotusNotesSettings lns = new LotusNotesSettings();

		try {
			NotesThread.sinitThread();
			notesThreadInitialized = true;

			String password = mSettings.getLotusPassword();
			String server = mSettings.getLotusServer();
			String dbPath = mSettings.getLotusMailFile();

			Session session = NotesFactory.createSession((String)null, (String)null, password);
			db = session.getDatabase(server, dbPath, false);

			String strDateFormat = getLotusServerDateFormat(session);
			dateFormat = new SimpleDateFormat(strDateFormat);

			Calendar now = Calendar.getInstance();
			now.add(Calendar.DATE, DAYS_PAST);
			startDate = now.getTime();
			now.add(Calendar.DATE, DAYS_FUTURE);
			endDate = now.getTime();

			String calendarQuery = "SELECT (@IsAvailable(CalendarDateTime) & (@Explode(CalendarDateTime) *= @Explode(@TextToTime(\"" +
			dateFormat.format(startDate) + " - " + dateFormat.format(endDate) + "\"))))";
			DocumentCollection queryResults = db.search(calendarQuery);
			return getCalendarEntryList(queryResults);

		} catch (Exception ex) {
			throw ex;
		} finally {
			// If true, the NotesThread failed to init. The LN dlls probably weren't found.
			// NOTE: Make sure this check is the first line in the finally block. When the
			// init fails, some of the finally block may get skipped.
			if (!notesThreadInitialized) {
				throw new Exception("There was a problem initializing the Lotus Notes thread!");
			}
			NotesThread.stermThread();
		}
	}


	/**
	 * Return the date format used on the Domino server.
	 */
	protected String getLotusServerDateFormat(Session session) throws NotesException {
		// Get our start and end query dates in Lotus Notes format. We will query
		// using the localized format for the dates (which is what Lotus expects).
		// E.g. in England the date may be 31/1/2011, but in the US it is 1/31/2011.
		String strDateFormat;
		// Get the date separator used on the Domino server, e.g. / or -
		String dateSep = session.getInternational().getDateSep();

		// Determine if the server date format is DMY, YMD, or MDY
		if (session.getInternational().isDateDMY()) {
			strDateFormat = "dd" + dateSep + "MM" + dateSep + "yyyy";                
		}
		else if (session.getInternational().isDateYMD()) {
			strDateFormat = "yyyy" + dateSep + "MM" + dateSep + "dd";
		}
		else {
			strDateFormat = "MM" + dateSep + "dd" + dateSep + "yyyy";
		}

		return strDateFormat;
	}

	/**
	 * Process a list of Lotus Notes entries returned from a query.
	 * Return a list of LotusNotesCalendarEntry objects.
	 */
	protected ArrayList<LotusNotesCalendarEntry> getCalendarEntryList(DocumentCollection queryResults) throws Exception {
		boolean addDoc;
		ArrayList<LotusNotesCalendarEntry> calendarEntries = new ArrayList<LotusNotesCalendarEntry>();
		LotusNotesCalendarEntry cal;

		Document doc;
		doc = queryResults.getFirstDocument();
		// Loop through all entries returned
		while (doc != null)
		{
			Item lnItem;
			addDoc = true;

			cal = new LotusNotesCalendarEntry();

			lnItem = doc.getFirstItem("Subject");
			if (!isItemEmpty(lnItem))
				cal.setSubject(lnItem.getText());
			else
				cal.setSubject("<no subject>");

			lnItem = doc.getFirstItem("Body");
			if (!isItemEmpty(lnItem))
				cal.setBody(lnItem.getText());

			// Get the type of Lotus calendar entry
			lnItem = doc.getFirstItem("Form");
			if (!isItemEmpty(lnItem))
				cal.setEntryType(lnItem.getText());
			else
				// Assume we have an appointment
				cal.setEntryType(LotusNotesCalendarEntry.EntryType.APPOINTMENT);

			if (cal.getEntryType() == LotusNotesCalendarEntry.EntryType.APPOINTMENT)
			{
				lnItem = doc.getFirstItem("AppointmentType");
				if (!isItemEmpty(lnItem))
					cal.setAppointmentType(lnItem.getText());
			}

			lnItem = doc.getFirstItem("Room");
			if (!isItemEmpty(lnItem))
				cal.setRoom(lnItem.getText());
			lnItem = doc.getFirstItem("Location");
			if (!isItemEmpty(lnItem))
				cal.setLocation(lnItem.getText());

			lnItem = doc.getFirstItem("$Alarm");
			if (!isItemEmpty(lnItem)) {
				cal.setAlarm(true);
				lnItem = doc.getFirstItem("$AlarmOffset");
				if (!isItemEmpty(lnItem))
					cal.setAlarmOffsetMins(Integer.parseInt(lnItem.getText()));
			}

			// When the Mark Private checkbox is checked, OrgConfidential is set to 1
			lnItem = doc.getFirstItem("OrgConfidential");
			if (!isItemEmpty(lnItem)) {
				if (lnItem.getText().equals("1"))
					cal.setPrivate(true);
			}

			//Get attendee info
			lnItem = doc.getFirstItem("REQUIREDATTENDEES");
			if (!isItemEmpty(lnItem)){
				cal.setRequiredAttendees(lnItem.getText());
			}
			lnItem = doc.getFirstItem("OPTIONALATTENDEES");
			if (!isItemEmpty(lnItem)){
				cal.setOptionalAttendees(lnItem.getText());
			}
			lnItem = doc.getFirstItem("CHAIR");
			if (!isItemEmpty(lnItem)){
				cal.setChairperson(lnItem.getText());
			}

			lnItem = doc.getFirstItem("APPTUNID");
			if (!isItemEmpty(lnItem)){
				// If the APPTUNID contains a URL (http or https), then the entry
				// isn't a standard Lotus Notes item. It is a link to an external calendar.
				// In this case, we want to ignore the entry.
				if (lnItem.getText().matches("(?i).*(https?|Notes):.*")) {
					addDoc = false;
				}
			}

			cal.setModifiedDateTime(doc.getLastModified().toJavaDate());

			lnItem = doc.getFirstItem("OrgRepeat");

			if (addDoc) {
				// If true, this is a repeating calendar entry
				if (!isItemEmpty(lnItem))
				{
					// Handle Lotus Notes repeating entries by creating multiple Google
					// entries

					Vector<?> startDates = null;
					Vector<?> endDates = null;

					lnItem = doc.getFirstItem("StartDateTime");
					if (!isItemEmpty(lnItem))
						startDates = lnItem.getValueDateTimeArray();

					lnItem = doc.getFirstItem("EndDateTime");
					if (!isItemEmpty(lnItem))
						endDates = lnItem.getValueDateTimeArray();

					if (startDates != null)
					{
						for (int i = 0; i < startDates.size(); i++) {
							if (startDates.get(i) instanceof DateTime) {
								DateTime notesDate = (DateTime)startDates.get(i);
								Date javaDate = notesDate.toJavaDate();

								// Only add the entry if it is within our sync date range
								if (isDateInRange(javaDate))
								{
									// We are creating multiple entries from one repeating entry.
									// We use the same Lotus UID for all entries because we will
									// prepend another GUID before inserting into Google.
									cal.setUID(doc.getUniversalID());

									cal.setStartDateTime(javaDate);

									if (endDates != null) {
										notesDate = (DateTime)endDates.get(i);
										cal.setEndDateTime(notesDate.toJavaDate());
									}

									calendarEntries.add(cal.clone());
								}
							}
						}
					}
				}
				else
				{
					cal.setUID(doc.getUniversalID());

					lnItem = doc.getFirstItem("StartDateTime");
					if (!isItemEmpty(lnItem))
						cal.setStartDateTime(lnItem.getDateTimeValue().toJavaDate());

					// For To Do tasks, the EndDateTime doesn't exist, but there is an EndDate value
					lnItem = doc.getFirstItem("EndDateTime");
					if (isItemEmpty(lnItem))
						lnItem = doc.getFirstItem("EndDate");
					if (!isItemEmpty(lnItem))
						cal.setEndDateTime(lnItem.getDateTimeValue().toJavaDate());

					// Only add the entry if it is within our sync date range
					if (isDateInRange(cal.getStartDateTime()))
						calendarEntries.add(cal);
				}
			}

			doc = queryResults.getNextDocument();
		}

		return calendarEntries;
	}


	/**
	 * Determine if the calendar entry date is in the range of dates to be processed.
	 * @param entryDate - The calendar date to inspect.
	 * @return True if the date is in the range, false otherwise.
	 */
	public boolean isDateInRange(Date entryDate) {
		if (entryDate != null && entryDate.compareTo(startDate) >= 0 && entryDate.compareTo(endDate) <= 0)
			return true;

		return false;
	}


	/**
	 * Returns true if the Lotus Notes Item object is empty or null.
	 * @param lnItem The object to inspect.
	 */
	protected boolean isItemEmpty(Item lnItem) {
		try {
			// Lotus Notes Item objects are usually read by name, e.g. lnItem = doc.getFirstItem("Subject").
			// If the name doesn't exist at all then null is returned.
			// If the name does exist, but doesn't have a value, then lnItem.getText() returns "".
			// Check for both conditions.
			if (lnItem == null || (lnItem != null && lnItem.getText().isEmpty()))
				return true;            
		} catch (Exception ex) {
			// An error means we couldn't read the Item, so consider it empty
			return true;
		}

		return false;
	}

	/**
	 * Try to detect some Lotus Notes settings and return them.
	 */
	public LotusNotesSettings detectLotusSettings(String lnPassword) throws Exception {
		boolean notesThreadInitialized = false;
		LotusNotesSettings lns = new LotusNotesSettings();

		try {
			NotesThread.sinitThread();
			notesThreadInitialized = true;

			Session session = NotesFactory.createSession((String)null, (String)null, lnPassword);

			lns.setMailFile(session.getEnvironmentString("MailFile", true));

			// The mail server will probably have the format "CN=MY-LN-SERVER/OU=SRV/O=AcmeCo".
			// Get only the value after "CN=" and before the first "/".
			String mailServer = session.getEnvironmentString("MailServer", true);
			int i = mailServer.indexOf("/");
			if (i > 0 && mailServer.substring(0, 3).equals("CN=")) {
				mailServer = mailServer.substring(3, i);
			}
			lns.setServerName(mailServer);

			lns.hasLocalServer = true;
			// Try to connect to the local Domino server.
			Database db = session.getDatabase(null, lns.getMailFile(), false);
			if (db == null)
				lns.hasLocalServer = false;

		} catch (Exception ex) {
			throw ex;
		} finally {
			// If true, the NotesThread failed to init. The LN dlls probably weren't found.
			// NOTE: Make sure this check is the first line in the finally block. When the
			// init fails, some of the finally block may get skipped.
			if (!notesThreadInitialized) {
				throw new Exception("There was a problem initializing the Lotus Notes thread!");
			}
			NotesThread.stermThread();
		}

		return lns;
	}
	
	public boolean checkPassword(String lnPassword) throws Exception {
		boolean notesThreadInitialized = false;
		boolean passwordOk = false;
		LotusNotesSettings lns = new LotusNotesSettings();

		try {
			NotesThread.sinitThread();
			notesThreadInitialized = true;
			NotesFactory.createSession((String)null, (String)null, lnPassword);
			passwordOk = true;
		} catch (Exception ex) {
		} finally {
			// If true, the NotesThread failed to init. The LN dlls probably weren't found.
			// NOTE: Make sure this check is the first line in the finally block. When the
			// init fails, some of the finally block may get skipped.
			if (!notesThreadInitialized) {
				throw new Exception("There was a problem initializing the Lotus Notes thread!");
			}
			NotesThread.stermThread();
		}

		return passwordOk;
	}

	public class LotusNotesSettings {
		private String mailFile;
		private String serverName;
		public boolean hasLocalServer;

		public String getMailFile() {
			return mailFile;
		}

		public void setMailFile(String mailFile) {
			this.mailFile = mailFile;
		}

		public String getServerName() {
			return serverName;
		}

		public void setServerName(String serverName) {
			this.serverName = serverName;
		}
	}

}

