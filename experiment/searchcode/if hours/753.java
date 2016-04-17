import java.util.Scanner;

public class _1_WorkHours {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner scan = new Scanner(System.in);
		int projectHours = scan.nextInt();
		int availableDays = scan.nextInt();
		int averageProductivity = scan.nextInt();

		double allHours = availableDays * 12;
		double workHours = allHours * 0.9;
		workHours *= ((double) averageProductivity / 100);
		int totalWorkHours = (int) workHours;

		int difference = totalWorkHours - projectHours;
		if (difference >= 0) {
			System.out.println("Yes\n" + difference);
		} else {
			System.out.println("No\n" + difference);
		}
	}

}
