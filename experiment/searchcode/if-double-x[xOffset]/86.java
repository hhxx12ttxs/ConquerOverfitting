import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Calendar;
import java.util.Vector;


import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.ccnx.ccn.CCNFilterListener;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNVersionedInputStream;
import org.ccnx.ccn.profiles.CommandMarker;
import org.ccnx.ccn.profiles.SegmentationProfile;
import org.ccnx.ccn.profiles.metadata.MetadataProfile;
import org.ccnx.ccn.profiles.security.KeyProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;





public class ccnOsciloscope extends Panel implements Runnable,WindowListener{
	


    protected boolean _finished = false;
    //protected ContentName _prefix; 
    protected CCNHandle _handle;


    public static String defual_namespace="ccnx:/uiuc.edu/apps/southfarm/";
    public static String nodeid = "node106";
    public static String apps = "/phidget/";
    
    public static int _readsize = 256;
    
    
    Vector current_data = new Vector();
    Vector voltage_data = new Vector();
    
    public ccnOsciloscope()throws MalformedContentNameStringException, ConfigurationException, IOException {
        //_prefix = ContentName.fromURI(namespace);
        _handle = CCNHandle.open();

    }
    
    Image offscreen;
    Dimension offscreensize;
    Graphics offgraphics;
    
    int xoffset = 0;
    public void update(Graphics g){
    	paint(g);
    }
    public void paint(Graphics g) {
       	Dimension d = getSize();

       	//create the offscreen image if necessary (only done once)
    	if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
    		offscreen = createImage(d.width, d.height);
    		offscreensize = d;
    		if (offgraphics != null) {
    			offgraphics.dispose();
    		}
    		offgraphics = offscreen.getGraphics();
    		offgraphics.setFont(getFont());
    	}
    	//blank the screen.
    	offgraphics.setColor(Color.black);
    	offgraphics.fillRect(0, 0, d.width, d.height);

 
    	drawGridLines(offgraphics);
    	drawAxisAndTics(offgraphics);
    	drawLegend(offgraphics);
    	drawData(offgraphics, current_data, plotColors[0]);
    	drawData(offgraphics, voltage_data, plotColors[1]);
    	//transfer the constructed image to the screen.
    	g.drawImage(offscreen, 0, 0, null); 
    }


    public static int NUM_Y_TICKS = 40;
    public static int NUM_X_TICKS = 40;
    // Draw the grid lines
    void drawGridLines(Graphics offgraphics ) {
    	offgraphics.setColor(new Color((float)0.2, (float)0.6, (float)0.2));

    	for(int i=0; i<=NUM_X_TICKS; i++){    		
    		double x = (1-(double)i/NUM_X_TICKS)*getSize().width - xoffset;
    		offgraphics.drawLine((int)x, 0, (int)x, getSize().height);
    	}
    	for(int i=0; i<=NUM_Y_TICKS; i++){    		
    		double y = (1-(double)i/NUM_Y_TICKS)*getSize().height;
    		offgraphics.drawLine(0, (int)y, getSize().width, (int)y);
    	}

    }
    void drawGridLines2(Graphics offgraphics ) {
    	offgraphics.setColor(new Color((float)0.2, (float)0.6, (float)0.2));

    	double x =  getSize().width - xoffset;
    	while (x >0 ) {
    		offgraphics.drawLine((int)x, 0, (int)x, getSize().height);
    		x -= (getSize().width/NUM_X_TICKS);
    	}

    	double y = getSize().height;
    	while (y >0) {
    		offgraphics.drawLine(0, (int)y, getSize().width, (int)y);
    		y -= (getSize().height/NUM_Y_TICKS);
    	}
    }
    double MAX_Y_VALUE = 20;
    double MAX_X_POINTS = 100;
    
	// Draw the tic marks and numbers
    void drawAxisAndTics(Graphics offgraphics){

    	offgraphics.setFont(new Font("Default", Font.PLAIN, 10));
    	offgraphics.setColor(Color.white);
    	
    	for (int i=0; i < NUM_Y_TICKS; i++){
   			if (i % 2 ==0){
   				String tickstr = new Double((MAX_Y_VALUE/NUM_Y_TICKS ) * i).toString();
   				int xsub = 1;
   				double  y = (1-(double)i/NUM_Y_TICKS)*getSize().height;
   				offgraphics.drawString(tickstr, xsub, (int)y+4);
    		}
   		}	
    }
    
    void drawAxisAndTics2(Graphics offgraphics){

    	offgraphics.setFont(new Font("Default", Font.PLAIN, 10));
    	offgraphics.setColor(Color.white);
    	
    	double y = getSize().height;
    	int i = 0;
    	while (y>0) {
   			if (i % 2 ==0){
   				String tickstr = new Double((MAX_Y_VALUE/NUM_Y_TICKS ) * i).toString();
   				int xsub = 1;
   				offgraphics.drawString(tickstr, xsub, (int)y+3);
    		}
   			i ++;
    		y -= (getSize().height/NUM_Y_TICKS);
    	}	
    }

    void drawData(Graphics g, Vector data, Color color ){
    	
    	offgraphics.setColor(color);
    	
    	for(int i = data.size() -2; i >=0; i --){
    		double v = ((Double)data.get(i)).doubleValue();
    		double v2 = ((Double)data.get(i+1)).doubleValue();
    		Dimension d = getSize();
    		
    		double y = (1-v/MAX_Y_VALUE) * d.height;
    		double y2 = (1-v2/MAX_Y_VALUE) * d.height;
    		double x = (1- (data.size()-i)/MAX_X_POINTS) * d.width;
    		double x2 = (1- (data.size()-i-1)/MAX_X_POINTS) * d.width;
    		if ( (y > 100000) || (y2>100000) || (y<-100000) || (y2<-100000))
    			continue;
			g.drawLine((int)x, (int)y, (int)x2, (int)y2);
			if (x2 <0)
				break;
    	}
    }
    
    Color plotColors[] = {	Color.pink, Color.green, Color.yellow,  Color.red, Color.blue, Color.magenta, Color.orange ,
    		Color.cyan, Color.white};
    
    void drawLegend( Graphics offgraphics ) {


    	// Draw the legend
		int activeChannels=2;

		offgraphics.setColor(Color.black);
		offgraphics.fillRect( getSize().width-20-130, 15, 130, 20*activeChannels );
		offgraphics.setColor(Color.white);
		offgraphics.drawRect( getSize().width-20-130, 15, 130, 20*activeChannels );
		
		
		offgraphics.setColor(Color.white);
		offgraphics.drawString( "Current(A)", getSize().width-20-100, 30+17*0 );
		offgraphics.setColor(plotColors[0]);
		offgraphics.fillRect( getSize().width-20-120, 22+17*0, 10, 10 );

		offgraphics.setColor(Color.white);
		offgraphics.drawString( "Voltage(V)", getSize().width-20-100, 30+17*1 );
		offgraphics.setColor(plotColors[1]);
		offgraphics.fillRect( getSize().width-20-120, 22+17*1, 10, 10 );
		
    }    
    
    private int getReading(String command){
    	
    	ContentName _prefix;
		try {
			_prefix = ContentName.fromURI(defual_namespace+nodeid+apps+command);
		} catch (MalformedContentNameStringException e) {
			e.printStackTrace();
			return Integer.MIN_VALUE;
		}
		
    	 
		try {
			CCNVersionedInputStream input = new CCNVersionedInputStream(_prefix, _handle);
			input.setTimeout( (int)duty_cycle/2 ); 

		
			byte [] buffer = new byte[_readsize];
			ByteArrayOutputStream byteouts = new ByteArrayOutputStream();
			int readcount = 0;
			int readtotal = 0;

			while ((readcount = input.read(buffer)) != -1){
				readtotal += readcount;
				byteouts.write(buffer, 0, readcount);
			}
			input.close();
			String s = new String(buffer, 0, readtotal);
			try{
				int i =  Integer.parseInt(s);
				return i;
			}catch(NumberFormatException e){
				e.printStackTrace();
				return Integer.MIN_VALUE;
			}
		    	
		} catch (IOException e) {
			e.printStackTrace();
			return Integer.MIN_VALUE;
		}
		

    }
    
    
    long last_scheduled_time = 0 ;
    long duty_cycle = 1000; //1000 millisec
    boolean finished = false;
    public void run() {
    	while(!finished){
    		double current = getReading("current");
    		System.out.println("====current: " + current);
    		current = (500 - current)/500 * 25; 
    		current_data.add(new Double(current));
    		
    		double voltage = getReading("voltage");
    		System.out.println("====voltage: " + voltage);
    		voltage = (voltage-500)/500 * 30; 
    		voltage_data.add(new Double(voltage));
    		
    		try {
    			Calendar now = Calendar.getInstance();
    			if (now.getTimeInMillis() - last_scheduled_time <=duty_cycle )
					Thread.sleep(duty_cycle - (now.getTimeInMillis() - last_scheduled_time));
    			now = Calendar.getInstance();
    			last_scheduled_time = now.getTimeInMillis(); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			}
    		
    		//xoffset = (xoffset + NUM_X_TICKS * 4 / 5) % NUM_X_TICKS;
			xoffset =(int) (xoffset + getSize().width /MAX_X_POINTS) % ((int)getSize().width/NUM_X_TICKS);
    		this.repaint();
    		
    	}
    	
    	//will reach here when closing
    	System.exit(0);
//
//		while(true){
//			CCNFileInputStream input;
//			try {
//				input = new CCNFileInputStream(_prefix, _handle);
//			} catch (IOException e) {
//				e.printStackTrace();
//				continue;
//			}
////		if (timeout != null) {
////			input.setTimeout(timeout); 
////		}
//		
//			byte [] buffer = new byte[_readsize];
//			ByteArrayOutputStream byteouts = new ByteArrayOutputStream();
//			int readcount = 0;
//			long readtotal = 0;
//			try {
//				while ((readcount = input.read(buffer)) != -1){
//					readtotal += readcount;
//					byteouts.write(buffer, 0, readcount);
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//				continue;
//			}
//		
//			ByteArrayInputStream byteinps = new ByteArrayInputStream(byteouts.toByteArray());
//
//
//    		Calendar now = Calendar.getInstance();
//
//    		//if (now.get(Calendar.SECOND) == 1)
//    		
//    		this.repaint();
//    		
//    		try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}
    		
    }
		    
	


		public void windowActivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void windowClosed(WindowEvent e) {
			// TODO Auto-generated method stub
			System.out.println("========closed=================================");
		}

		public void windowClosing(WindowEvent e) {
			// TODO Auto-generated method stub
			System.out.println("========closing=================================");
			finished = true;
		}

		public void windowDeactivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void windowDeiconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void windowIconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void windowOpened(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}
    

		public static void parseArg(String[] args){
			int startArg = 0;
			
			for (int i = 0; i < args.length ; i++) {

				if (args[i].equals("-node")) {
					if (args.length < (i + 2)) {
						usage();
						System.exit(1);
					}
					nodeid = args[++i];
					if (startArg <= i)
						startArg = i + 1;
				}else {
					usage();
					System.exit(1);
				}
			}
			
			
		}

		private static void usage(){
			System.out.println("usage: java ccnCamDisplay [-node NodeID]");
		}

	    public static void main(String args[]) throws Exception {
	    	parseArg(args);
	    	
	    	JFrame frame = new JFrame("Sensors of " +nodeid);
    	
	    	Panel panel = new ccnOsciloscope();
	    	frame.addWindowListener((WindowListener)panel);
	    	frame.getContentPane().add(panel);
	    	frame.setSize(500, 400);
	    	frame.setVisible(true);
	    	
	    	Thread th = new Thread((Runnable)panel);
	    	th.start();
	  }
    
    
    
    
    
    /** A simple inner class representing a 2D point. */
    class Point2D {
      double x, y;

      Point2D(double newX, double newY) {
	x = newX;
	y = newY;
      }

      double getX() {
	return x;
      }

      double getY() {
	return y;
      }

      public String toString() {
	return x+","+y;
      }
    }
}



