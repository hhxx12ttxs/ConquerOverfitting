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
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.Types;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.RealExpression;

import java.util.Arrays;


// public class DCMPG extends gov.nasa.jpf.jvm.bytecode.DCMPG {
public class DCMPG extends gov.nasa.jpf.symbc.bytecode.DCMPG {

  String _nextInsnName;


  public Instruction execute(SystemState ss, KernelState ks, ThreadInfo th) {
    StackFrame sf = th.getTopFrame();

    RealExpression sym_v1 = (RealExpression) sf.getOperandAttr(1);
    RealExpression sym_v2 = (RealExpression) sf.getOperandAttr(3);

    _nextInsnName = getNext().getMnemonic();
    if (sym_v1 == null && sym_v2 == null) { // both conditions are concrete
      return super.execute(ss, ks, th);
    } else  { // at least one condition is symbolic
      return DCMP_Utils.compareSymbolicValues(this, ss, th, sf,
                                              sym_v1, sym_v2,
                                              _nextInsnName);
    }
  }
}
// public class DCMPG extends gov.nasa.jpf.jvm.bytecode.DCMPG {

//   String _nextInsnName;


//   // NAMING DIFFERENCES, JPF CODE VS SUN'S JVM SPEC:
//   // 
//   // The roles of "v1" & "v2" in the code below are switched when compared to
//   // roles of "value1" and "value2" in Sun's/Oracle's description of the
//   // DCMPG bytecode in the JVM Spec.
//   //
//   // That is, the value at the top of the stack in the code below is called
//   // "v1".  But in the JVM spec, the value at the top of the stack is called
//   // "value2".
//   //
//   // Similarly for "v2" (here) and "value1" (JVM Spec).  Also, the attribures
//   // "sym_v1" and "sym_v2" are switched about when compared to the Spec.
//   //
//   // The naming difference means that some of the comments and code below
//   // appear to be reversed if you compare them to the JVM Spec.  However, the
//   // code works (or should work), it's just named differently than the spec.
//   //
//   // Translating DCMPG's description from the JVM Spec into the naming
//   // convention used here gives the following for concrete execution:
//   //   v1 is NaN or v2 is NaN --> 1
//   //   v2 > v1 --> 1
//   //   v2 = v1 --> 0
//   //   v2 < v1 --> -1
//   // Analogous rules apply for path conditions under symbolic execution.
//   // 
//   // This confusion is inherited from the original JPF bytecode.
//   public Instruction execute(SystemState ss, KernelState ks, ThreadInfo th) {
//     StackFrame sf = th.getTopFrame();

//     RealExpression sym_v1 = (RealExpression) sf.getOperandAttr(1);
//     RealExpression sym_v2 = (RealExpression) sf.getOperandAttr(3);

//     System.out.println("++++++ BEGIN DCMPG EXECUTE sym_v1: " + sym_v1
//                        + "    sym_v2: " + sym_v2);

//     _nextInsnName = getNext().getMnemonic();
//     if (sym_v1 == null && sym_v2 == null) { // both conditions are concrete
//       return super.execute(ss, ks, th);
//     } else  { // at least one condition is symbolic
//       ChoiceGenerator<Integer> cg;
//       int conditionValue;

//       if (!th.isFirstStepInsn()) { // first time around
//         System.out.println("++++++ DCMPG next instruction: " + getNext());
//         if ("ifle".equals(_nextInsnName)
//             || "iflt".equals(_nextInsnName)
//             || "ifge".equals(_nextInsnName)
//             || "ifgt".equals(_nextInsnName)
//             || "ifne".equals(_nextInsnName)) {
//           // If the next instruction is "ifle" or similar, then this
//           // "dcmpg" instruction is setting up for what is really only
//           // a two-way branch:
//           //  - For "ifle":
//           //    - Fall through the instruction if sym_v1 > sym_v2
//           //      (so DCMPG must push 1 onto the stack)
//           //    - Do the instruction's branch if sym_v1 <= sym_v2
//           //      (so DCMPG must push 0 onto the stack)
//           //  - For "iflt":
//           //    - Fall through the instruction if sym_v1 >= sym_v2
//           //      (so DCMPG must push 0 onto the stack)
//           //    - Do the instruction's branch if sym_v1 < sym_v2
//           //      (so DCMPG must push -1 onto the stack)
//           //  - For "ifge":
//           //    - Fall through the instruction if sym_v1 < sym_v2
//           //      (so DCMPG must push -1 onto the stack)
//           //    - Do the instruction's branch if sym_v1 >= sym_v2
//           //      (so DCMPG must push 0 onto the stack)
//           //  - For "ifgt":
//           //    - Fall through the instruction if sym_v2 <= sym_v1
//           //      (so DCMPG must push 0 onto the stack)
//           //    - Do the instruction's branch if sym_v2 > sym_v1
//           //      (so DCMPG must push 1 onto the stack)
//           //  - For "ifne":
//           //    - Fall through the instruction if sym_v1 == sym_v2
//           //      (so DCMPG must push 0 onto the stack)
//           //    - Do the instruction's branch if sym_v1 != sym_v2
//           //      (so DCMPG must push 1 onto the stack)
//           cg = new PCChoiceGenerator(2);
//         } else {
//           cg = new PCChoiceGenerator(3);
//         }
//         ss.setNextChoiceGenerator(cg);
//         return this;
//       } else { // this is what really returns results
//         ChoiceGenerator<?> curCg = ss.getChoiceGenerator();
//         assert (curCg instanceof PCChoiceGenerator)
//           : "expected PCChoiceGenerator, got: " + curCg;
//         cg = (PCChoiceGenerator)curCg;
//       }

//       double v1 = Types.longToDouble(th.longPop());
//       double v2 = Types.longToDouble(th.longPop());
//       System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//       PathCondition pc;

//       // pc is updated with the pc stored in the choice generator above
//       // get the path condition from the
//       // previous choice generator of the same type

//       ChoiceGenerator<?> prev_cg = cg.getPreviousChoiceGenerator();
//       while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
//         prev_cg = prev_cg.getPreviousChoiceGenerator();
//       }

//       if (prev_cg == null)
//         pc = new PathCondition();
//       else
//         pc = ((PCChoiceGenerator) prev_cg).getCurrentPC();

//       assert pc != null;

//       System.out.println("++++++ PC BEFORE UPDATE: " + pc.header);

//       if (cg.getTotalNumberOfChoices() == 2) {
//         if ("ifle".equals(_nextInsnName)) {
//           // Then/fall-through execution: conditionValue == 1
//           // Else/branching execution: conditionValue == 0
//           System.out.println("++++++ DCMPG/IFLE");
//           conditionValue = cg.getNextChoice().intValue();

//           if (conditionValue == 0) {
//             System.out.println("++++++ TAKE BRANCH / 0   Clause: sym_v1 <= sym_v2");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the "else" clause: sym_v1 <= sym_v2
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.LE, sym_v2, sym_v1);
//               } else
//                 pc._addDet(Comparator.LE, v2, sym_v1);
//             } else
//               pc._addDet(Comparator.LE, sym_v2, v1);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           } else { // conditionValue == 1
//             System.out.println("++++++ FALL THROUGH / 1   Clause: sym_v1 > sym_v2");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the "then" clause: sym_v1 > sym_v2
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.GT, sym_v2, sym_v1);
//               } else
//                 pc._addDet(Comparator.GT, v2, sym_v1);
//             } else
//               pc._addDet(Comparator.GT, sym_v2, v1);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           }
//           System.out.println("++++++ PC AFTER UPDATE: " + ((PCChoiceGenerator) cg).getCurrentPC().header);

//         } else if ("iflt".equals(_nextInsnName)) {
//           // The next instruction is "iflt" which needs values -1 and 0 to
//           // exercise its two different branches.  The fall-through execution
//           // of "iflt" corresponds to (conditionValue == 0) and is the
//           // "then" branch of the "if" statement.  The branching execution of
//           // "iflt" corresponds to (conditionValue == -1) and is the "else"
//           // branch of the "if" statement.
//           conditionValue = - cg.getNextChoice().intValue();
//           System.out.println("++++++ DCMPG/IFLT");
//           String[] names = sf.getLocalVariableNames();
//           System.out.println("                  names: " + Arrays.asList(names));
//           System.out.print("                  attrs: [");
//           for(int i=0;i<sf.getLocalVariableCount();i++){System.out.print(sf.getLocalAttr(i) + ", ");}
//           System.out.println("]");

//           if (conditionValue == 0) {
//             System.out.println("++++++ FALL THROUGH / 0   Clause: sym_v1 >= sym_v2");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the "else" clause: sym_v1 >= sym_v2
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.GE, sym_v2, sym_v1);
//               } else
//                 pc._addDet(Comparator.GE, v2, sym_v1);
//             } else
//               pc._addDet(Comparator.GE, sym_v2, v1);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           } else {  // conditionValue == -1
//             System.out.println("++++++ TAKE BRANCH / -1   Clause: sym_v1 < sym_v2");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the "then" clause: sym_v1 < sym_v2
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.LT, sym_v2, sym_v1);
//               } else
//                 pc._addDet(Comparator.LT, v2, sym_v1);
//             } else
//               pc._addDet(Comparator.LT, sym_v2, v1);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           }
//           System.out.println("++++++ PC AFTER UPDATE: " + ((PCChoiceGenerator) cg).getCurrentPC().header);

//         } else if ("ifge".equals(_nextInsnName)) {
//           // The next instruction is "ifge" which needs values -1 and 0 to
//           // exercise its two different branches.  The fall-through execution
//           // of "ifge" corresponds to (conditionValue == -1) and is the
//           // "then" branch of the "if" statement.  The branching execution of
//           // "ifge" corresponds to (conditionValue == 0) and is the "else"
//           // branch of the "if" statement.
//           conditionValue = - cg.getNextChoice().intValue();
//           System.out.println("++++++ DCMPG/IFGE");
//           String[] names = sf.getLocalVariableNames();
//           System.out.println("                  names: " + Arrays.asList(names));
//           System.out.print("                  attrs: [");
//           for(int i=0;i<sf.getLocalVariableCount();i++){System.out.print(sf.getLocalAttr(i) + ", ");}
//           System.out.println("]");

//           if (conditionValue == 0) {
//             System.out.println("++++++ TAKE BRANCH / 0   Clause: sym_v1 >= sym_v2");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the "else" clause: sym_v1 >= sym_v2
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.GE, sym_v2, sym_v1);
//               } else
//                 pc._addDet(Comparator.GE, v2, sym_v1);
//             } else
//               pc._addDet(Comparator.GE, sym_v2, v1);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           } else {  // conditionValue == -1
//             System.out.println("++++++ FALL THROUGH / -1   Clause: sym_v1 < sym_v2");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the "then" clause: sym_v1 < sym_v2
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.LT, sym_v2, sym_v1);
//               } else
//                 pc._addDet(Comparator.LT, v2, sym_v1);
//             } else
//               pc._addDet(Comparator.LT, sym_v2, v1);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           }
//           System.out.println("++++++ PC AFTER UPDATE: " + ((PCChoiceGenerator) cg).getCurrentPC().header);

//         } else if ("ifgt".equals(_nextInsnName)) {
//           // Fall through execution: conditionValue == 0
//           // Take branch execution: conditionValue == 1
//           System.out.println("++++++ DCMPG/IFGT");
//           conditionValue = cg.getNextChoice().intValue();

//           if (conditionValue == 1) {
//             System.out.println("++++++ TAKE BRANCH / 1   Clause: sym_v2 > sym_v1");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the "else" clause: sym_v2 > sym_v1
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.GT, sym_v2, sym_v1);
//               } else
//                 pc._addDet(Comparator.GT, v2, sym_v1);
//             } else
//               pc._addDet(Comparator.GT, sym_v2, v1);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           } else { // conditionValue == 0
//             System.out.println("++++++ THEN / 0   Clause: sym_v2 <= sym_v1");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the "then" clause: sym_v2 <= sym_v1
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.LE, sym_v2, sym_v1);
//               } else
//                 pc._addDet(Comparator.LE, v2, sym_v1);
//             } else
//               pc._addDet(Comparator.LE, sym_v2, v1);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           }
//           System.out.println("++++++ PC AFTER UPDATE: "
//                              + ((PCChoiceGenerator) cg).getCurrentPC().header);

//         } else if ("ifne".equals(_nextInsnName)) {
//           // Then/fall-through execution: conditionValue == 0
//           // Else/branching execution: conditionValue == 1
//           System.out.println("++++++ DCMPG/IFNE");
//           conditionValue = cg.getNextChoice().intValue();

//           if (conditionValue == 1) {
//             System.out.println("++++++ Take Branch / 1   Clause: sym_v1 != sym_v2");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the branching clause: sym_v1 != sym_v2
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.NE, sym_v1, sym_v2);
//               } else
//                 pc._addDet(Comparator.NE, sym_v1, v2);
//             } else
//               pc._addDet(Comparator.NE, v1, sym_v2);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           } else { // conditionValue == 0
//             System.out.println("++++++ Fall Thru / 0   Clause: sym_v1 == sym_v2");
//             System.out.println("++++++ sym_v1: " + sym_v1 + "    sym_v2: " + sym_v2);
//             System.out.println("++++++ v1: " + v1 + "    v2: " + v2);
//             // This is the falling thru clause: sym_v1 == sym_v2
//             if (sym_v1 != null) {
//               if (sym_v2 != null) { // both are symbolic values
//                 pc._addDet(Comparator.EQ, sym_v1, sym_v2);
//               } else
//                 pc._addDet(Comparator.EQ, sym_v1, v2);
//             } else
//               pc._addDet(Comparator.EQ, v1, sym_v2);
//             if (!pc.simplify()) {// not satisfiable
//               ss.setIgnored(true);
//             } else {
//               // pc.solve();
//               ((PCChoiceGenerator) cg).setCurrentPC(pc);
//               // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//             }
//           }
//           System.out.println("++++++ PC AFTER UPDATE: "
//                              + ((PCChoiceGenerator) cg).getCurrentPC().header);

//         } else {
//           throw new JPFException("DCMPG cannot handle choice generators for DCMPG+"
//                                  + (_nextInsnName == null
//                                     ? "null"
//                                     : _nextInsnName.toUpperCase())
//                                  + " instruction sequence");
//         }

//         // This DCMPG instruction is followed by something other than
//         // an IFLE, IFLT, IFGE, IFGT, or IFNE instruction, so we have
//         // to do the full set of three possible results: -1, 0, 1
//       } else {
//         conditionValue = cg.getNextChoice().intValue() - 1;

//         if (conditionValue == -1) {
//           if (sym_v1 != null) {
//             if (sym_v2 != null) { // both are symbolic values
//               pc._addDet(Comparator.LT, sym_v2, sym_v1);
//             } else
//               pc._addDet(Comparator.LT, v2, sym_v1);
//           } else
//             pc._addDet(Comparator.LT, sym_v2, v1);
//           if (!pc.simplify()) {// not satisfiable
//             ss.setIgnored(true);
//           } else {
//             // pc.solve();
//             ((PCChoiceGenerator) cg).setCurrentPC(pc);
//             // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//           }
//         } else if (conditionValue == 0) {
//           if (sym_v1 != null) {
//             if (sym_v2 != null) { // both are symbolic values
//               pc._addDet(Comparator.EQ, sym_v1, sym_v2);
//             } else
//               pc._addDet(Comparator.EQ, sym_v1, v2);
//           } else
//             pc._addDet(Comparator.EQ, v1, sym_v2);
//           if (!pc.simplify()) {// not satisfiable
//             ss.setIgnored(true);
//           } else {
//             // pc.solve();
//             ((PCChoiceGenerator) cg).setCurrentPC(pc);
//             // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//           }
//         } else {
//           if (sym_v1 != null) {
//             if (sym_v2 != null) { // both are symbolic values
//               pc._addDet(Comparator.GT, sym_v2, sym_v1);
//             } else
//               pc._addDet(Comparator.GT, v2, sym_v1);
//           } else
//             pc._addDet(Comparator.GT, sym_v2, v1);
//           if (!pc.simplify()) {// not satisfiable
//             ss.setIgnored(true);
//           } else {
//             // pc.solve();
//             ((PCChoiceGenerator) cg).setCurrentPC(pc);
//             // System.out.println(((PCChoiceGenerator) cg).getCurrentPC());
//           }
//         }
//       }
//       th.push(conditionValue, false);
//       return getNext(th);
//     }
//   }
// }

