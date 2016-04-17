package assignments.chap7;

public class Pe74 {

	public static void main(String[] args) {
		int[][] dayHours = { { 2, 4, 3, 4, 5, 8, 8 }, { 7, 3, 4, 3, 3, 4, 4 },
				{ 3, 3, 4, 3, 3, 2, 2 }, { 9, 3, 4, 7, 3, 4, 1 },
				{ 3, 5, 4, 3, 6, 3, 8 }, { 3, 4, 4, 6, 3, 4, 4 },
				{ 3, 7, 4, 8, 3, 8, 4 }, { 6, 3, 5, 9, 2, 7, 9 } };
		double[] weeklyHours = sumOfHours(dayHours);
		int[] index = sortWeeklyHours(weeklyHours);
		format(dayHours, index);

	}

	public static double[] sumOfHours(int dayHours[][]) {
		double[] weeklyHours = new double[dayHours.length];
		for (int row = 0; row < dayHours.length; row++) {
			int sum = 0;
			for (int column = 0; column < dayHours[column].length; column++) {
				sum = sum + dayHours[row][column];
			}
			weeklyHours[row] = sum;
		}
		return weeklyHours;
	}

	public static int[] sortWeeklyHours(double[] weeklyHours) {
		int[] index = new int[weeklyHours.length];
		double temp;
		int indexTemp;
		for (int i = 0; i < weeklyHours.length; i++) {
			index[i] = i;
		}
		for (int i = 0; i < weeklyHours.length; i++) {
			int max = i;
			for (int j = i; j < weeklyHours.length; j++) {
				if (weeklyHours[j] > weeklyHours[max]) {
					max = j;
				}
			}
			temp = weeklyHours[i];
			weeklyHours[i] = weeklyHours[max];
			weeklyHours[max] = temp;
			indexTemp = index[i];
			index[i] = index[max];
			index[max] = indexTemp;
		}
		return index;

	}

	public static void format(int dayHours[][], int index[]) {
		System.out.printf("%12s", " ");
		System.out.println("Su M  T  W  Th F Sa");

		for (int i = 0; i < dayHours.length; i++) {
			System.out.print("Employee " + index[i]);
			for (int j = 0; j < dayHours[i].length; j++) {
				System.out.printf("%3d", dayHours[index[i]][j]);

			}
			System.out.println();
		}
	}

}

