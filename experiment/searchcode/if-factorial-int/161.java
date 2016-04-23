package free;

/**
 * Created by viniciushisao on 18/04/2016.
 */
public class Factorial {

    public static int factorialDivision (int n, int k){
        return getFactorial(n)/getFactorial(k);
    }

    private static int getFactorial(int n){

        int nFactorial = 1;
        for (int i =1 ; i <=n ; i ++){
            nFactorial *=i;
        }
        return nFactorial;
    }

    public static int simpleFactorialDivision(int n, int k){
        if (n < k){
            return 0;
        }

        int res = 1;
        for (; k < n ; n --){
            res *= n;
        }

        return  res;
    }

}

