/*******************************************************************************
 * Copyright (c) 2012 Danilo Pianini.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.conditions.IntMoleculePresent;
import it.unibo.alchemist.model.interfaces.IAction;
import it.unibo.alchemist.model.interfaces.ICondition;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Danilo Pianini
 * @version 20110825
 * 
 * @param <N>
 * @param <D>
 */
public class Diffusion<N extends Number, D extends Number> extends ExpTimeReactionBindsNeighbors<N, D, Integer> {

	private static final long serialVersionUID = 7363967315457771232L;
	/**
	 * 
	 */
	public static final double DEFAULT_RATIO = 1.6, DEFAULT_VISCOSITY = 2.5, DEFAULT_TEMPERATURE = 298.15;
	private final HashMap<INode<Integer>, D> distances = new HashMap<INode<Integer>, D>();
	private final HashMap<INode<Integer>, Double> propensities = new HashMap<INode<Integer>, Double>();
	private final IMolecule mol;
	private final double r;
	private final double ni;
	private final double p;
	private final double t;
	private final int i = 0;

	/**
	 * @param molecule
	 *            the molecule which diffuses
	 * @param n
	 *            the node
	 * @param environment
	 *            the current environment
	 * @param rate
	 *            speed of the reaction
	 * @param random
	 *            the random engine for this simulation
	 * @param ratio
	 *            the ratio between an empirical constant k_f and the intrinsic
	 *            viscosity ni. According to Harding and Johnson
	 *            "The concentration dependence of macromolecular parameters",
	 *            this value is in a range of 1.4-1.7, and the more asymmetric a
	 *            particle is, the lower.
	 * @param viscosity
	 *            intrinsic viscosity coefficient. A rigid sphere has a ni = 2.5
	 * @param moleculeMass
	 *            mass of a molecule
	 * @param temp
	 *            temperature
	 */
	public Diffusion(final IMolecule molecule, final INode<Integer> n, final IEnvironment<N, D, Integer> environment, final double rate, final RandomEngine random, final double ratio, final double viscosity, final double moleculeMass, final double temp) {
		super(environment, n, rate, random);
		this.mol = molecule;
		this.r = ratio;
		this.ni = viscosity;
		this.p = moleculeMass;
		this.t = temp;
	}

	/**
	 * @param molecule
	 *            the molecule which diffuses
	 * @param n
	 *            the node
	 * @param environment
	 *            the current environment
	 * @param rate
	 *            speed of the reaction
	 * @param random
	 *            the random engine for this simulation
	 * @param molMass
	 *            molecular mass
	 */
	public Diffusion(final IMolecule molecule, final INode<Integer> n, final IEnvironment<N, D, Integer> environment, final double rate, final RandomEngine random, final double molMass) {
		this(molecule, n, environment, rate, random, DEFAULT_RATIO, DEFAULT_VISCOSITY, molMass, DEFAULT_TEMPERATURE);
	}

	/**
	 * @param c
	 *            this parameter is ignored.
	 */
	@Override
	public void setConditions(final List<? extends ICondition<Integer>> c) {
		ArrayList<ICondition<Integer>> cond = new ArrayList<ICondition<Integer>>(1);
		cond.add(buildPresentCondition(mol, getNode()));
		super.setConditions(cond);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.unibo.alchemist.model.implementations.reactions.ExpTimeReaction#setActions
	 * (java.util.List)
	 */
	@Override
	public void setActions(final List<? extends IAction<Integer>> c) {
		ArrayList<IAction<Integer>> cond = new ArrayList<IAction<Integer>>(1);
		cond.add(buildMoveAction(mol, getNode()));
		super.setActions(cond);
	}

	/**
	 * NOT IMPLEMENTED YET.
	 * 
	 * @param mol
	 *            the molecule which diffuses
	 * @param node
	 *            the current node
	 * @return a new move action
	 */
	protected static final IAction<Integer> buildMoveAction(final IMolecule mol, final INode<Integer> node) {
		// TODO: create action
		return null;
	}

	/**
	 * 
	 * @param mol
	 *            the molecule which diffuses
	 * @param n
	 *            the current node
	 * @return a new condition
	 */
	protected static final IntMoleculePresent buildPresentCondition(final IMolecule mol, final INode<Integer> n) {
		return new IntMoleculePresent(mol, n, 1);
	}

	@Override
	public double getPropensity() {
		initPossibleNodes();
		IPosition<N, D> mypos = getEnvironment().getPosition(getNode());
		double result = 0;
		for (INode<Integer> n : getPossibleNodes()) {
			try {
				D distance = mypos.getDistanceTo(getEnvironment().getPosition(n));
				D olddistance = distances.get(n);
				/*
				 * Note: cache is never freed.
				 */
				if (!distance.equals(olddistance)) {
					distances.put(n, distance);
					double prop = computePropensity(distance);
					result += prop;
					propensities.put(n, prop);
				} else {
					result += propensities.get(n);
				}
			} catch (UncomparableDistancesException e1) {
				e1.printStackTrace();
			}
		}
		return getRate() * result;
	}

	/**
	 * @param distance
	 * @return
	 */
	private double computePropensity(final D distance) {
		double l = distance.doubleValue();
		return diffusionCoefficient(getNode().getConcentration(mol), r, ni, p, l, t, i) / (l * l);
	}

	private static final double BC = -Math.PI * MathUtils.AVOGADRO / 6;

	/**
	 * This method computes the second virial coefficient as in Equation 27 of
	 * Lecca et al.
	 * "Stochastic simulation of the spatio-temporal dynamics of reaction-diffusion systems: the case for the bicoid gradient"
	 * 
	 * @param t
	 *            temperature number of iterations for approximation. According
	 *            to Lecca's article, values higher than 4 do not influence the
	 *            simulation results.
	 * @param i
	 *            iteractions to do
	 * @param useLanczos
	 *            true if you want to use Lanczos
	 * @return the second virial coefficient
	 */
	public static double secondVirialCoefficient(final double t, final int i, final boolean useLanczos) {
		double sum = 0;
		for (int j = 0; j < i; j++) {
			final double v = ((double) j) / 2 - 0.25;
			final double gamma = useLanczos ? MathUtils.lanczosGamma(v) : MathUtils.stirlingGamma(v);
			sum += Math.pow(4, j) * Math.pow(MathUtils.K_BOLTZMANN * t, v) * gamma / MathUtils.factDouble(j);
		}
		return BC * sum;
	}

	/**
	 * This function evaluates the value of the frictional coefficient as in
	 * Equation 24 of Lecca et al.
	 * "Stochastic simulation of the spatio-temporal dynamics of reaction-diffusion systems: the case for the bicoid gradient"
	 * 
	 * @param c
	 *            concentration of the molecule in the node (mesh or
	 *            compartment)
	 * @param r
	 *            the ratio between an empirical constant k_f and the intrinsic
	 *            viscosity ni. According to Harding and Johnson
	 *            "The concentration dependence of macromolecular parameters",
	 *            this value is in a range of 1.4-1.7, and the more asymmetric a
	 *            particle is, the lower.
	 * @param ni
	 *            intrinsic viscosity coefficient. A rigid sphere has a ni = 2.5
	 * @return the frictional coefficient f_(i,k)
	 */
	public static double frictionalCoefficient(final double c, final double r, final double ni) {
		return c * r * ni;
	}

	/**
	 * This function evaluates the value of the activity coefficient of the
	 * solute as in Equation 23 of Lecca et al.
	 * "Stochastic simulation of the spatio-temporal dynamics of reaction-diffusion systems: the case for the bicoid gradient"
	 * 
	 * @param p
	 *            mass of a molecule
	 * @param l
	 *            dimension of the mesh (distance between nodes)
	 * @param c
	 *            concentration of the molecule in the node (mesh or
	 *            compartment)
	 * @param t
	 *            temperature
	 * @param i
	 *            number of iterations for second virial coefficient
	 *            computation. Values higher than 4 may compromise performances.
	 *            Values higher than 170 may (will) result in wrong results.
	 * @return the activity coefficient y
	 */
	public static double activityCoefficient(final double p, final double l, final double c, final double t, final int i) {
		return Math.exp(k(p, l, t, i) * c * c);
	}

	/**
	 * Computes the value of the derivative of the activity coefficient on the
	 * concentration. The analytic expression for it was calculated manually by
	 * deriving the equation 23 of Lecca et al.
	 * "Stochastic simulation of the spatio-temporal dynamics of reaction-diffusion systems: the case for the bicoid gradient"
	 * 
	 * @param p
	 *            mass of a molecule
	 * @param l
	 *            dimension of the mesh (distance between nodes)
	 * @param c
	 *            concentration
	 * @param t
	 *            temperature
	 * @param i
	 *            number of iterations for second virial coefficient
	 *            computation. Values higher than 4 may compromise performances.
	 *            Values higher than 170 may (will) result in wrong results.
	 * @return the derivative of the activity coefficient on the concentration
	 */
	public static double dyDc(final double p, final double l, final double c, final double t, final int i) {
		final double k = k(p, c, t, i);
		return 2 * k * c * Math.exp(k * c * c);
	}

	/**
	 * This function calculates a part of the eq. 23 of Lecca et al.
	 * "Stochastic simulation of the spatio-temporal dynamics of reaction-diffusion systems: the case for the bicoid gradient"
	 * 
	 * @param p
	 *            mass of a molecule
	 * @param l
	 *            dimension of the mesh (distance between nodes)
	 * @param t
	 *            temperature
	 * @param i
	 *            number of iterations for second virial coefficient
	 *            computation. Values higher than 4 may compromise performances.
	 *            Values higher than 170 may (will) result in wrong results.
	 */
	private static double k(final double p, final double l, final double t, final int i) {
		return 2 * secondVirialCoefficient(t, i, true) * p * l / MathUtils.AVOGADRO;
	}

	/**
	 * This function calculates eq. 12 of Lecca et al.
	 * "Stochastic simulation of the spatio-temporal dynamics of reaction-diffusion systems: the case for the bicoid gradient"
	 * 
	 * @param c
	 *            concentration of the molecule in the node (mesh or
	 *            compartment)
	 * @param r
	 *            the ratio between an empirical constant k_f and the intrinsic
	 *            viscosity ni. According to Harding and Johnson
	 *            "The concentration dependence of macromolecular parameters",
	 *            this value is in a range of 1.4-1.7, and the more asymmetric a
	 *            particle is, the lower.
	 * @param ni
	 *            intrinsic viscosity coefficient. A rigid sphere has a ni = 2.5
	 * @param p
	 *            mass of a molecule
	 * @param l
	 *            dimension of the mesh (distance between nodes)
	 * @param t
	 *            temperature
	 * @param i
	 *            number of iterations for second virial coefficient
	 *            computation. Values higher than 4 may compromise performances.
	 *            Values higher than 170 may (will) result in wrong results.
	 * @return the diffusion coefficient
	 */
	public static double diffusionCoefficient(final double c, final double r, final double ni, final double p, final double l, final double t, final int i) {
		return MathUtils.K_BOLTZMANN * t / frictionalCoefficient(c, r, ni) * (1 + c * dyDc(p, l, c, t, i) / activityCoefficient(p, l, c, t, i));
	}
}

