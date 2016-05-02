package data.map.io;

import data.map.Terrain;

public class QuadTree {
	public static Node generateQuadTree(final Terrain[][] baseMap) {
		Terrain[][] expandedMap = getExpandedMap(baseMap);
		Node root = new Node(null, expandedMap, 0);
		generateTree(root);
		return root;
	}

	/**
	 * Expands the map. Quadtree encoding can only function on a n*n square grid, where n
	 * is a power of 2. This expands the Terrain[][] to that size, then copies the old
	 * data into the new array. If the old array fufills the conditions, that array may
	 * be returned instead.
	 *
	 * @param baseMap The map to take the data from
	 * @return The new, resized map
	 */
	private static Terrain[][] getExpandedMap(final Terrain[][] baseMap) {
		int width = baseMap.length; //The original width
		int height = baseMap[0].length; //The original height
		Terrain[][] terrainMap2; //Temporary Map which will be of n*n, n is some power of 2

		/*
		Check to see if either height or width is not a power of 2 and if they are equal.

		If either is not a power of two OR they are not equal then get the smallest power of two
		greater than the larger of height and width.

		Set height and width equal to this number.
		*/
		int width2; //The modified width
		int height2; //The modified height
		if(((height & (height - 1)) != 0 || (width & (width - 1)) != 0) || height != width) {
			if(height > width) {
				height2 = nearestPowerOfTwo(height);
				width2 = height2;
			} else {
				width2 = nearestPowerOfTwo(width);
				height2 = width2;
			}
		} else { //they're powers of 2
			return baseMap; //old map worked
		}
		/*
		Copy the old terrain map into the new one. It is possible to do a quadtree on this.
		*/
		terrainMap2 = new Terrain[height2][width2];

		for(int i = 0; i < baseMap.length; i++) {
			System.arraycopy(baseMap[i], 0, terrainMap2[i], 0, baseMap[i].length);
		}

		return terrainMap2;
	}

	/**
	 * Gets the next-largest power of two from a given number. 5 would give '8', 13 would give 16.
	 *
	 * @param i The initial number
	 * @return A power of two no less than the input number
	 */
	public static int nearestPowerOfTwo(final int i) {
		return (int) Math.pow(2, Math.ceil(Math.log(i) / Math.log(2)));
	}

	/**
	 * Generates a tree from the given root. This is a recursive algorithm to generate a minimized
	 * quadtree from the given input. It takes the root's quadrant, checks to see if it has to divide further,
	 * and repeats until the map is divided into squares of equal sizes.
	 * <p/>
	 * The initial node must have its quandrant set to the entire game map, in order to further divide.
	 *
	 * @param root The node to start with.
	 */
	private static void generateTree(Node root) {
		boolean singleIntensity = true;
		//Check to see if all Terrains are the exact same.
		for(int i = 0; i < root.quadrant.length && singleIntensity; i++) {
			for(int j = 0; j < root.quadrant.length && singleIntensity; j++) {
				if(!root.quadrant[0][0].equals(root.quadrant[i][j])) {
					singleIntensity = false;
				}
			}
		}

		if(singleIntensity) {
			root.type = root.quadrant[0][0];
		} else {
			//Special Case: 2x2 array
			//Children will simply be nodes typed based on the contents of each individual 2d array element
			if(root.quadrant.length == 2) {
				root.children = new Node[] {new Node(root.quadrant[0][0], root.nodeNum * 4 + 1),
						new Node(root.quadrant[0][1], root.nodeNum * 4 + 2),
						new Node(root.quadrant[1][0], root.nodeNum * 4 + 3),
						new Node(root.quadrant[1][1], root.nodeNum * 4 + 4)};
			} else {
				//In all other cases I split the array into 4 quadrants, making a node for each
				Terrain[][] quad = new Terrain[root.quadrant.length / 2][root.quadrant.length / 2];
				for(int i = 0; i < quad.length; i++) {
					System.arraycopy(root.quadrant[i], 0, quad[i], 0, quad.length);
				}
				Node c1 = new Node(null, quad, root.nodeNum * 4 + 1);

				for(int i = 0; i < quad.length; i++) {
					System.arraycopy(root.quadrant[i + quad.length], 0, quad[i], 0, quad.length);
				}
				Node c2 = new Node(null, quad, root.nodeNum * 4 + 2);

				for(int i = 0; i < quad.length; i++) {
					System.arraycopy(root.quadrant[i], quad.length, quad[i], 0, quad.length);
				}
				Node c3 = new Node(null, quad, root.nodeNum * 4 + 3);

				for(int i = 0; i < quad.length; i++) {
					System.arraycopy(root.quadrant[i + quad.length], quad.length, quad[i], 0, quad.length);
				}
				Node c4 = new Node(null, quad, root.nodeNum * 4 + 4);

				//Then I do recursion on those to generate the children for those nodes...
				generateTree(c1);
				generateTree(c2);
				generateTree(c3);
				generateTree(c4);

				//This nodes children then become those nodes
				root.children = new Node[] {c1, c2, c3, c4};
			}
		}
	}

	/**
	 * A Node in the Quadtree.
	 */
	static class Node {
		public Node[] children;
		public Terrain[][] quadrant;
		public Terrain type;
		public int nodeNum; //total number of squares contained

		public Node(Node[] children, Terrain[][] quadrant, int nodeNum) {
			this.children = children;
			this.quadrant = quadrant;
			this.nodeNum = nodeNum;
		}

		public Node(Terrain type, int nodeNum) {
			this.type = type;
			this.nodeNum = nodeNum;
		}
	}
}

