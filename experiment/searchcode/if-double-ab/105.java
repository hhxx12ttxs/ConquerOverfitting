package bubble;

import util.Vector3D;
import graphics.HitRecord;
import graphics.Sphere;

public class BubbleTriplets extends Sphere {
	Sphere A;
	Sphere B;
	Sphere C;
	Sphere D;
	Sphere E;
	Sphere F;

	public BubbleTriplets(Sphere bubbleA, Sphere bubbleB, Sphere bubbleD) {

		// TODO: ranking by size
		this.A = bubbleA;
		this.B = bubbleB;
		this.D = bubbleD;
		MakeTriplets();
	}

	
	private void MakeTriplets(){
		AdjustBPos();
		AdjustDPos();
		C = GenerateC(A, B);
		E = GenerateC(A, D);
		F = GenerateC(B, D);
	}
	
	private void AdjustBPos(){
		double ab2 = A.getRadius() * A.getRadius() + B.getRadius() * B.getRadius() - A.getRadius() * B.getRadius();
		double ab = Math.sqrt(ab2);
		Vector3D normAB = Vector3D.Normalize(Vector3D.Substract(B.getCenter(), A.getCenter()));
		Vector3D newBCenter = Vector3D.Add(A.getCenter(), Vector3D.Scale(normAB, ab));
		B.setCenter(newBCenter);
	}
	
	private void AdjustDPos(){
		double ad2 = A.getRadius() * A.getRadius() + D.getRadius() * D.getRadius() - A.getRadius() * D.getRadius();
		double ad = Math.sqrt(ad2);
		Vector3D normAD = Vector3D.Normalize(Vector3D.Substract(D.getCenter(), A.getCenter()));
		Vector3D newDCenter = Vector3D.Add(A.getCenter(), Vector3D.Scale(normAD, ad));
		D.setCenter(newDCenter);
	}
	
	private Sphere GenerateC(Sphere bubbleA, Sphere bubbleB){
		double radius = bubbleA.getRadius() * bubbleB.getRadius() / (bubbleA.getRadius() - bubbleB.getRadius());
		double bc = Math.sqrt(bubbleB.getRadius() * bubbleB.getRadius() + radius * radius - bubbleB.getRadius() * radius);
		Vector3D normAB = Vector3D.Normalize(Vector3D.Substract(bubbleB.getCenter(), bubbleA.getCenter()));
		Vector3D centerC = Vector3D.Add(bubbleB.getCenter(), Vector3D.Scale(normAB, bc));
		return new Sphere(centerC, radius);
	}
	
	
	public boolean hit(Vector3D start, Vector3D direction, double mint, double maxt){
		return false;
	}
	
	public HitRecord hitNearestPoint(Vector3D start, Vector3D direction, double mint, double maxt){
		//A : A surface but not inside B, not inside D
		//B : B surface but not inside A, not inside D
		//D:  D surface but not inside A, not inside B
		
		//C : C surface but inside B, not inside D
		//E : E surface but inside A, not inside C
		//F : F surface but inside D, inside E
		
		HitRecord record = null;
		double maxDistance = maxt;
		HitRecord recordA = A.hitNearestPoint(start, direction, mint, maxt);
		if(recordA != null && B.isInside(recordA.getHitPt()) == false && D.isInside(recordA.getHitPt()) == false){
			record = recordA;
			maxDistance = record.getDistance();
		} 
		HitRecord recordB = B.hitNearestPoint(start, direction, mint, maxDistance);
		if(recordB != null && A.isInside(recordB.getHitPt()) == false && D.isInside(recordB.getHitPt()) == false){
			record = recordB;
			maxDistance = record.getDistance();
		}
		HitRecord recordD = D.hitNearestPoint(start, direction, mint, maxDistance);
		if(recordD != null && A.isInside(recordD.getHitPt()) == false && B.isInside(recordD.getHitPt()) == false){
			record = recordD;
			maxDistance = record.getDistance();
		}
		
		HitRecord recordC = C.hitNearestPoint(start, direction, mint, maxDistance);
		if(recordC != null && B.isInside(recordC.getHitPt()) == true && D.isInside(recordC.getHitPt()) == false){
			record = recordC;
			maxDistance = record.getDistance();
		}
		HitRecord recordE = E.hitNearestPoint(start, direction, mint, maxDistance);
		if(recordE != null && A.isInside(recordE.getHitPt()) == true && B.isInside(recordE.getHitPt()) == false){
			record = recordE;
			maxDistance = record.getDistance();
		}
		HitRecord recordF = F.hitNearestPoint(start, direction, mint, maxDistance);
		if(recordF != null && D.isInside(recordF.getHitPt()) == true && E.isInside(recordF.getHitPt()) == true){
			record = recordF;
			maxDistance = record.getDistance();
		}
		return record;
	}
}

