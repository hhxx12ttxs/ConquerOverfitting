package mains;
import java.util.Scanner;
public class Clock {
	public static Scanner sc = new Scanner(System.in);

	int hours, minutes, seconds;

	public Clock(int horas, int minutos, int segundos){
		hours = horas;
		minutes = minutos;
		seconds = segundos;
	}
	public int GetHours(){
		return hours;
	}
	public int GetMinutes(){
		return minutes;
	}
	public int GetSeconds(){
		return seconds;
	}
	public void correctTime(){
		if(seconds>59){
			int newSeconds = seconds % 60;
			int moreMinutes = minutes / 60;
			minutes = minutes + moreMinutes;
			seconds = newSeconds;
		}
		if(minutes>59){
			int newMinutes = minutes % 60;
			int moreHours = minutes / 60;
			hours = hours + moreHours;
			minutes = newMinutes;}
		if(hours>23){
			hours = hours%24;
		}
	}
}

