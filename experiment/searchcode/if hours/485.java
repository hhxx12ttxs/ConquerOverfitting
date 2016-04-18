import java.util.Scanner;


public class _1_WorkHours {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		long hours = input.nextLong(); //how many hours she needs to finish the project
		double days = input.nextDouble(); //available days to finish the project
		int productivity = input.nextInt(); //percent value
		double hoursAfterBiking = (days - days*0.1)*12;
		double efficientWorkHours = (hoursAfterBiking*productivity)/100;
		long hoursToFinishProject = (long) (efficientWorkHours);
		if (hoursToFinishProject >= hours) {
			System.out.println("Yes");
			System.out.println(hoursToFinishProject - hours);
		}
		else if (hoursToFinishProject < hours) {
			System.out.println("No");
			System.out.println(hoursToFinishProject - hours);
		}
	}
}

