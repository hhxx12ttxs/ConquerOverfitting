

import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

import java.io.*;

public class EdgeDetectionBW extends Panel 
{
public EdgeDetectionBW(){
	displayImage();
}


	BufferedImage  imageForDisplay;     
	public static void main(String args[]) throws IOException

	{ 

		File file= new File("C:/Documents and Settings/Administrator/Desktop/test/lena.bmp");
		BufferedImage image = ImageIO.read(file);
		
		int w = image.getWidth(null);                 								/*Calculating the height and width of image */
		int h = image.getHeight(null);

		double storePixel[][]=new double [h][w];			 								/*For storing pixel values */
		double storeMaskResult[][]=new double [h][w];		 								/*For storing result after masking  */
		int  storeBinary[][]=new int [h][w];						    					/*For storing pixel values after comparing with threshold*/


													/*Taking input image*/

		for(int height = 0;height<h;height++)
		{
			for(int width = 0;width<w;width++)
			{
				int clr=  image.getRGB(height,width); 										/*Pixel values finding*/
				int  red   = (clr & 0x00ff0000) >> 16;
			int  green = (clr & 0x0000ff00) >> 8;
			int  blue  = (clr & 0x000000ff);	  
			double pixelvalue = (0.21*red)+(0.71*green)+(0.07*blue);
			storePixel[height][width]=pixelvalue;
			}
		}



		int  sobelInX[][]= {{-1,0,1},{-2,0,2},{-1,0,1}};
		int  sobelInY[][]={{-1,-2,-1},{0,0,0},{1,2,1}};										/*Initializing Sobel mask*/
		double pixelInX=0, pixelInY=0;

		for(int height = 1;height<h-1;height++)
		{
			for(int width = 1;width<w-1;width++)
			{
				pixelInX= (sobelInX[0][0]*storePixel[height-1][width-1])+
						(sobelInX[0][1]*storePixel[height][width-1])+
						(sobelInX[0][2]*storePixel[height+1][width-1])+
						(sobelInX[1][0]*storePixel[height-1][width])+						/*Masking operation*/
						(sobelInX[1][1]*storePixel[height][width])+
						(sobelInX[1][2]*storePixel[height+1][width])+
						(sobelInX[2][0]*storePixel[height-1][width+1])+
						(sobelInX[2][1]*storePixel[height][width+1])+
						(sobelInX[2][2]*storePixel[height+1][width+1]);

				pixelInY= (sobelInY[0][0]*storePixel[height-1][width-1])+
						(sobelInY[0][1]*storePixel[height][width-1])+
						(sobelInY[0][2]*storePixel[height+1][width-1])+
						(sobelInY[1][0]*storePixel[height-1][width])+
						(sobelInY[1][1]*storePixel[height][width])+
						(sobelInY[1][2]*storePixel[height+1][width])+
						(sobelInY[2][0]*storePixel[height-1][width+1])+
						(sobelInY[2][1]*storePixel[height][width+1])+
						(sobelInY[2][2]*storePixel[height+1][width+1]);

				storeMaskResult[height][width]=Math.ceil((Math.sqrt((pixelInX*pixelInX)+(pixelInY*pixelInY))));

				int temp1 =(int)storeMaskResult[height][width];


				if(temp1>33)	 
					storeBinary[height][width]=1;											/*Considering 33 as threshold compare with values*/
				else 
					storeBinary[height][width]=0;
			}

		}	

		File outputFile = new File("C:/Documents and Settings/Administrator/Desktop/test/out.bmp");     /*Initializing the output image named as "out.bmp"*/

		BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);



		for(int i=0;i<h;i++)
		{
			for (int j=0;j<w;j++)
			{

				if(storeBinary[i][j]==1)
					img.setRGB(i,j,0);

				else if(storeBinary[i][j] == 0)															/*Construction of output image*/
					img.setRGB(i, j,0xffffff);
			}
		}
		ImageIO.write(img, "bmp", outputFile); 



		JFrame frame = new JFrame("Display image");
		
		Panel panel = new EdgeDetectionBW();	
		frame.getContentPane().add(panel);																/*Displaying output image*/
		frame.setSize(500, 500);
		frame.setVisible(true);
	}

/*------------------------Paint method------------------------------*/

	
	public void paint(Graphics g) 
	{
		g.drawImage( imageForDisplay, 0, 0, null);
	}


/*----------------------------Display method ------------------------*/	
	
	
	public void displayImage() {
		try 
		{
			String imageName = "C:/Documents and Settings/Administrator/Desktop/test/out.bmp";
			File input = new File(imageName);
			imageForDisplay = ImageIO.read(input);

		} 
		catch (IOException ie) 
		{
			System.out.println("Error:"+ie.getMessage());
		}
	}
}





