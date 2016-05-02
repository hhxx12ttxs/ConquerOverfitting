package alpha1;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;




/*
 * THIS CLASS IS DOING THE SAME 
 * AS HISTOGRAM EQUALIZATION AND RECONSTRUCTING THE 
 * FINAL IMAGE.
 * METHOD "reconstruction" IS TAKING CDF AS INPUT
 * 
 */


public class ImageReconstruction 
{
	
	
	public int getMinCDF(int [] cdf)
	{
		int minCDF = 257;
		for(int i = 0; i<256; i++)
		{
			if(cdf[i]<minCDF && cdf[i]!=0) minCDF = cdf[i];
		}
		return minCDF;
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

	
	public String  reconstruction(int storePixelRedSp[][],int storePixelGreenSp[][],int storePixelBlueSp[][],int cdfRedSp[],int cdfGreenSp[],int cdfBlue[],int height,int width,String outPutImageName,String extension) throws IOException
	{
		
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



		int size = height*width;
		ImageReconstruction recons = new ImageReconstruction();
		equalizedRed=recons.equalization(cdfRedSp, size);
		equalizedGreen=recons.equalization(cdfGreenSp, size);
		equalizedBlue=recons.equalization(cdfBlue, size);
		
		picEqualizedRed = recons.picEqualized(storePixelRedSp,equalizedRed,width, height);
		picEqualizedGreen = recons.picEqualized(storePixelGreenSp,equalizedGreen,width, height);
		picEqualizedBlue = recons.picEqualized(storePixelBlueSp,equalizedBlue,width, height);

		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				picEqualizedRedTemp[h][w]=(int)picEqualizedRed[h][w];
				picEqualizedGreenTemp[h][w]=(int)picEqualizedGreen[h][w];
				picEqualizedBlueTemp[h][w]=(int)picEqualizedBlue[h][w];
				
				if(picEqualizedRedTemp[h][w]<0)
					picEqualizedRedTemp[h][w]=0;
				else if (picEqualizedRedTemp[h][w]>225)
					picEqualizedRedTemp[h][w]=225;
				
				if(picEqualizedGreenTemp[h][w]<0)
					picEqualizedGreenTemp[h][w]=0;
				else if (picEqualizedGreenTemp[h][w]>225)
					picEqualizedGreenTemp[h][w]=225;
				
				if(picEqualizedBlueTemp[h][w]<0)
					picEqualizedBlueTemp[h][w]=0;
				else if (picEqualizedBlueTemp[h][w]>225)
					picEqualizedBlueTemp[h][w]=225;
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

		String x= "";
		String y = x.concat(outPutImageName+"."+extension);
		File outputFile = new File(y);      /*Initializing the output image named as "out.bmp"*/
		BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);


		for(int h = 1;h<height-1;h++)

		{
			for(int w = 1;w<width-1;w++)
			{
				img.setRGB(w,h,(result[h][w]));
			}
		}
		ImageIO.write(img, extension, outputFile);	
		
		return y;
		
	}

}

