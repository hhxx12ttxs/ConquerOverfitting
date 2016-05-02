package edu.miracosta.cs113.lb2;

import java.util.Scanner;

public class PowerOfTwo
    {

        /**
         * @param args
         */
        public static void main(String[] args)
            {
                boolean notDone = true;
                // TODO Auto-generated method stub
                Scanner keyboard = new Scanner(System.in);
                System.out.println("Enter a number and I will tell you if it is a power of two");
                int userInput = keyboard.nextInt();
                
                
                if((userInput & (userInput-1))==0)
                 {
                     System.out.println("your number is a power of two");
                 }
                else
                 {
                     System.out.println("your number is not a power of two");
                     
                 }
                    
            }

    }

