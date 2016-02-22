package org.moca.imageprocessing;

public class Tools {
//	
//	public static int[][][] imgcat(int[][] r, int[][] g, int[][] b, int height, int width){
//		
//		int[][][] img = new int[height][width][3];
//		
//		for(int row=0; row < height; row++){
//			for(int col=0; col < width; col++){
//				img[row][col][0]=r[row][col];
//				img[row][col][1]=g[row][col];
//				img[row][col][2]=b[row][col];
//			}
//		}
//		
//		return img;
//	}
	
	public Tools(){
		
	}

	public static double[][][] imgcat(double[][] r, double[][] g, double[][] b, int height, int width){
		
		double[][][] img = new double[height][width][3];
		
		for(int row=0; row < height; row++){
			for(int col=0; col < width; col++){
				img[row][col][0]=r[row][col];
				img[row][col][1]=g[row][col];
				img[row][col][2]=b[row][col];
			}
		}
		
		return img;
	}
	
	public static double[][] imgGetRGB(double[][][] img, int color, int height, int width){
		try{
			double[][] r = new double[height][width];
			
			if(color >= 0 && color <= 2){
				for(int row=0; row < height; row++){
					for(int col=0; col < width; col++){
						r[row][col]=img[row][col][color];
					}
				}
				return r;
			}
			else{
				return null;
			}
		}catch(Exception e){
			System.out.println("Couldn't get R or G or B out of img " + e.toString());
		}
		return null;
	}
	
	public static double[][][] matrixRGBMult(double[][] mat, double[][][] img, int height, int width){
		try{
			int[][] r = new int[height][width];
			int[][] g = new int[height][width];
			int[][] b = new int[height][width];
			
			for(int row=0; row < height; row++){
				for(int col=0; col < width; col++){
					r[row][col]= Math.round((float)img[row][col][0]);
					g[row][col]= Math.round((float)img[row][col][1]);
					b[row][col]= Math.round((float)img[row][col][2]);
				}
			}
			
//			
//			print2Dmatrix("r",r);
//			print2Dmatrix("g",g);
//			print2Dmatrix("b",b);
//			
			double[][] er = new double[height][width];
			double[][] eg = new double[height][width];
			double[][] eb = new double[height][width];
			
			for(int row=0; row < height; row++){
				for(int col=0; col < width; col++){
					er[row][col]=(mat[0][0]*r[row][col]+mat[0][1]*g[row][col]+mat[0][2]*b[row][col]) % 251;
					eg[row][col]=(mat[1][0]*r[row][col]+mat[1][1]*g[row][col]+mat[1][2]*b[row][col]) % 251;
					eb[row][col]=(mat[2][0]*r[row][col]+mat[2][1]*g[row][col]+mat[2][2]*b[row][col]) % 251;
				}
			}
//
//			print2Dmatrix("er",er);
//			print2Dmatrix("eg",eg);
//			print2Dmatrix("eb",eb);
//			
			return imgcat(er,eg,eb,height,width);
		}catch(Exception e){
			System.out.println("Couldn't do matrix RGB mult " + e.toString());
			return null;
		}
	}
	
	public static double[] rgb2vector(double[][][] img, int height, int width){
		try{
			int totalNum = height*width*3;
			double[] result = new double[totalNum];
			int counter = 0;
			
			for(int row=0; row < height; row++){
				for(int col=0; col < width; col++){
					result[counter]=img[row][col][0];
					counter++;
				}
			}
			
			for(int row=0; row < height; row++){
				for(int col=0; col < width; col++){
					result[counter]=img[row][col][1];
					counter++;
				}
			}
			
			for(int row=0; row < height; row++){
				for(int col=0; col < width; col++){
					result[counter]=img[row][col][2];
					counter++;
				}
			}
			return result;
		}
		catch(Exception e){
			System.out.println("Couldn't convert rgb to one long vector " + e.toString());
			return null;
		}
	}
	
	public static double[][][] vector2RGB(double[] longVector, int height, int width){
		try{
			System.out.println("long vector length " + longVector.length);
			System.out.println("one-third long vector length decimal " + longVector.length/3);
			double fraction = (double)longVector.length/3;
			System.out.println("one-third long vector length int " + fraction);

			double[][] r = new double[height][width];
			double[][] g = new double[height][width];
			double[][] b = new double[height][width];
			int numElements = longVector.length/3;
			int counter = 0;
			
			while(counter < numElements){
				for(int row=0; row < height; row++){
					for(int col=0; col < width; col++){
						r[row][col] = longVector[counter];
						counter++;
					}
				}
			}
			while(counter < 2*numElements){
				for(int row=0; row < height; row++){
					for(int col=0; col < width; col++){
						g[row][col] = longVector[counter];
						counter++;
					}
				}
			}
			while(counter < 3*numElements){
				for(int row=0; row < height; row++){
					for(int col=0; col < width; col++){
						b[row][col] = longVector[counter];
						counter++;
					}
				}
			}
			return imgcat(r,g,b, height, width);
		}
		catch(Exception e){
			System.out.println("Couldn't convert long vector to rgb " + e.toString());
			return null;
		}
		
	}
	
	private static double[][] multiplyEachElement(double[][] mat, double factor){
		int height = mat.length;
		int width = mat[0].length;
		double[][] result = new double[height][width];
		
		for(int row=0; row < height; row++){
			for(int col=0; col < width; col++){
				result[row][col] = mat[row][col]*factor;
			}
		}
		return result;
	}
	
	private static double[][] modEachElement(double[][] mat, double modFactor){
		int height = mat.length;
		int width = mat[0].length;
		double[][] result = new double[height][width];
		
		for(int row=0; row < height; row++){
			for(int col=0; col < width; col++){
				result[row][col] = mat[row][col] % modFactor;
			}
		}
		return result;
	}
	
	public static int modinv(int num, int prime){
		int ai = repsq(num, prime-2, prime);
		return ai % prime;
	}
	
	public static int repsq(int a, int b, int prime){
		if(b==0)
			return a*0+1;
		else if(b % 2 == 1)
			return (repsq(a,b-1,prime)*a) % prime;
		else
			return ((int)Math.pow(repsq(a,b/2,prime), 2)) % prime;
	}
	
	public static void print2Dmatrix(String name, double[][] mat, int height, int width){
		System.out.println("\nPRINT MATRIX - " + name);
		for(int i=0; i < height; i++){
			for(int j=0; j < width; j++){
				System.out.print(mat[i][j] + "   ");
			}
			System.out.print("\n");
		}
	}
}

