//tdupreetan: creates a class time, and supplies all the methods to print time in various formats

public class Time{

//fields
private int hours;
private int minutes;

//constructors
public Time(){
	hours=0;
	minutes=0;
	}

//contructor that takes int inputs for hours and minutes 
public Time(int h, int m){
	//if minutes entered exceed 59, excess is carried over to hours 
	if (m>=60){
		int hFromM=m/60;
		h=h+hFromM;
		m=m%60;
		}
		
	hours=h;
	minutes=m;
	
	}

//constructor that take a string input and parses it into hours and minutes
public Time (String s){
	int colon=s.indexOf(':');
	int h=Integer.parseInt(s.substring(0, colon));
	int m=Integer.parseInt(s.substring(colon+1));
	
	//if minutes entered exceed 59, excess is carried over to hours 
	if (m>=60){
		int hFromM=m/60;
		h=h+hFromM;
		m=m%60;
		}
	
	hours=h;
	minutes=m;
	
	}
//method that returns a string representation of time in 24 hr format
public String toString(){
	//if hours exceed 24, excess is removed
	if (hours>24){
		hours=hours%24;
		}
	//formats minutes 
	String minutestr=minutes+"";	
	if (minutes==0){
		minutestr="00";
		}
	//formats minues
	if (minutes<10){
		minutestr='0'+minutestr;
		}
	//formats hours
	String hourstr=hours+"";
	if (hours==0){
		hourstr="00";
		}
	//formats hours
	if (hours<10){
		hourstr="0"+hourstr;
		}
	
	String timestr= (hourstr+':'+minutestr);
	
	//prints time in 24 hr format
	return timestr;
	
	}

//method that returns int minutes
public int getMinutes(){
	return this.minutes;
	}
//method that returns int hours
public int getHours(){
	return this.hours;
	}

//method that returns a string representation of time, in am-pm format
public String amPm(){
	//am-pm is set to default to am
	String indic= new String("AM");
	
	//if hours are greater than 24, excess is removed
	if(hours>24){
		hours=hours%24;
		}
	//if hours are greater than 12, twelse hours are subtracted and am-pm is set to pm
	else if (12<hours){
		hours=hours-12;
		indic="PM";
		}
	//formats minutes
	String min=minutes+"";	
		if (minutes==0){
		min="00";
		}
	//formats minutes
	else if(minutes<10){
		min='0'+min;
		}
	
	//formats hours
	String hr= hours+"";
		if (hours==0){
		hr="00";
		}
	
	String time=hr+':'+min+indic;
	//prints time in 12 hr format
	return time;
	
	}

}
	
