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
}

