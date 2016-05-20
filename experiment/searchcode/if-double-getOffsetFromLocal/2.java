package com.bagatelle.zplanner.estimate;

import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Seconds;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public final class FlotMgr {
	public enum Interval {
	    YEAR ("year", 31556926), 
	    MONTH ("month", 604800*4), 
	    WEEK ("week", 604800),
	    DAY ("day", 86400), 
	    HOUR ("hour", 3600), 
	    MINUTE ("minute", 60), 
	    SECOND ("second", 1);
	    
	    private long seconds;
	    private String desc;
	    
	    public long seconds() { return seconds; }
	    public String desc() { return desc; }
	    
	    Interval(String desc, long seconds) {
	    	this.desc = desc;
	    	this.seconds = seconds;
	    }
	}
	private final static Logger LOG = Logger.getLogger(FlotMgr.class);
	
	private long numIntervals;
	private Interval interval;
	private String json;
	private TreeMap<DateTime, Double> points = new TreeMap<DateTime, Double>();

	public TreeMap<DateTime, Double> getPoints() { return points; }
	public long getNumIntervals() { return numIntervals; }
	public String getInterval() { return interval.desc(); }
	public String getJson() { return json; }
	
	private FlotMgr() {}
	
	public FlotMgr(TreeMap<DateTime, Double> data) {
		points = interpolateDataPoints(data);
		json = generateJson();
		for(DateTime dt : points.keySet()) {
			LOG.debug("Timestamp: " + dt + "Estimate: " + points.get(dt));
		}
	}
	
	public final String generateJson()  {
		StringBuffer jsonString = new StringBuffer();
		jsonString.append("{ label: \"Work Remaining\", data: [");
		
		if(points.keySet().size() > 0) {
			for(DateTime dt : points.keySet()) {
				Double est = points.get(dt);
				if(dt != null && est != null) {
					//flot requires timestamps to be in UTC, so adjust accordingly
					DateTimeZone dtz = DateTimeZone.getDefault();
					int offset = dtz.getOffsetFromLocal(dt.getMillis());
					LOG.debug("Offset is:" + offset);
					jsonString.append("[" + (dt.getMillis() + offset) + "," + est.toString() + "]");
					
					//if another entry remains, append comma
					if(points.higherKey(dt) != null) {
						jsonString.append(",");	
					}
				}
			}
		}
		jsonString.append("] }");
		
		return jsonString.toString();		
	}
	
	private TreeMap<DateTime, Double> interpolateDataPoints(TreeMap<DateTime, Double> data) {
		DateTime start = data.firstKey();
		DateTime end = data.lastKey();
		
		TreeMap<DateTime, Double> result = new TreeMap<DateTime, Double>();		
		for(Entry<DateTime, Double> entry : data.entrySet()) {
			DateTime dt = entry.getKey().withZone(DateTimeZone.forID("UTC"));
			result.put(dt, entry.getValue());
		}
			
		LOG.debug("Start is: " + start + "End is: "+ end);
		interval = determineInterval(start, end);
		LOG.debug("Interval is:" + interval.seconds);
		
		DateTime startPoint = getIntervalStart(start, interval);
		DateTime endPoint = getIntervalEnd(end, interval);
		LOG.debug("Startpoint: " + startPoint + "Endpoint: " + endPoint);
		numIntervals = determineNumIntervals(startPoint, endPoint, interval);
		
		LOG.debug("Num intervals: " + numIntervals);
		long increment = interval.seconds * 1000;
		
		//FIXME: this is kinda lame, but algorithm doesn't work if startpoint is before the first estimate
		//otherwise
		if(startPoint.isBefore(start)) {
			result.put(startPoint, new Double(0));
		} 
		
		for(long i = 0; i < numIntervals; i++) {
			long ts = startPoint.getMillis()+ (increment * i);
			DateTime dt = new DateTime(ts);
			LOG.debug ("Adding datapoint: " + dt);
			//add a datapoint for each day provided our initial data set didn't already cover it
			if(!result.containsKey(dt)){
				result.put(dt, null);
			}
		}

		//this assumes the first item in the list *must* have an estimate and we use this
		Entry<DateTime, Double> first = result.firstEntry();
		Double currEst = first.getValue();
		assert(currEst != null);
		
		//for each datetime, see if it has an estimate
		//if so, keep applying it to each interval until we hit something new
		for(DateTime dt : result.keySet()) {
			Double tmp = result.get(dt);
			if(tmp != null && !tmp.equals(currEst)) {
				currEst = tmp;
			}
			
			result.put(dt, currEst);	
		}
		
		return result;
	}
	
	private DateTime getIntervalStart(DateTime start, Interval interval) {
		DateTime intervalStart = null;
		
		switch(interval) {
			case YEAR:
				intervalStart = new DateTime(start.getYear(), 0, 0, 0, 0, 0, 0);
				break;
			case MONTH:
				intervalStart = new DateTime(start.getYear(), start.getMonthOfYear(), 0, 0, 0, 0, 0);
				break;
			/*case WEEK:
				intervalStart = new DateTime(start.getYear(), start.getMonthOfYear(), 0, 0, 0, 0);
				break;*/
			case DAY:
				intervalStart = new DateTime(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), 0, 0, 0, 0);		
				break;
			case HOUR:
				intervalStart = new DateTime(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), start.getHourOfDay(), 0, 0, 0);						
				break;
			case MINUTE:
				intervalStart = new DateTime(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), start.getHourOfDay(), start.getMinuteOfHour(), 0, 0);		
				break;
			case SECOND:
				intervalStart = new DateTime(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute(), 0);		
				break;
			default:
				intervalStart = new DateTime(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth(), start.getHourOfDay(), start.getMinuteOfHour(), start.getSecondOfMinute(), start.getMillisOfSecond());		
				break;
		}
		
		return intervalStart;
	}
	
	private DateTime getIntervalEnd(DateTime end, Interval interval) {
		DateTime intervalEnd = null;
		
		switch(interval) {
			case YEAR:
				intervalEnd = new DateTime(end.getYear(), 0, 0, 0, 0, 0, 0).plusYears(1);
				break;
			case MONTH:
				intervalEnd = new DateTime(end.getYear(), end.getMonthOfYear(), 0, 0, 0, 0, 0).plusMonths(1);
				break;
			/*case WEEK:
				intervalStart = new DateTime(start.getYear(), start.getMonthOfYear(), 0, 0, 0, 0);
				break;*/
			case DAY:
				intervalEnd = new DateTime(end.getYear(), end.getMonthOfYear(), 
						end.getDayOfMonth(), 0, 0, 0, 0).plusDays(1);		
				break;
			case HOUR:
				intervalEnd = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(), 
						end.getHourOfDay(), 0, 0, 0).plusHours(1);						
				break;
			case MINUTE:
				intervalEnd = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(), 
						end.getHourOfDay(), end.getMinuteOfHour(), 0, 0).plusMinutes(1);
				break;
			case SECOND:
				intervalEnd = new DateTime(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth(), 
						end.getHourOfDay(), end.getMinuteOfHour(), end.getSecondOfMinute(), 0).plusSeconds(1);
				break;
			default:
				intervalEnd = end;		
				break;
		}
		
		return intervalEnd;
	}
	
	private Interval determineInterval(DateTime start, DateTime end) {
		Interval interval = null;	
		//MutablePeriod graphPeriod = new MutablePeriod(start, end);
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyy-MM-dd HH:mm:ss");
		//System.out.println("Start: " + start.toString(formatter) + "End: " + end.toString(formatter));
		
		int secondsBetween = Seconds.secondsBetween(start, end).getSeconds();
		LOG.debug("Seconds between:" + secondsBetween);
		
		if(secondsBetween >= Interval.YEAR.seconds * 3) {
			interval = Interval.YEAR;
		} else if (secondsBetween >= Interval.MONTH.seconds * 3) {
			interval = Interval.MONTH;
		/*} else if (secondsBetween >= Interval.WEEK.seconds * 3) {
			interval = Interval.WEEK;*/
		} else if (secondsBetween >= Interval.DAY.seconds * 3) {
			interval = Interval.DAY;
		} else if (secondsBetween >= Interval.HOUR.seconds * 3) {
			interval = Interval.HOUR;
		} else if (secondsBetween >= Interval.MINUTE.seconds * 3) {
			interval = Interval.MINUTE;
		} else {
			interval = Interval.SECOND;
		}
		LOG.debug("Interval is: " + interval.desc);
		return interval;
	}
	
	private long determineNumIntervals(DateTime start, DateTime end, Interval interval) {
		long numIntervals = 0;
		//MutablePeriod graphPeriod = new MutablePeriod(start, end);
		
		switch(interval) {
			case YEAR:
				numIntervals = Years.yearsBetween(start, end).getYears();
				break;
			case MONTH:
				numIntervals = Months.monthsBetween(start, end).getMonths();
				break;
			/*case WEEK:
				numIntervals = Weeks.weeksBetween(start, end).getWeeks();
				break;*/
			case DAY:
				numIntervals = Days.daysBetween(start, end).getDays();
				break;
			case HOUR:
				numIntervals = Hours.hoursBetween(start, end).getHours();
				break;
			case MINUTE:
				numIntervals = Minutes.minutesBetween(start, end).getMinutes();
				break;
			case SECOND:
				numIntervals = Seconds.secondsBetween(start, end).getSeconds();
				break;
			default:
				numIntervals = Seconds.secondsBetween(start, end).getSeconds();
				break;
		}
		
		return numIntervals;
	}


}
