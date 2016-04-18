package com.example.helsdas;

//import java.io.IOException;
import java.util.Date;

public class Count {
	
	public static long count(Date startTime, Date endTime) {
		long Time = endTime.getTime() - startTime.getTime();
		return Time;
	}
	
	public static String getTimeinHoursandMinuts(long Time) {
		
		long hours = Time/1000/60/60;
		long minuts = (Time/1000/60 - hours*60);
		
		if (minuts > 0 && minuts < 2) return hoursPadezh(hours) + minuts + " ������.";
		if (minuts > 1 && minuts < 5) return hoursPadezh(hours) + minuts + " ������.";
		else 			return hoursPadezh(hours) + minuts + " �����.";
	}
	
	static String hoursPadezh(long hours) {
		if (hours < 1) return "��� ������ ";
		if (hours < 5) return "��� ������ " + hours + " ���� � ";
		else return "��� ������ " + hours + " ����� � ";
	}
	
	public static CharSequence doIt(Date startTime, Date endTime) {
		return(getTimeinHoursandMinuts(count(startTime, endTime)));
	}

}


