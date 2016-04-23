
public class Exercise1 {

	public static void main(String[] args) {
		System.out.println(" n\t n!");
		for (int n = 1; n < 25; n++) {
			System.out.println(n + "\t" + factorial(n));
		}

	}

	// Precondition: n>=0
	// The largest value of n that this returns the correct value
	// is n=12. This is because anything larger will be greater than
	// 12 will produce a number greater than 32 bits, or greater than
	// 2^(32-1)
	/*
	public static int factorial(int n) {
		if(n==0) return 1;
		if(n==1) return 1;
		else return n*factorial(n-1);
	}
	*/
	// n=20 will now be the highest n that will return a correct value.
	// Anything after that will produce a number greater than 64 bits.
	public static long factorial(int n) {
		if(n==0) return 1;
		if(n==1) return 1;
		else return n*factorial(n-1);
	}

}

