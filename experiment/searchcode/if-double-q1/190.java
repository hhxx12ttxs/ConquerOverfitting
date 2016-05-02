package ce1002.s101502519;

import java.util.Scanner;

public class Q1 {
public static double toF(double C){   //??????? 
	C=C*9/5+32;
	return C;
}

public static double toC(double F){    //???????
	F=(F-32)*5/9;
	return F;
}
	
public static void main(String[] args){
	while(true){     //????
	System.out.println("Please choose the method you want to use:");  
	System.out.println("1.toFahrenheit");	
	System.out.println("2.toCelcius");	
	System.out.println("3.Exit ");	
	
	Scanner input = new Scanner(System.in);
	
	int a = input.nextInt();    //??????
	
	if(a==1){	//???????
		System.out.println("Please input the temperature: ");   
		double C = input.nextDouble();     //??????
		System.out.print(C);
		System.out.print(" in Celcius is equal to ");
		System.out.print(toF(C));
		System.out.println(" in Fahrenheit.");
		System.out.println();
	}
	else if(a==2){		//???????
		System.out.println("Please input the temperature: ");
		double F = input.nextDouble();
		System.out.print(F);     //??????
		System.out.print(" in Fahrenheit is equal to ");
		System.out.print(toC(F));
		System.out.println(" in Celcius.");	
		System.out.println();
	}
	else if(a==3){     //??
		System.out.println("Good Bye");	
		break;
	}
	}
}
}

