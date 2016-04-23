package chapterSix;

public class Factorial {
	
	public static int factorial(int n){
		if (n == 0){
			return 1;
		}else{
			return n * factorial(n-1);
		}
		
	}
	
	public static void main(String[] args){
		
		int n = 2;
		int b = 15;
		System.out.println(factorial(b)/factorial(n));
		
	}

}

