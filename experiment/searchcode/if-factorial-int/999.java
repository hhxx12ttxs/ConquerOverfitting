package net.euler.project.problems.second;

/**
 * Created by jetzen on 23.12.14.
 */

/**
 * 145 is a curious number, as 1! + 4! + 5! = 1 + 24 + 120 = 145.
 * Find the sum of all numbers which are equal to the sum of the factorial of their digits.
 * Note: as 1! = 1 and 2! = 2 are not sums they are not included.
 */
public class Problem34 {
    public static void main(String[] args) {
        long sum=0;
        System.out.println("digitFactorial: "+getDigitFactorial(145));
        System.out.println(getDigitFactorial(40585));
        for(int i=3;i<1_000_000;i++){
            long digitFactorial=getDigitFactorial(i);
            if(digitFactorial==i){
                sum+=digitFactorial;
                System.out.println("is equals for: "+i);
            }
        }
        System.out.println("the sum is: "+sum);
    }

    public static long getDigitFactorial(int number){
        long factorial=0;
        long i=number;
        while(i>0){
            factorial+=getFactorial(i%10);
            i/=10;
        }
        return factorial;
    }


    private static long getFactorial(long number){
        long factorial=1;
        for(int i=1;i<=number;i++){
            factorial*=i;
        }
        return factorial;
    }
}

