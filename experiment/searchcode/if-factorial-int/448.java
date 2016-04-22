package two_Classics;

/**
 * Created by Diego on 04/04/2016.
 */
public class Factorial {

    public static void main(String[] args) {
        Factorial f = new Factorial();
        System.out.println(f.factorial(4));
        System.out.println(f.factorialIteratively(4));
        System.out.println(f.factorial(10));
        System.out.println(f.factorialIteratively(10));
    }

    private int factorial (int n ) {
        if (n == 1) return 1;
        int result = n * factorial(n-1);
        return result;
    }

    private int factorialIteratively(int n){
        int result = 1;
        if (n ==1) return 1;
        for (int i = 1; i <= n; i++) {
            result = result*i;
        }
        return result;
    }
}

