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
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.IAction;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INeighborhood;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.model.interfaces.IReaction;

import java.util.List;


/**
 * @author Danilo Pianini
 * @version 20111107
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class MASSAgent extends AbstractAction<List<Double>> {

	private static final long serialVersionUID = -4274734253286882410L;
	private static final double LIMIT = 0.2;
	private final IEnvironment<Double, Double, List<Double>> env;
	private final IMolecule field, isSensor;

	public MASSAgent(final IReaction<List<Double>> r, final IEnvironment<Double, Double, List<Double>> env, final INode<List<Double>> node, final IMolecule field, final IMolecule isSensor, final RandomEngine random) {
		super(node);
		this.env = env;
		this.field = field;
		this.isSensor = isSensor;
	}

	@Override
	public void execute() {
		final IPosition<Double, Double> mypos = env.getPosition(getNode());
		final double myx = mypos.getCartesianCoordinates()[0];
		final double myy = mypos.getCartesianCoordinates()[1];
		final INeighborhood<Double, List<Double>> neigh = env.getNeighborhood(getNode());
		final double[] data = new double[2];
		// final ArrayList<double[]> wheel = new
		// ArrayList<double[]>(neigh.getNeighbors().size());
		double totalRatio = 0;
		boolean up = true, down = true, left = true, right = true;
		/*
		 * For each neighbor
		 */
		for (final INode<List<Double>> n : neigh.getNeighbors()) {
			/*
			 * If it still have an available direction
			 */
			if (up || down || left || right) {
				/*
				 * If the neighboring node is a person
				 */
				if (n.getConcentration(isSensor) == null) {
					final IPosition<Double, Double> pos = env.getPosition(n);
					try {
						if (pos.getDistanceTo(mypos) < 0.5) {
							final double x = pos.getCartesianCoordinates()[0];
							final double y = pos.getCartesianCoordinates()[1];
							double xdist = myx - x;
							xdist *= xdist;
							double ydist = myy - y;
							ydist *= ydist;
							if (xdist > ydist) {
								if (x > myx) {
									right = false;
								} else {
									left = false;
								}
							} else {
								if (y > myy) {
									down = false;
								} else {
									up = false;
								}
							}
						}
					} catch (UncomparableDistancesException e) {
						e.printStackTrace();
					}
					/*
					 * If the node is a sensor
					 */
				} else if (n.getConcentration(field) != null) {
					double fieldConcentration = n.getConcentration(field).get(1);
					if (fieldConcentration > totalRatio) {
						final IPosition<Double, Double> pos = env.getPosition(n);
						final double x = pos.getCartesianCoordinates()[0];
						final double y = pos.getCartesianCoordinates()[1];
						data[0] = x - myx;
						data[1] = y - myy;
						totalRatio = fieldConcentration;
					}
				}
			} else {
				break;
			}
		}
		if (totalRatio > 0 && (up || down || left || right)) {
			double dx = data[0];
			double dy = data[1];
			dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
			dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);
			boolean moveH = dx > 0 && right || dx < 0 && left;
			boolean moveV = dy > 0 && down || dy < 0 && up;
			if (moveH || moveV) {
				env.moveNode(getNode(), new Continuous2DEuclidean(moveH ? dx : 0, moveV ? dy : 0));
			}
		}
	}

	@Override
	public Context getContext() {
		return Context.LOCAL;
	}

	/* (non-Javadoc)
	 * @see alice.alchemist.model.interfaces.IAction#cloneOnNewNode(alice.alchemist.model.interfaces.INode, alice.alchemist.model.interfaces.IReaction)
	 */
	@Override
	public IAction<List<Double>> cloneOnNewNode(final INode<List<Double>> n, final IReaction<List<Double>> r) {
		return null;
	}

}

