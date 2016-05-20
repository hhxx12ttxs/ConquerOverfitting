/*******************************************************************************
 * Copyright (c) 2012 Danilo Pianini.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions;

/**
 * 
 */
import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.actions.SAPEREMoveNodeAgent;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.INeighborhood;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Giacomo Pronti
 * @author Danilo Pianini
 * 
 */
public class LsaMASSAgent extends SAPEREMoveNodeAgent<Double, Double> {

	private static final double LIMIT = 0.4;
	private static final double RANGE = 0.5;
	private static final long serialVersionUID = -4274734253286882410L;
	private static final double STEP = 0.1;
	private final ILsaMolecule fieldMol, sensor;
	private Double maxField;
	private final Double probMoving;
	private final RandomEngine rand;

	/**
	 * @param env
	 *            the environment
	 * @param node
	 *            the node
	 * @param field
	 *            the field to follow
	 * @param isSensor
	 *            sensors distinctive template
	 * @param random
	 *            random engine
	 * @param p
	 *            probability to move
	 */
	public LsaMASSAgent(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> env, final ILsaNode node, final ILsaMolecule field, final ILsaMolecule isSensor, final RandomEngine random, final Double p) {
		super(env, node);
		this.fieldMol = field;
		this.sensor = isSensor;
		this.rand = random;
		this.probMoving = p;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IAction#execute()
	 */
	@Override
	public void execute() {
		final IPosition<Double, Double> mypos = getCurrentPosition();
		final double myx = mypos.getCartesianCoordinates()[0];
		final double myy = mypos.getCartesianCoordinates()[1];
		double x = 0;
		double y = 0;
		final INeighborhood<Double, List<? extends ILsaMolecule>> neigh = getLocalNeighborhood();
		boolean up = true, down = true, left = true, right = true;
		List<IPosition<Double, Double>> poss = new ArrayList<IPosition<Double, Double>>();
		maxField = -1.0;

		for (final INode<List<? extends ILsaMolecule>> nodo : neigh.getNeighbors()) {
			LsaNode n = (LsaNode) nodo;
			if (up || down || left || right) {
				if (n.getConcentration(sensor).size() == 0) {
					final IPosition<Double, Double> pos = getPosition(n);
					try {
						if (pos.getDistanceTo(mypos) < RANGE) {
							x = pos.getCartesianCoordinates()[0];
							y = pos.getCartesianCoordinates()[1];
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
				} else {
					List<ILsaMolecule> fieldRes;
					try {
						fieldRes = n.getConcentration(fieldMol);
					} catch (IndexOutOfBoundsException e) {
						fieldRes = null;
					}
					if (fieldRes != null && fieldRes.size() != 0) {
						double val, valMax = 0;
						for (int i = 0; i < fieldRes.size(); i++) {
							val = (Double) fieldRes.get(i).getArg(2).calculate(null).getValue(null);
							if (val > valMax) {
								valMax = val;
							}
						}

						if (valMax > 0) {
							double fieldConcentration = valMax;
							if (fieldConcentration == maxField) {
								poss.add(getPosition(n));
							} else if (fieldConcentration > maxField) {
								maxField = fieldConcentration;
								poss.clear();
								poss.add(getPosition(n));

							}
						}
					}
				}
			} else {
				break;
			}
		}

		if (up || down || left || right) {
			double rnd = rand.nextDouble();
			if (rnd < probMoving && poss.size() != 0) {
				int intrnd = (int) (rand.nextDouble() * (poss.size() - 1));
				x = poss.get(intrnd).getCartesianCoordinates()[0];
				y = poss.get(intrnd).getCartesianCoordinates()[1];
				double dx = x - myx;
				double dy = y - myy;
				dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
				dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);
				boolean moveH = dx > 0 && right || dx < 0 && left;
				boolean moveV = dy > 0 && down || dy < 0 && up;
				if (moveH || moveV) {
					move(new Continuous2DEuclidean(moveH ? dx : 0, moveV ? dy : 0));
				}
			} else {
				double u = 0, d = 0, ri = 0, le = 0, ud = 0, lr = 0;
				if (up) {
					u = rand.nextDouble() * LIMIT;
				}
				if (down) {
					d = rand.nextDouble() * LIMIT;
				}
				if (left) {
					le = rand.nextDouble() * LIMIT;
				}
				if (right) {
					ri = rand.nextDouble() * LIMIT;
				}
				if ((u - d) > 0) {
					ud = (u - d) + STEP;
				} else {
					ud = (u - d) - STEP;
				}
				if ((le - ri) > 0) {
					lr = (le - ri) + STEP;
				} else {
					lr = (le - ri) - STEP;
				}
				move(new Continuous2DEuclidean(ud, lr));
			}
		}

	}

}

