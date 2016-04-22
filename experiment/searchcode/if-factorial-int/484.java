/**
 * Created by Алексей on 09.12.2015.
 */
public class Factorial {
    public static void main(String[] args) {
        int i = 5;
        System.out.println("factorial of i " + i + " " + factorial(i));
    }

    private static int factorial(int i) {
        if (i == 1){
//            throw new IllegalArgumentException();
            return 1;
        }
        int factorial = factorial(i - 1);
        int i1 = i * factorial;
        return i1;
    }
}

