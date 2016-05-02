import org.apache.commons.math.geometry.Vector3D;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class PendaphoneGestures 
{
	Vector3D rightBall;
	Vector3D leftBall;
	Vector3D firstLocation;
	
	Robot robot; //Generates native system events for controlling inputs (controlling the mouse)
	DisplayPlane plane;
	
	public final static int CALIBRATION = 0;
	public final static int INTERACTION = 1;
	
	public static int mode = CALIBRATION; //Initially set
	
	boolean pressed = false;
	
	public PendaphoneGestures()
	{	
		
		//Instantiating right and left ball with default vectors
		rightBall = new Vector3D(0,0,1);
		leftBall = new Vector3D(0,0,1);
	
		MovingBallListener mbl = new MovingBallListener(this);
		mbl.initConnection();
		
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setFL(Vector3D fl)
	{
		firstLocation = fl;
	}
	
	public void updateLocation(double[] tokens)
	{
		// have to reverse the direction of the x
		rightBall = new Vector3D(1-tokens[2], new Vector3D(toRadians(-tokens[0]), toRadians(-tokens[1])));//.subtract(new Vector3D(0,0,1));
		
		
		//rightBall = new Vector3D(tokens[2], new Vector3D(toRadians(tokens[0]), toRadians(tokens[1])));
		leftBall = new Vector3D(tokens[5], new Vector3D(toRadians(tokens[3]), toRadians(tokens[4])));
		

		
		if (mode == INTERACTION && rightBall.getNorm() > firstLocation.getNorm() + CalibrationPanel.move_buffer)
		{
			Vector3D projection = plane.projectToPlane(rightBall);

			//Vector3D projection = rightBall;
			robot.mouseMove((int)plane.translatePlaneX(projection), (int)plane.translatePlaneY(projection));
			//System.out.println(rightBall.getAlpha() + " , " + rightBall.getDelta() + " , " + rightBall.getNorm());
			//System.out.println("x,y = " + (int)plane.translatePlaneX(projection) + "," +  (int)plane.translatePlaneY(projection));
		}
		
		//if (plane != null)
		System.out.println("onPlane rightBall " + plane.onPlane(rightBall) + " - " + pressed);
		if (mode == INTERACTION && pressed == false && plane.onPlane(rightBall) > CalibrationPanel.click_buffer)	
		{	System.out.println("MOUSEPRESS " + plane.onPlane(rightBall));
			robot.mousePress(InputEvent.BUTTON1_MASK);
			pressed = true;
		}
		else if (mode == INTERACTION && pressed == true && plane.onPlane(rightBall) < CalibrationPanel.click_buffer)
		{
			System.out.println("MOUSERELEASED");
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			pressed = false;
		}
		
	}
	
	public Vector3D getRightLocation()
	{
		return rightBall;
	}
	
	public void setDisplayPlane(DisplayPlane dp)
	{
		this.plane = dp;
		mode = INTERACTION;
	}
	
	public double toRadians(double angle)
	{
		return angle*Math.PI/3;
		//return angle*Math.PI/2;
	}


}

