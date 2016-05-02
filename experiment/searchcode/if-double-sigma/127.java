package OnePopulation2DFFTOptimized;

import static java.lang.Math.* ;

public class GaussianTest {
            
    public static double gaussian( double x, double mu, double sigma ) {
        //hack to prevent introducing NaN into the system
        if ( sigma < .1 ) return 0 ;
        return exp(-.5*pow((x-mu)/sigma,2))/(sigma*sqrt(2*PI));
    }
    
    public static int wrap(int n, int m) {
        return n < 0 ? m - (-n % m) : n % m;
    }
    
    
    public static interface GaussianKernel {
        public double getSigma(); 
        public double [][] convolve(double [][] data);
    }
    
    public static class BruteForceGaussian implements GaussianKernel {

        double sigma ;
        double normalize ;
        double [][] kernel ;
        int kw ;
        int kc ;
        
        public BruteForceGaussian( double sigma )
        {
            if ( sigma <= 0.0 ) 
                throw new IllegalArgumentException("Size must be positive");
            
            this.sigma = sigma;
            
            kw = (int)ceil(6*sigma);
            kc = (int)round(3*sigma);
            
            kernel = new double[kw][kw];
            
            double sum = 0.0 ; 
            
            for ( int i = 0 ; i < kw ; i ++ )
            for ( int j = 0 ; j < kw ; j ++ )
                sum += kernel[i][j] = gaussian( hypot(i-kc,j-kc), 0, sigma ) ;
            
            normalize = 1.0 / sum ;
        }
        
        @Override
        public double getSigma()
        {
            return sigma ;
        }

        @Override
        public double[][] convolve(double[][] data)
        {
            double [][] result = new double[data.length][data[0].length];
            for ( int i = 0 ; i < data.length ; i ++ )
                for ( int j = 0 ; j < data[i].length ; j ++ ) 
                {
                    double convolve = 0.0 ;
                    for ( int ii = 0 ; ii < kw ; ii ++ )
                        for ( int jj = 0 ; jj < kw ; jj ++ )
                            convolve += data[wrap(i+ii-kc,data.length)][wrap(j+jj-kc,data[i].length)]*kernel[ii][jj] ;
                    result[i][j] = convolve * normalize ;
                }
            return result ;
        }
        
    }
    
    public static class SeperableGaussian implements GaussianKernel {

        double sigma ; 
        double [] kernel ; 
        double normalize ;
        int kw ;
        int kc ;
        
        public SeperableGaussian( double sigma ) 
        {
            if ( sigma <= 0.0 ) 
                throw new IllegalArgumentException("Size must be positive");
            
            this.sigma = sigma;
            
            kw = (int)ceil(6*sigma);
            kc = (int)round(3*sigma);
            
            kernel = new double[kw];
            
            double sum = 0.0 ; 
            
            for ( int i = 0 ; i < kw ; i ++ )
                sum += kernel[i] = gaussian( abs(i-kc), 0, sigma ) ;
            
            normalize = 1.0 / sum ;
        }
        
        @Override
        public double getSigma()
        {
            return sigma ;
        }

        /** Assumption : data is a square power of two sized array ! */
        @Override
        public double[][] convolve(double[][] data)
        {
            int size = data.length ; 
            int mask = size - 1 ;
            
            double [][] result1 = new double[size][size];
            double [][] result2 = new double[size][size];
            
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
                    result2[i][j] = convolve*normalize*normalize ;
                }
            return result2 ;
        }
    }
    
    public static void main(String [] args) {
        
        double sigma = 5.0 ;
        
        double[][] data = new double[100][100];
        for ( int i = 0 ; i < data.length ; i ++ )
            for ( int j = 0 ; j < data[i].length ; j ++ ) 
            {
                data[i][j] = random();
            }
        
        GaussianKernel g1 = new BruteForceGaussian(sigma);
        GaussianKernel g2 = new SeperableGaussian(sigma);
        
        double[][] data2 = g1.convolve(data);
        double[][] data3 = g2.convolve(data);
        
        double RMS = 0.0 ;
        for ( int i = 0 ; i < data2.length ; i ++ )
            for ( int j = 0 ; j < data2[i].length ; j ++ ) 
            {
                RMS += pow(data3[i][j]-data2[i][j],2);
            }
        
        System.out.printf("error : %f \n", sqrt(RMS/(data.length*data[0].length)) );
    }
}

