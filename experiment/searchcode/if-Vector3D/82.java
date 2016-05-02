package graphics;

import java.util.Random;

import util.PerlinNoise;
import util.Vector3D;

public class Sphere extends Surface{
	
	private Vector3D center;
	private double radius;
	
	public Sphere() {
		// TODO Auto-generated constructor stub
	}
	
	public Sphere(Vector3D center, double radius){
		this.center = center;
		this.radius = radius;
	}
	
	public boolean hit(Vector3D start, Vector3D direction, double mint, double maxt){//ray = e + td
		Vector3D ec = Vector3D.Substract(start, center);
		double ec2 =Vector3D.DotProduct(ec, ec);
		double d2 = 1;//Length(direction) = 1;
		double ecd = Vector3D.DotProduct(direction, ec);
		double discriminant = ecd * ecd - d2 * (ec2 - radius*radius);
		boolean hit = false;
		if(discriminant > 0){
			double sqrt = Math.sqrt(discriminant);
			double t1 = (-1*ecd - sqrt) / d2;
			double t2 = (-1*ecd + sqrt) / d2;
			if((t1 > mint && t1 < maxt) || (t1 > mint && t2 < maxt)){
				hit = true;
			}
		}
		return hit;
	}
	
	/* get the hit point. if many, return the nearest one*/
	public HitRecord hitNearestPoint(Vector3D start, Vector3D direction, double mint, double maxt){
		Vector3D ec = Vector3D.Substract(start, center);
		double ec2 =Vector3D.DotProduct(ec, ec);
		double d2 = 1;//Length(direction) = 1;
		double ecd = Vector3D.DotProduct(direction, ec);
		double discriminant = ecd * ecd - d2 * (ec2 - radius*radius);
		HitRecord record = null;
		if(discriminant > 0){
			double sqrt = Math.sqrt(discriminant);
			double t1 = (-1*ecd - sqrt) / d2;
			double t2 = (-1*ecd + sqrt) / d2;
			double distance = -1;
			if(t2 > mint && t2 < maxt) distance = t2;
			if(t1 > mint && t1 < maxt) distance = t1;//t1 < t2
			if(distance > 0){
				Vector3D hitPt = Vector3D.Add(start, Vector3D.Scale(direction, distance));
				record = new HitRecord(hitPt, this, distance);
			}
		}
		
		return record;
	}
	
	public double getThickness(Vector3D pt, double perlineRandom){
		//compute by pt.y & central.y
				//300nm ~ 2000 nm
				double min = 700;
				double max = 2000;
				double relative = (pt.y - (center.y - radius)) / (radius * 2);
				if(relative < 0) relative = 0;
				if(relative > 1) relative = 1;
				double thickness = (max - min) * (1-relative) + min;
				thickness += 300 * (2 + perlineRandom * 3) * (PerlinNoise.noise(pt.x/8, pt.y/8, pt.z/8));
				return thickness;
	}
	
	public boolean isInside(Vector3D pt){
		Vector3D pc = Vector3D.Substract(pt, center);
		if(pc.Length() < radius) return true;
		else return false;
	}
	
	public Vector3D getNormal(Vector3D pt){
		Vector3D normal = Vector3D.Substract(pt, center);
		return Vector3D.Normalize(normal);
	}

	public Vector3D getCenter() {
		return center;
	}

	public void setCenter(Vector3D center) {
		this.center = center;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
	
}

