package ua.lviv.lgs04a.lesson03.loops.Unit_15b;

public class Main {

	public static void main(String[] args) {
		// 15. Электронные часы показывают время в формате от 00:00 до 23:59.
		// Подсчитать сколько раз за сутки случается так, что слева от двоеточия
		// показывается симметричная комбинация для той, что справа от двоеточия
		// (например, 02:20, 11:11 или 15:51).
		int mirrorTimes = 0;
		int[] minsInt = new int[2];
		int[] hoursInt = new int[2];
		for (int mins = 0; mins <= 59; mins++) {
			for (int hours = 0; hours <= 23; hours++) {
				if (mins < 10) {
					minsInt[0] = 0;
					minsInt[1] = mins;
				} else {
					minsInt[0] = (mins - mins % 10) / 10;
					minsInt[1] = mins % 10;
				}
				if (hours < 10) {
					hoursInt[0] = 0;
					hoursInt[1] = hours;
				} else {
					hoursInt[0] = (hours - hours % 10) / 10;
					hoursInt[1] = hours % 10;
				}
				if (minsInt[0] == hoursInt[1] && minsInt[1] == hoursInt[0]) {
					mirrorTimes += 1;
				}
			}
		}
		System.out.println("The clock as mirror is " + mirrorTimes + " times for 24 hours.");
	}
}
