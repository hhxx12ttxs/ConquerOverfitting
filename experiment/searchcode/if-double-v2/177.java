//
// Copyright (C) 2006 United States Government as represented by the
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
//
package gov.nasa.jpf.continuity.bytecode;


import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.ChoiceGenerator;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.Types;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.RealExpression;


public class DCMP_Utils {

  // NAMING DIFFERENCES, JPF CODE VS SUN'S JVM SPEC:
  // 
  // The roles of "v1" & "v2" in the code below are switched when compared to
  // roles of "value1" and "value2" in Sun's/Oracle's description of the
  // DCMPx bytecode in the JVM Spec.
  //
  // That is, the value at the top of the stack in the code below is called
  // "v1".  But in the JVM spec, the value at the top of the stack is called
  // "value2".
  //
  // Similarly for "v2" (here) and "value1" (JVM Spec).  Also, the attributes
  // "sym_v1" and "sym_v2" are switched about when compared to the Spec.
  //
  //
  // SPECIAL CASES:
  //
  // If the next instruction is "ifle" or similar, then this
  // "dcmpx" instruction is setting up for what is really only
  // a two-way branch:
  //  - For "ifle":
  //    - Fall through the instruction if sym_v1 > sym_v2
  //      (so DCMPx must push 1 onto the stack)
  //    - Do the instruction's branch if sym_v1 <= sym_v2
  //      (so DCMPx must push 0 onto the stack)
  //  - For "iflt":
  //    - Fall through the instruction if sym_v1 >= sym_v2
  //      (so DCMPx must push 0 onto the stack)
  //    - Do the instruction's branch if sym_v1 < sym_v2
  //      (so DCMPx must push -1 onto the stack)
  //  - For "ifge":
  //    - Fall through the instruction if sym_v1 < sym_v2
  //      (so DCMPx must push -1 onto the stack)
  //    - Do the instruction's branch if sym_v1 >= sym_v2
  //      (so DCMPx must push 0 onto the stack)
  //  - For "ifgt":
  //    - Fall through the instruction if sym_v2 <= sym_v1
  //      (so DCMPx must push 0 onto the stack)
  //    - Do the instruction's branch if sym_v2 > sym_v1
  //      (so DCMPx must push 1 onto the stack)
  //  - For "ifne":
  //    - Fall through the instruction if sym_v1 == sym_v2
  //      (so DCMPx must push 0 onto the stack)
  //    - Do the instruction's branch if sym_v1 != sym_v2
  //      (so DCMPx must push 1 onto the stack)
  //
  public static Instruction compareSymbolicValues(Instruction thisDcmpInstr,
                                                  SystemState ss,
                                                  ThreadInfo th,
                                                  StackFrame sf,
                                                  RealExpression sym_v1,
                                                  RealExpression sym_v2,
                                                  String nextInstr) {
    ChoiceGenerator<Integer> cg;
    int conditionValue;

    if (!th.isFirstStepInsn()) { // first time around
      if ("ifle".equals(nextInstr)
          || "iflt".equals(nextInstr)
          || "ifge".equals(nextInstr)
          || "ifgt".equals(nextInstr)
          || "ifne".equals(nextInstr)) {
        cg = new PCChoiceGenerator(2);
      } else {
        cg = new PCChoiceGenerator(3);
      }
      ss.setNextChoiceGenerator(cg);
      return thisDcmpInstr;
    } else { // this is what really returns results
      ChoiceGenerator<?> curCg = ss.getChoiceGenerator();
      assert (curCg instanceof PCChoiceGenerator)
        : "expected PCChoiceGenerator, got: " + curCg;
      cg = (PCChoiceGenerator)curCg;

      if (cg.getTotalNumberOfChoices() == 2) {
        if ("ifle".equals(nextInstr)) {
          // Fall through execution: conditionValue == 1
          // Take branch execution: conditionValue == 0
          conditionValue = cg.getNextChoice().intValue();
          handleNextBranch(ss, th, cg,
                           sym_v1, sym_v2,
                           conditionValue, 0,
                           Comparator.GT, Comparator.LE);

        } else if ("iflt".equals(nextInstr)) {
          // Fall through execution: conditionValue == 0
          // Take branch execution: conditionValue == -1
          conditionValue = - cg.getNextChoice().intValue();
          handleNextBranch(ss, th, cg,
                           sym_v1, sym_v2,
                           conditionValue, -1,
                           Comparator.GE, Comparator.LT);

        } else if ("ifge".equals(nextInstr)) {
          // Fall through execution: conditionValue == -1
          // Take branch execution: conditionValue == 0
          conditionValue = - cg.getNextChoice().intValue();
          handleNextBranch(ss, th, cg,
                           sym_v1, sym_v2,
                           conditionValue, 0,
                           Comparator.LT, Comparator.GE);

        } else if ("ifgt".equals(nextInstr)) {
          // Fall through execution: conditionValue == 0
          // Take branch execution: conditionValue == 1
          conditionValue = cg.getNextChoice().intValue();
          handleNextBranch(ss, th, cg,
                           sym_v1, sym_v2,
                           conditionValue, 1,
                           Comparator.LE, Comparator.GT);

        } else if ("ifne".equals(nextInstr)) {
          // Fall through execution: conditionValue == 0
          // Take branch execution: conditionValue == 1
          conditionValue = cg.getNextChoice().intValue();
          handleNextBranch(ss, th, cg,
                           sym_v1, sym_v2,
                           conditionValue, 1,
                           Comparator.EQ, Comparator.NE);

        } else {
          throw new JPFException(thisDcmpInstr + " cannot handle choice generators for"
                                 + " the instruction sequence: " + thisDcmpInstr + "+"
                                 + (nextInstr == null
                                    ? "null"
                                    : nextInstr.toUpperCase()));
        }

        // This DCMPx instruction is followed by something other than
        // an IFLE, IFLT, IFGE, IFGT, or IFNE instruction, so we have
        // to do the full set of three possible results: -1, 0, 1
      } else {
        conditionValue = cg.getNextChoice().intValue() - 1;

        if (conditionValue == -1) {
          handleDcmpCase(ss, th, cg,
                         sym_v1, sym_v2,
                         Comparator.LT);
        } else if (conditionValue == 0) {
          handleDcmpCase(ss, th, cg,
                         sym_v1, sym_v2,
                         Comparator.EQ);
        } else {
          handleDcmpCase(ss, th, cg,
                         sym_v1, sym_v2,
                         Comparator.GT);
        }
      }
      th.push(conditionValue, false);
      return thisDcmpInstr.getNext(th);
    }
  }


  private static PathCondition getPrevPathCond(ChoiceGenerator<Integer> cg) {
    // pc is updated with the pc stored in the choice generator above
    // get the path condition from the
    // previous choice generator of the same type

    PathCondition pc;
    ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();

    while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
      prev_cg = prev_cg.getPreviousChoiceGenerator();
    }

    if (prev_cg == null)
      pc = new PathCondition();
    else
      pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

    assert pc != null;
    return pc;
  }


  private static void handleNextBranch(SystemState ss, ThreadInfo th,
                                       ChoiceGenerator<Integer> cg,
                                       RealExpression sym_v1, RealExpression sym_v2,
                                       int conditionValue, int branchCondVal,
                                       Comparator fallThruComp, Comparator branchComp) {
    if (conditionValue == branchCondVal) {
      // This is the take-the-branch clause
      handleDcmpCase(ss, th, cg, sym_v1, sym_v2, branchComp);
    } else {
      // This is the fall-through clause: sym_v1 > sym_v2
      handleDcmpCase(ss, th, cg, sym_v1, sym_v2, fallThruComp);

    }
  }



  private static void handleDcmpCase(SystemState ss, ThreadInfo th,
                                     ChoiceGenerator<Integer> cg,
                                     RealExpression sym_v1, RealExpression sym_v2,
                                     Comparator comparator) {
    double v1 = Types.longToDouble(th.longPop());
    double v2 = Types.longToDouble(th.longPop());
    PathCondition pc = getPrevPathCond(cg);

    if (sym_v1 != null) {
      if (sym_v2 != null) { // both are symbolic values
        pc._addDet(comparator, sym_v2, sym_v1);
      } else
        pc._addDet(comparator, v2, sym_v1);
    } else
      pc._addDet(comparator, sym_v2, v1);
    if (!pc.simplify()) {// not satisfiable
      ss.setIgnored(true);
    } else {
      // pc.solve();
      ((PCChoiceGenerator) cg).setCurrentPC(pc);
    }

  }
}




/*
          String[] names = sf.getLocalVariableNames();
          System.out.println("                  names: " + Arrays.asList(names));
          System.out.print("                  attrs: [");
          for(int i=0;i<sf.getLocalVariableCount();i++){System.out.print(sf.getLocalAttr(i) + ", ");}
          System.out.println("]");







          if (conditionValue == 1) {
            System.out.println("++++++ Take Branch / 1   Clause: sym_v1 != sym_v2");
            System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
            System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
            // This is the branching clause: sym_v1 != sym_v2
            if (sym_v1 != null) {
              if (sym_v2 != null) { // both are symbolic values
                pc._addDet(Comparator.NE, sym_v1, sym_v2);
              } else
                pc._addDet(Comparator.NE, sym_v1, v2);
            } else
              pc._addDet(Comparator.NE, v1, sym_v2);
            if (!pc.simplify()) {// not satisfiable
              ss.setIgnored(true);
            } else {
              // pc.solve();
              ((PCChoiceGenerator) cg).setCurrentPC(pc);
              // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
            }
          } else { // conditionValue == 0
            System.out.println("++++++ Fall Thru / 0   Clause: sym_v1 == sym_v2");
            System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
            System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
            // This is the falling thru clause: sym_v1 == sym_v2
            if (sym_v1 != null) {
              if (sym_v2 != null) { // both are symbolic values
                pc._addDet(Comparator.EQ, sym_v1, sym_v2);
              } else
                pc._addDet(Comparator.EQ, sym_v1, v2);
            } else
              pc._addDet(Comparator.EQ, v1, sym_v2);
            if (!pc.simplify()) {// not satisfiable
              ss.setIgnored(true);
            } else {
              // pc.solve();
              ((PCChoiceGenerator) cg).setCurrentPC(pc);
              // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
            }
          }
          System.out.println("++++++ PC AFTER UPDATE: "
                             + ((PCChoiceGenerator) cg).getCurrentPC().header);



      System.out.println("++++++ TAKE BRANCH / 0   Clause: sym_v1 "
                         + branchComp + " sym_v2");
      System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
      System.out.println("++++++ v1: " + v1 + "    v2: " + v2);



      System.out.println("++++++ FALL THROUGH / 1   Clause: sym_v1 "
                         + fallThruComp + " sym_v2");
      System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
      System.out.println("++++++ v1: " + v1 + "    v2: " + v2);


    System.out.println("++++++ v1: " + v1 + "    v2: " + v2);


    System.out.println("++++++ PC AFTER UPDATE: "
                       + ((PCChoiceGenerator) cg).getCurrentPC().header);
*/


