package a10.s100502501;
import javax.swing.*;
import java.awt.*;
public class Histogram extends JPanel{
	private int[] count;
	public void showHistogram(int[] count){ //set the count and display histogram 
	    this.count=count;
	    repaint();
	}
	protected void paintComponent(Graphics g){
	    super.paintComponent(g);
	    int width=getWidth();
	    int height=getHeight();
	    int interval=(width-40)/count.length;
	    int individualWidth=(int)(((width-40)/24)*0.60);
	    int maxCount=0;
	    int r0=255,g0=0,b0=0;
	    for(int i=0;i<count.length;i++){ //the maximum count has the highest bar
	      if(maxCount<count[i])
	        maxCount=count[i];
	    }
	    int x=30;
	    g.drawLine(10,height-45,width-10,height-45); // Draw a horizontal base line
	    for (int i=0;i<count.length;i++){
	    	int barHeight=(int)(((double)count[i]/(double)maxCount)*(height-55)); //find the bar height
	    	if(i<5){ //set different color of different bar
	    		g.setColor(new Color(r0,g0,b0));
	    		g0+=51;
	    	}	
	    	else if(i>=5&&i<11){
	    		r0-=43;
	    		if(r0<0)
	    			r0=0;
	    		g.setColor(new Color(r0,g0,b0));
	    	}
	    	else if(i>=11&&i<15){
	    		b0+=51;
	    		g0-=22;
	    		g.setColor(new Color(r0,g0,b0));
	    	}
	    	else if(i>=15&&i<21){
	    		g0-=22;
	    		if(g0<0)
	    			g0=0;
	    		g.setColor(new Color(r0,g0,b0));
	    	}
	    	else if(i>=21&&i<26){
	    		r0+=25;
	    		g.setColor(new Color(r0,g0,b0));
	    	}	
	    	g.fillRect(x,height-45-barHeight,individualWidth,barHeight); //display a bar with rectangle)
	    	g.setColor(Color.black); //set color of letter
	    	g.drawString((char)(65+i)+"",x,height-30);
	    	x+=interval; //move to the next character
	    }
	}
	public Dimension getPreferredSize() {
	    return new Dimension(400,300);
	}
}
