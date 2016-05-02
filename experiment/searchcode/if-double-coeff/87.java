package strategy.movement;

//import strategy.calculations.AngleBetweenTwoVectors;
import geometry.Vector;
import world.state.PitchInfo;
import world.state.Robot;
import world.state.WorldInterface;

import comms.control.ServerInterface;

public class FollowVector {
	
	// We must block turns for acceleration - Oli
	static boolean isRotating = false;
	
	/*
	private static double normalizeAngle(double angle)
	{
		double absAngle = Math.abs(angle);
		int n = (int) Math.floor(absAngle / (2*Math.PI));
		return Math.signum(angle)*();
	}
	*/
	public static void followVector(WorldInterface world, ServerInterface rc, double dx, double dy)
	{
		
		if(isRotating) {
			if (rc.isMoving()) {
				return;
			} else {
	            isRotating = false;
			}
		}
		//rc.setDefaultRotateSpeed(0.05);
    	//rc.setDefaultTravelSpeed(0.05);
		
		Robot us = world.getOurRobot();
		//double vectAngle = Math.atan2(dy, dx);
		double robotAngle = us.bearing;
		//double robotAngle = - Math.PI/2;
		double robotDirX = Math.sin(robotAngle);
		double robotDirY = -Math.cos(robotAngle);
		
		double angleDifference = Math.atan2(robotDirY, robotDirX) - Math.atan2(dy, dx);
		
		//System.out.println("bearing " + us.bearing);
		//System.out.println("angle diff: " + angleDifference);
		
		if(angleDifference > Math.PI)
			angleDifference -= 2*Math.PI;
		if(angleDifference < -Math.PI)
			angleDifference += 2*Math.PI;
		/*
		if(angleDifference > 0.1)
			rc.arcForward(-0.3);
		else if((angleDifference < -0.1))
			rc.arcForward(0.3);
		else
			rc.forward();
		*/
		
		if(angleDifference > Math.PI/4) {
	            rc.rotate((angleDifference / Math.PI) * 180);
	            isRotating = true;
	            return;
		}
		else if(angleDifference > 0.1)
	            rc.arcForward(-0.5);
	    else if((angleDifference < -Math.PI/4)) {
	            rc.rotate((angleDifference / Math.PI) * 180);
	            isRotating = true;
	            return;
	    }
	    else if(angleDifference < -0.1)
	            rc.arcForward(0.5);
	    else
	            rc.forward();
	
			
	} 
	
	public static void followVectorNoRotate(WorldInterface world, ServerInterface rc, double dx, double dy, double distance)
	{
		//rc.setDefaultRotateSpeed(0.05);
    	//rc.setDefaultTravelSpeed(0.05);
		
		Robot us = world.getOurRobot();
		//double vectAngle = Math.atan2(dy, dx);
		double robotAngle = us.bearing;
		//double robotAngle = - Math.PI/2;
		double robotDirX = Math.sin(robotAngle);
		double robotDirY = -Math.cos(robotAngle);
		
		double angleDifference = Math.atan2(robotDirY, robotDirX) - Math.atan2(dy, dx);
		
		//System.out.println("bearing " + us.bearing);
		//System.out.println("angle diff: " + angleDifference);

		double coeff = 0.03;
		double thres = 70;
		double radius = coeff * Math.sqrt(distance);
		if(distance < thres)
			radius = (0.03 / Math.sqrt(thres*thres*thres)) * distance * distance;
		
		
		if(angleDifference > Math.PI)
			angleDifference -= 2*Math.PI;
		if(angleDifference < -Math.PI)
			angleDifference += 2*Math.PI;
		
		if(radius > 0.5)
			radius = 0.5;
		
		if(angleDifference > 0.15)
		{
			rc.arcForward(-radius);
			//rc.arcForward(-0.35);
			//System.out.println("Turnin' right");
		}
		else if((angleDifference < -0.15))
		{
			rc.arcForward(radius);
			//rc.arcForward(0.35);
			//System.out.println("Turnin' left");
		}
		else
			rc.forward();
		
		
	} 
	
	public static void arcToPoint(WorldInterface world, ServerInterface rc, Vector rawTarget)
	{
		Vector lowBound = normalise(PitchInfo.safeLowerBoundSide);
		Vector upperBound = normalise(PitchInfo.safeUpperBoundSide);

		Vector target = normalise(rawTarget);
		Vector robotPos = normalise(world.getOurRobot().getPosition());
		Vector robotDirection = new Vector(world.getOurRobot().bearing);

		// Find the intersection point with each wall if the robot were to continue forwards
		Vector intTop, intBottom, intLeft, intRight;
		intTop =	robotPos.intersectY(robotDirection, lowBound.getY());
		intBottom = robotPos.intersectY(robotDirection, upperBound.getY());
		intLeft =	robotPos.intersectX(robotDirection, lowBound.getX());
		intRight =	robotPos.intersectX(robotDirection, upperBound.getX());
		
		// Get the minimum distance to an intersection point (null returns mean no intersection was possible)
		double intersectionDist = Double.POSITIVE_INFINITY;
		if (intTop != null) intersectionDist = Math.min(intersectionDist, robotPos.distance(intTop));
		if (intBottom != null) intersectionDist = Math.min(intersectionDist, robotPos.distance(intBottom));
		if (intLeft != null) intersectionDist = Math.min(intersectionDist, robotPos.distance(intLeft));
		if (intRight != null) intersectionDist = Math.min(intersectionDist, robotPos.distance(intRight));

		double turnAngle = Vector.angleBetweenPoints(robotPos, target) - world.getOurRobot().bearing;
		double arcRadius = Math.min(intersectionDist, robotPos.distance(target)) / (2 * Math.sin(turnAngle));
		rc.arcForward(arcRadius);
	}
	
	private static Vector normalise(Vector v) {
		double x = 2.4384 * (v.getX() - PitchInfo.lowerBoundSide.getX()) / (PitchInfo.upperBoundSide.getX());
		double y = 1.2192 * (v.getY() - PitchInfo.lowerBoundSide.getY()) / (PitchInfo.upperBoundSide.getY());
		return new Vector(x, y);
	}

}

