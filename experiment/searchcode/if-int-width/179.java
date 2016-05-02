
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

		int storePixelRedOut[][]=new int [height][width];
		int storePixelGreenOut[][]=new int [height][width];
		int storePixelBlueOut[][]=new int [height][width];
		
		int storePixelRedTemp[][]=new int [height][width];
		int storePixelGreenTemp[][]=new int [height][width];
		int storePixelBlueTemp[][]=new int [height][width];
		


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
		int [][]seedPointsRed  = new int[height][width];
		int [][]seedPointsGreen  = new int[height][width];
		int [][]seedPointsBlue  = new int[height][width];

		int [][]labelsRed  = new int[height][width];
		int [][]labelsGreen  = new int[height][width];
		int [][]labelsBlue  = new int[height][width];
		System.out.println("----------------------CHK--------------------------");
		System.out.println("Seed Red :"+storePixelRed[36][139]);
		System.out.println("Seed Green :"+storePixelGreen[36][139]);
		System.out.println("Seed Blue :"+storePixelBlue[36][139]);
		
		for(int h = 1;h<height-1;h++)

		{
			for(int w = 1;w<width-1;w++)
			{
				result[h][w]=0;
			}
		}
		int count =0;
		System.out.println(storeCoordinates.size());
		while(storeCoordinates.size() > 0)
		{
			Point thisPoint = storeCoordinates.get(0); 
			storeCoordinates.remove(0);
			int x =thisPoint.x-4;
			int y =thisPoint.y-36;
			
			
			
			
			GrowImageForARegion  grwImgRegn = new GrowImageForARegion();
			int thresholdRed=threshold[count];
			int thresholdGreen=threshold[count];
			int thresholdBlue=threshold[count];
			resultTemp =grwImgRegn.Region(storePixelRed,storePixelGreen,storePixelBlue,x,y,height,width,thresholdRed,thresholdGreen,thresholdBlue);
			
			for(int h = 1;h<height-1;h++)

			{
				for(int w = 1;w<width-1;w++)
				{
					if(result[h][w]==0)
						result[h][w]=resultTemp[h][w];
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








