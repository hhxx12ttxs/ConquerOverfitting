package homework_05;

import java.util.Scanner;

public class Problem14 {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		
		System.out.print("Enter a number: ");
		int number = sc.nextInt();
		
		System.out.println(nFactorial(number));
		
	}
	
	
//	edin nachin za reshavane na zadachata
//	
//	static void nFactorial(int number) {
//		
//		int factorial = 1;
//		
//		for(int i = 1; i <= number; i++){
//			
//			factorial = factorial * i;
//			
//		}
//		
//		System.out.println(factorial);
//		
//	}
	
//	vtori nachin za reshavane na zadachata :)
	static int nFactorial(int number){
		
		int factorial = 1;
		
		if(number == 0){
			
			return 1;
		}
		
		if(number > 0){
		
			for(int i = number; i > 0; i--){
				
				factorial = factorial * i;
				
			}
			
		}
		else{
			
			for(int i = number; i < 0; i++){
				
				factorial = factorial * i;
				
			}
		}
		
		return factorial;
		
	}

}

