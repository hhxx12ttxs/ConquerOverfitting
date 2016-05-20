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
import it.unibo.alchemist.model.implementations.actions.MoveNode;
import it.unibo.alchemist.model.implementations.positions.ContinuousGenericEuclidean;
import it.unibo.alchemist.model.interfaces.IAction;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.model.interfaces.IReaction;

import java.util.Collection;
import java.util.List;


/**
 * This class represents a reaction which moves the node away from neighbouring
 * nodes containing the tag molecule.
 * 
 * @author Danilo Pianini
 * @version 20110805
 * @param <D>
 * @param <Integer>
 * @param <N>
 * 
 */
public class Repulsion extends MoveNode<Double, Double, Integer> {

	private static final long serialVersionUID = 4690239358473152568L;
	private final IMolecule tag;

	/**
	 * Builds a new repulsion action.
	 * 
	 * @param env
	 *            The environment where to move
	 * @param n
	 *            The node to which this action belongs
	 * @param r
	 *            The reaction to which this action belongs
	 * @param move
	 *            A signal molecule which is useful to maintain dependencies
	 *            among reactions which operate physically on the environment
	 *            and may be influenced by the move, for instance those
	 *            conditions that check the number of neighborhoods. If no
	 *            conditions of this kind are present, just pass null.
	 * @param tag
	 *            A tag molecule: only neighbors containing tags will repel
	 */
	public Repulsion(final IEnvironment<Double, Double, Integer> env, final INode<Integer> n, final IReaction<Integer> r, final IMolecule move, final IMolecule tag) {
		super(env, n, null, move);
		this.tag = tag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.implementations.actions.MoveNode#execute()
	 */
	@Override
	public void execute() {
		final IPosition<Double, Double> pos = getEnvironment().getPosition(getNode());
		final Double[] posa = pos.getCartesianCoordinates();
		final double x = posa[0], y = posa[1];
		// List<? extends INode<Integer>> neighbors =
		// getEnvironment().getNeighborhood(getNode()).getNeighbors();
		Collection<? extends INode<Integer>> neighbors = getEnvironment().getNodes();
		double dx = 0, dy = 0;
		for (INode<Integer> n : neighbors) {
			if (n.getConcentration(tag) > 0 && n != getNode()) {
				final IPosition<Double, Double> neighpos = getEnvironment().getPosition(n);
				final Double[] neighcoor = neighpos.getCartesianCoordinates();
				try {
					double d = neighpos.getDistanceTo(pos);
					if (d < 2) {
						d *= d;
						dx += (x - neighcoor[0]) / d;
						dy += (y - neighcoor[1]) / d;
					}
				} catch (UncomparableDistancesException e) {
					e.printStackTrace();
				}
			}
		}
		final double limit = 0.1;
		dx = dx > 0 ? Math.min(dx, limit) : Math.max(dx, -limit);
		dy = dy > 0 ? Math.min(dy, limit) : Math.max(dy, -limit);
		if (dx != 0 || dy != 0) {
			setDirection(new ContinuousGenericEuclidean(new Double[] { dx, dy }));
			super.execute();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * alice.alchemist.model.implementations.actions.MoveNode#cloneOnNewNode
	 * (alice.alchemist.model.interfaces.INode,
	 * alice.alchemist.model.interfaces.IReaction)
	 */
	@Override
	public IAction<Integer> cloneOnNewNode(final INode<Integer> n, final IReaction<Integer> r) {
		return new Repulsion(getEnvironment(), n, r, getMove(), tag);
	}

}

