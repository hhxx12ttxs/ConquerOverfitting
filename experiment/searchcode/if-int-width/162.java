import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.ImageIO;


public class FloodFill 
{
	public static void main(String args[])
	{
		BufferedImage image = null;
		String imageName="C:/Documents and Settings/Administrator/Desktop/test/floodfill.bmp";
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
		
		int seddX=0;
		int seedY=0;
		
		Stack<Point> storeConnected = new Stack<Point>();
		

		for(int height = 0;height<h;height++)
		{
			for(int width = 0;width<w;width++)
			{
				
				if(((storePixelRed[height][width]<=storePixelRed[seedY][seddX]+5)&&(storePixelRed[height][width]>=storePixelRed[seedY][seddX]-5))&&((storePixelGreen[height][width]<=storePixelGreen[seedY][seddX]+5)&&(storePixelGreen[height][width]>=storePixelGreen[seedY][seddX]-5))&&((storePixelBlue[height][width]<=storePixelBlue[seedY][seddX]+5)&&storePixelBlue[height][width]>=storePixelBlue[seedY][seddX]-5))
				{
					
				}
				else
				{
					storeConnected.add(new Point(width,height));
				}
			}
		}
		
		
		
		
		
		
	}

}

