/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.cutstock.algorithm;

import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.github.cutstock.utils.ArithmeticUtil;

public class IlogAlgorithm extends CutStockProblem {
	private double rollWidth;
	private double[] widthArray;
	private double[] amountArray;
	private double gapWidth;

	public IlogAlgorithm() {
	}

	@Override
	public void init(double len, double[] widthArray, double[] amountArray,
			double gapWidth) {
		// because we add n gapwidth,but in fact we should have n-1(last cutted
		// has no gap)
		this.rollWidth = len;
		this.widthArray = widthArray;
		this.amountArray = amountArray;
		this.gapWidth = gapWidth;
	}

	@Override
	public CutStockResult start() {
		CutStockResult result = new CutStockResult();
		try {
			IloCplex cutSolver = new IloCplex();

			IloObjective RollsUsed = cutSolver.addMinimize();
			IloRange[] Fill = new IloRange[amountArray.length];
			for (int f = 0; f < amountArray.length; f++) {
				Fill[f] = cutSolver.addRange(amountArray[f], Double.MAX_VALUE);
				// Fill[f] = cutSolver.addRange(0,amountArray[f]+10);
			}

			IloNumVarArray Cut = new IloNumVarArray();

			int nWdth = widthArray.length;
			for (int j = 0; j < nWdth; j++) {
				Cut.add(cutSolver.numVar(
						cutSolver.column(RollsUsed, 1.0).and(
								cutSolver.column(Fill[j],
										(int) (rollWidth / widthArray[j]))),
						0.0, Double.MAX_VALUE));
			}
			cutSolver.setParam(IloCplex.IntParam.RootAlg,
					IloCplex.Algorithm.Primal);

			// / PATTERN-GENERATION PROBLEM ///
			IloCplex patSolver = new IloCplex();

			IloObjective ReducedCost = patSolver.addMinimize();
			IloNumVar[] Use = patSolver.numVarArray(nWdth, 0.,
					Double.MAX_VALUE, IloNumVarType.Int);
			patSolver.addRange(-Double.MAX_VALUE,
					patSolver.scalProd(widthArray, Use), rollWidth);

			// / COLUMN-GENERATION PROCEDURE ///

			double[] newPatt = new double[nWdth];

			// / COLUMN-GENERATION PROCEDURE ///
			Map<IloNumVar, double[]> patternMap = new HashMap<IloNumVar, double[]>();
			for (;;) {
				// / OPTIMIZE OVER CURRENT PATTERNS ///
				cutSolver.solve();
				// / FIND AND ADD A NEW PATTERN ///
				double[] price = cutSolver.getDuals(Fill);
				ReducedCost.setExpr(patSolver.diff(1.,
						patSolver.scalProd(Use, price)));
				patSolver.solve();
				if (patSolver.getObjValue() > -CspConfiguration.RC_EPS) {
					break;
				}
				newPatt = patSolver.getValues(Use);
				IloColumn column = cutSolver.column(RollsUsed, 1.);
				for (int p = 0; p < newPatt.length; p++) {
					column = column.and(cutSolver.column(Fill[p], newPatt[p]));
				}
				IloNumVar cutNumVar = cutSolver.numVar(column, 0.,
						Double.MAX_VALUE);
				patternMap.put(cutNumVar, newPatt);
				Cut.add(cutNumVar);
			}

			for (int i = 0; i < Cut.getSize(); i++) {
				cutSolver.add(cutSolver.conversion(Cut.getElement(i),
						IloNumVarType.Int));
			}

			cutSolver.solve();
			for (int j = 0; j < Cut.getSize(); j++) {
				IloNumVar iloNum = Cut.getElement(j);
				double[] pat = patternMap.get(iloNum);
				if (pat != null) {
					int cutNum = (int) cutSolver.getValue(iloNum);
					// ignore the pattern type has no use.
					if (cutNum > 0) {
						PatternInfo patternInfo = new PatternInfo(cutNum);
						for (int k = 0; k < pat.length; k++) {
							BigDecimal patEle = new BigDecimal(pat[k]);
							int lenNum = patEle.intValue();
							// ignore column whose num equals 0
							if (lenNum == 0) {
								continue;
							}
							patEle = patEle.setScale(0,
									BigDecimal.ROUND_HALF_UP);
							BigDecimal len = ArithmeticUtil.subtract(
									new BigDecimal(widthArray[k]),
									new BigDecimal(gapWidth));
							patternInfo.addColumn(len, lenNum);
						}
						result.addPatternInfo(patternInfo);
					}
				}
			}
			cutSolver.end();
			patSolver.end();
		} catch (IloException exc) {
			System.err.println("Concert exception '" + exc + "' caught");
		}

		{/*
		 * 
		 * // cplex never satisfy my needs,so we have to optimize again // 1.
		 * checkout unused data // 2. tick out should-not-used data // 3. fill
		 * the gap with these data // 4. use bfalgorithm process these data
		 * double[] size = widthArray; double[] amount = amountArray;
		 * Map<Double, Double> sizeamount = new HashMap<Double, Double>();
		 * 
		 * for (int i = 0; i < size.length; i++) { sizeamount.put(size[i],
		 * amount[i]); } Iterator<PatternInfo> patternInfoIt =
		 * result.createIterator(); while (patternInfoIt.hasNext()) {
		 * PatternInfo patternInfo = patternInfoIt.next(); int patternNum =
		 * patternInfo.getPatternNum(); for (int i = 0; i < patternNum; i++) {
		 * for (ColPattern colPat : patternInfo.getColPatterns()) { double key =
		 * colPat.getColWidth().add(new BigDecimal(gapWidth)) .doubleValue();
		 * double oriAmount = sizeamount.get(key); BigDecimal left = new
		 * BigDecimal(oriAmount) .subtract(new BigDecimal(colPat.getColNum()));
		 * if (left.compareTo(new BigDecimal(0)) < 0) { //cplex add more width
		 * ,we should keep with original number colPat.setColNum((int)
		 * oriAmount); sizeamount.put(key, 0.0); } else { sizeamount.put(key,
		 * left.doubleValue()); } } } } for (Iterator<Entry<Double, Double>> it1
		 * = sizeamount.entrySet().iterator(); it1.hasNext();) { Entry<Double,
		 * Double> entry = it1.next(); if ((entry.getValue().intValue()) > 0) {
		 * PatternInfo patternInfo = new PatternInfo(1);
		 * patternInfo.addColumn(new BigDecimal(entry.getKey()).subtract(new
		 * BigDecimal(gapWidth)), entry.getValue().intValue());
		 * result.addPatternInfo(patternInfo); } }
		 */
		}
		return result;
	}

}

