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
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
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
//CHECKSTYLE:OFF
@Deprecated
public class LsaAdvertAgent extends SAPEREMoveNodeAgent<Double, Double> {

	private static final int ARG_POSITION = 3;
	private static final double LIMIT = 0.2;
	private static final int MAX_GRAD = 300;
	private static final double MAX_PROXIMITY = 0.5;
	private static final double MAX_STEP = 0.1;
	private static final long serialVersionUID = -4274734253286882410L;
	private final ILsaMolecule field, isSensor, isPerson;
	private final Double probMoving;
	private final RandomEngine random;

	/**
	 * Builds a new LsaAdvertAgent.
	 * 
	 * @param env
	 *            the current environment
	 * @param node
	 *            the node that will host this agent
	 * @param fieldTemplate
	 *            template for the field
	 * @param person
	 *            template of detecting humans
	 * @param randomEngine
	 *            random engine
	 * @param p
	 *            probability to make a move
	 */
	public LsaAdvertAgent(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> env, final ILsaNode node, final ILsaMolecule fieldTemplate, final ILsaMolecule person, final RandomEngine randomEngine, final Double p) {
		super(env, node);
		this.field = fieldTemplate;
		this.isSensor = new LsaMolecule("sensor");
		this.random = randomEngine;
		this.isPerson = person;
		this.probMoving = p;

	}

	@Override
	public void execute() {
		addMatch("Type", getNode().getConcentration(isPerson).get(0).getArg(1).getRootNode());
		final IPosition<Double, Double> mypos = getCurrentPosition();
		final double myx = mypos.getCartesianCoordinates()[0];
		final double myy = mypos.getCartesianCoordinates()[1];
		double x = 0;
		double y = 0;
		final INeighborhood<Double, List<? extends ILsaMolecule>> neigh = getLocalNeighborhood();
		boolean up = true, down = true, left = true, right = true;
		final List<IPosition<Double, Double>> poss = new ArrayList<IPosition<Double, Double>>();
		double maxField = Double.NEGATIVE_INFINITY;
		for (final INode<List<? extends ILsaMolecule>> nodo : neigh.getNeighbors()) {
			LsaNode n = (LsaNode) nodo;
			if (up || down || left || right) {
				if (n.getConcentration(isSensor).size() == 0) {
					final IPosition<Double, Double> pos = getPosition(n);
					try {
						if (pos.getDistanceTo(mypos) < MAX_PROXIMITY) {
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
					List<IExpression> l = field.allocateVar(getMatches());
					try {
						fieldRes = n.getConcentration(new LsaMolecule(l));
					} catch (IndexOutOfBoundsException e) {
						fieldRes = null;
					}
					if (fieldRes != null && fieldRes.size() != 0) {

						double val, valMax = 0;
						for (int i = 0; i < fieldRes.size(); i++) {
							val = getLSAArgumentAsDouble(fieldRes.get(i), ARG_POSITION);
							if (val > valMax) {
								valMax = val;
							}
						}
						if (valMax > 0 && valMax < MAX_GRAD) {
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

		double rnd = random.nextFloat();
		if (poss.size() != 0) {
			int intrnd = (int) (random.nextFloat() * (poss.size() - 1));
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
			} else if (rnd > probMoving) {
				double u = 0, d = 0, ri = 0, le = 0, ud = 0, lr = 0;
				if (up) {
					u = random.nextFloat() * LIMIT;
				}
				if (down) {
					d = random.nextFloat() * LIMIT;
				}
				if (left) {
					le = random.nextFloat() * LIMIT;
				}
				if (right) {
					ri = random.nextFloat() * LIMIT;
				}
				if ((u - d) > 0) {
					ud = (u - d) + MAX_STEP;
				} else {
					ud = (u - d) - MAX_STEP;
				}
				if ((le - ri) > 0) {
					lr = (le - ri) + MAX_STEP;
				} else {
					lr = (le - ri) - MAX_STEP;
				}
				move(new Continuous2DEuclidean(ud, lr));
			}

		}
	}

}

