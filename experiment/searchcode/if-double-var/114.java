package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.expressions.implementations.Expression;
import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.implementations.Type;
import it.unibo.alchemist.expressions.interfaces.IExpression;
import it.unibo.alchemist.expressions.interfaces.ITreeNode;
import it.unibo.alchemist.expressions.utils.FasterString;
import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.utils.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * This class provides a fast and stable gradient implementation, inspired on
 * the NBR construct used in Proto.
 * 
 * @author Danilo Pianini
 * 
 */
public class SAPEREGradient extends ExpTimeReaction<List<? extends ILsaMolecule>> {

	private static final long serialVersionUID = 8362443887879500016L;
	private static final List<ILsaMolecule> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<ILsaMolecule>(0));
	private final ILsaMolecule source, gradient, gradientExpr, context;
	private final IExpression exp;
	private final IEnvironment<Double, Double, List<? extends ILsaMolecule>> environment;
	private final ILsaNode node;
	private final double threshold;
	private List<? extends ILsaMolecule> sourceCache;
	private List<? extends ILsaMolecule> contextCache;
	private Map<INode<List<? extends ILsaMolecule>>, IPosition<Double, Double>> positionCache = new HashMap<>();
	private Map<INode<List<? extends ILsaMolecule>>, List<? extends ILsaMolecule>> gradCache = new HashMap<>();
	private IPosition<Double, Double> mypos;
	private static final IExpression ZERO_NODE = new Expression(new NumTreeNode(0d));
	private final int argPosition;
	private boolean canRun = true;

	/**
	 * Builds a new SAPERE Gradient.
	 * 
	 * @param env
	 *            the current environment
	 * @param n
	 *            the node where this reaction is scheduled
	 * @param random
	 *            the RandomEngine to use
	 * @param sourceTemplate
	 *            a template ILsaMolecule representing the source
	 * @param gradientTemplate
	 *            a template ILsaMolecule representing the gradient. ALL the
	 *            variables MUST be the same of sourceTemplate: no uninstanced
	 *            variables are admitted when inserting tuples into nodes
	 * @param valuePosition
	 *            the point at which the computation of the new values should be
	 *            inserted. All the data after this position will be considered
	 *            "additional information" propagated by the source. All the
	 *            values before this one, instead, will be used to distinct
	 *            different gradients
	 * @param expression
	 *            the expression to use in order to calculate the new gradient
	 *            value. #T and #D are admitted, plus every variable present in
	 *            the gradient before valuePosition, and every variable matched
	 *            by the contextTemplate
	 * @param contextTemplate
	 *            a template ILsaMolecule. It can be used to match some contents
	 *            of the local node in order to have local informations to use
	 *            in the gradient value computation
	 * @param gradThreshold
	 *            if the value of the gradient grows above this threshold, the
	 *            gradient evaporates
	 * @param rate
	 *            Markovian Rate
	 */
	public SAPEREGradient(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> env, final ILsaNode n, final RandomEngine random, final ILsaMolecule sourceTemplate, final ILsaMolecule gradientTemplate, final int valuePosition, final String expression, final ILsaMolecule contextTemplate, final double gradThreshold, final double rate) {
		super(n, rate, random);
		context = contextTemplate;
		gradient = gradientTemplate;
		source = sourceTemplate;
		exp = new Expression(expression);
		environment = env;
		node = n;
		argPosition = valuePosition;
		threshold = gradThreshold;
		final List<IExpression> grexp = gradient.allocateVar(null);
		grexp.set(argPosition, exp);
		gradientExpr = new LsaMolecule(grexp);
		addInfluencedMolecule(gradient);
		addInfluencingMolecule(source);
		addInfluencingMolecule(gradient);
		if (context != null) {
			addInfluencingMolecule(context);
		}
	}

	@Override
	public ILsaNode getNode() {
		return node;
	}

	@Override
	public boolean canExecute() {
		return canRun;
	}

	@Override
	public double getPropensity() {
		/*
		 * It makes sense to reschedule the reaction if:
		 * 
		 * the source has changed
		 * 
		 * the contextual information has changed
		 * 
		 * the neighbors have moved
		 * 
		 * the gradients in the neighborhood have changed
		 */
		final List<? extends ILsaMolecule> sourceCacheTemp = node.getConcentration(source);
		final List<? extends ILsaMolecule> contextCacheTemp = context == null ? EMPTY_LIST : node.getConcentration(context);
		final Map<INode<List<? extends ILsaMolecule>>, IPosition<Double, Double>> positionCacheTemp = new HashMap<>();
		final Map<INode<List<? extends ILsaMolecule>>, List<? extends ILsaMolecule>> gradCacheTemp = new HashMap<>();
		final IPosition<Double, Double> curPos = environment.getPosition(node);
		for (final INode<List<? extends ILsaMolecule>> n : environment.getNeighborhood(node)) {
			positionCacheTemp.put(n, environment.getPosition(n));
			gradCacheTemp.put(n, n.getConcentration(gradient));
		}
		if (!sourceCacheTemp.equals(sourceCache) || !contextCacheTemp.equals(contextCache) || !positionCacheTemp.equals(positionCache) || !gradCacheTemp.equals(gradCache) || !mypos.equals(curPos)) {
			sourceCache = sourceCacheTemp;
			contextCache = contextCacheTemp;
			positionCache = positionCacheTemp;
			gradCache = gradCacheTemp;
			mypos = curPos;
			canRun = true;
		}
		return canRun ? getRate() : 0;
	}

	@Override
	public Context getInputContext() {
		return Context.NEIGHBORHOOD;
	}

	@Override
	public Context getOutputContext() {
		return Context.LOCAL;
	}

	/**
	 * Clean up existing gradients. The new values will be computed upon
	 * neighbors'
	 */
	private List<ILsaMolecule> cleanUpExistingAndRecomputeFromSource(final Map<FasterString, ITreeNode<?>> matches) {
		for (final ILsaMolecule g : node.getConcentration(gradient)) {
			node.removeConcentration(g);
		}
		final List<ILsaMolecule> createdFromSource = new ArrayList<>(sourceCache.size());
		for (final ILsaMolecule s : sourceCache) {
			for (int i = 0; i < source.size(); i++) {
				final ITreeNode<?> uninstancedArg = source.getArg(i).getRootNode();
				if (uninstancedArg.getType().equals(Type.VAR)) {
					matches.put(uninstancedArg.toFasterString(), s.getArg(i).getRootNode());
				}
			}
			final List<IExpression> gl = gradient.allocateVar(matches);
			final ILsaMolecule m = new LsaMolecule(gl);
			createdFromSource.add(m);
			node.setConcentration(m);
		}
		return createdFromSource;
	}

	@Override
	public void execute() {
		canRun = false;
		final Map<FasterString, ITreeNode<?>> matches = new HashMap<>();
		matches.put(LsaMolecule.SYN_T, new NumTreeNode(getTau().toDouble()));
		final List<ILsaMolecule> createdFromSource = cleanUpExistingAndRecomputeFromSource(matches);
		/*
		 * Context computation: if there are contexts matched, use the first.
		 * Otherwise, assign zero to every variable not yet instanced (to allow
		 * computation to proceed).
		 */
		if (contextCache.size() > 0) {
			final ILsaMolecule contextInstance = contextCache.get(0);
			for (int i = 0; i < context.argsNumber(); i++) {
				final ITreeNode<?> uninstancedArg = context.getArg(i).getRootNode();
				if (uninstancedArg.getType().equals(Type.VAR)) {
					final FasterString varName = uninstancedArg.toFasterString();
					final ITreeNode<?> matched = matches.get(varName);
					final ITreeNode<?> localVal = contextInstance.getArg(i).getRootNode();
					if (matched == null || matched.equals(localVal)) {
						matches.put(varName, localVal);
					} else {
						L.warn("You are doing something nasty.");
					}
				}
			}
		} else if (context != null) {
			for (int i = 0; i < context.argsNumber(); i++) {
				final ITreeNode<?> uninstancedArg = context.getArg(i).getRootNode();
				if (uninstancedArg.getType().equals(Type.VAR)) {
					final FasterString varName = uninstancedArg.toFasterString();
					final ITreeNode<?> matched = matches.get(varName);
					if (matched == null) {
						matches.put(varName, ZERO_NODE.getRootNode());
					}
				}
			}
		}
		/*
		 * All the gradients in the neighborhood which conflict with those
		 * generated by a source should not be considered
		 */
		final Map<INode<List<? extends ILsaMolecule>>, List<? extends ILsaMolecule>> filteredGradCache;
		if (createdFromSource.size() == 0) {
			filteredGradCache = gradCache;
		} else {
			filteredGradCache = new HashMap<>(gradCache.size(), 1.0f);
			for (final Entry<INode<List<? extends ILsaMolecule>>, List<? extends ILsaMolecule>> nn : gradCache.entrySet()) {
				final List<? extends ILsaMolecule> ol = nn.getValue();
				final List<ILsaMolecule> nl = new ArrayList<>(ol.size());
				for (final ILsaMolecule matchedGrad : ol) {
					boolean hasMatched = false;
					for (final ILsaMolecule sm : createdFromSource) {
						int i = 0;
						/*
						 * Check the exogenous gradients for all the arguments
						 * before the distance: if they match with at least one
						 * of the sources, they should not be considered
						 */
						// CHECKSTYLE:OFF
						for (; i < argPosition && matchedGrad.getArg(i).matches(sm.getArg(i), null); i++);
						// CHECKSTYLE:ON
						if (i == argPosition) {
							hasMatched = true;
							break;
						}
					}
					if (!hasMatched) {
						nl.add(matchedGrad);
					}
				}
				filteredGradCache.put(nn.getKey(), nl);
			}
		}
		/*
		 * Gradients in neighborhood must be discovered
		 */
		final List<ILsaMolecule> gradientsFound = new ArrayList<>();
		try {
			for (final Entry<INode<List<? extends ILsaMolecule>>, List<? extends ILsaMolecule>> nn : filteredGradCache.entrySet()) {
				final List<? extends ILsaMolecule> mgnList = nn.getValue();
				if (mgnList.size() > 0) {
					double distNode = positionCache.get(nn.getKey()).getDistanceTo(mypos);
					matches.put(LsaMolecule.SYN_D, new NumTreeNode(distNode));
					final Map<FasterString, ITreeNode<?>> localMatches = mgnList.size() > 1 ? new HashMap<>(matches) : matches;
					for (final ILsaMolecule mgn : mgnList) {
						/*
						 * Instance all the variables.
						 */
						for (int i = 0; i < gradient.size(); i++) {
							final ITreeNode<?> uninstancedArg = gradient.getArg(i).getRootNode();
							if (uninstancedArg.getType().equals(Type.VAR)) {
								final FasterString varName = uninstancedArg.toFasterString();
								final ITreeNode<?> localVal = mgn.getArg(i).getRootNode();
								localMatches.put(varName, localVal);
							}
						}
						/*
						 * Compute new value
						 */
						final List<IExpression> valuesFound = gradientExpr.allocateVar(localMatches);
						if (gradientsFound.size() == 0) {
							if (((Double) valuesFound.get(argPosition).getRootNodeData()) <= threshold) {
								gradientsFound.add(new LsaMolecule(valuesFound));
							}
						} else {
							boolean compatibleFound = false;
							for (int j = 0; j < gradientsFound.size(); j++) {
								final ILsaMolecule gradToCompare = gradientsFound.get(j);
								int i = 0;
								for (; i < argPosition; i++) {
									if (!gradToCompare.getArg(i).matches(valuesFound.get(i), null)) {
										/*
										 * Gradients are not compatible
										 */
										break;
									}
								}
								if (i == argPosition) {
									/*
									 * These two gradients are comparable
									 */
									compatibleFound = true;
									final double newVal = (Double) valuesFound.get(argPosition).getRootNodeData();
									final double oldVal = (Double) gradToCompare.getArg(argPosition).getRootNodeData();
									if (newVal < oldVal) {
										gradientsFound.set(j, new LsaMolecule(valuesFound));
									}
								}
							}
							if (!compatibleFound && ((Double) valuesFound.get(argPosition).getRootNodeData() < threshold)) {
								gradientsFound.add(new LsaMolecule(valuesFound));
							}
						}
					}
				}
			}
		} catch (UncomparableDistancesException e) {
			e.printStackTrace();
		}
		for (final ILsaMolecule grad : gradientsFound) {
			node.setConcentration(grad);
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append(": ");
		sb.append(source);
		if (context != null) {
			sb.append(", ");
			sb.append(context);
		}
		sb.append(" -");
		sb.append(getRate());
		sb.append("-> ");
		sb.append(gradient);
		sb.append(" : next scheduled @" + getTau());
		return sb.toString();
	}

}

