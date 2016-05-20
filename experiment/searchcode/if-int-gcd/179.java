// Java program to compute lcm of n positive integers, n >= 1
// supplied in an array            lcmlist.java   AB 2008
// modified by Sambit Bikas Pal 28th Aug

import javax.swing.*;

public class gcdlist {
  int val;
 
  gcdlist() {			// constructor
    String s = JOptionPane.showInputDialog("Enter total number of integers: ");
    int n = Integer.parseInt(s);
    int tab[] = new int[n+1];
    for (int k = 0; k < n; k++) {
      s = JOptionPane.showInputDialog("Enter the next integer in table: ");
      tab[k] = Integer.parseInt(s);
    }
    val = gcd(n, tab);
  }

  int gcd(int n, int tab[]) {
    int p;
    p = tab[0];
    for (int k = 1; k < n; k++) {
      p=rgcd(p,tab[k]);
    }
    return p;
  }

  static int rgcd(int a, int b) {
    int s = a%b;
    if (s == 0) return b;
    else return rgcd(b,s);
  }

  public static void main (String[] args) {
    gcdlist z = new gcdlist();
    JOptionPane.showMessageDialog(null, "The gcd of the integers in the table is: " + z.val);
    System.exit(0);
 }
}

