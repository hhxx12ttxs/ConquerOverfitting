package Arrays;

/**
 * Created by AKHIL on 11-Apr-16.
 */
public class Factorial {
    public static void main(String[] args) {
        int a = findFactorial(6);
        System.out.println(a);
    }
    private static int findFactorial(int n){
        if(n==0 || n==1)
            return 1;
        return n*findFactorial(n-1);
    }
}

