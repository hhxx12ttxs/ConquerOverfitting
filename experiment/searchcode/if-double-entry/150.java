package com.github.cutstock.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ArrayUtil {
	
	public static List<BigDecimal> toList(double[] arrays) {
		List<BigDecimal> ret = new ArrayList<BigDecimal>();
		for (int i = 0; i < arrays.length; i++) {
			ret.add(new BigDecimal(arrays[i]));
		}
		return ret;
	}
	
	public static double[] toArray(List<BigDecimal> dataList){
		double[] dataArray =new double[dataList.size()];
		int index = 0;
		for(BigDecimal data:dataList){
			dataArray[index++]=data.doubleValue();
		}
		return dataArray;
	}

	public static List<Double> normalizeDatas(double[] amountArray,
			double[] widthArray) {
		int index = 0;
		List<Double> result = new ArrayList();
		for(double width:widthArray){
			for(int i=0;i<amountArray[index];i++){
				result.add(width);
			}
			index++;
		}
		return result;
	}

	public static double[] toDoubleArray(List<Double> dataList) {
		double[] dataArray =new double[dataList.size()];
		int index = 0;
		for(Double data:dataList){
			dataArray[index++]=data.doubleValue();
		}
		return dataArray;
	}
	
	public static double[][] convert2widthamountArray(
			Map<Double, Double> sizeamount) {
		
		for (Iterator<Entry<Double, Double>> it1 = sizeamount
				.entrySet().iterator(); it1.hasNext();) {
			Entry<Double, Double> entry = it1.next();
			if ((entry.getValue().intValue()) == 0) {
				//remove unused data
				it1.remove();
			}
		}
		double[][] newWidthAmountArr = new double[2][sizeamount.size()];
		int index = 0; 
		for (Iterator<Entry<Double, Double>> it1 = sizeamount
				.entrySet().iterator(); it1.hasNext();) {
			Entry<Double, Double> entry = it1.next();
			newWidthAmountArr[0][index]=entry.getKey();
			newWidthAmountArr[1][index]=entry.getValue();
			index++;
		}
		return newWidthAmountArr;
	}
}

