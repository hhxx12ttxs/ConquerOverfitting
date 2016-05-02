/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geojme.geodetic;

import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.jme3.math.Vector3f;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author Vemund
 *			Direction of x can be changed by a boolean flag during
 *			conversions. The motive is that the x value returned by
 *			GeoTools is meant for a 2D screen, and the x value used by
 *			3D space is an additive inverse of this value.
 */
public class SpatialClassConverter {

	public static Vector3f CoordinateToVector3f(Coordinate in, boolean swapYZ,
			boolean swapXDir) {
		Vector3f out = new Vector3f();
		return CoordinateToVector3f(in, out, swapYZ, swapXDir);
	}

	public static Vector3f CoordinateToVector3f(Coordinate in, Vector3f out,
			boolean swapYZ, boolean swapXDir) {
		out.x = (float) in.x;

		// 3D.x = -2D.x
		if (swapXDir)
			out.x *= -1;

		if (swapYZ) {
			float prevY = (float) in.y;
			out.y = (float) in.z;
			out.z = prevY;
		} else {
			out.y = (float) in.y;
			out.z = (float) in.z;
		}
		return out;
	}

	public static Vector3f PointToVector3f(Point in, boolean swapYZ,
			boolean swapXDir) throws FactoryException,
			MismatchedDimensionException, TransformException {
		Vector3f out;
		if (swapYZ) {
			out = CoordinateToVector3f(in.getCoordinate(), true, swapXDir);
		} else {
			out = CoordinateToVector3f(in.getCoordinate(), false, swapXDir);
		}
		return out;
	}

	public static Coordinate Vector3fToCoordinate(Vector3f in, boolean swapYZ,
			boolean swapXDir) {

		Coordinate out = new Coordinate((double) in.x, (double) in.y,
				(double) in.z);

		// 2D.x = -3D.x
		if (swapXDir)
			out.x *= -1;

		if (swapYZ) {
			double prevY = out.y;
			out.y = out.z;
			out.z = prevY;
		}
		return out;
	}
}

