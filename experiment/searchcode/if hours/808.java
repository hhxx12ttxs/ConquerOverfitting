package ba.bitcamp.medina.banjic;

public class Airplane {

	public static void main(String[] args) {
		int hoursLaunch = 20;
		int minutesLaunch = 43;

		int hoursLands = 0;
		int minutesLands = 0;

		int minutesFlight = 516;

		int hours = 0;

		int minutes = minutesLaunch + minutesFlight;

		// I declared variables for time of airplane launch, time of landing and duration of flight
		// Sum of minutes, if bigger than 60 it will be divided by 60 and we will get hours of flight
		// minutes of flight are a modul of sum of minutes 
		// if hours we get is bigger than 24 then we subtract hours and 24 and that is how we get real hours
		
		
		if (minutes > 60) {

			hours = minutes / 60;

		}

		minutesLands = minutes % 60;
		hoursLands = hoursLaunch + hours;

		if (hoursLands > 24) {

			hoursLands -= 24;
		}

		System.out.println(hoursLands + ":" + minutesLands);

	}

}

