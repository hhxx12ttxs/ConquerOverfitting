/**
 * File: StatisticHelper.java
 * Created by: mhaimel
 * Created on: 8 Jun 2009
 * CVS:  $Id: StatisticHelper.java,v 1.4 2009/12/07 11:13:46 mhaimel Exp $
 */
package uk.ac.ebi.curtain.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.Map.Entry;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import uk.ac.ebi.curtain.model.graph.curtain.StatisticSummary;

/**
 * @author mhaimel
 *
 */
public class StatisticHelper<T extends Number> {
	
	private final List<T> values;

	public StatisticHelper(Collection<T> list) {
		this(list,false);
	}
	
	@SuppressWarnings("unchecked")
	public StatisticHelper(Collection<T> list,boolean isSorted) {
		List val = new ArrayList(list);
		if(!isSorted){
			Collections.sort(val);
		}
		this.values = val;
	}
	
	public List<T> getValues() {
		return values;
	}
	public Number wCov(int coverage){
		return wCov(getValues(), coverage);
	}
	
	public Integer occurrence(T val){
		return occurrence(getValues(),val);
	}

	public T max(){
		return max(getValues());
	}
	
	public T min(){
		return min(getValues());
	}
	
	public T q1(){
		return quartile(0.25);
	}

	public T q3(){
		return quartile(0.75);
	}
	
	public T quartile(double pct){
		return quartile(getValues(), pct);
	}
	
	public Number sum(){
		return sum(getValues());
	}
	
	public Number mean(){
		return mean(getValues());
	}

	public T median(){
		return quartile(getValues(),0.5);
	}
	
	public Number avg(){
		return mean();
	}	
	
	public T getN50(){
		return getNxValue(50);
	}
	
	public StatisticHelper<T> getTailList(T value, boolean include){
		List<T> list = new ArrayList<T>();
		for(T val : getValues()){
			
			if(include){
				if(val.doubleValue() >= value.doubleValue()){
					list.add(val);
				}
			} else {
				if(val.doubleValue() > value.doubleValue()){
					list.add(val);
				}
			}
		}
		return new StatisticHelper<T>(list,true);
	}
	
	public StatisticHelper<T> getHeadList(T value, boolean include){
		List<T> list = new ArrayList<T>();
		for(T val : getValues()){
			
			if(include){
				if(val.doubleValue() <= value.doubleValue()){
					list.add(val);
				} else {
					break; // sorted list -> 
				}
			} else {
				if(val.doubleValue() < value.doubleValue()){
					list.add(val);
				}
			}
		}
		return new StatisticHelper<T>(list,true);
	}
	
	/**
	 * Calculates N50 for given increasing sorted list
	 * <code>null</code> if the list is empty
	 * @param sortedList
	 * @param total
	 * @return nx Integer (e.g. 50 for N50)
	 */
	public T getNxValue(Integer x) {
		return getNxValue(getValues(),x);
	}


	public Double getStandardDeviation() {
		return getStdPopulation();
	}

	public Double getStdPopulation() {
		return getStd(0);
	}
	
	private Double getStd(int deduct){
		Double sum = differenceSum();
		int size = getSize();
		//(SUM/N-1)^(1/2)
		double deviation = Math.sqrt(sum /(size-deduct)); 	
		return deviation;
	}
	
	public Number zScore(int value) {
		return zScore(mean(),getStandardDeviation(),value);
	}
	
	public static <T extends Number> Integer occurrence(List<T> values, T val) {
		int cnt = 0;
		// list is sorted
		for(T v : values){
			if(v.equals(val)){
				++cnt;
			} else if(cnt > 0) {
				// sorted list - once a run of occurrences  was found - stop
				break;
			}
		}	
		return cnt;
	}
	
	public static Number getDistanceWeight(Integer insSize, Integer sd, Integer distance){
		int delta = Math.abs(insSize-distance);
		NormalDistributionImpl impl = new NormalDistributionImpl();
		impl.setMean(insSize);
		impl.setStandardDeviation(sd);
		double v;
		try {
			v = impl.cumulativeProbability(insSize-delta);
			v += 1-impl.cumulativeProbability(insSize+delta);
			double v3 = 1-((1-v)/(1-0.30));		
			return v3 > 0?v3:0;
		} catch (MathException e) {
			throw new Error(e);
		}
	}
	
	public static Number getPairedCoverage(Integer insSize, Integer sd, Integer gap, Integer readLength){
		Integer g = Math.abs(gap) + readLength+readLength;
		NormalDistributionImpl impl = new NormalDistributionImpl();
		impl.setMean(insSize);
		impl.setStandardDeviation(sd);
		try {
			double v = 1 - impl.cumulativeProbability(g);
			return v;
		} catch (MathException e) {
			throw new Error(e);
		}
	}

	private Double differenceSum(){
		return differenceSum(getValues());
	}
	
	public Double getVariance(){
		Double sum = differenceSum();
		int size = getSize();
		//(SUM/N)
		double variance = sum /size ;	
		return variance;
	}
	
	public int getSize() {
		return getValues().size();
	}
	
	
	public static <T extends Number> Number zScore(Number mean, Double std,int value) {
		double l = 1d/(Math.sqrt(2*Math.PI*std*std));
		double r = Math.pow(Math.E, 0 - (Math.pow(value-mean.doubleValue(),2)/(2*std*std)));
		
		return l*r;
	}
	
	public static <T extends Number> Number wCov(List<T> list, int coverage){
		return sum(list).doubleValue()*coverage;
	}
	
	public static <T extends Number> T max(List<T> list){
		if(!list.isEmpty())
			return list.get(list.size()-1);
		return null;
	}
	
	public static <T extends Number> T min(List<T> list){
		if(!list.isEmpty())
			return list.get(0);
		return null;
	}
	
	public static <T extends Number> T q1(List<T> list){
		return quartile(list,0.25);
	}

	
	public static <T extends Number> T quartile(List<T> list, double pct){
		if(list.isEmpty()){
			return null;
		}
		return list.get(
				Math.max(0,
					Math.min(
						list.size()-1, 
						(int)Math.round(list.size()*pct))));
	}

	public static <T extends Number> T q3(List<T> list){
		return quartile(list,0.75);
	}
	
	public static <T extends Number> Number sum(Collection<List<T>> listColl){
		double sum = 0d;
		for(List<? extends Number> list : listColl){
			sum += sum(list).doubleValue();
		}
		return sum;
	}
	
	public static <T extends Number> Number sum(List<T> list){
		double sum = 0.0;
		for(Number len : list){
			sum += len.doubleValue();
		}
		return sum;
	}
	
	public static <T extends Comparable<? super T>> void sort(Map<T, List<T>> map){
		for(Entry<T, List<T>> entry : map.entrySet()){
			Collections.sort(entry.getValue());
		}
	}
	
	public static <T extends Number> Number mean(List<T> list){
		if(list.size() == 0){
			return 0d;
		}
		Number sum = sum(list);
		return sum.doubleValue()/list.size();
	}

	public static <T extends Number> T median(List<T> list){
		return quartile(list,0.5);
	}
	
	public static <T extends Number> Number avg(List<T> list){
		return mean(list);
	}
	
	public static <T extends Number> T getN50(List<T> sortetList){
		return getNxValue(sortetList, 50);
	}

	public static Double getStandardDeviation(List<Integer> sortetList) {
		return getStdPopulation(sortetList);
	}

	public static Double getStdPopulation(List<Integer> sortetList) {
		return getStd(sortetList, 0);
	}
	
	public static <T extends Number> Double getStdSample(List<T> sortetList){
		return getStd(sortetList, 1);
	}
	
	private static <T extends Number> Double getStd(List<T> array, int deduct){
		Double sum = differenceSum(array);
		int size = array.size();
		//(SUM/N-1)^(1/2)
		double deviation = Math.sqrt(sum /(size-deduct)); 	
		return deviation;
	}
	
	public static <T extends Number> Double getVariance(List<T> array){
		Double sum = differenceSum(array);
		int size = array.size();
		//(SUM/N)
		double variance = sum /size ;	
		return variance;
	}
	
	public static <T extends Integer> List<T> onlyAbove(T min, List<T> sortedArray) {
		int i = 0;
		int size = sortedArray.size();
		while(i < size && sortedArray.get(i) <= min){
			++i;
		}
		return new ArrayList<T>(sortedArray.subList(i, sortedArray.size()));
	}	
	
	public static <T extends Integer>  List<T> onlyBelow(T max, List<T> sortedArray) {
		int i = 0;
		int size = sortedArray.size();
		while( i < size && sortedArray.get(i) < max){
			++i;
		}
		return new ArrayList<T>(sortedArray.subList(0, i));
	}
	
	/**
	 * SUM += (X - M)^2
	 * @param <T>
	 * @param array
	 * @return double
	 */
	private static <T extends Number> Double differenceSum(List<T> array){
		double sum = 0d;
		double avg = mean(array).doubleValue();
		
		// SUM += (X - M)^2
		int size = array.size();
		for(int i = 0; i < size; ++i){
			double val = array.get(i).doubleValue();
			sum += Math.pow((val-avg),2);
		}
		return sum;
	}
	
	public static void main(String[] args) {
		List<Integer> arr = Arrays.asList(new Integer[]{2, 2, 2, 3, 3, 4, 8, 8});
		System.out.println(getN50(arr));
		System.out.println(getNxValue(arr,20));
		System.out.println(getNxValue(arr,80));
	}
	
	
	
	/**
	 * Calculates N50 for given increasing sorted list
	 * <code>null</code> if the list is empty
	 * @param sortedList
	 * @param total
	 * @return nx Integer (e.g. 50 for N50)
	 */
	public static <T extends Number> T getNxValue(List<T> sortedList,Integer x) {
		if(sortedList.isEmpty()){
			return null;
		}
		Number total = sum(sortedList);
		Double mid = (total.doubleValue()*(x.doubleValue()/100));
		Double curr = 0.0;
		
//		int iCnt = sortetList.size()-1;
//		// this implementation steps one  
//		while((curr += sortetList.get(iCnt).doubleValue()) <= mid){
//			--iCnt;
//		}

		// this implementation steps one
		int len = sortedList.size();
		int iCnt = len-1;
		while((iCnt < len && iCnt >=0) && ((curr += sortedList.get(iCnt).doubleValue()) < mid)){
			--iCnt;
		}
		if(iCnt>= len){
			iCnt = len -1;
		}
		if(iCnt< 0){
			iCnt = 0;
		}
		return sortedList.get(iCnt);
	}
	
	@Override
	public String toString() {
		Number mean = mean();
		T min = min();
		Number sum = sum();
		T max = max();
		T median = median();
		Double sd = getStandardDeviation();
		return new StringBuilder()
			.append("size=").append(getSize()).append("; ")
			.append("min=").append(null!=min?min.intValue():min).append("; ")
			.append("mean=").append(null!=mean?mean.intValue():mean).append("; ")
			.append("median=").append(null!=median?median.intValue():median).append("; ")
			.append("max=").append(max).append("; ")
			.append("sum=").append(null!=sum?sum.intValue():sum).append("; ")
			.append("sd=").append(null!=sd?sd.intValue():sd).append("; ")
			.toString();
	}
	
	/**
	 * 
	 * @param sortedList of type <T>
	 * @return StatisticSummary
	 */
	public static <T extends Number> StatisticSummary<T> getStatistic(List<T> sortedList){
		return new StatisticSummary<T>()
			.setSize(sortedList.size())
			.setMin(min(sortedList))
			.setMax(max(sortedList))
			.setQ1(q1(sortedList))
			.setQ3(q3(sortedList))
			.setSum(sum(sortedList))
			.setN50(getN50(sortedList))
			.setMedian(median(sortedList))
			.setMean(mean(sortedList));
	}
	
	public static <T extends Number> String getSummary(List<T> sortedList){
		StatisticSummary<T> statistic = getStatistic(sortedList);
		return statistic.toString();
	}	
}

