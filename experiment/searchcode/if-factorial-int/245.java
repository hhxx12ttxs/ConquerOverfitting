package przygotowanka;
// Factorial - Silnia
public class RecursiveFactorial {
	int temp;
	public static void main (String [] args){
		int n = 3;
		RecursiveFactorial rf = new RecursiveFactorial();
		//System.out.println(rf.factorial(n));
		System.out.println(rf.factorialMy(n));
		
	}
	public int factorial(int n) {
		if (n == 0)
			return 1;
		else
			return n * factorial(n-1);
	}
	public int factorialMy(int n) {
		if (n == 0)
			return 1;
		else{
			temp = n * factorialMy(n-1);
			return temp;
		}
	}

}

