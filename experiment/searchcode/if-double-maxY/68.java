/*******************************************************************************
 * Copyright (c) 2012 Danilo Pianini.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
/**
 * 
 */
package it.unibo.alchemist.model.implementations.environments;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import org.danilopianini.concurrency.FastReadWriteLock;

import it.unibo.alchemist.core.implementations.Simulation;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.IAutoLinker;
import it.unibo.alchemist.model.interfaces.INeighborhood;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.utils.ArrayUtils;
import it.unibo.alchemist.utils.MathUtils;

/**
 * This class is meant to provide a 2D dynamic environment.
 * 
 * @author Danilo Pianini
 * 
 * @param <T>
 *            concentration type
 */
public class Continuous2DEuclideanDistanceAutolink<T> extends
		AbstractEnvironment<Double, Double, T> {

	private static final long serialVersionUID = 1991261960773282992L;
	private final double min, max;
	private final FastReadWriteLock rwLock = new FastReadWriteLock();
	private double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE,
			minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
	private final TIntObjectHashMap<INeighborhood<Double, T>> neighCache = new TIntObjectHashMap<>();
	private String separator = System.getProperty("line.separator");
	private IAutoLinker<T, Double> autolinker;

	/**
	 * Builds a new, empty environment.
	 * 
	 * @param minDist
	 *            if the distance between two nodes falls under this threshold,
	 *            the nodes cannot be considered neighbors
	 * @param maxDist
	 *            if the distance between two nodes raises over this threshold,
	 *            the nodes cannot be considered neighbors
	 */
	public Continuous2DEuclideanDistanceAutolink(final double minDist,
			final double maxDist) {
		autolinker = new EuclideanAutoLinker<T, Double, Double>(this,
				this.neighCache, minDist, maxDist);
		min = minDist;
		max = maxDist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * alice.alchemist.model.interfaces.IEnvironment#addNode(alice.alchemist
	 * .model.interfaces.INode, alice.alchemist.model.interfaces.IPosition)
	 */
	@Override
	public void addNode(final INode<T> node, final IPosition<Double, Double> p) {
		rwLock.write();
		addNodeInternally(node, p);
		/*
		 * Size updates
		 */
		Double[] coord = p.getCartesianCoordinates();
		double x = coord[0];
		double y = coord[1];
		includeObject(x, x, y, y);
		/*
		 * Neighborhood computation
		 */
		INeighborhood<Double, T> currn = autolinker.computeNeighborhood(node); // computeEnvironment(node,
																				// p);
		/*
		 * Reaction and dependencies creation on the engine. This must be
		 * executed only when the neighborhoods have been correctly computed.
		 */
		Simulation.nodeAdded(this, node);

		for (INode<T> n : currn.getNeighbors()) {
			Simulation.neighborAdded(this, node, n);
			Simulation.neighborAdded(this, n, node);
		}

		rwLock.release();
	}

	// /**
	// * Produces a new neighborhood after a node movement operation.
	// *
	// * @param center
	// * the node to recompute
	// * @param p
	// * its new position
	// * @return the new neighborhood
	// */
	// protected INeighborhood<Double, T> computeEnvironment(final INode<T>
	// center, final IPosition<Double, Double> p) {
	// Double[] cp = p.getCartesianCoordinates();
	// ArrayList<INode<T>> neighbors = new ArrayList<INode<T>>();
	// for (INode<T> n : getNodes()) {
	// if (!n.equals(center)) {
	// double d =
	// MathUtils.getEuclideanDistance(getPosition(n).getCartesianCoordinates(),
	// cp);
	// if (d >= min && d <= max) {
	// neighbors.add(n);
	// INeighborhood<Double, T> list = neighCache.get(n.getId());
	// if (!list.contains(center)) {
	// list.addNeighbor(center);
	// }
	// }
	// }
	// }
	// INeighborhood<Double, T> n = new Neighborhood<Double, Double, T>(center,
	// neighbors, this);
	// neighCache.put(center.getId(), n);
	// return n;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IEnvironment#getDimensions()
	 */
	@Override
	public int getDimensions() {
		return 2;
	}

	/**
	 * @return The maximum distance for two nodes to become linked
	 */
	public double getMaxDistance() {
		return max;
	}

	/**
	 * @return The minimum distance for two nodes to become linked
	 */
	public double getMinDistance() {
		return min;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * alice.alchemist.model.interfaces.IEnvironment#getNeighborhood(alice.alchemist
	 * .model.interfaces.INode<T>)
	 */
	@Override
	public INeighborhood<Double, T> getNeighborhood(final INode<T> center) {
		return neighCache.get(center.getId());
	}

	/**
	 * @return a pointer to the neighborhoods cache structure
	 */
	protected TIntObjectHashMap<INeighborhood<Double, T>> getNeighborsCache() {
		return neighCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IEnvironment#getOffset()
	 */
	@Override
	public Double[] getOffset() {
		return new Double[] { minX, minY };
	}

	/**
	 * @return the separator used in toString()
	 */
	public String getSeparator() {
		return separator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IEnvironment#getSize()
	 */
	@Override
	public Double[] getSize() {
		return new Double[] { maxX - minX, maxY - minY };
	}

	/**
	 * Allows to extend the size of the environment by adding some non-node
	 * object.
	 * 
	 * @param startx
	 *            minimum x position of the object
	 * @param endx
	 *            maximum x position of the object
	 * @param starty
	 *            minimum y position of the object
	 * @param endy
	 *            maximum y position of the object
	 */
	protected void includeObject(final double startx, final double endx,
			final double starty, final double endy) {
		if (startx < minX) {
			minX = startx;
		}
		if (starty < minY) {
			minY = starty;
		}
		if (endx > maxX) {
			maxX = endx;
		}
		if (endy > maxY) {
			maxY = endy;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * alice.alchemist.model.interfaces.IEnvironment#moveNode(alice.alchemist
	 * .model.interfaces.INode<T>, alice.alchemist.model.interfaces.IPosition)
	 */
	@Override
	public void moveNode(final INode<T> node,
			final IPosition<Double, Double> direction) {
		rwLock.write();
		/*
		 * New environment size computation
		 */
		Double[] oldcoord = getAndDeletePosition(node)
				.getCartesianCoordinates();
		Double[] newcoord = direction.getCartesianCoordinates();
		double x = oldcoord[0] + newcoord[0];
		double y = oldcoord[1] + newcoord[1];
		if (x < minX) {
			minX = x;
		}
		if (x > maxX) {
			maxX = x;
		}
		if (y < minY) {
			minY = y;
		}
		if (y > maxY) {
			maxY = y;
		}
		IPosition<Double, Double> newpos = new Continuous2DEuclidean(x, y);
		setPosition(node, newpos);
		/*
		 * The following optimization allows to define as local the context of
		 * reactions which are actually including a move, which should be
		 * normally considered global. This because for each node wich is
		 * detached, all the dependencies are updated, ensuring the soundness.
		 */
		List<? extends INode<T>> oldclone = ArrayUtils.cloneList(neighCache
				.get(node.getId()).getNeighbors());
		autolinker.computeNeighborhood(node); // computeEnvironment(node,
												// newpos);
		List<? extends INode<T>> newlist = ArrayUtils.cloneList(neighCache.get(
				node.getId()).getNeighbors());
		for (INode<T> n : oldclone) {
			if (!newlist.contains(n)) {
				Simulation.neighborRemoved(this, node, n);
				Simulation.neighborRemoved(this, n, node);
			}
		}
		for (INode<T> n : newlist) {
			if (!oldclone.contains(n)) {
				Simulation.neighborAdded(this, node, n);
				Simulation.neighborAdded(this, n, node);
			}
		}
		rwLock.release();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * alice.alchemist.model.interfaces.IEnvironment#removeNode(alice.alchemist
	 * .model.interfaces.INode<T>)
	 */
	@Override
	public void removeNode(final INode<T> node) {
		rwLock.write();
		super.removeNode(node);
		/*
		 * Neighborhood update
		 */
		INeighborhood<Double, T> neigh = neighCache.remove(node.getId());
		for (INode<T> n : neigh) {
			neighCache.get(n.getId()).getNeighbors().remove(node);
		}
		/*
		 * Update all the reactions which may have been affected by the node
		 * removal
		 */
		Simulation.nodeRemoved(this, node, neigh);
		rwLock.release();
	}

	/**
	 * @param s
	 *            the new separator
	 */
	public void setSeparator(final String s) {
		separator = s;
	}

	/**
	 * @param cp
	 * 			?
	 * @param center
	 * 			first node
	 * @param node
	 * 			second node
	 * @return
	 */
	protected boolean areNeighbors(final Double[] cp, final INode<T> center,
			final INode<T> node) {
		final double dist = MathUtils.getEuclideanDistance(
				this.getPosition(center).getCartesianCoordinates(), this
						.getPosition(node).getCartesianCoordinates());
		return dist <= this.getMinDistance() && dist <= this.getMaxDistance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (INode<T> n : this) {
			sb.append(n + separator);
		}
		return sb.toString();
	}

}

