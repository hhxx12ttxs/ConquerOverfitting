<<<<<<< HEAD
package com.clockworkaphid.landscape;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Landscape implements Serializable, Iterable<NodeStateWrapper> {

	/** */
	private static final long serialVersionUID = -2332628032435782355L;

	/** Map holding all of the layers which make up the landscape. */
	protected final Int2ObjectMap<Int2ObjectMap<Int2ObjectMap<NodeStateWrapper>>> nodesByPosition;
	/** Ordered list of the levels of the layers, which match the denominators of the fractions used to index the individual nodes in the layers. */
	protected final IntList denominators;
	protected final IntComparator denominatorsComparator;
	protected final Swapper denominatorsSwapper;

	/** Map holding all of the nodes in the landscape by their level of granularity (the amount of detail they represent. */
	protected final Float2ObjectOpenHashMap<ArrayList<NodeStateWrapper>> nodesByGranularity;
	/** Ordered list of the available granularities. */
	protected final FloatList granularities;
	protected final IntComparator granularitiesComparator;
	protected final Swapper granularitiesSwapper;

	protected int totalNodes;

	public Landscape() {
		super();

		this.nodesByPosition = new Int2ObjectOpenHashMap<Int2ObjectMap<Int2ObjectMap<NodeStateWrapper>>>();
		this.denominators = new IntArrayList();
		this.denominatorsComparator = new AbstractIntComparator() {
			@Override
			public int compare(int a, int b) {
				return denominators.getInt(a) - denominators.getInt(b);
			}
		};
		this.denominatorsSwapper = new Swapper() {
			private int spare;
			@Override
			public void swap(int a, int b) {
				spare = denominators.getInt(a);
				denominators.set(a, denominators.getInt(b));
				denominators.set(b, spare);
			}
		};

		this.nodesByGranularity = new Float2ObjectOpenHashMap<ArrayList<NodeStateWrapper>>();
		this.granularities = new FloatArrayList();
		this.granularitiesComparator = new AbstractIntComparator() {
			@Override
			public int compare(int a, int b) {
				return (int) (granularities.getFloat(a) - granularities.getFloat(b));
			}
		};
		this.granularitiesSwapper = new Swapper() {
			private float spare;
			@Override
			public void swap(int a, int b) {
				spare = granularities.getFloat(a);
				granularities.set(a, granularities.getFloat(b));
				granularities.set(b, spare);
			}
		};

		this.totalNodes = 0;
	}

	public NodeStateWrapper getNode(int eastNumerator, int northNumerator, int denominator) {

		final int commonDenominator = getLowestCommonDenominator(eastNumerator, denominator, northNumerator, denominator);

		final Int2ObjectMap<Int2ObjectMap<NodeStateWrapper>> layer = this.nodesByPosition.get(commonDenominator);

		if (layer == null) return null;

		// Or maybe row?
		final Int2ObjectMap<NodeStateWrapper> column = layer.get(eastNumerator * commonDenominator / denominator);

		if (column == null) return null;

		return column.get(northNumerator * commonDenominator / denominator);
	}

	public void addNode(Node node) {

		Int2ObjectMap<Int2ObjectMap<NodeStateWrapper>> layer = this.nodesByPosition.get(node.getDenominator());

		if (layer == null) {

			layer = new Int2ObjectOpenHashMap<Int2ObjectMap<NodeStateWrapper>>();
			this.nodesByPosition.put(node.getDenominator(), layer);

			this.denominators.add(node.getDenominator());
			Arrays.quickSort(0, denominators.size(), denominatorsComparator, denominatorsSwapper);
			System.out.println("denominators: " + this.denominators);
		}

		Int2ObjectMap<NodeStateWrapper> column = layer.get(node.getEastNumerator());

		if (column == null) {

			column = new Int2ObjectOpenHashMap<NodeStateWrapper>();
			layer.put(node.getEastNumerator(), column);
		}

		NodeStateWrapper wrappedNode = new NodeStateWrapper(node);
		
		column.put(node.getNorthNumerator(), wrappedNode);

		ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(node.getGranularity());

		if (nodes == null) {

			nodes = new ArrayList<NodeStateWrapper>();
			this.nodesByGranularity.put(node.getGranularity(), nodes);

			this.granularities.add(0, node.getGranularity());
			//TODO I'm hacking around, but I think there's some chance that this doesn't actually work
			Arrays.quickSort(0, granularities.size(), granularitiesComparator, granularitiesSwapper);
		}

		nodes.add(wrappedNode);

		this.totalNodes++;
	}
	
	protected void getParentWrappers(Node node, Map<Direction, NodeStateWrapper> parents) {
		
		for (Direction parentDiection: node.isDiamond ? Direction.DIAMOND_PARENTS : Direction.SQUARE_PARENTS) {	
			NodeStateWrapper parent = getNode(node.getEastNumerator()+parentDiection.eastOffset, node.getNorthNumerator()+parentDiection.northOffset, node.getDenominator());
			if (parent != null) {
				parents.put(parentDiection, parent);
			}
		}
	}
	
	protected void getParents(Node node, Map<Direction, Node> parents) {
		
		for (Direction parentDiection: node.isDiamond ? Direction.DIAMOND_PARENTS : Direction.SQUARE_PARENTS) {	
			NodeStateWrapper parent = getNode(node.getEastNumerator()+parentDiection.eastOffset, node.getNorthNumerator()+parentDiection.northOffset, node.getDenominator());
			if (parent != null) {
				parents.put(parentDiection, parent.node);
			}
		}
	}

	/**
	 * Calculate and return the lowest valid common denominator for the two stated fractions.
	 * 
	 * @param eastNumerator
	 * @param eastDenominator
	 * @param northNumerator
	 * @param northDenominator
	 * @return
	 */
	protected int getLowestCommonDenominator(int eastNumerator, int eastDenominator, int northNumerator, int northDenominator) {

		for (int i = 0; i < this.denominators.size(); i++) {

			int denominator = this.denominators.get(i);

			if ((eastNumerator == 0 || eastNumerator * denominator % eastDenominator == 0) && (northNumerator == 0 || northNumerator * denominator % northDenominator == 0)) return denominator;
		}

		return -1;
	}

	public void handleVertices(FloatBuffer vertices, FloatBuffer colours, float x, float y, float z) {

		for (int i = 0; i < this.granularities.size(); i++) {

			final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(i));

			for (int j = 0; j < nodes.size(); j++) {

				NodeStateWrapper node = nodes.get(j);
				if (node.isActive(x, y, z)) node.handleVertices(vertices, colours);
			}
		}
	}

	public void handleVertices(FloatBuffer vertices, FloatBuffer colours) {

		for (int i = 0; i < this.granularities.size(); i++) {

			final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(i));

			for (int j = 0; j < nodes.size(); j++) {

				nodes.get(j).handleVertices(vertices, colours);
			}
		}
	}

	public void handleLines(FloatBuffer vertices, FloatBuffer colours) {

		final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(0));

		for (int j = 0; j < nodes.size(); j++) {

			nodes.get(j).handleLines(this, vertices, colours);
		}
	}

	public void handleLines(FloatBuffer lineVertices, FloatBuffer lineColours, float x, float y, float z, boolean simplify, int startingLod) {

		if (!simplify) {

			final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(Math.min(startingLod, this.granularities.size() - 1)));

			for (int j = 0; j < nodes.size(); j++) {

				nodes.get(j).reset();
				nodes.get(j).handleLines(this, lineVertices, lineColours);
			}
			
		} else {

			for (int i = Math.min(startingLod, this.granularities.size() - 1); i < this.granularities.size() - 1; i++) {
				
				final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(i));

				for (int j = startingLod; j < nodes.size(); j++) {

					nodes.get(j).reset();
				}
			}
			
			for (int i = Math.min(startingLod, this.granularities.size() - 2); i < this.granularities.size()-1; i++) {
				
				final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(i));

				for (int j = 0; j < nodes.size(); j++) {

					NodeStateWrapper node = nodes.get(j);
					if (node.isActive(x, y, z)) node.handleLines(this, lineVertices, lineColours);
				}
			}
		}
	}

	public void handlePolygons(FloatBuffer vertices, FloatBuffer colours, IntBuffer indices, AtomicInteger counter, float x, float y, float z, float tolerance, boolean simplify, int startingLod) {

		if (!simplify) {

			final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(Math.min(startingLod, this.granularities.size() - 1)));

			for (int j = 0; j < nodes.size(); j++) {

				nodes.get(j).reset();
				nodes.get(j).handlePolygons(this, vertices, colours, indices, counter);
			}
			
		} else {

			for (int i = Math.min(startingLod, this.granularities.size()); i < this.granularities.size(); i++) {
				
				final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(i));

				for (int j = startingLod; j < nodes.size(); j++) {

					nodes.get(j).reset();
				}
			}
			
			for (int i = Math.min(startingLod, this.granularities.size() - 2); i < this.granularities.size()-1; i++) {
				final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(i));

				for (int j = 0; j < nodes.size(); j++) {

					NodeStateWrapper node = nodes.get(j);
					if (node.isActive(x, y, z)) node.handlePolygons(this, vertices, colours, indices, counter);
				}
			}
		}
	}

	public void handlePolygonsAndLines(FloatBuffer polygonVertices, FloatBuffer polygonColours, IntBuffer polygonIndices, AtomicInteger polygonCounter,
			FloatBuffer lineVertices, FloatBuffer lineColours, float x, float y, float z, boolean simplify, int startingLod) {

		for (int i = Math.min(startingLod, this.granularities.size()); i < this.granularities.size(); i++) {
			
			final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(i));

			for (int j = startingLod; j < nodes.size(); j++) {

				nodes.get(j).reset();
			}
		}
		
		for (int i = Math.min(startingLod, this.granularities.size() - 2); i < this.granularities.size()-1; i++) {
			final ArrayList<NodeStateWrapper> nodes = this.nodesByGranularity.get(this.granularities.get(i));
			for (int j = 0; j < nodes.size(); j++) {
				NodeStateWrapper node = nodes.get(j);
				if (!simplify || node.isActive(x, y, z)) {
					node.handlePolygons(this, polygonVertices, polygonColours, polygonIndices, polygonCounter);
					node.handleLines(this, lineVertices, lineColours);
				}
			}
		}
	}

	public int getTotalNodes() {

		return this.totalNodes;
	}

	public int getNodesAtHighestDetail() {

		return this.nodesByGranularity.get(this.granularities.get(0)).size();
	}

	public Iterator<NodeStateWrapper> iterator() {

		return new LandscapeNodeIterator();
	}

	// TODO Does not check for changes to underlying data
	/**
	 * 
	 */
	protected class LandscapeNodeIterator implements Iterator<NodeStateWrapper> {

		private int granularity;
		private int node;
		private ArrayList<NodeStateWrapper> currentNodes;

		public LandscapeNodeIterator() {

			this.granularity = 0;
			this.node = 0;
			this.currentNodes = nodesByGranularity.get(granularities.get(granularity));
		}

		@Override
		public boolean hasNext() {

			return currentNodes != null;
		}

		@Override
		public NodeStateWrapper next() {

			final NodeStateWrapper theNode = this.currentNodes.get(node++);

			if (node >= this.currentNodes.size()) {

				granularity++;
				node = 0;
				this.currentNodes = granularity < granularities.size() ? nodesByGranularity.get(granularities.get(granularity)) : null;
			}

			return theNode;
		}

		@Override
		public void remove() {

			// Not supported
		}
	}
=======
/*
 * Copyright 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.pdf417.decoder.ec;

import com.google.zxing.ChecksumException;

/**
 * <p>PDF417 error correction implementation.</p>
 *
 * <p>This <a href="http://en.wikipedia.org/wiki/Reed%E2%80%93Solomon_error_correction#Example">example</a>
 * is quite useful in understanding the algorithm.</p>
 *
 * @author Sean Owen
 * @see com.google.zxing.common.reedsolomon.ReedSolomonDecoder
 */
public final class ErrorCorrection {

  private final ModulusGF field;

  public ErrorCorrection() {
    this.field = ModulusGF.PDF417_GF;
  }

  /**
   * @return number of errors
   */
  public int decode(int[] received,
                    int numECCodewords,
                    int[] erasures) throws ChecksumException {

    ModulusPoly poly = new ModulusPoly(field, received);
    int[] S = new int[numECCodewords];
    boolean error = false;
    for (int i = numECCodewords; i > 0; i--) {
      int eval = poly.evaluateAt(field.exp(i));
      S[numECCodewords - i] = eval;
      if (eval != 0) {
        error = true;
      }
    }

    if (!error) {
      return 0;
    }

    ModulusPoly knownErrors = field.getOne();
    for (int erasure : erasures) {
      int b = field.exp(received.length - 1 - erasure);
      // Add (1 - bx) term:
      ModulusPoly term = new ModulusPoly(field, new int[] { field.subtract(0, b), 1 });
      knownErrors = knownErrors.multiply(term);
    }

    ModulusPoly syndrome = new ModulusPoly(field, S);
    //syndrome = syndrome.multiply(knownErrors);

    ModulusPoly[] sigmaOmega =
        runEuclideanAlgorithm(field.buildMonomial(numECCodewords, 1), syndrome, numECCodewords);
    ModulusPoly sigma = sigmaOmega[0];
    ModulusPoly omega = sigmaOmega[1];

    //sigma = sigma.multiply(knownErrors);

    int[] errorLocations = findErrorLocations(sigma);
    int[] errorMagnitudes = findErrorMagnitudes(omega, sigma, errorLocations);

    for (int i = 0; i < errorLocations.length; i++) {
      int position = received.length - 1 - field.log(errorLocations[i]);
      if (position < 0) {
        throw ChecksumException.getChecksumInstance();
      }
      received[position] = field.subtract(received[position], errorMagnitudes[i]);
    }
    return errorLocations.length;
  }

  private ModulusPoly[] runEuclideanAlgorithm(ModulusPoly a, ModulusPoly b, int R)
      throws ChecksumException {
    // Assume a's degree is >= b's
    if (a.getDegree() < b.getDegree()) {
      ModulusPoly temp = a;
      a = b;
      b = temp;
    }

    ModulusPoly rLast = a;
    ModulusPoly r = b;
    ModulusPoly tLast = field.getZero();
    ModulusPoly t = field.getOne();

    // Run Euclidean algorithm until r's degree is less than R/2
    while (r.getDegree() >= R / 2) {
      ModulusPoly rLastLast = rLast;
      ModulusPoly tLastLast = tLast;
      rLast = r;
      tLast = t;

      // Divide rLastLast by rLast, with quotient in q and remainder in r
      if (rLast.isZero()) {
        // Oops, Euclidean algorithm already terminated?
        throw ChecksumException.getChecksumInstance();
      }
      r = rLastLast;
      ModulusPoly q = field.getZero();
      int denominatorLeadingTerm = rLast.getCoefficient(rLast.getDegree());
      int dltInverse = field.inverse(denominatorLeadingTerm);
      while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
        int degreeDiff = r.getDegree() - rLast.getDegree();
        int scale = field.multiply(r.getCoefficient(r.getDegree()), dltInverse);
        q = q.add(field.buildMonomial(degreeDiff, scale));
        r = r.subtract(rLast.multiplyByMonomial(degreeDiff, scale));
      }

      t = q.multiply(tLast).subtract(tLastLast).negative();
    }

    int sigmaTildeAtZero = t.getCoefficient(0);
    if (sigmaTildeAtZero == 0) {
      throw ChecksumException.getChecksumInstance();
    }

    int inverse = field.inverse(sigmaTildeAtZero);
    ModulusPoly sigma = t.multiply(inverse);
    ModulusPoly omega = r.multiply(inverse);
    return new ModulusPoly[]{sigma, omega};
  }

  private int[] findErrorLocations(ModulusPoly errorLocator) throws ChecksumException {
    // This is a direct application of Chien's search
    int numErrors = errorLocator.getDegree();
    int[] result = new int[numErrors];
    int e = 0;
    for (int i = 1; i < field.getSize() && e < numErrors; i++) {
      if (errorLocator.evaluateAt(i) == 0) {
        result[e] = field.inverse(i);
        e++;
      }
    }
    if (e != numErrors) {
      throw ChecksumException.getChecksumInstance();
    }
    return result;
  }

  private int[] findErrorMagnitudes(ModulusPoly errorEvaluator,
                                    ModulusPoly errorLocator,
                                    int[] errorLocations) {
    int errorLocatorDegree = errorLocator.getDegree();
    int[] formalDerivativeCoefficients = new int[errorLocatorDegree];
    for (int i = 1; i <= errorLocatorDegree; i++) {
      formalDerivativeCoefficients[errorLocatorDegree - i] =
          field.multiply(i, errorLocator.getCoefficient(i));
    }
    ModulusPoly formalDerivative = new ModulusPoly(field, formalDerivativeCoefficients);

    // This is directly applying Forney's Formula
    int s = errorLocations.length;
    int[] result = new int[s];
    for (int i = 0; i < s; i++) {
      int xiInverse = field.inverse(errorLocations[i]);
      int numerator = field.subtract(0, errorEvaluator.evaluateAt(xiInverse));
      int denominator = field.inverse(formalDerivative.evaluateAt(xiInverse));
      result[i] = field.multiply(numerator, denominator);
    }
    return result;
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

