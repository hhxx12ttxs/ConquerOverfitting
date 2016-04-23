import jeliot.io.*;

class Factorial {

    public static void main() {
		int n = 7;
		Output.println(fact(n));
    }

	public static int fact(int n) {
		if (n <= 1) {
			return 1;
		} else {
			return n * fact(n-1);
		}
	}		
}
