/**
 * Stuff Mark added
 *   - colors variable
 *   - getColors()
 *   - setColors()
 */

package view;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.lang.*;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Virtual extends JComponent implements Runnable, OutputModule {
	/*x and y coordinates of center*/ 
	public int x_coordinate;
	public int y_coordinate;
	private static JFrame f;
	private ArrayList<String> colors;
	
	/*coordinates of nose, left, and right*/
	public int[]x;
	public int[]y;
	
	/*direction of nose in degrees*/
	public int heading;
	
	/*quadrant or axis the nose is pointing in*/
	public int direction;
	
	int obstacle_x;
	int obstacle_y;
	ArrayList<Shape>shapes = new ArrayList<Shape>();
	
	/*obstacle perception fields*/
	public int[]leftx;
	public int[]lefty;
	public int[]rightx;
	public int[]righty;
	
	public Virtual(String filename){
		/*get obstacle coordinates from filename*/
		Scanner input = null;
		String currentDir = new File("").getAbsolutePath();
		System.out.println("currentDir " + currentDir);
		String file = currentDir.concat("/" + filename);
		System.out.println("file " + file);
		try {
			input = new Scanner(new File(file));
		} catch (FileNotFoundException ex) {
			System.out.println("No File Found!");
		}
		while(input != null &&input.hasNext()){
			obstacle_x = Integer.parseInt(input.next()); 
			obstacle_y = Integer.parseInt(input.next());
			Rectangle2D.Double rec = new Rectangle2D.Double(obstacle_x-5,obstacle_y-5,10, 10); 
			shapes.add(rec);
		}
		
		/*perception fields for is_obstacle_on_*/
		leftx = new int[3];
		lefty = new int[3];
		
		rightx = new int[3];
		righty = new int[3];
		
		/*set starting point in the middle of the screen*/
		x_coordinate = 350;
		y_coordinate = 350;
		x = new int[3];
		y = new int[3];

		/*starting direction is N*/
		heading = 0;
		x[0] = x_coordinate;
		y[0] = y_coordinate-10;
		x[1] = x_coordinate + (int)(10*(Math.tan(Math.toRadians(30)))+.5);
		y[1] = y_coordinate+10;
		x[2] = x_coordinate-(int)(10*(Math.tan(Math.toRadians(30)))+.5);
		y[2] = y_coordinate+10;
		
		this.colors = new ArrayList<String>();
		
		getDirection();
		Thread t = new Thread(this);
		t.start();
		setUp();
	}

	public void backward(){
		System.out.println("BACKWARD");
		if(direction == 1){ /**Q1*/
			x_coordinate = x_coordinate-(int)(15*Math.sin(Math.toRadians(heading))+.5);
			y_coordinate = y_coordinate+(int)(15*Math.cos(Math.toRadians(heading))+.5);
		}
		else if(direction == 2){ /**Q2*/
			x_coordinate = x_coordinate-(int)(15*Math.cos(Math.toRadians(heading-90))+.5);
			y_coordinate = y_coordinate-(int)(15*Math.sin(Math.toRadians(heading-90))+.5);
		}
		else if(direction == 3){ /**Q3*/
			x_coordinate = x_coordinate+(int)(15*Math.sin(Math.toRadians(heading-180))+.5);
			y_coordinate = y_coordinate-(int)(15*Math.cos(Math.toRadians(heading-180))+.5);
		}
		else if(direction == 4){ /**Q4*/
			x_coordinate = x_coordinate+(int)(15*Math.cos(Math.toRadians(heading-270))+.5);
			y_coordinate = y_coordinate+(int)(15*Math.sin(Math.toRadians(heading-270))+.5);
		}
		else if(direction == 5){ /**N*/
			y_coordinate = y_coordinate+15;
		}
		else if(direction == 6){ /**E*/
			x_coordinate = x_coordinate-15;
		}
		else if(direction == 7){ /**S*/
			y_coordinate = y_coordinate-15;
		}
		else if(direction == 8){ /**W*/
			x_coordinate = x_coordinate+15;
		}
		setUp();
	}
	
	public void forward(){
		System.out.println("FORWARD");
		if(direction == 1){ /**Q1*/
			x_coordinate = x_coordinate+(int)(15*Math.sin(Math.toRadians(heading))+.5);
			y_coordinate = y_coordinate-(int)(15*Math.cos(Math.toRadians(heading))+.5);
		}
		else if(direction == 2){ /**Q2*/
			x_coordinate = x_coordinate+(int)(15*Math.cos(Math.toRadians(heading-90))+.5);
			y_coordinate = y_coordinate+(int)(15*Math.sin(Math.toRadians(heading-90))+.5);
		}
		else if(direction == 3){ /**Q3*/
			x_coordinate = x_coordinate-(int)(15*Math.sin(Math.toRadians(heading-180))+.5);
			y_coordinate = y_coordinate+(int)(15*Math.cos(Math.toRadians(heading-180))+.5);
		}
		else if(direction == 4){ /**Q4*/
			x_coordinate = x_coordinate-(int)(15*Math.cos(Math.toRadians(heading-270))+.5);
			y_coordinate = y_coordinate-(int)(15*Math.sin(Math.toRadians(heading-270))+.5);
		}
		else if(direction == 5){ /**N*/
			y_coordinate = y_coordinate-15;
		}
		else if(direction == 6){ /**E*/
			x_coordinate = x_coordinate+15;
		}
		else if(direction == 7){ /**S*/
			y_coordinate = y_coordinate+15;
		}
		else if(direction == 8){ /**W*/
			x_coordinate = x_coordinate-15;
		}
		setUp();
	}
	
	public void rotate_left(){
		System.out.println("ROTATE_LEFT");
		if(heading == 0){
			heading = 330;
		}
		else{
			heading = heading - 30;
		}
		setUp();
	}
	
	public void rotate_right(){
		System.out.println("ROTATE_RIGHT");
		if(heading == 330){
			heading = 0;
		}
		else{
			heading = heading + 30;
		}
		setUp();
	}
	
	/*draws nose, left, and right points based on direction*/
	public void setUp(){
	  System.out.println("set up");
		getDirection();
		switch(direction){
			case 1: /**Q1*/
				switch(heading){
					case 30:
						x[0] = x_coordinate+(int)(10*(Math.sin(Math.toRadians(30)))+.5);
						y[0] = y_coordinate-(int)(10*(Math.cos(Math.toRadians(30)))+.5);
						
						x[1] = x_coordinate;
						y[1] = y_coordinate+(int)(10/(Math.cos(Math.toRadians(30)))+.5);
						
						x[2] = x_coordinate-10;
						y[2] = y_coordinate+(int)(10*(Math.tan(Math.toRadians(30)))+.5);
						
						leftx[0] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						lefty[0] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						
						leftx[1] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(30))));
						lefty[1] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						
						leftx[2] = x_coordinate;
						lefty[2] = y_coordinate;
						
						rightx[0] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						righty[0] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						
						rightx[1] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						righty[1] = y_coordinate+(int)(20*(Math.sin(Math.toRadians(300))));
						
						rightx[2] = x_coordinate;
						righty[2] = y_coordinate;
						break;
					case 60:
						x[0] = x_coordinate+(int)(10*(Math.sin(Math.toRadians(60)))+.5);
						y[0] = y_coordinate-(int)(10*(Math.cos(Math.toRadians(60)))+.5);
						
						x[1] = x_coordinate-(int)(10*(Math.tan(Math.toRadians(30)))+.5);
						y[1] = y_coordinate+10;
						
						x[2] = x_coordinate-(int)(10/(Math.cos(Math.toRadians(30)))+.5);
						y[2] = y_coordinate;
						
						leftx[0] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						lefty[0] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						
						leftx[1] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(60))));
						lefty[1] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						
						leftx[2] = x_coordinate;
						lefty[2] = y_coordinate;
						
						rightx[0] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						righty[0] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						
						rightx[1] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						righty[1] = y_coordinate+(int)(20*(Math.sin(Math.toRadians(60))));
						
						rightx[2] = x_coordinate;
						righty[2] = y_coordinate;
						break;
				}
				break;
			case 2: /**Q2*/
				switch(heading){
					case 120:
						x[0] = x_coordinate+(int)(10*(Math.cos(Math.toRadians(30)))+.5);
						y[0] = y_coordinate+(int)(10*(Math.sin(Math.toRadians(30)))+.5);
						
						x[1] = x_coordinate-(int)(10/(Math.cos(Math.toRadians(30)))+.5);
						y[1] = y_coordinate;
						
						x[2] = x_coordinate-(int)(10*(Math.tan(Math.toRadians(30)))+.5);
						y[2] = y_coordinate-10;
						
						leftx[0] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						lefty[0] = y_coordinate+(int)(20*(Math.sin(Math.toRadians(30))));
						
						leftx[1] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						lefty[1] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						
						leftx[2] = x_coordinate;
						lefty[2] = y_coordinate;
						
						rightx[0] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						righty[0] = y_coordinate+(int)(20*(Math.sin(Math.toRadians(30))));
						
						rightx[1] = x_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						righty[1] = y_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						
						rightx[2] = x_coordinate;
						righty[2] = y_coordinate;
						break;
					case 150:
						x[0] = x_coordinate+(int)(10*(Math.cos(Math.toRadians(60)))+.5);
						y[0] = y_coordinate+(int)(10*(Math.sin(Math.toRadians(60)))+.5);
						
						x[1] = x_coordinate-10;
						y[1] = y_coordinate-(int)(10*(Math.tan(Math.toRadians(30)))+.5);
						
						x[2] = x_coordinate;
						y[2] = y_coordinate-(int)(10/(Math.cos(Math.toRadians(30)))+.5);
						
						leftx[0] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						lefty[0] = y_coordinate+(int)(20*(Math.sin(Math.toRadians(60))));
						
						leftx[1] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						lefty[1] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						
						leftx[2] = x_coordinate;
						lefty[2] = y_coordinate;
						
						rightx[0] = x_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						righty[0] = y_coordinate+(int)(20*(Math.sin(Math.toRadians(60))));
						
						rightx[1] = x_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						righty[1] = y_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						
						rightx[2] = x_coordinate;
						righty[2] = y_coordinate;
						break;
				}
				break;
			case 3: /**Q3*/
				switch(heading){
					case 210:
						x[0] = x_coordinate-(int)(10*(Math.sin(Math.toRadians(30)))+.5);
						y[0] = y_coordinate+(int)(10*(Math.cos(Math.toRadians(30)))+.5);
						
						x[1] = x_coordinate;
						y[1] = y_coordinate-(int)(10/(Math.cos(Math.toRadians(30)))+.5);
						
						x[2] = x_coordinate+10;
						y[2] = y_coordinate-(int)(10*(Math.tan(Math.toRadians(30)))+.5);
						
						leftx[0] = x_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						lefty[0] = y_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						
						leftx[1] = x_coordinate+(int)(20*(Math.sin(Math.toRadians(60))));
						lefty[1] = y_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						
						leftx[2] = x_coordinate;
						lefty[2] = y_coordinate;
						
						rightx[0] = x_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						righty[0] = y_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						
						rightx[1] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(30))));
						righty[1] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						
						rightx[2] = x_coordinate;
						righty[2] = y_coordinate;
						break;
					case 240:
						x[0] = x_coordinate-(int)(10*(Math.cos(Math.toRadians(30)))+.5);
						y[0] = y_coordinate+(int)(10*(Math.sin(Math.toRadians(30)))+.5);
						
						x[1] = x_coordinate+(int)(10*(Math.tan(Math.toRadians(30)))+.5);
						y[1] = y_coordinate-10;
						
						x[2] = x_coordinate+(int)(10/(Math.cos(Math.toRadians(30)))+.5);
						y[2] = y_coordinate;
						
						leftx[0] = x_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						lefty[0] = y_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						
						leftx[1] = x_coordinate+(int)(20*(Math.sin(Math.toRadians(30))));
						lefty[1] = y_coordinate+(int)(20*(Math.cos(Math.toRadians(30))));
						
						leftx[2] = x_coordinate;
						lefty[2] = y_coordinate;
						
						rightx[0] = x_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						righty[0] = y_coordinate+(int)(20*(Math.cos(Math.toRadians(60))));
						
						rightx[1] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(60))));
						righty[1] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						
						rightx[2] = x_coordinate;
						righty[2] = y_coordinate;
						break;
				}
				break;
			case 4: /**Q4*/
				switch(heading){
					case 300:
						x[0] = x_coordinate-(int)(10*(Math.cos(Math.toRadians(30)))+.5);
						y[0] = y_coordinate-(int)(10*(Math.sin(Math.toRadians(30)))+.5);
						
						x[1] = x_coordinate+(int)(10/(Math.cos(Math.toRadians(30)))+.5);
						y[1] = y_coordinate;
						
						x[2] = x_coordinate+(int)(10*(Math.tan(Math.toRadians(30)))+.5);
						y[2] = y_coordinate+10;
						
						leftx[0] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(30))));
						lefty[0] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						
						leftx[1] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(60))));
						lefty[1] = y_coordinate+(int)(20*(Math.sin(Math.toRadians(60))));
						
						leftx[2] = x_coordinate;
						lefty[2] = y_coordinate;
						
						rightx[0] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(30))));
						righty[0] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(30))));
						
						rightx[1] = x_coordinate+(int)(20*(Math.sin(Math.toRadians(30))));
						righty[1] = y_coordinate-(int)(20*(Math.cos(Math.toRadians(30))));
						
						rightx[2] = x_coordinate;
						righty[2] = y_coordinate;
						break;
					case 330:
						x[0] = x_coordinate-(int)(10*(Math.sin(Math.toRadians(30)))+.5);
						y[0] = y_coordinate-(int)(10*(Math.cos(Math.toRadians(30)))+.5);
						
						x[1] = x_coordinate+10;
						y[1] = y_coordinate+(int)(10*(Math.tan(Math.toRadians(30)))+.5);
						
						x[2] = x_coordinate;
						y[2] = y_coordinate+(int)(10/(Math.cos(Math.toRadians(30)))+.5);
						
						leftx[0] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(60))));
						lefty[0] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						
						leftx[1] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(30))));
						lefty[1] = y_coordinate+(int)(20*(Math.sin(Math.toRadians(30))));
						
						leftx[2] = x_coordinate;
						lefty[2] = y_coordinate;
						
						rightx[0] = x_coordinate-(int)(20*(Math.cos(Math.toRadians(60))));
						righty[0] = y_coordinate-(int)(20*(Math.sin(Math.toRadians(60))));
						
						rightx[1] = x_coordinate+(int)(20*(Math.sin(Math.toRadians(60))));
						righty[1] = y_coordinate-(int)(20*(Math.cos(Math.toRadians(60))));
						
						rightx[2] = x_coordinate;
						righty[2] = y_coordinate;
						break;
				}
				break;
			case 5: /**N*/
				x[0] = x_coordinate;
				y[0] = y_coordinate-10;
				
				x[1] = x_coordinate + (int)(10*(Math.tan(Math.toRadians(30)))+.5);
				y[1] = y_coordinate+10;
				
				x[2] = x_coordinate-(int)(10*(Math.tan(Math.toRadians(30)))+.5);
				y[2] = y_coordinate+10;
				
				leftx[0] = x_coordinate;
				lefty[0] = y_coordinate-20;
				
				leftx[1] = x_coordinate-20;
				lefty[1] = y_coordinate;
				
				leftx[2] = x_coordinate;
				lefty[2] = y_coordinate;
				
				rightx[0] = x_coordinate;
				righty[0] = y_coordinate-20;
				
				rightx[1] = x_coordinate+20;
				righty[1] = y_coordinate;
				
				rightx[2] = x_coordinate;
				righty[2] = y_coordinate;
				break;
			case 6: /**E*/
				x[0] = x_coordinate+10;
				y[0] = y_coordinate;
				
				x[1] = x_coordinate-10;
				y[1] = y_coordinate-(int)(10*(Math.tan(Math.toRadians(30)))+.5);
				
				x[2] = x_coordinate-10;
				y[2] = y_coordinate+(int)(10*(Math.tan(Math.toRadians(30)))+.5);
				
				leftx[0] = x_coordinate+20;
				lefty[0] = y_coordinate;
				
				leftx[1] = x_coordinate;
				lefty[1] = y_coordinate-20;
				
				leftx[2] = x_coordinate;
				lefty[2] = y_coordinate;
				
				rightx[0] = x_coordinate+20;
				righty[0] = y_coordinate;
				
				rightx[1] = x_coordinate;
				righty[1] = y_coordinate+20;
				
				rightx[2] = x_coordinate;
				righty[2] = y_coordinate;
				break;
			case 7: /**S*/
				x[0] = x_coordinate;
				y[0] = y_coordinate+10;
				
				x[1] = x_coordinate-(int)(10*(Math.tan(Math.toRadians(30)))+.5);
				y[1] = y_coordinate-10;
				
				x[2] = x_coordinate+(int)(10*(Math.tan(Math.toRadians(30)))+.5);
				y[2] = y_coordinate-10;
				
				leftx[0] = x_coordinate;
				lefty[0] = y_coordinate+20;
				
				leftx[1] = x_coordinate+20;
				lefty[1] = y_coordinate;
				
				leftx[2] = x_coordinate;
				lefty[2] = y_coordinate;
				
				rightx[0] = x_coordinate;
				righty[0] = y_coordinate+20;
				
				rightx[1] = x_coordinate-20;
				righty[1] = y_coordinate;
				
				rightx[2] = x_coordinate;
				righty[2] = y_coordinate;
				break;
			case 8: /**W*/
				x[0] = x_coordinate-10;
				y[0] = y_coordinate;
				
				x[1] = x_coordinate+10;
				y[1] = y_coordinate-(int)(10*(Math.tan(Math.toRadians(30)))+.5);
				
				x[2] = x_coordinate+10;
				y[2] = y_coordinate+(int)(10*(Math.tan(Math.toRadians(30)))+.5);
				
				leftx[0] = x_coordinate-20;
				lefty[0] = y_coordinate;
				
				leftx[1] = x_coordinate;
				lefty[1] = y_coordinate+20;
				
				leftx[2] = x_coordinate;
				lefty[2] = y_coordinate;
				
				rightx[0] = x_coordinate-20;
				righty[0] = y_coordinate;
				
				rightx[1] = x_coordinate;
				righty[1] = y_coordinate+20;
				
				rightx[2] = x_coordinate;
				righty[2] = y_coordinate;
				break;
		}
		/*if the center coordinate is out of bounds, bring finch back to center*/
		if((x_coordinate <= 0 || x_coordinate >= 700)||(y_coordinate <=0 || y_coordinate >=700)){
			x_coordinate = 350;
			y_coordinate = 350;
		}
		System.out.println("reached repaint");
		repaint();
		try{
		  System.out.println("sleep");
		Thread.sleep(500);
		} catch(InterruptedException ie){}
	}
	
	public void run() {
		
	}
	/*gives quadrant or axis in form of a number*/
	public int getDirection(){
		direction = heading/90;
		if(direction > 3){
			while(direction > 3){
				direction = direction-4;
			}
		}
		/**axis or quadrant*/
		if(heading%90 == 0){
			direction = direction +5;
			//System.out.printf("axis %d\n", direction-4);
			return direction;
		}
		else{
			direction = direction + 1;
			//System.out.printf("Quad %d\n", direction);
			return direction;
		}
	}

	/*public void getColors(ArrayList<Colors>){
		
	}*/
	
	public void paint(Graphics g) {
	  System.out.println("paint");
		super.paint(g);
		g.setColor(Color.blue);
		g.fillPolygon(x,y,3);
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.black);
		for (Shape s : shapes){
			g2.fill(s);
		}
	}

	@Override
	public void go_backward() {
		backward();
		// TODO Auto-generated method stub
		
	}

	@Override
	public void go_forward() {
		// TODO Auto-generated method stub
		forward();
	}

	@Override
	public boolean is_beak_down() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is_beak_up() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is_obstacle_not_on_left() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is_obstacle_not_on_right() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is_obstacle_on_left() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is_obstacle_on_right() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void play_file(String song) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void play_song(String song) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setColors(ArrayList<String> colors)
  {
    this.colors = colors;
  }
  
	@Override
  public ArrayList<String> getColors()
  {
	  System.out.println("Colors: ");
	  for (int i = 0; i < colors.size(); i++)
	    System.out.print(colors.get(i) + " ");
	  System.out.println();
    return colors;
    
  }
}


