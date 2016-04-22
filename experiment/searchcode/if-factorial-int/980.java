/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Problems;

/**
 *
 * @author Kelly
 */
public class Problem034_DigitFactorials {
    public static void main (String[] args) {
        int sumOfDigitFactorials = 0;
        for (int i = 10; i < 2540160; i++) {
            int digitFactorial = 0;
            for (int j = i; j > 0; j /= 10) {
                digitFactorial += factorial(j % 10);
            }
            if (i == digitFactorial) {
                System.out.println(i);
                sumOfDigitFactorials += i;
            }
        }
        
        System.out.println(sumOfDigitFactorials);
    }
    
    public static int factorial (int n) {
        int factorial = 1;
        switch (n) {
            case 0:
                factorial = 1;
                break;
            case 1:
                factorial = 1;
                break;
            case 2:
                factorial = 2;
                break;
            case 3:
                factorial = 6;
                break;
            case 4:
                factorial = 24;
                break;
            case 5: 
                factorial = 120;
                break;
            case 6:
                factorial = 720;
                break;
            case 7:
                factorial = 5040;
                break;
            case 8:
                factorial = 40320;
                break;
            case 9:
                factorial = 362880;
                break;
        }
        return factorial;
    }
}

