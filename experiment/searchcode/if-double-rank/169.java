package org.crap4j.gauge;


/*
 * Originally from a Sun demo, but heavily modified by Dan, then by Bob.
 * @(#)JPEGFlip.java	1.6  98/12/03
 *
 * Copyright 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.crap4j.util.FormatUtil;


public class CrapImageGenerator {

  private static final String NEEDLE_GIF = "needle.gif";
	private static final String BACKGROUND_GIF = "meter_background.gif";
	private Image gauge;
	private Image needle;
	private float crapScore;
	private String dir;
  private String crapGaugeFilename = "CRI.png";
  private float crapPercentWarningThreshold;
  private float crapPercentCriticalThreshold;

	public CrapImageGenerator(float crapNumber, 
      String dir, 
      String filename, 
      float crapPercentWarningThreshold, 
      float crapPercentCriticalThreshold) {
		this.crapScore = crapNumber;
		this.dir = dir;
    this.crapPercentWarningThreshold = crapPercentWarningThreshold;
    this.crapPercentCriticalThreshold = crapPercentCriticalThreshold;

    if (isValidFilename(filename))
      this.crapGaugeFilename = filename;
		gauge = new ImageIcon(CrapImageGenerator.class.getResource(BACKGROUND_GIF)).getImage();
		needle = new ImageIcon(CrapImageGenerator.class.getResource(NEEDLE_GIF)).getImage();		
	}

  private boolean isValidFilename(String filename) {
    return !isEmpty(filename);
  }

  public void makeGaugeAndWriteToFile() {
    writeImageToFile(makeGauge());  
  }
  
  private void writeImageToFile(BufferedImage bi) {
      File file = new File(dir, crapGaugeFilename);
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(file);
        ImageIO.write(bi, "png", out); // writeAsJpeg(bi, out);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            e.printStackTrace();
          }  
        }
      }
  }

	private BufferedImage makeGauge() {
    int w = getGaugeWidth();
    int h = getGaugeHeight();

		BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D imageGraphics = bufferedImage.createGraphics();
    imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    imageGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		imageGraphics.setBackground(Color.white);
		imageGraphics.clearRect(0, 0, w, h);
    imageGraphics.drawImage(gauge, 0, 0, w, h, null);
    drawCrapOnGauge(imageGraphics, w, h, crapScore);
		drawCrapScoreOnGauge(imageGraphics, w, h, crapScore);
    drawNeedle(w, h, imageGraphics, crapScore / 10.0f);
		return bufferedImage;

	}

  private void drawNeedle(int w, int h, Graphics2D big, float crapScore2) {
    int translateWidth = w/2;
    int translateHeight = h/2;
		big.translate(translateWidth, translateHeight);
		big.rotate(convertDegrees(crapScore2));
		big.translate(-translateWidth, -translateHeight);
    
    int needleW = needle.getWidth(null);
    int needleH = needle.getHeight(null);
		big.drawImage(needle, 0, 0, needleW, needleH, null);
  }

  private void drawCrapScoreOnGauge(Graphics2D big, int width, int height, float crapScore2) {
	String crapScoreString = FormatUtil.getNumberFormatter().format(crapScore2)+"%";
    Font currFont = big.getFont();
    Font bigger = currFont.deriveFont(Font.PLAIN, 14);
    big.setFont(bigger);
    FontMetrics fm = big.getFontMetrics();
    Rectangle2D rect = fm.getStringBounds(crapScoreString, big);
    
    double startX = width/2-((rect.getWidth() + 4)/2);
    double startY = (height-35)-rect.getHeight();
    
    Color color = getBackgroundColor(crapScore2);
    big.setColor(color);
    big.fillRect((int)startX, (int)startY, (int)rect.getWidth()+6, (int)rect.getHeight()+5);
    big.setColor(Color.BLACK);
    big.drawRect((int)startX-1, (int)startY-1, (int)rect.getWidth()+6+1, (int)rect.getHeight()+6);
    
    big.setColor(Color.DARK_GRAY);
    big.drawRect((int)startX, (int)startY, (int)rect.getWidth()+6, (int)rect.getHeight()+4);
    big.setColor(Color.BLACK);
    big.drawRect((int)startX-1, (int)startY-1, (int)rect.getWidth()+6+2, (int)rect.getHeight()+6);
    
    big.setColor(Color.BLACK);
    big.drawString(crapScoreString, (int)(width/2-((rect.getWidth()-4)/2)), height-36);
  }

  private void drawCrapOnGauge(Graphics2D big, int width, int height, float crapScore2) {
    Font currFont = big.getFont();
    Font bigger = currFont.deriveFont(Font.ITALIC, 14);
    big.setFont(bigger);
    FontMetrics fm = big.getFontMetrics();
    String crap = "C.R.A.P.";
    Rectangle2D rect = fm.getStringBounds(crap , big);
    
    big.setColor(Color.BLACK);
    
    // flip the logo high or low depending on where the needle will be.
    int heightOfLogo = height - (int)(height * .65);
    if (isInUpperHalfOfGauge(crapScore2) )
      heightOfLogo = height - (int)(height * .3);
    big.drawString(crap, (float) (width/2 - rect.getWidth()/2), (float)heightOfLogo);
  }

  private boolean isInUpperHalfOfGauge(float crapScore2) {
    boolean upperHalfOfGauge = crapScore2 > 3.5 && crapScore2 < 6.5;
    return upperHalfOfGauge;
  }

  
	private Color getBackgroundColor(float crapScore2) {
    if (crapScore / 100.0 >= crapPercentCriticalThreshold)
      return Color.red;
    else if (crapScore / 100.0 >= crapPercentWarningThreshold)
      return Color.ORANGE;
    else
      return Color.GREEN;
	}

	private double convertDegrees(float crapScore2) {
		double rank = Math.min(10.0, crapScore2);
		double degrees = 216+(29.0 * rank);
		return (Math.PI*degrees)/180;
	}

  private int getGaugeHeight() {
    return gauge.getWidth(null);
  }

  private int getGaugeWidth() {
    return gauge.getHeight(null);
  }

//  private void writeAsJpeg(BufferedImage bi, FileOutputStream out) throws IOException {
//    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//    JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
//    param.setQuality(1.0f, false);
//    encoder.setJPEGEncodeParam(param);
//    encoder.encode(bi);
//  }
//
	private static void showUsage(){
		System.out.println("Usage: crapScore destination_directory");
	}
	
	public static void main(String argv[]) {
		validateArgs(argv);
		String crapScore = validateCrapScore(argv[0]);
		String dir = validateOutputDirectory(argv[1]);
		CrapImageGenerator demo = new CrapImageGenerator(Float.parseFloat(crapScore), dir, null, 5.0f, 15.0f);
//    demo.makeGaugeAndWriteToFile(); // standard usage.
    
    // In the main, let's see the image too!
    BufferedImage bi = demo.makeGauge();
    demo.writeImageToFile(bi);
    if (!isRunningHeadless())
      displayOnScreen(bi);
	}

  private static String validateOutputDirectory(String dir) {
		if (isEmpty(dir)) {
			showUsage();
			System.exit(0);
		}
    return dir;
  }

  private static String validateCrapScore(String crapScore) {
		if (isEmpty(crapScore)) {
			showUsage();
		}
    return crapScore;
  }

  private static boolean isEmpty(String s) {
    return s == null || s.length() == 0;
  }

  private static void validateArgs(String[] argv) {
    if (argv == null || argv.length == 0) {
			showUsage();
			System.exit(0);
		}
  }

  private static boolean isRunningHeadless() {
    String headlessProperty = System.getProperty("java.awt.headless");
    return headlessProperty != null && headlessProperty.toUpperCase().equals("true");
  }

  private static void displayOnScreen(BufferedImage bi) {
    JFrame frame = new JFrame();
    JPanel gaugePanel = new JPanel();
    gaugePanel.add(new JLabel(new ImageIcon(bi)));
		frame.getContentPane().add(gaugePanel);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.pack();
		frame.setVisible(true);
  }

}

