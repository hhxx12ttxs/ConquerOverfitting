package recursion;

public class Factorial {

	public static void main(String[] args){
		
		System.out.println(calculateFactorial(6));
	}

	public static int calculateFactorial(int i) {
		
		System.out.println("Calculating factorial for "+i);
		if(i==0){
			return 1;
		}
		
		int factorial= i * calculateFactorial(i-1);
		System.out.println("Done calculating factorial for "+i+" = "+factorial);
		return factorial;
		}
}

