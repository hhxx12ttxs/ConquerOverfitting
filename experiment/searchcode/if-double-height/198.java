/**
 * This class creates the bufferimage used in Splot
 **/
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicLabelUI;

public class splotImage {
	private BufferedImage currentImg;
	private Color colorTable[] = new Color[6];
	
	public float scoreClasses[] = new float[12];
	
	/**
 	 * Constructor- creates a new BufferedImage based on the current calculated splot
 	 **/
	public splotImage(float[][] scoretable, Float high, Float low, int xwindows, int ywindows, Color[] colortable, int mersize, int wsize, String fileXname, String fileYname) {
		System.arraycopy(colortable, 0, colorTable, 0, 6);
		float colorClasses[] = new float[6];
		
		float temp;
		
		if((high == null) || (low == null)) {
			temp = (getLow(xwindows,ywindows,scoretable) < 0.00) 
				? (getHigh(xwindows,ywindows,scoretable) + (Math.abs(getLow(xwindows,ywindows,scoretable)))) / 10.0f 
						: (getHigh(xwindows,ywindows,scoretable) - (getLow(xwindows,ywindows,scoretable))) / 10.0f;
			setScoreClasses(getHigh(xwindows,ywindows,scoretable), getLow(xwindows,ywindows,scoretable), temp);
		}
		else {
			temp = (low < 0.00) ? (high + Math.abs(low)) / 10.0f : (high - low) / 10.0f;
			setScoreClasses(high.floatValue(), low.floatValue(), temp);
		}
		
		temp = 0.0f;
		if((high == null) || (low == null)) {
			temp = (getLow(xwindows,ywindows,scoretable) < 0.00) 
				? (getHigh(xwindows,ywindows,scoretable) + (Math.abs(getLow(xwindows,ywindows,scoretable)))) / 5.0f 
						: (getHigh(xwindows,ywindows,scoretable) - (getLow(xwindows,ywindows,scoretable))) / 5.0f;
			colorClasses[0] = getHigh(xwindows,ywindows,scoretable);
			colorClasses[5] = getLow(xwindows,ywindows,scoretable);
		}
		else {
			temp = (low < 0.00) ? (high + Math.abs(low)) / 5.0f : (high - low) / 5.0f;
			colorClasses[0] = high.floatValue();
			colorClasses[5] = low.floatValue();
		}
		// create color classes
		for(int i=1;i<5;i++) {
			colorClasses[i]= colorClasses[i-1]-temp;
		}
		
		// make image
		BufferedImage im = 
		      new BufferedImage(xwindows,ywindows,BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = im.getRaster();
		temp = 0.0f;
		for(int i=0; i<xwindows;i++) {
			for(int j=0;j<ywindows;j++) {
				temp = scoretable[i][j];
				if(temp>=colorClasses[0]) { // for top scores
					raster.setSample(i, (ywindows-1)-j, 0, colorTable[0].getRed());
					raster.setSample(i, (ywindows-1)-j, 1, colorTable[0].getGreen());
					raster.setSample(i, (ywindows-1)-j, 2, colorTable[0].getBlue());
				}
				if(temp<=colorClasses[5]) { // for bottom scores
					raster.setSample(i, (ywindows-1)-j, 0, colorTable[5].getRed());
					raster.setSample(i, (ywindows-1)-j, 1, colorTable[5].getGreen());
					raster.setSample(i, (ywindows-1)-j, 2, colorTable[5].getBlue());
				}
				else { // for everything else
					for(int k=0;k<5;k++) {
						if((temp<colorClasses[k])&&(temp>=colorClasses[k+1])) {
							double scoreTemp = ( temp - colorClasses[k+1] ) / ( ( (colorClasses[k] > 0.0f) ^ (colorClasses[k+1] > 0.0f) ) 
									? (Math.abs(colorClasses[k]) + Math.abs(colorClasses[k+1])) : (colorClasses[k] - colorClasses[k+1]) );
							raster.setSample(i, (ywindows-1)-j, 0, getColor(colorTable[k], colorTable[k+1], scoreTemp).getRed());
							raster.setSample(i, (ywindows-1)-j, 1, getColor(colorTable[k], colorTable[k+1], scoreTemp).getGreen());
							raster.setSample(i, (ywindows-1)-j, 2, getColor(colorTable[k], colorTable[k+1], scoreTemp).getBlue());
							break;
						}
					}
				}
			}
		}
		
		// save current image
		currentImg = im;
	}
	
	/*
	 * creates a new color that is a blend of the two given colors
	 * @param colorA color one to be blended
	 * @param colorB color two to be blended
	 * @return blended color
	 */
	private static Color getColor(Color colorA, Color colorB, double score) {
		int red,green,blue;
		// set the red band
		if (colorA.getRed()==colorB.getRed())
			red = colorA.getRed();
		else if(colorA.getRed()>colorB.getRed()) {
			red = colorA.getRed() - colorB.getRed();
			red = (int)((double)red * score) + colorB.getRed();
		}
		else {
			red = colorB.getRed() - colorA.getRed();
			red = (int)((double)red * score) + colorA.getRed();
		}
		// set the green band
		if(colorA.getGreen()==colorB.getGreen())
			green = colorA.getGreen(); 
		else if(colorA.getGreen()>colorB.getGreen()) {
			green = colorA.getGreen() - colorB.getGreen();
			green = (int)((double)green * score) + colorB.getGreen();
		}
		else {
			green = colorB.getGreen() - colorA.getGreen();
			green = (int)((double)green * score) + colorA.getGreen();
		}
		// set the blue band
		if(colorA.getBlue()==colorB.getBlue())
			blue = colorA.getBlue();
		else if(colorA.getBlue()>colorB.getBlue()) {
			blue = colorA.getBlue() - colorB.getBlue();
			blue = (int)((double)blue * score) + colorB.getBlue();
		}
		else {
			blue = colorB.getBlue() - colorA.getBlue();
			blue = (int)((double)blue * score) + colorA.getBlue();
		}
		return new Color(red, green, blue);
	}
	
	/*
	 * @return the highest score in the score matrix
	 */
	private float getHigh(int xwindows, int ywindows, float[][] score) {
		float temp = -1.00f;
		for(int i=0; i<xwindows;i++)
			for(int j=0;j<ywindows;j++)
				if(score[i][j]>temp) temp=score[i][j];
		return temp;
	}
	
	/*
	 * @return the lowest score in the score matrix
	 */
	private float getLow(int xwindows, int ywindows, float[][] score) {
		float temp = 1.00f;
		for(int i=0; i<xwindows;i++)
			for(int j=0;j<ywindows;j++)
				if(score[i][j]<temp) temp=score[i][j];
		return temp;
	}
	
	private void setScoreClasses(float high, float low, float temp) {
		//make colors
		scoreClasses[0] = high;
		for(int i=1;i<11;i++)
			scoreClasses[i]=scoreClasses[i-1]-temp;
		scoreClasses[11] = low;
	}
		
	//Standard getter for the displayPanel
	public JPanel getDisplay(double scale, int mersize, int wsize, String fileXname, String fileYname, JPanel displayPanel) {
		// for rescale
		if(scale!=1.0) {
			int width = currentImg.getWidth();
			int height = currentImg.getHeight();
			
			int newwidth = (int)((double)width*scale);
			int newheight = (int)((double)height*scale);
			
			//JPanel imagePane = new JPanel(new BorderLayout());
			//JScrollPane scrollPane = new JScrollPane(imagePane);
			
			BufferedImage newImage = new BufferedImage(newwidth,newheight,BufferedImage.TYPE_INT_RGB);
			
			Graphics2D g = (Graphics2D) newImage.getGraphics();
			g.drawImage(currentImg.getScaledInstance(newwidth, newheight, Image.SCALE_SMOOTH), 0, 0, null);
			
			currentImg = newImage;
		}
		
		// write image to jpanel
		JLabel header = new JLabel();
		if(wsize==-1) 
			header.setText("N-mer Size = " + mersize);
		else 
			header.setText("N-mer Size = " + mersize + ", Windows Size = " + wsize);
		header.setHorizontalAlignment(SwingConstants.CENTER);
		
		/*
		 * Vertical JLabel
		 * http://www.codeguru.com/java/articles/199.shtml
		 * Author: Zafir Anjum
		 */
		class VerticalLabelUI extends BasicLabelUI
		{
			//VerticalLabelUI labelUI = new VerticalLabelUI(false);
			
			protected boolean clockwise;
			VerticalLabelUI( boolean clockwise )
			{
				super();
				this.clockwise = clockwise;
			}
			

		    public Dimension getPreferredSize(JComponent c) 
		    {
		    	Dimension dim = super.getPreferredSize(c);
		    	return new Dimension( dim.height, dim.width );
		    }

		    private Rectangle paintIconR = new Rectangle();
		    private Rectangle paintTextR = new Rectangle();
		    private Rectangle paintViewR = new Rectangle();
		    private Insets paintViewInsets = new Insets(0, 0, 0, 0);

			public void paint(Graphics g, JComponent c) 
		    {
		        JLabel label = (JLabel)c;
		        String text = label.getText();
		        Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

		        if ((icon == null) && (text == null)) {
		            return;
		        }

		        FontMetrics fm = g.getFontMetrics();
		        paintViewInsets = c.getInsets(paintViewInsets);

		        paintViewR.x = paintViewInsets.left;
		        paintViewR.y = paintViewInsets.top;
		    	
		    	// Use inverted height & width
		        paintViewR.height = c.getWidth() - (paintViewInsets.left + paintViewInsets.right);
		        paintViewR.width = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

		        paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
		        paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

		        String clippedText = 
		            layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

		    	Graphics2D g2 = (Graphics2D) g;
		    	AffineTransform tr = g2.getTransform();
		    	if( clockwise )
		    	{
			    	g2.rotate( Math.PI / 2 ); 
		    		g2.translate( 0, - c.getWidth() );
		    	}
		    	else
		    	{
			    	g2.rotate( - Math.PI / 2 ); 
		    		g2.translate( - c.getHeight(), 0 );
		    	}

		    	if (icon != null) {
		            icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
		        }

		        if (text != null) {
		            int textX = paintTextR.x + ((int)frame.displayPanel.getHeight()/2)-(paintTextR.width/2);
		            int textY = paintTextR.y + fm.getAscent();

		            if (label.isEnabled()) {
		                paintEnabledText(label, g, clippedText, textX, textY);
		            }
		            else {
		                paintDisabledText(label, g, clippedText, textX, textY);
		            }
		        }
		    	
		    	g2.setTransform( tr );
		    }
		};
		
		JLabel vert = new JLabel(fileYname, SwingConstants.LEFT);
		vert.setUI( new VerticalLabelUI(false) );
		vert.setVerticalAlignment(SwingConstants.CENTER);
		
		JLabel fileX = new JLabel(fileXname);
		fileX.setHorizontalAlignment(SwingConstants.CENTER);
		
		JPanel imagePane = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(imagePane);
		scrollPane.setName("scrollPane");
		
		ImageIcon tempicon = new ImageIcon(currentImg);
		JLabel image = new JLabel(tempicon);
		image.setVerticalAlignment(SwingConstants.TOP);
		image.setHorizontalAlignment(SwingConstants.LEFT);
		
		Myheaders columnView = new Myheaders(Myheaders.HORIZONTAL, tempicon.getIconWidth(), wsize, scale);
		Myheaders rowView = new Myheaders(Myheaders.VERTICAL, tempicon.getIconHeight(), wsize, scale);
		columnView.setName("columnView");
		rowView.setName("rowView");
		
		scrollPane.setColumnHeaderView(columnView);
		scrollPane.setRowHeaderView(rowView);
		
		imagePane.setPreferredSize(new Dimension(currentImg.getWidth(),currentImg.getHeight()));
		imagePane.add(image, BorderLayout.CENTER);
		
		JViewport column = scrollPane.getColumnHeader();
		JViewport row = scrollPane.getRowHeader();
		
		JPanel scrollPanel = new JPanel(new BorderLayout());
		
		JPanel rowHeader = new JPanel(new BorderLayout());
		rowHeader.setLayout(new BoxLayout(rowHeader, BoxLayout.PAGE_AXIS));
		
		rowHeader.add(Box.createVerticalStrut(2));
		rowHeader.add(row);
		//row.setBackground(new Color(244,242,237));
		
		JPanel columnHeader = new JPanel();
		columnHeader.setLayout(new BoxLayout(columnHeader,BoxLayout.LINE_AXIS));
		
		columnHeader.add(Box.createRigidArea(new Dimension(77,35)));
		columnHeader.add(column);
		
		scrollPanel.add(rowHeader, BorderLayout.LINE_START);
		scrollPanel.add(scrollPane, BorderLayout.CENTER);
		scrollPanel.add(columnHeader, BorderLayout.PAGE_END);
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setName("contentPanel");
		contentPanel.add(scrollPanel, BorderLayout.CENTER);
		contentPanel.add(scrollPane.getVerticalScrollBar(), BorderLayout.LINE_END);
		contentPanel.add(scrollPane.getHorizontalScrollBar(), BorderLayout.PAGE_END);
		
		contentPanel.validate();
		
		displayPanel.removeAll();
		displayPanel.setLayout(new BorderLayout(5,3));
		displayPanel.add(header, BorderLayout.PAGE_START);
		displayPanel.add(vert, BorderLayout.LINE_START);
		displayPanel.add(contentPanel, BorderLayout.CENTER);
		displayPanel.add(fileX, BorderLayout.PAGE_END);
		
		arrowPanel arrow = new arrowPanel();
		arrow.setPreferredSize(new Dimension(150,contentPanel.getHeight()));
		displayPanel.add(arrow, BorderLayout.LINE_END);
		
		displayPanel.repaint();
		displayPanel.validate();
		
		return displayPanel;
	}
	
	// getter for the currentImg
	public BufferedImage getImage() {
		return currentImg;
	}
	
	class Myheaders extends JComponent {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int units = 100;
		protected static final int HORIZONTAL = 0;
		protected static final int VERTICAL = 1;
	    
		private int orientation;
	    private int increment;
	    private int wordsize;
	    private int SIZEV;
	    private int SIZEH;
	    private int length;
	    private double scale;
	    
	    public Myheaders(int o, int psize, int wsize, double s) {
	        orientation = o;
	        increment = units / 4;
	        if(o==1) {
	        	SIZEV = 75;
	        	length = psize;
	        	setPreferredSize(new Dimension(SIZEV, psize));
	        }
	        else {
	        	SIZEH = 35;
	        	length = psize;
	        	setPreferredSize(new Dimension(psize, SIZEH));
	        }
	        wordsize = wsize;
	        scale = s;
	    }
	    
		protected void paintComponent(Graphics g) {
	        Rectangle drawHere = g.getClipBounds();
	        
	        g.setColor(this.getParent().getBackground());
	        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);
	        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
	        
	        g.setColor(Color.BLACK);
    
	        int end = 0;
	        int start = 0;
	        int tickLength = 0;
	        int stop = (length % 100) - (length % increment);
	        String text = null;

	        if (orientation == HORIZONTAL) {
	            start = (drawHere.x / increment) * increment;
	            end = (((drawHere.x + drawHere.width) / increment) + 1) * increment;
	            if(start == 0){
	            	text = Integer.toString(0);
		        	tickLength = 10;
		        	// SIZEH-1
		        	g.drawLine(0, 0, 0, tickLength);
		            g.drawString(text, 2, 31);

		            text = null;
		            start = increment;
	            }
	        } else {
	            start = ((drawHere.y / increment) * increment) + (length % increment);
	            end = ((((drawHere.y + drawHere.height) / increment) + 1) * increment) + (length % increment);
	            if(end == length+increment) {
	            	text = Integer.toString(0);
		        	tickLength = 10;

		        	g.drawLine(SIZEV-1, length-1, SIZEV-tickLength-1, length-1);
		        	g.drawString(text, 9, length-1);
		        	
		        	text = null;
		        	end = (length+increment) - 2*increment;
	            }
	        }
	        
	        if (orientation == HORIZONTAL) {
	        	for (int i = start; i < end; i += increment) {
	        		text = Integer.toString((int)((((double)i)*((double)wordsize))/scale));
	        		if( (i % units) == 0 ) tickLength = 10;
	        		else tickLength = 5;
	        		g.drawLine(i, 0, i, tickLength);
	        		if( (i % units) == 0 && !(wordsize==-1)) g.drawString(text, i-3, 31);
	            }
	        }
	        if (orientation == VERTICAL) {
		        for (int i = end; i >= start; i -= increment) {
		        	text = Integer.toString((int)((((double)(length-i))*((double)wordsize))/scale));
		        	if( (i % units)-(length % increment) == stop ) tickLength = 10;
		            else tickLength = 5;
		        	g.drawLine(SIZEV-1, i, SIZEV-tickLength-1, i);
		        	
		        	if( (i % units)-(length % increment) == stop && (((length - i) * wordsize) >= 0) && !(wordsize==-1) ) g.drawString(text, 9, i+3);
		        }
	        }
		}
	}
	
	class arrowPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void paintComponent(Graphics g) {
			GeneralPath arrowA = new GeneralPath(GeneralPath.WIND_NON_ZERO ,5);
			GeneralPath arrowE = new GeneralPath(GeneralPath.WIND_NON_ZERO ,5);
			
			float initxy[] = { super.getSize().width, super.getSize().height };
			DecimalFormat data = new DecimalFormat("#0.00");
			
			Graphics2D arrow = (Graphics2D) g;
			
			arrowA.moveTo(Math.round(initxy[0]/3),Math.round((initxy[1]/7)-5)-(((initxy[0]/2)-(initxy[0]/6))/2));
			arrowA.lineTo(Math.round(initxy[0]/6),Math.round(initxy[1]/7)-5);
			arrowA.lineTo(Math.round(initxy[0]/2),Math.round(initxy[1]/7)-5);
			arrowA.closePath();
			
			arrowE.moveTo(Math.round(initxy[0]/3), Math.round(((initxy[1]-5)-(initxy[1]/7))+10)+(((initxy[0]/2)-(initxy[0]/6))/2));
			arrowE.lineTo(Math.round(initxy[0]/6),Math.round(((initxy[1]-5)-(initxy[1]/7))+10));
			arrowE.lineTo(Math.round(initxy[0]/2),Math.round(((initxy[1]-5)-(initxy[1]/7))+10));
			arrowE.closePath();
			
			arrow.setPaint(colorTable[0]);
			arrow.fill(arrowA);
			arrow.setPaint(new GradientPaint(Math.round(initxy[0]/3),Math.round(initxy[1]/7),colorTable[0],Math.round(initxy[0]/3),Math.round(2*(initxy[1]/7)),colorTable[1]));
			arrow.fill(new Rectangle2D.Double(Math.round(initxy[0]/6),Math.round(initxy[1]/7),Math.round(initxy[0]/3),(Math.round(initxy[1]/7))+3));
			arrow.setPaint(new GradientPaint(Math.round(initxy[0]/3),2*Math.round(initxy[1]/7),colorTable[1],Math.round(initxy[0]/3),Math.round(3*(initxy[1]/7)),colorTable[2]));
			arrow.fill(new Rectangle2D.Double(Math.round(initxy[0]/6),2*Math.round(initxy[1]/7),Math.round(initxy[0]/3),(Math.round(initxy[1]/7))+3));
			arrow.setPaint(new GradientPaint(Math.round(initxy[0]/3),3*Math.round(initxy[1]/7),colorTable[2],Math.round(initxy[0]/3),Math.round(4*(initxy[1]/7)),colorTable[3]));
			arrow.fill(new Rectangle2D.Double(Math.round(initxy[0]/6),3*Math.round(initxy[1]/7),Math.round(initxy[0]/3),(Math.round(initxy[1]/7))+3));
			arrow.setPaint(new GradientPaint(Math.round(initxy[0]/3),4*Math.round(initxy[1]/7),colorTable[3],Math.round(initxy[0]/3),Math.round(5*(initxy[1]/7)),colorTable[4]));
			arrow.fill(new Rectangle2D.Double(Math.round(initxy[0]/6),4*Math.round(initxy[1]/7),Math.round(initxy[0]/3),(Math.round(initxy[1]/7))+3));
			arrow.setPaint(new GradientPaint(Math.round(initxy[0]/3),5*Math.round(initxy[1]/7),colorTable[4],Math.round(initxy[0]/3),Math.round(6*(initxy[1]/7)),colorTable[5]));
			arrow.fill(new Rectangle2D.Double(Math.round(initxy[0]/6),5*Math.round(initxy[1]/7),Math.round(initxy[0]/3),(Math.round(initxy[1]/7))));
			arrow.setPaint(colorTable[5]);
			arrow.fill(arrowE);
			
			arrow.setPaint(Color.BLACK);
			arrow.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			arrow.drawString(data.format(scoreClasses[0]),3*Math.round(initxy[0]/4),Math.round(initxy[1]/7));
			arrow.drawString(data.format(scoreClasses[1]),3*Math.round(initxy[0]/4),3*Math.round(initxy[1]/14));
			arrow.drawString(data.format(scoreClasses[2]),3*Math.round(initxy[0]/4),2*Math.round(initxy[1]/7));
			arrow.drawString(data.format(scoreClasses[3]),3*Math.round(initxy[0]/4),5*Math.round(initxy[1]/14));
			arrow.drawString(data.format(scoreClasses[4]),3*Math.round(initxy[0]/4),3*Math.round(initxy[1]/7));
			arrow.drawString(data.format(scoreClasses[5]),3*Math.round(initxy[0]/4),7*Math.round(initxy[1]/14));
			arrow.drawString(data.format(scoreClasses[6]),3*Math.round(initxy[0]/4),4*Math.round(initxy[1]/7));
			arrow.drawString(data.format(scoreClasses[7]),3*Math.round(initxy[0]/4),9*Math.round(initxy[1]/14));
			arrow.drawString(data.format(scoreClasses[8]),3*Math.round(initxy[0]/4),5*Math.round(initxy[1]/7));
			arrow.drawString(data.format(scoreClasses[9]),3*Math.round(initxy[0]/4),11*Math.round(initxy[1]/14));
			arrow.drawString(data.format(scoreClasses[10]),3*Math.round(initxy[0]/4),6*Math.round(initxy[1]/7));
			
			arrow.scale(0.1,0.1);
		}
	}
}

