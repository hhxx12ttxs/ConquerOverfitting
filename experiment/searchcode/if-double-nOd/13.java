/*******************************************************************************
 * Copyright (c) 2012 Danilo Pianini.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.utils.FasterString;
import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.actions.SAPERELocalAgent;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IReaction;

import java.util.List;


/**
 * @author Giacomo Pronti
 * @author Danilo Pianini
 * 
 */
public class LsaCountNeighborsAction extends SAPERELocalAgent {

	private static final long serialVersionUID = -7128058274012426458L;
	private final FasterString countVarName;
	private final IEnvironment<?, ?, List<? extends ILsaMolecule>> env;
	private final ILsaMolecule mol;
	private final RandomEngine rnd;

	/**
	 * Builds a new action that counts neighbors which contain in their lsaSpace
	 * an lsaMolecule matching mol. The effect of this Action is to add to the
	 * matches list the variable countVar. The execution has no effect on the
	 * set of influenced molecules for the reaction.
	 * 
	 * @param environment
	 *            The environment to use
	 * @param node
	 *            The source node
	 * @param molToCount
	 *            The IlsaMolecule instance you want to search in neighbor lsa
	 *            space.
	 * @param countVar
	 *            The String representing the name of the counting var. (to add
	 *            to matches map)
	 * @param rand
	 *            Random engine
	 */
	public LsaCountNeighborsAction(final IEnvironment<?, ?, List<? extends ILsaMolecule>> environment, final ILsaNode node, final ILsaMolecule molToCount, final FasterString countVar, final RandomEngine rand) {
		super(node);
		rnd = rand;
		env = environment;
		countVarName = new FasterString(countVar);
		mol = molToCount;
	}

	/**
	 * Builds a new action that counts neighbors which contain in their lsaSpace
	 * an lsaMolecule matching mol. The effect of this Action is to add to the
	 * matches list the variable countVar. The execution has no effect on the
	 * set of influenced molecules for the reaction.
	 * 
	 * @param environment
	 *            The environment to use
	 * @param node
	 *            The source node
	 * @param molToCount
	 *            The IlsaMolecule instance you want to search in neighbor lsa
	 *            space.
	 * @param countVar
	 *            The String representing the name of the counting var. (to add
	 *            to matches map)
	 * @param rand
	 *            Random engine
	 */
	public LsaCountNeighborsAction(final IEnvironment<?, ?, List<? extends ILsaMolecule>> environment, final ILsaNode node, final ILsaMolecule molToCount, final String countVar, final RandomEngine rand) {
		this(environment, node, molToCount, new FasterString(countVar), rand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * alice.alchemist.model.implementations.actions.SAPEREAgent#cloneOnNewNode
	 * (alice.alchemist.model.interfaces.INode,
	 * alice.alchemist.model.interfaces.IReaction)
	 */
	@Override
	public LsaCountNeighborsAction cloneOnNewNode(final INode<List<? extends ILsaMolecule>> n, final IReaction<List<? extends ILsaMolecule>> r) {
		return new LsaCountNeighborsAction(getEnvironment(), (ILsaNode) n, mol, countVarName, rnd);
	}

	@Override
	public void execute() {
		List<IExpression> l = mol.allocateVar(getMatches());
		Double num = 0.0;
		if (env.getNeighborhood(getNode()) != null) {
			for (INode<List<? extends ILsaMolecule>> nod : env.getNeighborhood(getNode()).getNeighbors()) {
				nod = (LsaNode) nod;
				if (nod.getConcentration(new LsaMolecule(l)).size() != 0) {
					num++;
				}
			}
		}
		getMatches().put(countVarName, new NumTreeNode(num));
	}

	/**
	 * @return the current environment
	 */
	protected IEnvironment<?, ?, List<? extends ILsaMolecule>> getEnvironment() {
		return env;
	}

	/**
	 * @return a new random double
	 */
	protected double random() {
		return rnd.nextDouble();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.implementations.actions.SAPEREAgent#toString()
	 */
	@Override
	public String toString() {
		return "Count " + countVarName;
	}

}

