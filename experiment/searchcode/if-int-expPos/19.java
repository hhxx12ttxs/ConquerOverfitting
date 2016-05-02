/*
*   Class   Fmath
*
*   USAGE:  Mathematical class that supplements java.lang.Math and contains:
*               the main physical constants
*               trigonemetric functions absent from java.lang.Math
*               some useful additional mathematical functions
*               some conversion functions
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    June 2002
*   AMENDED: 6 January 2006, 12 April 2006, 5 May 2006, 28 July 2006, 27 December 2006,
*            29 March 2007, 29 April 2007, 2,9,15 & 26 June 2007, 20 October 2007, 4-6 December 2007
*            27 February 2008, 25 April 2008, 26 April 2008, 13 May 2008, 25/26 May 2008, 3-7 July 2008
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Fmath.html
*
*   Copyright (c) 2002 - 2008
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.math;

import java.util.ArrayList;
import java.util.Vector;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Fmath{

        // PHYSICAL CONSTANTS

        public static final double N_AVAGADRO = 6.0221419947e23;        /*      mol^-1          */
        public static final double K_BOLTZMANN = 1.380650324e-23;       /*      J K^-1          */
        public static final double H_PLANCK = 6.6260687652e-34;         /*      J s             */
        public static final double H_PLANCK_RED = H_PLANCK/(2*Math.PI); /*      J s             */
        public static final double C_LIGHT = 2.99792458e8;              /*      m s^-1          */
        public static final double R_GAS = 8.31447215;                  /*      J K^-1 mol^-1   */
        public static final double F_FARADAY = 9.6485341539e4;          /*      C mol^-1        */
        public static final double T_ABS = -273.15;                     /*      Celsius         */
        public static final double Q_ELECTRON = -1.60217646263e-19;     /*      C               */
        public static final double M_ELECTRON = 9.1093818872e-31;       /*      kg              */
        public static final double M_PROTON = 1.6726215813e-27;         /*      kg              */
        public static final double M_NEUTRON = 1.6749271613e-27;        /*      kg              */
        public static final double EPSILON_0 = 8.854187817e-12;         /*      F m^-1          */
        public static final double MU_0 = Math.PI*4e-7;                 /*      H m^-1 (N A^-2) */

        // MATHEMATICAL CONSTANTS
        public static final double EULER_CONSTANT_GAMMA = 0.5772156649015627;
        public static final double PI = Math.PI;                        /*  3.141592653589793D  */
        public static final double E = Math.E;                          /*  2.718281828459045D  */

        // HashMap for 'arithmetic integer' recognition nmethod
        private static final Map<Object,Object> integers = new HashMap<Object,Object>();
        static{
            integers.put(Integer.class, BigDecimal.valueOf(Integer.MAX_VALUE));
            integers.put(Long.class, BigDecimal.valueOf(Long.MAX_VALUE));
            integers.put(Byte.class, BigDecimal.valueOf(Byte.MAX_VALUE));
            integers.put(Short.class, BigDecimal.valueOf(Short.MAX_VALUE));
            integers.put(BigInteger.class, BigDecimal.valueOf(-1));
        }

        // METHODS

        // LOGARITHMS
        // Log to base 10 of a double number
        public static double log10(double a){
            return Math.log(a)/Math.log(10.0D);
        }

        // Log to base 10 of a float number
        public static float log10(float a){
            return (float) (Math.log((double)a)/Math.log(10.0D));
        }

        // Base 10 antilog of a double
        public static double antilog10(double x){
            return Math.pow(10.0D, x);
        }

        // Base 10 antilog of a float
        public static float antilog10(float x){
            return (float)Math.pow(10.0D, (double)x);
        }

        // Log to base e of a double number
        public static double log(double a){
            return Math.log(a);
        }

        // Log to base e of a float number
        public static float log(float a){
            return (float)Math.log((double)a);
        }

        // Base e antilog of a double
        public static double antilog(double x){
            return Math.exp(x);
        }

        // Base e antilog of a float
        public static float antilog(float x){
            return (float)Math.exp((double)x);
        }

        // Log to base 2 of a double number
        public static double log2(double a){
            return Math.log(a)/Math.log(2.0D);
        }

        // Log to base 2 of a float number
        public static float log2(float a){
            return (float) (Math.log((double)a)/Math.log(2.0D));
        }

        // Base 2 antilog of a double
        public static double antilog2(double x){
            return Math.pow(2.0D, x);
        }

        // Base 2 antilog of a float
        public static float antilog2(float x){
            return (float)Math.pow(2.0D, (double)x);
        }

        // Log to base b of a double number and double base
        public static double log10(double a, double b){
            return Math.log(a)/Math.log(b);
        }

        // Log to base b of a double number and int base
        public static double log10(double a, int b){
            return Math.log(a)/Math.log((double)b);
        }

        // Log to base b of a float number and flaot base
        public static float log10(float a, float b){
            return (float) (Math.log((double)a)/Math.log((double)b));
        }

        // Log to base b of a float number and int base
        public static float log10(float a, int b){
            return (float) (Math.log((double)a)/Math.log((double)b));
        }

        // SQUARES
        // Square of a double number
        public static double square(double a){
            return a*a;
        }

        // Square of a float number
        public static float square(float a){
            return a*a;
        }

        // Square of a BigDecimal number
        public static BigDecimal square(BigDecimal a){
            return a.multiply(a);
        }

        // Square of an int number
        public static int square(int a){
            return a*a;
        }

        // Square of a long number
        public static long square(long a){
            return a*a;
        }

        // Square of a BigInteger number
        public static BigInteger square(BigInteger a){
            return a.multiply(a);
        }

        // FACTORIALS
        // factorial of n
        // argument and return are integer, therefore limited to 0<=n<=12
        // see below for long and double arguments
        public static int factorial(int n){
            if(n<0)throw new IllegalArgumentException("n must be a positive integer");
            if(n>12)throw new IllegalArgumentException("n must less than 13 to avoid integer overflow\nTry long or double argument");
            int f = 1;
            for(int i=2; i<=n; i++)f*=i;
            return f;
        }

        // factorial of n
        // argument and return are long, therefore limited to 0<=n<=20
        // see below for double argument
        public static long factorial(long n){
            if(n<0)throw new IllegalArgumentException("n must be a positive integer");
            if(n>20)throw new IllegalArgumentException("n must less than 21 to avoid long integer overflow\nTry double argument");
            long f = 1;
            long iCount = 2L;
            while(iCount<=n){
                f*=iCount;
                iCount += 1L;
            }
            return f;
        }

        // factorial of n
        // Argument is of type BigInteger
        public static BigInteger factorial(BigInteger n){
            if(n.compareTo(BigInteger.ZERO)==-1)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            BigInteger one = BigInteger.ONE;
            BigInteger f = one;
            BigInteger iCount = new BigInteger("2");
            while(iCount.compareTo(n)!=1){
                f = f.multiply(iCount);
                iCount = iCount.add(one);
            }
            one = null;
            iCount = null;
            return f;
        }

        // factorial of n
        // Argument is of type double but must be, numerically, an integer
        // factorial returned as double but is, numerically, should be an integer
        // numerical rounding may makes this an approximation after n = 21
        public static double factorial(double n){
            if(n<0.0 || (n-Math.floor(n))!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 1.0D;
            double iCount = 2.0D;
            while(iCount<=n){
                f*=iCount;
                iCount += 1.0D;
            }
            return f;
        }

        // factorial of n
        // Argument is of type BigDecimal but must be, numerically, an integer
        public static BigDecimal factorial(BigDecimal n){
            if(n.compareTo(BigDecimal.ZERO)==-1 || !Fmath.isInteger(n))throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            BigDecimal one = BigDecimal.ONE;
            BigDecimal f = one;
            BigDecimal iCount = new BigDecimal(2.0D);
            while(iCount.compareTo(n)!=1){
                f = f.multiply(iCount);
                iCount = iCount.add(one);
            }
            one = null;
            iCount = null;
            return f;
        }



        // log to base e of the factorial of n
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(int n){
            if(n<0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            for(int i=2; i<=n; i++)f+=Math.log(i);
            return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(long n){
            if(n<0L)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            long iCount = 2L;
            while(iCount<=n){
                f+=Math.log(iCount);
                iCount += 1L;
            }
            return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(double n){
            if(n<0 || (n-Math.floor(n))!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            double iCount = 2.0D;
            while(iCount<=n){
                f+=Math.log(iCount);
                iCount += 1.0D;
            }
            return f;
        }


        // SIGN
        /*      returns -1 if x < 0 else returns 1   */
        //  double version
        public static double sign(double x){
            if (x<0.0){
                return -1.0;
            }
            else{
                return 1.0;
            }
        }

        /*      returns -1 if x < 0 else returns 1   */
        //  float version
        public static float sign(float x){
            if (x<0.0F){
                return -1.0F;
            }
            else{
                return 1.0F;
            }
        }

        /*      returns -1 if x < 0 else returns 1   */
        //  int version
        public static int sign(int x){
            if (x<0){
                return -1;
            }
            else{
                return 1;
            }
        }

        /*      returns -1 if x < 0 else returns 1   */
        // long version
        public static long sign(long x){
            if (x<0){
                return -1;
            }
            else{
                return 1;
            }
        }

        // ADDITIONAL TRIGONOMETRIC FUNCTIONS

        // Returns the length of the hypotenuse of a and b
        // i.e. sqrt(a*a+b*b) [without unecessary overflow or underflow]
        // double version
        public static double hypot(double aa, double bb){
            double amod=Math.abs(aa);
            double bmod=Math.abs(bb);
            double cc = 0.0D, ratio = 0.0D;
            if(amod==0.0){
                cc=bmod;
            }
            else{
                if(bmod==0.0){
                    cc=amod;
                }
                else{
                    if(amod>=bmod){
                        ratio=bmod/amod;
                        cc=amod*Math.sqrt(1.0 + ratio*ratio);
                    }
                    else{
                        ratio=amod/bmod;
                        cc=bmod*Math.sqrt(1.0 + ratio*ratio);
                    }
                }
            }
            return cc;
        }

        // Returns the length of the hypotenuse of a and b
        // i.e. sqrt(a*a+b*b) [without unecessary overflow or underflow]
        // float version
        public static float hypot(float aa, float bb){
            return (float) hypot((double) aa, (double) bb);
        }

        // Angle (in radians) subtended at coordinate C
        // given x, y coordinates of all apices, A, B and C, of a triangle
        public static double angle(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC){

            double ccos = Fmath.cos(xAtA, yAtA, xAtB, yAtB, xAtC, yAtC);
            return Math.acos(ccos);
        }

        // Angle (in radians) between sides sideA and sideB given all side lengths of a triangle
        public static double angle(double sideAC, double sideBC, double sideAB){

            double ccos = Fmath.cos(sideAC, sideBC, sideAB);
            return Math.acos(ccos);
        }

        // Sine of angle subtended at coordinate C
        // given x, y coordinates of all apices, A, B and C, of a triangle
        public static double sin(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC){
            double angle = Fmath.angle(xAtA, yAtA, xAtB, yAtB, xAtC, yAtC);
            return Math.sin(angle);
        }

        // Sine of angle between sides sideA and sideB given all side lengths of a triangle
        public static double sin(double sideAC, double sideBC, double sideAB){
            double angle = Fmath.angle(sideAC, sideBC, sideAB);
            return Math.sin(angle);
        }

        // Sine given angle in radians
        // for completion - returns Math.sin(arg)
        public static double sin(double arg){
            return Math.sin(arg);
        }

        // Inverse sine
        // Fmath.asin Checks limits - Java Math.asin returns NaN if without limits
        public static double asin(double a){
            if(a<-1.0D && a>1.0D) throw new IllegalArgumentException("Fmath.asin argument (" + a + ") must be >= -1.0 and <= 1.0");
            return Math.asin(a);
        }

        // Cosine of angle subtended at coordinate C
        // given x, y coordinates of all apices, A, B and C, of a triangle
        public static double cos(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC){
            double sideAC = Fmath.hypot(xAtA - xAtC, yAtA - yAtC);
            double sideBC = Fmath.hypot(xAtB - xAtC, yAtB - yAtC);
            double sideAB = Fmath.hypot(xAtA - xAtB, yAtA - yAtB);
            return Fmath.cos(sideAC, sideBC, sideAB);
        }

        // Cosine of angle between sides sideA and sideB given all side lengths of a triangle
        public static double cos(double sideAC, double sideBC, double sideAB){
            return 0.5D*(sideAC/sideBC + sideBC/sideAC - (sideAB/sideAC)*(sideAB/sideBC));
        }

         // Cosine given angle in radians
         // for completion - returns Java Math.cos(arg)
        public static double cos(double arg){
            return Math.cos(arg);
        }

        // Inverse cosine
        // Fmath.asin Checks limits - Java Math.asin returns NaN if without limits
        public static double acos(double a){
            if(a<-1.0D || a>1.0D) throw new IllegalArgumentException("Fmath.acos argument (" + a + ") must be >= -1.0 and <= 1.0");
            return Math.acos(a);
        }

        // Tangent of angle subtended at coordinate C
        // given x, y coordinates of all apices, A, B and C, of a triangle
        public static double tan(double xAtA, double yAtA, double xAtB, double yAtB, double xAtC, double yAtC){
            double angle = Fmath.angle(xAtA, yAtA, xAtB, yAtB, xAtC, yAtC);
            return Math.tan(angle);
        }

        // Tangent of angle between sides sideA and sideB given all side lengths of a triangle
        public static double tan(double sideAC, double sideBC, double sideAB){
            double angle = Fmath.angle(sideAC, sideBC, sideAB);
            return Math.tan(angle);
        }

        // Tangent given angle in radians
        // for completion - returns Math.tan(arg)
        public static double tan(double arg){
            return Math.tan(arg);
        }

        // Inverse tangent
        // for completion - returns Math.atan(arg)
        public static double atan(double a){
            return Math.atan(a);
        }

        // Inverse tangent - ratio numerator and denominator provided
        // for completion - returns Math.atan2(arg)
        public static double atan2(double a, double b){
            return Math.atan2(a, b);
        }

        // Cotangent
        public static double cot(double a){
            return 1.0D/Math.tan(a);
        }

        // Inverse cotangent
        public static double acot(double a){
            return Math.atan(1.0D/a);
        }

        // Inverse cotangent - ratio numerator and denominator provided
        public static double acot2(double a, double b){
            return Math.atan2(b, a);
        }

        // Secant
        public static double sec(double a){
            return 1.0/Math.cos(a);
        }

        // Inverse secant
        public static double asec(double a){
            if(a<1.0D && a>-1.0D) throw new IllegalArgumentException("asec argument (" + a + ") must be >= 1 or <= -1");
            return Math.acos(1.0/a);
        }

        // Cosecant
        public static double csc(double a){
            return 1.0D/Math.sin(a);
        }

        // Inverse cosecant
        public static double acsc(double a){
            if(a<1.0D && a>-1.0D) throw new IllegalArgumentException("acsc argument (" + a + ") must be >= 1 or <= -1");
            return Math.asin(1.0/a);
        }

        // Exsecant
        public static double exsec(double a){
            return (1.0/Math.cos(a)-1.0D);
        }

        // Inverse exsecant
        public static double aexsec(double a){
            if(a<0.0D && a>-2.0D) throw new IllegalArgumentException("aexsec argument (" + a + ") must be >= 0.0 and <= -2");
            return Math.asin(1.0D/(1.0D + a));
        }

        // Versine
        public static double vers(double a){
            return (1.0D - Math.cos(a));
        }

        // Inverse  versine
        public static double avers(double a){
            if(a<0.0D && a>2.0D) throw new IllegalArgumentException("avers argument (" + a + ") must be <= 2 and >= 0");
            return Math.acos(1.0D - a);
        }

        // Coversine
        public static double covers(double a){
            return (1.0D - Math.sin(a));
        }

        // Inverse coversine
        public static double acovers(double a){
            if(a<0.0D && a>2.0D) throw new IllegalArgumentException("acovers argument (" + a + ") must be <= 2 and >= 0");
            return Math.asin(1.0D - a);
        }

        // Haversine
        public static double hav(double a){
            return 0.5D*Fmath.vers(a);
        }

        // Inverse haversine
        public static double ahav(double a){
            if(a<0.0D && a>1.0D) throw new IllegalArgumentException("ahav argument (" + a + ") must be >= 0 and <= 1");
            return Fmath.acos(1.0D - 2.0D*a);
        }

        // Unnormalised sinc (unnormalised sine cardinal)   sin(x)/x
        public static double sinc(double a){
            if(Math.abs(a)<1e-40){
                return 1.0D;
            }
            else{
                return Math.sin(a)/a;
            }
        }

        // Normalised sinc (normalised sine cardinal)  sin(pi.x)/(pi.x)
        public static double nsinc(double a){
            if(Math.abs(a)<1e-40){
                return 1.0D;
            }
            else{
                return Math.sin(Math.PI*a)/(Math.PI*a);
            }
        }

        //Hyperbolic sine of a double number
        public static double sinh(double a){
            return 0.5D*(Math.exp(a)-Math.exp(-a));
        }

        // Inverse hyperbolic sine of a double number
        public static double asinh(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            return sgn*Math.log(a+Math.sqrt(a*a+1.0D));
        }

        //Hyperbolic cosine of a double number
        public static double cosh(double a){
            return 0.5D*(Math.exp(a)+Math.exp(-a));
        }

        // Inverse hyperbolic cosine of a double number
        public static double acosh(double a){
            if(a<1.0D) throw new IllegalArgumentException("acosh real number argument (" + a + ") must be >= 1");
            return Math.log(a+Math.sqrt(a*a-1.0D));
        }

        //Hyperbolic tangent of a double number
        public static double tanh(double a){
            return sinh(a)/cosh(a);
        }

        // Inverse hyperbolic tangent of a double number
        public static double atanh(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            if(a>1.0D) throw new IllegalArgumentException("atanh real number argument (" + sgn*a + ") must be >= -1 and <= 1");
            return 0.5D*sgn*(Math.log(1.0D + a)-Math.log(1.0D - a));
        }

        //Hyperbolic cotangent of a double number
        public static double coth(double a){
            return 1.0D/tanh(a);
        }

        // Inverse hyperbolic cotangent of a double number
        public static double acoth(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            if(a<1.0D) throw new IllegalArgumentException("acoth real number argument (" + sgn*a + ") must be <= -1 or >= 1");
            return 0.5D*sgn*(Math.log(1.0D + a)-Math.log(a - 1.0D));
        }

        //Hyperbolic secant of a double number
        public static double sech(double a){
                return 1.0D/cosh(a);
        }

        // Inverse hyperbolic secant of a double number
        public static double asech(double a){
            if(a>1.0D || a<0.0D) throw new IllegalArgumentException("asech real number argument (" + a + ") must be >= 0 and <= 1");
            return 0.5D*(Math.log(1.0D/a + Math.sqrt(1.0D/(a*a) - 1.0D)));
        }

        //Hyperbolic cosecant of a double number
        public static double csch(double a){
                return 1.0D/sinh(a);
        }

        // Inverse hyperbolic cosecant of a double number
        public static double acsch(double a){
            double sgn = 1.0D;
            if(a<0.0D){
                sgn = -1.0D;
                a = -a;
            }
            return 0.5D*sgn*(Math.log(1.0/a + Math.sqrt(1.0D/(a*a) + 1.0D)));
        }

    // MANTISSA ROUNDING (TRUNCATING)
    // returns a value of xDouble truncated to trunc decimal places
    public static double truncate(double xDouble, int trunc){
        double xTruncated = xDouble;
        if(!Fmath.isNaN(xDouble)){
            if(!Fmath.isPlusInfinity(xDouble)){
                if(!Fmath.isMinusInfinity(xDouble)){
                    if(xDouble!=0.0D){
                        String xString = ((new Double(xDouble)).toString()).trim();
                        xTruncated = Double.parseDouble(truncateProcedure(xString, trunc));
                    }
                }
            }
        }
        return xTruncated;
    }

    // returns a value of xFloat truncated to trunc decimal places
    public static float truncate(float xFloat, int trunc){
        float xTruncated = xFloat;
        if(!Fmath.isNaN(xFloat)){
            if(!Fmath.isPlusInfinity(xFloat)){
                if(!Fmath.isMinusInfinity(xFloat)){
                    if(xFloat!=0.0D){
                        String xString = ((new Float(xFloat)).toString()).trim();
                        xTruncated = Float.parseFloat(truncateProcedure(xString, trunc));
                    }
                }
            }
        }
        return xTruncated;
    }

    // private method for truncating a float or double expressed as a String
    private static String truncateProcedure(String xValue, int trunc){

        String xTruncated = xValue;
        String xWorking = xValue;
        String exponent = " ";
        String first = "+";
        int expPos = xValue.indexOf('E');
        int dotPos = xValue.indexOf('.');
        int minPos = xValue.indexOf('-');

        if(minPos!=-1){
            if(minPos==0){
                xWorking = xWorking.substring(1);
                first = "-";
                dotPos--;
                expPos--;
            }
        }
        if(expPos>-1){
            exponent = xWorking.substring(expPos);
            xWorking = xWorking.substring(0,expPos);
        }
        String xPreDot = null;
        String xPostDot = "0";
        String xDiscarded = null;
        String tempString = null;
        double tempDouble = 0.0D;
        if(dotPos>-1){
            xPreDot = xWorking.substring(0,dotPos);
            xPostDot = xWorking.substring(dotPos+1);
            int xLength = xPostDot.length();
            if(trunc<xLength){
                xDiscarded = xPostDot.substring(trunc);
                tempString = xDiscarded.substring(0,1) + ".";
                if(xDiscarded.length()>1){
                    tempString += xDiscarded.substring(1);
                }
                else{
                    tempString += "0";
                }
                tempDouble = Math.round(Double.parseDouble(tempString));

                if(trunc>0){
                    if(tempDouble>=5.0){
                        int[] xArray = new int[trunc+1];
                        xArray[0] = 0;
                        for(int i=0; i<trunc; i++){
                            xArray[i+1] = Integer.parseInt(xPostDot.substring(i,i+1));
                        }
                        boolean test = true;
                        int iCounter = trunc;
                        while(test){
                            xArray[iCounter] += 1;
                            if(iCounter>0){
                                if(xArray[iCounter]<10){
                                    test = false;
                                }
                                else{
                                    xArray[iCounter]=0;
                                    iCounter--;
                                }
                            }
                            else{
                                test = false;
                            }
                        }
                        int preInt = Integer.parseInt(xPreDot);
                        preInt += xArray[0];
                        xPreDot = (new Integer(preInt)).toString();
                        tempString = "";
                        for(int i=1; i<=trunc; i++){
                            tempString += (new Integer(xArray[i])).toString();
                        }
                        xPostDot = tempString;
                    }
                    else{
                        xPostDot = xPostDot.substring(0, trunc);
                    }
                }
                else{
                    if(tempDouble>=5.0){
                        int preInt = Integer.parseInt(xPreDot);
                        preInt++;
                        xPreDot = (new Integer(preInt)).toString();
                    }
                    xPostDot = "0";
                }
            }
            xTruncated = first + xPreDot.trim() + "." + xPostDot.trim() + exponent;
        }
        return xTruncated.trim();
    }

        // Returns true if x is infinite, i.e. is equal to either plus or minus infinity
        // x is double
        public static boolean isInfinity(double x){
            boolean test=false;
            if(x==Double.POSITIVE_INFINITY || x==Double.NEGATIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is infinite, i.e. is equal to either plus or minus infinity
        // x is float
        public static boolean isInfinity(float x){
            boolean test=false;
            if(x==Float.POSITIVE_INFINITY || x==Float.NEGATIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is plus infinity
        // x is double
        public static boolean isPlusInfinity(double x){
            boolean test=false;
            if(x==Double.POSITIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is plus infinity
        // x is float
        public static boolean isPlusInfinity(float x){
            boolean test=false;
            if(x==Float.POSITIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is minus infinity
        // x is double
        public static boolean isMinusInfinity(double x){
            boolean test=false;
            if(x==Double.NEGATIVE_INFINITY)test=true;
            return test;
        }

        // Returns true if x is minus infinity
        // x is float
        public static boolean isMinusInfinity(float x){
            boolean test=false;
            if(x==Float.NEGATIVE_INFINITY)test=true;
            return test;
        }


        // Returns true if x is 'Not a Number' (NaN)
        // x is double
        public static boolean isNaN(double x){
            boolean test=false;
            if(x!=x)test=true;
            return test;
        }

        // Returns true if x is 'Not a Number' (NaN)
        // x is float
        public static boolean isNaN(float x){
            boolean test=false;
            if(x!=x)test=true;
            return test;
        }

        // Returns true if x equals y
        // x and y are double
        // x may be float within range, PLUS_INFINITY, NEGATIVE_INFINITY, or NaN
        // NB!! This method treats two NaNs as equal
        public static boolean isEqual(double x, double y){
            boolean test=false;
            if(Fmath.isNaN(x)){
                if(Fmath.isNaN(y))test=true;
            }
            else{
                if(Fmath.isPlusInfinity(x)){
                    if(Fmath.isPlusInfinity(y))test=true;
                }
                else{
                    if(Fmath.isMinusInfinity(x)){
                        if(Fmath.isMinusInfinity(y))test=true;
                    }
                    else{
                        if(x==y)test=true;
                    }
                }
            }
            return test;
        }

        // Returns true if x equals y
        // x and y are float
        // x may be float within range, PLUS_INFINITY, NEGATIVE_INFINITY, or NaN
        // NB!! This method treats two NaNs as equal
        public static boolean isEqual(float x, float y){
            boolean test=false;
            if(Fmath.isNaN(x)){
                if(Fmath.isNaN(y))test=true;
            }
            else{
                if(Fmath.isPlusInfinity(x)){
                    if(Fmath.isPlusInfinity(y))test=true;
                }
                else{
                    if(Fmath.isMinusInfinity(x)){
                        if(Fmath.isMinusInfinity(y))test=true;
                    }
                    else{
                        if(x==y)test=true;
                    }
                }
            }
            return test;
        }

        // Returns true if x equals y
        // x and y are int
        public static boolean isEqual(int x, int y){
            boolean test=false;
            if(x==y)test=true;
            return test;
        }

        // Returns true if x equals y
        // x and y are char
        public static boolean isEqual(char x, char y){
            boolean test=false;
            if(x==y)test=true;
            return test;
        }

        // Returns true if x equals y
        // x and y are Strings
        public static boolean isEqual(String x, String y){
            boolean test=false;
            if(x.equals(y))test=true;
            return test;
        }

        // IS EQUAL WITHIN LIMITS
        // Returns true if x equals y within limits plus or minus limit
        // x and y are double
        public static boolean isEqualWithinLimits(double x, double y, double limit){
            boolean test=false;
            if(Math.abs(x-y)<=Math.abs(limit))test=true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are float
        public static boolean isEqualWithinLimits(float x, float y, float limit){
            boolean test=false;
            if(Math.abs(x-y)<=Math.abs(limit))test=true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are long
        public static boolean isEqualWithinLimits(long x, long y, long limit){
            boolean test=false;
            if(Math.abs(x-y)<=Math.abs(limit))test=true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are int
        public static boolean isEqualWithinLimits(int x, int y, int limit){
            boolean test=false;
            if(Math.abs(x-y)<=Math.abs(limit))test=true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are BigDecimal
        public static boolean isEqualWithinLimits(BigDecimal x, BigDecimal y, BigDecimal limit){
            boolean test=false;
            if(((x.subtract(y)).abs()).compareTo(limit.abs())<=0)test = true;
            return test;
        }

        // Returns true if x equals y within limits plus or minus limit
        // x and y are BigInteger
        public static boolean isEqualWithinLimits(BigInteger x, BigInteger y, BigInteger limit){
            boolean test=false;
            if(((x.subtract(y)).abs()).compareTo(limit.abs())<=0)test = true;
            return test;
        }


        // IS EQUAL WITHIN A PERCENTAGE
        // Returns true if x equals y within a percentage of the mean
        // x and y are double
        public static boolean isEqualWithinPerCent(double x, double y, double perCent){
            boolean test=false;
            double limit = Math.abs((x+y)*perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are float
        public static boolean isEqualWithinPerCent(float x, float y, float perCent){
            boolean test=false;
            double limit = Math.abs((x+y)*perCent/200.0F);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are long, percentage provided as double
        public static boolean isEqualWithinPerCent(long x, long y, double perCent){
            boolean test=false;
            double limit = Math.abs((x+y)*perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are long, percentage provided as int
        public static boolean isEqualWithinPerCent(long x, long y, long perCent){
            boolean test=false;
            double limit = Math.abs((double)(x+y)*(double)perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are int, percentage provided as double
        public static boolean isEqualWithinPerCent(int x, int y, double perCent){
            boolean test=false;
            double limit = Math.abs((double)(x+y)*perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are int, percentage provided as int
        public static boolean isEqualWithinPerCent(int x, int y, int perCent){
            boolean test=false;
            double limit = Math.abs((double)(x+y)*(double)perCent/200.0D);
            if(Math.abs(x-y)<=limit)test=true;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are BigDecimal
        public static boolean isEqualWithinPerCent(BigDecimal x, BigDecimal y, BigDecimal perCent){
            boolean test=false;
            BigDecimal limit = (x.add(y)).multiply(perCent).multiply(new BigDecimal("0.005"));
            if(((x.subtract(y)).abs()).compareTo(limit.abs())<=0)test = true;
            limit = null;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are BigDInteger, percentage provided as BigDecimal
        public static boolean isEqualWithinPerCent(BigInteger x, BigInteger y, BigDecimal perCent){
            boolean test=false;
            BigDecimal xx = new BigDecimal(x);
            BigDecimal yy = new BigDecimal(y);
            BigDecimal limit = (xx.add(yy)).multiply(perCent).multiply(new BigDecimal("0.005"));
            if(((xx.subtract(yy)).abs()).compareTo(limit.abs())<=0)test = true;
            limit = null;
            xx = null;
            yy = null;
            return test;
        }

        // Returns true if x equals y within a percentage of the mean
        // x and y are BigDInteger, percentage provided as BigInteger
        public static boolean isEqualWithinPerCent(BigInteger x, BigInteger y, BigInteger perCent){
            boolean test=false;
            BigDecimal xx = new BigDecimal(x);
            BigDecimal yy = new BigDecimal(y);
            BigDecimal pc = new BigDecimal(perCent);
            BigDecimal limit = (xx.add(yy)).multiply(pc).multiply(new BigDecimal("0.005"));
            if(((xx.subtract(yy)).abs()).compareTo(limit.abs())<=0)test = true;
            limit = null;
            xx = null;
            yy = null;
            pc = null;
            return test;
        }

        // COMPARISONS
        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are double
        public static int compare(double x, double y){
            Double X = new Double(x);
            Double Y = new Double(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are int
        public static int compare(int x, int y){
            Integer X = new Integer(x);
            Integer Y = new Integer(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are long
        public static int compare(long x, long y){
            Long X = new Long(x);
            Long Y = new Long(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are float
        public static int compare(float x, float y){
            Float X = new Float(x);
            Float Y = new Float(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are short
        public static int compare(byte x, byte y){
            Byte X = new Byte(x);
            Byte Y = new Byte(y);
            return X.compareTo(Y);
        }

        // Returns 0 if x == y
        // Returns -1 if x < y
        // Returns 1 if x > y
        // x and y are short
        public static int compare(short x, short y){
            Short X = new Short(x);
            Short Y = new Short(y);
            return X.compareTo(Y);
        }

        // IS AN INTEGER
        // Returns true if x is, arithmetically, an integer
        // Returns false if x is not, arithmetically, an integer
        public static boolean isInteger(double x){
            boolean retn = false;
            double xfloor = Math.floor(x);
            if((x - xfloor)==0.0D) retn = true;
            return retn;
        }

        // Returns true if all elements in the array x are, arithmetically, integers
        // Returns false if any element in the array x is not, arithmetically, an integer
        public static boolean isInteger(double[] x){
            boolean retn = true;
            boolean test = true;
            int ii = 0;
            while(test){
                double xfloor = Math.floor(x[ii]);
                if((x[ii] - xfloor)!=0.0D){
                    retn = false;
                    test = false;
                }
                else{
                    ii++;
                    if(ii==x.length)test=false;
                }
            }
            return retn;
        }

        // Returns true if x is, arithmetically, an integer
        // Returns false if x is not, arithmetically, an integer
        public static boolean isInteger(float x){
            boolean ret = false;
            float xfloor = (float)Math.floor(x);
            if((x - xfloor)==0.0F) ret = true;
            return ret;
        }


        // Returns true if all elements in the array x are, arithmetically, integers
        // Returns false if any element in the array x is not, arithmetically, an integer
        public static boolean isInteger(float[] x){
            boolean retn = true;
            boolean test = true;
            int ii = 0;
            while(test){
                float xfloor = (float)Math.floor(x[ii]);
                if((x[ii] - xfloor)!=0.0D){
                    retn = false;
                    test = false;
                }
                else{
                    ii++;
                    if(ii==x.length)test=false;
                }
            }
            return retn;
        }

        public static boolean isInteger (Number numberAsObject){
            boolean test = integers.containsKey(numberAsObject.getClass());
            if(!test){
                if(numberAsObject instanceof Double){
                    double dd = numberAsObject.doubleValue();
                    test = Fmath.isInteger(dd);
                }
                if(numberAsObject instanceof Float){
                    float dd = numberAsObject.floatValue();
                    test = Fmath.isInteger(dd);
                }
                if(numberAsObject instanceof BigDecimal){
                    double dd = numberAsObject.doubleValue();
                    test = Fmath.isInteger(dd);
                }
            }
            return test;
        }

        public static boolean isInteger (Number[] numberAsObject){
            boolean testall = true;
            for(int i=0; i<numberAsObject.length; i++){
                boolean test = integers.containsKey(numberAsObject[i].getClass());
                if(!test){
                    if(numberAsObject[i] instanceof Double){
                        double dd = numberAsObject[i].doubleValue();
                        test = Fmath.isInteger(dd);
                        if(!test)testall = false;
                    }
                    if(numberAsObject[i] instanceof Float){
                        float dd = numberAsObject[i].floatValue();
                        test = Fmath.isInteger(dd);
                        if(!test)testall = false;
                    }
                    if(numberAsObject[i] instanceof BigDecimal){
                        double dd = numberAsObject[i].doubleValue();
                        test = Fmath.isInteger(dd);
                        if(!test)testall = false;
                    }
                }
            }
            return testall;
        }

        // IS EVEN
        // Returns true if x is an even number, false if x is an odd number
        // x is int
        public static boolean isEven(int x){
            boolean test=false;
            if(x%2 == 0.0D)test=true;
            return test;
        }

        // Returns true if x is an even number, false if x is an odd number
        // x is float but must hold an integer value
        public static boolean isEven(float x){
            double y=Math.floor(x);
            if(((double)x - y)!= 0.0D)throw new IllegalArgumentException("the argument is not an integer");
            boolean test=false;
            y=Math.floor(x/2.0F);
            if(((double)(x/2.0F)-y) == 0.0D)test=true;
            return test;
        }

        // Returns true if x is an even number, false if x is an odd number
        // x is double but must hold an integer value
        public static boolean isEven(double x){
            double y=Math.floor(x);
            if((x - y)!= 0.0D)throw new IllegalArgumentException("the argument is not an integer");
            boolean test=false;
            y=Math.floor(x/2.0F);
            if((x/2.0D-y) == 0.0D)test=true;
            return test;
        }

        // IS ODD
        // Returns true if x is an odd number, false if x is an even number
        // x is int
        public static boolean isOdd(int x){
            boolean test=true;
            if(x%2 == 0.0D)test=false;
            return test;
        }

        // Returns true if x is an odd number, false if x is an even number
        // x is float but must hold an integer value
        public static boolean isOdd(float x){
            double y=Math.floor(x);
            if(((double)x - y)!= 0.0D)throw new IllegalArgumentException("the argument is not an integer");
            boolean test=true;
            y=Math.floor(x/2.0F);
            if(((double)(x/2.0F)-y) == 0.0D)test=false;
            return test;
        }

        // Returns true if x is an odd number, false if x is an even number
        // x is double but must hold an integer value
        public static boolean isOdd(double x){
            double y=Math.floor(x);
            if((x - y)!= 0.0D)throw new IllegalArgumentException("the argument is not an integer");
            boolean test=true;
            y=Math.floor(x/2.0F);
            if((x/2.0D-y) == 0.0D)test=false;
            return test;
        }

        // LEAP YEAR
        // Returns true if year (argument) is a leap year
        public static boolean leapYear(int year){
            boolean test = false;

            if(year%4 != 0){
                 test = false;
            }
            else{
                if(year%400 == 0){
                    test=true;
                }
                else{
                    if(year%100 == 0){
                        test=false;
                    }
                    else{
                        test=true;
                    }
                }
            }
            return test;
        }

        // COMPUTER TIME
        // Returns milliseconds since 0 hours 0 minutes 0 seconds on 1 Jan 1970
        public static long dateToJavaMilliS(int year, int month, int day, int hour, int min, int sec){

            long[] monthDays = {0L, 31L, 28L, 31L, 30L, 31L, 30L, 31L, 31L, 30L, 31L, 30L, 31L};
            long ms = 0L;

            long yearDiff = 0L;
            int yearTest = year-1;
            while(yearTest>=1970){
                yearDiff += 365;
                if(Fmath.leapYear(yearTest))yearDiff++;
                yearTest--;
            }
            yearDiff *= 24L*60L*60L*1000L;

            long monthDiff = 0L;
            int monthTest = month -1;
            while(monthTest>0){
                monthDiff += monthDays[monthTest];
                if(Fmath.leapYear(year))monthDiff++;
                monthTest--;
            }

            monthDiff *= 24L*60L*60L*1000L;

            ms = yearDiff + monthDiff + day*24L*60L*60L*1000L + hour*60L*60L*1000L + min*60L*1000L + sec*1000L;

            return ms;
        }

        // DEPRECATED METHODS
        // Several methods have been revised and moved to classes ArrayMaths, Conv or PrintToScreen

        // ARRAY MAXIMUM  (deprecated - see ArryMaths class)
        // Maximum of a 1D array of doubles, aa
        public static double maximum(double[] aa){
            int n = aa.length;
            double aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Maximum of a 1D array of floats, aa
        public static float maximum(float[] aa){
            int n = aa.length;
            float aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Maximum of a 1D array of ints, aa
        public static int maximum(int[] aa){
            int n = aa.length;
            int aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Maximum of a 1D array of longs, aa
        public static long maximum(long[] aa){
            long n = aa.length;
            long aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Minimum of a 1D array of doubles, aa
        public static double minimum(double[] aa){
            int n = aa.length;
            double aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // Minimum of a 1D array of floats, aa
        public static float minimum(float[] aa){
            int n = aa.length;
            float aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // ARRAY MINIMUM (deprecated - see ArryMaths class)
        // Minimum of a 1D array of ints, aa
        public static int minimum(int[] aa){
            int n = aa.length;
            int aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // Minimum of a 1D array of longs, aa
        public static long minimum(long[] aa){
            long n = aa.length;
            long aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // MAXIMUM DISTANCE BETWEEN ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Maximum distance between elements of a 1D array of doubles, aa
        public static double maximumDifference(double[] aa){
            return Fmath.maximum(aa) - Fmath.minimum(aa);
        }

        // Maximum distance between elements of a 1D array of floats, aa
        public static float maximumDifference(float[] aa){
            return Fmath.maximum(aa) - Fmath.minimum(aa);
        }

        // Maximum distance between elements of a 1D array of long, aa
        public static long maximumDifference(long[] aa){
            return Fmath.maximum(aa) - Fmath.minimum(aa);
        }

        // Maximum distance between elements of a 1D array of ints, aa
        public static int maximumDifference(int[] aa){
            return Fmath.maximum(aa) - Fmath.minimum(aa);
        }


        // MINIMUM DISTANCE BETWEEN ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Minimum distance between elements of a 1D array of doubles, aa
        public static double minimumDifference(double[] aa){
            double[] sorted = Fmath.selectionSort(aa);
            double n = aa.length;
            double diff = sorted[1] - sorted[0];
            double minDiff = diff;
            for(int i=1; i<n-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
            return minDiff;
        }

        // Minimum distance between elements of a 1D array of floats, aa
        public static float minimumDifference(float[] aa){
            float[] sorted = Fmath.selectionSort(aa);
            float n = aa.length;
            float diff = sorted[1] - sorted[0];
            float minDiff = diff;
            for(int i=1; i<n-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
            return minDiff;
        }

        // Minimum distance between elements of a 1D array of longs, aa
        public static long minimumDifference(long[] aa){
            long[] sorted = Fmath.selectionSort(aa);
            long n = aa.length;
            long diff = sorted[1] - sorted[0];
            long minDiff = diff;
            for(int i=1; i<n-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
            return minDiff;
        }

        // Minimum distance between elements of a 1D array of ints, aa
        public static int minimumDifference(int[] aa){
            int[] sorted = Fmath.selectionSort(aa);
            int n = aa.length;
            int diff = sorted[1] - sorted[0];
            int minDiff = diff;
            for(int i=1; i<n-1; i++){
                diff = sorted[i+1] - sorted[i];
                if(diff<minDiff)minDiff = diff;
            }
            return minDiff;
        }

        // REVERSE ORDER OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Reverse the order of the elements of a 1D array of doubles, aa
        public static double[] reverseArray(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // Reverse the order of the elements of a 1D array of floats, aa
        public static float[] reverseArray(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // Reverse the order of the elements of a 1D array of ints, aa
        public static int[] reverseArray(int[] aa){
            int n = aa.length;
            int[] bb = new int[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // Reverse the order of the elements of a 1D array of longs, aa
        public static long[] reverseArray(long[] aa){
            int n = aa.length;
            long[] bb = new long[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // Reverse the order of the elements of a 1D array of char, aa
        public static char[] reverseArray(char[] aa){
            int n = aa.length;
            char[] bb = new char[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[n-1-i];
            }
            return bb;
        }

        // ABSOLUTE VALUE OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // return absolute values of an array of doubles
        public static double[] arrayAbs(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = Math.abs(aa[i]);
            }
            return bb;
        }

        // return absolute values of an array of floats
        public static float[] arrayAbs(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++){
               bb[i] = Math.abs(aa[i]);
            }
            return bb;
        }

        // return absolute values of an array of long
        public static long[] arrayAbs(long[] aa){
            int n = aa.length;
            long[] bb = new long[n];
            for(int i=0; i<n; i++){
               bb[i] = Math.abs(aa[i]);
            }
            return bb;
        }

        // return absolute values of an array of int
        public static int[] arrayAbs(int[] aa){
            int n = aa.length;
            int[] bb = new int[n];
            for(int i=0; i<n; i++){
               bb[i] = Math.abs(aa[i]);
            }
            return bb;
        }

        // MULTIPLY ARRAY ELEMENTS BY A CONSTANT  (deprecated - see ArryMaths class)
        // multiply all elements by a constant double[] by double -> double[]
        public static double[] arrayMultByConstant(double[] aa, double constant){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[i]*constant;
            }
            return bb;
        }

        // multiply all elements by a constant int[] by double -> double[]
        public static double[] arrayMultByConstant(int[] aa, double constant){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = (double)aa[i]*constant;
            }
            return bb;
        }
        // multiply all elements by a constant double[] by int -> double[]
        public static double[] arrayMultByConstant(double[] aa, int constant){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = aa[i]*(double)constant;
            }
            return bb;
        }

        // multiply all elements by a constant int[] by int -> double[]
        public static double[] arrayMultByConstant(int[] aa, int constant){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++){
               bb[i] = (double)(aa[i]*constant);
            }
            return bb;
        }

        // LOG10 OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Log to base 10 of all elements of an array of doubles
        public static double[] log10Elements(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.log10(aa[i]);
            return bb;
        }

         // Log to base 10 of all elements of an array of floats
        public static float[] log10Elements(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.log10(aa[i]);
            return bb;
        }

        // NATURAL LOG OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Log to base e of all elements of an array of doubles
        public static double[] lnElements(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.log10(aa[i]);
            return bb;
        }

         // Log to base e of all elements of an array of floats
        public static float[] lnElements(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.log10(aa[i]);
            return bb;
        }

        // SQUARE ROOT OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Square root all elements of an array of doubles
        public static double[] squareRootElements(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.sqrt(aa[i]);
            return bb;
        }

         // Square root all elements of an array of floats
        public static float[] squareRootElements(float[] aa){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.sqrt(aa[i]);
            return bb;
        }

        // POWER OF ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // Raise all elements of an array of doubles to a double power
        public static double[] raiseElementsToPower(double[] aa, double power){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.pow(aa[i], power);
            return bb;
        }

        // Raise all elements of an array of doubles to an int power
        public static double[] raiseElementsToPower(double[] aa, int power){
            int n = aa.length;
            double[] bb = new double[n];
            for(int i=0; i<n; i++)bb[i] = Math.pow(aa[i], power);
            return bb;
        }

        // Raise all elements of an array of floats to a float power
        public static float[] raiseElementsToPower(float[] aa, float power){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.pow(aa[i], power);
            return bb;
        }

        // Raise all elements of an array of floats to an int power
        public static float[] raiseElementsToPower(float[] aa, int power){
            int n = aa.length;
            float[] bb = new float[n];
            for(int i=0; i<n; i++)bb[i] = (float)Math.pow(aa[i], power);
            return bb;
        }

        // INVERT ARRAY ELEMENTS  (deprecated - see ArryMaths class)
        // invert all elements of an array of doubles
        public static double[] invertElements(double[] aa){
            int n = aa.length;
            double[] bb = new double[n];
            for(
