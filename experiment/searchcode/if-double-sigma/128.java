/* FFT2D.java
 * Created on July 1, 2007, 10:21 PM
 */

package OnePopulation2DFFTOptimized;


import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import javax.swing.JFrame;
import math.complex;
import util.ComplexRGBTexture;
import util.SimpleImageIO;

import static java.lang.Math.* ;

/**
 * @author Michael Everett Rule
 */
public class FFT2D {
    
    private FFT2D() {}
    
    public static ComplexRGBTexture FFT( BufferedImage B, int size ) {
       if ( B.getType() != B.TYPE_USHORT_GRAY ) return null;
       
       DataBuffer data = B.getRaster().getDataBuffer();
       
       complex [][] Z  = new complex[size][size];
       complex [] temp = new complex[size];
       
       int i, j;
       
       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               temp[j] = new complex( data.getElem( j + i * size ) );
           }
           Z[i] = FFT.fft(temp);
       }
       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               temp[j] = Z[j][i];
           }
           Z[i] = FFT.fft(temp);
       }
       /*
       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               Z[i][j] = new complex( data.getElem( j + i * size ) );
           }
       }*/
       return new ComplexRGBTexture( Z );
    }
    
    public static ComplexRGBTexture inverseFFT( BufferedImage B, int size ) {
       if ( B.getType() != B.TYPE_USHORT_GRAY ) return null;
       
       DataBuffer data = B.getRaster().getDataBuffer();
       
       complex [][] Z  = new complex[size][size];
       complex [] temp = new complex[size];
       
       int i, j;
       
       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               temp[j] = new complex( data.getElem( j + i * size ) );
           }
           Z[i] = FFT.fft(temp);
       }
       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               temp[j] = Z[j][i];
           }
           Z[i] = FFT.fft(temp);
       }
       
       return new ComplexRGBTexture( Z );
    }

    public static ComplexRGBTexture FFT( ComplexRGBTexture B, int size ) {

       complex [][] Z  = new complex[size][size];
       complex [] temp = new complex[size];

       int i, j;

       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               temp[j] = B.getValue(j + i * size);
           }
           Z[i] = FFT.fft(temp);
       }
       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               temp[j] = Z[j][i];
           }
           complex [] tt = FFT.fft(temp);
           for (j = 0; j < size; j++ ) {
               Z[j][i] = tt[j];
           }
       }
       /*
       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               Z[i][j] = new complex( data.getElem( j + i * size ) );
           }
       }*/
       return new ComplexRGBTexture( Z );
    }

    public static ComplexRGBTexture inverseFFT( ComplexRGBTexture B, int size ) {

       complex [][] Z  = new complex[size][size];
       complex [] temp = new complex[size];

       int i, j;

       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               temp[j] = B.getValue(j + i * size);
           }
           Z[i] = FFT.ifft(temp);
       }
       for (i = 0; i < size; i ++ ) {
           for (j = 0; j < size; j++ ) {
               temp[j] = Z[j][i];
           }
           Z[i] = FFT.ifft(temp);
       }

       return new ComplexRGBTexture( Z );
    }


    public static complex [][] FFT( complex [][] B, int size ) {

       complex [][] Z  = new complex[size][];
       complex [] temp = new complex[size];

       int i, j;

       for (i = 0; i < size; i ++ ) {
           Z[i] = FFT.fft(B[i]);
       }
       for (j = 0; j < size; j ++ ) {
           for (i = 0; i < size; i++ ) {
               temp[i] = Z[i][j];
           }
           complex [] tt = FFT.fft(temp);
           for (i = 0; i < size; i++ ) {
               Z[i][j] = tt[i];
           }
       }
       return Z ;
    }

    public static complex [][] inverseFFT( complex [][] B, int size ) {

       complex [][] Z  = new complex[size][];
       complex [] temp = new complex[size];

       int i, j;

       for (i = 0; i < size; i ++ ) {
           Z[i] = FFT.ifft(B[i]);
       }
       for (j = 0; j < size; j ++ ) {
           for (i = 0; i < size; i++ ) {
               temp[i] = Z[i][j];
           }
           complex [] tt = FFT.ifft(temp);
           for (i = 0; i < size; i++ ) {
               Z[i][j] = tt[i];
           }
       }

       return Z;
    }

    ////////////////////////////////////////////////////////////////////////////

    public static double [][] FFT( double [][] B, int size ) {

       int length = B[0].length ;

       double [][] Z  = new double[size][];
       double [] temp = new double[length];

       int i, j;

       for (i = 0; i < size; i ++ ) {
           Z[i] = FFT.fft(B[i]);
       }
       for (j = 0; j < size; j ++ ) {
           for (i = 0; i < size; i++ ) {
               temp[2*i  ] = Z[i][j*2];
               temp[2*i+1] = Z[i][j*2+1];
           }
           double [] tt = FFT.fft(temp);
           for (i = 0; i < size; i++ ) {
               Z[i][j*2  ] = tt[i*2  ];
               Z[i][j*2+1] = tt[i*2+1];
           }
       }
       return Z ;
    }

    public static double [][] inverseFFT( double [][] B, int size ) {

       int length = B[0].length ;

       double [][] Z  = new double[size][];
       double [] temp = new double[length];

       int i, j;

       for (i = 0; i < size; i ++ ) {
           Z[i] = FFT.ifft(B[i]);
       }
       for (j = 0; j < size; j ++ ) {
           for (i = 0; i < size; i++ ) {
               temp[2*i  ] = Z[i][j*2];
               temp[2*i+1] = Z[i][j*2+1];
           }
           double [] tt = FFT.ifft(temp);
           for (i = 0; i < size; i++ ) {
               Z[i][j*2  ] = tt[i*2  ];
               Z[i][j*2+1] = tt[i*2+1];
           }
       }
       return Z ;
    }

    public static double [][] inverseFFTReal( double [][] B, int size, double [][] result ) {

       int length = B[0].length ;

       double [][] Z  = new double[size][];
       double [] temp = new double[length];

       int i, j;

       for (i = 0; i < size; i ++ ) {
           Z[i] = FFT.ifft(B[i]);
       }
       for (j = 0; j < size; j ++ ) {
           for (i = 0; i < size; i++ ) {
               temp[2*i  ] = Z[i][j*2];
               temp[2*i+1] = Z[i][j*2+1];
           }
           double [] tt = FFT.ifft(temp);
           for (i = 0; i < size; i++ ) {
               result[i][j] = tt[i*2];
           }
       }
       return result ;
    }

    ////////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////

    public static double [][] FFTOnlyReal( double [][] B, int size ) {

       int length = B[0].length ;

       double [][] Z  = new double[size][];
       double [] temp = new double[length*2];

       int i, j;

       for (i = 0; i < size; i ++ ) {
           Z[i] = FFT.fftOnlyReal(B[i]);
       }
       for (j = 0; j < size; j ++ ) {
           for (i = 0; i < size; i++ ) {
               temp[2*i  ] = Z[i][j*2];
               temp[2*i+1] = Z[i][j*2+1];
           }
           double [] tt = FFT.fft(temp);
           for (i = 0; i < size; i++ ) {
               Z[i][j*2  ] = tt[i*2  ];
               Z[i][j*2+1] = tt[i*2+1];
           }
       }
       return Z ;
    }

    /*
    public static double [][] inverseFFT( double [][] B, int size ) {

       int length = B[0].length ;

       double [][] Z  = new double[size][];
       double [] temp = new double[length];

       int i, j;

       for (i = 0; i < size; i ++ ) {
           Z[i] = FFT.ifft(B[i]);
       }
       for (j = 0; j < size; j ++ ) {
           for (i = 0; i < size; i++ ) {
               temp[2*i  ] = Z[i][j*2];
               temp[2*i+1] = Z[i][j*2+1];
           }
           double [] tt = FFT.ifft(temp);
           for (i = 0; i < size; i++ ) {
               Z[i][j*2  ] = tt[i*2  ];
               Z[i][j*2+1] = tt[i*2+1];
           }
       }
       return Z ;
    }
    */

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Perfoem an FFT convolution of two things.
     * @param image : a size by size double array
     * @param filter : a size by size double array
     * @param size : a positive non zero power of two
     */
    public static complex [][] convolve(complex [][] ctxo, complex [][] kero, int size)
    {
        complex [][] ctx = FFT( ctxo , size );
        complex [][] ker = FFT( kero , size );
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ )
                ctx[i][j].timeseq(ker[i][j]);
        return inverseFFT(ctx,size);
    }

    public static complex [][] blur(complex [][] ctxo, int size, double sigma)
    {
        int kc = size >> 1 ;
        int mm = size -  1 ;
        complex [][] kero = new complex[size][size];
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ )
                kero[i+kc&mm][j+kc&mm] = new complex(f( Math.hypot(i-kc,j-kc), sigma));
        return convolve(ctxo,kero,size);
    }

    public static class FFTConvolution
    {
        int size ;
        complex [][] fftkernel ;

        public FFTConvolution(complex [][] kero, int size )
        {
            this.size=size ;
            fftkernel = FFT( kero , size );
        }

        public complex [][] convolve( complex [][] ctxo )
        {
            complex [][] ctx = FFT( ctxo , size );
            for ( int i = 0 ; i < 512 ; i ++ )
                for ( int j = 0 ; j < 512 ; j ++ )
                    ctx[i][j].timeseq(fftkernel[i][j]);
            return inverseFFT(ctx,size);
        }
    }

    public static class GaussianConvolution
    {
        int size ;
        complex [][] fftkernel ;

        public GaussianConvolution(float sigma, int size )
        {
            this.size=size ;
            
            int kc = size >> 1 ;
            int mm = size -  1 ;
            complex [][] kero = new complex[size][size];
            for ( int i = 0 ; i < size ; i ++ )
                for ( int j = 0 ; j < size ; j ++ )
                    kero[i+kc&mm][j+kc&mm] = new complex(f( Math.hypot(i-kc,j-kc), sigma));
            fftkernel = FFT( kero , size );
        }

        public complex [][] convolve( complex [][] ctxo )
        {
            complex [][] ctx = FFT( ctxo , size );
            for ( int i = 0 ; i < size ; i ++ )
                for ( int j = 0 ; j < size ; j ++ )
                    ctx[i][j].timeseq(fftkernel[i][j]);
            return inverseFFT(ctx,size);
        }
    }

    public static double gaussian( double x, double mu, double sigma ) {
        //hack to prevent introducing NaN into the system
        if ( sigma < .1 ) return 0 ;
        return Math.exp(-.5*Math.pow((x-mu)/sigma,2))/(sigma*Math.sqrt(2*Math.PI));
    }

    public static double f( double x, double sigma ) {
        return Math.exp(-.5*Math.pow(x/sigma,2))/(sigma*sigma*2*Math.PI);
    }

    static void displayArray( double [][] array, int size, String title )
    {
        BufferedImage original = new BufferedImage( size, size, BufferedImage.TYPE_INT_RGB);
        double min = Double.MAX_VALUE ;
        double max = Double.MIN_VALUE ;
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ )
            {
                double x = array[i][j*2];
                min = min(x,min);
                max = max(x,max);
            }
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ )
            {
                int c = 0xff&((int)(0xff*(array[i][j*2]-min)/(max-min)));
                original.setRGB(j, i, c|(c<<8)|(c<<16));
            }
        (new SimpleImageIO.ImagePopup(original,title)).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    static void displayRealArray( double [][] array, int size, String title )
    {
        BufferedImage original = new BufferedImage( size, size, BufferedImage.TYPE_INT_RGB);
        double min = Double.MAX_VALUE ;
        double max = Double.MIN_VALUE ;
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ )
            {
                double x = array[i][j];
                min = min(x,min);
                max = max(x,max);
            }
        for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ )
            {
                int c = 0xff&((int)(0xff*(array[i][j]-min)/(max-min)));
                original.setRGB(j, i, c|(c<<8)|(c<<16));
            }
        (new SimpleImageIO.ImagePopup(original,title)).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    static public class RealValuedConvolution
    {
        int size ;
        double [][] ker ;

        RealValuedConvolution( double [][] kero, int size ) {
            this.size = size ;
            ker = FFT2D.FFTOnlyReal(kero, size);
        }

        public double [][] convolveReal(double [][] data, double [][] result)
        {
            double [][] ctx = FFT2D.FFTOnlyReal(data, size);

            for ( int i = 0 ; i < size ; i ++ )
            for ( int j = 0 ; j < size ; j ++ )
            {
                double real  = ctx[i][j*2  ] ;
                double imag  = ctx[i][j*2+1] ;
                double nreal = ker[i][j*2  ] ;
                double nimag = ker[i][j*2+1] ;

                double x4 = real * nreal ;
                double x5 = imag * nimag ;
                imag = ( real + imag ) * ( nreal + nimag ) - x4 - x5 ;
                real = x4 - x5 ;

                ctx[i][j*2  ] = real ;
                ctx[i][j*2+1] = imag ;
            }
            return FFT2D.inverseFFTReal(ctx, size, result);
        }
    }

    public static void main( String[] args ) {

        BufferedImage I = SimpleImageIO.LoadImage(9);
        
        BufferedImage bb = new BufferedImage( 512, 512, BufferedImage.TYPE_INT_RGB);
        bb.getGraphics().drawImage(I,0,0,512,512,null);
        DataBuffer d = bb.getData().getDataBuffer();

        double [][] ctxo = new double[512][512];
        double [][] kero = new double[512][512];
        
        for ( int i = 0 ; i < 512 ; i ++ )
            for ( int j = 0 ; j < 512 ; j ++ ) 
            {
                ctxo[i][j] = (d.getElem(i*512+j)&0xff)/256.0f ;
                kero[i+256&511][j+256&511] =
                        f( Math.hypot(i-256,j-256), 5) -
                        f( Math.hypot(i-256,j-256), 10);
            }

        displayRealArray(ctxo,512,"original");
        displayRealArray(kero,512,"kernel");

        RealValuedConvolution conv = new RealValuedConvolution(kero,512);
        double [][] ctx = new double[512][512];
        conv.convolveReal(ctxo, ctx);

        //double [][] ctx = FFT2D.FFTOnlyReal(ctxo, 512);
        //double [][] ker = FFT2D.FFTOnlyReal(kero, 512);

        //displayArray(ctx,512,"FFT original");
        //displayArray(ker,512,"FFT kernel");
/*
        for ( int i = 0 ; i < 512 ; i ++ )
            for ( int j = 0 ; j < 512 ; j ++ )
            {
                double real  = ctx[i][j*2  ] ;
                double imag  = ctx[i][j*2+1] ;
                double nreal = ker[i][j*2  ] ;
                double nimag = ker[i][j*2+1] ;

                double x4 = real * nreal ;
                double x5 = imag * nimag ;
                imag = ( real + imag ) * ( nreal + nimag ) - x4 - x5 ;
                real = x4 - x5 ;
                
                ctx[i][j*2  ] = real ;
                ctx[i][j*2+1] = imag ;
            }
*/

        //displayArray(ctx,512,"FFT result");

        //double [][] ctxc = FFT2D.inverseFFT(ctx, 512);

        displayRealArray(ctx,512,"result");

        /*
        complex [][] ctxo = new complex[512][512];
        for ( int i = 0 ; i < 512 ; i ++ )
            for ( int j = 0 ; j < 512 ; j ++ ) 
            {
                ctxo[i][j] = new complex((d.getElem(i*512+j)&0xff)/256.0f);
            }
        */
        //(new SimpleImageIO.ImagePopup((new ComplexRGBTexture(ctxo)).toBufferedImage(new BufferedImage(512,512,BufferedImage.TYPE_INT_RGB)),"original")).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //(new SimpleImageIO.ImagePopup((new ComplexRGBTexture(kero)).toBufferedImage(new BufferedImage(512,512,BufferedImage.TYPE_INT_RGB)),"filter")).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*
        complex [][] ctx = FFT( ctxo , 512 );
        complex [][] ker = FFT( kero , 512 );

        (new SimpleImageIO.ImagePopup((new ComplexRGBTexture(ctx)).toBufferedImage(new BufferedImage(512,512,BufferedImage.TYPE_INT_RGB)),"original FFT")).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        (new SimpleImageIO.ImagePopup((new ComplexRGBTexture(ker)).toBufferedImage(new BufferedImage(512,512,BufferedImage.TYPE_INT_RGB)),"filter FFT")).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        for ( int i = 0 ; i < 512 ; i ++ )
            for ( int j = 0 ; j < 512 ; j ++ )
            {
                ctx[i][j].timeseq(ker[i][j]);
            }
        
        (new SimpleImageIO.ImagePopup((new ComplexRGBTexture(ctx)).toBufferedImage(new BufferedImage(512,512,BufferedImage.TYPE_INT_RGB)),"convolution FFT")).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        complex [][] ictx = inverseFFT(ctx,512);
        */
        //complex [][] ictx = (new GaussianConvolution(5,512)).convolve(ctxo);
        //(new SimpleImageIO.ImagePopup((new ComplexRGBTexture(ictx)).toBufferedImage(new BufferedImage(512,512,BufferedImage.TYPE_INT_RGB)),"result")).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////


    public class SeperableGaussian {

        double sigma ;
        double [] kernel ;
        double normalize ;
        int kw ;
        int kc ;

        public SeperableGaussian( double sigma )
        {
            if ( sigma <= 0.1 )
            //    throw new IllegalArgumentException("Size must be positive");
                sigma = 0.1 ;

            this.sigma = sigma;

            kw = (int)ceil(6*sigma);
            kc = (int)round(3*sigma);

            kernel = new double[kw];

            double sum = 0.0 ;

            for ( int i = 0 ; i < kw ; i ++ )
                sum += kernel[i] = gaussian( abs(i-kc), 0, sigma ) ;

            normalize = 1.0 / (sum*sum) ;
        }

        public double getSigma()
        {
            return sigma ;
        }

        /** Assumption : data is a square power of two sized array ! */
        public double[][] convolve(double[][] data,double[][] result1,double[][] result2)
        {
            int size = data.length ;
            int mask = size - 1 ;

            for ( int i = 0 ; i < data.length ; i ++ ) {
                int I = i - kc ;
                for ( int j = 0 ; j < data[i].length ; j ++ )
                {
                    double convolve = 0.0 ;
                    for ( int ii = 0 ; ii < kw ; ii ++ )
                        convolve += data[I+ii&mask][j]*kernel[ii] ;
                    result1[i][j] = convolve ;
                }
            }
            for ( int i = 0 ; i < data.length ; i ++ )
                for ( int j = 0 ; j < data[i].length ; j ++ )
                {
                    int J = j-kc ;
                    double convolve = 0.0 ;
                    for ( int jj = 0 ; jj < kw ; jj ++ )
                        convolve += result1[i][J+jj&mask]*kernel[jj] ;
                    result2[i][j] = convolve*normalize ;
                }
            return result2 ;
        }
    }



    public static class CortexConvolution extends FFTConvolution
    {
        public final double sigmae ;
        public final double sigmai ;
        public final double ae ;
        public final double ai ;

        static double f( double x, double sigma ) {
            return Math.exp(-.5*Math.pow(x/sigma,2))/(sigma*sigma*2*Math.PI);
        }
        static complex [][] generateKernel(double sigmae,double sigmai,double ae,double ai,int size)
        {
            int kc = size >> 1 ;
            int mm = size -  1 ;
            complex [][] kero = new complex[size][size];
            for ( int i = 0 ; i < size ; i ++ )
                for ( int j = 0 ; j < size ; j ++ )
                    kero[i+kc&mm][j+kc&mm] =
                            new complex(
                                f( Math.hypot(i-kc,j-kc), sigmae)*ae -
                                f( Math.hypot(i-kc,j-kc), sigmai)*ai
                            );
            return kero ;
        }

        public CortexConvolution(double sigmae,double sigmai,double ae,double ai,int size)
        {
            super(generateKernel(sigmae,sigmai,ae,ai,size),size);
            this.sigmae = sigmae ;
            this.sigmai = sigmai ;
            this.ae = ae ;
            this.ai = ai ;
        }
    }

}

