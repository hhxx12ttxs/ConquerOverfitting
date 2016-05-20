package bbb.common.util.interpolators;

import java.awt.geom.*;
import java.util.*;

import bbb.common.data.*;
import bbb.common.util.*;

public class Bezier implements Interpolator {
	/*
	 * TODO: Broken.....
	 * */
	public Point2D getPos(List<Data> l, long time){
		int size = l.size();
		if(size <= 0)
			return null;

		if(size == 1)
			return l.get(0).getPos();

		long dt = time - l.get(size - 1).getTime();
		Point2D p3 = l.get(size - 1).getPos();
		Point2D p2 = p3;
		Point2D p0 = null;
		Point2D p1 = null;
		
		if(size == 2)
			p0 = p1 = l.get(0).getPos();
		else if(size == 3) {
			p0 = l.get(0).getPos();
			p1 = p2 = l.get(1).getPos();
		} else {
			p0 = l.get(size - 4).getPos();
			p1 = l.get(size - 3).getPos();
			p2 = l.get(size - 2).getPos();
		}
		double x=bezier(p0.getX(),p1.getX(), p2.getX(), p3.getX(), dt);
		double y=bezier(p0.getY(),p1.getY(), p2.getY(), p3.getY(), dt);
		return new EnhancedPoint(x, y);	
	}
	public static double bezier(double s0, double s1, double s2, double s3, double t) {
		double ti = 1-t;
		double ti2 = ti*ti;
		double t2 = t*t;
		return ti2*ti*s0 + 3*t*ti2*s1 + 3*t2*ti*s2 + t2*t*s3;
	}
}
/*

	public static double linear(double s0, double s1, double t) {
		return s0*t+(1-t)*s1;
	}

	public static double bezier4(double s0, double s1, double s2, double s3, double t) {
		double ti = 1-t;
		double ti2 = ti*ti;
		double t2 = t*t;
		return ti2*ti*s0 + 3*t*ti2*s1 + 3*t2*ti*s2 + t2*t*s3;
	}
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

	**
	 * The long time definitions
	 * angular versions (all in radians)
	 * TODO: if dt is too big it could throw a LowQualityInterpolation 
	 *
	public static double angLinear(double a0, long t0, double a1, long t1, long t) {
		return a0+linear(0, t0, Utils.normalAbsoluteAngle(a1-a0), t1, t);
	}
*/


