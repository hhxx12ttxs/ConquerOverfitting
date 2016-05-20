import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
   A Boat that can be moved around.
*/
public class BoatMoveableShape implements MoveableShape
{
	private int x;
	private int y;
	private double width;
	String color="BLACK";
   /**
      Constructs a boat item.
      @param x the left of the bounding rectangle
      @param y the top of the bounding rectangle
      @param width the width of the bounding rectangle
   */
   public BoatMoveableShape(int x, int y, double width)
   {
      this.x = x;
      this.y = y;
      this.width = width;
   }
	/**
		moves the boat
		@param dx the ammount of x to be moved
		@param dy the ammount of y to be moved
	*/
   public void translate(int dx, int dy)
   {
      x += dx;
      y += dy;
   }
	/**
		draws the shape
		@param g2 the graphics
	*/
   public void draw(Graphics2D g2)
   {
		// The top right point of boat base
		Point2D.Double r1
			= new Point2D.Double(x + width / 6, y + width / 3);
		// The botom right of the boat base
		  Point2D.Double r2
			= new Point2D.Double(x + width / 3, y + width/2);
		 // The bottom left of boat base
		Point2D.Double r3
			= new Point2D.Double(x + width * 2 / 3, y + width/2);
		// The top left point of boat base
		Point2D.Double r4
			= new Point2D.Double(x + width * 5 / 6, y + width / 3);
		//the point where mass connects to boat
		Point2D.Double m1
			= new Point2D.Double(x + width / 2, y + width / 3);
		//the top of the mass
		Point2D.Double m2
			= new Point2D.Double(x + width / 2, y);
		//the point that the flag extends to
		Point2D.Double m3
			= new Point2D.Double(x + width * 2 / 3, y + width / 5);
		//the mid-mass flag connect
		Point2D.Double m4
			= new Point2D.Double(x + width / 2, y + width / 5);
		
		//connects the dots to form shapes
		Line2D.Double BoatBaseFront
			= new Line2D.Double(r1, r2);
		Line2D.Double BoatBaseBottom
			= new Line2D.Double(r2, r3);
		Line2D.Double BoatBaseBack
			= new Line2D.Double(r3, r4);
		Line2D.Double BoatBaseTop
			= new Line2D.Double(r1, r4);
		Line2D.Double Mass
			= new Line2D.Double(m1, m2);
		Line2D.Double FlagTop
			= new Line2D.Double(m2,m3);
		Line2D.Double FlagBottom
			= new Line2D.Double(m3, m4);
		//checks to see if a color has been declared		
		if(color.equals("RED"))
		{
			g2.setColor(Color.RED);
		}
		if(color.equals("BLUE"))
		{
			g2.setColor(Color.BLUE);
		}
		if(color.equals("YELLOW"))
		{
			g2.setColor(Color.YELLOW);
		}
		if(color.equals("GREEN"))
		{
			g2.setColor(Color.GREEN);
		}
		//draw the shape
		  g2.draw(BoatBaseFront);
		  g2.draw(BoatBaseBottom);
		  g2.draw(BoatBaseBack);
		  g2.draw(BoatBaseTop);
		  g2.draw(Mass);
		  g2.draw(FlagTop);
		  g2.draw(FlagBottom);
    }
	/**
	returns current x value 
	@return x the current x value
	*/
	public int getCurX()
	{
		return x;
	}
	/*
	returns the current y value
	@return y the current y value
	*/
	public int getCurY()
	{
		return y;
	}
	/*
	resets the animation
	*/
	public void rAnimation()
	{
		x =0;
	}
	/*
	lets the color of the boat be set
	*/
	public void SetColor(String pcolor)
	{
		color=pcolor;
	}			
}

