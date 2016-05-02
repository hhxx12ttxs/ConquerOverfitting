package raytracer;

import util.Vector3d;

public class RefractionData extends Ray {
	private double reflection;
	private double transmission;
	private Vector3d reflectedDirection;
	
	private static final double EPSILON = 0.000001;
	
	/*
	public RefractionData(Ray reflectedRay, Ray refractedRay, double reflection, double transmission) {
		this.reflectedRay = reflectedRay;
		this.refractedRay = refractedRay;
		this.reflection = reflection;
		this.transmission = transmission;
	}
	*/
	
	public RefractionData(Vector3d reflectedDirection, Vector3d refractedDirection,
			double reflection, double transmission) {
		super(null, refractedDirection);
		this.reflectedDirection = reflectedDirection;
		this.reflection = reflection;
		this.transmission = transmission;
	}
	
	public void setOrigin(Vector3d origin) {
		this.origin = origin;
	}
	
	public Ray getReflectedRay() {
		if (this.reflectedDirection == null) return null;
		return new Ray(this.origin.add(this.reflectedDirection.mul(EPSILON)), this.reflectedDirection);
	}
	
	public Ray getRefractedRay() {
		if (this.direction == null) return null;
		return new Ray(this.origin.add(this.direction.mul(EPSILON)), this.direction);
	}
	
	public double getReflection() {
		return reflection;
	}
	
	public double getTransmission() {
		return transmission;
	}
}

