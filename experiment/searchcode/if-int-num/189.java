//he arithmetic sequence, 1487, 4817, 8147, in which each of the terms increases by 3330, 
//is unusual in two ways: (i) each of the three terms are prime, 
//and, (ii) each of the 4-digit numbers are permutations of one another.
//There are no arithmetic sequences made up of three 1-, 2-, or 3-digit primes, exhibiting this property, 
//but there is one other 4-digit increasing sequence.


//What 12-digit number do you form by concatenating the three terms in this sequence?

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

public class p49{
    public static  ArrayList getPrimes(int max)
    {
        ArrayList p = new ArrayList();
        int num = 3; 
        p.add(2);
        boolean add = true;
        while(num < max){
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

	public static void  getPerms(int num, ArrayList arr){
        getPerms("", "" + num, arr);
    }
    
    private static void getPerms(String first, String rest, ArrayList arr){
        if (rest.length() == 1)
            arr.add(Integer.parseInt(first + rest));
        else{
            for (int i = 0; i < rest.length(); i++)
                getPerms(first + rest.charAt(i), rest.substring(0,i) + rest.substring(i+1), arr);
        }
    }
   
    private static boolean isPerm(int a, int b){
        char[] x = ("" + a).toCharArray(); 
        char[] y = ("" + b).toCharArray(); 
        Arrays.sort(x);
        Arrays.sort(y);
        if(x.equals(y))
            return true;
        else return false;
    }

    private static boolean isIn(int begin, ArrayList p, int num){
        for(int i = begin; i < p.size(); i++){
            if((int) p.get(i) == num)
                return true;
            else if((int) p.get(i) > num)
                return false;
            
        }
        return false;
    }

	public static void main(String[] args){
        long startTime = System.currentTimeMillis();
       ArrayList primes = getPrimes(10000);
       int index = 0;
       while((int) primes.get(index) < 1000)
           index++;
       int begin = index;
       ArrayList<Integer> done = new ArrayList();
       int num = (int) primes.get(index);
loop:
       while(true){
            ArrayList<Integer> primePerms = new ArrayList();
            ArrayList<Integer> perms = new ArrayList();
            getPerms(num, perms);
            for(int i : perms)
               if(isIn(begin, primes, i))
                    primePerms.add(i);
            if(primePerms.size() < 3) 
                index++;
            else{
                Collections.sort(primePerms);
                if(!(primePerms.get(0) == 1487)){ 
                for(int i = 0; i+2 < primePerms.size(); i++)
                    for(int j = i + 1; j + 1 < primePerms.size(); j++)
                        for(int k = i + 2; k < primePerms.size(); k++)
                            if(primePerms.get(i) - primePerms.get(j) == primePerms.get(j) - primePerms.get(k) && !(primePerms.get(i).equals(primePerms.get(j)))){
                                System.out.println("" + primePerms.get(i) + primePerms.get(j) + primePerms.get(k));
                                long endTime = System.currentTimeMillis();
                                System.out.println(endTime - startTime);
                               break loop;

                        
                        
                            }
                }
                index++;
            }
             
           num = (int) primes.get(index);
       }
    }
}


// Solution is 296962999629
// Runtime: 89 ms
// Some things in the future: write psuedo code first. Then code. Made a few extraneous 
// functions. 
// This took waay too long to code. Some things that I learned from this problem:
//      Remembered hard recursion with the permutation problem
//      Syntax for some things.

// Runs through brute force. There are ways to improve, but it still works.
//Post COmments
// Also, I should have used StringBuilder class. 
// 

