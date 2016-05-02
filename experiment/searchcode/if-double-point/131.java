<<<<<<< HEAD
package nat.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * 
 * Implementation of bucket PR k-d tree.
 * 
 * @author Nat Pavasant
 * 
 * @param <V>
 *            The type of data to store
 */
public class PRKdBucketTree<V> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Distancer EUCLIDIAN = new Distancer.EuclidianDistancer();
	public static final Distancer MANHATTAN = new Distancer.ManhattanDistancer();

	private final PRKdBucketTree<V>[] children;
	private final Queue<KdEntry<V>> data;
	private final Distancer distancer;
	private final double[] lowerBound, upperBound;
	private final int[] numChildren;
	private final int allDimensions;
	private final int dimension;
	private final int maxDepth, maxDensity;
	private final double splitMedian;
	private final int numChildrenSelf;

	private boolean isLeaf = true;

	/**
	 * Create new Bucket PR k-d tree.
	 * 
	 * @param allDimensions
	 *            number of dimensions in the tree
	 * @param lowerBound
	 *            the minimum value of the location of each dimension
	 * @param upperBound
	 *            the maximum value of the location of each dimension
	 * @param numChildren
	 *            number of children in each dimension
	 * @param maxDepth
	 *            the max depth of the tree
	 * @param maxDensity
	 *            size of bucket in each leaf
	 * @param distancer
	 *            distance measurer
	 */
	@SuppressWarnings("unchecked")
	public PRKdBucketTree(int allDimensions, double[] lowerBound,
			double[] upperBound, int[] numChildren, int maxDepth,
			int maxDensity, Distancer distancer) {
		if (allDimensions < 1 || maxDensity < 1)
			throw new IllegalArgumentException(
					"Either dimension or density isn't positive integer.");

		if (lowerBound.length != allDimensions
				|| upperBound.length != allDimensions
				|| numChildren.length != allDimensions)
			throw new IllegalArgumentException(
					"Either bounds or children amount is more or less than dimension count.");

		for (double a : lowerBound) {
			if (a < 0)
				throw new IllegalArgumentException(
						"Can't set lower bound to negative number.");
		}

		for (int i = 0; i < lowerBound.length; i++) {
			if (lowerBound[i] > upperBound[i])
				throw new IllegalArgumentException(
						"Upper bound must have a value higer than lower bound.");
		}

		this.allDimensions = allDimensions;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.numChildren = numChildren;
		this.numChildrenSelf = numChildren[this.dimension];
		this.distancer = distancer;
		this.maxDepth = maxDepth;
		this.maxDensity = maxDensity;
		this.dimension = maxDepth % allDimensions;
		this.data = new LinkedList<KdEntry<V>>();
		this.children = new PRKdBucketTree[numChildren[this.dimension]];
		this.splitMedian = (upperBound[this.dimension] - lowerBound[this.dimension])
				/ numChildrenSelf;
	}

	// Another constructor
	public static <T> PRKdBucketTree<T> getTree(T dataType, int dimension,
			double lowerBound, double upperBound, int numChildren,
			int maxDepth, int maxDensity, Distancer distance) {
		double[] lowerBounds = new double[dimension];
		double[] upperBounds = new double[dimension];
		int[] numChildrens = new int[dimension];
		Arrays.fill(lowerBounds, lowerBound);
		Arrays.fill(upperBounds, upperBound);
		Arrays.fill(numChildrens, numChildren);
		return new PRKdBucketTree<T>(dimension, lowerBounds, upperBounds,
				numChildrens, maxDepth, maxDensity, distance);
	}

	public static <T> PRKdBucketTree<T> getTree(T dataType, int dimension,
			double upperBound, int numChildren, int maxDepth, int maxDensity,
			Distancer distance) {
		return getTree(dataType, dimension, 0, upperBound, numChildren,
				maxDepth, maxDensity, distance);
	}

	public static <T> PRKdBucketTree<T> getTree(T dataType, int dimension,
			double upperBound, int numChildren, int maxDepth, int maxDensity) {
		return getTree(dataType, dimension, 0, upperBound, numChildren,
				maxDepth, maxDensity, EUCLIDIAN);
	}

	public static <T> PRKdBucketTree<T> getTree(T dataType, int dimension,
			double upperBound, int maxDepth, int maxDensity) {
		return getTree(dataType, dimension, 0, upperBound, 2, maxDepth,
				maxDensity, EUCLIDIAN);
	}

	public static <T> PRKdBucketTree<T> getTree(T dataType, int dimension,
			double upperBound, int numChildren) {
		return getTree(dataType, dimension, 0, upperBound, numChildren, 500, 8,
				EUCLIDIAN);
	}

	public static <T> PRKdBucketTree<T> getTree(T dataType, int dimension,
			double upperBound) {
		return getTree(dataType, dimension, 0, upperBound, 2, 500, 8, EUCLIDIAN);
	}

	public static <T> PRKdBucketTree<T> getTree(T dataType, int dimension) {
		return getTree(dataType, dimension, 0, 1, 2, 500, 8, EUCLIDIAN);
	}

	/**
	 * Add new point to the tree
	 * 
	 * @param value
	 *            the stored value
	 * @param location
	 *            location of the point
	 * @return removed point if any
	 */
	public KdEntry<V> addPoint(V value, double[] location) {
		KdEntry<V> entry = new KdEntry<V>(value, location);
		return addPoint(entry);
	}

	/**
	 * Get the n-nearest neighbor.
	 * 
	 * @param size
	 *            number of neighbors
	 * @param center
	 *            center of the cluster
	 * @param weight
	 *            weighting for the distancer
	 * @return
	 */
	public KdCluster<V> getNearestNeighbors(int size, double[] center,
			double[] weight) {
		KdCluster<V> cluster = new KdCluster<V>(size, center, weight, distancer);
		nearestNeighborsSearch(cluster);
		return cluster;
	}

	/**
	 * Get the n-nearest neighbor.
	 * 
	 * @param size
	 *            number of neighbors
	 * @param center
	 *            center of the cluster
	 * @return
	 */
	public KdCluster<V> getNearestNeighbors(int size, double[] center) {
		double[] weight = new double[allDimensions];
		Arrays.fill(weight, 1d);
		return getNearestNeighbors(size, center, weight);
	}

	/**
	 * Back-end implementation of the entry adding.
	 * 
	 * @param entry
	 *            new entry to the tree
	 * @return removed point if any
	 */
	private KdEntry<V> addPoint(KdEntry<V> entry) {

		if (isLeaf) {

			// Still has spaces, add the data
			if (data.size() < maxDensity) {
				data.add(entry);
				return null;
			}

			// final leaf, unsplitable, remove element and add new one.
			if (maxDepth <= 1) {
				data.add(entry);
				return data.poll();
			}

			// if we reached here, we need to split this leaf to a branch.
			isLeaf = false;
			for (int i = 0; i < numChildrenSelf; i++) {
				children[i] = createChildTree(i);
			}
			for (KdEntry<V> p : data) {
				passToChildren(p);
			}

			data.clear();
		}

		// we are branch, pass to children
		return passToChildren(entry);
	}

	/**
	 * Perform n-nearest neighbor search
	 * 
	 * @param cluster
	 *            current working cluster
	 */
	private void nearestNeighborsSearch(KdCluster<V> cluster) {
		if (!cluster.isViable(this))
			return;
		if (isLeaf) {
			for (KdEntry<V> p : data) {
				cluster.consider(p);
			}
		} else {
			int index = getChildrenIndex(cluster.center[dimension]);
			for (int i = index, j = index + 1;; i--, j++) {
				if (i < 0 && j >= numChildrenSelf)
					break;
				if (i >= 0 && children[i] != null)
					children[i].nearestNeighborsSearch(cluster);
				if (j < numChildrenSelf && children[j] != null)
					children[j].nearestNeighborsSearch(cluster);
			}

			// if (cluster.isViable(children[index]))
			// children[index].nearestNeighborsSearch(cluster);
			// for (int i = index + 1; i < numChildrenSelf; i++)
			// if (cluster.isViable(children[i]))
			// children[i].nearestNeighborsSearch(cluster);
			// for (int i = index - 1; i >= 0; i--)
			// if (cluster.isViable(children[i]))
			// children[i].nearestNeighborsSearch(cluster);
		}
	}

	/**
	 * Pass the entry to correct children
	 * 
	 * @param p
	 *            entry
	 * @return removed point if any
	 */
	private KdEntry<V> passToChildren(KdEntry<V> p) {
		int i = getChildrenIndex(p.getLocation(dimension));

		if (children[i] == null)
			children[i] = createChildTree(i);

		return children[i].addPoint(p);
	}

	/**
	 * Return index of the children which will contains data with that value.
	 * 
	 * @param value
	 *            the value
	 * @return index of children
	 */
	private int getChildrenIndex(double value) {
		return (int) M.limit(0, Math.floor(value / splitMedian),
				numChildrenSelf - 1);
	}

	/**
	 * Create children tree with correct bound.
	 * 
	 * @param i
	 *            the index of the children
	 * @return created tree
	 */
	private PRKdBucketTree<V> createChildTree(int i) {
		double[] upperBound = this.upperBound.clone();
		double[] lowerBound = this.lowerBound.clone();
		lowerBound[dimension] = splitMedian * i;
		upperBound[dimension] = splitMedian * (i + 1);
		return new PRKdBucketTree<V>(allDimensions, lowerBound, upperBound,
				numChildren, maxDepth - 1, maxDensity, distancer);
	}

	public static class KdCluster<K> implements Iterable<KdPoint<K>> {
		private final PriorityQueue<KdPoint<K>> points;
		// private final SortedSet<KdPoint<K>> points;
		private final double[] center;
		private final double[] weight;
		private final int size;
		private final Distancer distancer;

		public KdCluster(int size, double[] center, double[] weight,
				Distancer distancer) {
			points = new PriorityQueue<KdPoint<K>>();
			// points = new TreeSet<KdPoint<K>>();
			this.size = size;
			this.center = center;
			this.weight = weight;
			this.distancer = distancer;
		}

		public void consider(KdEntry<K> k) {
			KdPoint<K> p = new KdPoint<K>(k, center, weight, distancer);

			if (points.size() < size) {
				points.add(p);
			} else if (points.peek().getDistanceToCenter() > p
					.getDistanceToCenter()) {
				points.poll();
				points.add(p);
			}
		}

		public boolean isViable(PRKdBucketTree<K> tree) {

			if (points.size() < size)
				return true;

			double[] testPoints = new double[center.length];

			for (int i = 0; i < center.length; i++)
				testPoints[i] = M.limit(tree.lowerBound[i], center[i],
						tree.upperBound[i]);

			return points.peek().getDistanceToCenter() > distancer.getDistance(
					center, testPoints, weight);
		}

		public Iterator<KdPoint<K>> iterator() {
			return points.iterator();
		}

		public Collection<KdPoint<K>> getValues() {
			Collection<KdPoint<K>> collect = new ArrayList<KdPoint<K>>(points
					.size());

			for (KdPoint<K> p : points) {
				collect.add(p);
			}

			return collect;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(center);
			result = prime * result
					+ ((distancer == null) ? 0 : distancer.hashCode());
			result = prime * result
					+ ((points == null) ? 0 : points.hashCode());
			result = prime * result + size;
			result = prime * result + Arrays.hashCode(weight);
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof KdCluster))
				return false;
			KdCluster other = (KdCluster) obj;
			if (!Arrays.equals(center, other.center))
				return false;
			if (distancer == null) {
				if (other.distancer != null)
					return false;
			} else if (!distancer.equals(other.distancer))
				return false;
			if (points == null) {
				if (other.points != null)
					return false;
			} else if (!points.equals(other.points))
				return false;
			if (size != other.size)
				return false;
			if (!Arrays.equals(weight, other.weight))
				return false;
			return true;
		}
	}

	private static class KdEntry<K> implements Serializable {
		private static final long serialVersionUID = 1L;

		private final K value;
		private final double[] location;

		public KdEntry(K value, double[] location) {
			super();
			this.value = value;
			this.location = location;
		}

		public K getValue() {
			return value;
		}

		public double[] getLocation() {
			return location;
		}

		public double getLocation(int a) {
			return location[a];
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(location);
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof KdEntry))
				return false;
			KdEntry other = (KdEntry) obj;
			if (!Arrays.equals(location, other.location))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	public static class KdPoint<K> extends KdEntry<K> implements Serializable,
			Comparable<KdPoint<K>> {
		private static final long serialVersionUID = 1L;
		private final double distanceToCenter;

		public KdPoint(KdEntry<K> p, double[] center, double[] weight,
				Distancer distancer) {
			super(p.getValue(), p.getLocation());
			distanceToCenter = distancer.getDistance(center, getLocation(),
					weight);
		}

		public double getDistanceToCenter() {
			return distanceToCenter;
		}

		@Override
		public String toString() {
			return (new Double(distanceToCenter)).toString();
		}

		public int compareTo(KdPoint<K> o) {
			return (int) Math.signum(o.distanceToCenter - distanceToCenter);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			long temp;
			temp = Double.doubleToLongBits(distanceToCenter);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (!(obj instanceof KdPoint))
				return false;
			KdPoint other = (KdPoint) obj;
			if (Double.doubleToLongBits(distanceToCenter) != Double
					.doubleToLongBits(other.distanceToCenter))
				return false;
			return true;
		}
	}

	public static abstract class Distancer {
		public double getDistance(double[] p1, double[] p2, double[] weight) {
			if (p1.length != p2.length)
				throw new IllegalArgumentException();
			return getPointDistance(p1, p2, weight);
		}

		public abstract double getPointDistance(double[] p1, double[] p2,
				double[] weight);

		public static class EuclidianDistancer extends Distancer {
			@Override
			public double getPointDistance(double[] p1, double[] p2,
					double[] weight) {
				double result = 0;
				for (int i = 0; i < p1.length; i++) {
					result += M.sqr(p1[i] - p2[i]) * weight[i];
				}
				return M.sqrt(result);
			}
		}

		public static class ManhattanDistancer extends Distancer {
			@Override
			public double getPointDistance(double[] p1, double[] p2,
					double[] weight) {
				double result = 0;
				for (int i = 0; i < p1.length; i++) {
					result += M.abs(p1[i] - p2[i]) * weight[i];
				}
				return result;
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + allDimensions;
		result = prime * result + Arrays.hashCode(children);
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + dimension;
		result = prime * result + (isLeaf ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(lowerBound);
		result = prime * result + maxDensity;
		result = prime * result + maxDepth;
		result = prime * result + Arrays.hashCode(numChildren);
		long temp;
		temp = Double.doubleToLongBits(splitMedian);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(upperBound);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PRKdBucketTree))
			return false;
		PRKdBucketTree other = (PRKdBucketTree) obj;
		if (allDimensions != other.allDimensions)
			return false;
		if (!Arrays.equals(children, other.children))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (dimension != other.dimension)
			return false;
		if (isLeaf != other.isLeaf)
			return false;
		if (!Arrays.equals(lowerBound, other.lowerBound))
			return false;
		if (maxDensity != other.maxDensity)
			return false;
		if (maxDepth != other.maxDepth)
			return false;
		if (!Arrays.equals(numChildren, other.numChildren))
			return false;
		if (Double.doubleToLongBits(splitMedian) != Double
				.doubleToLongBits(other.splitMedian))
			return false;
		if (!Arrays.equals(upperBound, other.upperBound))
			return false;
		return true;
	}

}
=======
package cgeo.geocaching.geopoint;

import cgeo.geocaching.ICoordinates;

import java.util.Locale;
import java.util.Set;



public class Viewport {

    public final Geopoint center;
    public final Geopoint bottomLeft;
    public final Geopoint topRight;

    public Viewport(final ICoordinates point1, final ICoordinates point2) {
        final Geopoint gp1 = point1.getCoords();
        final Geopoint gp2 = point2.getCoords();
        this.bottomLeft = new Geopoint(Math.min(gp1.getLatitude(), gp2.getLatitude()),
                Math.min(gp1.getLongitude(), gp2.getLongitude()));
        this.topRight = new Geopoint(Math.max(gp1.getLatitude(), gp2.getLatitude()),
                Math.max(gp1.getLongitude(), gp2.getLongitude()));
        this.center = new Geopoint((gp1.getLatitude() + gp2.getLatitude()) / 2,
                (gp1.getLongitude() + gp2.getLongitude()) / 2);
    }

    public Viewport(final ICoordinates center, final double latSpan, final double lonSpan) {
        this.center = center.getCoords();
        final double centerLat = this.center.getLatitude();
        final double centerLon = this.center.getLongitude();
        final double latHalfSpan = Math.abs(latSpan) / 2;
        final double lonHalfSpan = Math.abs(lonSpan) / 2;
        bottomLeft = new Geopoint(centerLat - latHalfSpan, centerLon - lonHalfSpan);
        topRight = new Geopoint(centerLat + latHalfSpan, centerLon + lonHalfSpan);
    }

    public double getLatitudeMin() {
        return bottomLeft.getLatitude();
    }

    public double getLatitudeMax() {
        return topRight.getLatitude();
    }

    public double getLongitudeMin() {
        return bottomLeft.getLongitude();
    }

    public double getLongitudeMax() {
        return topRight.getLongitude();
    }

    public Geopoint getCenter() {
        return center;
    }

    public double getLatitudeSpan() {
        return getLatitudeMax() - getLatitudeMin();
    }

    public double getLongitudeSpan() {
        return getLongitudeMax() - getLongitudeMin();
    }

    /**
     * Check whether a point is contained in this viewport.
     *
     * @param point
     *            the coordinates to check
     * @return true if the point is contained in this viewport, false otherwise
     */
    public boolean contains(final ICoordinates point) {
        final Geopoint coords = point.getCoords();
        return coords.getLongitudeE6() >= bottomLeft.getLongitudeE6()
                && coords.getLongitudeE6() <= topRight.getLongitudeE6()
                && coords.getLatitudeE6() >= bottomLeft.getLatitudeE6()
                && coords.getLatitudeE6() <= topRight.getLatitudeE6();
    }

    @Override
    public String toString() {
        return "(" + bottomLeft.toString() + "," + topRight.toString() + ")";
    }

    /**
     * Check whether another viewport is fully included into the current one.
     *
     * @param vp
     *            the other viewport
     * @return true if the vp is fully included into this one, false otherwise
     */
    public boolean includes(final Viewport vp) {
        return contains(vp.bottomLeft) && contains(vp.topRight);
    }

    /**
     * Return the "where" part of the string appropriate for a SQL query.
     *
     * @param dbTable
     *            the database table to use as prefix, or null if no prefix is required
     * @return the string without the "where" keyword
     */
    public String sqlWhere(final String dbTable) {
        final String prefix = dbTable == null ? "" : (dbTable + ".");
        return String.format((Locale) null,
                "%slatitude >= %s and %slatitude <= %s and %slongitude >= %s and %slongitude <= %s",
                prefix, getLatitudeMin(), prefix, getLatitudeMax(), prefix, getLongitudeMin(), prefix, getLongitudeMax());
    }

    /**
     * Return a widened or shrunk viewport.
     *
     * @param factor
     *            multiplicative factor for the latitude and longitude span (> 1 to widen, < 1 to shrink)
     * @return a widened or shrunk viewport
     */
    public Viewport resize(final double factor) {
        return new Viewport(getCenter(), getLatitudeSpan() * factor, getLongitudeSpan() * factor);
    }

    /**
     * Return a viewport that contains the current viewport as well as another point.
     * 
     * @param point
     *            the point we want in the viewport
     * @return either the same or an expanded viewport
     */
    public Viewport expand(final ICoordinates point) {
        if (contains(point)) {
            return this;
        }

        final Geopoint coords = point.getCoords();
        final double latitude = coords.getLatitude();
        final double longitude = coords.getLongitude();
        final double latMin = Math.min(getLatitudeMin(), latitude);
        final double latMax = Math.max(getLatitudeMax(), latitude);
        final double lonMin = Math.min(getLongitudeMin(), longitude);
        final double lonMax = Math.max(getLongitudeMax(), longitude);
        return new Viewport(new Geopoint(latMin, lonMin), new Geopoint(latMax, lonMax));
    }

    /**
     * Return the smallest viewport containing all the given points.
     *
     * @param points
     *            a set of points. Point with null coordinates (or null themselves) will be ignored
     * @return the smallest viewport containing the non-null coordinates, or null if no coordinates are non-null
     */
    static public Viewport containing(final Set<? extends ICoordinates> points) {
        Viewport viewport = null;
        for (final ICoordinates point : points) {
            if (point != null && point.getCoords() != null) {
                if (viewport == null) {
                    viewport = new Viewport(point, point);
                } else {
                    viewport = viewport.expand(point);
                }
            }
        }
        return viewport;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof Viewport)) {
            return false;
        }
        final Viewport vp = (Viewport) other;
        return bottomLeft.equals(vp.bottomLeft) && topRight.equals(vp.topRight);
    }

    @Override
    public int hashCode() {
        return bottomLeft.hashCode() ^ topRight.hashCode();
    }

}
>>>>>>> 76aa07461566a5976980e6696204781271955163

