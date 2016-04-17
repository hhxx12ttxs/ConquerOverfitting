import java.util.Scanner;


public class _01_WorkHours {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		int hoursToFinish = input.nextInt();
		int daysAvailableToFinish = input.nextInt();
		double productivity = input.nextInt();
		
		//days, of which 10% she will be biking
		double hoursOfWork = (daysAvailableToFinish - (0.1 * daysAvailableToFinish)) * 12;
		
		//hours * 75% productivity = 48.6 efficient work hours
		int hoursOfProductivity = (int)(hoursOfWork * (productivity/100.0));
		
		
		if (hoursToFinish > hoursOfProductivity) {
			System.out.println("No");
			System.out.println(hoursOfProductivity - hoursToFinish);
		}
		else {
			System.out.println("Yes");
			System.out.println(hoursToFinish - hoursOfProductivity);
		}

	}

}

