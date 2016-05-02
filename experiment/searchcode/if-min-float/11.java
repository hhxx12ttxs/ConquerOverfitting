/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snips.pml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import net.snips.pml.Constants.IncrementFunction;
import net.snips.pml.Constants.SvmKernelType;
import net.snips.pml.Constants.SvmType;


/**
 *
 * @author rhindi
 */
public class Parameter {

    //Is this parameter optimizable?
    public boolean optimizable = false;

    //The string representing this parameter
    public String key;

    //The min value for this parameter
    public float min;

    //The max value for this parameter
    public float max;

    //The increment for this parameter
    public float increment;

    //The current value
    public float value;

    //The type of operation to use for incrementation
    public IncrementFunction incrementFunction;

    public Parameter(){
    }

    public Parameter(String key, float value){
        this.key = key.toUpperCase();
        this.value = value;
    }

    public Parameter(String key, float value, float min, float max, float increment, IncrementFunction incrementFunction){
        this.key = key.toUpperCase();
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
        this.incrementFunction = incrementFunction;
    }

    public Parameter(String key, float min, float max, float increment, IncrementFunction incrementFunction){
        this.key = key.toUpperCase();
        this.min = min;
        this.max = max;
        this.increment = increment;
        this.value = (min + max) / 2.0f;
        this.incrementFunction = incrementFunction;
    }

    public Parameter(String key, float value, float min, float max, float increment, IncrementFunction incrementFunction, boolean optimizable){
        this.key = key.toUpperCase();
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
        this.optimizable = optimizable;
        this.incrementFunction = incrementFunction;
    }

    public Parameter(String key, float min, float max, float increment, IncrementFunction incrementFunction, boolean optimizable){
        this.key = key.toUpperCase();
        this.min = min;
        this.max = max;
        this.increment = increment;
        this.value = (min + max) / 2.0f;
        this.optimizable = optimizable;
        this.incrementFunction = incrementFunction;
    }

    public final ArrayList<Parameter> getAllValues(){

        ArrayList<Parameter> vals = new ArrayList<Parameter>();

        float currentVal = min;

        while(currentVal <= max){
            vals.add(new Parameter(key, currentVal, min, max, increment, incrementFunction, optimizable));
            currentVal = nextIncrement(currentVal);
        }

        return vals;
    }

    private float nextIncrement(float currentValue){

        if(incrementFunction == IncrementFunction.ADD){
            return currentValue + increment;
        }
        else if(incrementFunction == IncrementFunction.MULTIPLY)
        {
            return currentValue * increment;
        }

        return currentValue;
    }

    public final Parameter pickRandomParameterFromChoices(){

        ArrayList<Parameter> pars = getAllValues();
        return pars.get((int)(Math.random() * pars.size()));
    }

    public final Parameter pickRandomParameter(){

        Parameter p = this.clone();
        p.value = min + ((float)Math.random() * (max - min));

        return p;
    }

    public Parameter refocus(float rangeDividor){

        Parameter p = new Parameter();
        p.value = value;
        p.key = key;
        p.increment = increment / rangeDividor;
        p.incrementFunction = incrementFunction;
        p.optimizable = optimizable;

        float range = (max - min) / 2.0f;
        p.min = Math.max(min, value - (range / rangeDividor));
        p.max = Math.min(max, value + (range / rangeDividor));

        return p;
    }

    public final static HashMap<String, Parameter> operate(HashMap<String, Parameter> p1, HashMap<String, Parameter> p2, char operation){

        HashMap<String, Parameter> pars = new HashMap<String, Parameter>();

        if(p1 == null || p2 == null){
            return null;
        }

        if(p1.size() != p2.size()){
            return null;
        }

        Collection<String> keys = p1.keySet();

        for(String k : keys){

            if(!p2.containsKey(k)){
                return null;
            }

            float p2val = p2.get(k).value;
            Parameter p = p1.get(k).clone();

            if(p.optimizable){
                float val = operation == '+' ? p.value +  p2val : p.value - p2val;
                p.value = val < p.min ? p.min : val > p.max ? p.max : val;
            }

            pars.put(k, p);
        }

        return pars;
    }

    public final static HashMap<String, Parameter> mult(HashMap<String, Parameter> p1, float scalar){

        HashMap<String, Parameter> pars = new HashMap<String, Parameter>();

        Collection<String> keys = p1.keySet();

        for(String k : keys){

            Parameter p = p1.get(k).clone();

            if(p.optimizable){
                float val = scalar * p.value;
                p.value = val < p.min ? p.min : val > p.max ? p.max : val;
            }
            
            pars.put(k, p);
        }

        return pars;
    }

    public final static Parameter subP2FromP1(Parameter p1, Parameter p2){

        Parameter p = p1.clone();
        p.value -= p2.value;

        return p;

    }

    @Override
    public final Parameter clone(){
        return new Parameter(key, value, min, max, increment, incrementFunction, optimizable);
    }

    public final boolean equals(Parameter p){
        return p.key.equalsIgnoreCase(key) && p.value == value;
    }

    public final String serialize(){
        
        StringBuilder sb = new StringBuilder();

        sb.append(key);
        sb.append(Constants.serializationSeparator);

        sb.append(value);
        sb.append(Constants.serializationSeparator);

        sb.append(min);
        sb.append(Constants.serializationSeparator);

        sb.append(max);
        sb.append(Constants.serializationSeparator);

        sb.append(increment);
        sb.append(Constants.serializationSeparator);

        sb.append(incrementFunction);
        sb.append(Constants.serializationSeparator);

        sb.append(optimizable);
        sb.append(Constants.serializationSeparator);

        return sb.toString();
    }

    public int deserialize(String[] ss, int startPos){

        //String key, float value, float min, float max, float increment, IncrementFunction incrementFunction, boolean optimizable

        this.key = ss[startPos++];
        this.value = Float.parseFloat(ss[startPos++]);
        this.min = Float.parseFloat(ss[startPos++]);
        this.max = Float.parseFloat(ss[startPos++]);
        this.increment = Float.parseFloat(ss[startPos++]);
        this.incrementFunction = ss[startPos++].equalsIgnoreCase("ADD") ? IncrementFunction.ADD : IncrementFunction.MULTIPLY;;
        this.optimizable = Boolean.parseBoolean(ss[startPos++]);

        return startPos;
    }

    public static boolean areEqual(HashMap<String, Parameter> p1, HashMap<String, Parameter> p2){

        if(p1.size() != p2.size()){
            return false;
        }

        Collection<String> keys = p1.keySet();

        for(String k : keys){

            if(!p2.containsKey(k)){
                return false;
            }

            if(p2.get(k).equals(p1.get(k)) == false){
                return false;
            }
        }

        return true;
    }

}

