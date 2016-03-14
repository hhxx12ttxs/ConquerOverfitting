/* FFT.java
 * Created on July 1, 2007, 9:53 PM
 */

package OnePopulation2DFFTOptimized;

import math.complex;

/*************************************************************************
 *  Compilation:  javac FFT.java
 *  Execution:    java FFT N
 *  Dependencies: complex.java
 *
 *  Compute the FFT and inverse FFT of a length N complex sequence.
 *  Bare bones implementation that runs in O(N log N) time.
 *
 *  I didnt write this, though I modified it, should find a faster version.
 *
 *  Limitations
 *  -----------
 *   * assumes N is a power of 2
 *   * not the most memory efficient algorithm
 *  
 *************************************************************************/

public class FFT {

    // compute the FFT of x[], assuming its length is a power of 2
    public static complex[] fft(int[] x) {
        complex[] C = new complex[x.length];
        for (int i = 0; i < x.length; i++) C[i] = new complex(x[i]);
        return fft(C);
    }

    // compute the FFT of x[], assuming its length is a power of 2
    public static complex[] fft(complex[] x)
    {
        int N = x.length;
        int Nover2 = N >> 1 ;
        double NegativeTwoPIoverN = -2 * Math.PI / N ;

        complex[] y = new complex[N];

        // base case
        if (N == 1) {
            y[0] = x[0];
            return y;
        }

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) throw new RuntimeException("N is not a power of 2");
        complex[] even = new complex[N/2];
        complex[] odd  = new complex[N/2];
        for (int k = 0; k < N/2; k++) even[k] = x[2*k];
        for (int k = 0; k < N/2; k++) odd[k]  = x[2*k + 1];

        complex[] q = fft(even);
        complex[] r = fft(odd);

        for (int k = 0; k < N/2; k++)
        {
            double kth = k * NegativeTwoPIoverN;
            complex wk = new complex( Math.cos(kth), Math.sin(kth));
            y[k]          = q[k].plus (wk.times(r[k]));
            y[k + Nover2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }



    /// compute the FFT of x[], assuming its length is a power of 2
    /// we assume that the double [] contains packed complex numbers
    /// alternating real and imaginary values
    public static double[] fft(double[] x)
    {

        final int length = x.length ;
        final int N = length >> 1;
        final int Nover2 = N >> 1 ;
        final double NegativePIoverN = - Math.PI / N ;

        double[] y = new double[length];

        // base case
        if (N == 1) {
            y[0] = x[0];
            y[1] = x[1];
            return y;
        }

        double[] even = new double[N];
        double[] odd  = new double[N];

        for (int kR = 0; kR < N; kR+=2)
        {
            int kI = kR|1 ;
            int kR2 = kR<<1;
            even[kR] = x[kR2];
            even[kI] = x[kR2|1];
            int kI2 = kI<<1;
            odd [kR] = x[kI2];
            odd [kI] = x[kI2|1];
        }

        double[] q = fft(even);
        double[] r = fft(odd);

        for (int kR = 0; kR < N; kR+=2)
        {
            double kth = kR * NegativePIoverN;
            double real   = Math.cos(kth) ;
            double imag   = Math.sin(kth) ;

            int kI = kR | 1 ;
            double nreal  = r[kR] ;
            double nimag  = r[kI] ;

            double x4 = real * nreal ;
            double x5 = imag * nimag ;
            double kkimag = ( real + imag ) * ( nreal + nimag ) - x4 - x5 ;
            double kkreal = x4 - x5 ;

            double qkreal = q[kR];
            double qkimag = q[kI];
            y[kR]  = qkreal + kkreal ;
            y[kI]  = qkimag + kkimag ;

            int knR  = kR+N ;
            y[knR]   = qkreal - kkreal ;
            y[knR|1] = qkimag - kkimag ;
        }
        return y;
    }


    /// compute the FFT of x[], assuming its length is a power of 2
    /// we assume that the double [] contains packed complex numbers
    /// alternating real and imaginary values
    /// start = 0 ; skip = 1 ; elem = x.length / 2 
    /// is equivalent to fft(double[] x);
    public static double[] fft(double[] x, int start, int skip, int elem)
    {

        final int length = x.length ;
        final int N = length >> 1;
        final int Nover2 = N >> 1 ;
        final double NegativePIoverN = - Math.PI / N ;

        double[] y = new double[length];

        // base case
        if (N == 1) {
            y[0] = x[0];
            y[1] = x[1];
            return y;
        }

        double[] even = new double[N];
        double[] odd  = new double[N];

        for (int kR = 0; kR < N; kR+=2)
        {
            int kI = kR|1 ;
            int kR2 = kR<<1;
            even[kR] = x[kR2];
            even[kI] = x[kR2|1];
            int kI2 = kI<<1;
            odd [kR] = x[kI2];
            odd [kI] = x[kI2|1];
        }

        double[] q = fft(even);
        double[] r = fft(odd);

        for (int kR = 0; kR < N; kR+=2)
        {
            double kth = kR * NegativePIoverN;
            double real   = Math.cos(kth) ;
            double imag   = Math.sin(kth) ;

            int kI = kR | 1 ;
            double nreal  = r[kR] ;
            double nimag  = r[kI] ;

            double x4 = real * nreal ;
            double x5 = imag * nimag ;
            double kkimag = ( real + imag ) * ( nreal + nimag ) - x4 - x5 ;
            double kkreal = x4 - x5 ;

            double qkreal = q[kR];
            double qkimag = q[kI];
            y[kR]  = qkreal + kkreal ;
            y[kI]  = qkimag + kkimag ;

            int knR  = kR+N ;
            y[knR]   = qkreal - kkreal ;
            y[knR|1] = qkimag - kkimag ;
        }
        return y;
    }





    /// compute the FFT of x[], assuming its length is a power of 2
    /// we assume that the double [] contains only real numbers
    // returns an even/odd packed complex array as a double [][]
    public static double[] fftOnlyReal(double[] x)
    {
        final int length = x.length*2 ;
        final int N = x.length ;
        final int Nover2 = N >> 1 ;
        final double NegativePIoverN = - Math.PI / N ;

        double[] y = new double[length];

        // base case
        if (N == 1) {
            y[0] = x[0];
            y[1] = 0;
            return y;
        }

        double[] even = new double[Nover2];
        double[] odd  = new double[Nover2];

        for (int k = 0; k < Nover2; k++)
        {
            even[k] = x[k<<1];
            odd [k] = x[(k<<1)|1];
        }

        double[] q = fftOnlyReal(even);
        double[] r = fftOnlyReal(odd);

        // x, even, odd are real only
        // q, r, and y are all complex packed

        for (int kR = 0; kR < N; kR+=2)
        {
            double kth = kR * NegativePIoverN;
            double real   = Math.cos(kth) ;
            double imag   = Math.sin(kth) ;

            int kI = kR | 1 ;
            double nreal  = r[kR] ;
            double nimag  = r[kI] ;

            double x4 = real * nreal ;
            double x5 = imag * nimag ;
            double kkimag = ( real + imag ) * ( nreal + nimag ) - x4 - x5 ;
            double kkreal = x4 - x5 ;

            double qkreal = q[kR];
            double qkimag = q[kI];
            y[kR]  = qkreal + kkreal ;
            y[kI]  = qkimag + kkimag ;

            int knR  = kR+N ;
            y[knR]   = qkreal - kkreal ;
            y[knR|1] = qkimag - kkimag ;
        }
        return y;
    }


    // compute the inverse FFT of x[], assuming its length is a power of 2
    public static complex[] ifft(complex[] x) {
        int N = x.length;

        // take conjugate
        for (int i = 0; i < N; i++)
            x[i] = complex.conj(x[i]);

        // compute forward FFT
        complex[] y = fft(x);

        // take conjugate again
        for (int i = 0; i < N; i++)
            y[i] = complex.conj(y[i]);

        // divide by N
        double overN = 1.0/N ;
        for (int i = 0; i < N; i++)
            y[i] = y[i].scale(overN);

        return y;
    }

    // compute the inverse FFT of x[], assuming its length is a power of 2
    public static double[] ifft(double[] x)
    {
        int N = x.length >> 1;

        // take conjugate
        for (int i = 1; i < 2*N; i+=2)
        {
            x[i] = -x[i];
        }

        // compute forward FFT
        double[] y = fft(x);

        // take conjugate again
        // divide by N
        double overN = 1.0/N ;
        for (int i = 0; i < N; i++)
        {
            int kR = i<<1 ;
            int kI = kR|1 ;
            y[kI]  = -y[kI]*overN;
            y[kR] *= overN;
        }

        return y;

    }

    // compute the convolution of x and y
    // crazy fun
    public static complex[] convolve(complex[] x, complex[] y) {
        if (x.length != y.length) throw new RuntimeException("Dimensions don't agree");
        int N = x.length;

        // compute FFT of each sequence
        complex[] a = fft(x);
        complex[] b = fft(y);

        // point-wise multiply
        complex[] c = new complex[N];
        for (int i = 0; i < N; i++)
            c[i] = a[i].times(b[i]);

        // compute inverse FFT
        return ifft(c);
    }



    // test client
    public static void main(String[] args) {
        /*
        int N = 512 ;//Integer.parseInt(args[0]);
        complex[] x = new complex[N];

        // original data
        for (int i = 0; i < N; i++) {
            x[i] = new complex(i, 0);
        }
        for (int i = 0; i < N; i++)
            System.out.println(x[i]);
        System.out.println();

        // FFT of original data
        complex[] y = fft(x);
        for (int i = 0; i < N; i++)
            System.out.println(y[i]);
        System.out.println("\bINVERTING:\n");

        // take inverse FFT
        complex[] z = ifft(y);
        for (int i = 0; i < N; i++)
            System.out.println(z[i]);
        System.out.println();

        // convolution of x with itself
        complex[] c = convolve(x, x);
        for (int i = 0; i < N; i++)
            System.out.println(c[i]);*/


        int N = 16 ;//Integer.parseInt(args[0]);
        double[] x = new double[N*2];

        // original data
        for (int i = 0; i < N; i++) {
            x[i*2  ] = i;
        }
        
        for (int i = 0; i < N; i++)
            System.out.println(x[i*2]);
        System.out.println();
        

        // FFT of original data
        double[] y = fft(x);
        for (int i = 0; i < N; i++)
            System.out.println(y[i*2]+"+i*"+y[i*2+1]);
        System.out.println("\bINVERTING:\n");

        
        // take inverse FFT
        double[] z = ifft(y);
        for (int i = 0; i < N; i++)
            System.out.println(z[i*2]);
        System.out.println();

        /*
        // convolution of x with itself
        //complex[] c = convolve(x, x);
        //for (int i = 0; i < N; i++)
        //    System.out.println(c[i]);
        */
    }

}


