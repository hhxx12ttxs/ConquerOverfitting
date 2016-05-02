/**************************************************************************************
 * Author: David Yanez                                                                *
 * Date: September 2012                                                               *
 *                                                                                    *
 **************************************************************************************/

package com.espertech.esper.projects.web_traffic.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;



/**
  *  Class used to Generate Random data 
  *
*/
public class RatioMapGenerator {

/**
  *  Function that maps a set of objects with its probability ratio for string value 
  *  @param min_val  minimum value allowed
  *  @param max_value  maximum value allowed
  *  @param ratios_array  probability distribution function of the ramdom data
*/	
	static public HashMap<Object, Double> generateStringRatioMap(String prefix, String posfix, Double[] ratios_array){
			
		HashMap<Object, Double> string_ratio_map = new HashMap<Object, Double>();
		int idx = 0;
		for (Double ratio : ratios_array){
			string_ratio_map.put((prefix+idx+posfix), ratio);
			idx +=1;
		}
		return string_ratio_map;
	}

/**
  *  Function that maps a set of objects with its probability ratio for integer value
  *  @param min_val  minimum value allowed
  *  @param max_value  maximum value allowed
  *  @param ratios_array  probability distribution function of the ramdom data
*/
	static public HashMap<Object, Double> generateIntegerRatioMap(int min_val, int max_val, Double[] ratios_array){
			
		HashMap<Object, Double> integer_ratio_map = new HashMap<Object, Double>();
		int ratio_len = ratios_array.length;
		double range = max_val-min_val;
		double step_size = range/ratio_len;
		double curr_val = min_val;

		for (Double ratio : ratios_array){
			integer_ratio_map.put((int)Math.floor(curr_val), ratio); 
			curr_val = curr_val+step_size;
		}
		return integer_ratio_map;
	}

/**
  *  Function that maps a set of objects with its probability ratio for integer value
  *  @param min_val  minimum value allowed
  *  @param max_value  maximum value allowed
  *  @param ratios_array  probability distribution function of the ramdom data
*/
	static public HashMap<Object, Double> generateObjectRatioMap(Object[] object_array, Double[] ratios_array){
		
		HashMap<Object, Double> object_ratio_map = new HashMap<Object, Double>();
		Double ratio ;
		for (int i=0;i<object_array.length;i++){
			if (i < ratios_array.length){
				ratio = ratios_array[i];
			}else{
				ratio = 0.0;
			}
			object_ratio_map.put(object_array[i], ratio);
		}
		
		return object_ratio_map;
	}

/**
  *  Generate and returns random data value given given a set of data objects given its probability distribution map 
  *  @param number_elem  number of elements to be generated
  *  @param object_generator_function_name  the function name of the function that would generate the random objects.
*/
	static public HashMap<Object, Double> generateRandomMap(int number_elem, String object_generator_function_name) {
		
		HashMap<Object, Double> ip_map = new HashMap<Object, Double>();

		double ratio_sum = 0.0;
		double ratio_value;
		for (int n_ip=0;n_ip<number_elem;n_ip++){
			ratio_value = Math.random();
		    ratio_sum += ratio_value;
		    // using java reflection to call a method dynamically to get the object

	    	Method generator_method = null;
			try {
				generator_method = RatioMapGenerator.class.getMethod(object_generator_function_name, null);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				ip_map.put(generator_method.invoke(null) , ratio_value);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		    
		}
		for (Map.Entry<Object, Double> entry : ip_map.entrySet()){
			entry.setValue(entry.getValue()/ratio_sum);
		}
		return ip_map;
	}
	
	static public String generateRandomIP(){
		
		String ip_addresse = "";
		int value = (int)Math.floor(Math.random() *256);
		for (int i=1;i<3;i++){
			value = (int)Math.floor(Math.random() *256);
			ip_addresse += "." + value;
		}
		return ip_addresse;
	}
	
	static public String generateRandomSession(){
		String session_id = "sid_";
		int value = (int)Math.floor(Math.random() *9999999);
		session_id += value;
		return session_id;
	}

}

