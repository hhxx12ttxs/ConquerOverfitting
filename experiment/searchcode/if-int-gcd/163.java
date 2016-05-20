import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FibonacciPrime {

    static boolean[] numbers;
    static int gcd;
    
    //find all the prime factors of N
    public static List<Integer> factorize(int n) {
        List<Integer> factors = new ArrayList<Integer>();

        for (int i = 2; i < numbers.length; i++) {
            if (!numbers[i] && n % i == 0) {
                factors.add(i);
            }
        }
        factors.add(n);
        return factors;
    }

    // Compute a Prime Table
    public static void primeSieve() {
        numbers[1] = true;
        numbers[0] = true;
        int i, j;
        for (i = 4; i < numbers.length; i += 2) {
            numbers[i] = true;
        }
        for (i = 3; i < Math.sqrt(numbers.length) + 1; i += 2) {
            if (!numbers[i]) {
                for (j = i + i; j < numbers.length; j += i) {
                    if (!numbers[j]) {
                        numbers[j] = true;
                    }
                }
            }
        }
    }
    
    //Generate Fibonaaci numbers
    public static long findFibo(List<Integer> factors) {
        long f1 = 1, f2 = 1, f = 0;
        long max = (long) Math.pow(10, 18)+1;        
        do {
            f = f1 + f2;
            f1 = f2;
            f2 = f;
            for (int x : factors) {                
                if (f % x == 0) {                    
                    gcd = x;
                    return f;
                }
            }
        } while (f < max);        
        return 0;
    }

    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine());
        int[] noList = new int[n];
        int i, max = Integer.MIN_VALUE;

        for (i = 0; i < n; i++) {
            noList[i] = Integer.parseInt(br.readLine());
            if (noList[i] > max) {
                max = noList[i];
            }
        }

        numbers = new boolean[(max / 2) + 1];
        primeSieve();
        List<Integer> factors = null;
        for (int no : noList) {
            factors = factorize(no);            
            System.out.println(findFibo(factors) + " " + gcd);
        }



    }
}

