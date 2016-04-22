/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Problems;

import java.util.ArrayList;

/**
 *
 * @author Kelly
 */
public class Problem074_DigitFactorialChains {
    public static void main (String[] args) {
        int count = 0;
        for (int i = 1; i < 1000000; i++) {
            ArrayList<Integer> digitFactorialSums = new ArrayList<Integer>();
            int n = i;
            int digitFactorialSum = n;
            
            while (!digitFactorialSums.contains(digitFactorialSum)) {
                digitFactorialSums.add(digitFactorialSum);
                
                digitFactorialSum = 0;
                for (int j = n; j > 0; j /= 10) {
                    int factorial = 1;
                    switch(j % 10) {
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
                    digitFactorialSum += factorial;
                }
                n = digitFactorialSum;
            }
            
            if (digitFactorialSums.size() == 60)
                count++;
        }
        
        System.out.println(count);
    }
}

