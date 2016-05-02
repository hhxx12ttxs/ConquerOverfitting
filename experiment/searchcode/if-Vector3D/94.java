
import org.apache.commons.math.geometry.Vector3D;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Toolkit;


public class DisplayPlane 
{
	Vector3D bottomRight;
	Vector3D bottomLeft;
	Vector3D topLeft;
	Vector3D normalVector;
	
	//Dimension realBounds;
	
	int height;
	int width;
	int xPos;
	int yPos;
	
	
	/** Display Plane constructor. Needs the three calibration points, and the dimensions and location of the calibration box.
	*/
	public DisplayPlane(Vector3D bottomRight, Vector3D bottomLeft, Vector3D topLeft, int height, int width, int xPos, int yPos)
	{
		// calibration vectors and normal to the plane
		this.bottomRight = bottomRight;
		this.bottomLeft = bottomLeft;
		this.topLeft = topLeft;
		this.normalVector = Vector3D.crossProduct(topLeft.subtract(bottomLeft), bottomRight.subtract(bottomLeft));
				
		// dimensions of the calibration box
		this.height = height;
		this.width = width;
		this.xPos = xPos;
		this.yPos = yPos;
		
		// bounds of the screen (not currently necessary?)
		//realBounds = Toolkit.getDefaultToolkit().getScreenSize();
		
	}
	
	
	/** Projects the location of the vector to a point on the plane. Given the initial vector, returns the projected
	 * vector.
	 * @param ball
	 * @return
	 */
	public Vector3D projectToPlane(Vector3D ball)
	{
		//a point on the plane . normal vector / inital point . normal vector
		double scalarMultiplier = (Vector3D.dotProduct(bottomLeft, normalVector))/(Vector3D.dotProduct(ball, normalVector));
		System.out.println("Scalar Multiplier: " + scalarMultiplier);
		//return the initial vector multiplied by the calculated above multiplier 
		return new Vector3D(scalarMultiplier*ball.getNorm(), new Vector3D(ball.getAlpha(), ball.getDelta()));
	}
	
	/** Given the projected point, finds the x value on the plane, and scales it to the display surface. */
	public double translatePlaneX(Vector3D pointProjection)
	{
		Vector3D twoDvector = pointProjection.subtract(bottomLeft);
		double angle = Vector3D.angle(twoDvector, bottomRight.subtract(bottomLeft));
		return scaleX(twoDvector.getNorm()*Math.cos(angle));
	}
	
	/** Given the projected point, finds the x value on the plane, and scales it to the display surface. */
	public double translatePlaneY(Vector3D pointProjection)
	{
		// calculate the vector on the plane between the projected point and bottom left of the calibration box
		Vector3D twoDvector = pointProjection.subtract(bottomLeft); 
		
		// calculate the angle between the vector on the plane and the bottom of the calibration box
		double angle = Vector3D.angle(twoDvector, bottomRight.subtract(bottomLeft));
		//System.out.println(angle);
		if (twoDvector.getDelta() >= 0) //if the twoDvector is above the calibration box, proceed as normal
			return scaleY(twoDvector.getNorm()*Math.sin(angle));
		else //if the twoDvector is below the bottom of the calibration box, the angle needs to be adjusted to be greater than PI
			return scaleY(twoDvector.getNorm()*Math.sin(2*Math.PI - angle));
	}	
	

	/** Scales the x component of the vector to appear on the screen.
	 */
	public double scaleX(double xValue)
	{
		// scale the xValue based on the calibration dimensions. The origin is xPos.
		return (xValue*width/(bottomRight.subtract(bottomLeft)).getNorm() + xPos);
	}
	
	/** Scales the y component of the vector to appear on the screen.
	 */
	public double scaleY(double yValue)
	{		
		// scale the yValue based on the calibration dimensions. The origin is yPos.
		return (yPos + height + yValue*height/(topLeft.subtract(bottomLeft)).getNorm()); // origin here is yPos + height
	}
	
	/** Tests to see how close the vector is to the plane
	 */
	public double onPlane(Vector3D point)
	{
		return Vector3D.dotProduct(point.subtract(bottomLeft), normalVector);
	}
	
	public double computeClickBuffer()
	{
		return -1;
	}
	
	public double computeMoveBuffer()
	{
		return -1;
	}
	
	
}

