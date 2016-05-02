package prototype2;

import java.util.ArrayList;

public class DirectionVector {
	private double intensity;
	private double orientation;

	public DirectionVector(DirectionVector old) {
		this.intensity = old.intensity;
		this.orientation = old.orientation;
	}

	public DirectionVector(double intensity, double orientation) {
		this.intensity = intensity;
		this.orientation = orientation;
	}

	public DirectionVector() {
		intensity = 0.0;
		orientation = 0.0;
	}

	public double getOrientation() {
		return orientation;
	}

	public double getIntensity() {
		return intensity;
	}

	public static DirectionVector sum(ArrayList<DirectionVector> vectors) {
		if (vectors == null)
			return new DirectionVector();
		if (vectors.size() == 0)
			return new DirectionVector();
		DirectionVector retval = new DirectionVector(vectors.get(0));
		for (DirectionVector directionVector : vectors.subList(1, vectors
				.size())) {
			retval.addDirectionVector(directionVector);
		}
		return retval;
	}

	// FIXME ovo definitivno ne radi kako treba
	public void addDirectionVector(DirectionVector other) {
		if (this.intensity == 0 && this.orientation == 0) {
			this.intensity = other.intensity;
			this.orientation = other.orientation;
			return;
		}
		double hor1, hor2, ver1, ver2;
		hor1 = this.intensity * Math.cos(this.orientation);
		hor2 = other.intensity * Math.cos(other.orientation);
		ver1 = this.intensity * Math.sin(this.orientation);
		ver2 = other.intensity * Math.sin(other.orientation);
//		System.out.println(this.orientation);
//		System.out.println(other.orientation);
//		System.out.println(hor1 + "," + ver1);
//		System.out.println(hor2 + "," + ver2);
		this.intensity = Math.sqrt((hor1 + hor2) * (hor1 + hor2)
				+ (ver1 + ver2) * (ver1 + ver2));
		// this.orientation = Math.atan((ver1 + ver2) / (hor1 + hor2));
		// if (hor1 + hor2 < 0)
		// this.orientation += Math.PI;
		// if(ver1 + ver2 < 0)
		// this.orientation += 2*Math.PI;
		double gx, gy, ga = 0;
		gx = hor1 + hor2;
		gy = ver1 + ver2;
		if (gx != 0.0) {
			ga = (Math.atan(gy / gx));
//			System.out.println(ga);
			if ((gx < 0.0) && (gy < 0.0)) {
				// System.out.println("last");
				ga = ga + Math.PI;
			} else {
				// System.out.println("in");
				if (gx < 0.0) {
					// System.out.println("this");
					ga = Math.PI + ga;
				}
				if (gy < 0.0) {
					// System.out.println("this2");
					ga = ga - 2*Math.PI;
				}
			}
		} else if (gy > 0.0) {
			ga = Math.PI / 2;
		} else if (gy < 0.0)
			ga = (3 / 2) * Math.PI;
		this.orientation = ga;
	}

	public static DirectionVector addTwoVectors(DirectionVector v1,
			DirectionVector v2) {
		DirectionVector retval = new DirectionVector(v1);
		retval.addDirectionVector(v2);
		return retval;
	}

	public String toString() {
		return intensity + "[" + orientation + "]";
	}
}
