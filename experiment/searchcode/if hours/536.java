package Core;

import java.util.Timer;

public class Timemgmt{
	
	/*
	 * CORE GAME FILE, DO NOT EDIT!
	 * 
	 * 
	 * Timemgmt by PSCA119
	 * 
	 * // PRE-GENERATED SYSTEM CRITICAL -- DO NOT EDIT --
	 * 
	 * Source code provided below.
	 */
	
	private static String _seconds = "0";
	private static String _minutes = "0";
	private static String _hours = "0";
	public static int seconds = 0;
	private static int minutes = 0;
	private static int hours = 0;

	public static void init(){
		Timer timer = new Timer();
		timer.schedule(new TimeCall(), 0, 1000);
	}
	
public static String get(){
		
		if(seconds >= 60){
			seconds = 0;
			minutes = minutes + 1;
		}
		if(minutes >= 60){
			minutes = 0;
			hours = hours + 1;
		}
		
		if(seconds <= 9){
			_seconds = "0" + seconds;
		} else {
			_seconds = "" + seconds;
		}
		if(minutes <= 9){
			_minutes = "0" + minutes;
		} else {
			_minutes = "" + minutes;
		}
		if(hours <= 9){
			_hours = "0" + hours;
		} else {
			_hours = "" + hours;
		}
		
		return _hours + ":" + _minutes + ":" + _seconds;
	}
}

