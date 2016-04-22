
import java.util.Scanner;

public class _12CalculateNFactorial {
    public static void main(String args[])
    {
        Scanner scan = new Scanner(System.in);
        int userInput = scan.nextInt();
        Factorial number = new Factorial();
        int factNum = number.factorial(userInput);
        System.out.println(factNum);
    }
    static int Factorial(int a) {

        int factorial = a * a - 1;
        return factorial;
    }
}
class Factorial
{
    int factorial(int n)
    {
        int result;

        if(n==1 || n == 0) {
            return 1;
        }
        result = factorial(n-1) * n;
        return result;
    }
}
