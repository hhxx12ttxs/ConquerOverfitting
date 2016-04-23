package tests.factorial;

public class Factorial {

    public static void main(String[] args) {
		/*
		System.out.println("Result: " + factorialRec(5) + "\n");
		System.out.println("Result: " + factorialRec(10) + "\n");
		System.out.println("Result: " + factorialRec(15) + "\n");
		System.out.println("Result: " + factorialRec(15) + "\n");
		*/
		System.out.println("Result: " + factorial(5) + "\n");
		System.out.println("Result: " + factorial(10) + "\n");
		System.out.println("Result: " + factorial(15) + "\n");
		System.out.println("Result: " + factorial(15) + "\n");
    }

    public static long factorialRec(int n) {
		if (n == 0) {
		    return 1;
		} else {
		    return n * factorialRec(n-1);
		}
    }
    
    public static long factorial(int n) {
    	int res = 1;
    	for (int i = 2; i <= n; i++) {
    		res += i;
    	}
    	return res;
    }
}

