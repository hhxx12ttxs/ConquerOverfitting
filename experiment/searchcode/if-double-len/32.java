package com.github.cutstock.algorithm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.github.cutstock.utils.ArithmeticUtil;
import com.github.cutstock.utils.ArrayUtil;
import com.github.cutstock.utils.CutstockUtils;

public class BFAlgorithm extends CutStockProblem {

	private double[] bestResult;
	private int currentTime = 1;
	private double maxLeft = 0;

	public BFAlgorithm(){
		
	}
	private Map<Double, Stack<BigDecimal>> groupedDataMap;

	public Map<Double, Stack<BigDecimal>> getGroupedDataMap() {
		return groupedDataMap;
	}

	public void setGroupedDataMap(Map<Double, Stack<BigDecimal>> groupedDataMap) {
		this.groupedDataMap = groupedDataMap;
	}

	private Map<Double, Stack<BigDecimal>> tempGroupData = new LinkedHashMap<Double, Stack<BigDecimal>>();
	private double[] lowDimensionData;

	private final int MAX_DIMENSION = 20;

	protected BigDecimal standardLen;

	@Override
	public CutStockResult start() {
		standardLen = new BigDecimal(rollWidth);
		List<double[]> ret = new ArrayList<double[]>();
		groupedDataMap = groupData();
		
		List<BigDecimal> selectedMaxEleList = new ArrayList<BigDecimal>();
		for (Iterator<Entry<Double, Stack<BigDecimal>>> it = groupedDataMap
				.entrySet().iterator(); it.hasNext();) {
			
			Entry<Double, Stack<BigDecimal>> maxEntry = it.next();
			Stack<BigDecimal> groupStack = maxEntry.getValue();
			BigDecimal maxKeyValue = new BigDecimal(maxEntry.getKey());
			while (!groupStack.isEmpty()) {
				BigDecimal curRest = ArithmeticUtil.subtract(standardLen,maxKeyValue);
				do {
					if (!groupStack.isEmpty()) {
						selectedMaxEleList.add(groupStack.pop());
					} else {
						break;
					}
					curRest = ArithmeticUtil.subtract(curRest,maxKeyValue);
				} while (curRest.compareTo(new BigDecimal(0)) > 0);
				if (curRest.compareTo(new BigDecimal(0)) < 0) {
					curRest = curRest.add(maxKeyValue);
				}
				// curRest as maxValue ,select combination
				// selectDatas(curRest,selectedEleList);
				lowDimensionData = reduceDimensions(curRest);
				// if (lowDimensionData.length == 0) {
				// lowDimensionData = reduceDimensions(curRest
				// + maxEntry.getKey());
				// }
				// doRecursion(lowDimensionData,curRest);
				findBestFit(lowDimensionData, curRest);
				mergeSelection(selectedMaxEleList, bestResult);
				if (bestResult.length <= 10) {
					doRecursion(bestResult, standardLen);
				}
				pushbackUnusedData(lowDimensionData, curRest);
				ret.add(bestResult);
				bestResult = null;
				selectedMaxEleList.clear();
			}
		}
		// convert 2 cut result
		CutStockResult result = CutstockUtils.convertArray2CutStock(ret,new BigDecimal(gapWidth));
		return result;
	}

	private void findBestFit(double[] lowDimensionData2, BigDecimal curRest) {
		List<BigDecimal> bestFitList = new ArrayList<BigDecimal>();
		BigDecimal minRest = curRest;
		BigDecimal tempMinRest = curRest;
		for (int i = 0; i < lowDimensionData2.length; i++) {
			int arrayNum = -1;
			for (int j = 0; j < lowDimensionData2.length; j++) {
				BigDecimal restLen = ArithmeticUtil.subtract(minRest,new BigDecimal(
						lowDimensionData2[j]));
				if (ArithmeticUtil.greaterThan(restLen, new BigDecimal(0))
						&&ArithmeticUtil.greaterThan(tempMinRest, restLen)) {
					tempMinRest = restLen;
					arrayNum = j;
				}
			}
			minRest = tempMinRest;
			if (arrayNum >= 0) {
				bestFitList.add(new BigDecimal(lowDimensionData2[arrayNum]));
			}
		}
		bestResult = ArrayUtil.toArray(bestFitList);
	}

	private void pushbackUnusedData(double[] lowDemiData, BigDecimal curRest) {
		BigDecimal curRest1 = curRest;
		for (int i = 0; i < lowDemiData.length; i++) {
			if (lowDemiData[i] == -1) {
				continue;
			}
			curRest1 = ArithmeticUtil.subtract(curRest1,new BigDecimal(lowDemiData[i]));
			if (ArithmeticUtil.greaterThanOrEqual(curRest1,new BigDecimal(0))) {
				removeFromTempMap(lowDemiData[i]);
			}
		}
		pushbackToMapFromTemp();
	}

	private void pushbackToMapFromTemp() {
		for (Iterator<Entry<Double, Stack<BigDecimal>>> it = tempGroupData
				.entrySet().iterator(); it.hasNext();) {
			Entry<Double, Stack<BigDecimal>> entryData = it.next();
			Stack<BigDecimal> dataStack = entryData.getValue();
			Double entryKey = entryData.getKey();
			while (!dataStack.isEmpty()) {
				groupedDataMap.get(entryKey).add(dataStack.pop());
			}
		}
	}

	private void removeFromTempMap(double usedData) {
		if (usedData < 0) {
			return;
		}
		for (Iterator<Entry<Double, Stack<BigDecimal>>> it = tempGroupData
				.entrySet().iterator(); it.hasNext();) {
			Entry<Double, Stack<BigDecimal>> entryData = it.next();
			if (entryData.getKey() == usedData) {
				if (!entryData.getValue().isEmpty()) {
					entryData.getValue().pop();
				}
			}
		}
	}

	private void pushbackToMap(double unusedData) {
		// add
		for (Iterator<Entry<Double, Stack<BigDecimal>>> it = groupedDataMap
				.entrySet().iterator(); it.hasNext();) {
			Entry<Double, Stack<BigDecimal>> entryData = it.next();
			if (entryData.getKey() == unusedData) {
				entryData.getValue().add(new BigDecimal(unusedData));
			}
		}
	}

	private void mergeSelection(List<BigDecimal> selectedEleList,
			double[] bestResult1) {
		// insert selectedMax to the top of bestResult
		double[] mergedBestResult = new double[bestResult1.length
				+ selectedEleList.size()];
		System.arraycopy(bestResult1, 0, mergedBestResult,
				selectedEleList.size(), bestResult1.length);
		int index = 0;
		for (Iterator<BigDecimal> it = selectedEleList.iterator(); it.hasNext();) {
			mergedBestResult[index++] = it.next().doubleValue();
		}
		bestResult = mergedBestResult;
	}

	private int[] selectDatas(int maxData, List<Integer> selectedEleList) {
		int[] selectedDataArray = new int[] {};
		return selectedDataArray;
	}

	private double[] reverseArray() {
		List<BigDecimal> dataList = ArrayUtil.toList(lowDimensionData);
		Collections.reverse(dataList);
		return ArrayUtil.toArray(dataList);
	}

	private void updateMap(int[] curBest) {
		for (int removedata : curBest) {
			for (Iterator<Entry<Double, Stack<BigDecimal>>> it = tempGroupData
					.entrySet().iterator(); it.hasNext();) {
				Entry<Double, Stack<BigDecimal>> curEntry = it.next();
				if (curEntry.getKey() == removedata) {
					if (curEntry.getValue().isEmpty()) {
						continue;
					}
					curEntry.getValue().pop();
				}
			}

		}
	}

	private double[] doRecursion(double[] dataArray, BigDecimal curMaxLeft) {
		return exhaustion(dataArray, curMaxLeft, 0, dataArray.length);
	}

	//
	// 1. check all len,if len is greater than 20(the array dimesions great than
	// 20,the calculation of greedy algorithm will be endless ),the effection
	// would not good,so
	// the len should restrict to 20;
	// 2. if all len less than 20, this step could ignore,but not,we will cut
	// some duplicate data,restrict every single data num to 5;

	public double[] reduceDimensions(BigDecimal topData) {
		for (Iterator<Double> it = groupedDataMap.keySet().iterator(); it
				.hasNext();) {
			Double keyData = it.next();
			Stack<BigDecimal> tmpStack = tempGroupData.get(keyData);
			if (tmpStack == null) {
				tempGroupData.put(keyData, new Stack<BigDecimal>());
			}

			Stack<BigDecimal> sourceGroupData = groupedDataMap.get(keyData);
			if (sourceGroupData == null || sourceGroupData.isEmpty()) {
				continue;
			}
			tmpStack = tempGroupData.get(keyData);
			while (topData.subtract(new BigDecimal(tmpStack.size() * keyData))
					.subtract(new BigDecimal(keyData))
					.compareTo(new BigDecimal(0)) > 0) {
				if (sourceGroupData.isEmpty()) {
					break;
				}
				tmpStack.add(sourceGroupData.pop());
			}
		}
		// second time to optimalize the data
		removeImpossibleData(tempGroupData);
		double[] normalData = normalizeGroupData(tempGroupData);
		return normalData;
	}

	private void removeImpossibleData(Map<Double, Stack<BigDecimal>> normalData) {
		if (isNeedRemove(normalData)) {
			List<Double> reversedIndex = getMapIndexReversed(normalData);
			BigDecimal maxLenValue = new BigDecimal(reversedIndex.get(0).doubleValue());
			BigDecimal maxRest = ArithmeticUtil.subtract(standardLen,maxLenValue);
			// the sum of lesser not more than maxRest
			List<BigDecimal> removeList = new ArrayList<BigDecimal>();
			for (int i = 1; i < reversedIndex.size(); i++) {
				Stack<BigDecimal> curDataStack = normalData.get(reversedIndex
						.get(i));
				BigDecimal curLeft = maxRest;
				for (Iterator<BigDecimal> it = curDataStack.iterator(); it
						.hasNext();) {
					BigDecimal curLen = it.next();
					curLeft = ArithmeticUtil.subtract(curLeft,curLen);
					if (curLeft.compareTo(new BigDecimal(0)) < 0) {
						removeList.add(curLen);
						pushback(curLen);
					}
				}
			}
			for (int i = 0; i < removeList.size(); i++) {
				BigDecimal curDataValue = removeList.get(i);
				if (tempGroupData.get(curDataValue) != null) {
					Stack<BigDecimal> curStack = tempGroupData
							.get(curDataValue);
					curStack.pop();
				}
			}
		}
	}

	private void pushback(BigDecimal curLen) {
		groupedDataMap.get(curLen).push(curLen);
	}

	private List<Double> getMapIndexReversed(Map<Double, Stack<BigDecimal>> normalData) {
		List<Double> indexes = new ArrayList<Double>();
		for (Iterator<Double> it = normalData.keySet().iterator(); it.hasNext();) {
			indexes.add(it.next());
		}
		Collections.reverse(indexes);
		return indexes;
	}

	private boolean isNeedRemove(Map<Double, Stack<BigDecimal>> normalData) {
		int count = 0;
		for (Iterator<Entry<Double, Stack<BigDecimal>>> it = normalData
				.entrySet().iterator(); it.hasNext();) {
			count += it.next().getValue().size();
		}
		return count > MAX_DIMENSION ? true : false;
	}

	private double[] normalizeGroupData(
			Map<Double, Stack<BigDecimal>> tempGroupData) {
		List<BigDecimal> ret = new ArrayList<BigDecimal>();
		for (Iterator<Entry<Double, Stack<BigDecimal>>> it = tempGroupData
				.entrySet().iterator(); it.hasNext();) {
			Entry<Double, Stack<BigDecimal>> groupData = it.next();
			Stack<BigDecimal> values = groupData.getValue();
			for (Iterator<BigDecimal> it1 = values.iterator(); it1.hasNext();) {
				ret.add(it1.next());
			}
		}
		return doubleValues(ret);
	}

	private double[] doubleValues(List<BigDecimal> params) {
		int len = params.size();
		double[] temp = new double[len];
		for (int i = 0; i < len; i++) {
			temp[i] = params.get(i).doubleValue();
		}
		return temp;
	}

	private double[] exhaustion(double[] array, BigDecimal curMaxLeft,
			int start, int end) {
		if (start == end) {
			calc(array, curMaxLeft);
		} else {
			for (int i = start; i < end; i++) {
				swap(array, start, i);
				exhaustion(array, curMaxLeft, start + 1, end);
				swap(array, start, i);
			}
		}
		return array;
	}

	private void swap(double[] array, int i, int k) {
		double temp = array[i];
		array[i] = array[k];
		array[k] = temp;
	}

	private double[] calc(double[] array, BigDecimal curRest) {
		int tCurrentTime = 0;
		double left = 0;
		List<BigDecimal> bestList = new ArrayList<BigDecimal>();
		for (int i = 0; i < array.length;) {
			// left = standardLen - array[i++];
			left = ArithmeticUtil.subtract(curRest,new BigDecimal(array[i++])).doubleValue();
			bestList.add(new BigDecimal(array[i - 1]));
			while (left > 0 && i < array.length) {
				double tmpLeft = left;
				tmpLeft = tmpLeft - array[i];
				if (tmpLeft > 0) {
					left = tmpLeft;
					bestList.add(new BigDecimal(array[i]));
					i++;
				} else {
					break;
				}
			}
			tCurrentTime++;
			break;
		}
		double[] bestDataArray = doubleValues(bestList);
		if (bestResult == null) {
			currentTime = tCurrentTime;
			maxLeft = left;
			setBestResult(bestDataArray.clone());
		}
		if (tCurrentTime < currentTime) {
			currentTime = tCurrentTime;
			maxLeft = left;
			setBestResult(bestDataArray.clone());

		} else if (tCurrentTime == currentTime && left < maxLeft) {
			setBestResult(bestDataArray.clone());
			maxLeft = left;
		}
		return bestDataArray;
	}

	public static int getBestFitValue(int curValue,
			Map<Integer, Stack<Integer>> groupedDataMap) {
		int bestValue = 0;
		int curKey = 0;
		int restValue = Integer.MAX_VALUE;
		for (Iterator<Integer> it = groupedDataMap.keySet().iterator(); it
				.hasNext();) {
			int keyValue = it.next();
			int curRest = curValue - keyValue;
			if (curRest < 0) {
				continue;
			}

			if (curRest < restValue) {
				restValue = curRest;
				curKey = keyValue;
			}
		}

		Stack<Integer> curBestStack = groupedDataMap.get(curKey);

		bestValue = curBestStack.pop();
		if (curBestStack.isEmpty()) {
			groupedDataMap.remove(curKey);
		}
		return bestValue;
	}

	public Map<Double, Stack<BigDecimal>> groupData() {
		Map<Double, Stack<BigDecimal>> groupedDataMap = new LinkedHashMap<Double, Stack<BigDecimal>>();
//		Arrays.sort(amountArray);
//		Arrays.sort(widthArray);
		reverse(amountArray);
		reverse(widthArray);
		for (int i = 0; i < amountArray.length; i++) {
			double curWidth = widthArray[i];
			Stack<BigDecimal> dataStack = groupedDataMap.get(curWidth);
			if (dataStack == null) {
				groupedDataMap.put(curWidth, new Stack<BigDecimal>());
				dataStack = groupedDataMap.get(curWidth);
			}
			for (int j = 0; j < amountArray[i]; j++) {
				dataStack.add(new BigDecimal(curWidth));
			}
		}
		return groupedDataMap;
	}

	
	private void reverse(double[] arr) {
		int size = arr.length;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < i; j++) {
				double temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
			}
		}
	}

	public double[] getBestResult() {
		return bestResult;
	}

	public void setBestResult(double[] bestResult) {
		this.bestResult = bestResult;
	}

}

