package com.exedosoft.plat.util;

import javax.imageio.ImageIO;
import javax.imageio.IIOException;
import javax.swing.plaf.ComponentUI;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;

public class ImgTest {
	
	
	
	 public static BufferedImage rotateImg( BufferedImage image, int degree, Color bgcolor ){
		 
		  int iw = image.getWidth();//??????? 
		  int ih = image.getHeight();//???????  
		  int w=0;
		  int h=0; 
		  int x=0; 
		  int y=0; 
		  degree=degree%360;
		  if(degree<0)degree=360+degree;//??????0-360???
		  double ang=degree* 0.0174532925;//???????
		  
		  /**
		   *??????????????
		   */
		   
		  if(degree == 180|| degree == 0 || degree == 360){
		   w = iw; 
		   h = ih; 
		  }else if(degree == 90|| degree == 270){ 
		   w = ih; 
		   h = iw;  
		  }else{  
		   int d=iw+ih;  
		   w=(int)(d*Math.abs(Math.cos(ang)));
		   h=(int)(d*Math.abs(Math.sin(ang)));
		  }
		  
		  x = (w/2)-(iw/2);//??????
		  y = (h/2)-(ih/2); 
		  BufferedImage rotatedImage=new BufferedImage(w,h,image.getType()); 
		  Graphics gs=rotatedImage.getGraphics();
		  gs.setColor(bgcolor);
		  gs.fillRect(0,0,w,h);//???????????????
		  AffineTransform at=new AffineTransform();
		  at.rotate(ang,w/2,h/2);//????
		  at.translate(x,y); 
		  AffineTransformOp op=new AffineTransformOp(at,AffineTransformOp.TYPE_NEAREST_NEIGHBOR); 
		  op.filter(image, rotatedImage); 
		  image=rotatedImage;
		  return image;
		 }


	public static void main(String argv[]) throws IOException {
		
		Random r = new Random();
		File fi = new File("c:/bb" + r.nextInt() + ".txt"); // ????
//		BufferedImage bis = ImageIO.read(fi);
//		
//		ImgTest.rotateImg(bis,30,Color.white);
		fi.createNewFile();
			
			
		System.out.println(fi.getAbsolutePath());

		
		
		
		
		
//		try {
//			File fi = new File("c:/zhang.jpg"); // ????
//			File fo = new File("c:/imgTest.jpg"); // ??????????
//			int nw = 100;
//			AffineTransform transform = new AffineTransform();
//			BufferedImage bis = ImageIO.read(fi);
//			AffineTransformOp ato = new AffineTransformOp(transform, null);
//			
//			AffineTransform at=new AffineTransform();
//			  at.rotate(ang,w/2,h/2);//????
//			  at.translate(x,y); 
//			  AffineTransformOp op=new AffineTransformOp(at,AffineTransformOp.TYPE_NEAREST_NEIGHBOR); 
//			  op.filter(image, rotatedImage); 
//			  image=rotatedImage;
//			  return image;
//
//			
//			
//			BufferedImage bid = new BufferedImage(nw, nh,
//					BufferedImage.TYPE_3BYTE_BGR);
//			ato.filter(bis, bid);
//			ImageIO.write(bid, "jpeg", fo);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}

