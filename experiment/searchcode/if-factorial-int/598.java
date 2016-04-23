import java.util.Scanner;

/**
 * Created on 4/3/2016.
 */
public class factorial {

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        System.out.println(findFactorial(n));
        System.out.println(findFactorialRecursively(n));
    }

    static int findFactorial(int number) {
        int factorial = 1;
        for (int i = 2; i <= number ; i++)
            factorial *= i;

        return factorial;
    }

    static int findFactorialRecursively(int number) {
        if (number == 2)
            return 2;

        else
            return number * findFactorial(number - 1);
    }

}

