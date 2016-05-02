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
import it.unibo.alchemist.model.implementations.actions.AbstractAction;
import it.unibo.alchemist.model.implementations.positions.ContinuousGenericEuclidean;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.IAction;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.model.interfaces.IReaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * This action moves a node inside a given environment, evaluating a repulsive
 * force (similar to Coulomb's law) among all nodes and creating a sort of
 * spring (using Hooke's law) among neighbors.
 * 
 * @author Danilo Pianini
 * @version 20110511
 * 
 */
public class ForceFieldMove<T> extends AbstractAction<T> {

	private static final long serialVersionUID = -5867654295577425307L;
	private final IEnvironment<Double, Double, T> env;
	private final IReaction<T> r;
	private final double kr, ka, delta;

	/**
	 * Builds a new ForceFieldMove reaction. The distance between two nodes of
	 * the same neighborhood will be (kr/ka)^(1/3).
	 * 
	 * @param env
	 *            the environment
	 * @param n
	 *            the node
	 * @param kr
	 *            the repulsive force constant
	 * @param ka
	 *            the attractive force constant
	 * @param delta
	 *            the maximum length a node can walk in a single execution of
	 *            this reaction.
	 */
	public ForceFieldMove(final IEnvironment<Double, Double, T> env, final INode<T> n, final IReaction<T> r, final double kr, final double ka, final double delta) {
		super(n);
		this.env = env;
		this.r = r;
		this.kr = kr * delta;
		this.ka = ka * delta;
		this.delta = delta;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IAction#execute()
	 */
	@Override
	public void execute() {
		scan(env.getNodes(), env.getNeighborhood(getNode()).getNeighbors());
	}

	protected void scan(final Collection<? extends INode<T>> a, final List<? extends INode<T>> b) {
		INode<T> n = getNode();
		Double[] coord = new Double[env.getDimensions()];
		for (int i = 0; i < coord.length; i++) {
			coord[i] = 0d;
		}
		IPosition<Double, Double> np = env.getPosition(n);
		for (INode<T> node : a) {
			if (!n.equals(node)) {
				IPosition<Double, Double> nodep = env.getPosition(node);
				try {
					double d = np.getDistanceTo(nodep);
					if (d != 0) {
						double fr = kr / (d * d * d);
						Double[] npa = np.getCartesianCoordinates();
						Double[] nodepa = nodep.getCartesianCoordinates();
						for (int i = 0; i < coord.length; i++) {
							coord[i] += fr * (npa[i] - nodepa[i]);
						}
					}
				} catch (UncomparableDistancesException e) {
					e.printStackTrace();
				}
			}
		}
		for (INode<T> node : b) {
			if (!n.equals(node)) {
				IPosition<Double, Double> nodep = env.getPosition(node);
				Double[] npa = np.getCartesianCoordinates();
				Double[] nodepa = nodep.getCartesianCoordinates();
				for (int i = 0; i < coord.length; i++) {
					double r = nodepa[i] - npa[i];
					coord[i] += ka * r;
				}
			}
		}
		for (int i = 0; i < coord.length; i++) {
			coord[i] = coord[i] > 0 ? Math.min(delta, coord[i]) : Math.max(-delta, coord[i]);
		}
		ContinuousGenericEuclidean newpos = new ContinuousGenericEuclidean(coord);
		env.moveNode(n, newpos);
	}

	public boolean sameContent(final INode<T> n1, final INode<T> n2) {
		if (n1.getChemicalSpecies() == n2.getChemicalSpecies()) {
			for (int i = 0; i < n1.getChemicalSpecies(); i++) {
				if (n1.getConcentration(i) != n2.getConcentration(i)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IAction#getContext()
	 */
	@Override
	public Context getContext() {
		return Context.LOCAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IAction#getModifiedMolecules()
	 */
	@Override
	public List<IMolecule> getModifiedMolecules() {
		return new ArrayList<IMolecule>(0);
	}

	protected IEnvironment<Double, Double, T> getEnvironment() {
		return env;
	}

	protected IReaction<T> getReaction() {
		return r;
	}

	protected double getKr() {
		return kr;
	}

	protected double getKa() {
		return ka;
	}

	protected double getDelta() {
		return delta;
	}

	@Override
	public IAction<T> cloneOnNewNode(final INode<T> n, final IReaction<T> r) {
		return new ForceFieldMove<T>(env, n, r, kr, ka, delta);
	}
}

