/*
David Hann
Exam 1
*/
public class BreakTime
{
	//default constants
	private static final int DEFAULT_WEEKS=0;
	private static final int DEFAULT_DAYS=0;
	private static final int DEFAULT_HOURS=0;
	//constants
	private static final int DAYS_IN_WEEK=7;
	private static final int HOURS_IN_DAY=24;
	//weeks, days and hours until break
	private int weeks;
	private int days;
	private int hours;
	//default constructor
	public BreakTime()
	{
		setWeeks(DEFAULT_WEEKS);
		setDays(DEFAULT_DAYS);
		setHours(DEFAULT_HOURS);
	}
	//non-default constructor
	public BreakTime(int ww, int dd, int hh)
	{
		setWeeks(ww);
		setDays(dd);
		setHours(hh);
	}
	//set* methods
	public void setWeeks(int ww)
	{
		weeks=ww>=0?ww:DEFAULT_WEEKS;
	}
	public void setDays(int dd)
	{
		days=(dd>=0 && dd<DAYS_IN_WEEK)?dd:DEFAULT_DAYS;
/*		while (days>=DAYS_IN_WEEK)
		{
			days=days-DAYS_IN_WEEK;
			weeks++;
		}
*/	}
	public void setHours(int hh)
	{
		hours=(hh>=0 && hh<HOURS_IN_DAY)?hh:DEFAULT_HOURS;
/*		while (hours>=HOURS_IN_DAY)
		{
			hours=hours-HOURS_IN_DAY;
			days++;
		}
*/	}
	//get* methods
	public int getWeeks()
	{
		return weeks;
	}
	public int getDays()
	{
		return days;
	}
	public int getHours()
	{
		return hours;
	}
	//totalWeeks returns the total time in real number weeks
	public double totalWeeks()
	{
//		System.out.println(""+(double)hours/(double)HOURS_IN_DAY);
		return weeks+((days+hours/(double)HOURS_IN_DAY)/(double)DAYS_IN_WEEK);
	}
	//totalDays returns the total time in real number days
	public double totalDays()
	{
		return weeks*DAYS_IN_WEEK+days+hours/(double)HOURS_IN_DAY;
	}
	//totalHours returns the total time in real number hours
	public double totalHours()
	{
		return (weeks*DAYS_IN_WEEK+days)*HOURS_IN_DAY+hours;
	}
	//compareTo method
	public int compareTo(BreakTime x)
	{
		double currentTimeLeft=totalHours();
		double xTimeLeft=x.totalHours();
		if (currentTimeLeft>xTimeLeft)
		{
			return 1;
		}
		else if (currentTimeLeft<xTimeLeft)
		{
			return -1;
		}
		else return 0;
	}
	//toString method
	public String toString()
	{
		return weeks+" weeks, "+days+" days, and "+hours+" until summer vacation";
	}
}

