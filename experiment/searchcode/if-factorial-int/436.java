/*
/   Project Euler problem #20 Factorial digit sum
/
/   Find the sum of the digits in the number 100!
/
*/

import java.math.BigInteger;


public class Euler_20
{
    public static void main(String[] args)
    {
        BigInteger oneHundredFactorial = new BigInteger("2");
        BigInteger nextFactorial;

        int sumOfDigits = 0;

        for (int i = 3; i <= 100; i++) {
            nextFactorial = new BigInteger(String.valueOf(i));
            oneHundredFactorial = oneHundredFactorial.multiply(nextFactorial);
        }

        String factorialString = oneHundredFactorial.toString();
        String[] factorialStringSplit = factorialString.split("");

        for (int i = 0; i < factorialStringSplit.length; i++) 
            if (!factorialStringSplit[i].equals("")) 
                sumOfDigits += Integer.parseInt(factorialStringSplit[i]);

        System.out.println("Answer: " + sumOfDigits);
    }

}

