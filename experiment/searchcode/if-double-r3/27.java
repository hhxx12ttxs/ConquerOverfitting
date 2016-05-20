import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
   A car that can be moved around.
*/
public class CarMoveableShape implements MoveableShape
{
	private int x;
	private int y;
	private double width;
	String color="black";
   /**
      Constructs a car item.
      @param x the left of the bounding rectangle
      @param y the top of the bounding rectangle
      @param width the width of the bounding rectangle
   */
   public CarMoveableShape(int x, int y, double width)
   {
      this.x = x;
      this.y = y;
      this.width = width;
   }
	/**
		moves the car item
		@param dx the ammount to be moved in x direction
		@param dy the ammount to be moved in y direction
	*/
   public void translate(int dx, int dy)
   {
      x += dx;
      y += dy;
   }
	/**
	draws the car
	@param g2 the graphics
	*/
   public void draw(Graphics2D g2)
   {
      Rectangle2D.Double body
            = new Rectangle2D.Double(x, y + width / 6, 
                  width - 1, width / 6);
      Ellipse2D.Double frontTire
            = new Ellipse2D.Double(x + width / 6, y + width / 3, 
                  width / 6, width / 6);
      Ellipse2D.Double rearTire
            = new Ellipse2D.Double(x + width * 2 / 3, y + width / 3,
                  width / 6, width / 6);

      // The bottom of the front windshield
      Point2D.Double r1
            = new Point2D.Double(x + width / 6, y + width / 6);
      // The front of the roof
      Point2D.Double r2
            = new Point2D.Double(x + width / 3, y);
      // The rear of the roof
      Point2D.Double r3
            = new Point2D.Double(x + width * 2 / 3, y);
      // The bottom of the rear windshield
      Point2D.Double r4
            = new Point2D.Double(x + width * 5 / 6, y + width / 6);
      Line2D.Double frontWindshield
            = new Line2D.Double(r1, r2);
      Line2D.Double roofTop
            = new Line2D.Double(r2, r3);
      Line2D.Double rearWindshield
            = new Line2D.Double(r3, r4);
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
		
	 //draws the image
      g2.draw(body);
      g2.draw(frontTire);
      g2.draw(rearTire);
      g2.draw(frontWindshield);
      g2.draw(roofTop);
      g2.draw(rearWindshield);
    }
	/**
	returns the current x value
	@return x the current x value
	*/
	public int getCurX()
	{
		return x;
	}
	/**
	returns the current y value
	@return y the current y value
	*/
	public int getCurY()
	{
		return y;
	}
	/**
	resets the Animation at x=0
	*/
	public void rAnimation()
	{
		x =0;
	}
	/**
	sets the color of the shape
	*/
	public void SetColor(String pcolor)
	{
		color=pcolor;
	}	
}

