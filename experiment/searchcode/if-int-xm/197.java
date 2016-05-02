//TODO: needs to be simplified;
// summary printing broken

//
//Copyright (C) 2007 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
package gov.nasa.jpf.continuity;


import gov.nasa.jpf.jvm.ChoiceGenerator;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;

import gov.nasa.jpf.simplify.CombineInequalitiesVisitor;
import gov.nasa.jpf.simplify.DefaultSimplifier;
import gov.nasa.jpf.simplify.DerivativeExpression;
import gov.nasa.jpf.simplify.DerivativesRewriter;
import gov.nasa.jpf.simplify.RewriterVisitor;
import gov.nasa.jpf.simplify.Simplifier;

import static gov.nasa.jpf.continuity.PathConditionBoundary.boundary;

import gov.nasa.jpf.symbc.Debug;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.RealConstant;
import gov.nasa.jpf.symbc.numeric.RealConstraint;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;
import gov.nasa.jpf.symbc.numeric.SymbolicConstraintsGeneral;

import gov.nasa.jpf.symbc.numeric.solvers.ProblemCoral;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContinuityListener extends PropertyListenerAdapter {
  /*
   * Save the method summaries to a file for use by others
   */
  public void searchFinished(Search search) {
    // printPcPfMap();
    // printPcBoundaries();
    // printPathFcns();
    printDiscontConds();
  }


  private void printPathFcns() {
    Simplifier simplifier = makePfDerivSimplifier();
    System.out.println(">>> Path Functions and Derivatives: ");
    int i = 1;
    for (Expression e :  PathFunctionUtils.getPathFunctionMap().values()) {
      RealExpression pathFcn = (RealExpression)e;
      String pathFcnName = "PF" + i;
      i++;
      System.out.println("   " + pathFcnName + " = "
                         + simplifier.simplify(pathFcn).toString());
      for (SymbolicReal sr : PathFunctionUtils.allSymbolicReals()) {
        RealExpression deriv = new DerivativeExpression(sr, pathFcn);
        System.out.println("       (deriv " + sr.getName()
                           + " " + pathFcnName + ") = "
                           + simplifier.simplify(deriv).toString());
      }
      System.out.println("\n-----------------------------");
    }
  }

  public Simplifier makePfDerivSimplifier() {
    RewriterVisitor derivEngine = new DerivativesRewriter();
    List<SymbolicReal> symVars = PathFunctionUtils.allSymbolicReals();

    for (int i=0; i<symVars.size(); i++) {
      for (int j=i+1; j<symVars.size(); j++) {
        derivEngine.addRules(new DerivativeExpression(symVars.get(i), symVars.get(j)),
                             new RealConstant(0.0),
                             new DerivativeExpression(symVars.get(j), symVars.get(i)),
                             new RealConstant(0.0));
      }
    }
    return new DefaultSimplifier(derivEngine);
  }


  private void printPcPfMap() {
    System.out.println(">>> PC PF map: ");
    for (Map.Entry<PathCondition,Expression> e :  PathFunctionUtils.getPathFunctionMap().entrySet()) {
      PathCondition pc = e.getKey();
      if (pc != null) {
        // pc.solve();
        System.out.println("   PATH CONDITION: (" + pc.toString() + ")");
      } else {
        System.out.println("   PATH CONDITION: null");
      }
      System.out.println("   PATH FUNCTION: (" + e.getValue().toString() + ")");
      System.out.println("--------------------------");
    }
  }

  private Simplifier _simplifier
    = (new DefaultSimplifier()).addVisitors(new CombineInequalitiesVisitor());

  private void printPcBoundaries() {
    System.out.println(">>> PC Boundaries: ");
    Map<PathCondition,Expression> entries =  PathFunctionUtils.getPathFunctionMap();
    int numEntries = entries.size();
    PathCondition[] pcs = entries.keySet().toArray(new PathCondition[numEntries]);
    Expression[] pfs = entries.values().toArray(new Expression[numEntries]);

    for (int i=0; i<numEntries; i++) {
      PathCondition pc1 = pcs[i];
      for (int j=i+1; j<numEntries; j++) {
        PathCondition pc2 = pcs[j];

        System.out.println("\n  PC1: " + pc1);
        System.out.println("\n  PC2: " + pc2);
        System.out.println("\n  boundary(PC1,PC2): "
                           + boundary((RealConstraint)pc1.header,
                                      (RealConstraint)pc2.header));
        System.out.println("--------------------------");
      }
    }
  }


  private DiscontinuityConditions _dc = new DiscontinuityConditions();

  private void printDiscontConds() {
    System.out.println(">>> Discontinuity Conditions: ");
    Map<PathCondition,Expression> entries =  PathFunctionUtils.getPathFunctionMap();
    int numEntries = entries.size();
    PathCondition[] pcs = entries.keySet().toArray(new PathCondition[numEntries]);
    Expression[] pfs = entries.values().toArray(new Expression[numEntries]);

    for (int i=0; i<numEntries; i++) {
      PathCondition pc1 = pcs[i];
      RealExpression pf1 = (RealExpression)pfs[i];
      for (int j=i+1; j<numEntries; j++) {
        PathCondition pc2 = pcs[j];
        RealExpression pf2 = (RealExpression)pfs[j];
        RealConstraint dc = _dc.discontinuityCondition((RealConstraint)pc1.header,
                                                       (RealConstraint)pc2.header,
                                                       pf1,
                                                       pf2);
        RealConstraint cc = _dc.continuityCondition((RealConstraint)pc1.header,
                                                    (RealConstraint)pc2.header,
                                                    pf1,
                                                    pf2);
        System.out.println("  PC1: " + pc1.header.stringPC());
        System.out.println("  PF1: " + pf1);
        System.out.println("--------------------------\n");
        System.out.println("  PC2: " + pc2.header.stringPC());
        System.out.println("  PF2: " + pf2);
        System.out.println("--------------------------\n\n");
        System.out.println("  discontinuityCondition(PC1,PC2,PF1,PF2): "
                           + dc.stringPC());
        System.out.println("--------------------------\n");

        PathCondition dcPc = new PathCondition(dc);
        boolean solved = dcPc.solve();
        System.out.println("--------------------------\n");
        System.out.println("DC solved: " + solved);
        Map<String,Object> varVals = new HashMap<String,Object>();
        dc.getVarVals(varVals);
        System.out.println("DC Vars=Vals: " + varVals + "\n");
        PathCondition.flagSolved = false;
        System.out.println("DC: " + dc.stringPC());


        SymbolicConstraintsGeneral solver = new SymbolicConstraintsGeneral();
        boolean result1 = solver.solve(dcPc);
        // System.out.println("\nCoral Rep of DC: " + ((ProblemCoral)solver.getPB()).getPc());
        // System.out.println("--------------------------\n\n");
        // gov.nasa.jpf.simplify.ParseRealConstraint _parser = new gov.nasa.jpf.simplify.ParseRealConstraint();
        // RealConstraint cc = _parser.prefixParseConstraint("(== ym (+ 1.0 xm))");
        // RealConstraint cc = _parser.prefixParseConstraint("(== (- ym xm) (+ 3.0 ym))");

        // cc.and = cc1;

        // System.out.println("  continuityCondition(PC1,PC2,PF1,PF2): "
        //                    + cc.stringPC());
        // System.out.println("--------------------------\n");
        PathCondition ccPc = new PathCondition(cc);
        solved = ccPc.solve();
        System.out.println("--------------------------\n");
        System.out.println("CC solved: " + solved);
        varVals = new HashMap<String,Object>();
        cc.getVarVals(varVals);
        System.out.println("CC Vars=Vals: " + varVals + "\n");
        PathCondition.flagSolved = false;
        System.out.println("CC: " + cc.stringPC());
        System.out.println("======================================");
        System.out.println("======================================");
      }
    }
  }


  // private static void printPCPrefix(JVM vm, String msg) {
  //   PathCondition pc = getPC(vm);
  //   if (pc != null) {
  //     // pc.solve();
  //     System.out.println(msg + pc.stringPCPrefix());
  //   }
  //   else
  //     System.out.println(msg + " PC is null");
  // }

  private static PathCondition getPC(JVM vm) {
    ChoiceGenerator<?> cg = vm.getChoiceGenerator();
    PathCondition pc = null;

    if (!(cg instanceof PCChoiceGenerator)) {
      ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
      while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
        prev_cg = prev_cg.getPreviousChoiceGenerator();
      }
      cg = prev_cg;
    }

    if ((cg instanceof PCChoiceGenerator) && cg != null) {
      pc = ((PCChoiceGenerator) cg).getCurrentPC();
    }
    return pc;
  }
}

