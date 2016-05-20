// Java program to compute gcd of two positive integers  AB  Aug 2008
// Uses an iterative method	gcditer1.java

import javax.swing.*;

class gcdrec {
  public static void main(String[] args) {
    int r, x, y;
    String sx, sy;

    sx = JOptionPane.showInputDialog("\nEnter the first integer:\t");
    x = Integer.parseInt(sx);
    sy = JOptionPane.showInputDialog("\nEnter the second integer:\t");
    y = Integer.parseInt(sy);

//    r = x;
//    while ((x/r * r != x)  || (y/r * r != y)) r = r-1;
   r = gcd(x,y); 

   System.out.println("\nThe gcd of the two numbers is " + r + "\n");
  }
  static int gcd(int a, int b) {

    int s = a%b;

    if (s == 0) return b;

    else return gcd(b,s);

  }

}

