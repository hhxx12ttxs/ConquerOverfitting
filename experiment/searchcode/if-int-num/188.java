import eulerTools.Tools;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;
/*By replacing the 1st digit of *3, it turns out that six of the nine possible values: 13, 23, 43, 53, 73, and 83, are all prime.

By replacing the 3rd and 4th digits of 56**3 with the same digit, this 5-digit number is the first example having seven primes among the ten generated numbers, yielding the family: 56003, 56113, 56333, 56443, 56663, 56773, and 56993. Consequently 56003, being the first member of this family, is the smallest prime with this property.

Find the smallest prime which, by replacing part of the number (not necessarily adjacent digits) with the same digit, is part of an eight prime value family.
*/

public class p51{

    public static int changeNum(String binary, int num, int digit){
        String n = "" + num ;
        for(int i = 0; i < binary.length(); i++){
            if(binary.charAt(i) == (char) 49){
                if(i == 0 && digit == 0)
                    return num;
               n = n.substring(0,i) + digit + n.substring(i+1); 
            }
        }
        return (Integer.parseInt(n));
    }

    public static String[] generateBinaryArrays(int length){
        int num = 1;
        for(int i = 0; i < length; i++)
            num = num * 2;
        String[] arr = new String[num/2];
        for(int i = 0; i < arr.length; i++)
            arr[i] = Integer.toBinaryString(2*i+1);
       return arr;
    }

    public static void main(String[] args){
        long start = System.currentTimeMillis();
        int num = 56993;
        int count = 0;
        int req = 9;
        String arr[] = generateBinaryArrays(5);
        ArrayList<Integer> nums = new ArrayList();
        boolean[] primes = Tools.getPrimes(1000000);
loop:
        while(num < primes.length){
            for(int i = 0; i < arr.length; i++){
                for(int d = 0; d < 10; d++){
                    int newNum = changeNum(arr[i], num, d);
                    if (primes[newNum]){
                        nums.add(newNum);
                        count++;
                    }
                }
                if(count >= req){
                    Collections.sort(nums);
                    System.out.println(nums.get(1));
                    break loop;
                }
                else{
                    nums = new ArrayList();
                    count = 0;
                }
            }
            num++;
            if(num == 100000 || num == 1000000)
                generateBinaryArrays((""+num).length());
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start)/1000); 
    }
}

// Prints: 121313
// Takes about 2 seconds
// My Method. Generate list of primes using sieve method. 
// Then, for a certain num, find all possible combinations for single, double ... n changes
// Test if each one is prime.
// If the family is greater than 8, then bingo. Else, increase num by one.
//
//I like my solution. The better would be to compare different prime families. Like, see if there are 8 **xyz, or *x*y*z, and test each one of those.
//Yet again, need better psuedocode. Need to write comments between sessions.
//
//Another improvement I did was changing my list of primes into a boolean array. Index represents the number. T or F represents prime. Makes access and testing primes all in constant time. 
//
//

