package util;

import javax.swing.text.Position;

import com.sun.jndi.url.corbaname.corbanameURLContextFactory;

public class Vector3D {
	public double x;
	public double y;
	public double z;

	public Vector3D() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3D Clone(){
		return new Vector3D(x, y, z);
	}
	
	public static Vector3D LinearSum(Vector3D[] vecs, double[] coes) {
		Vector3D newVec = new Vector3D();
		for (int i = 0; i < vecs.length; i++) {
			newVec.x += vecs[i].x * coes[i];
			newVec.y += vecs[i].y * coes[i];
			newVec.z += vecs[i].z * coes[i];
		}
		return newVec;
	}
	
	public double Length() {
		double length = x * x + y * y + z * z;
		return Math.sqrt(length);
	}

	public static Vector3D Normalize(Vector3D vec){
		double length = vec.Length();
		if(length != 0){
			return new Vector3D(vec.x / length, vec.y / length, vec.z / length);
		}else{
			return vec;
		}
	}
	public static Vector3D Multiply(Vector3D vec, double coe) {
		return new Vector3D(vec.x * coe, vec.y * coe, vec.z * coe);
	}

	public static Vector3D Add(Vector3D a, Vector3D b) {
		return new Vector3D(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public static Vector3D Substract(Vector3D a, Vector3D b) {
		return new Vector3D(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static double DotProduct(Vector3D a, Vector3D b) {
		double product = a.x * b.x + a.y * b.y + a.z * b.z;
		return product;
	}

	public static Vector3D CrossProduct(Vector3D a, Vector3D b){
		double x = a.y * b.z - a.z * b.y;
		double y = a.z * b.x - a.x * b.z;
		double z = a.x * b.y - a.y * b.x;
		return new Vector3D(x, y, z);
	}
	
	public static double cos(Vector3D a, Vector3D b) {
		double product = Vector3D.DotProduct(a, b);
		double lengthA = a.Length();
		double lengthB = b.Length();
		if (lengthA == 0 || lengthB == 0) {
			return -9999;
		} else {
			return product / lengthA / lengthB;
		}
	}

	public static Vector3D Scale(Vector3D vec, double scale){
		return new Vector3D(vec.x * scale, vec.y * scale, vec.z * scale);
	}
	
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
	
	public void Print(String description){
		System.out.println(description + "= " + x + "\t" + y + "\t" + z);
		
	}
}

