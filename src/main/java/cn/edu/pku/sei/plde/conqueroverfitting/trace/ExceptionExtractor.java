package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class ExceptionExtractor {

    public static Map<String, List<String>> extractWithAbandonTrueValue(List<TraceResult> traceResults){
        Map<String, List<String>> trueValues = new HashMap<String, List<String>>();
        Map<String, List<String>> exceptionValues = new HashMap<String, List<String>>();
        // Add true values to the trueValues List.
        for (TraceResult traceResult: traceResults){
            if (!traceResult.getTestResult()) {
                continue;
            }
            Set<String> keys = traceResult.getResultMap().keySet();
            for (String key: keys){
                List<String> value = trueValues.containsKey(key)?appandList(trueValues.get(key),traceResult.get(key)):traceResult.get(key);
                trueValues.put(key, value);
            }
        }
        // Add the exception values to the exceptionValues List
        for (TraceResult traceResult: traceResults){
            if (traceResult.getTestResult()){
                continue;
            }
            Set<String> keys = traceResult.getResultMap().keySet();
            for (String key: keys){
                if (!exceptionValues.containsKey(key)){
                    exceptionValues.put(key, new ArrayList<String>());
                }
                for (String value: traceResult.get(key)){
                    if (!trueValues.get(key).contains(value)){
                        exceptionValues.get(key).add(value);
                    }
                }
            }
        }
        return exceptionValues;
    }

    private static <T> List<T> appandList(List<T> aa, List<T> bb){
        List<T> result = new ArrayList<T>();
        for (T a: aa){
            if (!result.contains(a)){
                result.add(a);
            }
        }
        for (T b: bb){
            if (!result.contains(b)){
                result.add(b);
            }
        }
        return result;
    }
}
