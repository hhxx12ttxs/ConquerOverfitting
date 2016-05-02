// what are the last ten digits of the sum of 1^1 + 2^2 ... 1000^1000

//Basic method: When doing the exponenets, only keep track of the last ten digits. The rest are extraneous.
// This prevents any long overflow. 


//Post Script
//Shouldve modded the sum for the last ten digits. Makes the answer easier to read.
//Also should have documented run time and all that stuff. 

public class p48{

    public static long square(long base, long exp){
        long bae = 1;
        long div = 10000000000L ;
        while(exp > 0){
            if(bae > 99999999)
                bae = bae % div;
            bae = base * bae;
            exp--;
        }
        return bae;
    }


    public static void main(String[] args){
        long sum = 0;
        for (long i = 1; i <= 1000; i++)
            sum +=  square(i,i);
       System.out.println(sum);
    }
}

