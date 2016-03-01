package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class ExceptionExtractor {

    public static Map<VariableInfo, List<String>> extract(List<TraceResult> traceResults, List<VariableInfo> vars, String project){
        Map<VariableInfo, List<String>> exceptionVariable = ExceptionExtractor.extractWithAbandonTrueValue(traceResults, vars);
        Map<VariableInfo, List<String>> cleanedVariable = new HashMap<>();
        for (Map.Entry<VariableInfo, List<String>> var: exceptionVariable.entrySet()){
            List<String> unrepeatValue = new ArrayList(new HashSet(var.getValue()));
            //删掉疑似for循环的计数的数据
            if (!isForLoopParam(unrepeatValue)){
                cleanedVariable.put(var.getKey(), unrepeatValue);
            }
        }
        return ExceptionExtractor.filterWithSearchBoundary(cleanedVariable,project,10);
    }
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
                    if (!trueValues.containsKey(infoKey)){
                        exceptionValues.get(infoKey).add(value);
                    }
                    else {
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
                //delete the blank key-value
                if (exceptionValues.get(infoKey).size() == 0){
                    exceptionValues.remove(infoKey);
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
            if (entry.getKey() == null){
                continue;
            }
            if (entry.getKey().isSimpleType && entry.getKey().variableSimpleType==null){
                continue;
            }
            if (!entry.getKey().isSimpleType && entry.getKey().otherType == null){
                continue;
            }
            String valueType = entry.getKey().isSimpleType?entry.getKey().variableSimpleType.toString():entry.getKey().otherType;
            ArrayList<String> keywords = new ArrayList<String>();
            keywords.add("if");
            keywords.add(valueType);
            keywords.add(entry.getKey().variableName);
            GathererJava gathererJava = new GathererJava(keywords, project+"-"+entry.getKey().variableName);
            File codePackage = new File("experiment/searchcode/"+project+"-"+entry.getKey().variableName);
            if (!codePackage.exists()){
                gathererJava.searchCode();
                if (codePackage.list().length < 30 && !entry.getKey().isSimpleType){
                    keywords.remove(entry.getKey().variableName);
                    gathererJava = new GathererJava(keywords, project+"-"+entry.getKey().variableName);
                    gathererJava.searchCode();
                }
            }
            BoundaryCollect boundaryCollect = new BoundaryCollect("experiment/searchcode/"+project+"-"+entry.getKey().variableName);
            List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
            if (boundaryList == null){
                continue;
            }
            List<BoundaryInfo> filteredList = BoundaryFilter.getBoundaryWithName(boundaryList, entry.getKey().variableName);
            if (filteredList.size() == 0 && !entry.getKey().isSimpleType){
                //对于复杂的数据结构,不等于null总是一个好的方法
                if (result.containsKey(entry.getKey())){
                    result.get(entry.getKey()).add("null");
                }
                else {
                    result.put(entry.getKey(),new ArrayList<String>(Arrays.asList("null")));
                }
            }
            if (entry.getValue().size()< 10){
                for (String value: entry.getValue()){
                    int valueCount = BoundaryFilter.countTheValueOccurs(filteredList,value, valueType);
                    if (Config.judgeResultOfFilterWithSearchBoundary(filteredList.size(),valueCount, value)){
                        if (result.containsKey(entry.getKey())){
                            result.get(entry.getKey()).add(value);
                        }
                        else {
                            result.put(entry.getKey(),new ArrayList<String>(Arrays.asList(value)));
                        }
                    }
                }
            }
            else {

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

    private static boolean isForLoopParam(List<String> vars){
        List<Integer> nums = new ArrayList<>();
        for (String var: vars){
            if (!StringUtils.isNumeric(var)){
                return false;
            }
            try {
                int num = Integer.parseInt(var);
                nums.add(num);
            }catch (Exception e){
                return false;
            }
        }
        if (nums.size() < 5){
            return false;
        }
        Collections.sort(nums);
        int first = nums.get(0);
        for (int i=1; i< nums.size();i++){
            int second = nums.get(i);
            if (Math.abs(first-second) != 1){
                return false;
            }
            first = second;
        }
        return true;
    }
}
