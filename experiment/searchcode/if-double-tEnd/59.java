package analytics.event.operations;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import Logging.Logger;
import analytics.event.Event;
import analytics.event.StatisticsEvent;
import analytics.event.UserEvent;
import analytics.event.operations.UserEventOperation.UserSession;

public class UserEventOperation {

	private static Logger logger = Logger.getLogger("AnalyticServerRMIObject");

	private static UserEventOperation instance;
	// hashmap to store usersessions and time of the sessions
	// problem: users can have multiple sessions
	private ConcurrentHashMap<String, ArrayList<UserSession>> userSessions = new ConcurrentHashMap<String, ArrayList<UserSession>>();
	private ArrayList<Long> finishedSessionTimes = new ArrayList<Long>();

	private UserEventOperation() {

	}

	public class UserSession {
		public UserSession(String getUsername, Long tStart, Long tEnd,
				boolean finished) {

			this.getUsername = getUsername;
			this.tStart = tStart;
			this.tEnd = tEnd;
			this.finished = finished;
		}

		public String getUsername;
		public Long tStart;
		public Long tEnd;
		public boolean finished = false;
	}

	private boolean eventUserLogin(UserEvent login) {
		ArrayList<UserSession> sessionDurations;

		if (!login.getType().equals(Event.USER_LOGIN))
			return false;
		if (!userSessions.contains(login.getUserName())) {
			sessionDurations = new ArrayList<UserSession>();
			userSessions.put(login.getUserName(), sessionDurations);
		} else {
			sessionDurations = userSessions.get(login.getUserName());
		}
		// add a "0" to indicate the session has started now
		UserSession session = new UserSession(login.getUserName(),
				login.getTimestamp(), (long) 0, false);
		sessionDurations.add(session);
		
		return true;
	}

	private Long eventUserLogout(UserEvent ue) {
		ArrayList<UserSession> sessionDurations;
		int mark = 0;
		System.out.println(mark++);
		if (ue.getType().equals(Event.USER_LOGIN))
			return (long) 0;
		System.out.println(mark++);
		if (!userSessions.contains(ue.getUserName())) {
			System.out.println(mark++);
			return (long) 0;
			
		} else {
			sessionDurations = userSessions.get(ue.getUserName());
		}System.out.println(mark++);
		if (sessionDurations.size() == 0)
			return (long) 0;
		System.out.println(mark++);
		UserSession session = sessionDurations.get(sessionDurations.size() - 1);

		// if unfinished, finish it
		if (session.finished == false) {
			session.tEnd = ue.getTimestamp();
			session.finished = true;
			return (session.tEnd - session.tStart);
		}
		System.out.println(mark++);
		return (long) 0;
	}

	public static UserEventOperation getInstance() {

		if (instance == null)
			instance = new UserEventOperation();
		return instance;
	}

	public ArrayList<Event> execute(Event e) {
		logger.info("Received Event");
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(e);
		if (e instanceof UserEvent) {
			logger.info("Received UserEvent");
			UserEvent event = (UserEvent) e;
			if (e.getType().equals(Event.USER_LOGIN)) {
				this.eventUserLogin(event);
			} else {
				long sessionTime = this.eventUserLogout(event);
				if (sessionTime > 0) {
					 finishedSessionTimes.add(sessionTime); 
					 events.addAll(this.newStatisticsSessionTimeEvents());
				}                    
			}
		} else {
			logger.error("Not Received UserEvent");
		}
		return events;

	}

	private ArrayList<Event> newStatisticsSessionTimeEvents() {
		Long minTime = (long) 0;
		Long maxTime = (long) 0;
		double avgTime = 0;
		
		int sumTime = 0;
		
		for (Long time : finishedSessionTimes) {
			if (time < minTime) minTime = time;
			if (time > maxTime) maxTime = time;
			sumTime += time;
		}
		avgTime = sumTime / finishedSessionTimes.size();
		
		StatisticsEvent sessionMin = new StatisticsEvent();
		StatisticsEvent sessionMax = new StatisticsEvent();
		StatisticsEvent sessionAvg = new StatisticsEvent();
		
		sessionMin.setTimestamp();
		sessionMax.setTimestamp();
		sessionAvg.setTimestamp();
		
		sessionMin.setValue(minTime);
		sessionMax.setValue(maxTime);
		sessionAvg.setValue(avgTime);
		
		ArrayList<Event> events = new ArrayList<Event>();
		
		events.add(sessionMin);
		events.add(sessionMax);
		events.add(sessionAvg);
		
		return events;
	}
}

