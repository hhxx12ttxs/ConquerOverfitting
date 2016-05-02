package alpha1;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class EdgeDetection {

	BufferedImage  imageForDisplay; 

	public String  detectEdge(String path,String inputImageName,String extension)
	{



		/*------------------------ Getting input input image path ---------------------------*/		
		BufferedImage inputImage = null;
		String imageName=path;
		String outPutImageName = inputImageName.concat("_EdgeDetect_sobel");
		File input = new File(imageName);
		try
		{
			inputImage = ImageIO.read(input);

		} 
		catch (IOException e)
		{

			System.out.println("Error:"+e.getMessage());
		}

		/*---------------------------Getting height and width of the image-------------------*/		

		int width = inputImage.getWidth(null);                 										
		int height = inputImage.getHeight(null);


		/*---------------------------String pixel values-------------------------------------*/

		int storePixelRed[][]=new int [height][width];
		int storePixelGreen[][]=new int [height][width];
		int storePixelBlue[][]=new int [height][width];



		int storeMaskResultRed[][]=new int [height][width];
		int storeMaskResultGreen[][]=new int [height][width];
		int storeMaskResultBlue[][]=new int [height][width];
		int  storeBinary[][]=new int [height][width];						    					

		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				int clr=  inputImage.getRGB(w,h); 	
				int  red   = (clr & 0x00ff0000) >> 16;
			int  green = (clr & 0x0000ff00) >> 8;
			int  blue  = (clr & 0x000000ff);	  

			storePixelRed[h][w]=red;		
			storePixelGreen[h][w]=green;
			storePixelBlue[h][w]=blue;		

			}
		}


		/*--------------------------------Sobel operator-------------------------------------*/

		int  sobelInY[][]= {{-1,0,1},{-2,0,2},{-1,0,1}};
		int  sobelInX[][]= {{-1,-2,-1},{0,0,0},{1,2,1}};										
		int pixelInX=0, pixelInY=0;

		for(int h = 1;h<height-1;h++)
		{
			for(int w = 1;w<width-1;w++)
			{
				pixelInX= 
						(sobelInX[0][0]*storePixelRed[h-1][w-1])+
						(sobelInX[0][1]*storePixelRed[h-1][w])+
						(sobelInX[0][2]*storePixelRed[h-1][w+1])+
						(sobelInX[1][0]*storePixelRed[h][w-1])+					
						(sobelInX[1][1]*storePixelRed[h][w])+
						(sobelInX[1][2]*storePixelRed[h][w+1])+
						(sobelInX[2][0]*storePixelRed[h+1][w-1])+
						(sobelInX[2][1]*storePixelRed[h+1][w])+
						(sobelInX[2][2]*storePixelRed[h+1][w+1]);

				pixelInY= 
						(sobelInY[0][0]*storePixelRed[h-1][w-1])+
						(sobelInY[0][1]*storePixelRed[h-1][w])+
						(sobelInY[0][2]*storePixelRed[h-1][w+1])+
						(sobelInY[1][0]*storePixelRed[h][w-1])+
						(sobelInY[1][1]*storePixelRed[h][w])+
						(sobelInY[1][2]*storePixelRed[h][w+1])+
						(sobelInY[2][0]*storePixelRed[h+1][w-1])+
						(sobelInY[2][1]*storePixelRed[h+1][w])+
						(sobelInY[2][2]*storePixelRed[h+1][w+1]);

				storeMaskResultRed[h][w]=(int)(Math.sqrt((pixelInX*pixelInX)+(pixelInY*pixelInY)));

				if(storeMaskResultRed[h][w]>225)
					storeMaskResultRed[h][w]=225;
				else if(storeMaskResultRed[h][w]<0)
					storeMaskResultRed[h][w]=0;

			}

		}



		for(int h = 1;h<height-1;h++)
		{
			for(int w= 1;w<width-1;w++)
			{
				pixelInX= (sobelInX[0][0]*storePixelGreen[h-1][w-1])+
						(sobelInX[0][1]*storePixelGreen[h-1][w])+
						(sobelInX[0][2]*storePixelGreen[h-1][w+1])+
						(sobelInX[1][0]*storePixelGreen[h][w-1])+						/*Masking operation*/
						(sobelInX[1][1]*storePixelGreen[h][w])+
						(sobelInX[1][2]*storePixelGreen[h][w+1])+
						(sobelInX[2][0]*storePixelGreen[h+1][w-1])+
						(sobelInX[2][1]*storePixelGreen[h+1][w])+
						(sobelInX[2][2]*storePixelGreen[h+1][w+1]);

				pixelInY= (sobelInY[0][0]*storePixelGreen[h-1][w-1])+
						(sobelInY[0][1]*storePixelGreen[h-1][w])+
						(sobelInY[0][2]*storePixelGreen[h-1][w+1])+
						(sobelInY[1][0]*storePixelGreen[h][w-1])+
						(sobelInY[1][1]*storePixelGreen[h][w])+
						(sobelInY[1][2]*storePixelGreen[h][w+1])+
						(sobelInY[2][0]*storePixelGreen[h+1][w-1])+
						(sobelInY[2][1]*storePixelGreen[h+1][w])+
						(sobelInY[2][2]*storePixelGreen[h+1][w+1]);

				storeMaskResultGreen[h][w]=(int) (Math.sqrt((pixelInX*pixelInX)+(pixelInY*pixelInY)));
				//String x=String.valueOf(storeMaskResultGreen[height][width]);

				if(storeMaskResultGreen[h][w]>225)
					storeMaskResultGreen[h][w]=225;
				else if (storeMaskResultGreen[h][w]<0)
					storeMaskResultGreen[h][w]=0;
			}

		}


		for(int h= 1;h<height-1;h++)
		{
			for(int w = 1;w<width-1;w++)
			{
				pixelInX= 
						(sobelInX[0][0]*storePixelBlue[h-1][w-1])+
						(sobelInX[0][1]*storePixelBlue[h-1][w])+
						(sobelInX[0][2]*storePixelBlue[h-1][w+1])+
						(sobelInX[1][0]*storePixelBlue[h][w-1])+						/*Masking operation*/
						(sobelInX[1][1]*storePixelBlue[h][w])+
						(sobelInX[1][2]*storePixelBlue[h][w+1])+
						(sobelInX[2][0]*storePixelBlue[h+1][w-1])+
						(sobelInX[2][1]*storePixelBlue[h+1][w])+
						(sobelInX[2][2]*storePixelBlue[h+1][w+1]);

				pixelInY= 
						(sobelInY[0][0]*storePixelBlue[h-1][w-1])+
						(sobelInY[0][1]*storePixelBlue[h-1][w])+
						(sobelInY[0][2]*storePixelBlue[h-1][w+1])+
						(sobelInY[1][0]*storePixelBlue[h][w-1])+
						(sobelInY[1][1]*storePixelBlue[h][w])+
						(sobelInY[1][2]*storePixelBlue[h][w+1])+
						(sobelInY[2][0]*storePixelBlue[h+1][w-1])+
						(sobelInY[2][1]*storePixelBlue[h+1][w])+
						(sobelInY[2][2]*storePixelBlue[h+1][w+1]);

				storeMaskResultBlue[h][w]=(int)Math.sqrt((pixelInX*pixelInX)+(pixelInY*pixelInY));

				if(storeMaskResultBlue[h][w]>225)
					storeMaskResultBlue[h][w]=225;
				else if(storeMaskResultBlue[h][w]<0)
					storeMaskResultBlue[h][w]=0;
			}

		}




		/*---------------------Image reconstruction----------------------------------------*/



		for(int h = 1;h<height-1;h++)
		{
			for(int w = 1;w<width-1;w++)
			{
				storeBinary[h][w]=(int) ((storeMaskResultRed[h][w]<<16 & 0xff0000)+
						(storeMaskResultGreen[h][w]<<8 & 0x00ff00 ) +
						(storeMaskResultBlue[h][w] & 0x0000ff));

			}
		}


		
		String x= "";
		String y = x.concat(outPutImageName+"."+extension);
		File outputFile = new File(y);     
		BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

		for(int h = 1;h<height-1;h++)

		{
			for(int w = 1;w<width-1;w++)
			{
				img.setRGB(w,h,(storeBinary[h][w]));
			}
		}

		try 
		{
			ImageIO.write(img, extension, outputFile);
		} 
		catch (IOException e) 
		{
			
			System.out.println("Error :"+e.getMessage());
			e.printStackTrace();
		} 
		return y;
	}
	


}



