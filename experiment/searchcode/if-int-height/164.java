package alpha1;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ImageNegatives 
{
	public int[][] getRGB(File file,int index) 
	{
		BufferedImage buf=null;
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
	
	public static int  findMax(int inputValue[][],int height, int width)
	{
		int value =-100;
		
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				if(value<inputValue[i][j])
					value=inputValue[i][j];
			}
		}
		
		
		return value;
		
	}
	
	
	public int [][] getNegetive(int [][]inputValue,int height,int width)
	{
		int storeResult[][]=new int [height][width];
		
		int maxPixel = findMax(inputValue,height,width);
		
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				storeResult[i][j]=maxPixel-inputValue[i][j];
			}
		}
		
		
		return storeResult;
	}
	
	
	
	
	
	
	public void getImage(int resultNegRed[][],int resultNegGreen[][],int resultNegBlue[][],int height ,int width,String outputName, String extension) throws IOException
	{
	
		int [][]result  = new int[height][width];

		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				result[h][w]= ((resultNegRed[h][w] <<16 & 0xff0000)+
						(resultNegGreen[h][w]<<8 & 0x00ff00 ) +
						(resultNegBlue[h][w] & 0x0000ff));

			}
		}
		

		File outputFile = new File(outputName);     /*Initializing the output image named as "out.bmp"*/
		BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);


		for(int h = 1;h<height-1;h++)

		{
			for(int w = 1;w<width-1;w++)
			{
				img.setRGB(w,h,(result[h][w]));
			}
		}
		ImageIO.write(img, extension, outputFile);

	}


	
	
	public String  getNegativeImage(String imagePath, String imageName,String  extension)
	{
		
			File file = new File(imagePath);
			BufferedImage image =null;
			try {
				image = ImageIO.read(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ImageNegatives ngvtImage = new ImageNegatives(); 
		
			
			int width = image.getWidth(null);                 										/*Calculating the height and width of image */
			int height = image.getHeight(null);

			int storePixelRed[][]=new int [height][width];
			int storePixelGreen[][]=new int [height][width];
			int storePixelBlue[][]=new int [height][width];


			int [][]resultNegRed = new int [height][width];
			int [][]resultNegGreen = new int [height][width];
			int [][]resultNegBlue = new int [height][width];
			
			
			storePixelRed = ngvtImage.getRGB(file,1);
			storePixelGreen= ngvtImage.getRGB(file,2);
			storePixelBlue = ngvtImage.getRGB(file,3);
			
			String outPutImageName = imageName.concat("_negativeImage");
			String x= "";
			String y = x.concat(outPutImageName+"."+extension);
			
			resultNegRed = ngvtImage.getNegetive(storePixelRed,height,width);
			resultNegGreen = ngvtImage.getNegetive(storePixelGreen,height,width);
			resultNegBlue = ngvtImage.getNegetive(storePixelBlue,height,width);
			try {
				ngvtImage.getImage(resultNegRed, resultNegGreen, resultNegBlue, height, width ,y,extension);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			
			
			return y;

	}
}


