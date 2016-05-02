package alpha1;



import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageThresholding 
{
	
	public int[][] getRGB(File file,int index) 
	{
		BufferedImage buf=null;
		try {
			buf = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int width = buf.getWidth();
		int height = buf.getHeight();
		int c = 0,sum=11111110;
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
					if(rgb[h][w]<sum)
						sum=rgb[h][w];
					
					
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
	
	
	public int findThreshold(int inputPixel[][],int height,int width, double initialValue)
	{
		int threshold=0,counter=0,size=height*width;
		double error=4;
		
		double mean1 = 0,mean2=0,globalMeanPresent=0;
		int []input = new int [height*width];
		
		for(int h = 0; h<height; h++)
		{
			for(int w = 0; w<width ; w++)
			{
				input[counter]=inputPixel[h][w];
				counter++;
			}
		}
	
		
	
		
		while(error!=0)
		{
			int []temp1 = new int [height*width];
			int []temp2 = new int [height*width];
			int sum1=0,sum2=0,count1=0,count2=0;
			
			for(int i=0;i<size;i++)
			{
				if(input[i]>initialValue)
				{
					temp1[i]=input[i];
					sum1=sum1+temp1[i];
					count1++;
				}
				else
				{
					temp2[i]=input[i];
					sum2=sum2+temp2[i];
					count2++;
				}
			}
			
			if(count1!=0)
				mean1=(sum1/count1);
			else 
				count1=count2;
			if(count2!=0)
				mean2=(sum2/count2);
			else 
				count2=count1;
			
			
			globalMeanPresent = (mean1+mean2)/2;
			error=Math.abs(initialValue-globalMeanPresent);
			initialValue=globalMeanPresent;
		
		}
		
		threshold=(int)Math.ceil(globalMeanPresent);
		
		return threshold;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public String getThreshold(String path,String inputImageName,String extension)
	{
		
		BufferedImage inputImage = null;
		String imageName=path;
		String outPutImageName = inputImageName.concat("_imageThreshold");
		File input = new File(imageName);
		
		ImageThresholding thrshld = new ImageThresholding();
		
		
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
		
		
		int result [][] = new int[height][width];
		int initialValue = 125;
		
		
		storePixelRed = thrshld.getRGB(input,1);
		storePixelGreen= thrshld.getRGB(input,2);
		storePixelBlue = thrshld.getRGB(input,3);
		
		int threshold=thrshld.findThreshold(storePixelRed, height, width, initialValue);

		for(int h = 0; h<height; h++)
		{
			for(int w = 0; w<width ; w++)
			{
				if(storePixelRed[h][w]>threshold)
				{
					storePixelRed[h][w]=255;
					storePixelGreen[h][w]=255;
					storePixelBlue[h][w]=255;
				}
				else
				{
					storePixelRed[h][w]=0;
					storePixelGreen[h][w]=0;
					storePixelBlue[h][w]=0;
				}
			}
		}
		
		
		for(int h = 0;h<height;h++)
		{
			for(int w = 0;w<width;w++)
			{
				result[h][w]= ((storePixelRed[h][w] <<16 & 0xff0000)+
						(storePixelGreen[h][w]<<8 & 0x00ff00 ) +
						(storePixelBlue[h][w] & 0x0000ff));
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
				img.setRGB(w,h,(result[h][w]));
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



