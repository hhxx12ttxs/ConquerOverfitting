package graphics;

import util.Vector3D;

public class TriangleFilm extends Triangle{
	
	private double thickness;
	
	public TriangleFilm(){
		super();
	}
	
	public TriangleFilm(Vector3D a, Vector3D b, Vector3D c){
		super(a, b, c);
	}
	
	public TriangleFilm(Vector3D[] verticles){
		super(verticles);
	}
	
	public boolean hit(Vector3D start, Vector3D direction, double mint,
			double maxt){
		return super.hit(start, direction, mint, maxt);
	}
	
	public HitRecord hitNearestPoint(Vector3D start, Vector3D direction,
			double mint, double maxt) {
		HitRecord record  = super.hitNearestPoint(start, direction, mint, maxt);
		Vector3D outerPt = record.getHitPt();
		//if normal*direction < 0 ==> return the outer
		//else 					  ==> return the inner
		if(Vector3D.DotProduct(normal, direction) < 0){
			return record;
		}else{
			//inner = outer - thickness/sin * direction
			double cos = Vector3D.cos(direction, normal);
			double sin = Math.sqrt(1 - cos * cos);
			Vector3D innerPt = Vector3D.Add(outerPt, Vector3D.Multiply(direction, -1 * thickness / sin));
			HitRecord innerRecord = new HitRecord(innerPt,this, record.getDistance() - thickness / sin);
			return innerRecord;
		}
	} 
}

