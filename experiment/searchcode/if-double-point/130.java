<<<<<<< HEAD
package org.csdgn.util.kd2;

import java.util.Arrays;

/**
 * This is a KD Bucket Tree, for fast sorting and searching of K dimensional data.
 * @author Chase
 *
 */
public class KDTreeC {
	/**
	 * Item, for moving data around.
	 * @author Chase
	 */
	public class Item {
		public double[] pnt;
		public Object obj;
		public double distance;
		private Item(double[] p, Object o) {
			pnt = p; obj = o;
		}
	}
	private final int dimensions;
	private final int bucket_size;
	private NodeKD root;

	/**
	 * Constructor with value for dimensions.
	 * @param dimensions - Number of dimensions
	 */
	public KDTreeC(int dimensions) {
		this.dimensions = dimensions;
		this.bucket_size = 64;
		this.root = new NodeKD(this);
	}

	/**
	 * Constructor with value for dimensions and bucket size.
	 * @param dimensions - Number of dimensions
	 * @param bucket - Size of the buckets.
	 */
	public KDTreeC(int dimensions, int bucket) {
		this.dimensions = dimensions;
		this.bucket_size = bucket;
		this.root = new NodeKD(this);
	}

	/**
	 * Add a key and its associated value to the tree.
	 * @param key - Key to add
	 * @param val - object to add
	 */
	public void add(double[] key, Object val) {
		root.add(new Item(key,val));
	}

	/**
	 * Returns all PointKD within a certain range defined by an upper and lower PointKD.
	 * @param low - lower bounds of area
	 * @param high - upper bounds of area
	 * @return - All PointKD between low and high.
	 */
	public Item[] getRange(double[] low, double[] high) {
		return root.range(high, low);
	}

	/**
	 * Gets the N nearest neighbors to the given key.
	 * @param key - Key
	 * @param num - Number of results
	 * @return Array of Item Objects, distances within the items
	 * are the square of the actual distance between them and the key
	 */
	public Item[] getNearestNeighbor(double[] key, int num) {
		ShiftArray arr = new ShiftArray(num);
		root.nearestn(arr, key);
		return arr.getArray();
	}
	/**
	 * Compares arrays of double and returns the euclidean distance
	 * between them.
	 *
	 * @param a - The first set of numbers
	 * @param b - The second set of numbers
	 * @return The distance squared between <b>a</b> and <b>b</b>.
	 */
	public static final double distance(double[] a, double[] b) {
		double total = 0;
		for (int i = 0; i < a.length; ++i)
			total += (b[i] - a[i]) * (b[i] - a[i]);
		return Math.sqrt(total);
	}
	/**
	 * Compares arrays of double and returns the squared euclidean distance
	 * between them.
	 *
	 * @param a - The first set of numbers
	 * @param b - The second set of numbers
	 * @return The distance squared between <b>a</b> and <b>b</b>.
	 */
	public static final double distanceSq(double[] a, double[] b) {
		double total = 0;
		for (int i = 0; i < a.length; ++i)
			total += (b[i] - a[i]) * (b[i] - a[i]);
		return total;
	}

	//Internal tree node
	private class NodeKD {
		private KDTreeC owner;
		private NodeKD left, right;
		private double[] upper, lower;
		private Item[] bucket;
		private int current, dim;
		private double slice;

		//note: we always start as a bucket
		private NodeKD(KDTreeC own) {
			owner = own;
			upper = lower = null;
			left = right = null;
			bucket = new Item[own.bucket_size];
			current = 0;
			dim = 0;
		}
		//when we create non-root nodes within this class
		//we use this one here
		private NodeKD(NodeKD node) {
			owner = node.owner;
			dim = node.dim + 1;
			bucket = new Item[owner.bucket_size];
			if(dim + 1 > owner.dimensions) dim = 0;
			left = right = null;
			upper = lower = null;
			current = 0;
		}
		//what it says on the tin
		private void add(Item m) {
			if(bucket == null) {
				//Branch
				if(m.pnt[dim] > slice)
					right.add(m);
				else left.add(m);
			} else {
				//Bucket
				if(current+1>owner.bucket_size) {
					split(m);
					return;
				}
				bucket[current++] = m;
			}
			expand(m.pnt);
		}
		//nearest neighbor thing
		private void nearestn(ShiftArray arr, double[] data) {
			if(bucket == null) {
				//Branch
				if(data[dim] > slice) {
					right.nearestn(arr, data);
					if(left.current != 0) {
						if(KDTreeC.distanceSq(left.nearestRect(data),data)
								< arr.getLongest()) {
							left.nearestn(arr, data);
						}
					}

				} else {
					left.nearestn(arr, data);
					if(right.current != 0) {
						if(KDTreeC.distanceSq(right.nearestRect(data),data)
								< arr.getLongest()) {
							right.nearestn(arr, data);
						}
					}
				}
			} else {
				//Bucket
				for(int i = 0; i < current; i++) {
					bucket[i].distance = KDTreeC.distanceSq(bucket[i].pnt, data);
					arr.add(bucket[i]);
				}
			}
		}
		//gets all items from within a range
		private Item[] range(double[] upper, double[] lower) {
			//TODO: clean this up a bit
			if(bucket == null) {
				//Branch
				Item[] tmp = new Item[0];
				if (intersects(upper,lower,left.upper,left.lower)) {
					Item[] tmpl = left.range(upper,lower);
					if(0 == tmp.length)
						tmp = tmpl;
				}
				if (intersects(upper,lower,right.upper,right.lower)) {
					Item[] tmpr = right.range(upper,lower);
					if (0 == tmp.length)
						tmp = tmpr;
					else if (0 < tmpr.length) {
						Item[] tmp2 = new Item[tmp.length + tmpr.length];
						System.arraycopy(tmp, 0, tmp2, 0, tmp.length);
						System.arraycopy(tmpr, 0, tmp2, tmp.length, tmpr.length);
						tmp = tmp2;
					}
				}
				return tmp;
			}
			//Bucket
			Item[] tmp = new Item[current];
			int n = 0;
			for (int i = 0; i < current; i++) {
				if (contains(upper, lower, bucket[i].pnt)) {
					tmp[n++] = bucket[i];
				}
			}
			Item[] tmp2 = new Item[n];
			System.arraycopy(tmp, 0, tmp2, 0, n);
			return tmp2;
		}

		//These are helper functions from here down
		//check if this hyper rectangle contains a give hyper-point
		public boolean contains(double[] upper, double[] lower, double[] point) {
			if(current == 0) return false;
			for(int i=0; i<point.length; ++i) {
				if(point[i] > upper[i] ||
					point[i] < lower[i])
						return false;
			}
			return true;
		}
		//checks if two hyper-rectangles intersect
		public boolean intersects(double[] up0, double[] low0,
				double[] up1, double[] low1) {
			for(int i=0; i<up0.length; ++i) {
				if(up1[i] < low0[i] || low1[i] > up0[i]) return false;
			}
			return true;
		}
		//splits a bucket into a branch
		private void split(Item m) {
			//split based on our bound data
			slice = (upper[dim]+lower[dim])/2.0;
			left = new NodeKD(this);
			right = new NodeKD(this);
			for(int i=0; i<current; ++i) {
				if(bucket[i].pnt[dim] > slice)
					right.add(bucket[i]);
				else left.add(bucket[i]);
			}
			bucket = null;
			add(m);
		}
		//gets nearest point to data within this hyper rectangle
		private double[] nearestRect(double[] data) {
			double[] nearest = data.clone();
			for(int i = 0; i < data.length; ++i) {
				if(nearest[i] > upper[i]) nearest[i] = upper[i];
				if(nearest[i] < lower[i]) nearest[i] = lower[i];
			}
			return nearest;
		}
		//expands this hyper rectangle
		private void expand(double[] data) {
			if(upper == null) {
				upper = Arrays.copyOf(data, owner.dimensions);
				lower = Arrays.copyOf(data, owner.dimensions);
				return;
			}
			for(int i=0; i<data.length; ++i) {
				if(upper[i] < data[i]) upper[i] = data[i];
				if(lower[i] > data[i]) lower[i] = data[i];
			}
		}
	}
	//A simple shift array that sifts data up
	//as we add new ones to lower in the array.
	private class ShiftArray {
		private Item[] items;
		private final int max;
		private int current;
		private ShiftArray(int maximum) {
			max = maximum;
			current = 0;
			items = new Item[max];
		}
		private void add(Item m) {
			int i;
			for(i=current;i>0&&items[i-1].distance >  m.distance; --i);
			if(i >= max) return;
			if(current < max) ++current;
			System.arraycopy(items, i, items, i+1, current-(i+1));
			items[i] = m;
		}
		private double getLongest() {
			if (current < max) return Double.POSITIVE_INFINITY;
			return items[max-1].distance;
		}
		private Item[] getArray() {
			return items.clone();
		}
	}
}
=======
// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

/**
 *
 * @author sk89q
 */
public class Vector2D {
    protected final double x, z;

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(double x, double z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(int x, int z) {
        this.x = (double) x;
        this.z = (double) z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param x
     * @param z
     */
    public Vector2D(float x, float z) {
        this.x = (double) x;
        this.z = (double) z;
    }

    /**
     * Construct the Vector2D object.
     *
     * @param pt
     */
    public Vector2D(Vector2D pt) {
        this.x = pt.x;
        this.z = pt.z;
    }

    /**
     * Construct the Vector2D object.
     */
    public Vector2D() {
        this.x = 0;
        this.z = 0;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @return the x
     */
    public int getBlockX() {
        return (int) Math.round(x);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector2D setX(double x) {
        return new Vector2D(x, z);
    }

    /**
     * Set X.
     *
     * @param x
     * @return new vector
     */
    public Vector2D setX(int x) {
        return new Vector2D(x, z);
    }

    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * @return the z
     */
    public int getBlockZ() {
        return (int) Math.round(z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector2D setZ(double z) {
        return new Vector2D(x, z);
    }

    /**
     * Set Z.
     *
     * @param z
     * @return new vector
     */
    public Vector2D setZ(int z) {
        return new Vector2D(x, z);
    }

    /**
     * Adds two points.
     *
     * @param other
     * @return New point
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, z + other.z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D add(double x, double z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    /**
     * Adds two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D add(int x, int z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    /**
     * Adds points.
     *
     * @param others
     * @return New point
     */
    public Vector2D add(Vector2D... others) {
        double newX = x, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX += others[i].x;
            newZ += others[i].z;
        }
        return new Vector2D(newX, newZ);
    }

    /**
     * Subtracts two points.
     *
     * @param other
     * @return New point
     */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, z - other.z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D subtract(double x, double z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    /**
     * Subtract two points.
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D subtract(int x, int z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    /**
     * Subtract points.
     *
     * @param others
     * @return New point
     */
    public Vector2D subtract(Vector2D... others) {
        double newX = x, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX -= others[i].x;
            newZ -= others[i].z;
        }
        return new Vector2D(newX, newZ);
    }

    /**
     * Component-wise multiplication
     *
     * @param other
     * @return New point
     */
    public Vector2D multiply(Vector2D other) {
        return new Vector2D(x * other.x, z * other.z);
    }

    /**
     * Component-wise multiplication
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D multiply(double x, double z) {
        return new Vector2D(this.x * x, this.z * z);
    }

    /**
     * Component-wise multiplication
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D multiply(int x, int z) {
        return new Vector2D(this.x * x, this.z * z);
    }

    /**
     * Component-wise multiplication
     *
     * @param others
     * @return New point
     */
    public Vector2D multiply(Vector2D... others) {
        double newX = x, newZ = z;

        for (int i = 0; i < others.length; ++i) {
            newX *= others[i].x;
            newZ *= others[i].z;
        }
        return new Vector2D(newX, newZ);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector2D multiply(double n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector2D multiply(float n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Scalar multiplication.
     *
     * @param n
     * @return New point
     */
    public Vector2D multiply(int n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    /**
     * Component-wise division
     *
     * @param other
     * @return New point
     */
    public Vector2D divide(Vector2D other) {
        return new Vector2D(x / other.x, z / other.z);
    }

    /**
     * Component-wise division
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D divide(double x, double z) {
        return new Vector2D(this.x / x, this.z / z);
    }

    /**
     * Component-wise division
     *
     * @param x
     * @param y
     * @param z
     * @return New point
     */
    public Vector2D divide(int x, int z) {
        return new Vector2D(this.x / x, this.z / z);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector2D divide(int n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector2D divide(double n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Scalar division.
     *
     * @param n
     * @return new point
     */
    public Vector2D divide(float n) {
        return new Vector2D(x / n, z / n);
    }

    /**
     * Get the length of the vector.
     *
     * @return length
     */
    public double length() {
        return Math.sqrt(x * x + z * z);
    }

    /**
     * Get the length^2 of the vector.
     *
     * @return length^2
     */
    public double lengthSq() {
        return x * x + z * z;
    }

    /**
     * Get the distance away from a point.
     *
     * @param pt
     * @return distance
     */
    public double distance(Vector2D pt) {
        return Math.sqrt(Math.pow(pt.x - x, 2) +
                Math.pow(pt.z - z, 2));
    }

    /**
     * Get the distance away from a point, squared.
     *
     * @param pt
     * @return distance
     */
    public double distanceSq(Vector2D pt) {
        return Math.pow(pt.x - x, 2) +
                Math.pow(pt.z - z, 2);
    }

    /**
     * Get the normalized vector.
     *
     * @return vector
     */
    public Vector2D normalize() {
        return divide(length());
    }

    /**
     * Gets the dot product of this and another vector.
     *
     * @param other
     * @return the dot product of this and the other vector
     */
    public double dot(Vector2D other) {
        return x * other.x + z * other.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithin(Vector2D min, Vector2D max) {
        return x >= min.x && x <= max.x
                && z >= min.z && z <= max.z;
    }

    /**
     * Checks to see if a vector is contained with another.
     *
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithinBlock(Vector2D min, Vector2D max) {
        return getBlockX() >= min.getBlockX() && getBlockX() <= max.getBlockX()
                && getBlockZ() >= min.getBlockZ() && getBlockZ() <= max.getBlockZ();
    }

    /**
     * Rounds all components down.
     *
     * @return
     */
    public Vector2D floor() {
        return new Vector2D(Math.floor(x), Math.floor(z));
    }

    /**
     * Rounds all components up.
     *
     * @return
     */
    public Vector2D ceil() {
        return new Vector2D(Math.ceil(x), Math.ceil(z));
    }

    /**
     * Rounds all components to the closest integer.<br>
     *<br>
     * Components < 0.5 are rounded down, otherwise up
     *
     * @return
     */
    public Vector2D round() {
        return new Vector2D(Math.floor(x + 0.5), Math.floor(z + 0.5));
    }

    /**
     * 2D transformation.
     *
     * @param angle in degrees
     * @param aboutX about which x coordinate to rotate
     * @param aboutZ about which z coordinate to rotate
     * @param translateX what to add after rotation
     * @param translateZ what to add after rotation
     * @return
     */
    public Vector2D transform2D(double angle,
            double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);
        return new Vector2D(
            x2 + aboutX + translateX,
            z2 + aboutZ + translateZ
        );
    }

    public boolean isCollinearWith(Vector2D other) {
        if (x == 0 && z == 0) {
            // this is a zero vector
            return true;
        }

        final double otherX = other.x;
        final double otherZ = other.z;

        if (otherX == 0 && otherZ == 0) {
            // other is a zero vector
            return true;
        }

        if ((x == 0) != (otherX == 0)) return false;
        if ((z == 0) != (otherZ == 0)) return false;

        final double quotientX = otherX / x;
        if (!Double.isNaN(quotientX)) {
            return other.equals(multiply(quotientX));
        }

        final double quotientZ = otherZ / z;
        if (!Double.isNaN(quotientZ)) {
            return other.equals(multiply(quotientZ));
        }

        throw new RuntimeException("This should not happen");
    }

    /**
     * Gets a BlockVector version.
     *
     * @return BlockVector
     */
    public BlockVector2D toBlockVector2D() {
        return new BlockVector2D(this);
    }

    /**
     * Checks if another object is equivalent.
     *
     * @param obj
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2D)) {
            return false;
        }

        Vector2D other = (Vector2D) obj;
        return other.x == this.x && other.z == this.z;

    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return ((new Double(x)).hashCode() >> 13) ^
                (new Double(z)).hashCode();
    }

    /**
     * Returns string representation "(x, y, z)".
     *
     * @return string
     */
    @Override
    public String toString() {
        return "(" + x + ", " + z + ")";
    }

    /**
     * Creates a 3D vector by adding a zero Y component to this vector.
     *
     * @return Vector
     */
    public Vector toVector() {
        return new Vector(x, 0, z);
    }

    /**
     * Creates a 3D vector by adding the specified Y component to this vector.
     *
     * @return Vector
     */
    public Vector toVector(double y) {
        return new Vector(x, y, z);
    }

    /**
     * Gets the minimum components of two vectors.
     *
     * @param v1
     * @param v2
     * @return minimum
     */
    public static Vector2D getMinimum(Vector2D v1, Vector2D v2) {
        return new Vector2D(
            Math.min(v1.x, v2.x),
            Math.min(v1.z, v2.z)
        );
    }

    /**
     * Gets the maximum components of two vectors.
     *
     * @param v1
     * @param v2
     * @return maximum
     */
    public static Vector2D getMaximum(Vector2D v1, Vector2D v2) {
        return new Vector2D(
            Math.max(v1.x, v2.x),
            Math.max(v1.z, v2.z)
        );
    }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
