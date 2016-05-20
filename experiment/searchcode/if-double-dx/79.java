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
import it.unibo.alchemist.external.cern.jet.random.engine.MersenneTwister;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



/**
 * @author Danilo Pianini
 * @version 20110322
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class MuseumVisitorFollowMolecule<T extends Number> implements IAction<T> {

	private static final long serialVersionUID = -4274734253286882410L;
	private static final double limit = 0.2;
	private static final List<IMolecule> mod = new ArrayList<IMolecule>(0);
	private final RandomEngine rand = new MersenneTwister(new Date());
	private final IEnvironment<Double, Double, T> env;
	private final INode<T> node;
	private final IMolecule field, human, sensorOn;

	public MuseumVisitorFollowMolecule(final IEnvironment<Double, Double, T> env, final INode<T> node, final IMolecule field, final IMolecule human, final IMolecule sensorOn) {
		this.env = env;
		this.node = node;
		this.field = field;
		this.human = human;
		this.sensorOn = sensorOn;
	}

	@Override
	public IAction<T> cloneOnNewNode(final INode<T> n, final IReaction<T> r) {
		return new MuseumVisitorFollowMolecule<T>(env, n, field, human, sensorOn);
	}

	@Override
	public void execute() {
		final IPosition<Double, Double> mypos = env.getPosition(node);
		final double myx = mypos.getCartesianCoordinates()[0];
		final double myy = mypos.getCartesianCoordinates()[1];
		final INeighborhood<Double, T> neigh = env.getNeighborhood(node);
		final ArrayList<double[]> wheel = new ArrayList<double[]>(neigh.getNeighbors().size());
		// double dx=0d, dy=0d;
		// double bestRatio = Double.MIN_VALUE;
		double totalRatio = 0;
		boolean up = true, down = true, left = true, right = true;
		for (final INode<T> n : neigh.getNeighbors()) {
			if (up || down || left || right) {
				if (n.getConcentration(human).doubleValue() > 0) {
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
				} else if (n.getConcentration(sensorOn).doubleValue() > 0) {
					double fieldConcentration = n.getConcentration(field).doubleValue();
					if (fieldConcentration > 0) {
						fieldConcentration = Math.pow(2, fieldConcentration);
						totalRatio += fieldConcentration;
						final IPosition<Double, Double> pos = env.getPosition(n);
						final double x = pos.getCartesianCoordinates()[0];
						final double y = pos.getCartesianCoordinates()[1];
						wheel.add(new double[] { fieldConcentration, x - myx, y - myy });
					}
				}
			} else {
				break;
			}
		}
		if (up || down || left || right) {
			double rnd = rand.nextDouble() * totalRatio;
			for (double[] data : wheel) {
				if (rnd <= data[0]) {
					double dx = data[1];
					double dy = data[2];
					dx = dx > 0 ? Math.min(limit, dx) : Math.max(-limit, dx);
					dy = dy > 0 ? Math.min(limit, dy) : Math.max(-limit, dy);
					boolean moveH = dx > 0 && right || dx < 0 && left;
					boolean moveV = dy > 0 && down || dy < 0 && up;
					if (moveH || moveV) {
						env.moveNode(node, new Continuous2DEuclidean(moveH ? dx : 0, moveV ? dy : 0));
					}
					break;
				}
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

