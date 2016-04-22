import java.util.Scanner;

public class Factorial {

    public static void main(String args[]) {

        Scanner scan = new Scanner(System.in);

        int n = scan.nextInt();

        System.out.print(factorial(n));

    }

    private static long factorial(int n) {   
     
        if (n == 1)
            return 1;
        else
            return n * factorial(n - 1);               
    }

}
