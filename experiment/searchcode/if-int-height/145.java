package alpha2;


import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.ImageIO;


public class RegionGrow 
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






	public void getRegion(Stack<Point> storeCoordinates, String inputImagePath, int threshold[])
	{

		BufferedImage inputImage = null;
		String imageName=inputImagePath;
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

		
		int []meanRed  = new int[storeCoordinates.size()];
		int []meanGreen  = new int[storeCoordinates.size()];
		int []meanBlue  = new int[storeCoordinates.size()];



		RegionGrow rgnGrw = new RegionGrow();

		try {
			storePixelRed = rgnGrw.getRGB(input,1);
		} catch (IOException e) {

			e.printStackTrace();
		}
		try {
			storePixelGreen= rgnGrw.getRGB(input,2);
		} catch (IOException e) {

			e.printStackTrace();
		}
		try {
			storePixelBlue = rgnGrw.getRGB(input,3);
		} catch (IOException e) {

			e.printStackTrace();
		}

		int [][]result  = new int[height][width];
		int [][]resultTemp  = new int[height][width];
		
		
		System.out.println("----------------------CHK--------------------------");
		
		
		for(int h = 1;h<height-1;h++)

		{
			for(int w = 1;w<width-1;w++)
			{
				result[h][w]=0;
			}
		}
		int count =0,length=0;
		System.out.println(storeCoordinates.size());
		int addRed=0,addGreen=0,addBlue=0;

		while(storeCoordinates.size() > 0)
		{
			Point thisPoint = storeCoordinates.get(0); 
			storeCoordinates.remove(0);
			int x =thisPoint.x;
			int y =thisPoint.y;
			
			
			
			
			GrowImageForARegion  grwImgRegn = new GrowImageForARegion();
			int thresholdRed=threshold[count++];
			int thresholdGreen=threshold[count++];
			int thresholdBlue=threshold[count++];
			resultTemp =grwImgRegn.Region(storePixelRed,storePixelGreen,storePixelBlue,x,y,height,width,thresholdRed,thresholdGreen,thresholdBlue);
			
			int countRed=0,countGreen=0,countBlue=0;
			int maxRed=0,maxGreen=0,maxBlue=0;
			for(int h = 0; h<height; h++)
			{
				for(int w = 0; w<width ; w++)
				{
					int c =resultTemp[h][w];
					int clrRed = (c&0x00ff0000)>>16;
					addRed=addRed+clrRed;
					if(clrRed>maxRed)
					{
						maxRed=clrRed;
					}
						
					int clrGreen = (c&0x0000ff00)>>8;
					addGreen=addGreen+clrGreen;
					if(clrGreen>maxGreen)
					{
						maxGreen=clrGreen;
					}
					
				 	int clrBlue = (c&0x000000ff);
				 	addBlue=addBlue+clrBlue;
				 	if(clrBlue>maxBlue)
					{
				 		maxBlue=clrBlue;
					}
				 	
				 	if(clrRed!=0)
				 		countRed++;
				 	if(clrGreen!=0)
				 		countGreen++;
				 	if(clrBlue!=0)
				 		countBlue++;
				}					
			}
			
			meanRed[length]=maxRed;//(int)addRed/countRed;
			meanGreen[length]=maxGreen;//(int)addGreen/countGreen;
			meanBlue[length]=maxBlue;//(int)addBlue/countBlue;

			
			for(int h = 1;h<height-1;h++)

			{
				for(int w = 1;w<width-1;w++)
				{
					if(result[h][w]==0)
						result[h][w]=resultTemp[h][w];
				}
			}
			length++;
		}
		
/*----------------------------------FLOOD FILL-----------------------------------------*/
		System.out.println(length);
		
		int mintemp=0;
		
		for(int h = 0;h<height;h++)

		{
			for(int w = 0;w<width;w++)
			{
				if(result[h][w]==0)
				{
					int flag=0;
					for(int i=0;i<length;i++)
					{
						int tempRed=Math.abs(storePixelRed[h][w]-meanRed[i]);
						int tempGreen=Math.abs(storePixelGreen[h][w]-meanGreen[i]);
						int tempBlue=Math.abs(storePixelBlue[h][w]-meanBlue[i]);
						int totalTemp = tempRed+tempGreen+tempBlue;
						
						if(i==0)
						{
							mintemp=totalTemp;
							flag=i;
						}
						else if (totalTemp<mintemp)
						{
							mintemp=totalTemp;
							flag=i;
						}			
					}
					
					result[h][w]=((meanRed[flag] <<16 & 0xff0000)+
							(meanGreen[flag]<<8 & 0x00ff00 ) +
							(meanBlue[flag] & 0x0000ff));
					
					
				}
					
			}
		}

/*----------------------------------------IMAGE RECONSTRUCTION-----------------------------------------------*/







		File outputFile = new File("out_initial1.bmp");     
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
			e.printStackTrace();
		}

		System.out.println("Image ready ");


		

	}
}








