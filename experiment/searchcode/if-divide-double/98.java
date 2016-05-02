package a1.s100502007;

import java.util.Scanner;

public class A11 {
	public static double Add(double first,double second){//add method
		double add = first + second;
		return add;
	}
	public static double Minus(double first,double second){//minus method
		double minus = first - second;
		return minus;
	}
	public static double Multiple(double first,double second){//multiple method
		double multiple = first * second;
		return multiple;
	}
	public static double Divide(double first,double second){//divide method
		double divide = first / second;
		return divide;
	}
	public static void main (String[] args){
		Scanner input = new Scanner(System.in);
			System.out.print("Please enter your name.\n");
			String name = input.next();//????
			System.out.print("Please enter your student number.\n");
			int number = input.nextInt();//????
		for( ; ; ){
			System.out.print("Welcome to use this method.\n"+"1.add.\n"+"2.minus.\n"+"3.multiple\n"+"4.divide\n"+"5.show information\n"+"6.exit\n");//????3
			int choose = input.nextInt();;//????
			switch(choose){
				case 1:
					double first1 = input.nextDouble();
					double second1 = input.nextDouble();
					System.out.print("?? = "+Add(first1,second1));
					break;
				case 2:
					double first2 = input.nextDouble();
					double second2 = input.nextDouble();
					System.out.print("?? = "+Minus(first2,second2));
					break;
				case 3:
					double first3 = input.nextDouble();
					double second3 = input.nextDouble();
					System.out.print("?? = "+Multiple(first3,second3));
					break;
				case 4:
					double first4 = input.nextDouble();
					double second4 = input.nextDouble();
					System.out.print("?? = "+Divide(first4,second4));
					break;
				case 5:
					System.out.print(name+" "+number+" \n");
					break;
				case 6:
					break;
			}
			if(choose == 6){//????
				break;
			}
			}
		}
	
}

