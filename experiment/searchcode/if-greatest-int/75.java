// Which prime, below one-million, can be written as the sum of the most consecutive primes?

import java.util.ArrayList;

public class p50{

    public static  ArrayList getPrimes()
    {
        ArrayList p = new ArrayList();
        int num = 3; 
        p.add(2);
        boolean add = true;
        while(num < 1000000){
            for(int i = 0; i < p.size(); i++){
                if (num % (int) p.get(i) == 0){
                    add = false;
                    break;
                }
            }
            if(add)
                p.add(num);
            num++;
            add = true;
        }
        return p;
    }

    public static void main(String[] args){
        ArrayList p = getPrimes();
        int sum = 0;
        int index = 0;
        int start = 0;
        System.out.println(p.size());
        do{
            sum = 0;
            while(sum < 1000000 && index < p.size()){
                sum += (int) p.get(index);
                if (sum > 1000000)
                    sum-= (int) p.get(index);
                index++;
            }
            start++;
            index = start;
        }while(!(p.contains(sum)));
        System.out.println(sum);
    }
}

                
// Looking back, my method to this problem was pretty awful. In fact, I was lucky that the program even output the correect answer
// 1.) This problem was before I changed my getPrimes method to a better version, primarily Sieve using addition.
// 2.) I misintepreted or made the false assumption that the greatest prime would have the most consec sums
// The answers on the forums is really neat. Generate the sum while it is below 1,000,000. Then, remove the lowest prime.
// IF it is still not prime, remove highest prime. 
// Then follow in the pattern 2L, 1L1H, 2H, 3L, 2L1H .... 
// This could be found using pascals triangle, or a simple formula which increases high by one and decreases low by one. 
// I decided not to change my answer as a reminder of the lessons learned. 

