/**
 * A library to interact with Virtual Worlds such as OpenSim
 * Copyright (C) 2012  Jitendra Chauhan, Email: jitendra.chauhan@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package jj2000.j2k.util;

/**
 * This class contains a collection of utility methods fro mathematical
 * operations. All methods are static.
 * */
public class MathUtil {

    /**
     * Method that calculates the floor of the log, base 2, of 'x'. The
     * calculation is performed in integer arithmetic, therefore, it is exact.
     *
     * @param x The value to calculate log2 on.
     *
     * @return floor(log(x)/log(2)), calculated in an exact way.
     * */
    public static int log2(int x) {
        int y,v;
        // No log of 0 or negative
        if (x <= 0) {
            throw new IllegalArgumentException(""+x+" <= 0");
        }
        // Calculate log2 (it's actually floor log2)
        v = x;
        y = -1;
        while (v>0) {
            v >>=1;
            y++;
        }
        return y;
    }

    /** 
     * Method that calculates the Least Common Multiple (LCM) of two strictly
     * positive integer numbers.
     *
     * @param x1 First number
     *
     * @param x2 Second number
     * */
    public static final int lcm(int x1,int x2) {
        if(x1<=0 || x2<=0) {
            throw new IllegalArgumentException("Cannot compute the least "+
                                               "common multiple of two "+
                                               "numbers if one, at least,"+
                                               "is negative.");
        }
        int max,min;
        if (x1>x2) {
            max = x1;
            min = x2;
        } else {
            max = x2;
            min = x1;
        }
        for(int i=1; i<=min; i++) {
            if( (max*i)%min == 0 ) {
                return i*max;
            }
        }
        throw new Error("Cannot find the least common multiple of numbers "+
                        x1+" and "+x2);
    }

    /** 
     * Method that calculates the Least Common Multiple (LCM) of several
     * positive integer numbers.
     *
     * @param x Array containing the numbers.
     * */
    public static final int lcm(int[] x) {
        if(x.length<2) {
            throw new Error("Do not use this method if there are less than"+
                            " two numbers.");
        }
        int tmp = lcm(x[x.length-1],x[x.length-2]);
        for(int i=x.length-3; i>=0; i--) {
            if(x[i]<=0) {
                throw new IllegalArgumentException("Cannot compute the least "+
                                                   "common multiple of "+
                                                   "several numbers where "+
                                                   "one, at least,"+
                                                   "is negative.");
            }
            tmp = lcm(tmp,x[i]);
        }
        return tmp;
    }

    /** 
     * Method that calculates the Greatest Common Divisor (GCD) of two
     * positive integer numbers.
     * */
    public static final int gcd(int x1,int x2) {
        if(x1<0 || x2<0) {
            throw new IllegalArgumentException("Cannot compute the GCD "+
                                               "if one integer is negative.");
        }
        int a,b,g,z;

        if(x1>x2) {
            a = x1;
            b = x2;
        } else {
            a = x2;
            b = x1;
        }

        if(b==0) return 0;

        g = b;
        
        while (g!=0) {
            z= a%g;
            a = g;
            g = z;
        }
        return a;
    }

    /** 
     * Method that calculates the Greatest Common Divisor (GCD) of several
     * positive integer numbers.
     *
     * @param x Array containing the numbers.
     * */
    public static final int gcd(int[] x) {
        if(x.length<2) {
            throw new Error("Do not use this method if there are less than"+
                            " two numbers.");
        }
        int tmp = gcd(x[x.length-1],x[x.length-2]);
        for(int i=x.length-3; i>=0; i--) {
            if(x[i]<0) {
                throw new IllegalArgumentException("Cannot compute the least "+
                                                   "common multiple of "+
                                                   "several numbers where "+
                                                   "one, at least,"+
                                                   "is negative.");
            }
            tmp = gcd(tmp,x[i]);
        }
        return tmp;
    } 
}

