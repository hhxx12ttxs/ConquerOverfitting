/**
 * ExHashMap.java   1.00    2005-3-4
 * Copyright 2004 KANAMIC . All rights reserved.
 */
package com.rainstars.common.util.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Function	:  
 *
 * @version 1.00
 * @author  YT      2005-3-4  New
 */

public class ExtHashMap extends HashMap {


	
    /**
     * Function:	Get Integer Object<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public Integer getINTEGER(String key){
        return (Integer) this.get(key);
    }
    
    /**
     * Function:	Get Long Object<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public Long	getLONG(String key){
        return (Long) this.get(key);
    }
    
    
    /**
     * Function:	Get Float object<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public Float getFLOAT(String key){
        return (Float) this.get(key);
    }
    
    /**
     * Function:	Get Double object<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public Double getDOUBLE(String key){
        return (Double) this.get(key);
    }
    
    /**
     * Function:	Get int value<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public int	getInt(String key){
        String value = (String) this.get(key);
        if (value == null) return 0;
        return Integer.parseInt(value);
    }
    
    /**
     * Function:	Get long value<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public long getLong(String key){
        String value = (String) this.get(key);
        if (value == null) return 0;
        return Long.parseLong(value);    
    }
    
    /**
     * Function:	Get float value<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public float getFloat(String key){
        String value = (String) this.get(key);
        if (value == null) return 0;
        return Float.parseFloat(value);    
    }
    
    /**
     * Function:	Get double value<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public double getDouble(String key){
        String value = (String) this.get(key);
        if (value == null) return 0.0;
        return Double.parseDouble(value);    
    }
    
    /**
     * Function:	Get String value<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public String getString(String key){
        return (String) this.get(key);
    }
    
    /**
     * Function:	Get Map Object<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public Map getMap(String key){
        return (Map) this.get(key);
    }
    
    
    /**
     * Function:	Get List Object<br>
     * 
     * Produce Describe:
     * 
     * @param key
     * @return
     */
    public List getList(String key){
        return (List) this.get(key);
    }

}

