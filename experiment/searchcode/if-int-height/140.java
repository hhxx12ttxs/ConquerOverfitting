import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;



public class HistoEqualizationForRGB
{


	public int [] histogram(int[][] colorScale,int height, int width )
	{
		int [] pixNum = new int [256];

		for(int c = 0; c<256; c++)
		{
			int sum = 0;
			for(int h=0;h<height;h++) 
			{
				for(int w=0;w<width;w++)
					if(colorScale[h][w]==c) sum++;
			}
			pixNum[c] = sum;

		}

		return pixNum;
	}

	public int [] getCDF(int [] histogram)
	{

		int [] cdf = new int [256];
		int cum = 0;
		for(int i = 0; i<256; i++)
		{
			cum += histogram[i];
			cdf[i] = cum;
		}
		return cdf;
	}

	public int getMinCDF(int [] cdf)
	{
		int minCDF = 257;
		for(int i = 0; i<256; i++)
		{
			if(cdf[i]<minCDF && cdf[i]!=0) minCDF = cdf[i];
		}
		return minCDF;
	}

	public int getMaxCDF(int [] cdf)
	{
		int maxCDF = 0;
		for(int i = 0; i<256; i++)
		{
			if(cdf[i]>maxCDF) maxCDF = cdf[i];
		}
		return maxCDF;
	}

	public float[] equalization(int [] cdf, int pictSize)
	{
		int min = getMinCDF(cdf);
		float e [] = new float[256];

		for(int i = 0; i<256; i++)
		{
			e[i] = (float)((((float)cdf[i]-min)/(float)pictSize)*255);


		}
		for(int i = 0; i<256; i++)
		{
			if(e[i]<0) e[i]=0;
			if(e[i]>255) e[i]=255;	
		}
		return e;
	}

	public float [][] picEqualized(int [][]storePixel, float []equalization,int w, int h)
	{

		float [][] newGS = new float[h][w];

		for(int i = 0; i<h; i++)
		{
			for(int j = 0; j<w; j++)
			{

				newGS [i][j]=  equalization[storePixel[i][j]]; //convert

			}
		}
		return newGS;
	}

	/*public void drawImage(int [] newGS, int w, int h)throws IOException
	{

		int counter = 0;
		File outputFile = new File("out.bmp");
		BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

		for(int height = 1;height<h-1;height++)

		{
			for(int width = 1;width<w-1;width++)
			{
				img.setRGB(width,height,(int) (newGS[counter]));
				counter++;
			}
		}

		ImageIO.write(img, "bmp", outputFile);

	}*/

	public static void main (String args[]) throws IOException
	{

		HistoEqualizationForRGB	 he = new HistoEqualizationForRGB();
		File file = new File("C:/Documents and Settings/Administrator/Desktop/test/out.bmp");
		BufferedImage image = ImageIO.read(file);
		int width = image.getWidth(null);                 										/*Calculating the height and width of image */
		int height = image.getHeight(null);

		int size = width * height;

		int histogramRed [] = new int[256];
		int histogramGreen [] = new int[256];
		int histogramBlue [] = new int[256];

		int cdfRed [] = new int[256];
		int cdfGreen [] = new int[256];
		int cdfBlue [] = new int[256];

		float equalizedRed [] = new float[256];
		float equalizedGreen [] = new float[256];
		float equalizedBlue [] = new float[256];

		float[][] picEqualizedRed = new float[height][width];
		float [][]picEqualizedGreen  = new float[height][width];
		float [][]picEqualizedBlue  = new float[height][width];


		int [][] picEqualizedRedTemp = new int[height][width];
		int [][]picEqualizedGreenTemp  = new int[height][width];
		int [][]picEqualizedBlueTemp  = new int[height][width];


		int result [][] = new int[height][width];


		int storePixelRed[][]=new int [height][width];
		int storePixelGreen[][]=new int [height][width];
		int storePixelBlue[][]=new int [height][width];


		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				int clr=  image.getRGB(w,h); 	/*Pixel values finding*/
				int  red   = (clr & 0x00ff0000) >> 16;
			int  green = (clr & 0x0000ff00) >> 8;
			int  blue  = (clr & 0x000000ff);	  

			storePixelRed[h][w]=red;		
			storePixelGreen[h][w]=green;
			storePixelBlue[h][w]=blue;	


			
			}
		}

		histogramRed = he.histogram(storePixelRed,height,width);
		histogramGreen = he.histogram(storePixelGreen,height,width);
		histogramBlue = he.histogram(storePixelBlue,height,width);


		cdfRed = he.getCDF(histogramRed);
		cdfGreen = he.getCDF(histogramGreen);
		cdfBlue =he.getCDF(histogramBlue);


		equalizedRed = he.equalization(cdfRed, size);
		equalizedGreen = he.equalization(cdfGreen, size);
		equalizedBlue = he.equalization(cdfBlue, size);


		picEqualizedRed = he.picEqualized(storePixelRed,equalizedRed,width, height);
		picEqualizedGreen = he.picEqualized(storePixelGreen,equalizedGreen,width, height);
		picEqualizedBlue = he.picEqualized(storePixelBlue,equalizedBlue,width, height);


		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				picEqualizedRedTemp[h][w]=(int)picEqualizedRed[h][w];
				picEqualizedGreenTemp[h][w]=(int)picEqualizedGreen[h][w];
				picEqualizedBlueTemp[h][w]=(int)picEqualizedBlue[h][w];
			}
		}




		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				result[h][w]= ((picEqualizedRedTemp[h][w] <<16 & 0xff0000)+
						(picEqualizedGreenTemp[h][w]<<8 & 0x00ff00 ) +
						(picEqualizedBlueTemp[h][w] & 0x0000ff));

			}
		}

		File outputFile = new File("out.bmp");     /*Initializing the output image named as "out.bmp"*/
		BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);


		for(int h = 1;h<height-1;h++)

		{
			for(int w = 1;w<width-1;w++)
			{
				img.setRGB(w,h,(result[h][w]));
			}
		}
		ImageIO.write(img, "bmp", outputFile);



	}
}

