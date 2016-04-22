/**
 * Created by User on 31.03.2016.
 */
public class Factorial {
    public static void main(String[] args) {
        int num = 5;
        System.out.println(factorial(num));

    }

    private static int factorial(int base) {
        if(base==1) return 1;
        return base*factorial(base-1);
    }
}

