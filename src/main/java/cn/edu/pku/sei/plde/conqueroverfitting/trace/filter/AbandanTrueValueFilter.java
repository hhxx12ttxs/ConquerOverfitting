package cn.edu.pku.sei.plde.conqueroverfitting.trace.filter;

import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import java.util.*;

/**
 * Created by yanrunfa on 16/3/4.
 */
public class AbandanTrueValueFilter {
    public static Map<VariableInfo, List<String>> filter(List<TraceResult> traceResults, List<VariableInfo> vars){
        Map<VariableInfo, List<String>> trueValues = filterTrueValue(traceResults, vars);
        Map<VariableInfo, List<String>> exceptionValues = new HashMap<VariableInfo, List<String>>();
        // Add the exception values to the exceptionValues List
        for (TraceResult traceResult: traceResults){
            if (traceResult.getTestResult()){
                continue;
            }
            Set<String> keys = traceResult.getResultMap().keySet();
            for (String key: keys){
                VariableInfo infoKey = getVariableInfoWithName(vars, key);
                if (infoKey == null){
                    continue;
                }
                if (!exceptionValues.containsKey(infoKey)){
                    exceptionValues.put(infoKey, new ArrayList<String>());
                }
                if (TypeUtils.isComplexType(infoKey.getStringType())){
                    for (String value: traceResult.get(key)){
                        if (value.equals("true")){
                            exceptionValues.get(infoKey).add("null");
                        }
                    }
                    continue;
                }
                for (String value: traceResult.get(key)){
                    String[] valueArray = stringToArray(value);
                    if (!trueValues.containsKey(infoKey)){
                        exceptionValues.get(infoKey).add(value);
                    }
                    else {
                        int count = 0;
                        for (String v: valueArray){
                            if (trueValues.get(infoKey).toString().contains(", "+v) ||
                                    trueValues.get(infoKey).toString().contains(v+",") ||
                                    trueValues.get(infoKey).toString().contains("["+v+"]")
                                    ){
                                count++;
                            }
                        }
                        if (count < valueArray.length){
                            exceptionValues.get(infoKey).add(value);
                        }
                    }

                }
                //delete the blank key-value
                if (exceptionValues.get(infoKey).size() == 0){
                    exceptionValues.remove(infoKey);
                }
            }
        }
        return exceptionValues;
    }

    public static Map<VariableInfo, List<String>> filterTrueValue(List<TraceResult> traceResults, List<VariableInfo> vars){
        Map<VariableInfo, List<String>> trueValues = new HashMap<VariableInfo, List<String>>();
        for (TraceResult traceResult: traceResults){
            if (!traceResult.getTestResult()) {
                continue;
            }
            Set<String> keys = traceResult.getResultMap().keySet();
            for (String key: keys){
                VariableInfo infoKey = getVariableInfoWithName(vars, key);
                if (infoKey == null){
                    continue;
                }
                //if (infoKey.isParameter){
                //    continue;
                //}

                List<String> value = trueValues.containsKey(infoKey)?appandList(trueValues.get(infoKey),traceResult.get(key)):traceResult.get(key);
                trueValues.put(infoKey, value);
            }
        }
        return trueValues;
    }


    public static Map<VariableInfo, List<String>> getTrueValue(List<TraceResult> traceResults, List<VariableInfo> vars){
        Map<VariableInfo, List<String>> trueValues = new HashMap<VariableInfo, List<String>>();
        for (TraceResult traceResult: traceResults){
            if (!traceResult.getTestResult()) {
                continue;
            }
            Set<String> keys = traceResult.getResultMap().keySet();
            for (String key: keys){
                VariableInfo infoKey = getVariableInfoWithName(vars, key);
                if (infoKey == null){
                    continue;
                }
                List<String> value = trueValues.containsKey(infoKey)?appandList(trueValues.get(infoKey),traceResult.get(key)):traceResult.get(key);
                trueValues.put(infoKey, value);
            }
        }
        return trueValues;
    }

    public static <T> List<T> appandList(List<T> aa, List<T> bb){
        List<T> result = new ArrayList<T>();
        result.addAll(aa);
        result.removeAll(bb);
        result.addAll(bb);
        return result;
    }

    public static String[] stringToArray(String value){
        if (value.startsWith("[") && value.endsWith("]")){
            return value.substring(1,value.length()-1).split(",");
        }
        return new String[]{value};
    }

    public static VariableInfo getVariableInfoWithName(List<VariableInfo> infos, String name){
        for (VariableInfo info: infos){
            if (info.variableName.equals(name)){
                return info;
            }
        }
        return null;
    }


}
