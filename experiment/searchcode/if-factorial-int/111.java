/**
 * Created by den on 22.01.15.
 */
public class Factorial {
    public int getFactorial(int n) {
        if (n == 0)
            return 1;
        int factorial = 0;
        while (n != 0) {
            if (factorial == 0)
                factorial = n;
            else
                factorial = factorial * n;
            n--;
        }
        return factorial;
    }

    public static void main(String[] args) {
        Factorial rf = new Factorial();
        System.out.println(rf.getFactorial(1));
    }
}

