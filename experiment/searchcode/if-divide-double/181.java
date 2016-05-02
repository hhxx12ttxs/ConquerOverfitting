//A11.java
package a1.s100502502;
import java.util.Scanner;
public class A11 {
	public static double result = 0;//initialize it
	public static void main(String[] args){
		Scanner input = new Scanner(System.in);
		System.out.print("Enter your name and student number : ");
		String name = input.next();//save user name
		String number = input.next();//save student number
		while(true){
			//print list
			System.out.println("1.add.   2.minus.   3.multiple.   4.divide.   5.show user information.  6.exit.");
			System.out.print("Choose number : ");
			int choose = input.nextInt();//save user's choice
			if(choose == 6)//exit
				break;
			else if(choose == 5){//show user information
				System.out.println("name : " + name);
				System.out.println("student number : " + number);
				continue;
			}
			else
				System.out.print("Enter value : ");
				double value = input.nextDouble();//save input value	
				switch(choose){
					case 1://add
						System.out.println(result + " + " + value + " = " + add(value));
						break;
					case 2://minus
						System.out.println(result + " - " + value + " = " + minus(value));
						break;
					case 3://multiple
						System.out.println(result + " * " + value + " = " + multiple(value));
						break;
					case 4://divide
						System.out.println(result + " / " + value + " = " + (value));
						break;
					default:
						break;
				}
		}
		System.out.println("ByeBye~");//show leave message 
	}
	public static double add(double num){//function add
		result = result + num;
		return result;
	}
	public static double minus(double num){//function minus
		result = result - num;
		return result;
	}
	public static double multiple(double num){//function multiple
		result = result * num;
		return result;
	}
	public static double divide(double num){//function divide
		result = result / num;
		return result;
	}
}

