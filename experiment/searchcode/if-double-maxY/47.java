package sirentropy.emob;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Emob;
import net.minecraft.src.Vec3D;
import sirentropy.util.CoordsUtil;

import com.jme3.math.Vector3f;

public class EmobQuickBB extends EmobBB {

	private int pointsPerChunkSide = 0;

	List<Vector3f> collisionPoints = new ArrayList<Vector3f>();

	private int minPointsPerSide = 0;

	public EmobQuickBB(Emob emob, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		super(emob, minX, minY, minZ, maxX, maxY, maxZ);

	}

	private void createCollisionPoints() {
		int pointsPerLength = getPointsPerLength();
		int pointsPerWidth = getPointsPerWidth();
		int totalPoints = 2 * (pointsPerLength + pointsPerWidth) + 8;
		for (int i = 0; i < totalPoints; i++) {
			collisionPoints.add(new Vector3f());
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
		Vec3D corner1 = emob.emobWorld.emob.emobWorld.getLowerBackLeftCorner();
		Vec3D corner2 = emob.emobWorld.emob.emobWorld.getUpperFrontRightCorner(corner1);

		Iterator<Vector3f> it = collisionPoints.iterator();
		Vec3D cornerParent1 = emob.convertToPosInParentCoords(corner1);
		Vec3D cornerParent2 = emob.convertToPosInParentCoords(corner2);
		it.next().set(cornerParent1);
		it.next().set((float) cornerParent1.xCoord, (float) cornerParent1.yCoord, (float) cornerParent2.zCoord);
		it.next().set((float) cornerParent1.xCoord, (float) cornerParent2.yCoord, (float) cornerParent1.zCoord);
		it.next().set((float) cornerParent1.xCoord, (float) cornerParent2.yCoord, (float) cornerParent2.zCoord);
		it.next().set((float) cornerParent2.xCoord, (float) cornerParent1.yCoord, (float) cornerParent1.zCoord);
		it.next().set((float) cornerParent2.xCoord, (float) cornerParent1.yCoord, (float) cornerParent2.zCoord);
		it.next().set((float) cornerParent2.xCoord, (float) cornerParent2.yCoord, (float) cornerParent1.zCoord);
		it.next().set(cornerParent2);

		float y = (float) ((cornerParent2.yCoord + cornerParent1.yCoord) / 2D);
		int pointsPerLength = getPointsPerLength();
		if (pointsPerLength > 1) {
			float stepX = (float) ((corner2.xCoord - corner1.xCoord) / (pointsPerLength - 1));
			// for (float x = (float) cornerParent1.xCoord; x <=
			// cornerParent2.xCoord; x += stepX) {
			for (int i = 0; i < pointsPerLength; i++) {
				it.next().set(i * stepX, y, (float) corner1.zCoord);
				it.next().set(i * stepX, y, (float) corner2.zCoord);
			}
		}
		int pointsPerWidth = getPointsPerWidth();
		if (pointsPerWidth > 1) {
			float stepZ = (float) ((corner2.zCoord - corner1.zCoord) / (pointsPerLength - 1));
			// for (float z = (float) cornerParent1.zCoord; z <=
			// cornerParent2.zCoord; z += stepZ) {
			for (int i = 0; i < pointsPerWidth; i++) {
				it.next().set((float) corner1.xCoord, y, i * stepZ);
				it.next().set((float) corner2.xCoord, y, i * stepZ);
			}
		}
	}
	
	public boolean calculateOffset(Vec3D offset) {
		boolean result = false;
		updateCollisionPoints();
		for (Vector3f point : collisionPoints) {
			AxisAlignedBB collisionPointBB = createBB(point);
			List<AxisAlignedBB> bbs = emob.worldObj.getCollidingBoundingBoxes(emob, collisionPointBB.addCoord(offset.xCoord, offset.yCoord, offset.zCoord));
			for (AxisAlignedBB bb : bbs) {
//				bb = bb.addCoord(offset.xCoord, offset.yCoord, offset.zCoord);
				result |= CoordsUtil.calculateOffset(offset, new Vector3f[] { point },
						CoordsUtil.getBBPoints(new double[] { bb.minX, bb.minY, bb.minZ }, new double[] { bb.maxX, bb.maxY, bb.maxZ }));
			}
		}
		
		return result;
	}

	private AxisAlignedBB createBB(Vector3f point) {
		return AxisAlignedBB.getBoundingBox(point.x-0.5, point.y-0.5, point.z-0.5, point.x+0.5, point.y+0.5, point.z +0.5);
	}
	
	
	private AxisAlignedBB createBB2(Vector3f point, Vec3D offset) {
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

