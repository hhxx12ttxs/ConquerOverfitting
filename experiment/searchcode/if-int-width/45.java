import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class LogTransform 
{

	public int[][] getRGB(File file,int index) throws IOException
	{
		BufferedImage buf = ImageIO.read(file);
		int width = buf.getWidth();
		int height = buf.getHeight();
		int c = 0;
		int [][] rgb = new int[height][width];
		for(int h = 0; h<height; h++)
		{
			for(int w = 0; w<width ; w++)
			{
				c = buf.getRGB(w,h);
				if (index==1)
				{
					int clr = (c&0x00ff0000)>>16;
			rgb[h][w]=clr;

				}
				else if (index==2)
				{
					int clr = (c&0x0000ff00)>>8;
					rgb[h][w]=clr;					
				}
				else
				{
					int clr = (c&0x000000ff);
					rgb[h][w]=clr;
				}					
			}
		}
		return rgb;
	}


	public float[][] getAverage(int storePixelRed[][],int storePixelGreen[][],int storePixelBlue[][],int height,int width)
	{
		float [][] result = new float [height][width];

		for(int h=0;h<height;h++)
		{
			for(int w=0;w<width;w++)
			{
				result[h][w] = (float)((storePixelRed[h][w]+storePixelGreen[h][w]+storePixelBlue[h][w])/3);
			}
		}

		return result;
	}



	public float[][] getLogTransform(float pixelValues[][], int height,int width)
	{
		float [][]logTransform = new float [height][width];
		double constant =255/(Math.log10(1+255));

		for(int h = 0; h<height; h++)
		{
			for(int w = 0; w<width ; w++)
			{
				logTransform[h][w] = (float)Math.floor((constant*( (Math.log10(pixelValues[h][w]+1)))));                        ///(Math.log10(1.02195252256666132))
				
			}
		}
		return logTransform;
	}




	public void getImage(float resultLogRed[][],float resultLogGreen[][],float resultLogBlue[][],int height ,int width) throws IOException
	{
		int [][] picEqualizedRedTemp = new int[height][width];
		int [][]picEqualizedGreenTemp  = new int[height][width];
		int [][]picEqualizedBlueTemp  = new int[height][width];
		
		int [][]result  = new int[height][width];


		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				picEqualizedRedTemp[h][w]=(int)resultLogRed[h][w];
				picEqualizedGreenTemp[h][w]=(int)resultLogGreen[h][w];
				picEqualizedBlueTemp[h][w]=(int)resultLogBlue[h][w];
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



	public static void main(String args[]) throws IOException
	{


		LogTransform logTrnsfrm = new LogTransform();


		File file = new File("C:/Documents and Settings/Administrator/Desktop/ah/Picture1.png");
		BufferedImage image = ImageIO.read(file);
		int width = image.getWidth(null);                 										/*Calculating the height and width of image */
		int height = image.getHeight(null);

		int storePixelRed[][]=new int [height][width];
		int storePixelGreen[][]=new int [height][width];
		int storePixelBlue[][]=new int [height][width];

		float [][]getAverage = new float[height][width];

		float [][]resultLogRed = new float [height][width];
		float [][]resultLogGreen = new float [height][width];
		float [][]resultLogBlue = new float [height][width];

		storePixelRed = logTrnsfrm.getRGB(file,1);
		storePixelGreen= logTrnsfrm.getRGB(file,2);
		storePixelBlue = logTrnsfrm.getRGB(file,3);

		getAverage = logTrnsfrm.getAverage(storePixelRed, storePixelGreen, storePixelBlue, height, width);

		resultLogRed = logTrnsfrm.getLogTransform(getAverage, height, width);
		resultLogGreen = logTrnsfrm.getLogTransform(getAverage, height, width);
		resultLogBlue = logTrnsfrm.getLogTransform(getAverage, height, width);

		for(int h = 0; h<height; h++)
		{
			for(int w = 0; w<width ; w++)
			{
				//System.out.println(resultLogBlue[h][w]);
			}
		}

		//System.out.println(resultLogBlue[0][0]);
		logTrnsfrm.getImage(resultLogRed, resultLogGreen, resultLogBlue, height, width);		
	}

}

