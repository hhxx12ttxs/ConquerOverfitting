/**
 * @author Eric D. Dill eddill@ncsu.edu
 * @author James D. Martin jdmartin@ncsu.edu
 * Copyright � 2010-2013 North Carolina State University. All rights reserved
 */
package geometry;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;


/**
 * A JVector has coordinates (i, j, k)
 * 
 * @author Eric Dill
 * 			eddill@ncsu.edu
 * @version 1.1
 * 
 * @since
 * 	Version 1.1 changes: 
 * 	Added length() method.
 * 	Added unit() method.
 * 	Changed toString() method to output coordinates in tab delimited format
 */
public class JVector implements Cloneable, Serializable, Comparable<JVector>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6422841222417090890L;
	private static final double tolerance = 1e-6;
	/* The i-coordinate of the JVector */
	public double i;
	
	/* The j-coordinate of the JVector */
	public double j;
	
	/* The k-coordinate of the JVector */
	public double k;
	
	public final static JVector zero = new JVector(0, 0, 0);
	public final static JVector x = new JVector(1, 0, 0);
	public final static JVector y = new JVector(0, 1, 0);
	public final static JVector z = new JVector(0, 0, 1);
	public final static JVector xy = new JVector(1, 1, 0);
	public final static JVector xz = new JVector(1, 0, 1);
	public final static JVector yz = new JVector(0, 1, 1);
	public final static JVector xyz = new JVector(1, 1, 1);
	public final static JVector[] v100s = get100Family();
	public final static JVector[] v110s = get110Family();
	public final static JVector[] v111s = get111Family();
	public final static JVector[] v112s = get112Family();
	public final static JVector[][] axes100 = getAxes(JVector.get100Family(), JVector.get100Family(), JVector.get100Family());
	public final static JVector[][] axes110 = getAxes(JVector.get100Family(), JVector.get110Family(), JVector.get110Family());
	public final static JVector[][] axes111 = getAxes(JVector.get110Family(), JVector.get112Family(), JVector.get111Family());
	public final static JVector[][] axes112 = getAxes(JVector.get110Family(), JVector.get111Family(), JVector.get112Family());

	public final static JVector[] v100sU = get100FamilyUnique();
	public final static JVector[] v110sU = get110FamilyUnique();
	public final static JVector[] v111sU = get111FamilyUnique();
	public final static JVector[] v112sU = get112FamilyUnique();
	public final static JVector[][] axes100U = getUniqueAxes(JVector.get100Family(), JVector.get100Family(), JVector.get100Family());
	public final static JVector[][] axes110U = getUniqueAxes(JVector.get100Family(), JVector.get110Family(), JVector.get110Family());
	public final static JVector[][] axes111U = getUniqueAxes(JVector.get110Family(), JVector.get112Family(), JVector.get111Family());
	public final static JVector[][] axes112U = getUniqueAxes(JVector.get110Family(), JVector.get111Family(), JVector.get112Family());
	public final static JVector[] cube_corners = getCubeCorners();
	public final static JVector[] cube_faces = getCubeFaces();
	public static final JVector[] fcc2x2x2Pos = get2x2x2FCCPositions();
	public static final JVector[] firstShell = getFirstShell();
	public static final JVector[] secondShell = getSecondShell();
	public static final JVector[] fcc1 = fcc(1);
	public static final JVector[] fcc2 = fcc(2);
	public static final JVector[] fcc3 = fcc(3);
	public static final JVector[] fcc4 = fcc(4);
	public static final JVector[] fcc5 = fcc(5);
	public static final JVector[] fcc6 = fcc(6);

	/**
	 * Constructor to initialize a new JVector object with coordinates
	 * @param i	The i coordinate
	 * @param j	The j coordinate
	 * @param k	The k coordinate
	 */
	public JVector(double i, double j, double k)
	{
		this.i = i;
		
		this.j = j;
		
		this.k = k;
	}
	/**
	 * Constructor to initialize a new JVector object with coordinates
	 * @param i	The i coordinate
	 * @param j	The j coordinate
	 * @param k	The k coordinate
	 */
	public JVector(int[] vals)
	{
		i = vals[0];
		j = vals[1];
		k = vals[2];
	}
	/**
	 * Constructor to initialize a new JVector object to 0, 0, 0
	 */
	public JVector()
	{
		i = 0;
		j = 0;
		k = 0;
	}
	/* --------------------------------------------------------------- */
	
						/* CLASS OPERATIONS */
	
	/* --------------------------------------------------------------- */
	
	
	/**
	 * Method to add two vectors
	 * @param one	A vector
	 * @param two	A vector
	 * @return	A new vector object pointing from the tail of one to the head of two
	 */
	public static JVector add(JVector one, JVector two) { return new JVector(one.i + two.i, one.j + two.j, one.k + two.k); }
	
	/**
	 * Method to subtract two vectors in the order one - two
	 * @param one	A vector
	 * @param two	A vector
	 * @return	A new vector object pointing from the head of two to the head of one
	 */
	public static JVector subtract(JVector one, JVector two) { return new JVector(one.i - two.i, one.j - two.j, one.k - two.k); }
	
	/**
	 * Method to calculate the dot product of the two vectors in the order one . two
	 * @param one	A vector
	 * @param two	A vector
	 * @return	The dot product of the two vectors. Returns a new JVector object.
	 */
	public static double dot(JVector one, JVector two) { return one.i * two.i + one.j * two.j + one.k * two.k; }
	//public static double angle(JVector one, JVector two) { return 180/Math.PI*Math.acos(dot(one, two)/one.length()/two.length()); }
	public static double angleDegrees(JVector one, JVector two) {
		double len = one.length()*two.length();
		double dot = dot(one, two);
		if(Math.abs((dot-len)/dot)<tolerance)
			len=dot;
		if(Math.abs((len-dot)/len)<tolerance)
			dot=len;
		double acos = Math.acos(dot/len);
		double angle = 180 /Math.PI*acos;
		return angle; 
	}
	public static double angleRadians (JVector one, JVector two) { return Math.acos(dot(one, two)/one.length()/two.length()); }
	/**
	 * Method to calculate the cross product of the two vectors in the order one x two
	 * @param one	A vector
	 * @param two	A vector
	 * @return	The cross product of the two vectors. Returns a new JVector object.
	 */
	public static JVector cross(JVector one, JVector two)
	{
		double x = one.j * two.k - one.k * two.j;
		
		double y = one.k * two.i - one.i * two.k;
		
		double z = one.i * two.j - one.j * two.i;
		
		return new JVector(x, y, z);
	}
	
	/**
	 * Method to multiply the calling JVector by the passed Double
	 * @param toMultiply	The double to multiply the calling vector by
	 * @return	The product of the JVector by a scalar.  New JVector Object.
	 */
	public static JVector multiply(JVector v, double scalar)
	{
		JVector temp = (JVector) v.clone();
		
		temp.i *= scalar;

		temp.j *= scalar;
		
		temp.k *= scalar;
		
		return temp;
	}
	public static JVector perturb(JVector v1, JVector origin, double phiToPerturb) {
		JVector axis = new JVector(Math.random(), Math.random(), Math.random());
		return Quaternion.rotate(new Quaternion(v1), axis, origin, phiToPerturb).position;
	}
	public static JVector perturb(JVector v1, double phiToPerturb) {
		JVector axis = new JVector(Math.random(), Math.random(), Math.random());
		return Quaternion.rotate(new Quaternion(v1), axis, new JVector(0, 0, 0), phiToPerturb).position;
	}
	/**
	 * This method calculates the clockwise rotation of a JVector respective to a 
	 * JVector origin relative to a JVector axis by an angle phi.
	 * @param v1	The vector to rotate
	 * @param axis	The axis to rotate around
	 * @param origin	The orgin of rotation
	 * @param phi	The angle to rotate by IN DEGREES
	 * @return The rotated vector. New JVector object.
	 */
	public static JVector rotate(JVector v1, JVector axis, JVector origin, double phi) {
		return Quaternion.rotate(new Quaternion(v1), axis, new JVector(0, 0, 0), phi).position;
	}
	public static double distance(JVector v1, JVector v2) {
		return (subtract(v1, v2)).length();
	}
	/* --------------------------------------------------------------- */
	
						/* FAMILIES OF VECTORS */
	
	/* --------------------------------------------------------------- */
	public static JVector[] getSecondShell() {
		JVector[] secondShell = new JVector[55];
		int idx = 0;
		JVector test;
		boolean exists;
		for(int i = 0; i < firstShell.length; i++) {
			for(int j = 0; j < firstShell.length; j++) {
				test = add(firstShell[i], firstShell[j]);
				exists = false;
				for(int k = 0; k < idx; k++) {
					if(subtract(test, secondShell[k]).roundInt().length() == 0) {
						exists = true;
						break;
					}
				}
				if(!exists) {
					secondShell[idx] = (JVector) test.clone();
					idx++;
				}
			}
		}
		return secondShell;
	}
	public static JVector[] getFirstShell() {
		JVector[] firstShell = new JVector[13];
		firstShell[0] = new JVector(0, 0, 0);
		for(int i = 1; i < 13; i++) { firstShell[i] = JVector.v110s[i-1]; }
		return firstShell;
	}
	
	public static JVector[] getCubeCorners() {
		JVector[] corners = new JVector[8];
		corners[0] = new JVector(0, 0, 0);
		corners[1] = new JVector(1, 0, 0);
		corners[2] = new JVector(0, 1, 0);
		corners[3] = new JVector(0, 0, 1);
		corners[4] = new JVector(1, 1, 0);
		corners[5] = new JVector(1, 0, 1);
		corners[6] = new JVector(0, 1, 1);
		corners[7] = new JVector(1, 1, 1);
		return corners;
	}
	public static JVector[] getCubeFaces() {
		JVector[] faces = new JVector[6];
		faces[0] = new JVector(.5, .5, 0);
		faces[1] = new JVector(.5, 0, .5);
		faces[2] = new JVector(0, .5, .5);
		faces[3] = new JVector(.5, .5, 1);
		faces[4] = new JVector(.5, 1, .5);
		faces[5] = new JVector(1, .5, .5);
		return faces;
	}
	public static boolean inArray(JVector[] array, JVector test) {
		for(int i = 0; i < array.length; i++) {
			if(array[i] == null) { continue; }
			if(subtract(array[i], test).length() == 0) { return true; }
		}
		return false;
	}
	public static boolean isSameFamily(JVector v1, JVector v2, int numDecimalPlaces) {
		v1.abs();
		v2.abs();
		double[] arr1 = new double[] {v1.i, v1.j, v1.k};
		double[] arr2 = new double[] {v2.i, v2.j, v2.k};
		
		Arrays.sort(arr1);
		Arrays.sort(arr2);
		
		double d1, d2;
		for(int i = 0; i < arr1.length; i++) {
			d1 = Math.rint(arr1[i] * Math.pow(10, numDecimalPlaces))/Math.pow(10, numDecimalPlaces);
			d2 = Math.rint(arr2[i] * Math.pow(10, numDecimalPlaces))/Math.pow(10, numDecimalPlaces);
			if(d1 != d2)
				return false;
		}
		
		return true;
		
	}
	public static JVector[] get2x2x2FCCPositions() {
		JVector[] fccPos = new JVector[63];
		JVector temp;
		int idx = 0;

		for(int i = 0; i < cube_corners.length; i++) {
			for(int j = 0; j < cube_corners.length; j++) {
				temp = JVector.add(cube_corners[i], cube_corners[j]);
				if(!inArray(fccPos, temp)) {
					fccPos[idx] = (JVector) temp.clone();
					idx++;
				}
			}
			for(int j = 0; j < cube_faces.length; j++) {
				temp = JVector.add(cube_corners[i], cube_faces[j]);
				if(!inArray(fccPos, temp)) {
					fccPos[idx] = (JVector) temp.clone();
					idx++;
				}
			}
		}
		return fccPos;
	}
	public static JVector[] getPrimitivePositions(int n, int boxSize) {
		int numPos = (int) Math.pow(n/boxSize, 3);
		JVector[] primPos = new JVector[numPos];
		int idx = 0;
		for(int a = 0; a < n; a+=boxSize) {
			for(int b = 0; b < n; b+=boxSize) {
				for(int c = 0; c < n; c+=boxSize) {
					primPos[idx] = new JVector(a, b, c);
					idx++;
				}
			}
		}
		return primPos;
	}
	public static JVector[] getNextFCC(JVector pos) {
		JVector[] next = new JVector[12];
		for(int i = 0; i < v110s.length; i++) {
			next[i] = JVector.add(pos, v110s[i]);
		}
		return next;
	}
	public static JVector[] fcc(int layers) {
		int numMols;
		if(layers%2 == 0) { numMols = (int) (4*Math.pow(layers/2, 3) + 6 * Math.pow(layers/2, 2) + 3*layers/2 + 1); }
		else  { numMols = (int) (4*Math.pow((layers+1)/2, 3)); }
		JVector[] fcc = new JVector[numMols];
		fcc[0] = new JVector(0, 0, 0);
		JVector[] toIns;
		int idx = 1;
		for(int i = 0; i <= layers; i++) {
			for(int j = 0; j < fcc.length; j++) {
				if(fcc[j] == null) { continue; }
				toIns = getNextFCC(fcc[j]);
				for(int k = 0; k < toIns.length; k++) {
					if(!inArray(fcc, toIns[k]) && toIns[k].i < layers+1 && toIns[k].j < layers+1 && toIns[k].k < layers+1
							&& toIns[k].i >= 0 && toIns[k].j >= 0 && toIns[k].k >= 0) {
						fcc[idx] = (JVector) toIns[k].clone();
						idx++;
					}
				}
			}
		}
		return fcc;
	}
	public static JVector[] getFCCPositions(int n) {
		int numPos = 4*n*n*n + 6*n*n + 3*n + 1;
		JVector[] fccPos = new JVector[numPos];
		JVector temp;
		int idx = 0;
		JVector trans = new JVector();
		for(int a = 0; a < n; a++) {
			trans.i = a;
			for(int b = 0; b< n; b++) {
				trans.j = b;
				for(int c = 0; c < n; c++) {
					trans.k = c;
					for(int j = 0; j < cube_corners.length; j++) {
						temp = JVector.add(trans, cube_corners[j]);
						if(!inArray(fccPos, temp)) {
							fccPos[idx] = (JVector) temp.clone();
							idx++;
						}
					}
					for(int j = 0; j < cube_faces.length; j++) {
						temp = JVector.add(trans, cube_faces[j]);
						if(!inArray(fccPos, temp)) {
							fccPos[idx] = (JVector) temp.clone();
							idx++;
						}
					}
				}
			}
		}
		
		return fccPos;
	}
	public static JVector[] getH00(int h, int zero) {
		JVector[] h00 = new JVector[6];
		h00[0] = new JVector(h, zero, zero);
		h00[1] = new JVector(-h, zero, zero);
		h00[2] = new JVector(zero, h, zero);
		h00[3] = new JVector(zero, -h, zero);
		h00[4] = new JVector(zero, zero, h);
		h00[5] = new JVector(zero, zero, -h);
		return h00;
	}
	public static JVector[] get100Family() {
		return getH00(1, 0);
	}
	
	public static JVector[] getHKL(JVector vec) {
		return getHKL(vec.i, vec.j, vec.k);
	}
	public static JVector[] getHKL(double h, double k, double l) {
		JVector[] hkl = new JVector[48];
		Vector<JVector[]> all = new Vector<JVector[]>();
		all.add(getHKLSubset(h,k,l));
		all.add(getHKLSubset(h,l,k));
		all.add(getHKLSubset(k,h,l));
		all.add(getHKLSubset(k,l,h));
		all.add(getHKLSubset(l,h,k));
		all.add(getHKLSubset(l,k,h));

		int idx = 0;
		while(all.size() > 0) {
			JVector[] set = all.remove(0);
			for(int i = 0; i < set.length; i++) {
				hkl[idx++] = set[i];
			}
		}
		return removeDuplicates(hkl);
	}
	
	private static JVector[] removeDuplicates(JVector[] hkl) {
		Vector<JVector> free = new Vector<JVector>();
		JVector v1, v2;
		for(int i = 0; i < hkl.length; i++) {
			v1 = hkl[i];
			boolean isDuplicate = false;
			for(int j = i+1; j < hkl.length; j++) {
				v2 = hkl[j];
				if(JVector.angleDegrees(v1, v2) == 0) {
					isDuplicate = true;
				}
			}
			if(!isDuplicate) {
				free.add(v1);
			}
		}
		JVector[] duplicatesRemoved = new JVector[free.size()];
		duplicatesRemoved = free.toArray(duplicatesRemoved);
		return duplicatesRemoved;
	}
	
	private static JVector[] getHKLSubset(double h, double k, double l) {
		JVector[] set = new JVector[8];
		
		set[0] = new JVector(h, k, l);
		set[1] = new JVector(h, k, -l);
		set[2] = new JVector(h, -k, l);
		set[3] = new JVector(-h, k, l);
		set[4] = new JVector(h, -k, -l);
		set[5] = new JVector(-h, k, -l);
		set[6] = new JVector(-h, -k, l);
		set[7] = new JVector(-h, -k, -l);
		
		return set;
	}
	
	public static JVector[] get110Family() {
		JVector[] axes110 = new JVector[12];
		// <110> set
		axes110[0] = new JVector(1, 1, 0);
		axes110[1] = new JVector(1, -1, 0);
		axes110[2] = new JVector(-1, 1, 0);
		axes110[3] = new JVector(-1, -1, 0);
		// <101> set
		axes110[4] = new JVector(1, 0, 1);
		axes110[5] = new JVector(1, 0, -1);
		axes110[6] = new JVector(-1, 0, 1);
		axes110[7] = new JVector(-1, 0, -1);
		// <011> set
		axes110[8] = new JVector(0, 1, 1);
		axes110[9] = new JVector(0, 1, -1);
		axes110[10] = new JVector(0, -1, 1);
		axes110[11] = new JVector(0, -1, -1);
		
		return axes110;
	}
	
	public static JVector[] get111Family() {
		JVector[] axes111 = new JVector[8];
		axes111[0] = new JVector(1, 1, 1);
		axes111[1] = new JVector(-1, 1, 1);
		axes111[2] = new JVector(1, -1, 1);
		axes111[3] = new JVector(1, 1, -1);
		axes111[4] = new JVector(-1, -1, 1);
		axes111[5] = new JVector(-1, 1, -1);
		axes111[6] = new JVector(1, -1, -1);
		axes111[7] = new JVector(-1, -1, -1);
		return axes111;
	}
	
	public static JVector[] get112Family() {
		JVector[] axes112 = new JVector[24];
		// <112> set
		axes112[0] = new JVector(1, 1, 2);
		axes112[1] = new JVector(1, -1, 2);
		axes112[2] = new JVector(-1, 1, 2);
		axes112[3] = new JVector(-1, -1, 2);
		axes112[4] = new JVector(1, 1, -2);
		axes112[5] = new JVector(1, -1, -2);
		axes112[6] = new JVector(-1, 1, -2);
		axes112[7] = new JVector(-1, -1, -2);
		// <121> set
		axes112[8] = new JVector(1, 2, 1);
		axes112[9] = new JVector(1, 2, -1);
		axes112[10] = new JVector(-1, 2, 1);
		axes112[11] = new JVector(-1, 2, -1);
		axes112[12] = new JVector(1, -2, 1);
		axes112[13] = new JVector(1, -2, -1);
		axes112[14] = new JVector(-1, -2, 1);
		axes112[15] = new JVector(-1, -2, -1);
		// <211> set
		axes112[16] = new JVector(2, 1, 1);
		axes112[17] = new JVector(2, 1, -1);
		axes112[18] = new JVector(2, -1, 1);
		axes112[19] = new JVector(2, -1, -1);
		axes112[20] = new JVector(-2, 1, 1);
		axes112[21] = new JVector(-2, 1, -1);
		axes112[22] = new JVector(-2, -1, 1);
		axes112[23] = new JVector(-2, -1, -1);
		
		return axes112;
	}

	public static JVector[] get100FamilyUnique() {
		JVector[] axes100 = new JVector[3];
		axes100[0] = new JVector(1, 0, 0);
		axes100[1] = new JVector(0, 1, 0);
		axes100[2] = new JVector(0, 0, 1);
		return axes100;
	}
	public static JVector[] get110FamilyUnique() {
		JVector[] axes110 = new JVector[6];
		// <110> set
		axes110[0] = new JVector(1, 1, 0);
		axes110[1] = new JVector(1, -1, 0);
		// <101> set
		axes110[2] = new JVector(1, 0, 1);
		axes110[3] = new JVector(1, 0, -1);
		// <011> set
		axes110[4] = new JVector(0, 1, 1);
		axes110[5] = new JVector(0, 1, -1);
		
		return axes110;
	}
	
	public static JVector[] get111FamilyUnique() {
		JVector[] axes111 = new JVector[8];
		axes111[0] = new JVector(1, 1, 1);
		axes111[1] = new JVector(-1, 1, 1);
		axes111[2] = new JVector(1, -1, 1);
		axes111[3] = new JVector(1, 1, -1);
		return axes111;
	}
	
	public static JVector[] get112FamilyUnique() {
		JVector[] axes112 = new JVector[12];
		// <112> set
		axes112[0] = new JVector(1, 1, 2);
		axes112[1] = new JVector(1, -1, 2);
		axes112[2] = new JVector(-1, 1, 2);
		axes112[3] = new JVector(-1, -1, 2);
		// <121> set
		axes112[4] = new JVector(1, 2, 1);
		axes112[5] = new JVector(1, 2, -1);
		axes112[6] = new JVector(-1, 2, 1);
		axes112[7] = new JVector(-1, 2, -1);
		// <211> set
		axes112[8] = new JVector(2, 1, 1);
		axes112[9] = new JVector(2, 1, -1);
		axes112[10] = new JVector(2, -1, 1);
		axes112[11] = new JVector(2, -1, -1);
		
		return axes112;
	}
	private static boolean checkForOrthogonality(JVector x, JVector y, JVector z) {
		double xyDot, xzDot, yzDot, xAngle = 90, yAngle=90, zAngle=90;
		JVector xTest, yTest, zTest;
		xyDot = dot(x, y);
		xzDot = dot(x, z);
		yzDot = dot(y, z);
		if(Math.abs(xyDot) < tolerance && Math.abs(xzDot) < tolerance && Math.abs(yzDot) < tolerance) {
			zTest = cross(x, y);
			yTest = cross(z, x);
			xTest = cross(y, z);
			/*
			try {
				xAngle = angle(xTest.unit(), x.unit());
				yAngle = angle(yTest.unit(), y.unit());
				zAngle = angle(zTest.unit(), z.unit());
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/

			xAngle = angleDegrees(xTest, x);
			yAngle = angleDegrees(yTest, y);
			zAngle = angleDegrees(zTest, z);
			if(Math.abs(xAngle) < tolerance && Math.abs(yAngle) < tolerance && Math.abs(zAngle) < tolerance)
				return true;
		}
		return false;
	}
	public static JVector[][] getAxes(JVector[] x, JVector[] y, JVector[] z) {
		JVector[][] temp = new JVector[100][3];
		int tempIdx = 0;
		Stack<Integer> stack = new Stack<Integer>();
		for(int k = 0; k < z.length; k++) {
			for(int j = 0; j < y.length; j++) {
				for(int i = 0; i < x.length; i++) {		
					if(checkForOrthogonality(x[i], y[j], z[k])) {
						temp[tempIdx][0] = z[k];
						temp[tempIdx][1] = x[i];
						temp[tempIdx][2] = y[j];
						tempIdx++;
					}
				}
			}
		}
		for(int i = 0; i < temp.length; i++) {
			if(temp[i][0] == null)
				temp[i] = null;
		}
		double phi;
		for(int i = 0; i < temp.length; i++) {
			/* check to see if the vector at temp[j] already exists in the region temp[0->i] */
			if(temp[i] == null) { continue; }
			for(int j = 0; j < i; j++) {
				if(temp[i] == null) { continue; }
				if(temp[j] == null) { continue; }
				phi = angleDegrees(temp[i][0], temp[j][0]);
				if(Math.abs(phi) < tolerance) {
					temp[i] = null;
				}
			}
		}
		for(int i = 0; i < temp.length; i++) {
			if(temp[i] != null)
				stack.push(i);
		}
		JVector[][] axes = new JVector[stack.size()][3];
		int idx = 0;
		for(int i = 0; i < axes.length; i++) {
			idx = stack.pop();
			axes[i][0] = (JVector) temp[idx][0].clone();
			axes[i][1] = (JVector) temp[idx][1].clone();
			axes[i][2] = (JVector) temp[idx][2].clone();
		}
		temp = null;
		return axes;
	}
	public static JVector[][] getUniqueAxes(JVector[] x, JVector[] y, JVector[] z) {
		JVector[][] axes = getAxes(x, y, z);
		Stack<Integer> stack = new Stack<Integer>();
		stack.clear();
		JVector cur;
		boolean alreadyIn = false;
		// remove duplicate axes
		for(int i = 0; i < axes.length; i++) {
			cur = JVector.multiply(axes[i][0], -1);
			alreadyIn = false;
			for(int j = i; j < axes.length; j++) {
				if(JVector.angleDegrees(cur, axes[j][0]) == 0)
					alreadyIn = true;
			}
			if(!alreadyIn)
				stack.push(i);
		}
		int stackSize = stack.size();
		int stackIdx = 0;
		JVector[][] axesNew = new JVector[stackSize][3];
		// make a new array with only the unique axes
		for(int i = 0; i < stackSize; i++) {
			stackIdx = stack.pop();
			for(int j = 0; j < 3; j++) {
				axesNew[i][j] = axes[stackIdx][j];
			}
		}
		return axesNew;
	}
	
	public static double det(JVector p1, JVector p2, JVector p3) {
		double a = p1.i*(p2.j*p3.k - p2.k*p3.j);
		double b = p1.j*(p2.k*p3.i-p2.i*p3.k);
		double c = p1.k*(p2.i*p3.j-p2.j*p3.i);
		double d = a + b + c;
	
		return d;
	}
	
	public static JVector[] getRandomlyAlignedOrthogonalAxes() {
		Random r = new Random();
		JVector[] axes = JVector.get100FamilyUnique();
		double phi = (r.nextDouble()-.5)*180;
		axes[0] = JVector.rotate(axes[0], axes[2], JVector.zero, phi);
		axes[1] = JVector.rotate(axes[1], axes[2], JVector.zero, phi);

		phi = (r.nextDouble()-.5)*180;
		axes[0] = JVector.rotate(axes[0], axes[1], JVector.zero, phi);
		axes[2] = JVector.rotate(axes[2], axes[1], JVector.zero, phi);
		
		phi = (r.nextDouble()-.5)*180;
		axes[1] = JVector.rotate(axes[1], axes[0], JVector.zero, phi);
		axes[2] = JVector.rotate(axes[2], axes[0], JVector.zero, phi);
		
		return axes;
	}
	/* --------------------------------------------------------------- */
	
						/* INSTANCE OPERATIONS */
	
	/* --------------------------------------------------------------- */
	
	public double[] toArray() {
		return new double[] {i, j, k};
	}
	/**
	 * Method to calculate the length of the vector
	 * @return	The length of the vector
	 */
	public double length() { return Math.sqrt(i*i + j*j + k*k); }
	/**
	 * Returns a new JVector object with i, j, k rounted to the nearest int via the Math.round() method.
	 * @return new JVector(Math.round(i), Math.round(j), Math.round(k)
	 */
	public JVector roundInt() {
		return new JVector(Math.round(i), Math.round(j), Math.round(k));
	}
	
	/**
	 * rounds i, j, k to their integer values
	 * @return long[] toInt = {Math.round(i), Math.round(j), Math.round(k)};
	 */
	public int[] toInt() {
		int[] toInt = {(int) Math.round(i), (int) Math.round(j), (int) Math.round(k)};
		return toInt;
	}
	
	/* --------------------------------------------------------------- */
	
					/* IN PLACE CALCULATIONS */

	/* --------------------------------------------------------------- */
	
	/**
	 * In place calculation of the absolute value of the vector
	 */
	public void abs() {
		assert assertNumsAreValid(this);
		i = Math.abs(i);
		j = Math.abs(j);
		k = Math.abs(k);
	}
	/**
	 * Method to determine the unit vector of this vector object
	 * @return	A NEW vector object equal to the unit vector of the calling vector
	 */
	public JVector unit() {
		assert assertNumsAreValid(this);
		double length = length();
		if(length == 0) { return new JVector(0, 0, 0); }
		
		return new JVector(i / length, j / length, k / length);
	}
	public void multiply(double val) {
		assert assertNumsAreValid(this);
		i *= val;
		j *= val;
		k *= val;
	}
	public void unit_inPlace() {
		assert assertNumsAreValid(this);
		divide(length());
	}
	public void add(JVector toAdd) {
		assert assertNumsAreValid(this);
		assert assertNumsAreValid(toAdd);
		
		i += toAdd.i;
		j += toAdd.j;
		k += toAdd.k;
	}
	public void divide(double val) {
		assert val != 0;
		assert assertNumsAreValid(this);
		
		i /= val;
		j /= val;
		k /= val;
	}
	
	public void set(JVector toSet) {
		assert assertNumsAreValid(this);
		assert assertNumsAreValid(toSet);
		i = toSet.i;
		j = toSet.j;
		k = toSet.k;
	}
	private boolean assertNumsAreValid(JVector v) {
		assert !Double.isInfinite(v.i) : "JVector.assertNumsAreValid(). i is infinite";
		assert !Double.isInfinite(v.j) : "JVector.assertNumsAreValid(). j is infinite";
		assert !Double.isInfinite(v.k) : "JVector.assertNumsAreValid(). k is infinite";
		assert !Double.isInfinite(v.i) : "JVector.assertNumsAreValid(). i is NaN";
		assert !Double.isInfinite(v.j) : "JVector.assertNumsAreValid(). j is NaN";
		assert !Double.isInfinite(v.k) : "JVector.assertNumsAreValid(). k is NaN";
		
		return true;
	}
	
	/* --------------------------------------------------------------- */
	
					/* BASIC CLASS OPERATIONS */

	/* --------------------------------------------------------------- */
	
	/**
	 * Setter method for the i coordinate
	 * @param i	The i coordinate
	 */
	public void setI(double i) { this.i = i; }
	
	/**
	 * Setter method for the j coordinate
	 * @param j	The j coordinate
	 */
	public void setJ(double j) { this.j = j; }
	
	/**
	 * Setter method for the k coordinate
	 * @param k	The k coordinate
	 */
	public void setK(double k) { this.k = k; }
	
	/**
	 * Getter method for the i coordinate
	 * @return	The i coordinate
	 */
	public double getI() { return i; }
	
	/**
	 * Getter method for the j coordinate
	 * @return	The j coordinate
	 */
	public double getJ() { return j; }
	
	/**
	 * Getter method for the k coordinate
	 * @return	The k coordinate
	 */
	public double getK() { return k; }
	
	@Override
	public Object clone()
	{
		return new JVector(i, j, k);
	}

	/**
	 * 
	 * @return i + j + k;
	 */
	public double componentSum() { return i + j + k; }
	
	/**
	 * 
	 * @return Math.abs(i) + Math.abs(j) + Math.abs(k);
	 */
	public double absComponentSum() { return Math.abs(i) + Math.abs(j) + Math.abs(k); }
	/**
	 * Override for the toString() method.  This method outputs
	 * the coordinates in tab-delimited format.
	 */
	@Override
	public String toString() { return i + "," + j + "," + k; }

	public String toString(int numDecimals) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(numDecimals);
		nf.setMinimumFractionDigits(numDecimals);
		
		return nf.format(i) + "\t" + nf.format(j) + "\t" + nf.format(k); 
	}
	public String toStringSpace(int numDecimals) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(numDecimals);
		nf.setMinimumFractionDigits(numDecimals);
		
		return nf.format(i) + " " + nf.format(j) + " " + nf.format(k); 
	}
	
	/**
	 * 
	 * @return: return i + "\t" + j + "\t" + k;
	 */
	public String toTabString() {
		return i + "\t" + j + "\t" + k;
	}
	@Override
	public int compareTo(JVector o) {
		// -1 less than
		// 0 equal to
		// 1 greater than
		double olen = o.length();
		double len = length();
		if(olen > len)
			return 1;
		if(olen < len)
			return -1;

		return 0;
	}
}

