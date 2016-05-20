package roboflight.phys;

import javax.vecmath.Vector3d;

/**
 * This is my 'collision' library. Rather it is all about intersection testing.
 * 
 * These are from Game Physics by David H. Eberly and Ken Shoemake (for the most
 * part)
 * 
 * @author Chase
 * 
 */
public final class Collision {
	private static final int ta0i[] = new int[]{ 2, 0, 1 };
	private static final int ta1i[] = new int[]{ 1, 2, 0 };
	private static final int tx0i[] = new int[]{ 1, 0, 0 };
	private static final int tx1i[] = new int[]{ 2, 2, 1 };
	private static final double margin = 1e-08;
	private static final double cutoff = 1.0 - margin;

	/**
	 * @return true if they intersect
	 */
	public static final boolean BoxTest(BoundingBox b0, BoundingBox b1) {
		boolean existsParallelPair = false;

		// For convenience
		final Vector3d A[] = b0.axis;
		final Vector3d B[] = b1.axis;
		final double[] EA = b0.extent;
		final double[] EB = b1.extent;
		Vector3d D = new Vector3d(b1.center);
		D.sub(b0.center);
		// matrix C = A^T B, c_{ij} = dot(A_i,B_j)
		double C[][] = new double[3][3];
		double AbsC[][] = new double[3][3]; // |c_{ij}|
		double AD[] = new double[3]; // dot(A_i,D)
		// interval radii and distance between centers
		double r0, r1, r;
		double r01; // = r0 + r1

		int b, a, i;

		// Axis C0 + t * A[a]
		for(a = 0; a < 3; ++a) {
			for(i = 0; i < 3; ++i) {
				C[a][i] = A[a].dot(B[i]);
				AbsC[a][i] = Math.abs(C[a][i]);
				if(AbsC[a][i] > cutoff)
					existsParallelPair = true;
			}
			AD[a] = A[a].dot(D);
			r = Math.abs(AD[a]);
			r1 = EB[0] * AbsC[a][0] + EB[1] * AbsC[a][1] + EB[2] * AbsC[a][2];
			r01 = EA[a] + r1;
			if(r > r01)
				return false;
		}

		// Axis C0 + t * B[b]
		for(b = 0; b < 3; ++b) {
			r = Math.abs(B[b].dot(D));
			r0 = EA[0] * AbsC[0][b] + EA[1] * AbsC[1][b] + EA[2] * AbsC[2][b];
			r01 = r0 + EB[b];
			if(r > r01)
				return false;
		}

		if(existsParallelPair) {
			return true;
		}

		// Axis C0 + t * A[a]xB[b]
		for(a = 0; a < 3; ++a) {
			int ta0 = ta0i[a];
			int ta1 = ta1i[a];
			int ta2 = tx0i[a];
			int ta3 = tx1i[a];
			for(b = 0; b < 3; ++b) {
				int tb0 = tx0i[b];
				int tb1 = tx1i[b];
				r = Math.abs(AD[ta0] * C[ta1][b] - AD[ta1] * C[ta0][b]);
				r0 = EA[ta2] * AbsC[ta3][b] + EA[ta3] * AbsC[ta2][b];
				r1 = EB[tb0] * AbsC[a][tb1] + EB[tb1] * AbsC[a][tb0];
				r01 = r0 + r1;
				if(r > r01)
					return false;
			}
		}

		return true;
	}

	/**
	 * @return true if they intersect within the given time
	 */
	public static final boolean BoxTest(BoundingBox b0, BoundingBox b1, Vector3d v0, Vector3d v1, int tMax) {
		if(v0.equals(v1)) {
			if(BoxTest(b0, b1))
				return true;
			return false;
		}

		boolean existsParallelPair = false;

		// For convenience
		final Vector3d A[] = b0.axis;
		final Vector3d B[] = b1.axis;
		final double[] EA = b0.extent;
		final double[] EB = b1.extent;
		Vector3d D = new Vector3d(b1.center);
		D.sub(b0.center);
		Vector3d W = new Vector3d(v1);
		W.sub(v0);
		double C[][] = new double[3][3]; // matrix C = A^T B, c_{ij} =
		// dot(A_i,B_j)
		double AbsC[][] = new double[3][3]; // |c_{ij}|
		double AD[] = new double[3]; // dot(A_i,D)
		double AW[] = new double[3]; // dot(A_i,W)
		double min0, max0, min1, max1, center, radius, speed;
		int i, j;

		// The equivalent to pass by reference in Java.
		double mContactTime[] = new double[]{ 0 };
		double tlast[] = new double[]{ Double.MAX_VALUE };

		// Axes C0 + t * A[i]
		for(i = 0; i < 3; ++i) {
			for(j = 0; j < 3; ++j) {
				C[i][j] = A[i].dot(B[j]);
				AbsC[i][j] = Math.abs(C[i][j]);
				if(AbsC[i][j] > cutoff)
					existsParallelPair = true;
			}
			AD[i] = A[i].dot(D);
			AW[i] = A[i].dot(W);
			min0 = -EA[i];
			max0 = +EA[i];
			radius = EB[0] * AbsC[i][0] + EB[1] * AbsC[i][1] + EB[2] * AbsC[i][2];
			min1 = AD[i] - radius;
			max1 = AD[i] + radius;
			speed = AW[i];
			if(IsSeparated(min0, max0, min1, max1, speed, tMax, tlast, mContactTime))
				return false;
		}

		// axes C0+t*B[i]
		for(i = 0; i < 3; ++i) {
			radius = EA[0] * AbsC[0][i] + EA[1] * AbsC[1][i] + EA[2] * AbsC[2][i];
			min0 = -radius;
			max0 = +radius;
			center = B[i].dot(D);
			min1 = center - EB[i];
			max1 = center + EB[i];
			speed = W.dot(B[i]);
			if(IsSeparated(min0, max0, min1, max1, speed, tMax, tlast, mContactTime))
				return false;
		}

		if(existsParallelPair)
			return true;

		// Axis C0 + t*A[i]xB[j]
		for(i = 0; i < 3; ++i) {
			int ta0 = ta0i[i];
			int ta1 = ta1i[i];
			int ta2 = tx0i[i];
			int ta3 = tx1i[i];
			for(j = 0; j < 3; ++j) {
				int tb0 = tx0i[j];
				int tb1 = tx1i[j];
				radius = EA[ta2] * AbsC[ta3][j] + EA[ta3] * AbsC[ta2][j];
				min0 = -radius;
				max0 = +radius;
				center = AD[ta0] * C[ta1][j] - AD[ta1] * C[ta0][j];
				radius = EB[tb0] * AbsC[i][tb1] + EB[tb1] * AbsC[i][tb0];
				min1 = center - radius;
				max1 = center + radius;
				speed = AW[ta0] * C[ta1][j] - AW[ta1] * C[ta0][j];
				if(IsSeparated(min0, max0, min1, max1, speed, tMax, tlast, mContactTime))
					return false;
			}
		}

		return true;
	}

	private static final boolean IsSeparated(double min0, double max0, double min1, double max1, double speed,
											 double tmax, double tlast[], double mContactTime[]) {
		double invSpeed, t;

		// TODO: optimize this function
		if(max1 < min0) {
			if(speed <= 0.0)
				return true;
			invSpeed = 1.0 / speed;
			t = (min0 - max1) * invSpeed;
			if(t > mContactTime[0])
				mContactTime[0] = t;
			if(mContactTime[0] > tmax)
				return true;
			t = (max0 - min1) * invSpeed;
			if(t < tlast[0])
				tlast[0] = t;
			if(mContactTime[0] > tlast[0])
				return true;
		} else if(max0 < min1) {
			if(speed >= 0.0)
				return true;
			invSpeed = 1.0 / speed;

			t = (max0 - min1) * invSpeed;
			if(t > mContactTime[0])
				mContactTime[0] = t;

			if(mContactTime[0] > tmax)
				return true;

			t = (min0 - max1) * invSpeed;
			if(t < tlast[0])
				tlast[0] = t;

			if(mContactTime[0] > tlast[0])
				return true;
		} else { // box0 and box1 initially overlap
			if(speed > 0.0) {
				t = (max0 - min1) / speed;
				if(t < tlast[0])
					tlast[0] = t;
				if(mContactTime[0] > tlast[0])
					return true;
			} else if(speed < 0.0) {
				t = (min0 - max1) / speed;
				if(t < tlast[0])
					tlast[0] = t;
				if(mContactTime[0] > tlast[0])
					return true;
			}
		}

		return false;
	}

	/**
	 * @return true if they intersect
	 */
	public static final boolean SphereTest(Sphere s0, Sphere s1) {
		double r = s0.radius + s1.radius;
		return s0.center.distanceSquared(s1.center) <= r * r;
	}

	/**
	 * @return true if they intersect
	 */
	public static final boolean SphereTest(Sphere s0, Sphere s1, Vector3d v0, Vector3d v1, int time) {
		Vector3d relVelocity = new Vector3d(v1);
		relVelocity.sub(v0);
		double a = relVelocity.lengthSquared();
		Vector3d diff = new Vector3d(s1.center);
		diff.sub(s0.center);
		double c = diff.lengthSquared();
		double rSum = s0.radius + s1.radius;
		double rSumSqr = rSum * rSum;

		if(a > 0.0) {
			double b = diff.dot(relVelocity);
			if(b <= 0.0) {
				if(-time * a <= b)
					return a * c - b * b <= a * rSumSqr;
				return time * (time * a + 2.0 * b) + c <= rSumSqr;
			}
		}

		return c <= rSumSqr;
	}

	/**
	 * @return true if they intersect
	 */
	public static final boolean BoxSphereTest(BoundingBox b, Sphere s) {
		// Test for intersection in the coordinate system of the box by
		// transforming the sphere into that coordinate system.
		Vector3d cdiff = new Vector3d(s.center);
		cdiff.sub(b.center);

		double ax = Math.abs(cdiff.dot(b.axis[0]));
		double ay = Math.abs(cdiff.dot(b.axis[1]));
		double az = Math.abs(cdiff.dot(b.axis[2]));
		double dx = ax - b.extent[0];
		double dy = ay - b.extent[1];
		double dz = az - b.extent[2];

		if(ax <= b.extent[0]) {
			if(ay <= b.extent[1]) {
				if(az <= b.extent[2])
					// Sphere center inside box.
					return true;
				else
					// Potential sphere-face intersection with face z.
					return dz <= s.radius;
			} else {
				if(az <= b.extent[2]) {
					// Potential sphere-face intersection with face y.
					return dy <= s.radius;
				} else {
					// Potential sphere-edge intersection with edge formed
					// by faces y and z.
					double rsqr = s.radius * s.radius;
					return dy * dy + dz * dz <= rsqr;
				}
			}
		} else {
			if(ay <= b.extent[1]) {
				if(az <= b.extent[2]) {
					// Potential sphere-face intersection with face x.
					return dx <= s.radius;
				} else {
					// Potential sphere-edge intersection with edge formed
					// by faces x and z.
					double rsqr = s.radius * s.radius;
					return dx * dx + dz * dz <= rsqr;
				}
			} else {
				if(az <= b.extent[2]) {
					// Potential sphere-edge intersection with edge formed
					// by faces x and y.
					double rsqr = s.radius * s.radius;
					return dx * dx + dy * dy <= rsqr;
				} else {
					// Potential sphere-vertex intersection at corner formed
					// by faces x,y,z.
					double rsqr = s.radius * s.radius;
					return dx * dx + dy * dy + dz * dz <= rsqr;
				}
			}
		}
	}

	/**
	 * @return true if intersect
	 */
	public static final boolean BoxSphereTest(BoundingBox b, Sphere s, Vector3d v0, Vector3d v1, int tMax) {

		// Find intersections relative to the coordinate system of the box.
		// The sphere is transformed to the box coordinates and the velocity of
		// the sphere is relative to the box.
		Vector3d cdiff = new Vector3d(s.center);
		cdiff.sub(b.center);
		Vector3d relVelocity = new Vector3d(v1);
		relVelocity.sub(v0);
		double ax = cdiff.dot(b.axis[0]);
		double ay = cdiff.dot(b.axis[1]);
		double az = cdiff.dot(b.axis[2]);
		double vx = relVelocity.dot(b.axis[0]);
		double vy = relVelocity.dot(b.axis[1]);
		double vz = relVelocity.dot(b.axis[2]);

		double mContactTime[] = new double[]{ 0 };
		// Flip coordinate frame into the first octant.
		if(ax < 0) {
			ax = -ax;
			vx = -vx;
		}
		if(ay < 0) {
			ay = -ay;
			vy = -vy;
		}
		if(az < 0) {
			az = -az;
			vz = -vz;
		}

		int retVal;

		if(ax <= b.extent[0]) {
			if(ay <= b.extent[1]) {
				if(az <= b.extent[2]) {
					// The sphere center is inside box. Return it as the contact
					// point, but report an "other" intersection type.
					mContactTime[0] = 0;
					return true;
				} else {
					// Sphere above face on axis Z.
					retVal = FindFaceRegionIntersection(b.extent[0], b.extent[1], b.extent[2], ax, ay, az, vx, vy, vz,
														true, mContactTime, s.radius);
				}
			} else {
				if(az <= b.extent[2]) {
					// Sphere above face on axis Y.
					retVal = FindFaceRegionIntersection(b.extent[0], b.extent[2], b.extent[1], ax, az, ay, vx, vz, vy,
														true, mContactTime, s.radius);
				} else {
					// Sphere is above the edge formed by faces y and z.
					retVal = FindEdgeRegionIntersection(b.extent[1], b.extent[0], b.extent[2], ay, ax, az, vy, vx, vz,
														true, mContactTime, s.radius);
				}
			}
		} else {
			if(ay <= b.extent[1]) {
				if(az <= b.extent[2]) {
					// Sphere above face on axis X.
					retVal = FindFaceRegionIntersection(b.extent[1], b.extent[2], b.extent[0], ay, az, ax, vy, vz, vx,
														true, mContactTime, s.radius);
				} else {
					// Sphere is above the edge formed by faces x and z.
					retVal = FindEdgeRegionIntersection(b.extent[0], b.extent[1], b.extent[2], ax, ay, az, vx, vy, vz,
														true, mContactTime, s.radius);
				}
			} else {
				if(az <= b.extent[2]) {
					// Sphere is above the edge formed by faces x and y.
					retVal = FindEdgeRegionIntersection(b.extent[0], b.extent[2], b.extent[1], ax, az, ay, vx, vz, vy,
														true, mContactTime, s.radius);
				} else {
					// sphere is above the corner formed by faces x,y,z
					retVal = FindVertexRegionIntersection(b.extent[0], b.extent[1], b.extent[2], ax, ay, az, vx, vy,
														  vz, mContactTime, s.radius);
				}
			}
		}
		// if(mContactTime[0] > tMax || retVal != 0) {
		// return false;
		// }
		// This seems to fix it!
		if(retVal == 0 || mContactTime[0] > tMax) {
			return false;
		}
		// Point intersection
		return true;
	}

	private final static double GetVertexIntersection(double dx, double dy, double dz, double vx, double vy, double vz,
													  double rsqr) {
		// Finds the time of a 3D line-sphere intersection between a line
		// P = Dt, where P = (dx, dy, dz) and D = (vx, vy, vz) and
		// a sphere of radius^2 rsqr. Note: only valid if there is, in fact,
		// an intersection.
		double vsqr = vx * vx + vy * vy + vz * vz;
		double dot = dx * vx + dy * vy + dz * vz;
		double diff = dx * dx + dy * dy + dz * dz - rsqr;
		double inv = 1.0 / Math.sqrt(Math.abs(dot * dot - vsqr * diff));
		return diff * inv / (1.0 - dot * inv);
	}

	private final static double GetEdgeIntersection(double dx, double dz, double vx, double vz, double vsqr, double rsqr) {
		// Finds the time of a 2D line-circle intersection between a line
		// P = Dt where P = (dx,dz) and D = (vx, vz) and a circle of radius^2
		// rsqr. Note: only valid if there is, in fact, an intersection.

		double dot = vx * dx + vz * dz;
		double diff = dx * dx + dz * dz - rsqr;
		double inv = 1.0 / Math.sqrt(Math.abs(dot * dot - vsqr * diff));
		return diff * inv / (1.0 - dot * inv);
	}

	private final static int FindFaceRegionIntersection(double ex, double ey, double ez, double cx, double cy,
														double cz, double vx, double vy, double vz, boolean aboveFace,
														double mContactTime[], double sr) {
		// Returns when and whether a sphere in the region above face +Z
		// intersects face +Z or any of its vertices or edges. The input
		// aboveFace is true when the x and y coordinates are within the x and y
		// extents. The function will still work if they are not, but it needs
		// to be false then, to avoid some checks that assume that x and y are
		// within the extents. This function checks face z, and the vertex and
		// two edges that the velocity is headed towards on the face.

		// Check for already intersecting if above face.
		if(cz <= ez + sr && aboveFace) {
			mContactTime[0] = 0;
			return -1;
		}

		// Check for easy out (moving away on Z axis).
		if(vz >= 0) {
			return 0;
		}

		double rsqr = sr * sr;

		double vsqrX = vz * vz + vx * vx;
		double vsqrY = vz * vz + vy * vy;
		double dx, dy, dz = cz - ez;
		double crossX, crossY;
		int signX, signY;

		// This determines which way the box is heading and finds the values of
		// CrossX and CrossY which are positive if the sphere center will not
		// pass through the box. Then it is only necessary to check two edges,
		// the face and the vertex for intersection.

		if(vx >= 0) {
			signX = 1;
			dx = cx - ex;
			crossX = vx * dz - vz * dx;
		} else {
			signX = -1;
			dx = cx + ex;
			crossX = vz * dx - vx * dz;
		}

		if(vy >= 0) {
			signY = 1;
			dy = cy - ey;
			crossY = vy * dz - vz * dy;
		} else {
			signY = -1;
			dy = cy + ey;
			crossY = vz * dy - vy * dz;
		}

		// Does the circle intersect along the x edge?
		if(crossX > sr * vx * signX) {
			if(crossX * crossX > rsqr * vsqrX) {
				// Sphere overshoots box on the x-axis (either side).
				return 0;
			}

			// Does the circle hit the y edge?
			if(crossY > sr * vy * signY) {
				// Potential vertex intersection.
				if(crossY * crossY > rsqr * vsqrY) {
					// Sphere overshoots box on the y-axis (either side).
					return 0;
				}

				Vector3d relVelocity = new Vector3d(vx, vy, vz);
				Vector3d D = new Vector3d(dx, dy, dz);
				Vector3d cross = new Vector3d();
				cross.cross(D, relVelocity);
				if(cross.lengthSquared() > rsqr * relVelocity.lengthSquared()) {
					// Sphere overshoots the box on the corner.
					return 0;
				}

				mContactTime[0] = GetVertexIntersection(dx, dy, dz, vx, vy, vz, rsqr);
			} else {
				// x-edge intersection
				mContactTime[0] = GetEdgeIntersection(dx, dz, vx, vz, vsqrX, rsqr);
			}
		} else {
			// Does the circle hit the y edge?
			if(crossY > sr * vy * signY) {
				// Potential y-edge intersection.
				if(crossY * crossY > rsqr * vsqrY) {
					// Sphere overshoots box on the y-axis (either side).
					return 0;
				}

				mContactTime[0] = GetEdgeIntersection(dy, dz, vy, vz, vsqrY, rsqr);
			} else {
				// Face intersection (easy).
				mContactTime[0] = (-dz + sr) / vz;
			}
		}
		return 1;
	}

	private final static int FindJustEdgeIntersection(double cy, double ex, double ey, double ez, double dx, double dz,
													  double vx, double vy, double vz, double mContactTime[], double sr) {
		// Finds the intersection of a point dx and dz away from an edge with
		// direction y. The sphere is at a point cy, and the edge is at the
		// point ex. Checks the edge and the vertex the velocity is heading
		// towards.

		double rsqr = sr * sr;
		double dy, crossZ, crossX; // possible edge/vertex intersection

		// Depending on the sign of Vy, pick the vertex that the velocity is
		// heading towards on the edge, as well as creating crossX and crossZ
		// such that their sign will always be positive if the sphere center
		// goes over that edge.

		if(vy >= 0) {
			dy = cy - ey;
			crossZ = dx * vy - dy * vx;
			crossX = dz * vy - dy * vz;
		} else {
			dy = cy + ey;
			crossZ = dy * vx - dx * vy;
			crossX = dy * vz - dz * vy;
		}

		// Check where on edge this intersection will occur.
		if(crossZ >= 0 && crossX >= 0 && crossX * crossX + crossZ * crossZ > vy * vy * sr * sr) {
			// Sphere potentially intersects with vertex.
			Vector3d relVelocity = new Vector3d(vx, vy, vz);
			Vector3d D = new Vector3d(dx, dy, dz);
			Vector3d cross = new Vector3d();
			cross.cross(D, relVelocity);
			if(cross.lengthSquared() > rsqr * relVelocity.lengthSquared()) {
				// Sphere overshoots the box on the vertex.
				return 0;
			}

			// Sphere actually does intersect the vertex.
			mContactTime[0] = GetVertexIntersection(dx, dy, dz, vx, vy, vz, rsqr);
		} else {
			// Sphere intersects with edge.
			double vsqrX = vz * vz + vx * vx;
			mContactTime[0] = GetEdgeIntersection(dx, dz, vx, vz, vsqrX, rsqr);
		}
		return 1;
	}

	private final static int FindEdgeRegionIntersection(double ex, double ey, double ez, double cx, double cy,
														double cz, double vx, double vy, double vz, boolean aboveEdge,
														double[] mContactTime, double sr) {
		// Assumes the sphere center is in the region above the x and z planes.
		// The input aboveEdge is true when the y coordinate is within the y
		// extents. The function will still work if it is not, but it needs to
		// be false then, to avoid some checks that assume that y is within the
		// extent. This function checks the edge that the region is above, as
		// well as does a "face region" check on the face it is heading towards.

		double dx = cx - ex;
		double dz = cz - ez;
		double rsqr = sr * sr;

		if(aboveEdge) {
			double diff = dx * dx + dz * dz - rsqr;
			if(diff <= 0.0) {
				// Circle is already intersecting the box.
				mContactTime[0] = 0.0;
				return -1;
			}
		}

		double dot = vx * dx + vz * dz;
		if(dot >= 0.0) {
			// Circle not moving towards box.
			return 0;
		}

		// The value dotPerp splits the region of interest along the edge in the
		// middle of that region.
		double dotPerp = vz * dx - vx * dz;
		if(dotPerp >= 0.0) {
			// Sphere moving towards +z face.
			if(vx >= 0.0) {
				// Passed corner, moving away from box.
				return 0;
			}
			if(dotPerp <= -sr * vx) {
				return FindJustEdgeIntersection(cy, ez, ey, ex, dz, dx, vz, vy, vx, mContactTime, sr);
			}

			// Now, check the face of z for intersections.
			return FindFaceRegionIntersection(ex, ey, ez, cx, cy, cz, vx, vy, vz, false, mContactTime, sr);
		} else {
			// Sphere moving towards +x face.
			if(vz >= 0.0) {
				// Passed corner, moving away from box.
				return 0;
			}

			// Check intersection with x-z edge. See the note above about
			// "scraping" objects.
			if(dotPerp >= sr * vz) {
				// Possible edge/vertex intersection.
				return FindJustEdgeIntersection(cy, ex, ey, ez, dx, dz, vx, vy, vz, mContactTime, sr);
			}

			// Now, check the face of x for intersections.
			return FindFaceRegionIntersection(ez, ey, ex, cz, cy, cx, vz, vy, vx, false, mContactTime, sr);
		}
	}

	private final static int FindVertexRegionIntersection(double ex, double ey, double ez, double cx, double cy,
														  double cz, double vx, double vy, double vz,
														  double[] mContactTime, double sr) {
		// Assumes the sphere is above the vertex +ex, +ey, +ez.
		double dx = cx - ex;
		double dy = cy - ey;
		double dz = cz - ez;
		double rsqr = sr * sr;
		double diff = dx * dx + dy * dy + dz * dz - rsqr;

		if(diff <= 0) {
			// Sphere is already intersecting the box.
			mContactTime[0] = 0;
			return -1;
		}

		if(vx * dx + vy * dy + vz * dz >= 0) {
			// Sphere not moving towards box.
			return 0;
		}

		double crossX = vy * dz - vz * dy;
		double crossY = vx * dz - vz * dx;
		double crossZ = vy * dx - vx * dy;
		double crX2 = crossX * crossX;
		double crY2 = crossY * crossY;
		double crZ2 = crossZ * crossZ;
		double vx2 = vx * vx;
		double vy2 = vy * vy;
		double vz2 = vz * vz;

		// Intersection with the vertex?
		if(crossY < 0 && crossZ >= 0 && crY2 + crZ2 <= rsqr * vx2 || crossZ < 0 && crossX < 0
				&& crX2 + crZ2 <= rsqr * vy2 || crossY >= 0 && crossX >= 0 && crX2 + crY2 <= rsqr * vz2) {
			// Standard line-sphere intersection.
			mContactTime[0] = GetVertexIntersection(dx, dy, dz, vx, vy, vz, sr * sr);
			return 1;
		} else if(crossY < 0 && crossZ >= 0) {
			// x edge region, check y,z planes.
			return FindEdgeRegionIntersection(ey, ex, ez, cy, cx, cz, vy, vx, vz, false, mContactTime, sr);
		} else if(crossZ < 0 && crossX < 0) {
			// y edge region, check x,z planes.
			return FindEdgeRegionIntersection(ex, ey, ez, cx, cy, cz, vx, vy, vz, false, mContactTime, sr);
		} else { // crossY >= 0 && crossX >= 0
			// z edge region, check x,y planes.
			return FindEdgeRegionIntersection(ex, ez, ey, cx, cz, cy, vx, vz, vy, false, mContactTime, sr);
		}
	}
}

