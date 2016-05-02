/* Written by CT 23/02/2006
   This program finds the roots of a quadratic equation
*/

package week1;

import java.util.*;

public class FindRoots 
{
  public static void main(String[] args) 
  { 
    double a,b,c;               // coefficients of the equation
    double r1 = 0.0, r2 = 0.0;  // real roots of the equation
    double disc;                // discriminant (don't really need this comment..)
    
    // This is so it can read from the keyboard.
    Scanner console = new Scanner(System.in);

    // Getting user input for coefficients    
    System.out.println ("Enter value for a");
    a = console.nextDouble();
    System.out.println ("Enter value for b");
    b = console.nextDouble();
    System.out.println ("Enter value for c");
    c = console.nextDouble();
 
    // Computing results    
    disc = b*b - 4*a*c;    
    // no real roots exist if discriminant is negative     
    if (disc > 0.0)
    {    
    	r1 = ( -b + Math.sqrt(disc)) / (2*a);
    	r2 = ( -b - Math.sqrt(disc)) / (2*a);
    }

    // Displaying results    
    if (disc >= 0) 
    	System.out.println("Roots are " + r1 + " and " + r2);
    else  
    	System.out.println("No real roots exist");    
  }        
}

