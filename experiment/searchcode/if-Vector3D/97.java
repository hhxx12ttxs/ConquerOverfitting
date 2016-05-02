package bubble;

import util.Vector3D;
import graphics.HitRecord;
import graphics.Sphere;

public class BubblePair extends Sphere{
	
	Sphere A;
	Sphere B;
	Sphere C;
	public BubblePair(Sphere bubbleA, Sphere bubbleB){
		
		if(bubbleA.getRadius() >= bubbleB.getRadius()){
			this.A = bubbleA;
			this.B = bubbleB;
		}else{
			this.A = bubbleB;
			this.B = bubbleA;
		}
		MakePair();
	}
	
	private void MakePair(){
		AdjustBPos();
		GenerateC();
	}
	private void AdjustBPos(){
		double ab2 = A.getRadius() * A.getRadius() + B.getRadius() * B.getRadius() - A.getRadius() * B.getRadius();
		double ab = Math.sqrt(ab2);
		Vector3D normAB = Vector3D.Normalize(Vector3D.Substract(B.getCenter(), A.getCenter()));
		Vector3D newBCenter = Vector3D.Add(A.getCenter(), Vector3D.Scale(normAB, ab));
		B.setCenter(newBCenter);
	}
	private void GenerateC(){
		double radius = A.getRadius() * B.getRadius() / (A.getRadius() - B.getRadius());
		double bc = Math.sqrt(B.getRadius() * B.getRadius() + radius * radius - B.getRadius() * radius);
		Vector3D normAB = Vector3D.Normalize(Vector3D.Substract(B.getCenter(), A.getCenter()));
		Vector3D centerC = Vector3D.Add(B.getCenter(), Vector3D.Scale(normAB, bc));
		C = new Sphere(centerC, radius);
	}
	
	public boolean hit(Vector3D start, Vector3D direction, double mint, double maxt){
		return false;
	}
	
	public HitRecord hitNearestPoint(Vector3D start, Vector3D direction, double mint, double maxt){
		//A : A surface but not inside B
		//B : B surface but not inside A
		//C : C surface but inside B
		HitRecord record = null;
		double maxDistance = maxt;
		HitRecord recordA = A.hitNearestPoint(start, direction, mint, maxt);
		if(recordA != null && B.isInside(recordA.getHitPt()) == false){
			record = recordA;
			maxDistance = record.getDistance();
		} 
		HitRecord recordB = B.hitNearestPoint(start, direction, mint, maxDistance);
		if(recordB != null && A.isInside(recordB.getHitPt()) == false){
			record = recordB;
			maxDistance = record.getDistance();
		}
		HitRecord recordC = C.hitNearestPoint(start, direction, mint, maxDistance);
		if(recordC != null && B.isInside(recordC.getHitPt()) == true){
			record = recordC;
			maxDistance = record.getDistance();
		}
		
		return record;
	}
}

