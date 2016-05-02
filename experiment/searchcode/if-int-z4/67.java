/*
 *  _____ ____  ____  _     ____  ____ ____ftw.
 * /  __//  __\/  _ \/ \ /\/  __\/  _ \\__  \
 * | |  _|  \/|| / \|| | |||  \/|| / \|  /  |
 * | |_//|    /| \_/|| \_/||  __/| \_/| _\  |
 * \____\\_/\_\\____/\____/\_/   \____//____/
 */
package org.shapes.squarecorner;

import javax.vecmath.Vector3f;

import org.gm.game.Game;
import org.shapes.Hook;
import org.shapes.Shape;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

public class SquareCorner extends Shape {

	public interface SquareCornerHook extends Hook {
		public void setSize(double s);
	}

	private double size = Game.tunnelSize;
	private int res = 48;

	public double getSize() {
		return size;
	}

	/**
	 * Set the size of the cube and propagate this to the hooks
	 *
	 * @param s
	 */
	public void setSize(double s) {
		size = s;
		for (Hook hook : hooks) {
			if (hook instanceof SquareCornerHook) {
				SquareCornerHook cubeHook = (SquareCornerHook) hook;
				cubeHook.setSize(s);
			}
		}

	}

	public int getRes() {
		return res;
	}

	private class Collision {

		SquareCorner squarecorner;

		ObjectArrayList<Vector3f> tubeCollisionPoints = new ObjectArrayList<Vector3f>();

		ConvexHullShape colTubeFloor = new ConvexHullShape(tubeCollisionPoints);
		ConvexHullShape colTubeLeft = new ConvexHullShape(tubeCollisionPoints);
		ConvexHullShape colTubeRight = new ConvexHullShape(tubeCollisionPoints);

		public Collision(SquareCorner s) {
			squarecorner = s;
		}

		public CollisionShape create() {

			int res = squarecorner.getRes();
			float y1, z1, y2, z2, y3, z3, y4, z4;
			double hoek = 0;
			float shift = (float) size / 2;
			double hoekbocht = 90;

			double outerRadius = size * (2.0 / 3.0);
			double innerRadius = size * (1.0 / 3.0);

			float hbottom = (float) innerRadius - shift; // trucje =)
			float htop = (float) outerRadius - shift; // trucje =)

			CompoundShape compound = new CompoundShape();
			Transform transform = new Transform();
			transform.setIdentity();

			for (int i = 0; i < res; i++) {

				y1 = (float) (Math.sin(Math.toRadians(hoek)) * outerRadius) - shift;
				z1 = (float) (Math.cos(Math.toRadians(hoek)) * outerRadius) - shift;

				y3 = (float) (Math.sin(Math.toRadians(hoek)) * innerRadius) - shift;
				z3 = (float) (Math.cos(Math.toRadians(hoek)) * innerRadius) - shift;

				// Calculate next point
				hoek += hoekbocht / res;

				y2 = (float) (Math.sin(Math.toRadians(hoek)) * outerRadius) - shift;
				z2 = (float) (Math.cos(Math.toRadians(hoek)) * outerRadius) - shift;

				y4 = (float) (Math.sin(Math.toRadians(hoek)) * innerRadius) - shift;
				z4 = (float) (Math.cos(Math.toRadians(hoek)) * innerRadius) - shift;

				// Ceiling
				ConvexHullShape colTubeCeiling = new ConvexHullShape(
						tubeCollisionPoints);
				colTubeCeiling.addPoint(new Vector3f(hbottom, y1, z1));
				colTubeCeiling.addPoint(new Vector3f(hbottom, y2, z2));
				colTubeCeiling.addPoint(new Vector3f(htop, y2, z2));
				colTubeCeiling.addPoint(new Vector3f(htop, y1, z1));
				compound.addChildShape(transform, colTubeCeiling);

				// Floor
				colTubeFloor.addPoint(new Vector3f(hbottom, y4, z4));
				colTubeFloor.addPoint(new Vector3f(hbottom, y3, z3));
				colTubeFloor.addPoint(new Vector3f(htop, y3, z3));
				colTubeFloor.addPoint(new Vector3f(htop, y4, z4));

				// Left Side
				colTubeLeft.addPoint(new Vector3f(hbottom, y1, z1));
				colTubeLeft.addPoint(new Vector3f(hbottom, y2, z2));
				colTubeLeft.addPoint(new Vector3f(hbottom, y3, z3));
				colTubeLeft.addPoint(new Vector3f(hbottom, y4, z4));

				// Right Side
				colTubeRight.addPoint(new Vector3f(htop, y1, z1));
				colTubeRight.addPoint(new Vector3f(htop, y2, z2));
				colTubeRight.addPoint(new Vector3f(htop, y3, z3));
				colTubeRight.addPoint(new Vector3f(htop, y4, z4));

			}

			compound.addChildShape(transform, colTubeFloor);
			compound.addChildShape(transform, colTubeLeft);
			compound.addChildShape(transform, colTubeRight);

			return compound;

		}

	}

	@Override
	public CollisionShape createCollisionShape() {
		// TODO lame..
		return new Collision(this).create();
	}

}

