package alpha1;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class LogTransform 
{

	public int[][] getRGB(File file,int index) 
	{

		BufferedImage buf = null;
		try {
			buf = ImageIO.read(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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



	public float[][] applyLogTransform(float pixelValues[][], int height,int width)
	{
		float [][]logTransform = new float [height][width];
		float [][]logTransformTemp = new float [height][width];
		
		
		//double constant =255/(Math.log10(100));
		double temp=-1;

		for(int h = 0; h<height; h++)
		{
			for(int w = 0; w<width ; w++)
			{
				logTransformTemp[h][w] = (float)Math.floor((1*( (Math.log10(pixelValues[h][w]+1)))));                        ///(Math.log10(1.02195252256666132))
				if (logTransform[h][w]>temp)
					temp=logTransform[h][w];
			}
		}
		
		
		
		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				logTransform[h][w]=(float) Math.ceil((255*logTransformTemp[h][w])/temp);
			}
		}
		
		
		
		return logTransform;
	}




	public String  getImage(float resultLogRed[][],float resultLogGreen[][],float resultLogBlue[][],int height ,int width, String outPutImageName, String extension)
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
		

		String x= "";
		String y = x.concat(outPutImageName+"."+extension);
		File outputFile = new File(y);     /*Initializing the output image named as "out.bmp"*/
		BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);


		for(int h = 1;h<height-1;h++)

		{
			for(int w = 1;w<width-1;w++)
			{
				img.setRGB(w,h,(result[h][w]));
			}
		}
		try {
			ImageIO.write(img, "bmp", outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return y;
	}



	public  String  getLogTransform(String path,String inputImageName,String extension) throws IOException
	{

		BufferedImage inputImage = null;
		String imageName=path;
		String outPutImageName = inputImageName.concat("_LogTransformation");
		File input = new File(imageName);
		
		try
		{
			inputImage = ImageIO.read(input);

		} 
		catch (IOException e)
		{

			System.out.println("Error:"+e.getMessage());
		}
		
		int width = inputImage.getWidth(null);                 										/*Calculating the height and width of image */
		int height = inputImage.getHeight(null);

		int storePixelRed[][]=new int [height][width];
		int storePixelGreen[][]=new int [height][width];
		int storePixelBlue[][]=new int [height][width];

		float [][]getAverage = new float[height][width];

		float [][]resultLogRed = new float [height][width];
		float [][]resultLogGreen = new float [height][width];
		float [][]resultLogBlue = new float [height][width];
		String outPut=null;

		storePixelRed = getRGB(input,1);
		storePixelGreen= getRGB(input,2);
		storePixelBlue = getRGB(input,3);
		

		getAverage = getAverage(storePixelRed, storePixelGreen, storePixelBlue, height, width);
		
		resultLogRed = applyLogTransform(getAverage, height, width);
		resultLogGreen = applyLogTransform(getAverage, height, width);
		resultLogBlue = applyLogTransform(getAverage, height, width);
		outPut = getImage(resultLogRed, resultLogGreen, resultLogBlue, height, width,outPutImageName,extension);
		
		
		return outPut;
	}

}

