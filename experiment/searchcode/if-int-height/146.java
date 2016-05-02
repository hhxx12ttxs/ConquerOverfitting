import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ThresholdingLOG 
{
	public static void main(String args[])
	{

		BufferedImage image = null;
		String imageName="C:/Documents and Settings/Administrator/Desktop/test/lena.bmp";
		File input = new File(imageName);
		try {
			image = ImageIO.read(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		int w = image.getWidth(null);                 										/*Calculating the height and width of image */
		int h = image.getHeight(null);
		

			/*For storing pixel values */
		int storePixelRed[][]=new int [h][w];
		int storePixelGreen[][]=new int [h][w];
		int storePixelBlue[][]=new int [h][w];
		
		
		
		int storeMaskResultRed[][]=new int [h][w];
		int storeMaskResultGreen[][]=new int [h][w];
		int storeMaskResultBlue[][]=new int [h][w];/*For storing result after masking  */
		int  storeBinary[][]=new int [h][w];						    					/*For storing pixel values after comparing with threshold*/
	
		for(int height = 0;height<h;height++)
		{
			for(int width = 0;width<w;width++)
			{
				int clr=  image.getRGB(width,height); 	/*Pixel values finding*/
				int  red   = (clr & 0x00ff0000) >> 16;
				int  green = (clr & 0x0000ff00) >> 8;
				int  blue  = (clr & 0x000000ff);	  
				
				storePixelRed[height][width]=red;		
				storePixelGreen[height][width]=green;
				storePixelBlue[height][width]=blue;		
				
			}
		}
		
		/*for(int height = 0;height<h;height++)
		{
			for(int width = 0;width<w;width++)
			{
				if(storePixelRed[height][width]>=0)
					storePixelRed[height][width]=255;
				else if(storePixelRed[height][width]<0)
					storePixelRed[height][width]=0;
			}
		}
		
		for(int height = 0;height<h;height++)
		{
			for(int width = 0;width<w;width++)
			{
				
				if(storePixelGreen[height][width]>=0)
					storePixelGreen[height][width]=255;
				else if(storePixelGreen[height][width]<0)
					storePixelGreen[height][width]=0;
				
			}
		}
		
		
		for(int height = 0;height<h;height++)
		{
			for(int width = 0;width<w;width++)
			{

				if(storePixelBlue[height][width]>=0)
					storePixelBlue[height][width]=255;
				else if(storePixelBlue[height][width]<0)
					storePixelBlue[height][width]=0;
			}
		}
		*/
		
		for(int height = 1;height<h-1;height++)
		{
			for(int width = 1;width<w-1;width++)
			{
				storeBinary[height][width]=((storePixelRed[height][width]<<16 & 0xff0000)+
												  (storePixelGreen[height][width]<<8 & 0x00ff00 ) +
												  (storePixelBlue[height][width] & 0x0000ff));
				
			}
		}
		
		
		File outputFile = new File("c:/outThreshold.bmp");     /*Initializing the output image named as "out.bmp"*/
		BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);


		for(int x = 1;x<h-1;x++)

		{
			for(int y = 1;y<w -1;y++)
			{
				img.setRGB(y,x,(storeBinary[x][y]));
			}
		}
		try {
			ImageIO.write(img, "bmp", outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		

	}



