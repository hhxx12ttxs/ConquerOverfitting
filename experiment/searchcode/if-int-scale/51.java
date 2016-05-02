//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.plexiViewer.lite.gui;

import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;
import java.util.Properties;


public class PlexiPrinter {
	
	private ImagePlus imp;
	private static Properties printPrefs = new Properties();
	private double scaling = 100.0;
	private boolean drawBorder;
	private  boolean center = true;

	public PlexiPrinter() {
		
	}

	public PlexiPrinter(ImagePlus image) {
		imp = image;
	}
	
	public void print(Image img, String title) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		PrintJob job = toolkit.getPrintJob(new Frame(), title, printPrefs);
		if (job==null)
			return;
		Graphics g = job.getGraphics();
		if (g==null)
			return;
		Dimension pageSize = job.getPageDimension();
		double scale = scaling/100.0;

		int width = img.getWidth(null);
		int height = img.getHeight(null);
		int margin = 20;
		int labelHeight = 0;
		int printWidth = (int)(width*scale);
		int printHeight = (int)(height*scale);

		int maxWidth = pageSize.width-margin*2;
		int maxHeight = pageSize.height-(margin+labelHeight)*2;
		g.setColor(Color.black);
			labelHeight = 15;
			g.setFont(new Font("SanSerif", Font.PLAIN, 12));
			g.drawString(title, margin+5, margin+labelHeight-3);
			if (center && width<maxWidth && height<maxHeight)
				 g.translate((pageSize.width-width)/2, labelHeight+(pageSize.height-height)/2);
			 else
				 g.translate(margin, margin+labelHeight);
			 if (drawBorder)
				 g.drawRect(-1, -1, (int)(printWidth)+1, (int)(printHeight)+1);
			 g.drawImage(img, 0, 0, printWidth, printHeight, null);
			 g.dispose();
			 job.end();
	}


	public  void print(ImagePlus imp) {
		  ImageWindow win = imp.getWindow();
		  if (win==null)
			  return;
		  ImageCanvas ic = win.getCanvas();
		  Toolkit toolkit = Toolkit.getDefaultToolkit();
		  PrintJob job = toolkit.getPrintJob(win, imp.getTitle(), printPrefs);
		  if (job==null)
			  return;
		  Graphics g = job.getGraphics();
		  if (g==null)
			  return;
		  Dimension pageSize = job.getPageDimension();
		 double scale = scaling/100.0;

		  int width = imp.getWidth();
		  int height = imp.getHeight();
		  Roi roi = imp.getRoi();
		  boolean crop = false;
		  int margin = 20;
		  int labelHeight = 0;
	 	  int printWidth = (int)(width*scale);
		  int printHeight = (int)(height*scale);

		  int maxWidth = pageSize.width-margin*2;
		  int maxHeight = pageSize.height-(margin+labelHeight)*2;
		  g.setColor(Color.black);
			  labelHeight = 15;
			  g.setFont(new Font("SanSerif", Font.PLAIN, 12));
			  g.drawString(imp.getTitle(), margin+5, margin+labelHeight-3);
		  ImageProcessor ip = imp.getProcessor();

		  if (width>maxWidth || height>maxHeight) {
			  // scale to fit page
			  double hscale = (double)maxWidth/width;
			  double vscale = (double)maxHeight/height;
			  if (hscale<=vscale)
				  scale = hscale;
			  else
				  scale = vscale;
			  printWidth = (int)(width*scale);
			  printHeight = (int)(height*scale);
			  if (System.getProperty("os.name").startsWith("Windows") && System.getProperty("java.version").startsWith("1.3.1")) {
				  // workaround for Windows/Java 1.3.1 printing bug
				  ip.setInterpolate(true);
				  ip = ip.resize(printWidth, printHeight);
			  }
		  }
		  Image img = ip.createImage();
		  if (center && width<maxWidth && height<maxHeight)
			  g.translate((pageSize.width-width)/2, labelHeight+(pageSize.height-height)/2);
		  else
			  g.translate(margin, margin+labelHeight);
		  if (drawBorder)
			  g.drawRect(-1, -1, (int)(printWidth)+1, (int)(printHeight)+1);
		  //g.setClip(0, 0, pageSize.width, pageSize.height);
		  //IJ.log(width+" "+height+" "+printWidth+" "+printHeight+" "+pageSize.width+" "+pageSize.height);
		  g.drawImage(img, 0, 0, printWidth, printHeight, null);
		  g.dispose();
		  job.end();
		  
	  }

  }


