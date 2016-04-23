public class factorial {
    public static void main(String [] args) {
	int f = Integer.parseInt(args[0]);
	System.out.println(factorialRecursive(f));
	System.out.println(factorialTailRecurse(f));
	System.out.println(factorialIterative(f));
    }

    private static int factorialRecursive(int f) {
	if(f <= 1) {
	    return 1;
	}
	else {
	    return f * factorialRecursive(f - 1);
	}
    }

    private static int factorialTailRecurse(int f) {
	return tailHelper(f, 1);
    }

    private static int tailHelper(int f, int n) {
	if(f <= 1) {
	    return n;
	}
	else {
	    return tailHelper(f - 1, n * f);
	}
    } 

    private static int factorialIterative(int f) {
	int fac = 1;
	while(f >= 1) {
	    fac *= f;
	    f--;
	}
	return fac;
    }
}
	   
	
