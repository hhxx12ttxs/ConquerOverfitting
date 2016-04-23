package recursion;

public class Factorial {
	
	int factorial(int num) {
		
		System.out.println("num : " + num);
		
		if(num > 1)
			return (num * factorial(num - 1));
		else
			return 1;
	}

	public static void main(String[] args) {
		
		Factorial f = new Factorial();		
		int num=5;		
		System.out.println("Factorial of " + num + " is " + f.factorial(num));

	}

}

