/* $Id: Testlet.java,v 1.2 2005/03/13 17:47:10 woudt Exp $
 *
 * Copyright (C) 1999-2005 The Cryptix Foundation Limited.
 * All rights reserved.
 * 
 * Use, modification, copying and distribution of this software is subject 
 * the terms and conditions of the Cryptix General Licence. You should have 
 * received a copy of the Cryptix General License along with this library; 
 * if not, you can download a copy from http://www.cryptix.org/ .
 */

package cryptix.test;

import java.io.*;
import java.util.*;

/**
 * Abstract superclass for all tests.
 *
 * @author  Edwin Woudt (edwin@cryptix.org)
 * @author  Jeroen van Gelderen (gelderen@cryptix.org)
 * @author  David Hopwood
 * @author  Raif Naffah
 * @author  Systemics Ltd
 */
public abstract class Testlet {
    
    int level;
    boolean fail;
    Vector debug;
    String globalname, testname, testdatadir;
    long start;
    
    // constructor
    public Testlet(String name) {
        while (name.length() < 28) name = name+" ";
        this.globalname = name;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public void setTestdatadir(String dir) {
        this.testdatadir = dir;
    }
    
    public String getTestdatadir() {
        return testdatadir;
    }
    
    // must be implemented by the subclass
    public abstract void test() throws Exception;

    // used by subclass to report
    public void beginTest(String name) {
        while (name.length() < 33) name = name+" ";
        this.testname = name;
        if (level>=1) System.out.print("*** Testing    "+name);
        if (level>=3) System.out.println();
        if (level==2) debug = new Vector();
        start = System.currentTimeMillis();
    }
    
    // used by subclass to report
    public void debug(String text) {
        if (level==2) debug.addElement(text);
        if (level>=3) System.out.println("    >>> "+text);
    }
        
    // used by subclass to report
    public void passIf(boolean pass) {
        String time = ""+(System.currentTimeMillis()-start);
        while (time.length() < 8) time = " "+time;
        if (pass) {
            if (level>=3) System.out.print("*** Result for "+testname);
            if (level>=1) System.out.println(" >   OK   <"+time);
        } else {
            fail = true;
            if (level>=3) System.out.print("*** Result for "+testname);
            if (level>=1) System.out.println(" > Failed <"+time);
            if (level==2) {
                for (int i=0; i<debug.size(); i++) {
                    System.out.println("    >>> "+(String)debug.elementAt(i));
                }
            }
        }
    }
    
    // called to run all the tests
    public boolean run() {
        if (level>=1) System.out.println();
        if (level>=0) System.out.print("Running tests for: "+globalname);
        if (level>=1) System.out.println();
        if (level>=1) System.out.println("----------------------------------"+
                                         "---------------------------------");
        fail = false;
        try {
            test();
        } catch (Throwable t) {
            fail = true;
            if (level>=3) System.out.print("*** Result for "+testname);
            if (level>=1) System.out.println(" > Except <");
            if (level==2) {
                for (int i=0; i<debug.size(); i++) {
                    System.out.println("    >>> "+(String)debug.elementAt(i));
                }
            }
            if (level>=2) t.printStackTrace();
        }
        if (level>=1) System.out.println("----------------------------------"+
                                         "---------------------------------");
        if (level>=1) System.out.print("Final result for: "+globalname);
        if (fail) {
            if (level>=0) System.out.println(" ==> Failed <==");
        } else {
            if (level>=0) System.out.println(" ==>   OK   <==");
        }
        if (level>=1) System.out.println("----------------------------------"+
                                         "---------------------------------");
        return !fail;
    }
    
    // utility methods
    
    private static final char[] hexDigits = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    public static byte[] parseHexString(String hex) {
        int len = hex.length();
        byte[] buf = new byte[((len + 1) / 2)];

        int i = 0, j = 0;
        if ((len % 2) == 1)
            buf[j++] = (byte) fromDigit(hex.charAt(i++));

        while (i < len) {
            buf[j++] = (byte) ((fromDigit(hex.charAt(i++)) << 4) |
                                fromDigit(hex.charAt(i++)));
        }
        return buf;
    }
    
    
    /**
     * Returns the number from 0 to 15 corresponding to the hex digit <i>ch</i>.
     */
    public static int fromDigit(char ch) 
    {
        if (ch >= '0' && ch <= '9')
            return ch - '0';
        if (ch >= 'A' && ch <= 'F')
            return ch - 'A' + 10;
        if (ch >= 'a' && ch <= 'f')
            return ch - 'a' + 10;

        throw new IllegalArgumentException("invalid hex digit '" + ch + "'");
    }
    
    
    public static String toString(byte[] ba) {
        return toString(ba, 0, ba.length);
    }
    
    
    /**
     * Returns a string of hexadecimal digits from a byte array. Each
     * byte is converted to 2 hex symbols.
     * <p>
     * If offset and length are omitted, the whole array is used.
     */
    public static String toString(byte[] ba, int offset, int length) {
        char[] buf = new char[length * 3];
        int j = 0;
        int k;

        for (int i = offset; i < offset + length; i++) {
            k = ba[i];
            buf[j++] = hexDigits[(k >>> 4) & 0x0F];
            buf[j++] = hexDigits[ k        & 0x0F];
            buf[j++] = ' ';
        }
        return new String(buf);
    }
    
    public static boolean isEqual(byte[] a, byte[] b)
    {
        if(a.length != b.length)
            return false;
            
        for(int i=0; i<a.length; i++)
            if(a[i]!=b[i]) return false;
            
        return true;
    }

}

