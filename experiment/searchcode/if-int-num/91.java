package eulerTools;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Tools{
    //Returns primes as form of a boolean array
    //True = prime
    //Index = number
    public static boolean[] getPrimes(int max){
        boolean[] isPrime = new boolean[max + 1];
        int i = 2;
        for(int k = 0; k < isPrime.length; k++)
            isPrime[k] = true;
        isPrime[0]  = false;
        isPrime[1] = false;
        while(i < isPrime.length){
            if(isPrime[i]){
                for(int j = i+i; j < isPrime.length; j += i)
                    isPrime[j] = false;
            }
            i++;
        }
        return isPrime;
    }

           

    //Copys all possible permutations of the word into an arraylist
	public static void getPerms(String word, ArrayList arr){
        getPerms("", word, arr);
    }
    
    private static void getPerms(String first, String rest, ArrayList arr){
        if (rest.length() == 1)
            arr.add(Integer.parseInt(first + rest));
        else{
            for (int i = 0; i < rest.length(); i++)
                getPerms(first + rest.charAt(i), rest.substring(0,i) + rest.substring(i+1), arr);
        }
    }
   
    //Checks if word a is a perm of word b
    //Does this by putting all characters into an array, then 
    //sorts and sees if both arrays are equal.
    //Takes log(n)*n time. 
    public static boolean isPerm(String a,String b){
        char[] x = a.toCharArray(); 
        char[] y = b.toCharArray(); 
        Arrays.sort(x);
        Arrays.sort(y);
        if(Arrays.equals(x,y))
            return true;
        else return false;
    }
    public static boolean isPerm(int a,int b){
        char[] x =(""+ a).toCharArray(); 
        char[] y = (""+b).toCharArray(); 
        Arrays.sort(x);
        Arrays.sort(y);
        if(Arrays.equals(x,y))
            return true;
        else return false;
    }

    //Returns true if Palindrome
    public static boolean isPalindrome(String num){
        int start = 0;
        int len = num.length();
        int end = len - 1;
        while(start < len/2){
            if (num.charAt(start) != num.charAt(end))
                return false;
            start++;
            end--;
        }
        return true;
    }

    public static boolean isPalindrome(int num){
        return isPalindrome("" + num);
    }
    
    public static int reverse(int num){
        String rev = reverse("" + num);
        int n = Integer.parseInt(rev);
        return n;
    }

    //Returns the reverse of the string.
    public static String reverse(String num){
        int len = num.length();
        for(int i = 0; i < len - 1; i++){
            num = num.substring(0,i) + num.charAt(len - 1) + num.substring(i, len -1);
        }
        return num;
    }
    


    //Pretty useless method. Better to use binarySearch in collections
    //Linear searches for an element from certain starting point.
    //Returns false when reaches end or numbers are greater than key.
    public static boolean isIn(int begin, ArrayList p, int num){
        for(int i = begin; i < p.size(); i++){
            if((int) p.get(i) == num)
                return true;
            else if((int) p.get(i) > num)
                return false;

        }
        return false;
    }

 
   
} 

