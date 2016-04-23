/*--------------------------------------------------------------*
 *  File Name: Factorial.java                                   |
 *  Author: Anthony Davis St. Aubin				|
 *  KUID: 2482383						|
 *  Email Address: a162s797@ku.edu				|
 *  Lab Number: 05                                              |
 *  Description: Computes the factorial of a user-input number,	|
 *    factorialUntil.						|
 *  Last Changed: 15-February-2013				|
 ---------------------------------------------------------------*/
import java.util.Scanner;
public class Factorial {
  public static void main(String[] args) {

    int keepTrack = 2;	    //the cases of 0! and 1! are handled seperately;
    int factorialOut = 1;   //output of the program, initialized to 1 for the cases of 0! and 1!;
    int factorialUntil;	    //user-input;
    Scanner keyboard1 = new Scanner(System.in);

    //Request a positive input;
    System.out.print("Enter a number to compute factorial: ");
    factorialUntil = keyboard1.nextInt();
    while(factorialUntil<0) {
      System.out.print("\nPlease enter a non-negative number: ");
      factorialUntil = keyboard1.nextInt();
    }

    //Compute the factorial of factorialUntil;
    //if (factorialUntil == 0) {factorialOut = 1;}    //unneccessary because of previous assignment of factorialOut to 1;
    while(keepTrack <= factorialUntil) {
      factorialOut *= keepTrack;
      keepTrack++;
    }

    System.out.printf("\nThe factorial of %d is: %d",factorialUntil,factorialOut);
  }
}

