package mcmods.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Entity;
import net.minecraft.src.EntitySubWorld;
import net.minecraft.src.MathHelper;
import net.minecraft.src.ModLoader;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.Vec3;

public class CoordsUtil {
	public static int triangles[][] = new int[][] { { 0, 1, 2 }, { 2, 3, 0 }, { 4, 5, 6 },
			{ 6, 7, 4 }, { 1, 5, 6 }, { 6, 2, 1 }, { 0, 4, 7 }, { 7, 3, 0 }, { 3, 2, 6 },
			{ 6, 7, 3 }, { 0, 1, 5 }, { 5, 4, 0 } };

	private static double dirs[][] = new double[][] { { -1, 0, 0 }, { 1, 0, 0 }, { 0, -1, 0 },
			{ 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 } };

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
	public static Vector3d localPosToParent(Vector3d localPos, Vector3d pos, Vector3d rot) {
		Matrix4d m = new Matrix4d();
		m.setTranslation(pos);
		rot.y = -rot.y; // FIXME This is to compensate rotation -angle in
						// RenderVehicle
		rotate(m, rot);
		Vector3d result = new Vector3d(localPos);
		m.transform(result);

		// Revert side effects
		rot.y = -rot.y;

		return result;

	}

	public static Vector3f localVelToParent(Vector3f localVel, Vector3d rot) {
		Matrix4d m = new Matrix4d();
		rot.y = -rot.y; // FIXME This is to compensate rotation -angle in
						// RenderVehicle
		rotate(m, rot);
		Vector3f result = new Vector3f(localVel);
		m.transform(result);
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
	public static javax.vecmath.Vector3d parentPosToLocal(javax.vecmath.Vector3d parentPos,
			javax.vecmath.Vector3d pos, javax.vecmath.Vector3d rot) {
		Matrix4d m = new Matrix4d();
		rot.y = -rot.y; // FIXME This is to compensate rotation -angle in
						// RenderVehicle

		rotateInverse(m, rot);
		Vector3d negPos = new Vector3d();
		negPos.negate(pos);
		m.setTranslation(negPos);

		Vector3d result = new Vector3d();
		m.transform(parentPos, result);

		// Revert side effects
		rot.y = -rot.y;
		// pos.x -= 1F;
		// pos.z -= 1F;

		return result;

	}

	public static Vector3f parentVelToLocal(Vector3f parentVel, Vector3d rot) {
		Matrix4d m = new Matrix4d();
		rot.y = -rot.y; // FIXME This is to compensate rotation -angle in
						// RenderVehicle

		rotateInverse(m, rot);

		Vector3f result = new Vector3f(parentVel);
		m.transform(result);
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

	public static void rotate(Matrix4d m, Vector3d rot) {
		// m.rotate(rot.y, new Vector3d(0F, 1F, 0F));
		// m.rotate(rot.z, new Vector3d(0F, 0F, 1F));
		// m.rotate(rot.x, new Vector3d(1F, 0F, 0F));
		m.rotY(rot.y);
		m.rotZ(rot.z);
		m.rotX(rot.x);
	}

	public static void rotateInverse(javax.vecmath.Matrix4d m, Vector3d rot) {
		// m.rotate(-rot.x, new Vector3d(1F, 0F, 0F));
		// m.rotate(-rot.z, new Vector3d(0F, 0F, 1F));
		// m.rotate(-rot.y, new Vector3d(0F, 1F, 0F));
		m.rotX(-rot.x);
		m.rotZ(-rot.x);
		m.rotY(-rot.x);

	}

	public static Vector3f getEulerAnglesFromMatrix(Matrix4f m) {
		Vector3f result = new Vector3f((float) Math.atan2(m.m21, m.m11), -(float) Math.atan2(m.m02,
				m.m00), -(float) Math.asin(-m.m01));
		return result;
	}

	public static Vector3d getPosVector3d(Entity entity) {
		return new Vector3d((float) entity.posX, (float) entity.posY, (float) entity.posZ);
	}

	public static Vector3f getVelVector3f(Entity entity) {
		return new Vector3f((float) entity.motionX, (float) entity.motionY, (float) entity.motionZ);
	}

	public static Vector3d getVelVector3d(Entity entity) {
		return new Vector3d(entity.motionX, entity.motionY, entity.motionZ);
	}

	public static Vector3d getRotVector3d(Entity entity) {
		return new Vector3d(0F, (float) Math.toRadians(entity.rotationYaw),
				(float) Math.toRadians(entity.rotationPitch));
	}

	public static Vector3d getLastTickPosVector3d(Entity vehicle) {
		return new Vector3d((float) vehicle.lastTickPosX, (float) vehicle.lastTickPosY,
				(float) vehicle.lastTickPosZ);
	}

	public static Vector3f getLastTickPosVector3f(Entity parentEntity) {
		return new Vector3f((float) parentEntity.lastTickPosX, (float) parentEntity.lastTickPosY,
				(float) parentEntity.lastTickPosZ);
	}

	public static double returnOrigIfDifferenceTooLow(double orig, double other) {
		if (Math.abs(orig - other) < 1E-4) {
			return orig;
		} else {
			return other;
		}
	}

	public static Vec3 collideWithBlock(AxisAlignedBB entityBB, Vec3 entityV, AxisAlignedBB bb) {

		double dist[] = new double[6];
		dist[0] = entityBB.maxX - bb.minX;
		dist[1] = bb.maxX - entityBB.minX;
		dist[2] = entityBB.maxY - bb.minY;
		dist[3] = bb.maxY - entityBB.minY;
		dist[4] = entityBB.maxZ - bb.minZ;
		dist[5] = bb.maxZ - entityBB.minZ;

		if (Math.abs(entityV.yCoord) > 0.4) {
			System.out.printf("%.2f, %.2f, %.2f, %.2f, %.2f, %.2f\n", dist[0], dist[1], dist[2],
					dist[3], dist[4], dist[5]);
		}

		// if (Math.abs(entityV.yCoord) > Math.abs(entityV.zCoord) &&
		// Math.abs(entityV.yCoord) > Math.abs(entityV.xCoord)) {
		// if (entityV.yCoord < 0) {
		// return Vec3.createVectorHelper(0, dist[2], 0);
		// } else {
		// return Vec3.createVectorHelper(0, -dist[3], 0);
		// }
		// }

		// if (entityV.yCoord <= -0.95) {
		// return Vec3.createVectorHelper(0, dist[2], 0);
		// }
		//
		// if (entityV.yCoord >= 0.95) {
		// return Vec3.createVectorHelper(0, -dist[3], 0);
		// }

		if (entityV.yCoord < 0 && dist[3] >= 1.0) {
			return Vec3.createVectorHelper(0, dist[2], 0);
		}

		if (entityV.yCoord > 0 && dist[2] >= 1.0) {
			return Vec3.createVectorHelper(0, -dist[3], 0);
		}

		double minDist = Double.MAX_VALUE;
		int minPos = 0;
		for (int i = 0; i < dist.length; i++) {
			if (dist[i] < minDist && dist[i] > 0D) {
				minDist = dist[i];
				minPos = i;
			}
		}

		return Vec3.createVectorHelper(dist[minPos] * dirs[minPos][0], dist[minPos]
				* dirs[minPos][1], dist[minPos] * dirs[minPos][2]);

	}

	public static double assignIfDifferenceIsGreaterThan(double minDiff, double newValue,
			double oldValue) {
		return assignIfDifferenceIsGreaterThan(minDiff, newValue - oldValue, newValue, oldValue);
	}

	public static double assignIfDifferenceIsGreaterThan(double minDiff, double diff,
			double newValue, double oldValue) {
		if (Math.abs(diff) > minDiff) {
			return newValue;
		} else {
			return oldValue;
		}
	}

	public static boolean equals(Vector3f v1, Vector3f v2) {
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
				return v1.x == v2.x && v1.y == v2.y && v1.z == v2.z;
			}
		}
	}

	public static boolean equals(Vec3 v1, Vec3 v2) {
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

	// public static MovingObjectPosition rayTrace(Emob emob, Entity entity,
	// float dist) {
	// EmobWorld emobWorld = emob.emobWorld;
	// Vector3d pos = CoordsUtil.getPosVector3d(entity);
	// Vector3d look = CoordsUtil.getLook(entity);
	// Vector3d posLook = new Vector3d();
	// posLook.scaleAdd(dist, look, pos);
	// pos = emob.convertToPosInLocalCoords(pos);
	// posLook = emob.convertToPosInLocalCoords(posLook);
	// return emobWorld.rayTraceBlocks(toVec3(pos), toVec3(posLook));
	// }

	public static Vec3 getPos(Entity entity) {
		Vec3 pos = Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
		return pos;
	}

	public static Vec3 toVec3(Vector3f vec) {
		return Vec3.createVectorHelper(vec.x, vec.y, vec.z);
	}

	public static Vec3 toVec3(Vector3d vec) {
		return Vec3.createVectorHelper(vec.x, vec.y, vec.z);
	}

	public static Vector3d getLook(Entity entity) {
		float cosYaw = (float) Math.cos(-entity.rotationYaw * 0.01745329F - Math.PI);
		float sinYaw = (float) Math.sin(-entity.rotationYaw * 0.01745329F - Math.PI);
		float cosPitch = (float) -Math.cos(-entity.rotationPitch * 0.01745329F);
		float sinPitch = (float) Math.sin(-entity.rotationPitch * 0.01745329F);
		Vector3d look = new Vector3d(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
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

	public static Vector3d rotateVector(Vector3d vec, double angleInDegrees) {
		double angle = angleInDegrees * Math.PI / 180D;
		double cosa = Math.cos(angle);
		double sina = Math.sin(angle);
		return new Vector3d(vec.x * cosa - vec.z * sina, vec.y,
				vec.z * cosa + vec.x * sina);
	}

	public static Vec3 rotateVector2(Vec3 vectorInLocalCoordinates, double rotationAngle) {
		double r = Math.sqrt(vectorInLocalCoordinates.xCoord * vectorInLocalCoordinates.xCoord
				+ vectorInLocalCoordinates.zCoord * vectorInLocalCoordinates.zCoord);
		double angle = Math.atan2(vectorInLocalCoordinates.zCoord, vectorInLocalCoordinates.xCoord)
				+ (rotationAngle * Math.PI / 180D);
		// if (angle < 0D) {
		// angle += Math.PI * 2;
		// } else if (angle >= 360D) {
		// angle -= Math.PI *2;
		// }
		Vec3 rotatedVector = Vec3.createVectorHelper(r * Math.cos(angle),
				vectorInLocalCoordinates.yCoord, r * Math.sin(angle));
		return rotatedVector;
	}

	public static double invertedHyperbole(double x) {
		return -1 / ((x + 1)) + 1;
	}

	public static Vec3 getPrevPos(Entity entity) {
		Vec3 pos = Vec3.createVectorHelper(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
		return pos;
	}

	public static Vec3 getMotion(Entity entity) {
		return Vec3.createVectorHelper(entity.motionX, entity.motionY, entity.motionZ);
	}

	public static final Vec3 UNIT_VECTOR_Z = Vec3.createVectorHelper(0, 0, 1);

	// public static int getSideFacingPlayer(EntityPlayer par2EntityPlayer) {
	// return MathHelper.floor_double((double) (par2EntityPlayer.rotationYaw *
	// 4.0F / 360.0F) + 0.5D) & 3;
	//
	// }

	// public static Vector3d[] getEmobPointsInParentCoords(Emob emob) {
	// Vector3d c0 = emob.emobWorld.getLowerBackLeftCorner();
	// Vector3d c6 = emob.emobWorld.getUpperFrontRightCorner(c0);
	//
	// Vector3d c0Parent = emob.convertToPosInParentCoords(c0);
	// Vector3d c6Parent = emob.convertToPosInParentCoords(c6);
	// Vector3d[] points = CoordsUtil.getBBPoints(new double[] { c0Parent.x,
	// c0Parent.y, c0Parent.z },
	// new double[] { c6Parent.x, c6Parent.y, c6Parent.z });
	//
	// return points;
	// }

	public static javax.vecmath.Vector3d[] getBBPoints(double[] corner0, double[] corner1) {
		javax.vecmath.Vector3d points[] = new javax.vecmath.Vector3d[8];

		points[0] = new javax.vecmath.Vector3d((float) corner0[0], (float) corner0[1],
				(float) corner0[2]);
		points[6] = new javax.vecmath.Vector3d((float) corner1[0], (float) corner1[1],
				(float) corner1[2]);

		points[1] = new javax.vecmath.Vector3d(points[6].x, points[0].y, points[0].z);
		points[2] = new javax.vecmath.Vector3d(points[6].x, points[6].y, points[0].z);
		points[3] = new javax.vecmath.Vector3d(points[0].x, points[6].y, points[0].z);
		points[4] = new javax.vecmath.Vector3d(points[0].x, points[0].y, points[6].z);
		points[5] = new javax.vecmath.Vector3d(points[6].x, points[0].y, points[6].z);
		points[7] = new javax.vecmath.Vector3d(points[0].x, points[6].y, points[6].z);
		return points;
	}

	public static void add(Vec3 vec1, Vec3 vec2) {
		vec1.xCoord += vec2.xCoord;
		vec1.yCoord += vec2.yCoord;
		vec1.zCoord += vec2.zCoord;
	}

	public static void multiply(Vec3 vec1, double num) {
		vec1.xCoord *= num;
		vec1.yCoord *= num;
		vec1.zCoord *= num;
	}

	public static Vector3d calculateFromVector(Entity entity, float ticks) {
		float x = (float) (entity.prevPosX + (entity.posX - entity.prevPosX) * (double) ticks);
		float y = (float) ((entity.prevPosY + (entity.posY - entity.prevPosY) * (double) ticks + 1.6200000000000001D) - (double) entity.yOffset);
		float z = (float) (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) ticks);
		Vector3d from = new Vector3d(x, y, z);
		return from;
	}

	public static Vector3d calculateToVector(Entity entity, float ticks, Vector3d from,
			float maxDist) {
		float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch)
				* ticks;
		float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * ticks;
		float f3 = MathHelper.cos(-yaw * 0.01745329F - 3.141593F);
		float f4 = MathHelper.sin(-yaw * 0.01745329F - 3.141593F);
		float f5 = -MathHelper.cos(-pitch * 0.01745329F);
		float f6 = MathHelper.sin(-pitch * 0.01745329F);
		float dx = f4 * f5;
		float dy = f6;
		float dz = f3 * f5;
		// float dist = 5F + new Vector3d(getHeightBlocks(), getWidthBlocks(),
		// getLengthBlocks()).length();

		Vector3d to = new Vector3d(from.x + dx * maxDist, from.y + dy * maxDist, from.z + dz
				* maxDist);
		return to;
	}

	public static MovingObjectPosition getMouseOverExcludingEntity(float ticks,
			Entity excludedEntity) {
		excludedEntities.clear();
		excludedEntities.add(excludedEntity);
		return getMouseOverExcludingSet(ticks, excludedEntities);

	}

	public static MovingObjectPosition getMouseOverExcludingSet(float ticks,
			Set<Entity> excludedEntities) {
		Minecraft mc = ModLoader.getMinecraftInstance();
		MovingObjectPosition objectMouseOver = null;
		if (mc.renderViewEntity != null) {
			if (mc.theWorld != null) {
				double reach = (double) mc.playerController.getBlockReachDistance();
				objectMouseOver = mc.renderViewEntity.rayTrace(reach, ticks);
				double newReach = reach;
				Vec3 from = mc.renderViewEntity.getPosition(ticks);

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

				Vec3 look = mc.renderViewEntity.getLook(ticks);
				Vec3 to = from.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord
						* reach);
				Entity pointedEntity = null;
				float var9 = 1.0F;
				List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
						mc.renderViewEntity,
						mc.renderViewEntity.boundingBox.addCoord(look.xCoord * reach,
								look.yCoord * reach, look.zCoord * reach).expand((double) var9,
								(double) var9, (double) var9));
				double postNewReach = newReach;

				for (int i = 0; i < entities.size(); ++i) {
					Entity entity = (Entity) entities.get(i);

					if (entity.canBeCollidedWith()
							&& (excludedEntities == null || !excludedEntities.contains(entity))) {
						float border = entity.getCollisionBorderSize();
						AxisAlignedBB bb = entity.boundingBox.expand((double) border,
								(double) border, (double) border);
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

	public static Vector3d convertToPosInLocalCoords(EntitySubWorld entity,
			Vector3d posInParentCoords) {
		Vector3d posInLocalCoords = CoordsUtil.parentPosToLocal(posInParentCoords,
				CoordsUtil.getPosVector3d(entity), CoordsUtil.getRotVector3d(entity));
		return posInLocalCoords;
	}

//	public static Vec3 convertToPosInLocalCoords(EntitySubWorld entity, Vec3 posInParentCoords) {
//		Vec3 emobPos = Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
//
//		Vec3 difference = Vec3.createVectorHelper(posInParentCoords.xCoord - emobPos.xCoord,
//				posInParentCoords.yCoord - emobPos.yCoord, posInParentCoords.zCoord
//						- emobPos.zCoord);
//		Vec3 localPos = CoordsUtil.rotateVector(difference, -entity.rotationYaw);
//		return localPos;
//	}

	public static Vector3i convertToPosInLocalCoords(EntitySubWorld entity,
			Vector3i posInParentCoords) {
		Vector3d pos = convertToPosInLocalCoords(entity, new Vector3d(posInParentCoords.x,
				posInParentCoords.y, posInParentCoords.z));
		return new Vector3i((int) Math.floor(pos.x), (int) Math.floor(pos.y),
				(int) Math.floor(pos.z));
	}

	public static Vector3d convertToPosInParentCoords(EntitySubWorld entity,
			Vector3d posInLocalCoordinates) {
		return CoordsUtil.localPosToParent(posInLocalCoordinates,
				CoordsUtil.getPosVector3d(entity), CoordsUtil.getRotVector3d(entity));
	}

	public static Vector3i convertToPosInParentCoords(EntitySubWorld entity,
			Vector3i posInLocalCoordinates) {
		Vector3d pos = convertToPosInParentCoords(entity, Vec3.createVectorHelper(
				posInLocalCoordinates.x, posInLocalCoordinates.y, posInLocalCoordinates.z));
		return new Vector3i((int) Math.floor(pos.x), (int) Math.floor(pos.y),
				(int) Math.floor(pos.z));
	}

	public static Vector3d convertToPosInParentCoords(EntitySubWorld entity,
			Vec3 posInLocalCoordinates) {
		return convertToPosInParentCoords(entity, new Vector3d(
				(float) posInLocalCoordinates.xCoord, (float) posInLocalCoordinates.yCoord,
				(float) posInLocalCoordinates.zCoord));
		// Vec3 rotatedVector = CoordsUtil.rotateVector(posInLocalCoordinates,
		// rotationYaw);
		// return rotatedVector.addVector(posX, posY, posZ);
	}

	public static AxisAlignedBB convertBBToParentCoords(EntitySubWorld entity,
			AxisAlignedBB bbInLocalCoords) {
		Vector3d posInLocalCoords = getBBMiddle(entity, bbInLocalCoords);
		Vector3d posInParentCoords = convertToPosInParentCoords(entity, posInLocalCoords);
		return moveBBTo(entity, bbInLocalCoords, posInParentCoords);
	}

	public static AxisAlignedBB convertBBToLocalCoords(EntitySubWorld entity,
			AxisAlignedBB bbInParentCoords) {
		Vector3d posInParentCoords = getBBMiddle(entity, bbInParentCoords);
		Vector3d posInLocalCoords = convertToPosInLocalCoords(entity, posInParentCoords);
		return moveBBTo(entity, bbInParentCoords, posInLocalCoords);
	}

	public static Vector3d getBBMiddle(EntitySubWorld entity, AxisAlignedBB bb) {
		return new Vector3d((float) (bb.maxX + bb.minX) / 2f, (float) (bb.maxY + bb.minY) / 2f,
				(float) (bb.maxZ + bb.minZ) / 2f);
	}

	public static AxisAlignedBB moveBBTo(EntitySubWorld entity, AxisAlignedBB bb, Vector3d pos) {
		double halfLength = (bb.maxX - bb.minX) / 2D;
		double halfHeight = (bb.maxY - bb.minY) / 2D;
		double halfWidth = (bb.maxZ - bb.minZ) / 2D;
		return AxisAlignedBB.getBoundingBox(pos.x - halfLength, pos.y - halfHeight, pos.z
				- halfWidth, pos.x + halfLength, pos.y + halfHeight, pos.z + halfWidth);
	}

}

