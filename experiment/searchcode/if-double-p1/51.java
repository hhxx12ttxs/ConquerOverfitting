package bbb.common.util;
import robocode.util.Utils;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Interpolation {

	/**
	 * The [0,1] definitions
	 * scalar versions
	 * t E [0,1]
	 */
	public static double linear(double s0, double s1, double t) {
		return s0*t+(1-t)*s1;
	}

	public static double bezier4(double s0, double s1, double s2, double s3, double t) {
		double ti = 1-t;
		double ti2 = ti*ti;
		double t2 = t*t;
		return ti2*ti*s0 + 3*t*ti2*s1 + 3*t2*ti*s2 + t2*t*s3;
	}

	
	/**
	 * The long time definitions
	 * scalar versions
	 */
	public static double linear(double s0, long t0, double s1, long t1, long t) {
		double dt = t1-t0;
		if(dt == 0) return s0;
		return s0+((double)(t-t0))*(s1-s0)/dt;
	}

	public static double bezier4(double s0, long t0, double s1, double s2, double s3, long t1, double t) {
		double dt = t1-t0;
		if(dt == 0) return s0;
		return bezier4(s0, s1, s2, s3, (t-t0)/dt);
	}

	/**
	 * Example:
	 * pos = Interpolation.bezierPos(
		new Line2D (
			new Point2D(getX(t0), getY(t0)),
			new Point2D(getX(t0)+getVelocity(t0)*Math.sin(getHeading(t0)), getY(t0)+getVelocity(t0)*Math.cos(getHeading(t0)))
		, t0, 
		new Line2D (
			new Point2D(getX(t1), getY(t1)),
			new Point2D(getX(t1)+getVelocity(t1)*Math.sin(getHeading(t1)), getY(t1)+getVelocity(t1)*Math.cos(getHeading(t1)))
		, t1,
		t
		);

	 */
	public static Point2D bezierPos(Line2D p0, long t0, Line2D p1, long t1, double t) {
		double dt = t1-t0;
		if(dt == 0) return p0.getP1();
		Point2D inv_vel1 = new Point2D.Double(
			p1.getP1().getX()*2-p1.getP2().getX(), p1.getP1().getY()*2-p1.getP2().getY()
		);
		return new Point2D.Double(
				bezier4(p0.getP1().getX(), p0.getP2().getX(), inv_vel1.getX(), p1.getP1().getX(), (t-t0)/dt), 			
				bezier4(p0.getP1().getY(), p0.getP2().getY(), inv_vel1.getY(), p1.getP1().getY(), (t-t0)/dt)
			); 
	}

	/**
	 * The long time definitions
	 * angular versions (all in radians)
	 * TODO: if dt is too big it could throw a LowQualityInterpolation 
	 */
	public static double angLinear(double a0, long t0, double a1, long t1, long t) {
		return a0+linear(0, t0, Utils.normalAbsoluteAngle(a1-a0), t1, t);
	}


}

