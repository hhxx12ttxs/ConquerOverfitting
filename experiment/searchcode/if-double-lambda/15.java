/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utility;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

/**
 *
 * @author jrpeterson
 */
// basic class for matrix addition, multiplication, subtraction, etc
// my crudy version which is here at the moment for backwards compatability with the DLT if we decide to use
// it otherwise, use april linalg stuff because it is better

// except my convolution method makes more sense than his
public class Matrix {
    
    public static double[][] ones(int rows, int columns) {
        double[][] O = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                O[i][j] = 1;
            }
        }
        return O;
    }

    public static double[] add(double[] A, double[] B) {
        assert A.length == B.length : "Warning Expected A and B to have the same length";

        double[] C = new double[A.length];
        for (int i = 0; i < A.length; i++) {
            C[i] = A[i] + B[i];
        }
        return C;
    }

    public static double[][] add(double[][] A, double[][] B) {
        assert ((A.length == B.length) && (A[0].length == B[0].length)) : "Warning Expected A and B to have the same dimensions";

        double[][] C = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }

    public static double[] subtract(double[] A, double[] B) {
        return add(A, times(-1.0, B));
    }

    public static double[][] subtract(double[][] A, double[][] B) {
        return add(A, times(-1.0, B));
    }

    public static double dotProduct(double[] A, double[] B) {
        assert (A.length == B.length) : "Warning Expected A and B to have the same length";

        double value = 0;
        for (int i = 0; i < A.length; i++) {
            value += A[i] * B[i];
        }
        return value;
    }

    public static double[] normalizeL1(double[] A) {
        double total = 0;
        for (int i = 0; i < A.length; i++) {
            total = total + Math.abs(A[i]);
        }
        return Matrix.times(1 / total, A);
    }

    public static double det(double[][] A) {
        assert (A.length == A[0].length) : "Warning Square matrix only";
        assert ((A.length == 2) || (A.length == 3)) : "Warning 2x2 or 3x3 only";

        if (A.length == 2) {
            return A[0][0] * A[1][1] - A[0][1] * A[1][0];
        } else {
            return (A[0][0] * (A[2][2] * A[1][1] - A[2][1] * A[1][2]) - A[1][0] * (A[2][2] * A[0][1] - A[2][1] * A[0][2])
                    + A[2][0] * (A[1][2] * A[0][1] - A[1][1] * A[0][2]));
        }
    }

    public static double[][] inverse(double[][] A) {
        assert (A.length == A[0].length) : "Warning Square matrix only";
        assert ((A.length == 2) || (A.length == 3)) : "Warning 2x2 or 3x3 only";

        double detA = det(A);
        if (Math.abs(detA) <= 1e-15) {
            System.out.println("Warning Matrix may be singular");
        }

        if (A.length == 2) {
            return Matrix.times(1 / detA, new double[][]{{A[1][1], -A[0][1]}, {-A[1][0], A[0][0]}});
        } else {
            double[][] AI = new double[3][3];
            AI[0][0] = A[2][2] * A[1][1] - A[2][1] * A[1][2];
            AI[0][1] = -(A[2][2] * A[0][1] - A[2][1] * A[0][2]);
            AI[0][2] = A[1][2] * A[0][1] - A[1][1] * A[0][2];
            AI[1][0] = -(A[2][2] * A[1][0] - A[2][0] * A[1][2]);
            AI[1][1] = A[2][2] * A[0][0] - A[2][0] * A[0][2];
            AI[1][2] = -(A[1][2] * A[0][0] - A[1][0] * A[0][2]);
            AI[2][0] = A[2][1] * A[1][0] - A[2][0] * A[1][1];
            AI[2][1] = -(A[2][1] * A[0][0] - A[2][0] * A[0][1]);
            AI[2][2] = A[1][1] * A[0][0] - A[1][0] * A[0][1];

            return Matrix.times(1 / detA, AI);
        }
    }

    public static double[] crossProduct(double[] A, double[] B) {
        assert ((A.length == B.length) && ((A.length == 3) || (A.length == 4))) :
                "Warning only supports vectors in 3D or homogeneous 3D";
        double[] C = new double[A.length];
        if (A.length == 4) {
            C[3] = 1; // homogeneous coordinates
        }
        C[0] = A[1] * B[2] - A[2] * B[1];
        C[1] = A[2] * B[0] - A[0] * B[2];
        C[2] = A[0] * B[1] - A[1] * B[0];
        return C;
    }

    public static double[] times(double A, double[] B) {
        double[] C = new double[B.length];
        for (int i = 0; i < B.length; i++) {
            C[i] = A * B[i];
        }
        return C;
    }

    public static double[][] times(double A, double[][] B) {
        double[][] C = new double[B.length][B[0].length];
        for (int i = 0; i < B.length; i++) {
            C[i] = times(A, B[i]);
        }
        return C;
    }

    public static double[][] times(double[][] A, double[][] B) {
        assert (A[0].length == B.length) : "Warning must have same number of columns in A as rows in B";

        double[][] C = new double[A.length][B[0].length];
        for (int i = 0; i < C.length; i++) {
            for (int j = 0; j < C[i].length; j++) {
                C[i][j] = dotProduct(getRow(A, i), getColumn(B, j));
            }
        }
        return C;
    }

    public static double[] getRow(double[][] A, int R) {
        double[] Row = new double[A[R].length];
        System.arraycopy(A[R], 0, Row, 0, A[R].length);
        return Row;
    }

    public static double[] getColumn(double[][] A, int C) {
        double[] Column = new double[A.length];
        for (int i = 0; i < A.length; i++) {
            Column[i] = A[i][C];
        }
        return Column;
    }

    public static double[][] toColumnVec(double[] A) {
        double[][] C = new double[A.length][1];
        for (int i = 0; i < C.length; i++) {
            C[i][0] = A[i];
        }
        return C;
    }

    public static double[][] toRowVec(double[] A) {
        double[][] C = new double[1][A.length];
        C[0] = A;
        return C;
    }

    public static double[][] transpose(double[][] A) {
        double[][] C = new double[A[0].length][A.length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                C[j][i] = A[i][j];
            }
        }
        return C;
    }

    public static double L2norm(double[] A) {
        double sum = 0;
        for (int i = 0; i < A.length; i++) {
            sum += A[i] * A[i];
        }
        return Math.sqrt(sum);
    }
    
    public static double L2norm2(double[] A) {
        double sum = 0;
        for (int i = 0; i < A.length; i++) {
            sum += A[i] * A[i];
        }
        return sum;
    }

    public static boolean equals(double[] A, double[] B) {
        if (A.length != B.length) {
            return false;
        } else {
            for (int i = 0; i < A.length; i++) {
                if (A[i] != B[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public static double[] toH(double[] A) {
        double[] newV = new double[A.length + 1];
        System.arraycopy(A, 0, newV, 0, A.length);
        newV[A.length] = 1;
        return newV;
    }

    public static void print(double A) {
        System.out.println(A);
    }

    public static void print(double[] A) {
        for (int j = 0; j < A.length; j++) {
            System.out.print(A[j] + " ");
        }
        System.out.print("\n");
    }

    public static void print(double[][] A) {
        for (int i = 0; i < A.length; i++) {
            print(A[i]);
        }
        System.out.print("\n");
    }
    
    public static double max(double[][] A) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                if (A[i][j] > max) {
                    max = A[i][j];
                }
            }
        }
        return max;
    }
    
    public static double min(double[][] A) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < A.length; i++) {
            for (int j =0; j < A[0].length; j++) {
                if (A[i][j] < min) {
                    min = A[i][j];
                }
            }
        }
        return min;
    }
    
    // where scaleFactor indicates how many pixels correspond to each element of the matrix
    public static BufferedImage visualize(double[][] A, double scaleFactor) {
        
        if (scaleFactor > 1) {
            // upsamples the image
            int imgWidth = (int) Math.ceil(scaleFactor*A[0].length);
            int imgHeight = (int) Math.ceil(scaleFactor*A.length);
            BufferedImage img = new BufferedImage(imgWidth,imgHeight,BufferedImage.TYPE_INT_RGB);
            int[] databuff =((DataBufferInt)(img.getRaster().getDataBuffer())).getData();
            
            // find the minimum value set that to 0, then normalize to 255
            A = add(A,times(-min(A),ones(A.length,A[0].length)));
            A = times(255.0/max(A),A); 
            
            int cordi,cordj,rgb;
            for (int i = 0; i < imgHeight; i++) {
                cordi = (int) Math.floor(i/scaleFactor);
                for (int j = 0; j < imgWidth; j++) {
                    cordj = (int) Math.floor(j/scaleFactor);
                    rgb = (((int)A[cordi][cordj] & 0xff) << 16) | 
                            (((int)A[cordi][cordj] & 0xff) << 8) | ((int)A[cordi][cordj] & 0xff);
                    databuff[i*imgWidth + j] = rgb;
                }
            }
            return img;
            
        } else if (scaleFactor == 1) {
            // normal no scaling
            BufferedImage img = new BufferedImage(A[0].length,A.length,BufferedImage.TYPE_INT_RGB);
            int[] databuff =((DataBufferInt)(img.getRaster().getDataBuffer())).getData();
            
            // find the minimum value set that to 0, then normalize to 255
            A = add(A,times(-min(A),ones(A.length,A[0].length)));
            A = times(255.0/max(A),A); 
            
            int rgb;
            for (int i = 0; i < A.length; i++) {
                for (int j = 0; j < A[0].length; j++) {
                    rgb = (((int)A[i][j] & 0xff) << 16) | (((int)A[i][j] & 0xff) << 8) | ((int)A[i][j] & 0xff);
                    databuff[i*A[0].length + j] = rgb;
                }
            }
            return img;
        } else if ((scaleFactor < 1) && (scaleFactor > 0)) {
            // downsamples the image after bluring to avoid aliasing
            // note that downsampling may not keep the image quite centered
            int imgWidth = (int) Math.ceil(scaleFactor*A[0].length);
            int imgHeight = (int) Math.ceil(scaleFactor*A.length);
            BufferedImage img = new BufferedImage(imgWidth,imgHeight,BufferedImage.TYPE_INT_RGB);
            int[] databuff =((DataBufferInt)(img.getRaster().getDataBuffer())).getData();
            
            double sigma = 0.375/scaleFactor;
            // now need to blur A so that don't get aliasing
            int size = (int) Math.round(3*sigma);
            if (size % 2 == 0) {
                size++; // to make sure that it is odd
            }
            double[][] G = Gaussian2D(sigma,size);
            double[][] bA = convolveC(A,G);
            
            // find the minimum value set that to 0, then normalize to 255
            bA = add(bA,times(-min(bA),ones(bA.length,bA[0].length)));
            bA = times(255.0/max(bA),bA); 
            
            int cordi,cordj,rgb;
            for (int i = 0; i < imgHeight; i++) {
                cordi = (int) Math.floor(i/scaleFactor);
                for (int j = 0; j < imgWidth; j++) {
                    cordj = (int) Math.floor(j/scaleFactor);
                    rgb = (((int)bA[cordi][cordj] & 0xff) << 16) | 
                            (((int)bA[cordi][cordj] & 0xff) << 8) | ((int)bA[cordi][cordj] & 0xff);
                    databuff[i*imgWidth + j] = rgb;
                }
            }
            return img;
        } else {
            assert false : "ScaleFactor must be greater than 0";
            return null;
        }
    }

    public static double[][] identity(int S) {
        return identity(S, S);
    }

    public static double[][] identity(int R, int C) {
        double[][] I = new double[R][C];
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                if (i == j) {
                    I[i][j] = 1.0;
                }
            }
        }
        return I;
    }
    
    public static double[][] rbt2D(double dx, double dy, double theta) {
        double[][] rbt = new double[3][3];
        rbt[0][0] = Math.cos(theta);
        rbt[0][1] = -Math.sin(theta);
        rbt[1][0] = -rbt[0][1];
        rbt[1][1] = rbt[0][0];
        rbt[0][2] = dx;
        rbt[1][2] = dy;
        rbt[2][2] = 1;
        return rbt;
    }

    // convolution of discrete valued functions in 1D
    // returns a new discrete function with the same length as the 1st argument "crops it"
    // need kernel to be odd so don't shift
    public static double[] convolveC(double[] F, double[] K) {
        assert (K.length % 2 != 0) : "Warning Kernel must be odd";

        double[] G = new double[F.length];

        int start, end; // calculate bounds to make sure don't go out of bounds
        int Klength = K.length;
        int Glength = G.length;
        int Klength2 = Klength / 2;

        for (int i = 0; i < Glength; i++) {
            // lower bound
            if ((i - Klength2) < 0) {
                start = Klength2 - i; // need to start later to stay in bounds
            } else {
                start = 0;
            }
            // upper bound
            if ((i + Klength2) >= Glength) {
                end = Glength - i + Klength2; // need to stop sooner to stay in bounds
            } else {
                end = Klength;
            }

            for (int j = start; j < end; j++) {
                G[i] += F[i + j - Klength2] * K[(Klength - 1) - j]; // sum across elements have to reflect the kernel
            }
        }
        return G;
    }

    //convolution of discrete valued functions in 2D, second argument considered to be the kernel
    // crops to dimensions of the first argument, functions just like the matlab one
    // need kernel to be odd so that it won't shift
    /*
    public static double[][] convolveC(double[][] F, double[][] K) {
        assert ((K.length % 2 != 0) && (K[0].length % 2 != 0)) : "Warning, Kernel must be odd";

        double[][] G = new double[F.length][F[0].length];

        int GWidth = G.length; // x dimension, how many columns
        int GHeight = G[0].length; // y dimension, how many rows
        int KWidth = K.length;
        int KWidth2 = KWidth / 2;
        int KHeight = K[0].length;
        int KHeight2 = KHeight / 2;

        int xstart, xend, ystart, yend;

        for (int j = 0; j < GHeight; j++) {
            for (int i = 0; i < GWidth; i++) {
                // lower bound x
                if ((i - KWidth2) < 0) {
                    xstart = KWidth2 - i;
                } else {
                    xstart = 0;
                }
                // upper bound x
                if ((i + KWidth2) >= GWidth) {
                    xend = GWidth - i + KWidth2;
                } else {
                    xend = KWidth;
                }

                // lower bound y
                if ((j - KHeight2) < 0) {
                    ystart = KHeight2 - j;
                } else {
                    ystart = 0;
                }
                // upper bound y
                if ((j + KHeight2) >= GHeight) {
                    yend = GHeight - j + KHeight2;
                } else {
                    yend = KHeight;
                }
                for (int y = ystart; y < yend; y++) {
                    for (int x = xstart; x < xend; x++) {
                        G[i][j] += F[i + x - KWidth2][j + y - KHeight2]
                                * K[(KWidth - 1) - x][(KHeight - 1) - y]; // reflect the kernel
                    }
                }
            }
        }

        return G;
    }*/
    
    public static double[][] compose(ArrayList<double[]> rows) {
        int numrows = rows.size();
        int numcolumns = rows.get(0).length;
        
        double[][] M = new double[numrows][numcolumns];
        
        for (int r = 0; r < numrows; r++) {
            assert (rows.get(r).length == numcolumns) : "incompatible dimensions";
            System.arraycopy(rows.get(r), 0, M[r], 0, numcolumns);
        }
        return M;
    }

    
    public static double convolveCPoint(double[][] F, double[][] K, int y, int x) {
        int GWidth = F.length; 
        int GHeight = F[0].length; 
        int KWidth = K.length;
        int KWidth2 = KWidth / 2;
        int KHeight = K[0].length;
        int KHeight2 = KHeight / 2;

	int xstart, xend, ystart, yend;

	// lower bound y
	if ((y - KWidth2) < 0) {
	    ystart = KWidth2 - y;
	} else {
	    ystart = 0;
	}
	// upper bound y
	if ((y + KWidth2) >= GWidth) {
	    yend = GWidth - y + KWidth2;
	} else {
	    yend = KWidth;
	}
	
	// lower bound x
	if ((x - KHeight2) < 0) {
	    xstart = KHeight2 - x;
	} else {
	    xstart = 0;
	}
	// upper bound y
	if ((x + KHeight2) >= GHeight) {
	    xend = GHeight - x + KHeight2;
	} else {
	    xend = KHeight;
	}

	double result = 0;
	for (int yi = ystart; yi < yend; yi++) {
	    for (int xi = xstart; xi < xend; xi++) {
		result += F[y + yi - KWidth2][x + xi - KHeight2]
		    * K[(KWidth - 1) - yi][(KHeight - 1) - xi]; // reflect the kernel
	    }
	}

	return result;
    }
    
    // 2D convolution using 1D arrays for moar speed!!!!! respects row boundaries
    public static double[] convolveC(double[] F, int FWidth, double[] K, int KWidth) {
        assert ((KWidth % 2 != 0) && (K.length/KWidth % 2 != 0)) : "Warning, Kernel must be odd";

        double[] G = new double[F.length];
        int GWidth = FWidth; 
        int GHeight = G.length/GWidth; 
        
        int KWidth2 = KWidth / 2;
        int KHeight = K.length/KWidth;
        int KHeight2 = KHeight / 2;

        int xstart, xend, ystart, yend;
        for (int i = 0; i < GHeight; i++) {
            // lower bound x
            if (i < KHeight2) {
                xstart = KHeight2 - i;
            } else {
                xstart = 0;
            }
            // upper bound x
            if ((i + KHeight2) >= GHeight) {
                xend = GHeight - i + KHeight2;
            } else {
                xend = KHeight;
            }
            for (int j = 0; j < GWidth; j++) {
               
                // lower bound y
                if (j < KWidth2) {
                    ystart = KWidth2 - j;
                } else {
                    ystart = 0;
                }
                // upper bound y
                if ((j + KWidth2) >= GWidth) {
                    yend = GWidth - j + KWidth2;
                } else {
                    yend = KWidth;
                }
                double R = 0;
                int Findex, Kindex;
                for (int x = xstart; x < xend; x++) {
                    Findex = (i + x - KHeight2)*FWidth + j + ystart - KWidth2;
                    Kindex = ((KHeight-1)-x)*KWidth + ((KWidth-1) - ystart);
                    for (int y = ystart; y < yend; y++) {
                         R += F[Findex]*K[Kindex]; // can't forget to reflect the kernel
                         Findex++;
                         Kindex--;
                    }
                }
                G[i*GWidth + j] = R;
                        
            }
        }

        return G;
    }
	

    // looks like past me was stupid and had it backwards resulting in less speed!
    // note that width and height are swapped from normal
    public static double[][] convolveC(double[][] F, double[][] K) {
        assert ((K.length % 2 != 0) && (K[0].length % 2 != 0)) : "Warning, Kernel must be odd";

        double[][] G = new double[F.length][F[0].length];

        int GWidth = G.length; 
        int GHeight = G[0].length; 
        int KWidth = K.length;
        int KWidth2 = KWidth / 2;
        int KHeight = K[0].length;
        int KHeight2 = KHeight / 2;

        int xstart, xend, ystart, yend;

        for (int i = 0; i < GWidth; i++) {
            for (int j = 0; j < GHeight; j++) {
                // lower bound x
                if ((i - KWidth2) < 0) {
                    xstart = KWidth2 - i;
                } else {
                    xstart = 0;
                }
                // upper bound x
                if ((i + KWidth2) >= GWidth) {
                    xend = GWidth - i + KWidth2;
                } else {
                    xend = KWidth;
                }

                // lower bound y
                if ((j - KHeight2) < 0) {
                    ystart = KHeight2 - j;
                } else {
                    ystart = 0;
                }
                // upper bound y
                if ((j + KHeight2) >= GHeight) {
                    yend = GHeight - j + KHeight2;
                } else {
                    yend = KHeight;
                }
                double R = 0;
                for (int x = xstart; x < xend; x++) {
                    double[] Frow = F[i + x - KWidth2];
                    double[] Krow = K[(KWidth - 1) - x]; // reflect
                    for (int y = ystart; y < yend; y++) {
                         R += Frow[j + y - KHeight2] * Krow[(KHeight - 1) - y]; // reflect the kernel
                    }
                }
                G[i][j] = R;
            }
        }
        return G;
    }
    
    // this version deals with edge effects by not convolving in areas that don't fit the full kernel
    // still returns the same size
    // note that width and height are swapped from normal
    public static double[][] convolveCBounded(double[][] F, double[][] K) {
        assert ((K.length % 2 != 0) && (K[0].length % 2 != 0)) : "Warning, Kernel must be odd";

        double[][] G = new double[F.length][F[0].length];

        int GWidth = G.length; 
        int GHeight = G[0].length; 
        int KWidth = K.length;
        int KWidth2 = KWidth / 2;
        int KHeight = K[0].length;
        int KHeight2 = KHeight / 2;

        int xstart, xend, ystart, yend;

        for (int i = KWidth2; i < GWidth-KWidth2; i++) {
            for (int j = KHeight2; j < GHeight-KHeight2; j++) {
                double R = 0;
                for (int x = 0; x < KWidth; x++) {
                    double[] Frow = F[i + x - KWidth2];
                    double[] Krow = K[(KWidth - 1) - x]; // reflect
                    for (int y = 0; y < KHeight; y++) {
                         R += Frow[j + y - KHeight2] * Krow[(KHeight - 1) - y]; // reflect the kernel
                    }
                }
                G[i][j] = R;
            }
        }
        return G;
    }
    
    
    // scales the given matrix to the given dimensions
    public static double[][] scale(double[][] A, int targetWidth, int targetHeight) {
        double[][] sA = new double[targetHeight][targetWidth];
        
        double scaleWidth = ((double) A[0].length)/((double) targetWidth);
        double scaleHeight =((double) A.length)/((double) targetHeight);
        
        if (((1/scaleWidth) < 0.5) || ((1/scaleHeight) < 0.5)) {
            double sigma = 0.375 * Math.min(scaleHeight, scaleWidth);
            // now need to blur A so that don't get aliasing
            int size = (int) Math.round(3 * sigma);
            if (size % 2 == 0) {
                size++; // to make sure that it is odd
            }
            double[][] G = Gaussian2D(sigma, size);
            A = convolveC(A, G);
        }

        int targeti, targetj;
        for (int i = 0; i < sA.length; i++) {
            targeti = (int) Math.floor(scaleHeight * i);
            for (int j = 0; j < sA[0].length; j++) {
                targetj = (int) Math.floor(scaleWidth * j);
		//System.out.println("("+i+", "+j+") => ("+targeti+", "+targetj+")");
                sA[i][j] = A[targeti][targetj];
            }
        }
        return sA;
    }
    
    // scales the given matrix to the given dimensions
    public static int[][] scale(int[][] A, int targetWidth, int targetHeight) {
        int[][] sA = new int[targetHeight][targetWidth];

        double scaleWidth = ((double) A[0].length) / ((double) targetWidth);
        double scaleHeight = ((double) A.length) / ((double) targetHeight);

        int targeti, targetj;
        for (int i = 0; i < sA.length; i++) {
            targeti = (int) Math.floor(scaleHeight * i);
            for (int j = 0; j < sA[0].length; j++) {
                targetj = (int) Math.floor(scaleWidth * j);
                sA[i][j] = A[targeti][targetj];
            }
        }
        return sA;

    }
    
    // sort of like a dot product for matricies, I made up the name
    public static double resolve(double[][] A, double[][] B) {
        assert ((A.length == B.length) && (A[0].length == B[0].length)) : "Warning incompatible dimensions";
        double R = 0;
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                R += A[i][j]*B[i][j];
            }
        }
        return R;
    }
    
    // bounds checking is for silly people
    public static double resolveSub(double[][] A, int startrow, int startcol, double[][] B) {
        assert ((startrow + B.length < A.length) && (startcol + B.length < A[0].length)) : "Warning incompatible dimensions";
        double R = 0;
        double[] Arow,Brow;
        for (int i = 0; i < B.length; i++) {
            Arow = A[i+startrow];
            Brow = B[i];
            for (int j = 0; j < B.length; j++) {
                R += Arow[j+startcol]*Brow[j];
            }
        }
        return R;
    }


    // sigma is the value standard deviation of the filter, size, is the size of the filter
    // if not odd, then center will be in between pixels
    // and center is assumed to be at the center of the distribution
    public static double[] Gaussian1D(double sigma, int size) {
        double sigma2 = sigma*sigma;
        double[] G = new double[size];
        double x;
        double total = 0;
        for (int i = 0; i < size; i++) {
            x = i - size/2.0 + 0.5; // x value at the center of the cell
            G[i] = (1/Math.sqrt(2*Math.PI*sigma2))*Math.exp((-x*x)/(2*sigma2));
            total += G[i];
        }
        G = Matrix.times(1/total,G); // normalize gaussian
        return G;
    }

    public static double[][] Gaussian2D(double sigma, int size) {
        double[][] G = new double[size][size];
        double x,y;
        double total = 0;

        for (int j = 0; j < size; j++) {
            y = j - size/2.0 + 0.5; // y value at the center of this row
            for (int i = 0; i < size; i++) {
                x = i - size/2.0 + 0.5; // x value at the center of the cell
                G[i][j] = (1/Math.sqrt(2*Math.PI*sigma*sigma))*Math.exp((-x*x)/(2*sigma*sigma));
                G[i][j] *= (1/Math.sqrt(2*Math.PI*sigma*sigma))*Math.exp((-y*y)/(2*sigma*sigma));
                total += G[i][j];
            }
        }
        G = Matrix.times(1/total, G); // normalize gaussian
        return G;
    }
    
    public static double[][] LofG2D(double sigma, int size) {
        double[][] lG = new double[size][size];
        double x,y,x2,y2;
        
        double sigma2 = sigma*sigma;
        double sigma4 = sigma2*sigma2;
        for (int j = 0; j < size; j++) {
            y = j - size/2.0 + 0.5; 
            y2 = y*y;
            for (int i = 0; i < size; i++) {
                x = i - size/2.0 + 0.5;
                x2 = x*x;
                lG[i][j] = -(1/(Math.PI*sigma4))*(1 - (x2 + y2)/(2*sigma2))*Math.exp(-(x2+y2)/(2*sigma2));
            }
        }
        
        return lG;
    }
    
    public static double[][] roll(double[] A, int numColumns) {
        assert (A.length % numColumns == 0) : "Warning not rectangular";
        
        double[][] Am = new double[(int) A.length/numColumns][numColumns];
        
        for (int i = 0; i < Am.length; i++) {
            System.arraycopy(A, i*numColumns, Am[i], 0, numColumns);
        }
        return Am;
    }
        
    public static double[] unroll(double[][] A) {
        double[] Av = new double[A.length * A[0].length];
        
        for (int i = 0; i < A.length; i++) {
            System.arraycopy(A[i],0,Av,i*A[i].length,A[i].length);
        }
        return Av;
    }
    
    

    public static double[] copy(double[] A) {
	double[] B = new double[A.length];
        System.arraycopy(A, 0, B, 0, A.length);
	return B;
    }

    // decomposes 2x2 matrix into eigenvalues and eigenvectors
    public static double[][][] EigDecomp(double[][] A) {
        assert (A.length == A[0].length) && (A.length == 2) : "Warning 2x2 only";

        double T = A[0][0] + A[1][1];
        double D = det(A);

        double[][] Lambda = new double[2][2]; // stores the eigenvalues
        double[][] Q = new double[2][2]; // the eigenvectors

        Lambda[0][0] = T/2 + Math.sqrt((T*T/4)-D); // this one is guarenteed to be the bigger one
        Lambda[1][1] = T/2 - Math.sqrt((T*T/4)-D);

        if (A[1][0] != 0) {
            // first eigen vector
            Q[0][0] = Lambda[0][0] - A[1][1];
            Q[1][0] = A[1][0];
            // second eigen vector
            Q[0][1] = Lambda[1][1] - A[1][1];
            Q[1][1] = A[1][0];
        } else if (A[0][1] != 0) {
            // first eigen vector
            Q[0][0] = A[0][1];
            Q[1][0] = Lambda[0][0] - A[0][0];
            // second eigen vector
            Q[0][1] = A[0][1];
            Q[1][1] = Lambda[1][1] - A[1][1];
        } else if (A[1][1] != 0) {
             // first eigen vector
            Q[0][0] = 0;
            Q[1][0] = 1;
            // second eigen vector
            Q[0][1] = 1;
            Q[1][1] = 0;
        } else {
            // first eigen vector
            Q[0][0] = 1;
            Q[1][0] = 0;
            // second eigen vector
            Q[0][1] = 0;
            Q[1][1] = 1;
        }

        // normalize eigenvectors
        for (int j = 0; j < 2; j++) {
            double[] v = Matrix.getColumn(Q, j);
            v = Matrix.times(1/Matrix.L2norm(v), v);
            for (int i = 0; i < 2; i++) {
                Q[i][j] = v[i];
            }
        }

        return new double[][][] {Lambda,Q};
    }
}


