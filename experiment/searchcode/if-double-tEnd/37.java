package analytics.event.operations;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import Logging.Logger;
import analytics.event.AuctionEvent;
import analytics.event.BidEvent;
import analytics.event.Event;
import analytics.event.StatisticsEvent;
import analytics.event.UserEvent;

public class EventOperation {

	private static EventOperation instance;
	private int maxBidValue = 0;
	private int bidCounter = 0;
	
	private int auctionEnded = 0;
	private int bidWonCounter = 0;
	private long timeStart = (new Date()).getTime();
	private ConcurrentHashMap<String, ArrayList<Session>> sessions = new ConcurrentHashMap<String, ArrayList<Session>>();
	private ArrayList<Tupel> finishedSessionTimes = new ArrayList<Tupel>();

	private static Logger logger = Logger.getLogger("EventOperation");

	private EventOperation() {
	}

	public class Tupel {
		public Tupel(long value, boolean usersession) {
			this.value = value;
			this.usersession = usersession;
		}

		public long value;
		public boolean usersession;
	}

	public class Session {
		public Session(String sessionName, Long tStart, Long tEnd,
				boolean finished) {

			this.sessionName = sessionName;
			this.tStart = tStart;
			this.tEnd = tEnd;
			this.finished = finished;
		}

		public String sessionName;
		public Long tStart;
		public Long tEnd;
		public boolean finished = false;
	}

	public static EventOperation getInstance() {

		if (instance == null)
			instance = new EventOperation();
		return instance;
	}

	public ArrayList<Event> execute(Event e) {
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(e);

		if (e instanceof BidEvent) {
			
			BidEvent event = (BidEvent) e;
			if (event.getType().equals(Event.BID_PLACED)) this.bidCounter++;
			if (event.getType().equals(Event.BID_WON)) this.bidWonCounter++;
			if (event.getPrice() > maxBidValue) {
				maxBidValue = (int) event.getPrice();
				StatisticsEvent se2 = new StatisticsEvent();
				se2.setTimestamp();
				se2.setType(Event.BID_PRICE_MAX);
				se2.setValue(maxBidValue);
				events.add(se2);
			}
			StatisticsEvent se3 = new StatisticsEvent();
			se3.setTimestamp();
			se3.setType(Event.BID_COUNT_PER_MINUTE);
			double seconds = (new Date().getTime() - this.timeStart) / 1000;
			//System.out.println("seconds "+seconds);
			//System.out.println("this.bidCounter "+this.bidCounter);
			if (seconds > 0)
				se3.setValue(this.bidCounter / (seconds / 60));
			else {
				se3.setValue(Double.POSITIVE_INFINITY);
			}
			events.add(se3);
		} else if ((e instanceof UserEvent) || (e instanceof AuctionEvent)) {

			
			// UserEvent event = (UserEvent) e;
			if (e.getType().equals(Event.USER_LOGIN)
					|| e.getType().equals(Event.AUCTION_STARTED)) {
				this.eventSessionLogin(e);
			} else {
				long sessionTime = this.eventSessionLogout(e);
				logger.info("sessionTime is " + sessionTime);
				if (sessionTime > 0) {
					finishedSessionTimes.add(new Tupel(sessionTime,
							(e instanceof UserEvent)));
					if (e instanceof UserEvent) {
						//System.out.println("users");
						events.addAll(this.newUserStatisticsSessionTimeEvents());
					} else {
						//if (e.getType().equals(Event.AUCTION_ENDED)) {
							this.auctionEnded++;
						//}
						//System.out.println("auctions");
						events.addAll(this
								.newAuctionStatisticsSessionTimeEvents());
					}
				}
			}
		} else {
			logger.error("Not Received UserEvent");
		}
		//System.out.println("events "+events.size());
		return events;
	}

	private boolean eventSessionLogin(Event login) {
		ArrayList<Session> sessionDurations;

		if (!login.getType().equals(Event.USER_LOGIN)
				&& !login.getType().equals(Event.AUCTION_STARTED))
			return false;
		String id = "";
		if (login instanceof UserEvent) {
			id = ((UserEvent) login).getUserName();
		} else if (login instanceof AuctionEvent) {
			id = "auction" + ((AuctionEvent) login).getAuctionID();
		}

		//System.out.println(id);
		if (!sessions.containsKey(id)) {
			sessionDurations = new ArrayList<Session>();
			sessions.put(id, sessionDurations);
		} else {
			sessionDurations = sessions.get(id);
		}

		// add a "0" to indicate the session has started now
		Session session = new Session(id, login.getTimestamp(), (long) 0, false);
		sessionDurations.add(session);

		// logger.info(sessionDurations.toString());
		return true;
	}

	private Long eventSessionLogout(Event event) {
		ArrayList<Session> sessionDurations;
		int mark = 0;

		if (event.getType().equals(Event.USER_LOGIN))
			return (long) 0;
		
		String id = "";
		if (event instanceof UserEvent) {
			id = ((UserEvent) event).getUserName();
		} else if (event instanceof AuctionEvent) {
			id = "auction" + ((AuctionEvent) event).getAuctionID();
		}

		//System.out.println(id);
		if (!sessions.containsKey(id)) {
			//System.out.println(mark++);
			logger.error("A session ended, which was not found as active");
			return (long) 0;

		} else {
			sessionDurations = sessions.get(id);
		}
		
		if (sessionDurations.size() == 0)
			return (long) 0;
		
		Session session = sessionDurations.get(sessionDurations.size() - 1);

		// if unfinished, finish it
		if (session.finished == false) {
			session.tEnd = event.getTimestamp();
			session.finished = true;
			logger.info("actually works");
			return (session.tEnd - session.tStart);
		}
		
		return (long) 0;
	}

	private ArrayList<Event> newUserStatisticsSessionTimeEvents() {
		double minTime = Long.MAX_VALUE;
		double maxTime = Long.MIN_VALUE;
		double avgTime = 0;
		int counter = 0;
		int sumTime = 0;

		//System.out.println("size " + finishedSessionTimes.size());
		for (Tupel tupel : finishedSessionTimes) {
			//System.out					.println("tupel " + tupel.value + " " + tupel.usersession);
			if (tupel.usersession) {// return new ArrayList<Event>();
				long time = tupel.value;
				if (time < minTime)
					minTime = time;
				if (time > maxTime)
					maxTime = time;
				sumTime += time;
				counter++;
			}
		}
		if (counter == 0) {
			return new ArrayList<Event>();
		}
		avgTime = sumTime / counter;

		//System.out.println("avgTime " + avgTime);
		StatisticsEvent sessionMin = new StatisticsEvent();
		StatisticsEvent sessionMax = new StatisticsEvent();
		StatisticsEvent sessionAvg = new StatisticsEvent();

		sessionMin.setTimestamp();
		sessionMax.setTimestamp();
		sessionAvg.setTimestamp();

		sessionMin.setValue(minTime/1000);
		sessionMax.setValue(maxTime/1000);
		sessionAvg.setValue(avgTime/1000);

		sessionMin.setType(Event.USER_SESSIONTIME_MIN);
		sessionMax.setType(Event.USER_SESSIONTIME_MAX);
		sessionAvg.setType(Event.USER_SESSIONTIME_AVG);

		ArrayList<Event> events = new ArrayList<Event>();

		events.add(sessionMin);
		events.add(sessionMax);
		events.add(sessionAvg);
		
		return events;
	}

	private ArrayList<Event> newAuctionStatisticsSessionTimeEvents() {
		double avgTime = 0;
		int sumTime = 0;
		int counter = 0;

		for (Tupel tupel : finishedSessionTimes) {
			if (!tupel.usersession) {
				long time = tupel.value;
				counter++;
				sumTime += time;
				//System.out.println("sesionTimes:" + time);
			}
		}
		if (counter == 0) {
			return new ArrayList<Event>();
		}

		avgTime = sumTime / counter;
		StatisticsEvent sessionAvg = new StatisticsEvent();
		sessionAvg.setTimestamp();
		sessionAvg.setValue((double)avgTime / 1000);
		sessionAvg.setType(Event.AUCTION_TIME_AVG);
		
		StatisticsEvent successRatio = new StatisticsEvent();
		successRatio.setTimestamp();
		successRatio.setValue((double)this.bidWonCounter / (double)this.auctionEnded);
		successRatio.setType(Event.AUCTION_SUCCESS_RATIO);
		
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(sessionAvg);
		events.add(successRatio);

		return events;
	}
}

