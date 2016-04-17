package ba.bitcamp.medina.banjic;

public class Period {

	public static void main(String[] args) {

		// This is a method which calculates how many milliseconds passed since
		// 1.1.1970
		// It is in UTC time zone

		long time = System.currentTimeMillis();

		// We divide this value to get the number of hours passed
		long hours = time / 3600000;

		// Now we add 2 to a module of hours to get hours in our zone UTC+2
		long hoursNow = (hours % 24) + 2;

		// IF statements below compared hours to see which period of day is now
		if (hoursNow >= 6 && hoursNow <= 10) {
			System.out.println("Morning");
		}

		else if (hoursNow >= 10 && hoursNow <= 18) {
			System.out.println("Day");
		}

		else if (hoursNow >= 18 && hoursNow <= 22) {
			System.out.println("Evening");
		}

		else if (hoursNow >= 22 && hoursNow <= 6) {
			System.out.println("Night");
		}

	}

}

