import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import javax.imageio.ImageIO;


public class ImageGrowing1 
{

	private static long threshold=1;

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






	public static void main(String args[]) throws IOException
	{
		File file = new File("C:/Documents and Settings/Administrator/Desktop/test/src.jpg");
		BufferedImage image=null;
		try 
		{
			image = ImageIO.read(file);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		int width = image.getWidth(null);                 										/*Calculating the height and width of image */
		int height = image.getHeight(null);

		int storePixelRed[][]=new int [height][width];
		int storePixelGreen[][]=new int [height][width];
		int storePixelBlue[][]=new int [height][width];
		
		int storePixelRedOut[][]=new int [height][width];
		int storePixelGreenOut[][]=new int [height][width];
		int storePixelBlueOut[][]=new int [height][width];




		ImageGrowing1 imgGrng = new ImageGrowing1();

		storePixelRed = imgGrng.getRGB(file,1);
		storePixelGreen= imgGrng.getRGB(file,2);
		storePixelBlue = imgGrng.getRGB(file,3);

		int [][]result  = new int[height][width];
		int [][]seedPointsRed  = new int[height][width];
		int [][]seedPointsGreen  = new int[height][width];
		int [][]seedPointsBlue  = new int[height][width];

		int [][]labelsRed  = new int[height][width];
		int [][]labelsGreen  = new int[height][width];
		int [][]labelsBlue  = new int[height][width];
		
		int seedRed=251;
		int seedGreen=0;
		int seedBlue=239;




		for(int h=0;h<height;h++)
		{
			for(int w=0;w<width;w++)
			{
				if((storePixelRed[h][w]==seedRed)&&(storePixelGreen[h][w]==seedGreen)&&(storePixelBlue[h][w]==seedBlue))
				{
					seedPointsRed[h][w]=seedRed;
					seedPointsGreen[h][w]=seedGreen;
					seedPointsBlue[h][w]=seedBlue;
				}
				
				else
				{
					seedPointsRed[h][w]=0;
					seedPointsGreen[h][w]=0;
					seedPointsBlue[h][w]=0;
				}
						

				labelsRed[h][w] = -1;
				labelsGreen[h][w] = -1;
				labelsBlue[h][w] = -1;
			}

		}

		Stack<Point> storeConnectedRed = new Stack<Point>();
		Stack<Point> storeConnectedGreen = new Stack<Point>();
		Stack<Point> storeConnectedBlue = new Stack<Point>();


/*--------------------------------ENTER THE SEEDS IN QUEUE------------------------------*/

		for(int h=0;h<height;h++)
		{
			for(int w=0;w<width;w++)
			{
				if(seedPointsRed[h][w]==seedRed)
				{
					storeConnectedRed.add(new Point(w,h));

				}
				if(seedPointsGreen[h][w]==seedGreen)
				{
					storeConnectedGreen.add(new Point(w,h));

				}
				if(seedPointsBlue[h][w]==seedBlue)
				{
					storeConnectedBlue.add(new Point(w,h));

				}
			}
		}
/*--------------------------------NEXT OPERATION----------------------------------------*/

		while(storeConnectedRed.size() > 0)
		{
			Point thisPointRed = storeConnectedRed.get(0); 
			storeConnectedRed.remove(0);

			for(int th=-1;th<=1;th++)
			{
				for(int tw=-1;tw<=1;tw++)
				{
					int rx = thisPointRed.x+tw;
					int ry = thisPointRed.y+th;// Skip pixels outside of the image.


					if ((rx < 0) || (ry < 0) || (ry>=height) || (rx>=width)) continue;
					if (labelsRed[ry][rx] < 0) 
					{
						if (Math.abs(storePixelRed[ry][rx]-seedRed)<threshold)
						{ 
							storeConnectedRed.add(new Point(rx,ry));
							seedPointsRed[ry][rx]=seedRed;
							labelsRed[ry][rx]=1;
						}
						else 
							seedPointsRed[ry][rx]=0;
					}
				} 
			}	
		}
			
		
		while(storeConnectedGreen.size() > 0)
		{	
			
			
			Point thisPointGreen = storeConnectedGreen.get(0); 
			storeConnectedGreen.remove(0);
			
			

			
			for(int th=-1;th<=1;th++)
			{
				for(int tw=-1;tw<=1;tw++)
				{
					int rx = thisPointGreen.x+tw;
					int ry = thisPointGreen.y+th;// Skip pixels outside of the image.


					if ((rx < 0) || (ry < 0) || (ry>=height) || (rx>=width)) continue;
					if (labelsGreen[ry][rx] < 0) 
					{
						if (Math.abs(storePixelGreen[ry][rx]-seedGreen)<threshold)
						{ 
							storeConnectedGreen.add(new Point(rx,ry));
							seedPointsGreen[ry][rx]=seedGreen;
							labelsGreen[ry][rx]=1;
						}
						else 
							seedPointsGreen[ry][rx]=0;
					}
				} 
			}	
		}
		
		while(storeConnectedBlue.size() > 0)
		{
			
			

			Point thisPointBlue = storeConnectedBlue.get(0); 
			storeConnectedBlue.remove(0);

			
			for(int th=-1;th<=1;th++)
			{
				for(int tw=-1;tw<=1;tw++)
				{
					int rx = thisPointBlue.x+tw;
					int ry = thisPointBlue.y+th;// Skip pixels outside of the image.


					if ((rx < 0) || (ry < 0) || (ry>=height) || (rx>=width)) continue;
					if (labelsBlue[ry][rx] < 0) 
					{
						if (Math.abs(storePixelBlue[ry][rx]-seedBlue)<threshold)
						{ 
							storeConnectedBlue.add(new Point(rx,ry));
							seedPointsBlue[ry][rx]=seedBlue;
							labelsBlue[ry][rx]=1;
						}
						else 
							seedPointsBlue[ry][rx]=0;
					}
				} 
			}		
		}


/*----------------------------------------IMAGE RECONSTRUCTION-----------------------------------------------*/



		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				result[h][w]= ((seedPointsRed[h][w] <<16 & 0xff0000)+
						(seedPointsGreen[h][w]<<8 & 0x00ff00 ) +
						(seedPointsBlue[h][w] & 0x0000ff));

			}
		}



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
		
		
/*-----------------------------------------GET THE EDGES OF NEW IMAGE-----------------------*/		
		
		
		
		

		
	}
}



