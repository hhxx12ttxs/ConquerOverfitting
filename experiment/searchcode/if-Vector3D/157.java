package graphics;

import util.Vector3D;

public class Box extends Surface{
	private Vector3D minPoint;
	private Vector3D maxPoint;
	
	public Box(){}
	
	public Box(Vector3D minPoint, Vector3D maxPoint){
		this.minPoint = minPoint;
		this.maxPoint = maxPoint;
	}
	public boolean hit(Vector3D start, Vector3D direction, double mint, double maxt){
		double txmin, txmax, tymin, tymax, tzmin, tzmax;
		if(direction.x > 0){
			txmin = (minPoint.x - start.x) / direction.x;
			txmax = (maxPoint.x - start.x) / direction.x;
		}else{
			txmin = (start.x - minPoint.x) / direction.x;
			txmax = (start.x - maxPoint.x) / direction.x;
		}
		if(direction.y > 0){
			tymin = (minPoint.y - start.y) / direction.y;
			tymax = (maxPoint.y - start.y) / direction.y;
		}else{
			tymin = (start.y - minPoint.y) / direction.y;
			tymax = (start.y - maxPoint.y) / direction.y;
		}
		if(direction.z > 0){
			tzmin = (minPoint.z - start.z) / direction.z;
			tzmax = (maxPoint.z - start.z) / direction.z;
		}else{
			tzmin = (start.z - minPoint.z) / direction.z;
			tzmax = (start.z - maxPoint.z) / direction.z;
		}
		double tmin = Math.max(Math.max(txmin, tymin), tzmin);
		double tmax = Math.min(Math.min(txmax, tymax), tzmax);
		if(tmin < tmax) return true;
		else return false;
	}
	
}

