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
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.IAction;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INeighborhood;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.model.interfaces.IReaction;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Danilo Pianini
 * @version 20110305
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class MuseumVisitorPDABehavior implements IAction<Integer> {

	private static final long serialVersionUID = -2894220591243558470L;
	private static final double limit = 0.2;
	private static final List<IMolecule> mod = new ArrayList<IMolecule>(0);
	private final IEnvironment<Double, Double, Integer> env;
	private final INode<Integer> node;
	private final IMolecule fire, person, door, human, sensor;
	private final int fireThreshold;
	private boolean thereIsFire = false;

	public MuseumVisitorPDABehavior(final IEnvironment<Double, Double, Integer> env, final INode<Integer> node, final IMolecule fire, final IMolecule picture, final IMolecule door, final IMolecule human, final IMolecule sensor, final int fireThreshold) {
		this.env = env;
		this.node = node;
		this.fire = fire;
		this.person = picture;
		this.door = door;
		this.human = human;
		this.sensor = sensor;
		this.fireThreshold = fireThreshold;
	}

	@Override
	public IAction<Integer> cloneOnNewNode(final INode<Integer> n, final IReaction<Integer> r) {
		// TODO: we need the new reaction!!
		return new MuseumVisitorPDABehavior(env, n, fire, person, door, human, sensor, fireThreshold);
	}

	@Override
	public void execute() {
		final IPosition<Double, Double> mypos = env.getPosition(node);
		final double myx = mypos.getCartesianCoordinates()[0];
		final double myy = mypos.getCartesianCoordinates()[1];
		final INeighborhood<Double, Integer> neigh = env.getNeighborhood(node);
		double dx = 0d, dy = 0d;
		int bestRatio = Integer.MIN_VALUE;
		boolean up = true, down = true, left = true, right = true;
		for (final INode<Integer> n : neigh.getNeighbors()) {
			if (up || down || left || right) {
				if (thereIsFire && n.getConcentration(sensor) > 0) {
					final IPosition<Double, Double> pos = env.getPosition(n);
					final double x = pos.getCartesianCoordinates()[0];
					final double y = pos.getCartesianCoordinates()[1];
					final int d = n.getConcentration(door);
					final int f = n.getConcentration(fire);
					final int p = n.getConcentration(person);
					final int delta = d * 2 - f * f / 250 - p;
					if (delta > bestRatio) {
						dx = (x - myx);
						dy = (y - myy);
						bestRatio = delta;
					}
				} else if (n.getConcentration(sensor) > 0) {
					if (n.getConcentration(fire) > 0) {
						thereIsFire = true;
					}
				}
				if (n.getConcentration(human) > 0) {
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
				}
			}
		}
		if (thereIsFire) {
			dx = dx > 0 ? Math.min(limit, dx) : Math.max(-limit, dx);
			dy = dy > 0 ? Math.min(limit, dy) : Math.max(-limit, dy);
			boolean moveH = dx > 0 && right || dx < 0 && left;
			boolean moveV = dy > 0 && down || dy < 0 && up;
			if (moveH || moveV) {
				env.moveNode(node, new Continuous2DEuclidean(moveH ? dx : 0, moveV ? dy : 0));
			}
		}
	}

	@Override
	public Context getContext() {
		return Context.LOCAL;
	}

	@Override
	public List<IMolecule> getModifiedMolecules() {
		return mod;
	}

}

