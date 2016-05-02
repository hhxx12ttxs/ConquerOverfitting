package perspectives.graph;

import java.awt.Point;
import java.awt.geom.Point2D;

import perspectives.util.Util;

public class EdgeCompatibility {
	
	//this method is for compatibility.four types of compatibility: angular,scale,position,visibility
	
	Point2D i1,i2,im,pm;
	
	public double getCompatibility(Point2D.Double p1, Point2D.Double p2, Point2D.Double q1, Point2D.Double q2, boolean verbose)
	{
		double compatibility = 1.0;
		

		double ac = this.getAngularCompatibility(p1, p2, q1, q2);
		
			

		double sc = this.getScaleCompatibility(p1, p2, q1, q2);
		

		double pc =this.getPositionCompatibility(p1, p2, q1, q2);
		
		double vc1 = this.getVisibilityCompatibility(p1, p2, q1, q2);
		double vc2 = this.getVisibilityCompatibility(q1, q2, p1, p2);
		double vc = Math.min(vc1, vc2);
		vc = Math.sqrt(vc);
		//vc = 1;
		
		if (verbose)
		System.out.println(ac + " " + sc + " " + pc + " " + vc);
		
		compatibility*= ac*sc*pc*vc;		
		
		return compatibility;		
	}
	
	
	private double getAngularCompatibility(Point2D.Double p1, Point2D.Double p2, Point2D.Double q1, Point2D.Double q2)
	{
		//Angular Compatibility = |P.Q / |P| |Q| |		
		double pLength = Point.distance(p1.x, p1.y, p2.x, p2.y);
		double qLength=Point.distance(q1.x, q1.y, q2.x, q2.y);
		
		if (pLength == 0 || qLength == 0) return 0;
		
		// P.Q = PxQx+PyQy = 
		double pqDotProduct = ((p1.x - p2.x) * (q1.x-q2.x))+((p1.y - p2.y) * (q1.y-q2.y));
		double angComp =0;
		if(pLength!=0 && qLength!=0){
			angComp = Math.abs(pqDotProduct / (pLength * qLength));
		}
		
		return angComp;
	}
	
	private double getScaleCompatibility(Point2D.Double p1, Point2D.Double p2, Point2D.Double q1, Point2D.Double q2)
	{
		double pLength = Point.distance(p1.x, p1.y, p2.x, p2.y);
		double qLength=Point.distance(q1.x, q1.y, q2.x, q2.y);
		//Scale Compatabilty
		double lAvg=(pLength+qLength)/2;
		double scaleComp=2/((lAvg/Math.min(pLength, qLength))+(Math.max(pLength, qLength)/lAvg));
		
		return scaleComp;
	}
	
	private double getPositionCompatibility(Point2D.Double p1, Point2D.Double p2, Point2D.Double q1, Point2D.Double q2)
	{
		double pLength = Point.distance(p1.x, p1.y, p2.x, p2.y);
		double qLength=Point.distance(q1.x, q1.y, q2.x, q2.y);
		//Scale Compatabilty
		double lAvg=(pLength+qLength)/2;
		
		//Position Compatibility
		Point2D.Double pMid = new Point2D.Double((p1.x+p2.x)/2, (p1.y+p2.y)/2);
		Point2D.Double qMid = new Point2D.Double((q1.x+q2.x)/2, (q1.y+q2.y)/2);
		double midDistance=Point.distance(pMid.x,pMid.y, qMid.x ,qMid.y);
		double posComp=lAvg/(lAvg+midDistance);
		return posComp;
	}
	
	public double getVisibilityCompatibility(Point2D.Double p1, Point2D.Double p2, Point2D.Double q1, Point2D.Double q2)
	{
		this.i1 = null;
		this.i2 = null;
		this.im = null;
		
		double mp = (p2.y - p1.y)/(p2.x-p1.x);
		
		Point2D i1 = null;
		if (p2.x - p1.x == 0)
			i1 = Util.getLineLineIntersection(p1.x, p1.y, p2.x, p2.y, q1.x, q1.y, q1.x+1, q1.y);
		else if (p2.y-p1.y == 0)
			i1 = Util.getLineLineIntersection(p1.x, p1.y, p2.x, p2.y, q1.x, q1.y, q1.x, q1.y+1);
		else
			i1 = Util.getLineLineIntersection(p1.x, p1.y, p2.x, p2.y, q1.x, q1.y, q1.x+1, q1.y - 1./mp);
		
		Point2D i2 = null;
		if (p2.x - p1.x == 0)
			i2 = Util.getLineLineIntersection(p1.x, p1.y, p2.x, p2.y, q2.x, q2.y, q2.x+1, q2.y);
		else if (p2.y-p1.y == 0)
			i2 = Util.getLineLineIntersection(p1.x, p1.y, p2.x, p2.y, q2.x, q2.y, q2.x, q2.y+1);
		else
			i2 = Util.getLineLineIntersection(p1.x, p1.y, p2.x, p2.y, q2.x, q2.y, q2.x+1, q2.y - 1./mp);
		
		if (i1 == null || i2 == null)
			return 0;
		
		Point2D.Double im = new Point2D.Double((i1.getX() + i2.getX())/2, (i1.getY() + i2.getY())/2);
		
		double ilength = Point.distance(i1.getX(), i1.getY(), i2.getX(), i2.getY());
		
		this.i1 = i1;
		this.i2 = i2;
		this.im = im;
		
		
		if (ilength == 0) return 0;
		
		Point2D.Double pMid = new Point2D.Double((p1.x+p2.x)/2, (p1.y+p2.y)/2);
		
		this.pm = pMid;
		
		double pmimlength = Point.distance(pMid.getX(), pMid.getY(), im.getX(), im.getY());
		
		double f = pmimlength/ilength;
		
		
		if (f > 1) return 0;
		
		else return 1-f;
	}

}

