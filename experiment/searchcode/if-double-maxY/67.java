package mcmods.subworld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.EntitySubWorld;
import net.minecraft.src.Vec3;

public class EmobQuickBB extends EmobBB {

	private int pointsPerChunkSide = 0;

	List<Vector3d> collisionPoints = new ArrayList<Vector3d>();

	private int minPointsPerSide = 0;

	public EmobQuickBB(EntitySubWorld emob, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		super(emob, minX, minY, minZ, maxX, maxY, maxZ);

	}

	private void createCollisionPoints() {
		int pointsPerLength = getPointsPerLength();
		int pointsPerWidth = getPointsPerWidth();
		int totalPoints = 2 * (pointsPerLength + pointsPerWidth) + 8;
		for (int i = 0; i < totalPoints; i++) {
			collisionPoints.add(new Vector3d());
		}
	}

	private int getPointsPerWidth() {
		return Math.max(emob.getWidthBlocks() / 16, minPointsPerSide) * pointsPerChunkSide;
	}

	private int getPointsPerLength() {
		return Math.max(emob.getLengthBlocks() / 16, minPointsPerSide) * pointsPerChunkSide;

	}

	private void updateCollisionPoints() {
		if (collisionPoints.isEmpty()) {
			createCollisionPoints();
		}
		Vector3d corner1 = emob.getSubWorld().getLowerBackLeftCorner();
		Vector3d corner2 = emob.getSubWorld().getUpperFrontRightCorner(corner1);

		Iterator<Vector3d> it = collisionPoints.iterator();
		Vector3d cornerParent1 = emob.convertToPosInParentCoords(corner1);
		Vector3d cornerParent2 = emob.convertToPosInParentCoords(corner2);
		it.next().set(cornerParent1);
		it.next().set((float) cornerParent1.x, (float) cornerParent1.y, (float) cornerParent2.z);
		it.next().set((float) cornerParent1.x, (float) cornerParent2.y, (float) cornerParent1.z);
		it.next().set((float) cornerParent1.x, (float) cornerParent2.y, (float) cornerParent2.z);
		it.next().set((float) cornerParent2.x, (float) cornerParent1.y, (float) cornerParent1.z);
		it.next().set((float) cornerParent2.x, (float) cornerParent1.y, (float) cornerParent2.z);
		it.next().set((float) cornerParent2.x, (float) cornerParent2.y, (float) cornerParent1.z);
		it.next().set(cornerParent2);

		float y = (float) ((cornerParent2.y + cornerParent1.y) / 2D);
		int pointsPerLength = getPointsPerLength();
		if (pointsPerLength > 1) {
			float stepX = (float) ((corner2.x - corner1.x) / (pointsPerLength - 1));
			// for (float x = (float) cornerParent1.x; x <=
			// cornerParent2.x; x += stepX) {
			for (int i = 0; i < pointsPerLength; i++) {
				it.next().set(i * stepX, y, (float) corner1.z);
				it.next().set(i * stepX, y, (float) corner2.z);
			}
		}
		int pointsPerWidth = getPointsPerWidth();
		if (pointsPerWidth > 1) {
			float stepZ = (float) ((corner2.z - corner1.z) / (pointsPerLength - 1));
			// for (float z = (float) cornerParent1.z; z <=
			// cornerParent2.z; z += stepZ) {
			for (int i = 0; i < pointsPerWidth; i++) {
				it.next().set((float) corner1.x, y, i * stepZ);
				it.next().set((float) corner2.x, y, i * stepZ);
			}
		}
	}
	

	private AxisAlignedBB createBB(Vector3f point) {
		return AxisAlignedBB.getBoundingBox(point.x-0.5, point.y-0.5, point.z-0.5, point.x+0.5, point.y+0.5, point.z +0.5);
	}
	
	
	private AxisAlignedBB createBB2(Vector3f point, Vec3 offset) {
		double minX = Math.min(point.x - offset.xCoord, point.x + offset.xCoord);
		// double minY = Math.min(point.y - offset.yCoord, point.y +
		// offset.yCoord);
		double minY = point.y - 8;
		double minZ = Math.min(point.z - offset.zCoord, point.z + offset.zCoord);
		double maxX = Math.max(point.x - offset.xCoord, point.x + offset.xCoord);
		// double maxY = Math.max(point.y - offset.yCoord, point.y +
		// offset.yCoord);
		double maxY = point.y + 8;
		double maxZ = Math.max(point.z - offset.zCoord, point.z + offset.zCoord);
		if (maxX <= minX) {
			maxX = minX + 1;
		}
		// if (maxY <= minY) {
		// maxY = minY + 1;
		// }
		if (maxZ <= minZ) {
			maxZ = minZ + 1;
		}
		return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

}

