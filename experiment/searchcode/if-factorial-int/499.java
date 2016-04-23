/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package homework05;

/**
 *
 * @author Trey
 */

import java.util.Scanner;
public class Homework05 {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        
        System.out.print("Enter a number: ");
        int factorial = in.nextInt();
        
        System.out.println(factorial);
        
        int fact = Factorial.PrintNumbers(factorial-1);
        
        System.out.println(factorial*fact);
    }
}

class Factorial {
    static int PrintNumbers(int factorial) {
        return PrintNumbers(factorial, factorial+1);
    }
    
    static int PrintNumbers(int factorial, int space) {
        for(int i = space; i > factorial; i--) {
            System.out.printf("%2s", ' ');
        }
        
        System.out.println(factorial);
        
        if(factorial == 0) {
            for(int i = space; i > factorial ; i--)
                System.out.printf("%2s", ' ');
            
            System.out.println(1);
            return 1;
        }
        
        int fact = factorial*PrintNumbers(factorial-1, space);
        
        for(int i = space; i > fact ; i--)
            System.out.printf("%2s", ' ');
        
        System.out.println(fact);
        return fact;
    }     
    
//    static long factorial(int n) {
//        if(n == 1)                  //base case
//            return 1;
//        
//        return n * factorial(n - 1); //general case: n * (n-1)!
//    }
}
