package sirentropy.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Emob;
import net.minecraft.src.EmobWorld;
import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.ModLoader;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.Vec3D;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.jme3.math.Ray;
import com.jme3.math.Triangle;

public class CoordsUtil {
	public static int triangles[][] = new int[][] { { 0, 1, 2 }, { 2, 3, 0 }, { 4, 5, 6 }, { 6, 7, 4 }, { 1, 5, 6 }, { 6, 2, 1 },
			{ 0, 4, 7 }, { 7, 3, 0 }, { 3, 2, 6 }, { 6, 7, 3 }, { 0, 1, 5 }, { 5, 4, 0 } };

	private static double dirs[][] = new double[][] { { -1, 0, 0 }, { 1, 0, 0 }, { 0, -1, 0 }, { 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 } };

	private static Set<Entity> excludedEntities = new HashSet<Entity>();
	/**
	 * @param localPos
	 *            The coordinates of the object in local coordinates
	 * @param pos
	 *            The pos of the origin of the local coord system in global
	 *            coordinates
	 * @param rot
	 *            The rotation of the local coord system in global coordinates
	 *            (roll, yaw, pitch)
	 * @return
	 */
	public static Vector4f localPosToParent(Vector4f localPos, Vector3f pos, Vector3f rot) {
		Matrix4f m = new Matrix4f();
		m.translate(pos);
		rot.y = -rot.y; // FIXME This is to compensate rotation -angle in
						// RenderVehicle
		rotate(m, rot);
		Vector4f result = new Vector4f();
		Matrix4f.transform(m, localPos, result);

		// Revert side effects
		rot.y = -rot.y;

		return result;

	}

	public static Vector4f localVelToParent(Vector4f localVel, Vector3f rot) {
		Matrix4f m = new Matrix4f();
		rot.y = -rot.y; // FIXME This is to compensate rotation -angle in
						// RenderVehicle
		rotate(m, rot);
		Vector4f result = new Vector4f();
		Matrix4f.transform(m, localVel, result);
		// Revert side effects
		rot.y = -rot.y;

		return result;
	}

	/**
	 * @param parentPos
	 *            Position of the object in parent coordinates
	 * @param pos
	 *            The pos of the origin of the local coord system in global
	 *            coordinates
	 * @param rot
	 *            The rotation of the local coord system in global coordinates
	 *            (roll, yaw, pitch)
	 * @return
	 */
	public static Vector4f parentPosToLocal(Vector4f parentPos, Vector3f pos, Vector3f rot) {
		Matrix4f m = new Matrix4f();
		rot.y = -rot.y; // FIXME This is to compensate rotation -angle in
						// RenderVehicle

		rotateInverse(m, rot);
		m.translate(pos.negate(pos));

		Vector4f result = new Vector4f();
		Matrix4f.transform(m, parentPos, result);
		// Revert side effects
		rot.y = -rot.y;
		// pos.x -= 1F;
		// pos.z -= 1F;

		return result;

	}

	public static Vector4f parentVelToLocal(Vector4f parentVel, Vector3f rot) {
		Matrix4f m = new Matrix4f();
		rot.y = -rot.y; // FIXME This is to compensate rotation -angle in
						// RenderVehicle

		rotateInverse(m, rot);

		Vector4f result = new Vector4f();
		Matrix4f.transform(m, parentVel, result);
		// Revert side effects
		rot.y = -rot.y;

		return result;
	}

	// /**
	// * roll = atan2(m21, m11)
	// * yaw = atan2(m02, m00)
	// * pitch = asin(-m01)
	// * @param localRot Rotation in local coordinates
	// * @param rot Rotation of the local coordinate system in global
	// coordinates
	// * @return
	// */
	// public static Vector3f localRotToParent(Vector3f localRot, Vector3f rot)
	// {
	// Matrix4f m = new Matrix4f();
	// System.out.println("Rot: " + rot);
	// rotate(m, rot);
	// System.out.println("LocalRot: " + localRot);
	// rotate(m, localRot);
	//
	// Vector3f result = getEulerAnglesFromMatrix(m);
	//
	// return result;
	//
	// }
	//
	//
	//
	//
	// public static Vector3f parentRotToLocal(Vector3f parentRot, Vector3f rot)
	// {
	// Matrix4f m = new Matrix4f();
	// rotate(m, parentRot);
	// rotateInverse(m, rot);
	//
	// Vector3f result = getEulerAnglesFromMatrix(m);
	//
	// return result;
	//
	// }

	public static void rotate(Matrix4f m, Vector3f rot) {
		m.rotate(rot.y, new Vector3f(0F, 1F, 0F));
		m.rotate(rot.z, new Vector3f(0F, 0F, 1F));
		m.rotate(rot.x, new Vector3f(1F, 0F, 0F));
	}

	public static void rotateInverse(Matrix4f m, Vector3f rot) {
		m.rotate(-rot.x, new Vector3f(1F, 0F, 0F));
		m.rotate(-rot.z, new Vector3f(0F, 0F, 1F));
		m.rotate(-rot.y, new Vector3f(0F, 1F, 0F));
	}

	public static Vector3f getEulerAnglesFromMatrix(Matrix4f m) {
		Vector3f result = new Vector3f((float) Math.atan2(m.m21, m.m11), -(float) Math.atan2(m.m02, m.m00), -(float) Math.asin(-m.m01));
		return result;
	}

	public static Vector3f getPosVector3f(Entity entity) {
		return new Vector3f((float) entity.posX, (float) entity.posY, (float) entity.posZ);
	}

	public static Vector4f getPosVector4f(Entity entity) {
		return new Vector4f((float) entity.posX, (float) entity.posY, (float) entity.posZ, 1F);
	}

	public static Vector4f getVelVector4f(Entity entity) {
		return new Vector4f((float) entity.motionX, (float) entity.motionY, (float) entity.motionZ, 1F);
	}

	public static Vector3f getRotVector3f(Entity entity) {
		return new Vector3f(0F, (float) Math.toRadians(entity.rotationYaw), (float) Math.toRadians(entity.rotationPitch));
	}

	public static Vector3f getLastTickPosVector3f(Entity vehicle) {
		return new Vector3f((float) vehicle.lastTickPosX, (float) vehicle.lastTickPosY, (float) vehicle.lastTickPosZ);
	}

	public static Vector4f getLastTickPosVector4f(Entity parentEntity) {
		return new Vector4f((float) parentEntity.lastTickPosX, (float) parentEntity.lastTickPosY, (float) parentEntity.lastTickPosZ, 1F);
	}

	//
	// public static javax.vecmath.Vector3f getSizeVector3f(Entity entity) {
	// return new javax.vecmath.Vector3f((float)entity.width,
	// (float)entity.height, (float)entity.width);
	// }
	//
	// public static javax.vecmath.Vector3f getHalfSizeVector3f(Entity entity) {
	// return new javax.vecmath.Vector3f((float)entity.width/2f,
	// (float)entity.height/2f, (float)entity.width/2f);
	// }
	//

	public static double returnOrigIfDifferenceTooLow(double orig, double other) {
		if (Math.abs(orig - other) < 1E-4) {
			return orig;
		} else {
			return other;
		}
	}

	public static Vec3D collideWithBlock(AxisAlignedBB entityBB, Vec3D entityV, AxisAlignedBB bb) {

		double dist[] = new double[6];
		dist[0] = entityBB.maxX - bb.minX;
		dist[1] = bb.maxX - entityBB.minX;
		dist[2] = entityBB.maxY - bb.minY;
		dist[3] = bb.maxY - entityBB.minY;
		dist[4] = entityBB.maxZ - bb.minZ;
		dist[5] = bb.maxZ - entityBB.minZ;

		if (Math.abs(entityV.yCoord) > 0.4) {
			System.out.printf("%.2f, %.2f, %.2f, %.2f, %.2f, %.2f\n", dist[0], dist[1], dist[2], dist[3], dist[4], dist[5]);
		}

		// if (Math.abs(entityV.yCoord) > Math.abs(entityV.zCoord) &&
		// Math.abs(entityV.yCoord) > Math.abs(entityV.xCoord)) {
		// if (entityV.yCoord < 0) {
		// return Vec3D.createVectorHelper(0, dist[2], 0);
		// } else {
		// return Vec3D.createVectorHelper(0, -dist[3], 0);
		// }
		// }

		// if (entityV.yCoord <= -0.95) {
		// return Vec3D.createVectorHelper(0, dist[2], 0);
		// }
		//
		// if (entityV.yCoord >= 0.95) {
		// return Vec3D.createVectorHelper(0, -dist[3], 0);
		// }

		if (entityV.yCoord < 0 && dist[3] >= 1.0) {
			return Vec3D.createVectorHelper(0, dist[2], 0);
		}

		if (entityV.yCoord > 0 && dist[2] >= 1.0) {
			return Vec3D.createVectorHelper(0, -dist[3], 0);
		}

		double minDist = Double.MAX_VALUE;
		int minPos = 0;
		for (int i = 0; i < dist.length; i++) {
			if (dist[i] < minDist && dist[i] > 0D) {
				minDist = dist[i];
				minPos = i;
			}
		}

		return Vec3D.createVectorHelper(dist[minPos] * dirs[minPos][0], dist[minPos] * dirs[minPos][1], dist[minPos] * dirs[minPos][2]);

	}

	public static double assignIfDifferenceIsGreaterThan(double difference, double newValue, double oldValue) {
		if (Math.abs(newValue - oldValue) > difference) {
			return newValue;
		} else {
			return oldValue;
		}
	}

	public static boolean equals(Vector4f v1, Vector4f v2) {
		if (v1 == null) {
			if (v2 == null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (v2 == null) {
				return false;
			} else {
				return v1.x == v2.x && v1.y == v2.y && v1.z == v2.z && v1.w == v2.w;
			}
		}
	}

	public static boolean equals(Vec3D v1, Vec3D v2) {
		if (v1 == null) {
			if (v2 == null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (v2 == null) {
				return false;
			} else {
				return v1.xCoord == v2.xCoord && v1.yCoord == v2.yCoord && v1.zCoord == v2.zCoord;
			}
		}
	}

	public static MovingObjectPosition rayTrace(Emob emob, Entity entity, double dist) {
		EmobWorld emobWorld = emob.emobWorld;
		Vec3D pos = CoordsUtil.getPos(entity);
		Vec3D look = CoordsUtil.getLook(entity);
		Vec3D posLook = pos.addVector(look.xCoord * dist, look.yCoord * dist, look.zCoord * dist);
		pos = emob.convertToPosInLocalCoords(pos);
		posLook = emob.convertToPosInLocalCoords(posLook);
		return emobWorld.rayTraceBlocks(pos, posLook);
	}

	public static Vec3D getPos(Entity entity) {
		Vec3D pos = Vec3D.createVectorHelper(entity.posX, entity.posY, entity.posZ);
		return pos;
	}

	public static Vec3D getLook(Entity entity) {
		double cosYaw = Math.cos(-entity.rotationYaw * 0.01745329F - Math.PI);
		double sinYaw = Math.sin(-entity.rotationYaw * 0.01745329F - Math.PI);
		double cosPitch = -Math.cos(-entity.rotationPitch * 0.01745329F);
		double sinPitch = Math.sin(-entity.rotationPitch * 0.01745329F);
		Vec3D look = Vec3D.createVectorHelper(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
		return look;
	}

	public static float standardizeAngle(float angle) {
		if (angle < 0) {
			return angle + 360;
		} else if (angle > 360) {
			return angle - 360;
		}
		return angle;
	}

	// public static Transform getTransformFromEntity(Entity entity) {
	// Transform startTransform = new Transform();
	// startTransform.setIdentity();
	// startTransform.origin.set((float)entity.posX, (float)entity.posY,
	// (float)entity.posZ);
	// return startTransform;
	// }

	public static Vec3D rotateVector(Vec3D vec, double angleInDegrees) {
		double angle = angleInDegrees * Math.PI / 180D;
		double cosa = Math.cos(angle);
		double sina = Math.sin(angle);
		return Vec3D.createVectorHelper(vec.xCoord * cosa - vec.zCoord * sina, vec.yCoord, vec.zCoord * cosa + vec.xCoord * sina);
	}

	public static Vec3D rotateVector2(Vec3D vectorInLocalCoordinates, double rotationAngle) {
		double r = Math.sqrt(vectorInLocalCoordinates.xCoord * vectorInLocalCoordinates.xCoord + vectorInLocalCoordinates.zCoord
				* vectorInLocalCoordinates.zCoord);
		double angle = Math.atan2(vectorInLocalCoordinates.zCoord, vectorInLocalCoordinates.xCoord) + (rotationAngle * Math.PI / 180D);
		// if (angle < 0D) {
		// angle += Math.PI * 2;
		// } else if (angle >= 360D) {
		// angle -= Math.PI *2;
		// }
		Vec3D rotatedVector = Vec3D.createVectorHelper(r * Math.cos(angle), vectorInLocalCoordinates.yCoord, r * Math.sin(angle));
		return rotatedVector;
	}

	public static double invertedHyperbole(double x) {
		return -1 / ((x + 1)) + 1;
	}

	public static Vec3D getPrevPos(Entity entity) {
		Vec3D pos = Vec3D.createVectorHelper(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
		return pos;
	}

	public static Vec3D getMotion(Entity entity) {
		return Vec3D.createVectorHelper(entity.motionX, entity.motionY, entity.motionZ);
	}

	public static final Vec3D UNIT_VECTOR_Z = Vec3D.createVectorHelper(0, 0, 1);

	// public static int getSideFacingPlayer(EntityPlayer par2EntityPlayer) {
	// return MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw *
	// 4.0F / 360.0F) + 0.5D) & 3;
	//
	// }

	/**
	 * Calculates offset of bb1 wrt bb2
	 * 
	 * @param bb1Offset
	 * @param bb1Points
	 * @param bb2Points
	 *            Must be calculated using
	 *            {@link #getBBPoints(double[], double[])}
	 * @return true if offset has been changed, false otherwise
	 */
	public static boolean calculateOffset(Vec3D bb1Offset, com.jme3.math.Vector3f[] bb1Points, com.jme3.math.Vector3f[] bb2Points) {
		Triangle[] bb2Triangles = CoordsUtil.getTriangles(bb2Points);
		Vec3D prevOffset = Vec3D.createVectorHelper(bb1Offset.xCoord, bb1Offset.yCoord, bb1Offset.zCoord);
		double minDist = Double.MAX_VALUE;
		com.jme3.math.Vector3f minIntersection = new com.jme3.math.Vector3f();
		com.jme3.math.Vector3f minOrigin = new com.jme3.math.Vector3f();
		Triangle closestTriangle;

		Ray r = new Ray();
		com.jme3.math.Vector3f origin = new com.jme3.math.Vector3f();
		com.jme3.math.Vector3f direction = new com.jme3.math.Vector3f((float) -bb1Offset.xCoord, (float) -bb1Offset.yCoord,
				(float) -bb1Offset.zCoord);
		com.jme3.math.Vector3f intersection = new com.jme3.math.Vector3f();
		r.setDirection(direction);
		// Finds the closest point of bb1 to bb2
		for (int i = 0; i < bb1Points.length; i++) {
			// Casts a ray to intersect with all of the triangles of bb2
			origin.set(bb1Points[i]);
			r.setOrigin(origin);
			for (int j = 0; j < bb2Triangles.length; j++) {
				if (r.intersectWhere(bb2Triangles[j], intersection)) {
					float d = origin.distanceSquared(intersection);
					if (d < minDist) {
						minDist = d;
						minOrigin.set(origin);
						minIntersection.set(intersection);
						closestTriangle = bb2Triangles[j];
					}
				}
			}
		}
		if (minDist != Double.MAX_VALUE) {
			if (minDist < bb1Offset.xCoord * bb1Offset.xCoord + bb1Offset.yCoord * bb1Offset.yCoord + bb1Offset.zCoord * bb1Offset.zCoord) {
				double adjustment = -1e-3;
				bb1Offset.xCoord = (minOrigin.x - minIntersection.x);
				bb1Offset.yCoord = (minOrigin.y - minIntersection.y);
				bb1Offset.zCoord = (minOrigin.z - minIntersection.z);
				if (prevOffset.xCoord != bb1Offset.xCoord) {
					bb1Offset.xCoord += Math.signum(bb1Offset.xCoord) * adjustment;
				}
				if (prevOffset.yCoord != bb1Offset.yCoord) {
					bb1Offset.yCoord += Math.signum(bb1Offset.yCoord) * adjustment;
				}
				if (prevOffset.zCoord != bb1Offset.zCoord) {
					bb1Offset.zCoord += Math.signum(bb1Offset.zCoord) * adjustment;
				}
				return true;
			}
		}
		return false;
	}

	public static com.jme3.math.Vector3f[] getEmobPointsInParentCoords(Emob emob) {
		Vec3D c0 = emob.emobWorld.emob.emobWorld.getLowerBackLeftCorner();
		Vec3D c6 = emob.emobWorld.emob.emobWorld.getUpperFrontRightCorner(c0);

		Vec3D c0Parent = emob.convertToPosInParentCoords(c0);
		Vec3D c6Parent = emob.convertToPosInParentCoords(c6);
		com.jme3.math.Vector3f[] points = CoordsUtil.getBBPoints(new double[] { c0Parent.xCoord, c0Parent.yCoord, c0Parent.zCoord },
				new double[] { c6Parent.xCoord, c6Parent.yCoord, c6Parent.zCoord });

		return points;
	}

	public static com.jme3.math.Vector3f[] getBBPoints(double[] corner0, double[] corner1) {
		com.jme3.math.Vector3f points[] = new com.jme3.math.Vector3f[8];

		points[0] = new com.jme3.math.Vector3f((float) corner0[0], (float) corner0[1], (float) corner0[2]);
		points[6] = new com.jme3.math.Vector3f((float) corner1[0], (float) corner1[1], (float) corner1[2]);

		points[1] = new com.jme3.math.Vector3f(points[6].x, points[0].y, points[0].z);
		points[2] = new com.jme3.math.Vector3f(points[6].x, points[6].y, points[0].z);
		points[3] = new com.jme3.math.Vector3f(points[0].x, points[6].y, points[0].z);
		points[4] = new com.jme3.math.Vector3f(points[0].x, points[0].y, points[6].z);
		points[5] = new com.jme3.math.Vector3f(points[6].x, points[0].y, points[6].z);
		points[7] = new com.jme3.math.Vector3f(points[0].x, points[6].y, points[6].z);
		return points;
	}

	/**
	 * Get the triangles of a cube represented by entityPoints.
	 * 
	 * @param entityPoints
	 *            must be obtained using
	 *            {@link #getBBPoints(double[], double[])}
	 * @return
	 */
	private static Triangle[] getTriangles(com.jme3.math.Vector3f[] entityPoints) {
		Triangle[] t = new Triangle[12];
		// Vector3f[] points = getAllPointsInParentCoords(emob);
		// Iterates over triangles
		for (int i = 0; i < 12; i++) {
			t[i] = new Triangle();
			// Iterates over points of each triangle
			for (int p = 0; p < 3; p++) {
				com.jme3.math.Vector3f point = entityPoints[triangles[i][p]];
				t[i].set(p, point);
			}
		}
		return t;
	}

	public static void add(Vec3D vec1, Vec3D vec2) {
		vec1.xCoord += vec2.xCoord;
		vec1.yCoord += vec2.yCoord;
		vec1.zCoord += vec2.zCoord;
	}

	public static void multiply(Vec3D vec1, double num) {
		vec1.xCoord *= num;
		vec1.yCoord *= num;
		vec1.zCoord *= num;
	}

	public static Vec3D calculateFromVector(Entity entity, float ticks) {
		double x = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) ticks;
		double y = (entity.prevPosY + (entity.posY - entity.prevPosY) * (double) ticks + 1.6200000000000001D) - (double) entity.yOffset;
		double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) ticks;
		Vec3D from = Vec3D.createVectorHelper(x, y, z);
		return from;
	}

	public static Vec3D calculateToVector(Entity entity, float ticks, Vec3D from, double maxDist) {
		float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * ticks;
		float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * ticks;
		float f3 = MathHelper.cos(-yaw * 0.01745329F - 3.141593F);
		float f4 = MathHelper.sin(-yaw * 0.01745329F - 3.141593F);
		float f5 = -MathHelper.cos(-pitch * 0.01745329F);
		float f6 = MathHelper.sin(-pitch * 0.01745329F);
		float dx = f4 * f5;
		float dy = f6;
		float dz = f3 * f5;
		// float dist = 5F + new Vector3f(getHeightBlocks(), getWidthBlocks(),
		// getLengthBlocks()).length();

		Vec3D to = Vec3D.createVectorHelper(from.xCoord + dx * maxDist, from.yCoord + dy * maxDist, from.zCoord + dz * maxDist);
		return to;
	}

	public static MovingObjectPosition getMouseOverExcludingEntity(float ticks, Entity excludedEntity) {
		excludedEntities.clear();
		excludedEntities.add(excludedEntity);
		return getMouseOverExcludingSet(ticks, excludedEntities);

	}
	public static MovingObjectPosition getMouseOverExcludingSet(float ticks, Set<Entity> excludedEntities) {
		Minecraft mc = ModLoader.getMinecraftInstance();
		MovingObjectPosition objectMouseOver = null;
		if (mc.renderViewEntity != null) {
			if (mc.theWorld != null) {
				double reach = (double) mc.playerController.getBlockReachDistance();
				objectMouseOver = mc.renderViewEntity.rayTrace(reach, ticks);
				double newReach = reach;
				Vec3D from = mc.renderViewEntity.getPosition(ticks);

				if (mc.playerController.extendedReach()) {
					reach = 6.0D;
					newReach = 6.0D;
				} else {
					if (reach > 3.0D) {
						newReach = 3.0D;
					}

					reach = newReach;
				}

				if (objectMouseOver != null) {
					newReach = objectMouseOver.hitVec.distanceTo(from);
				}

				Vec3D look = mc.renderViewEntity.getLook(ticks);
				Vec3D to = from.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
				Entity pointedEntity = null;
				float var9 = 1.0F;
				List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
						mc.renderViewEntity,
						mc.renderViewEntity.boundingBox.addCoord(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach).expand(
								(double) var9, (double) var9, (double) var9));
				double postNewReach = newReach;

				for (int i = 0; i < entities.size(); ++i) {
					Entity entity = (Entity) entities.get(i);

					if (entity.canBeCollidedWith() && (excludedEntities == null  || !excludedEntities.contains(entity))) {
						float border = entity.getCollisionBorderSize();
						AxisAlignedBB bb = entity.boundingBox.expand((double) border, (double) border, (double) border);
						MovingObjectPosition var17 = bb.calculateIntercept(from, to);

						if (bb.isVecInside(from)) {
							if (0.0D < postNewReach || postNewReach == 0.0D) {
								pointedEntity = entity;
								postNewReach = 0.0D;
							}
						} else if (var17 != null) {
							double dist = from.distanceTo(var17.hitVec);

							if (dist < postNewReach || postNewReach == 0.0D) {
								pointedEntity = entity;
								postNewReach = dist;
							}
						}
					}
				}

				if (pointedEntity != null && (postNewReach < newReach || objectMouseOver == null)) {
					objectMouseOver = new MovingObjectPosition(pointedEntity);
				}
			}
		}
		return objectMouseOver;
	}

}

