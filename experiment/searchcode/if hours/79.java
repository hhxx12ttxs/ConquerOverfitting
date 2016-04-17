import java.util.*;
public class GPACalc {
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.print("A Credit Hours: ");
		int aHours = scan.nextInt();
		System.out.print("B Credit Hours: ");
		int bHours = scan.nextInt();
		System.out.print("C Credit Hours: ");
		int cHours = scan.nextInt();
		System.out.print("D Credit Hours: ");
		int dHours = scan.nextInt();
		System.out.print("F Credit Hours: ");
		int fHours = scan.nextInt();
		int sumHours = aHours + bHours + cHours + dHours + fHours;
		double gpaHours = 4.0 * aHours + 3.0*bHours + 2.0*cHours + 1.0 * dHours + 0.0 * fHours;
		if(sumHours>0){
			System.out.printf("GPA: %.2f", (gpaHours/sumHours));
		}else{
			System.out.print("You have not earned any grades.");
		}
	}
}
