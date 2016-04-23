//Bryan Bauer
//Program 4
//An easy recursion example to get a quick review of recursion...
//Calculating a factorial using recursion
public class Recursion_easy {

	public static void main(String[] args) {
		System.out.println("The factorial of 10 is: " + factorial(10));
		System.out.println("The factorial of 3 is: " + factorial(3));
		System.out.println("The factorial of 2 is: " + factorial(2));
		System.out.println("The factorial of 15 is: " + factorial(15));
		System.out.println("The factorial of 1 is: " + factorial(1));
		
	}
	
	public static int factorial(int x){
		if(x != 0){
			return x * factorial(x-1);
		} else { //This is the base case when x = 0 the recursion stops.
			return 1;
		}
	}
}

