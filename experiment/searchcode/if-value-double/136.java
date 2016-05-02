/**
 * Author: Pascal Poupart (ppoupart@cs.uwaterloo.ca)
 * Reference: Chapter 5 of Poupart's PhD thesis
 * (http://www.cs.uwaterloo.ca/~ppoupart/publications/ut-thesis/ut-thesis.pdf)
 * NOTE: Parts of this code might have been modified for use by libpomdp
 *       - Diego Maniloff
 */

package libpomdp.common.add.symbolic;

import java.util.*;
import java.io.*;

public class OP {

    //////////////////////////////////////////////////////
    // add 2 DDs
    //////////////////////////////////////////////////////
    public static DD add(DD dd1, DD dd2) {

	// dd1 precedes dd2
	if (dd1.getVar() > dd2.getVar()) {

	    if (dd2.getVar() == 0 && dd2.getVal() == 0 && dd2.getConfig() == null)
		return dd1;

	    Pair pair = new Pair(dd1,dd2);
	    DD storedResult = (DD)Global.addHashtable.get(pair);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd1.getChildren().length];
	    for (int i=0; i<dd1.getChildren().length; i++) {
		children[i] = OP.add(dd1.getChildren()[i],dd2);
	    }
	    DD result = DDnode.myNew(dd1.getVar(),children);
	    Global.addHashtable.put(pair,result);
	    return result;
	}

	// dd2 precedes dd1 {
	else if (dd2.getVar() > dd1.getVar()) {

	    if (dd1.getVar() == 0 && dd1.getVal() == 0 && dd1.getConfig() == null)
		return dd2;

	    Pair pair = new Pair(dd1,dd2);
	    DD storedResult = (DD)Global.addHashtable.get(pair);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd2.getChildren().length];
	    for (int i=0; i<dd2.getChildren().length; i++) {
		children[i] = OP.add(dd2.getChildren()[i],dd1);
	    }
	    DD result = DDnode.myNew(dd2.getVar(),children);
	    Global.addHashtable.put(pair,result);
	    return result;
	}

	// dd2 and dd1 have same root var
	else if (dd1.getVar() > 0) {

	    Pair pair = new Pair(dd1,dd2);
	    DD storedResult = (DD)Global.addHashtable.get(pair);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd1.getChildren().length];
	    for (int i=0; i<dd1.getChildren().length; i++) {
		children[i] = OP.add(dd1.getChildren()[i],dd2.getChildren()[i]);
	    }
	    DD result = DDnode.myNew(dd1.getVar(),children);
	    Global.addHashtable.put(pair,result);
	    return result;
	}

	// dd1 and dd2 are leaves
	else {
	    double newVal = dd1.getVal() + dd2.getVal();
	    int[][] newConfig = Config.merge(dd1.getConfig(),dd2.getConfig());
	    return DDleaf.myNew(newVal,newConfig);
	}
    }

    //////////////////////////////////////////////////////
    // subtract 2 DDs
    //////////////////////////////////////////////////////
    public static DD sub(DD dd1, DD dd2) {
	return OP.add(dd1, OP.neg(dd2));
    }

    //////////////////////////////////////////////////////
    // add N DDs
    //////////////////////////////////////////////////////
    public static DD addN(DD[] ddArray) {
	DD ddSum = DD.zero;
	for (int i=0; i<ddArray.length; i++) {
	    ddSum = OP.add(ddSum,ddArray[i]);
	}
	return ddSum;
    }

    public static DD addN(DD dd) {
	return dd;
    }

    public static DD addN(Collection dds) {
	DD ddSum = DD.zero;
	Iterator ddIterator = dds.iterator();
	while (ddIterator.hasNext()) {
	    DD dd = (DD)ddIterator.next();
	    ddSum = OP.add(ddSum,dd);
	}
	return ddSum;
    }

    //////////////////////////////////////////////////////
    // absolute value of a DD
    //////////////////////////////////////////////////////
    public static DD abs(DD dd) {
				
	// dd is a leaf
	if (dd.getVar() == 0) {
	    if (dd.getVal() >= 0) return dd;
	    else return DDleaf.myNew(-dd.getVal(), dd.getConfig());
	}

	// dd is a node
	else {
	    DD children[];
	    children = new DD[dd.getChildren().length];
	    for (int i=0; i<dd.getChildren().length; i++) {
		children[i] = OP.abs(dd.getChildren()[i]);
	    }
	    return DDnode.myNew(dd.getVar(),children);
	}
    }

    //////////////////////////////////////////////////////
    // negate a DD
    //////////////////////////////////////////////////////
    public static DD neg(DD dd) {
				
	// dd is a leaf
	if (dd.getVar() == 0)
	    return DDleaf.myNew(-dd.getVal(), dd.getConfig());

	// dd is a node
	else {
	    DD children[];
	    children = new DD[dd.getChildren().length];
	    for (int i=0; i<dd.getChildren().length; i++) {
		children[i] = OP.neg(dd.getChildren()[i]);
	    }
	    return DDnode.myNew(dd.getVar(),children);
	}
    }

    //////////////////////////////////////////////////////
    // shift variable index by of all variables by n (useful when priming variables in value iteration)
    //////////////////////////////////////////////////////
    public static DD primeVars(DD dd, int n) {
	HashMap hashtable = new HashMap();
	return primeVars(dd,n,hashtable);
    }

    public static DD primeVars(DD dd, int n, HashMap hashtable) {
				
	// dd is a leaf
	if (dd.getVar() == 0)
	    return dd;

	// dd is a node
	else {
	    DD result = (DD)hashtable.get(dd);
	    if (result != null) return result;

	    DD children[];
	    children = new DD[dd.getChildren().length];
	    for (int i=0; i<dd.getChildren().length; i++) {
		children[i] = OP.primeVars(dd.getChildren()[i],n);
	    }
	    result = DDnode.myNew(dd.getVar()+n,children);
	    hashtable.put(dd,result);
	    return result;
	}
    }

    public static DD[] primeVarsN(DD[] dds, int n) {
	DD[] primedDds = new DD[dds.length];
	for (int i=0; i<dds.length; i++) {
	    primedDds[i] = OP.primeVars(dds[i],n); 
	}
	return primedDds;
    }

    //////////////////////////////////////////////////////
    // swap variables
    //////////////////////////////////////////////////////
    public static DD[] swapVars(DD[] ddArray, int[][] varMapping) {
	DD[] results = new DD[ddArray.length];
	for (int i=0; i<ddArray.length; i++) {
	    int[][] relevantVarMapping = Config.intersection(varMapping,ddArray[i].getVarSet());
	    results[i] = swapVars(ddArray[i], relevantVarMapping);
	}
	return results;
    }

    public static DD swapVars(DD dd, int[][] varMapping) {
	if (varMapping == null || varMapping[0].length == 0) return dd;
	else return OP.reorder(swapVarsNoReordering(dd, varMapping));
    }

    public static DD swapVarsNoReordering(DD dd, int[][] varMapping) {
				
	// dd is a leaf
	if (dd.getVar() == 0 || varMapping[0].length == 0)
	    return dd;

	// dd is a node
	else {
	    int idx = MySet.find(varMapping[0],dd.getVar());
	    DD[] children = new DD[dd.getChildren().length];

	    // swap variable
	    if (idx != -1) {
		int[][] restMapping = Config.removeIth(varMapping,idx);
		for (int i=0; i<children.length; i++) 
		    children[i] = OP.swapVarsNoReordering(dd.getChildren()[i],restMapping);
		return DDnode.myNew(varMapping[1][idx],children);           
	    }
                        
	    // root is not swapped
	    else {
		for (int i=0; i<children.length; i++) 
		    children[i] = OP.swapVarsNoReordering(dd.getChildren()[i],varMapping);
		return DDnode.myNew(dd.getVar(),children);  
	    }
	}
    }

    //////////////////////////////////////////////////////
    // multiply 2 DDs
    //////////////////////////////////////////////////////
    public static DD mult(DD dd1, DD dd2) {

	// dd1 precedes dd2
	if (dd1.getVar() > dd2.getVar()) {

	    if (dd2.getVar() == 0 && dd2.getVal() == 0)
		return dd2;
	    else if (dd2.getVar() == 0 && dd2.getVal() == 1 && dd2.getConfig() == null)
		return dd1;

	    Pair pair = new Pair(dd1,dd2);
	    DD storedResult = (DD)Global.multHashtable.get(pair);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd1.getChildren().length];
	    for (int i=0; i<dd1.getChildren().length; i++) {
		children[i] = OP.mult(dd1.getChildren()[i],dd2);
	    }
	    DD result = DDnode.myNew(dd1.getVar(),children);
	    Global.multHashtable.put(pair,result);
	    return result;
	}

	// dd2 precedes dd1 {
	else if (dd2.getVar() > dd1.getVar()) {

	    if (dd1.getVar() == 0 && dd1.getVal() == 0)
		return dd1;
	    else if (dd1.getVar() == 0 && dd1.getVal() == 0 && dd1.getConfig() == null)
		return dd2;

	    Pair pair = new Pair(dd1,dd2);
	    DD storedResult = (DD)Global.multHashtable.get(pair);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd2.getChildren().length];
	    for (int i=0; i<dd2.getChildren().length; i++) {
		children[i] = OP.mult(dd2.getChildren()[i],dd1);
	    }
	    DD result = DDnode.myNew(dd2.getVar(),children);
	    Global.multHashtable.put(pair,result);
	    return result;
	}

	// dd2 and dd1 have same root var
	else if (dd1.getVar() > 0) {

	    Pair pair = new Pair(dd1,dd2);
	    DD storedResult = (DD)Global.multHashtable.get(pair);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd1.getChildren().length];
	    for (int i=0; i<dd1.getChildren().length; i++) {
		children[i] = OP.mult(dd1.getChildren()[i],dd2.getChildren()[i]);
	    }
	    DD result = DDnode.myNew(dd1.getVar(),children);
	    Global.multHashtable.put(pair,result);
	    return result;
	}

	// dd1 and dd2 are leaves
	else {
	    double newVal = dd1.getVal() * dd2.getVal();
	    int[][] newConfig = Config.merge(dd1.getConfig(),dd2.getConfig());
	    return DDleaf.myNew(newVal,newConfig);
	}
    }

    //////////////////////////////////////////////////////
    // divide 2 DDs
    //////////////////////////////////////////////////////
    public static DD div(DD dd1, DD dd2) {
	return OP.mult(dd1, OP.inv(dd2));
    }

    //////////////////////////////////////////////////////
    // multiply N DDs
    //////////////////////////////////////////////////////
    public static DD multN(DD[] ddArray) {
	DD ddProd = DD.one;
	for (int i=0; i<ddArray.length; i++) {
	    ddProd = OP.mult(ddProd,ddArray[i]);
	}
	return ddProd;
    }

    public static DD multN(DD dd) {
	return dd;
    }

    public static DD multN(Collection dds) {
	DD ddProd = DD.one;
	Iterator ddIterator = dds.iterator();
	while (ddIterator.hasNext()) {
	    DD dd = (DD)ddIterator.next();
	    ddProd = OP.mult(ddProd,dd);
	}
	return ddProd;
    }

    //////////////////////////////////////////////////////
    // inverse of a DD
    //////////////////////////////////////////////////////
    public static DD inv(DD dd) {
				
	// dd is a leaf
	if (dd.getVar() == 0)
	    return DDleaf.myNew(1/dd.getVal(), dd.getConfig());

	// dd is a node
	else {
	    DD children[];
	    children = new DD[dd.getChildren().length];
	    for (int i=0; i<dd.getChildren().length; i++) {
		children[i] = OP.inv(dd.getChildren()[i]);
	    }
	    return DDnode.myNew(dd.getVar(),children);
	}
    }


    //////////////////////////////////////////////////////
    // nat log of a DD (should make this general for any f)
    //////////////////////////////////////////////////////
    public static DD log(DD dd) {
				
	// dd is a leaf
	if (dd.getVar() == 0)
	    return DDleaf.myNew(Math.log(dd.getVal()), dd.getConfig());

	// dd is a node
	else {
	    DD children[];
	    children = new DD[dd.getChildren().length];
	    for (int i=0; i<dd.getChildren().length; i++) {
		children[i] = OP.log(dd.getChildren()[i]);
	    }
	    return DDnode.myNew(dd.getVar(),children);
	}
    }

    //////////////////////////////////////////////////////
    // replace val1 with val2 in a DD
    //////////////////////////////////////////////////////
    public static DD replace(DD dd, double val1, double val2) {
				
	// dd is a leaf
	if (dd.getVar() == 0) {
	    if (dd.getVal() == val1) return DDleaf.myNew(val2, dd.getConfig());
	    else return dd;
	}
						

	// dd is a node
	else {
	    DD children[];
	    children = new DD[dd.getChildren().length];
	    for (int i=0; i<dd.getChildren().length; i++) {
		children[i] = OP.replace(dd.getChildren()[i], val1, val2);
	    }
	    return DDnode.myNew(dd.getVar(),children);
	}
    }

    //////////////////////////////////////////////////////
    // addout (sumout) a variable from a DD
    //////////////////////////////////////////////////////
    public static DD addout(DD dd, int var) {

	HashMap hashtable = new HashMap();
	return addout(dd, var, hashtable);
    }

    public static DD addout(DD dd, int var, HashMap hashtable) {

	// it's a leaf
	if (dd.getVar() == 0) {
	    return DDleaf.myNew(Global.varDomSize[var-1]*dd.getVal(), dd.getConfig());
	}

	DD result = (DD)hashtable.get(dd);
	if (result != null) return result;

	// root is variable that must be eliminated
	if (dd.getVar() == var) {
	    // have to collapse all children into a new node
	    result = OP.addN(dd.getChildren());
	}

	// descend down the tree until 'var' is found
	else {
	    DD children[];
	    children = new DD[dd.getChildren().length];
	    for (int i=0; i<dd.getChildren().length; i++) {
		children[i] = OP.addout(dd.getChildren()[i], var);
	    }
	    result = DDnode.myNew(dd.getVar(),children);
	}

	// store result
	hashtable.put(dd,result);
	return result;
    }

    //////////////////////////////////////////////////////
    // selectVarGreedily
    //////////////////////////////////////////////////////
    public static int selectVarGreedily(DD[] ddArray, int[] vars) {

	// estimate cost of eliminating each var
	double bestSize = Double.POSITIVE_INFINITY;
	int bestVar = 0;
        for (int i=0; i<vars.length; i++) {
            int[] newVarSet = new int[0];
	    double sizeEstimate = 1;
	    int nAffectedDds = 0;
	    for (int ddId=0; ddId<ddArray.length; ddId++) {
		if (ddArray[ddId] == null)
		    System.out.println("ddArray[" + ddId + "] is null");
                int[] varSet = ddArray[ddId].getVarSet();
                if (MySet.find(varSet,vars[i]) >= 0) {
                    newVarSet = MySet.union(varSet,newVarSet);
		    sizeEstimate *= ddArray[ddId].getNumLeaves();
		    nAffectedDds += 1;
		}
	    }

	    // # of affected DDs <= 1 or # of vars is <= 2
	    if (nAffectedDds <= 1 || newVarSet.length <= 2) {
		return vars[i];
	    }

	    // compute sizeUpperBound:
	    // sizeUpperBound = min(sizeEstimate, prod(varDomSize(newScope)));
	    double sizeUpperBound = 1;
            for (int j=0; j<newVarSet.length; j++) {
		sizeUpperBound *= Global.varDomSize[newVarSet[j]-1];
		if (sizeUpperBound >= sizeEstimate) break;
	    }
	    if (sizeUpperBound < sizeEstimate) sizeEstimate = sizeUpperBound;

	    // revise bestVar
	    if (sizeUpperBound < bestSize) {
		bestSize = sizeUpperBound;
		bestVar = vars[i];
	    }
	}
	return bestVar;
    }

    //////////////////////////////////////////////////////
    // dotProductNoMem  (Set container, don't store results)
    //////////////////////////////////////////////////////
    public static double dotProductNoMem(DD dd1, DD dd2, SortedSet<Integer> vars) {

	// should cache results to speed up things a little

	if ((dd1.getVar() == 0 && dd1.getVal() == 0) || 
	    (dd2.getVar() == 0 && dd2.getVal() == 0))
	    return 0;

	// dd1 precedes dd2
	if (dd1.getVar() > dd2.getVar()) {

	    //Pair pair = new Pair(dd1,dd2);
	    //DD storedResult = (DD)Global.addHashtable.get(pair);
	    //if (storedResult != null) return storedResult;

	    SortedSet<Integer> remainingVars = new TreeSet<Integer>(vars);
	    remainingVars.remove(new Integer(dd1.getVar()));
	    double dp = 0;
	    for (int i=0; i<dd1.getChildren().length; i++) {
		dp += OP.dotProductNoMem(dd1.getChildren()[i],dd2,remainingVars);
	    }
	    return dp;

	    //DD result = DDnode.myNew(dd1.getVar(),children);
	    //Global.addHashtable.put(pair,result);
	    //return result;
	}

	// dd2 precedes dd1 {
	else if (dd2.getVar() > dd1.getVar()) {

	    //Pair pair = new Pair(dd1,dd2);
	    //DD storedResult = (DD)Global.addHashtable.get(pair);
	    //if (storedResult != null) return storedResult;

	    SortedSet<Integer> remainingVars = new TreeSet<Integer>(vars);
	    remainingVars.remove(new Integer(dd2.getVar()));
	    double dp = 0;
	    for (int i=0; i<dd2.getChildren().length; i++) {
		dp += OP.dotProductNoMem(dd2.getChildren()[i],dd1,remainingVars);
	    }
	    return dp;

	    //DD result = DDnode.myNew(dd2.getVar(),children);
	    //Global.addHashtable.put(pair,result);
	    //return result;
	}

	// dd2 and dd1 have same root var
	else if (dd1.getVar() > 0) {

	    //Pair pair = new Pair(dd1,dd2);
	    //DD storedResult = (DD)Global.addHashtable.get(pair);
	    //if (storedResult != null) return storedResult;

	    SortedSet<Integer> remainingVars = new TreeSet<Integer>(vars);
	    remainingVars.remove(new Integer(dd1.getVar()));
	    double dp = 0;
	    for (int i=0; i<dd1.getChildren().length; i++) {
		dp += OP.dotProductNoMem(dd1.getChildren()[i],dd2.getChildren()[i],remainingVars);
	    }
	    return dp;

	    //DD result = DDnode.myNew(dd1.getVar(),children);
	    //Global.addHashtable.put(pair,result);
	    //return result;
	}

	// dd1 and dd2 are leaves
	else {
	    double result = dd1.getVal() * dd2.getVal();
	    Iterator varIterator = vars.iterator();
	    while(varIterator.hasNext()) {
		Integer var = (Integer)varIterator.next();
		result *= Global.varDomSize[var.intValue()-1];
	    }
	    return result;
	}
    }

    //////////////////////////////////////////////////////
    // dotProduct2  (My set, store results)
    //////////////////////////////////////////////////////
    public static double[][] dotProductLeafPrune(DD[] dd1Array, DD[] dd2Array, int[] vars) {
	double[][] results = new double[dd1Array.length][dd2Array.length];
	for (int i=0; i<dd1Array.length; i++) {
	    for (int j=0; j<dd2Array.length; j++) {
		results[i][j] = dotProductLeafPrune(dd1Array[i], dd2Array[j], vars);
	    }
	}
	return results;
    }

    public static double[] dotProductLeafPrune(DD dd1, DD[] dd2Array, int[] vars) {
	double[] results = new double[dd2Array.length];
	for (int i=0; i<dd2Array.length; i++) {
	    results[i] = dotProductLeafPrune(dd1, dd2Array[i], vars);
	}
	return results;
    }
         
    public static double[] dotProductLeafPrune(DD[] dd1Array, DD dd2, int[] vars) {
	double[] results = new double[dd1Array.length];
	for (int i=0; i<dd1Array.length; i++) {
	    results[i] = dotProductLeafPrune(dd1Array[i], dd2, vars);
	}
	return results;
    }
         
    public static double dotProductLeafPrune(DD dd1, DD dd2, int[] vars) {

	// dd1 is a leaf
	if (dd1.getVar() == 0) {
	    double dd1Val = dd1.getVal();
	    if (dd1Val == 0) return 0;
	    double dd2Sum = dd2.getSum();
	    if (dd2Sum == 0) return 0;
	    int[] remainingVars = MySet.diff(vars,dd2.getVarSet());
	    int multiplicativeFactor = 1;
	    for (int j=0; j<remainingVars.length; j++) 
		multiplicativeFactor *= Global.varDomSize[remainingVars[j]-1];
	    return dd1Val * multiplicativeFactor * dd2Sum;
	}

	// dd2 is a leaf
	if (dd2.getVar() == 0) {
	    double dd2Val = dd2.getVal();
	    if (dd2Val == 0) return 0;
	    double dd1Sum = dd1.getSum();
	    if (dd1Sum == 0) return 0;
	    int[] remainingVars = MySet.diff(vars,dd1.getVarSet());
	    int multiplicativeFactor = 1;
	    for (int j=0; j<remainingVars.length; j++) 
		multiplicativeFactor *= Global.varDomSize[remainingVars[j]-1];
	    return dd2Val * multiplicativeFactor * dd1Sum;
	}

	// dd2 and dd1 have same root var
	if (dd1.getVar() == dd2.getVar()) {
                    
	    TripletSet triplet = new TripletSet(dd1,dd2, vars);
	    Double storedResult = (Double)Global.dotProductHashtable.get(triplet);
	    if (storedResult != null) return storedResult.doubleValue();

	    int[] remainingVars = MySet.remove(vars,dd1.getVar());
	    double dp = 0;
	    for (int i=0; i<dd1.getChildren().length; i++) {
		dp += OP.dotProductLeafPrune(dd1.getChildren()[i],dd2.getChildren()[i],remainingVars);
	    }
	    Global.dotProductHashtable.put(triplet,new Double(dp));
	    return dp;
	}

	// dd1's root precedes dd2's root
	if (dd1.getVar() > dd2.getVar()) {
                    
	    TripletSet triplet = new TripletSet(dd1,dd2, vars);
	    Double storedResult = (Double)Global.dotProductHashtable.get(triplet);
	    if (storedResult != null) return storedResult.doubleValue();

	    int[] remainingVars = MySet.remove(vars,dd1.getVar());
	    double dp = 0;
	    for (int i=0; i<dd1.getChildren().length; i++) {
		dp += OP.dotProductLeafPrune(dd1.getChildren()[i],dd2,remainingVars);
	    }
	    Global.dotProductHashtable.put(triplet,new Double(dp));
	    return dp;
	}

	// dd2's root precedes dd1's root
	else {
                    
	    TripletSet triplet = new TripletSet(dd1,dd2, vars);
	    Double storedResult = (Double)Global.dotProductHashtable.get(triplet);
	    if (storedResult != null) return storedResult.doubleValue();

	    int[] remainingVars = MySet.remove(vars,dd2.getVar());
	    double dp = 0;
	    for (int i=0; i<dd2.getChildren().length; i++) {
		dp += OP.dotProductLeafPrune(dd1,dd2.getChildren()[i],remainingVars);
	    }
	    Global.dotProductHashtable.put(triplet,new Double(dp));
	    return dp;
	}

	/*
	// dd1 and dd2 have different root variables
	else {
                    
	// find common variable
	int commonVar = 0;
	int[] dd1VarSet = dd1.getVarSet();
	int[] dd2VarSet = dd2.getVarSet();
	int ptr1 = dd1VarSet.length-1;
	int ptr2 = dd2VarSet.length-1;
	while (ptr1 >= 0 && ptr2 >= 0) {
	if (dd1VarSet[ptr1] == dd2VarSet[ptr2]) {
	commonVar = dd1VarSet[ptr1];
	break;
	}
	else if (dd1VarSet[ptr1] > dd2VarSet[ptr2]) ptr1--;
	else ptr2--;
	} 
                        
	// there is a common variable
	if (commonVar > 0) {
	int[] remainingVars = MySet.remove(vars,commonVar);
	double dp = 0;
	int[][] config = new int[2][1];
	config[0][0] = commonVar;
	for (int i=0; i<Global.varDomSize[commonVar-1]; i++) {
	config[1][0] = i+1;
	dp += OP.dotProductLeafPrune(OP.restrictOrdered(dd1,config),OP.restrictOrdered(dd2,config),remainingVars);
	}
	return dp;
	}
                    
	// no common variable
	else {
	int[] remainingVars = MySet.diff(MySet.diff(vars,dd1VarSet),dd2VarSet);
	int multiplicativeFactor = 1;
	for (int j=0; j<remainingVars.length; j++) 
	multiplicativeFactor *= Global.varDomSize[remainingVars[j]-1];
	return multiplicativeFactor * dd1.getSum() * dd2.getSum();
	}
	} */

    }

        
    //////////////////////////////////////////////////////
    // dotProduct  (My set, store results)
    //////////////////////////////////////////////////////
    public static double[][] dotProduct(DD[] dd1Array, DD[] dd2Array, int[] vars) {
	double[][] results = new double[dd1Array.length][dd2Array.length];
	for (int i=0; i<dd1Array.length; i++) {
	    for (int j=0; j<dd2Array.length; j++) {
		results[i][j] = dotProduct(dd1Array[i], dd2Array[j], vars);
	    }
	}
	return results;
    }
         
    public static double[] dotProduct(DD dd1, DD[] dd2Array, int[] vars) {
	double[] results = new double[dd2Array.length];
	for (int i=0; i<dd2Array.length; i++) {
	    results[i] = dotProduct(dd1, dd2Array[i], vars);
	}
	return results;
    }
         
    public static double[] dotProduct(DD[] dd1Array, DD dd2, int[] vars) {
	double[] results = new double[dd1Array.length];
	for (int i=0; i<dd1Array.length; i++) {
	    results[i] = dotProduct(dd1Array[i], dd2, vars);
	}
	return results;
    }
         
    public static double dotProduct(DD dd1, DD dd2, int[] vars) {

	if ((dd1.getVar() == 0 && dd1.getVal() == 0) || 
	    (dd2.getVar() == 0 && dd2.getVal() == 0))
	    return 0;

	// dd1 precedes dd2
	if (dd1.getVar() > dd2.getVar()) {

	    TripletSet triplet = new TripletSet(dd1,dd2, vars);
	    Double storedResult = (Double)Global.dotProductHashtable.get(triplet);
	    if (storedResult != null) return storedResult.doubleValue();

	    int[] remainingVars = MySet.remove(vars,dd1.getVar());
	    double dp = 0;
	    for (int i=0; i<dd1.getChildren().length; i++) {
		dp += OP.dotProduct(dd1.getChildren()[i],dd2,remainingVars);
	    }
	    Global.dotProductHashtable.put(triplet,new Double(dp));
	    return dp;
	}

	// dd2 precedes dd1 {
	else if (dd2.getVar() > dd1.getVar()) {

	    TripletSet triplet = new TripletSet(dd1,dd2, vars);
	    Double storedResult = (Double)Global.dotProductHashtable.get(triplet);
	    if (storedResult != null) return storedResult.doubleValue();

	    int[] remainingVars = MySet.remove(vars,dd2.getVar());
	    double dp = 0;
	    for (int i=0; i<dd2.getChildren().length; i++) {
		dp += OP.dotProduct(dd2.getChildren()[i],dd1,remainingVars);
	    }
	    Global.dotProductHashtable.put(triplet,new Double(dp));
	    return dp;
	}

	// dd2 and dd1 have same root var
	else if (dd1.getVar() > 0) {

	    TripletSet triplet = new TripletSet(dd1,dd2, vars);
	    Double storedResult = (Double)Global.dotProductHashtable.get(triplet);
	    if (storedResult != null) return storedResult.doubleValue();

	    int[] remainingVars = MySet.remove(vars,dd1.getVar());
	    double dp = 0;
	    for (int i=0; i<dd1.getChildren().length; i++) {
		dp += OP.dotProduct(dd1.getChildren()[i],dd2.getChildren()[i],remainingVars);
	    }
	    Global.dotProductHashtable.put(triplet,new Double(dp));
	    return dp;
	}

	// dd1 and dd2 are leaves
	else {
	    double result = dd1.getVal() * dd2.getVal();
	    for (int i=0; i<vars.length; i++) {
		result *= Global.varDomSize[vars[i]-1];
	    }
	    return result;
	}
    }

    //////////////////////////////////////////////////////
    // dotProductNoMem  (My set, don't store results)
    //////////////////////////////////////////////////////
    public static double[][] dotProductNoMem(DD[] dd1Array, DD[] dd2Array, int[] vars) {
	double[][] results = new double[dd1Array.length][dd2Array.length];
	for (int i=0; i<dd1Array.length; i++) {
	    for (int j=0; j<dd2Array.length; j++) {
		results[i][j] = dotProductNoMem(dd1Array[i], dd2Array[j], vars);
	    }
	}
	return results;
    }
         
    public static double[] dotProductNoMem(DD dd1, DD[] dd2Array, int[] vars) {
	double[] results = new double[dd2Array.length];
	for (int i=0; i<dd2Array.length; i++) {
	    results[i] = dotProductNoMem(dd1, dd2Array[i], vars);
	}
	return results;
    }
         
    public static double[] dotProductNoMem(DD[] dd1Array, DD dd2, int[] vars) {
	double[] results = new double[dd1Array.length];
	for (int i=0; i<dd1Array.length; i++) {
	    results[i] = dotProductNoMem(dd1Array[i], dd2, vars);
	}
	return results;
    }
         
    public static double dotProductNoMem(DD dd1, DD dd2, int[] vars) {

	// should cache results to speed up things a little

	if ((dd1.getVar() == 0 && dd1.getVal() == 0) || 
	    (dd2.getVar() == 0 && dd2.getVal() == 0))
	    return 0;

	// dd1 precedes dd2
	if (dd1.getVar() > dd2.getVar()) {

	    int[] remainingVars = MySet.remove(vars,dd1.getVar());
	    double dp = 0;
	    for (int i=0; i<dd1.getChildren().length; i++) {
		dp += OP.dotProductNoMem(dd1.getChildren()[i],dd2,remainingVars);
	    }
	    return dp;
	}

	// dd2 precedes dd1 {
	else if (dd2.getVar() > dd1.getVar()) {

	    int[] remainingVars = MySet.remove(vars,dd2.getVar());
	    double dp = 0;
	    for (int i=0; i<dd2.getChildren().length; i++) {
		dp += OP.dotProductNoMem(dd2.getChildren()[i],dd1,remainingVars);
	    }
	    return dp;
	}

	// dd2 and dd1 have same root var
	else if (dd1.getVar() > 0) {

	    int[] remainingVars = MySet.remove(vars,dd1.getVar());
	    double dp = 0;
	    for (int i=0; i<dd1.getChildren().length; i++) {
		dp += OP.dotProductNoMem(dd1.getChildren()[i],dd2.getChildren()[i],remainingVars);
	    }
	    return dp;
	}

	// dd1 and dd2 are leaves
	else {
	    double result = dd1.getVal() * dd2.getVal();
	    for (int i=0; i<vars.length; i++) {
		result *= Global.varDomSize[vars[i]-1];
	    }
	    return result;
	}
    }

    //////////////////////////////////////////////////////
    // factoredExpectation
    //////////////////////////////////////////////////////
    public static double factoredExpectationSparse(DD[] factDist, DD dd) {

	DD[] factDistArray = new DD[Global.varDomSize.length+1];
	for (int i=0; i<factDist.length; i++) {
	    factDistArray[factDist[i].getVar()] = factDist[i];
	}
	HashMap hashtable = new HashMap();

	return factoredExpectationSparse(factDistArray, dd, hashtable);
    }

    public static double factoredExpectationSparse(DD[] factDistArray, DD dd, HashMap hashtable) {

	// it's a leaf
	int varId = dd.getVar();
	if (varId == 0) 
	    return dd.getVal();
				
	// it's a node
	else {
	    Double storedResult = (Double)hashtable.get(dd);
	    if (storedResult != null) return storedResult.doubleValue();

	    DD[] children = dd.getChildren();
	    double result = 0;

	    if (factDistArray[varId] != null) {
		DD[] scalingConstants = factDistArray[varId].getChildren();
		for (int i=0; i<children.length; i++) {
		    if (scalingConstants[i].getVal() != 0) {
			result += scalingConstants[i].getVal() * OP.factoredExpectationSparse(factDistArray, children[i], hashtable);
		    }
		}
	    }
	    else {
		for (int i=0; i<children.length; i++) {
		    result += 1.0/children.length * OP.factoredExpectationSparse(factDistArray, children[i], hashtable);
		}
	    }
	    hashtable.put(dd, new Double(result));
	    return result;
	}
    }

    public static double[][] factoredExpectationSparse(DD[][] factDist, DD[] ddArray) {

	double[][] results = new double[factDist.length][ddArray.length];
	DD[][] factDistArray = new DD[factDist.length][Global.varDomSize.length+1];
	for (int i=0; i<factDist.length; i++) {
	    for (int j=0; j<factDist[i].length; j++) {
		factDistArray[i][factDist[i][j].getVar()] = factDist[i][j];
	    }
	}
	for (int i=0; i<factDistArray.length; i++)  {    
	    for (int j=0; j<ddArray.length; j++) {
		HashMap hashtable = new HashMap();
		results[i][j] = factoredExpectationSparse(factDistArray[i],ddArray[j],hashtable);
	    }
	}
	return results;
    }

    public static double[] factoredExpectationSparse(DD[][] factDist, DD dd) {

	double[] results = new double[factDist.length];
	DD[][] factDistArray = new DD[factDist.length][Global.varDomSize.length+1];
	for (int i=0; i<factDist.length; i++) {
	    for (int j=0; j<factDist[i].length; j++) {
		factDistArray[i][factDist[i][j].getVar()] = factDist[i][j];
	    }
	    HashMap hashtable = new HashMap();
	    results[i] = factoredExpectationSparse(factDistArray[i], dd, hashtable);
	}

	return results;
    }
    
    public static double factoredExpectationSparseNoMem(DD[] factDist, DD dd) {

	DD[] factDistArray = new DD[Global.varDomSize.length+1];
	for (int i=0; i<factDist.length; i++) {
	    factDistArray[factDist[i].getVar()] = factDist[i];
	}

	return factoredExpectationSparseNoMemRecursive(factDistArray, dd);
    }

    public static double factoredExpectationSparseNoMemRecursive(DD[] factDistArray, DD dd) {

	// it's a leaf
	int varId = dd.getVar();
	if (varId == 0) 
	    return dd.getVal();
				
	// it's a node
	else {
	    DD[] children = dd.getChildren();
	    double result = 0;

	    if (factDistArray[varId] != null) {
		DD[] scalingConstants = factDistArray[varId].getChildren();
		for (int i=0; i<children.length; i++) {
		    if (scalingConstants[i].getVal() != 0) {
			result += scalingConstants[i].getVal() * OP.factoredExpectationSparseNoMemRecursive(factDistArray, children[i]);
		    }
		}
	    }
	    else {
		for (int i=0; i<children.length; i++) {
		    result += 1.0/children.length * OP.factoredExpectationSparseNoMemRecursive(factDistArray, children[i]);
		}
	    }
	    return result;
	}
    }

    public static double[][] factoredExpectationSparseNoMem(DD[][] factDist, DD[] ddArray) {

	double[][] results = new double[factDist.length][ddArray.length];
	DD[][] factDistArray = new DD[factDist.length][Global.varDomSize.length+1];
	for (int i=0; i<factDist.length; i++) {
	    for (int j=0; j<factDist[i].length; j++) {
		factDistArray[i][factDist[i][j].getVar()] = factDist[i][j];
	    }
	}
	for (int i=0; i<factDistArray.length; i++)  {    
	    for (int j=0; j<ddArray.length; j++) {
		results[i][j] = factoredExpectationSparseNoMemRecursive(factDistArray[i],ddArray[j]);
	    }
	}
	return results;
    }

    public static double[] factoredExpectationSparseNoMem(DD[] factDist, DD[] ddArray) {

	DD[] factDistArray = new DD[Global.varDomSize.length+1];
	for (int i=0; i<factDist.length; i++) {
	    factDistArray[factDist[i].getVar()] = factDist[i];
	}

	double[] results = new double[ddArray.length];
	for (int j=0; j<ddArray.length; j++) {
	    results[j] = factoredExpectationSparseNoMemRecursive(factDistArray,ddArray[j]);
	}
	return results;
    }

    public static double[] factoredExpectationSparseNoMem(DD[][] factDist, DD dd) {

	double[] results = new double[factDist.length];
	DD[][] factDistArray = new DD[factDist.length][Global.varDomSize.length+1];
	for (int i=0; i<factDist.length; i++) {
	    for (int j=0; j<factDist[i].length; j++) {
		factDistArray[i][factDist[i][j].getVar()] = factDist[i][j];
	    }
	    results[i] = factoredExpectationSparseNoMemRecursive(factDistArray[i], dd);
	}
	return results;
    }

    public static double[][] factoredExpectationSparseParallel(DD[][] factDist, DD[] ddArray) {

	double[][] results = new double[factDist.length][ddArray.length];
	for (int i=0; i<ddArray.length; i++) {
	    double[] temp = factoredExpectationSparseParallel(factDist,ddArray[i]);
	    for (int j=0; j<temp.length; j++)  results[j][i] = temp[j];    
	}
	return results;
    }

    public static double[] factoredExpectationSparseParallel(DD[][] factDist, DD dd) {

	DD[][] factDistArray = new DD[factDist.length][Global.varDomSize.length+1];
	for (int i=0; i<factDist.length; i++) {
	    for (int j=0; j<factDist[i].length; j++) {
		factDistArray[i][factDist[i][j].getVar()] = factDist[i][j];
	    }
	}

	return factoredExpectationSparseParallelRecursive(factDistArray, dd);
    }
 
    public static double[] factoredExpectationSparseParallelRecursive(DD[][] factDistArray, DD dd) {

	double[] result = new double[factDistArray.length];
	int varId = dd.getVar();

	// it's a leaf
	if (varId == 0) {
	    double value = dd.getVal();
	    for (int i=0; i<factDistArray.length; i++) result[i] = value;
	}
				
	// it's a node
	else {
	    DD[] children = dd.getChildren();
	    int[] nnzIds = new int[factDistArray.length];
	    for (int childId=0; childId<children.length; childId++) {
                            
		// find and count non-zero distributions for each child
		int nnzDist = 0;
		for (int i=0; i<factDistArray.length; i++) {
		    if (factDistArray[i][varId] == null || factDistArray[i][varId].getChildren()[childId].getVal() != 0) {
			nnzIds[nnzDist] = i;
			nnzDist++;
		    }
		}
                            
		if (nnzDist > 0) {    

		    // build array of non-zero factored distributions for each child
		    DD[][] childFactDistArray = new DD[nnzDist][];
		    for (int id=0; id<nnzDist; id++) {
			childFactDistArray[id] = factDistArray[nnzIds[id]];
		    }
                            
		    // aggregate results for each child
		    double[] childResult = factoredExpectationSparseParallelRecursive(childFactDistArray,children[childId]);
		    for (int id=0; id<nnzDist; id++) {
			if (childFactDistArray[id][varId] == null) 
			    result[nnzIds[id]] += 1.0/children.length * childResult[id];
			else result[nnzIds[id]] += childFactDistArray[id][varId].getChildren()[childId].getVal() * childResult[id];
		    }
		}
	    }
	}
                
	return result;
    }
    

    public static double[][] factoredExpectationSparseParallel2(DD[][] factDist, DD[] ddArray) {

	double[][] results = new double[factDist.length][ddArray.length];
	for (int i=0; i<ddArray.length; i++) {
	    double[] temp = factoredExpectationSparseParallel2(factDist,ddArray[i]);
	    for (int j=0; j<temp.length; j++)  results[j][i] = temp[j];    
	}
	return results;
    }
    
    public static double[] factoredExpectationSparseParallel2(DD[][] factDist, DD dd) {

	DD[][][] factDistArray = new DD[factDist[1].length+1][factDist.length][];
	int varId = dd.getVar();
	for (int i=0; i<factDist.length; i++) {
	    factDistArray[varId][i] = new DD[factDist[1].length+1];
	    for (int j=0; j<factDist[i].length; j++) {
		factDistArray[varId][i][factDist[i][j].getVar()] = factDist[i][j];
	    }
	}
	int[][] nnzIds = new int[factDist[1].length+1][factDist.length];
	double[][] results = new double[factDist[1].length+1][factDist.length];

	return factoredExpectationSparseParallel2(factDistArray, dd, factDist.length, nnzIds, results);
    }
 
    public static double[] factoredExpectationSparseParallel2(DD[][][] factDistArray, DD dd, int nnzDists, int[][] nnzIds, double[][] results) {

	int varId = dd.getVar();

	// it's a leaf
	if (varId == 0) {
	    double value = dd.getVal();
	    for (int i=0; i<nnzDists; i++) results[0][i] = value;
	}
				
	// it's a node
	else {

	    // initialize results
	    for (int i=0; i<nnzDists; i++) results[varId][i] = 0;
                    
	    // compute results
	    DD[] children = dd.getChildren();
	    for (int childId=0; childId<children.length; childId++) {
                           
		int childVarId = children[childId].getVar();
		// find and count non-zero distributions for each child
		int nnzChildDists = 0;
		for (int i=0; i<nnzDists; i++) {
		    if (factDistArray[varId][i][varId] == null || factDistArray[varId][i][varId].getChildren()[childId].getVal() != 0) {
			nnzIds[varId][nnzChildDists] = i;
			factDistArray[childVarId][nnzChildDists] = factDistArray[varId][i];
			nnzChildDists++;
		    }
		}
                            
		if (nnzChildDists > 0) {    
                                
		    // aggregate results for each child
		    double[] childResults = factoredExpectationSparseParallel2(factDistArray,children[childId],nnzChildDists,nnzIds,results);
		    for (int id=0; id<nnzChildDists; id++) {
			if (factDistArray[childVarId][id][varId] == null) 
			    results[varId][nnzIds[varId][id]] += 1.0/children.length * childResults[id];
			else results[varId][nnzIds[varId][id]] += factDistArray[childVarId][id][varId].getChildren()[childId].getVal() * childResults[id];
		    }
		}
	    }
	}
                
	return results[varId];
    }
    
    public static double[] factoredExpectationParallel(DD[][] factDist, DD dd) {

	DD[][] factDistArray = new DD[factDist.length][Global.varDomSize.length+1];
	for (int i=0; i<factDist.length; i++) {
	    for (int j=0; j<factDist[i].length; j++) {
		factDistArray[i][factDist[i][j].getVar()] = factDist[i][j];
	    }
	}
	HashMap hashtable = new HashMap();

	return factoredExpectationParallel(factDistArray, dd, hashtable);
    }

    
    public static double[] factoredExpectationParallel(DD[][] factDistArray, DD dd, HashMap hashtable) {

	// it's a leaf
	if (dd.getVar() == 0) {
	    double value = dd.getVal();
	    double[] result = new double[factDistArray.length];
	    for (int i=0; i<factDistArray.length; i++) {
		result[i] = value;
	    }
	    return result;
	}
				
	// it's a node
	else {
	    MyDoubleArray storedResult = (MyDoubleArray)hashtable.get(dd);
	    if (storedResult != null) return storedResult.doubleArray;

	    DD[] children = dd.getChildren();
	    double[] result = new double[factDistArray.length];

	    // compute children results
	    double[][] childrenResults = new double[children.length][];
	    double[] childResult = new double[factDistArray.length];
	    for (int childId=0; childId<children.length; childId++) {
		childrenResults[childId] = childResult;
		for (int i=0; i<factDistArray.length; i++) {
		    if (factDistArray[i][dd.getVar()] == null || factDistArray[i][dd.getVar()].getChildren()[childId].getVal() != 0) {
			childrenResults[childId] = OP.factoredExpectationParallel(factDistArray, children[childId], hashtable);
			break;
		    }
		}
	    }

	    // aggregate children results
	    for (int i=0; i<factDistArray.length; i++) {
		if (factDistArray[i][dd.getVar()] != null) {
		    DD[] scalingConstants = factDistArray[i][dd.getVar()].getChildren();
		    for (int childId=0; childId<children.length; childId++) {
			result[i] += scalingConstants[childId].getVal() * childrenResults[childId][i];
		    }
		}
		else {
		    for (int childId=0; childId<children.length; childId++) {
			result[i] += 1.0/children.length * childrenResults[childId][i];
		    }
		}
	    }

	    hashtable.put(dd, new MyDoubleArray(result));
	    return result;
	}
    }

    //////////////////////////////////////////////////////
    // addMultarElim (summout variables from a product of DDs using variable elimincation)
    //////////////////////////////////////////////////////
    public static DD addMultVarElim(DD[] dds, int[] vars) {

	// check if any of the dds are zero
	for (int i=0; i<dds.length; i++) {
	    if (dds[i].getVar() == 0 && dds[i].getVal() == 0) return DD.zero;
	}

	// eliminate variables one by one
	while (vars != null && vars.length > 0) {
  
	    // eliminate deterministic variables
	    boolean deterministic = true;
	    while (deterministic && vars.length > 0) {
		deterministic = false;
		for (int ddId=0; ddId<dds.length; ddId++) {
		    int[] varIds = dds[ddId].getVarSet();
		    if (varIds.length == 1 && MySet.find(vars,varIds[0]) >= 0) {
			DD[] children = dds[ddId].getChildren();
			int valId = -1;
			for (int childId = 0; childId<children.length; childId++) {
			    double value = children[childId].getVal();
			    if (value == 1 && !deterministic) {
				deterministic = true;
				valId = childId+1;
			    }
			    else if ((value != 0 && value != 1) || (value == 1 && deterministic)) {
				deterministic = false;
				break;
			    }
			}
			if (deterministic) {
			    vars = MySet.remove(vars,varIds[0]);
			    int[][] config = new int[2][1];
			    config[0][0] = varIds[0];
			    config[1][0] = valId;
			    dds = DDcollection.removeIth(dds,ddId);
			    for (int i=0; i<dds.length; i++) {
				if (MySet.find(dds[i].getVarSet(),varIds[0]) >= 0)
				    dds[i] = OP.restrictOrdered(dds[i],config);
			    }
			    break;
			}
		    }
		}
	    }
	    if (vars.length <= 0) break;
                    
	    // greedily choose var to eliminate
	    int bestVar = OP.selectVarGreedily(dds, vars);

	    // multiply together trees that depend on var
	    DD newDd = DD.one;
	    for (int ddId=0; ddId<dds.length; ddId++) {
		if (MySet.find(dds[ddId].getVarSet(),bestVar) >= 0) {
		    newDd = OP.mult(newDd,dds[ddId]);
		    dds = DDcollection.removeIth(dds,ddId);
		    ddId--;
		}
	    }

	    // sumout bestVar from newDd
	    newDd = OP.addout(newDd, bestVar);
	    if (newDd.getVar() == 0 && newDd.getVal() == 0) return DD.zero;

	    // add new tree to dds
	    dds = DDcollection.add(dds,newDd);

	    // remove bestVar from vars
	    vars = MySet.remove(vars,bestVar);
	}				

	// multiply remaining trees and the newly added one; the resulting tree
	// is now free of any variable that appeared in vars
	return OP.multN(dds);
    }

    public static DD addMultVarElim(DD dd, int[] vars) {
	DD[] dds = new DD[1];
	dds[0] = dd;
	return addMultVarElim(dds, vars);
    }

    public static DD addMultCutSet(DD[] dds, DD dd, int[] vars) {

	// it's a leaf
	if (dd.getVar() == 0) {
	    if (dd.getVal() == 0) return DD.zero;
	    else return OP.mult(dd,OP.addMultVarElim(dds,vars));
	}

	// it's a node
	else {

	    // check if any of the dds are zero
	    for (int i=0; i<dds.length; i++) {
		if (dds[i].getVar() == 0 && dds[i].getVal() == 0) return DD.zero;
	    }

	    // root var must be sumed out
	    int varId = MySet.find(vars, dd.getVar());
	    if (varId >= 0) {
		int[] remainingVars = MySet.removeIth(vars,varId);
		DD[] children = dd.getChildren();
		int[][] config = new int[2][1];
		config[0][0] = dd.getVar();
		DD result = DD.zero;
		for (int i=0; i<children.length; i++) {
		    config[1][0] = i+1;
		    DD[] restrictedDds = OP.restrictOrderedN(dds,config);
		    result = OP.add(result,OP.addMultCutSet(restrictedDds,children[i],remainingVars));
		}
		return result;
	    }

	    // root var doesn't need to be sumed out
	    else {
		DD[] children = dd.getChildren();
		int[][] config = new int[2][1];
		config[0][0] = dd.getVar();
		DD[] newChildren = new DD[children.length];
		for (int i=0; i<children.length; i++) {
		    config[1][0] = i+1;
		    DD[] restrictedDds = OP.restrictOrderedN(dds,config);
		    newChildren[i] = OP.addMultCutSet(restrictedDds,children[i],vars);
		}
		return DDnode.myNew(dd.getVar(),newChildren);
	    }
	}
    }

    //////////////////////////////////////////////////////
    // max of 2 DDs
    //////////////////////////////////////////////////////
    public static DD max(DD dd1, DD dd2) {
	return max(dd1,dd2,null);
    }

    public static DD max(DD dd1, DD dd2, int[][] config) {

	// dd2 parent of dd1
	if (dd1.getVar() < dd2.getVar()) {
	    TripletConfig triplet = new TripletConfig(dd1,dd2,config);
	    DD storedResult = (DD)Global.maxHashtable.get(triplet);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd2.getChildren().length];
	    for (int i=0; i<dd2.getChildren().length; i++) {
		children[i] = OP.max(dd1, dd2.getChildren()[i], config);
	    }
	    DD result = DDnode.myNew(dd2.getVar(),children);
	    Global.maxHashtable.put(triplet,result);
	    return result;
	}
		
	// dd1 parent of dd2
	else if (dd2.getVar() < dd1.getVar()) {

	    TripletConfig triplet = new TripletConfig(dd1,dd2,config);
	    DD storedResult = (DD)Global.maxHashtable.get(triplet);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd1.getChildren().length];
	    for (int i=0; i<dd1.getChildren().length; i++) {
		children[i] = OP.max(dd1.getChildren()[i], dd2, config);
	    }
	    DD result = DDnode.myNew(dd1.getVar(),children);
	    Global.maxHashtable.put(triplet,result);
	    return result;
	}

	// the two variables have equal id
	else if (dd1.getVar() > 0) {

	    TripletConfig triplet = new TripletConfig(dd1,dd2,config);
	    DD storedResult = (DD)Global.maxHashtable.get(triplet);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd1.getChildren().length];
	    for (int i=0; i<dd1.getChildren().length; i++) {
		children[i] = OP.max(dd1.getChildren()[i], dd2.getChildren()[i], config);
	    }
	    DD result = DDnode.myNew(dd1.getVar(),children);
	    Global.maxHashtable.put(triplet,result);
	    return result;
	}

	// they are both leaves 
	else {
	    if (dd1.getVal() < dd2.getVal()) {
		int[][] newConfig = Config.merge(config,dd2.getConfig());
		return DDleaf.myNew(dd2.getVal(), newConfig);
	    }
	    else {
		return dd1;
	    }
	}
    }

    //////////////////////////////////////////////////////
    // max of N DDs
    //////////////////////////////////////////////////////
    public static DD maxN(DD[] ddArray) {
	DD ddMax = ddArray[0];
	for (int i=1; i<ddArray.length; i++) {
	    ddMax = OP.max(ddMax,ddArray[i]);
	}
	return ddMax;
    }

    public static DD maxN(DD dd) {
	return dd;
    }

    //////////////////////////////////////////////////////
    // min of 2 DDs
    //////////////////////////////////////////////////////
    public static DD min(DD dd1, DD dd2) {
	return min(dd1,dd2,null);
    }

    public static DD min(DD dd1, DD dd2, int[][] config) {
				
	// dd2 parent of dd1
	if (dd1.getVar() < dd2.getVar()) {

	    TripletConfig triplet = new TripletConfig(dd1,dd2,config);
	    DD storedResult = (DD)Global.minHashtable.get(triplet);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd2.getChildren().length];
	    for (int i=0; i<dd2.getChildren().length; i++) {
		children[i] = OP.min(dd1, dd2.getChildren()[i], config);
	    }
	    DD result = DDnode.myNew(dd2.getVar(),children);
	    Global.minHashtable.put(triplet,result);
	    return result;
	}
		
	// dd1 parent of dd2
	else if (dd2.getVar() < dd1.getVar()) {

	    TripletConfig triplet = new TripletConfig(dd1,dd2,config);
	    DD storedResult = (DD)Global.minHashtable.get(triplet);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd1.getChildren().length];
	    for (int i=0; i<dd1.getChildren().length; i++) {
		children[i] = OP.min(dd2, dd1.getChildren()[i], config);
	    }
	    DD result = DDnode.myNew(dd1.getVar(),children);
	    Global.minHashtable.put(triplet,result);
	    return result;
	}

	// the two variables have equal id
	else if (dd1.getVar() > 0) {

	    TripletConfig triplet = new TripletConfig(dd1,dd2,config);
	    DD storedResult = (DD)Global.minHashtable.get(triplet);
	    if (storedResult != null) return storedResult;

	    DD children[];
	    children = new DD[dd1.getChildren().length];
	    for (int i=0; i<dd1.getChildren().length; i++) {
		children[i] = OP.min(dd1.getChildren()[i], dd2.getChildren()[i], config);
	    }
	    DD result = DDnode.myNew(dd1.getVar(),children);
	    Global.minHashtable.put(triplet,result);
	    return result;
	}

	// they are both leaves 
	else {
	    if (dd1.getVal() > dd2.getVal()) {
		int[][] newConfig = Config.merge(config,dd2.getConfig());
		return DDleaf.myNew(dd2.getVal(), newConfig);
	    }
	    else {
		return dd1;
	    }
	}
    }

    //////////////////////////////////////////////////////
    // maxNormDiff
    //////////////////////////////////////////////////////
    public static boolean maxNormDiff(DD dd1, DD dd2, double threshold) {
	HashMap hashtable = new HashMap();
	return OP.maxNormDiff(dd1, dd2, threshold, hashtable);
    }

    public static boolean maxNormDiff(DD dd1, DD dd2, double threshold, HashMap hashtable) {

	// dd1 precedes dd2
	if (dd1.getVar() > dd2.getVar()) {

	    Pair pair = new Pair(dd1,dd2);
	    Boolean storedResult = (Boolean)hashtable.get(pair);
	    if (storedResult != null) return storedResult.booleanValue();

	    boolean result = true;
	    DD[] children = dd1.getChildren();
	    for (int i=0; i<children.length; i++) {
		if (!OP.maxNormDiff(children[i],dd2,threshold,hashtable)) {
		    result = false;
		    break;
		}
	    }
			 
	    hashtable.put(pair,new Boolean(result));
	    return result;
	}

	// dd2 precedes dd1 {
	else if (dd2.getVar() > dd1.getVar()) {

	    Pair pair = new Pair(dd1,dd2);
	    Boolean storedResult = (Boolean)hashtable.get(pair);
	    if (storedResult != null) return storedResult.booleanValue();

	    boolean result = true;
	    DD[] children = dd2.getChildren();
	    for (int i=0; i<children.length; i++) {
		if (!OP.maxNormDiff(children[i],dd1,threshold,hashtable)) {
		    result = false;
		    break;
		}
	    }

	    hashtable.put(pair,new Boolean(result));
	    return result;
	}

	// dd2 and dd1 have same root var
	else if (dd1.getVar() > 0) {

	    Pair pair = new Pair(dd1,dd2);
	    Boolean storedResult = (Boolean)hashtable.get(pair);
	    if (storedResult != null) return storedResult.booleanValue();

	    boolean result = true;
	    DD[] children1 = dd1.getChildren();
	    DD[] children2 = dd2.getChildren();
	    for (int i=0; i<children1.length; i++) {
		if (!OP.maxNormDiff(children1[i],children2[i],threshold,hashtable)) {
		    result = false;
		    break;
		}
	    }

	    hashtable.put(pair,new Boolean(result));
	    return result;
	}

	// dd1 and dd2 are leaves
	else {
	    double diff = dd1.getVal() - dd2.getVal();
	    if (-threshold <= diff && diff <= threshold) return true;
	    else return false;
	}
    }

    //////////////////////////////////////////////////////
    // maxAll (find leaf with maximum value)
    //////////////////////////////////////////////////////
        
    public static double[] maxAllN(DD[] dds) {

	double[] results = new double[dds.length];
	for (int i=0; i<dds.length; i++)
	    results[i] = OP.maxAll(dds[i]);
	return results;
    }

    public static double maxAllN(DD dd) {
	HashMap hashtable = new HashMap();
	return maxAll(dd,hashtable);
    }

    public static double maxAll(DD dd) {
	HashMap hashtable = new HashMap();
	return maxAll(dd,hashtable);
    }

    public static double maxAll(DD dd, HashMap hashtable) {

	Double storedResult = (Double)hashtable.get(dd);
	if (storedResult != null) return storedResult.doubleValue();

	// it's a leaf
	double result = Double.NEGATIVE_INFINITY;
	if (dd.getVar() == 0) result = dd.getVal();
	else {
	    DD[] children = dd.getChildren();
	    for (int i=0; i<children.length; i++) {
		double maxVal = OP.maxAll(children[i],hashtable);
		if (result < maxVal) result = maxVal;
	    }
	}
	hashtable.put(dd,new Double(result));
	return result;
    }

    //////////////////////////////////////////////////////
    // minAll (find leaf with minimum value)
    //////////////////////////////////////////////////////

    public static double[] minAllN(DD[] dds) {

	double[] results = new double[dds.length];
	for (int i=0; i<dds.length; i++)
	    results[i] = OP.minAll(dds[i]);
	return results;
    }

    public static double minAllN(DD dd) {
	HashMap hashtable = new HashMap();
	return minAll(dd,hashtable);
    }

    public static double minAll(DD dd) {
	HashMap hashtable = new HashMap();
	return minAll(dd,hashtable);
    }

    public static double minAll(DD dd, HashMap hashtable) {

	Double storedResult = (Double)hashtable.get(dd);
	if (storedResult != null) return storedResult.doubleValue();

	// it's a leaf
	double result = Double.POSITIVE_INFINITY;
	if (dd.getVar() == 0) result = dd.getVal();
	else {
	    DD[] children = dd.getChildren();
	    for (int i=0; i<children.length; i++) {
		double minVal = OP.minAll(children[i],hashtable);
		if (result > minVal) result = minVal;
	    }
	}
	hashtable.put(dd,new Double(result));
	return result;
    }

    //////////////////////////////////////////////////////
    // restrict some variables to some values
    //////////////////////////////////////////////////////
    public static DD restrict(DD dd, int[][] config) {
				
	// it's a leaf
	if (dd.getVar() == 0) {
	    return dd;
	}

	// root is variable that must be restricted
	int index = MySet.find(config[0],dd.getVar());
	if (index >= 0) {
	    int[][] restConfig = Config.removeIth(config,index);
                    
	    // terminate early
	    if (config[0].length == 0)
		return dd.getChildren()[config[1][index]-1];
                    
	    // recurse
	    else
		return OP.restrict(dd.getChildren()[config[1][index]-1],restConfig);
	}
				
	// have to restrict all children recursively
	DD children[];
	children = new DD[dd.getChildren().length];
	for (int i=0; i < dd.getChildren().length; i++) {
	    children[i] = OP.restrict(dd.getChildren()[i],config);
	}
	return DDnode.myNew(dd.getVar(),children);
    }

    //////////////////////////////////////////////////////
    // restrictOrdered (faster restrict fn that assumes a variable ordering)
    //////////////////////////////////////////////////////
    public static DD restrictOrdered(DD dd, int[][] config) {

	// optimized to terminate early by exploiting variable ordering
				
	// find var index
	int variable = dd.getVar();
	boolean smallerVar = false;
	int index = -1;
	for (int i=0; i<config[0].length; i++) {
	    if (config[0][i] < variable) smallerVar = true;
	    if (config[0][i] == variable) {
		index = i;
		break;
	    }
	}

	// nothing to restrict
	if (index == -1 && !smallerVar) return dd;

	// root is variable that must be restricted
	if (index >= 0) {
	    return OP.restrict(dd.getChildren()[config[1][index]-1],config);
	}
				
	// have to restrict all children recursively
	DD[] children = new DD[dd.getChildren().length];
	for (int i=0; i < children.length; i++) {
	    children[i] = OP.restrict(dd.getChildren()[i],config);
	}
	return DDnode.myNew(variable,children);
    }

    //////////////////////////////////////////////////////
    // evaluate a DD for some configuration of variables
    //////////////////////////////////////////////////////
    public static double eval(DD dd, int[][] config) {
	return OP.restrictOrdered(dd,config).getVal();
    }

    //////////////////////////////////////////////////////
    // restrict N DDs
    //////////////////////////////////////////////////////
    public static DD[] restrictN(DD[] dds, int[][] config) {
	DD[] restrictedDds = new DD[dds.length];
	for (int i=0; i<dds.length; i++) {
	    restrictedDds[i] = OP.restrict(dds[i],config);
	}
	return restrictedDds;
    }

    public static DD[] restrictN(DD dd, int[][] config) {
	DD[] dds = new DD[1];
	dds[0] = dd;
	return restrictN(dds, config);
    }

    //////////////////////////////////////////////////////
    // restrict N ordered DDs
    //////////////////////////////////////////////////////
    public static DD[] restrictOrderedN(DD[] dds, int[][] config) {
	DD[] restrictedDds = new DD[dds.length];
	for (int i=0; i<dds.length; i++) {
	    restrictedDds[i] = OP.restrictOrdered(dds[i],config);
	}
	return restrictedDds;
    }

    public static DD[] restrictOrderedN(DD dd, int[][] config) {
	DD[] dds = new DD[1];
	dds[0] = dd;
	return restrictOrderedN(dds, config);
    }

    //////////////////////////////////////////////////////
    // evaluate N DDs
    //////////////////////////////////////////////////////
    public static double[] evalN(DD[] dds, int[][] config) {

	DD[] restrictedDds = OP.restrictN(dds, config);

	double[] values = new double[dds.length];
	for (int i=0; i<dds.length; i++) {
	    values[i] = restrictedDds[i].getVal();
	}
	return values;
    }

    public static double[] evalN(DD dd, int[][] config) {
	double[] values = new double[1];
	values[0] = OP.restrict(dd,config).getVal();
	return values;
    }

    //////////////////////////////////////////////////////
    // maxout a variable from a DD
    //////////////////////////////////////////////////////
    public static DD maxout(DD dd, int var) {

	// it's a leaf or 'var' is not part of the tree
	if (dd.getVar() < var) {
	    return dd;
	}

	// root is variable that must be eliminated
	if (dd.getVar() == var) {
	    // have to collapse all children into a new node
	    DD newDD = DDleaf.myNew(Double.NEGATIVE_INFINITY);
	    for (int i=0; i < dd.getChildren().length; i++) {
		int[][] config = new int[2][1];
		config[0][0] = dd.getVar();
		config[1][0] = i+1;
		newDD = OP.max(newDD, dd.getChildren()[i], config);
	    }
	    return newDD;
	}

	// descend down the tree until 'var' is found
	else {
	    DD children[];
	    children = new DD[dd.getChildren().length];
	    for (int i=0; i<dd.getChildren().length; i++) {
		children[i] = OP.maxout(dd.getChildren()[i], var);
	    }
	    return DDnode.myNew(dd.getVar(),children);
	}
    }

    //////////////////////////////////////////////////////
    // minout a variable from a DD
    //////////////////////////////////////////////////////
    public static DD minout(DD dd, int var) {

	// it's a leaf or 'var' is not part of the tree
	if (dd.getVar() < var) {
	    return dd;
	}

	// root is variable that must be eliminated
	if (dd.getVar() == var) {
	    // have to collapse all children into a new node
	    DD newDD = DDleaf.myNew(Double.POSITIVE_INFINITY);
	    for (int i=0; i < dd.getChildren().length; i++) {
		int[][] config = new int[2][1];
		config[0][0] = dd.getVar();
		config[1][0] = i+1;
		newDD = OP.min(newDD, dd.getChildren()[i], config);
	    }
	    return newDD;
	}

	// descend down the tree until 'var' is found
	else {
	    DD children[];
	    children = new DD[dd.getChildren().length];
	    for (int i=0; i<dd.getChildren().length; i++) {
		children[i] = OP.minout(dd.getChildren()[i], var);
	    }
	    return DDnode.myNew(dd.getVar(),children);
	}
    }

    //////////////////////////////////////////////////////
    // maxAddVarElim (maxout some variables from a sum of DDs using variable elimination)
    //////////////////////////////////////////////////////
    public static DD maxAddVarElim(DD[] dds, int[] vars) {

	// eliminate variables one by one
	while (vars != null && vars.length > 0) {
  
	    // greedily choose var to eliminate
	    int bestVar = OP.selectVarGreedily(dds, vars);
  
	    // add together trees that depend on var
	    DD newDd = DD.zero;
	    for (int ddId=0; ddId<dds.length; ddId++) {
		if (MySet.find(dds[ddId].getVarSet(),bestVar) >= 0) {
		    newDd = OP.add(newDd,dds[ddId]);
		    dds = DDcollection.removeIth(dds,ddId);
		    ddId--;
		}
	    }

	    // sumout bestVar from newDd
	    newDd = OP.maxout(newDd, bestVar);

	    // add new tree to dds
	    dds = DDcollection.add(dds,newDd);

	    // remove bestVar from vars
	    vars = MySet.remove(vars,bestVar);
	}				

	// add remaining trees and the newly added one; the resulting tree
	// is now free of any variable that appeared in vars
	return OP.addN(dds);
    }

    public static DD maxAddVarElim(DD dd, int[] vars) {
	DD[] dds = new DD[1];
	dds[0] = dd;
	return maxAddVarElim(dds, vars);
    }

    //////////////////////////////////////////////////////
    // minAddVarElim (minout som variables from a sum of DDs using variable elimination
    //////////////////////////////////////////////////////
    public static DD minAddVarElim(DD[] dds, int[] vars) {

	// eliminate variables one by one
	while (vars != null && vars.length > 0) {
  
	    // greedily choose var to eliminate
	    int bestVar = OP.selectVarGreedily(dds, vars);
  
	    // add together trees that depend on var
	    DD newDd = DD.zero;
	    for (int ddId=0; ddId<dds.length; ddId++) {
		if (MySet.find(dds[ddId].getVarSet(),bestVar) >= 0) {
		    newDd = OP.add(newDd,dds[ddId]);
		    dds = DDcollection.removeIth(dds,ddId);
		    ddId--;
		}
	    }

	    // sumout bestVar from newDd
	    newDd = OP.minout(newDd, bestVar);

	    // add new tree to dds
	    dds = DDcollection.add(dds,newDd);

	    // remove bestVar from vars
	    vars = MySet.remove(vars,bestVar);
	}				

	// add remaining trees and the newly added one; the resulting tree
	// is now free of any variable that appeared in vars
	return OP.addN(dds);
    }

    public static DD minAddVarElim(DD dd, int[] vars) {
	DD[] dds = new DD[1];
	dds[0] = dd;
	return minAddVarElim(dds, vars);
    }

    //////////////////////////////////////////////////////
    // orderLast
    //////////////////////////////////////////////////////
    public static DD orderLast(DD dd, int varId) {

	// it's a leaf
	if (dd.getVar() == 0)
	    return dd;
				
	// it's a node that *may* need to be reordered
	DD[] children = dd.getChildren();
	if (dd.getVar() == varId) {
	    int rootVarId = 0;
	    for (int i=0; i<children.length; i++) {
		if (children[i].getVar() > 0) { // it's a node
		    rootVarId = children[i].getVar();
		    break;
		}
	    }

	    // no reordering necessary
	    if (rootVarId == 0)
		return dd;

	    // reorder
	    else {
		int[][] config = new int[2][1];
		config[0][0] = rootVarId;
		DD[] newChildren = new DD[Global.varDomSize[rootVarId-1]];
		for (int i=0; i<newChildren.length; i++) {
		    config[1][0] = i+1;
		    DD restDd = OP.restrict(dd,config);
		    newChildren[i] = OP.orderLast(restDd,varId);
		}
		return DDnode.myNew(rootVarId,newChildren);
	    }	
	}

	// it's a node different from varId, so no need to reorder
	else {
	    DD[] newChildren = new DD[children.length];
	    for (int i=0; i<children.length; i++) {
		newChildren[i] = OP.orderLast(children[i],varId);
	    }
	    return DDnode.myNew(dd.getVar(),newChildren);
	}
    }

    //////////////////////////////////////////////////////
    // reorder a DD according to the variable ordering
    //////////////////////////////////////////////////////
    public static DD reorder(DD dd) {

	// it's a leaf
	if (dd.getVar() == 0)
	    return dd;
				
	// it's a node
	int[] varSet = dd.getVarSet();
	int highestVar = varSet[varSet.length-1];
	int[][] config = new int[2][1];
	config[0][0] = highestVar;
	DD[] children = new DD[Global.varDomSize[highestVar-1]];
	for (int i=0; i<Global.varDomSize[highestVar-1]; i++) {
	    config[1][0] = i+1;
	    DD restDd = OP.restrict(dd,config);
	    children[i] = OP.reorder(restDd);
	}
	return DDnode.myNew(highestVar,children);
    }

    //////////////////////////////////////////////////////
    // extractConfig
    //////////////////////////////////////////////////////
    public static DD extractConfig(DD dd, int[] vars) {

	// dd is a leaf
	if (dd.getVar() == 0) {
	    int[][] config = Config.extend(dd.getConfig(),vars);
	    return Config.convert2dd(config);
	}

	// dd is a node
	else {
	    DD[] children = new DD[dd.getChildren().length];
	    for (int i=0; i<children.length; i++) {
		children[i] = OP.extractConfig(dd.getChildren()[i], vars);
	    }
	    return DDnode.myNew(dd.getVar(),children);
	}

    }


    //////////////////////////////////////////////////////
    // clearConfig
    //////////////////////////////////////////////////////
    public static DD clearConfig(DD dd) {

	// dd is a leaf
	if (dd.getVar() == 0) {
	    if (dd.getConfig() == null) return dd;
	    else return DDleaf.myNew(dd.getVal());
	}

	// dd is a node
	else {
	    DD[] children = new DD[dd.getChildren().length];
	    for (int i=0; i<children.length; i++) {
		children[i] = OP.clearConfig(dd.getChildren()[i]);
	    }
	    return DDnode.myNew(dd.getVar(),children);
	}
    }

    //////////////////////////////////////////////////////
    // printPolicySpuddFormat
    //////////////////////////////////////////////////////
 
    public static void printPolicySpuddFormat(String filename, DD[] valuef, int[] pol) {
	FileOutputStream fos = null;
	PrintStream ps = null;
	try {
	    fos = new FileOutputStream(filename,true);
	    ps = new PrintStream(fos);
	} catch (FileNotFoundException e) {
	    System.out.println("Error: file not found\n");
	    System.exit(1);
	}
	
	for (int avec=0; avec < valuef.length; avec++) {
	    ps.print("dd " + pol[avec]  + "\n");
	    valuef[avec].printSpuddDD(ps);
	    ps.print("\n enddd\n");
	}
	
	try {
	    fos
