package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class ExceptionExtractor {

    /**
     *
     * @param traceResults the trace result
     * @return the filtered key-value map
     */
    public static Map<VariableInfo, List<String>> extractWithAbandonTrueValue(List<TraceResult> traceResults, List<VariableInfo> vars){
        Map<VariableInfo, List<String>> trueValues = new HashMap<VariableInfo, List<String>>();
        Map<VariableInfo, List<String>> exceptionValues = new HashMap<VariableInfo, List<String>>();
        // Add true values to the trueValues List.
        for (TraceResult traceResult: traceResults){
            if (!traceResult.getTestResult()) {
                continue;
            }
            Set<String> keys = traceResult.getResultMap().keySet();
            for (String key: keys){
                VariableInfo infoKey = getVariableInfoWithName(vars, key);
                List<String> value = trueValues.containsKey(infoKey)?appandList(trueValues.get(infoKey),traceResult.get(key)):traceResult.get(key);
                trueValues.put(infoKey, value);
            }
        }
        // Add the exception values to the exceptionValues List
        for (TraceResult traceResult: traceResults){
            if (traceResult.getTestResult()){
                continue;
            }
            Set<String> keys = traceResult.getResultMap().keySet();
            for (String key: keys){
                VariableInfo infoKey = getVariableInfoWithName(vars, key);
                if (!exceptionValues.containsKey(infoKey)){
                    exceptionValues.put(infoKey, new ArrayList<String>());
                }
                for (String value: traceResult.get(key)){
                    String[] valueArray = StringToArray(value);
                    int count = 0;
                    for (String v: valueArray){
                        if (trueValues.get(infoKey).toString().contains(","+v) || trueValues.get(infoKey).toString().contains(v+",")){
                            count++;
                        }
                    }
                    if (count < valueArray.length){
                        exceptionValues.get(infoKey).add(value);
                    }
                }
            }
        }
        return exceptionValues;
    }

    /**
     *
     * @param exceptionVariable the  key-value map to be filtered
     * @param project the project name use for search
     * @param count the parameter of filtering
     * @return the filtered key-value map
     */
    public static Map<VariableInfo, List<String>> filterWithSearchBoundary(Map<VariableInfo, List<String>> exceptionVariable, String project, int count){
        Map<VariableInfo, List<String>> result = new HashMap<VariableInfo, List<String>>();
        for (Map.Entry<VariableInfo, List<String>> entry: exceptionVariable.entrySet()){
            ArrayList<String> keywords = new ArrayList<String>();
            keywords.add("if");
            keywords.add(entry.getKey().variableName);
            GathererJava gathererJava = new GathererJava(keywords, project+"-"+entry.getKey().variableName);
            if (!new File("experiment/searchcode/"+project+"-"+entry.getKey().variableName).exists()){
                gathererJava.searchCode();
            }
            BoundaryCollect boundaryCollect = new BoundaryCollect("experiment/searchcode/"+project+"-"+entry.getKey().variableName);
            List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
            List<BoundaryInfo> filteredList = BoundaryFilter.getBoundaryWithName(boundaryList, entry.getKey().variableName);
            for (String value: entry.getValue()){
                String valueType = entry.getKey().isSimpleType?entry.getKey().variableSimpleType.toString():entry.getKey().otherType;
                int valueCount = BoundaryFilter.countTheValueOccurs(filteredList,value, valueType);
                if (valueCount >= count){
                    if (result.containsKey(entry.getKey())){
                        result.get(entry.getKey()).add(value);
                    }
                    else {
                        result.put(entry.getKey(),Arrays.asList(value));
                    }
                }
            }
        }
        return result;
    }

    private static <T> List<T> appandList(List<T> aa, List<T> bb){
        List<T> result = new ArrayList<T>();
        result.addAll(aa);
        result.removeAll(bb);
        result.addAll(bb);
        return result;
    }

    private static String[] StringToArray(String value){
        if (value.startsWith("[") && value.endsWith("]")){
            return value.substring(1,value.length()-1).split(",");
        }
        return new String[]{value};
    }

    private static VariableInfo getVariableInfoWithName(List<VariableInfo> infos, String name){
        for (VariableInfo info: infos){
            if (info.variableName.equals(name)){
                return info;
            }
        }
        return null;
    }

}
