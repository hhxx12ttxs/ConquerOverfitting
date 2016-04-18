import java.util.Scanner;
public class CarPark {
	public static void main(String[]args){
		double hours;
		double temp;
		double totalReceipts=0;
		Scanner input=new Scanner(System.in);
		do
		{
			System.out.print("Enter number of hours (-1 to quit):"+"\n");
			hours=input.nextInt();
			if(hours==-1){
				System.out.print("exit");
			}
			else{
				temp=calculateCharges(hours);
				totalReceipts+=temp;
				System.out.print("Current Charge: �"+temp+"; Total reciepts: �"+totalReceipts+"\n");
			}
	}
	while(hours!=-1);
	}
	public static double calculateCharges(double charges){
		double hours=charges;
		if (hours<3)
		{
			charges =5;
		}
		else if (hours<16){
			charges= ((hours - 3)*1.5)+5;
		}
		else if(hours<=24){
			charges=25;
		}
		return charges;
	}
}

