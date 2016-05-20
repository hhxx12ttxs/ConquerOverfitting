/**************************************************************************************
 * Author: David Yanez                                                                *
 * Date: September 2012                                                               *
 *                                                                                    *
 **************************************************************************************/

package com.espertech.esper.projects.web_traffic.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//TODO:  refactoring the class and function name for more clear names

/**
  *  Class that is used to get random data.
*/
public class RandomObject {
	
	private static  Random random = new Random(System.currentTimeMillis());
	
	static private void Normalize_Ratios(Collection<Double> ratios){
    	/**
    	 * Make sure the sum of ratios is equal to 1
    	 */
    	Double sum_ratio = 0.0;
    	for (Double ratio: ratios){
    		sum_ratio+= ratio;
    	}

    	for (Double ratio: ratios){
    		ratio = ratio/sum_ratio;
    	}
    }
/**
  *  Convert a map of Key:Ratio to Key:Number
*/	
	static private HashMap<Object, Double> RatioMapToNumberMap(int numberOfEvents, HashMap<Object, Double> RatioMap){
	    	
	    	HashMap<Object, Double> NumberMap = new HashMap<Object, Double>(RatioMap);
	    	
	    	for (Map.Entry<Object, Double> entry : RatioMap.entrySet()){
	    		NumberMap.put(entry.getKey(), entry.getValue()*numberOfEvents);
	    	}
	    	
	    	return NumberMap;
	  }
	
	
/**
  *  get a random value given a ratio distribution 
*/
	static public Object getRandomObjectFromDistribution(HashMap<Object, Double> DistributionRatio){
		
	
    	int max_val = 1000;
    	HashMap<Object, Double> DistributionRangeNumbers = RatioMapToNumberMap(max_val, DistributionRatio);
        Double cumul_val = 0.0;
    	for (Map.Entry<Object, Double> entry : DistributionRangeNumbers.entrySet()){
    		cumul_val += entry.getValue();
    		entry.setValue(cumul_val);
    	}
    	
    	int randomNumber = Math.abs(random.nextInt()) % max_val;
    	int rangeNumber;
    	
    	for (Map.Entry<Object, Double> entry : DistributionRangeNumbers.entrySet()){
    		rangeNumber = (int) Math.floor((double) entry.getValue());
    		if (randomNumber <= rangeNumber){
    			return entry.getKey();
    		}
    		entry.setValue(cumul_val);
    	}
    	
    	return DistributionRatio.keySet().toArray()[0];
    }

	
}

