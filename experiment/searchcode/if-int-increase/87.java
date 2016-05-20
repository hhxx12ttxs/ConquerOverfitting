/** ------------------------------------------------------------------------- *
 * libpomdp
 * ========
 * File: AndOrTreeUpdateAdd.java
 * Description: extension of the AndOrTree class to implement
 *              online updates of the offline bounds using ADD's
 * Copyright (c) 2009, 2010 Diego Maniloff
 --------------------------------------------------------------------------- */

package libpomdp.solve.hybrid;

// imports
import java.io.PrintStream;
import java.util.Set;

import libpomdp.common.CustomVector;
import libpomdp.common.Pomdp;
import libpomdp.common.Utils;
import libpomdp.common.ValueFunction;
import libpomdp.common.add.PomdpAdd;
import libpomdp.common.add.ValueFunctionAdd;
import libpomdp.common.add.symbolic.DD;
import libpomdp.common.add.symbolic.DDleaf;
import libpomdp.common.add.symbolic.OP;
import libpomdp.solve.online.AndNode;
import libpomdp.solve.online.OrNode;
import libpomdp.solve.online.AndOrTree;
import libpomdp.solve.online.ExpandHeuristic;
import libpomdp.solve.online.HeuristicSearchAndNode;
import libpomdp.solve.online.HeuristicSearchOrNode;
import libpomdp.solve.hybrid.HybridValueIterationOrNode;

import org.math.array.DoubleArray;
import org.math.array.IntegerArray;

public class AndOrTreeUpdateAdd extends AndOrTree {

    // ------------------------------------------------------------------------
    // properties
    // ------------------------------------------------------------------------

    /// backup heuristic
    private BackupHeuristic bakH;

    // / supportSetSize[i] is the number of beliefs in the subtree of
    // / this node that are supported by alpha-vector i
    public CustomVector treeSupportSetSize;


    /// same constructor with backup heuristic
    public AndOrTreeUpdateAdd(Pomdp prob,
			      HybridValueIterationOrNode root,
			      ValueFunction L,
			      ValueFunction U,
			      ExpandHeuristic exph,
			      BackupHeuristic bakh) {
	super(prob, root, L, U, exph);

	this.bakH = bakh;
	this.treeSupportSetSize = new CustomVector(getLB().size());
	this.treeSupportSetSize.zero(); // not sure if this is necessary
    }

    /// Overridden initializer (is there another way???)
    //    public void init(BeliefState belief) {
    //	this.root.init(belief, -1, null);
    //	this.root.u = getUB().V(this.root.getBeliefState());
    //	this.root.l = getLB().V(this.root.getBeliefState());
    //	// should have separate plan ids to avoid this!
    //    }

    /**
     * expand(HybridValueIterationOrNode en):
     *
     * one-step expansion of |A||O| HybridValueIterationOrNodes
     * fully overridden here to access internals and have
     * more control - allows for greater speed
     */
    public void expand(HybridValueIterationOrNode en){
	// make sure this node hasn't been expanded before
	if (en.getChildren() != null) {
	    System.err.println("node cannot be expanded, it already has children");
	    return;
	}

	// declarations
	HybridValueIterationAndNode a;
	HybridValueIterationOrNode  o;
	// poba vector for each action
	CustomVector pOba;
	// save this node's old bounds
	double old_l = en.l;
	double old_u = en.u;
	// allocate space for the children AND nodes
	//en.children = new HybridValueIterationAndNode[getProblem().getnrAct()];
	//for(int action = 0; action < getProblem().getnrAct(); action++)
	//    en.children[action] = new HybridValueIterationAndNode();
	en.initChildren(getProblem().nrActions());
	// iterate through the AND nodes
	for (int action = 0; action < getProblem().nrActions(); action++) {
	    // type-cast, doon't yet know of a nicer way to do this
	    a = en.getChild(action);
	    // initialize this node, precompute and store Rba
	    a.init(action, en, getProblem().expectedImmediateReward(en.getBeliefState(), action));
	    // pre-compute observation probabilities for the children of this node
	    pOba = getProblem().observationProbabilities(en.getBeliefState(), action);
	    // allocate space for the children OR nodes
	    //	    a.children = new HybridValueIterationOrNode[getProblem().getnrObs()];
	    //	    for(int observation = 0; observation < getProblem().getnrObs(); observation++) {
	    //		if(pOba[observation] != 0)
	    //		    a.children[observation] = new HybridValueIterationOrNode();
	    //	    }
	    a.initChildren(getProblem().nrObservations(), pOba);
	    // iterate through new fringe OR nodes
	    for (int observation = 0; observation < getProblem().nrObservations(); observation++) {
		// type-cast, doon't yet know of a nicer way to do this
		o = a.getChild(observation);
		// ZERO-PROB OBSERVATIONS:
		// here we should continue the loop and avoid re-computing V^L and V^U
		// for belief nodes with poba == 0
		if (pOba.get(observation) == 0) {
		    // this should never happen now
		    System.err.println("SMTHIHNGS WRRRRONG");
		    continue;
		}
		// initialize this node with factored belief, set its poba
		o.init(getProblem().nextBeliefState(en.getBeliefState(),action,observation), observation, a);
		o.getBeliefState().setPoba(pOba.get(observation));
		// compute upper and lower bounds for this node
		o.u = getUB().V(o.getBeliefState());
		o.l = getLB().V(o.getBeliefState());
		// save one valid plan id for this andNode
		// may be saved multiple times, but it's ok
		a.validPlanid = o.getBeliefState().getAlphaVectorIndex();
		// H(b)
		o.h_b = expH.h_b(o);
		// H(b,a,o)
		o.h_bao = expH.h_bao(o);
		// H*(b) will be H(b) upon creation
		o.hStar = o.h_b;
		// bStar is a reference to itself since o is a fringe node
		o.bStar = o;
		// increase subtree size of en accordingly
		en.setSubTreeSize(en.getSubTreeSize() + 1);
		// add each of these to the support set sizes
		treeSupportSetSize.set(o.getBeliefState().getAlphaVectorIndex(),
			treeSupportSetSize.get(o.getBeliefState().getAlphaVectorIndex()) + 1);
	    } // HybridValueIterationOrNode loop

	    // L(b,a) = R(b,a) + \gamma \sum_o P(o|b,a)L(tao(b,a,o))
	    a.l = ANDpropagateL(a);
	    a.u = ANDpropagateU(a);
	    // observation in the path to the next node to expand
	    a.oStar = expH.oStar(a);
	    // H*(b,a)
	    a.hStar = expH.hANDStar(a);
	    // b*(b,a) - propagate ref of b*
	    a.bStar = a.getChild(a.oStar).bStar;

	    // the backup candidate and bakheuristic stay as in the
	    // initialization of the andNode since none of its children
	    // can be backed-up given that they are all fringe nodes
	    // we only allocate the right amount of space for bakHeuristicStar
	    a.bakHeuristicStar = new double[getLB().size()]; // all zeros
	    a.bakCandidate     = new HybridValueIterationOrNode[getLB().size()]; // all nulls
	}  // andNode loop

	// update values in en
	en.l = ORpropagateLexpand(en);
	en.u = ORpropagateU(en);
	// update H(b)
	en.h_b = expH.h_b(en);
	// H(b,a)
	en.h_ba = expH.h_ba(en);
	// a_b = argmax_a {H(b,a) * H*(b,a)}
	en.aStar = expH.aStar(en);
	// value of best heuristic in the subtree of en
	// H*(b) = H(b,a_b) * H*(b,a_b)
	en.hStar = expH.hORStar(en);
	// update reference to best fringe node in the subtree of en
	en.bStar = en.getChild(en.aStar).bStar;
	// one-step improvement
	en.oneStepDeltaLower = en.l - old_l;
	en.oneStepDeltaUpper = en.u - old_u;
	if(en.oneStepDeltaLower < 0) System.err.println("Hmmmmmmmmmmm");
	// compute backup heuristic for this newly expanded node
	en.bakHeuristic = bakH.h_b(en);
	// the backup candidate is still itself and it has its own value as best
	// we can now allocate the right amount of space for the bakheuristics
	en.bakHeuristicStar = new CustomVector(getLB().size());
	en.bakHeuristicStar.zero(); // all zeros
	en.bakCandidate = new HybridValueIterationOrNode[getLB().size()]; // all
									       // nulls
	en.bakHeuristicStar.set(en.getBeliefState().getAlphaVectorIndex(),
		en.bakHeuristic);
	en.bakCandidate[en.getBeliefState().getAlphaVectorIndex()] = en;
    } // (overridden) expand


    /**
     * updateAncestors:
     *
     * now keeps track of the best candidate node to backup
     * maintains |A| fringe lists
     */
    public void updateAncestors(HybridValueIterationOrNode n) {
	// make sure this is not the call after expanding the root
	// this should also fix the subTreeSize problem as well
	if (null == n.getChildren()) return;
	// if array.length does not count nulls, then we could use that here...
	// could also just keep n untouched, and use o from the beginning...
	int subTreeSizeDelta = n.getSubTreeSize();

	HybridValueIterationAndNode a;
	HybridValueIterationOrNode  o;

	while(n != getRoot()) { // reference comparison
	    // get the AND parent node
	    a = n.getParent();
	    // update the andNode that is parent of n
	    a.l = ANDpropagateL(a);
	    a.u = ANDpropagateU(a);
	    // best observation for expansion
	    a.oStar = expH.oStar(a);
	    // H*(b,a)
	    a.hStar = expH.hANDStar(a);
	    // b*(b,a) - propagate ref of b*
	    a.bStar = a.getChild(a.oStar).bStar;
	    // propagate references of backup candidates and its H value
	    for (int i = 0; i < getLB().size(); i++) {
		a.bakCandidate[i] = bakH.updateBakStar(a, n.getObs(), i);
	    }
	    // increase subtree size by the branching factor |A||O|
	    //a.subTreeSize += getProblem().getnrAct() * getProblem().getnrObs();

	    // get the OR parent of the parent
	    o = a.getParent();
	    // update the HybridValueIterationOrNode that is parent of the parent
	    o.l = ORpropagateL(o);
	    o.u = ORpropagateU(o);
	    // H(b,a)
	    o.h_ba = expH.h_ba(o);
	    // best action for expansion
	    o.aStar = expH.aStar(o);
	    // value of best heuristic in the subtree of en
	    o.hStar = o.h_ba[o.aStar] * o.getChild(o.aStar).hStar;
	    // update reference to best fringe node in the subtree of en
	    o.bStar = o.getChild(o.aStar).bStar;
	    // update reference of backup candidate and its H value
	    for (int i = 0; i < getLB().size(); i++) {
		o.bakCandidate[i] = bakH.updateBakStar(o, a.getAct(), i);
	    }
	    // increase subtree size accordingly
	    o.setSubTreeSize(o.getSubTreeSize() + subTreeSizeDelta);
	    // iterate (maybe better to say n = o ?)
	    n = n.getParent().getParent();
	}

    } // (overridden) updateAncestors


    /**
     * moveTree:
     *
     */
    @Override
    public void moveTree(OrNode newroot) {
	super.moveTree(newroot);
	// reset treeSupportSetSize
	this.treeSupportSetSize.zero();
    } // (overridden) moveTree


    @Override
    public PomdpAdd getProblem() {
	return (PomdpAdd) super.getProblem();
    }

    /**
     * ORpropagateLexpand:
     *
     * Need a special propagate function for the expand
     * case of an HybridValueIterationOrNode since we are now saving the
     * oneStepBestAction, but we cannot overwrite this
     * during updateAncestors since it is with respect to
     * the current lower bound.
     *
     * @param o
     * @return
     *
     * L(b) = max{max_a L(b,a), L(b)}
     */
    protected double ORpropagateLexpand(HybridValueIterationOrNode o) {
	// construct array with L(b,a)
	double Lba[] = new double[getProblem().nrActions()];
	for(HybridValueIterationAndNode a : o.getChildren())
	    Lba[a.getAct()] = a.l;
	o.oneStepBestAction = Utils.argmax(Lba);
	// compare to current bound
	return Math.max(Lba[o.oneStepBestAction], o.l);
    } //  ORpropagateLexpand


    /**
     * backup the lower bound at the root node - not used for now
     */
    public double[] backupLowerAtRoot() {
	// decls
	DD gamma  = DDleaf.myNew(getProblem().getGamma());
	DD gab    = DD.zero;
	int bestA = currentBestAction(); // consider caching this value maybe
	DD lowerBound [] = ((ValueFunctionAdd)getLB()).getvAdd();
	// \sum_o g_{a,o}^i
	for(HybridValueIterationOrNode o : getRoot().getChild(bestA).getChildren()) {
	    //if(o==null) continue;
	    // compute g_{a,o}^{planid}
	    // getProblem().gao(lowerBound[o.belief.getplanid()], bestA, o.getobs()).display();
	    gab = OP.add(gab, getProblem().
		    gao(lowerBound[o.getBeliefState().getAlphaVectorIndex()], bestA, o.getObs()));
	}
	// multiply result by discount factor and add it to r_a
	gab = OP.mult(gamma, gab);
	gab = OP.add(getProblem().R[bestA], gab);
	return OP.convert2array(gab, getProblem().getstaIds());

    } // backupLowerAtRoot


    /**
     * backupLowerAtNode:
     *
     * backup the lower bound at the given HybridValueIterationOrNode
     * and update the offline lower bound by adding the
     * new alpha vector to the value function representation
     * Using the current info from the tree, a backup operation is
     * reduced to computing a particular gab vector
     */
    public ValueFunction backupLowerAtNode(HybridValueIterationOrNode on) {
	// make sure this node is not in the fringe
	if(null == on.getChildren()) {
	    System.err.println("Attempted to backup a fringe node");
	    return null;
	}
	// decls
	DD gamma  = DDleaf.myNew(getProblem().getGamma());
	DD gab    = DD.zero;
	//int bestA = currentBestActionAtNode(on); // consider caching this value maybe
	int obs   = 0;
	DD lowerBound [] = ((ValueFunctionAdd)getLB()).getvAdd();
	// \sum_o g_{a,o}^i
	for(HybridValueIterationOrNode o : on.getChild(on.oneStepBestAction).getChildren()) {
	    if(o==null) {
		// in this case we use any valid supporting alpha to compute g_{a,o}^{i}
		gab = OP.add(gab, getProblem().
			gao(lowerBound[on.getChild(on.oneStepBestAction).validPlanid],
				on.oneStepBestAction,
				obs));
	    } else {
		// compute g_{a,o}^{planid}
		gab = OP.add(gab, getProblem().
			gao(lowerBound[o.getBeliefState().getAlphaVectorIndex()],
				on.oneStepBestAction,
				obs));
	    }
	    // iterate counter
	    obs++;
	}
	// multiply result by discount factor and add it to r_a
	gab = OP.mult(gamma, gab);
    gab = OP.add(getProblem().R[on.oneStepBestAction], gab);
	// add newly computed vector to the tree's offline lower bound - NO
	// PRUNING FOR NOW
	ValueFunctionAdd newLB = new ValueFunctionAdd(
		Utils.append(lowerBound, gab),
		getProblem().getstaIds(),
		Utils.horzCat(
                      getLB().getActions(),
                      on.oneStepBestAction));
	setLB(newLB);

    // return
	return newLB;
	// how about coding a union operation in ValueFunction?
	// this function does not watch for repeated vectors yet
    } // backupLowerAtNode

    @Override
    public HybridValueIterationOrNode getRoot() {
	return (HybridValueIterationOrNode) super.getRoot();
    }

    /**
     * expectedReuse:
     * calculate expected # of belief nodes
     * to reuse given current expanded tree and
     * best action to execute
     */
    public double expectedReuse() {
	int bestA   = currentBestAction();
	double expR = 0;
	for(HybridValueIterationOrNode o : getRoot().getChild(bestA).getChildren()) {
	    // null nodes do not contribute to this sum
	    if(o!=null) expR += o.getBeliefState().getPoba() * o.getSubTreeSize();
	}
	return expR;
    } // expectedReuse

    /// reuse ratio
    /// correct the extra |A||O| in subTreeSize of root node
    public double expectedReuseRatio() {
	return expectedReuse() /
	(getRoot().getSubTreeSize() - getProblem().nrObservations() * getProblem().nrActions());
    } // expectedReuseRatio

    /// overriden here to print the backupHeuristic of each node
    /// output a dot-formatted file to print the tree
    /// starting from a given HybridValueIterationOrNode
    @Override
    public void printdot(String filename) {
	HybridValueIterationOrNode root = getRoot();
	PrintStream out = null;
	try {
	    out = new
	    PrintStream(filename);
	}catch(Exception e) {
	    System.err.println(e.toString());
	}
	//out = System.out;
	// print file headers
	out.println("digraph T {");
	// print node
	orprint((HybridValueIterationOrNode) root,out);
	// print closing
	out.println("}");
    }

    /// print HybridValueIterationOrNode
    private void orprint(HybridValueIterationOrNode o, PrintStream out) {
	// print this node
	@SuppressWarnings("unused")
	String b = "";
	if (getProblem().nrStates() < 4)
	    b = "b=[" + DoubleArray.toString("%.2f",
		    o.getBeliefState().getPoint().getArray()) + "]\\n";
	out.format(o.hashCode() + "[label=\"" +
		//b +
		"U(b)= %.2f\\n" +
		"L(b)= %.2f\\n" +
		"expH(b)= %.2f\\n" +
		"expH*(b)= %.2f\\n" +
		"bakH(b)= %.2f" +
		"\"];\n", o.u, o.l, o.h_b, o.hStar, o.bakHeuristic);
	// every or node has a reference to the best node to expand in its subtree
	out.println(o.hashCode() + "->" + o.bStar.hashCode() +
	"[label=\"b*\",weight=0,color=blue];");
	// if this is the root, then print an edge to the best candidate node to backup
	if (o == getRoot()) {
	    System.err.println("lenght is" + treeSupportSetSize.size());
	    double nstar[] = new double[treeSupportSetSize.size()];
	    for (int i = 0; i < treeSupportSetSize.size(); i++) {
		nstar[i] = treeSupportSetSize.get(i) / o.getSubTreeSize();
		System.err.println(nstar[i]);
	    }
	    double f[] = new double[treeSupportSetSize.size()];
	    for (int i = 0; i < treeSupportSetSize.size(); i++) {
		f[i] = o.bakHeuristicStar.get(i) * nstar[i];
		System.err.println(f[i]);
	    }
	    int istar = Utils.argmax(f);
	    System.err.println(istar);
	    if (f[istar] > 0) out.println(o.hashCode() + "->" + o.bakCandidate[istar].hashCode() +
		    "[label=\"bakCandidate\",weight=0,color=orange];");
	}
	// check it's not in the fringe before calling andprint
	if (o.getChildren() == null) return;
	// print outgoing edges from this node
	for(AndNode a : o.getChildren()) {
	    out.print(o.hashCode() + "->" + a.hashCode() +
		    "[label=\"" +
		    "H(b,a)=" + o.h_ba[a.getAct()] +
	    "\"];");
	}
	out.println();
	// recurse
	for(HybridValueIterationAndNode a : o.getChildren()) andprint(a, out);
    }

    /// print andNode
    protected void andprint(HybridValueIterationAndNode a, PrintStream out) {
	// print this node
	out.format(a.hashCode() + "[label=\"" +
		   "a=" + getProblem().getActionString(a.getAct()) + "\\n" +
		   "U(b,a)= %.2f\\n" +
		   "L(b,a)= %.2f" +
		   "\"];\n", a.u, a.l);

	// print outgoing edges for this node
	for(HybridValueIterationOrNode o : a.getChildren()) {
	    if (!(o==null))
		out.format(a.hashCode() + "->" + o.hashCode() +
			   "[label=\"" +
			   "obs: " + getProblem().getObservationString(o.getObs()) + "\\n" +
			   "P(o|b,a)= %.2f\\n" +
			   "H(b,a,o)= %.2f" +
			   "\"];",
			   o.getBeliefState().getPoba(),
			   o.h_bao);
	}
	out.println();

	// every or node has a reference to the best node to backup in its subtree
	//out.println(a.hashCode() + "->" + a.bakCandidate.hashCode() +
	//"[label=\"bakCandidate\",weight=0,color=orange];");

	// recurse
	for(HybridValueIterationOrNode o : a.getChildren()) if(!(o==null)) orprint(o,out);
    }

} // AndOrTreeUpdateAdd

