package cn.edu.pku.sei.plde.conqueroverfitting.trace.filter;

import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.*;

/**
 * Created by yanrunfa on 16/3/4.
 */
public class AbandanTrueValueFilter {
    public static Map<VariableInfo, List<String>> abandon(List<TraceResult> traceResults, List<VariableInfo> vars) {
        List<TraceResult> traceBackup = TraceResult.copy(traceResults);
        Map<VariableInfo, List<String>> exceptionValues = new HashMap<VariableInfo, List<String>>();
        Map<VariableInfo, List<String>> trueVariable = AbandanTrueValueFilter.getTrueValue(traceBackup, vars);
        Map<VariableInfo, List<String>> falseVariable = AbandanTrueValueFilter.getFalseValue(traceBackup, vars);
        for (Map.Entry<VariableInfo, List<String>> entry: falseVariable.entrySet()){
            if (!trueVariable.containsKey(entry.getKey())){
                exceptionValues.put(entry.getKey(), entry.getValue());
                continue;
            }
            List<String> values = entry.getValue();
            int before = values.size();
            values.removeAll(trueVariable.get(entry.getKey()));
            int after = values.size();
            if (before == after){
                exceptionValues.put(entry.getKey(), entry.getValue());
            }

            if (TypeUtils.isArrayFromName(entry.getKey().variableName)){
                List<String> falseValues = new ArrayList<>();
                List<String> trueValues = trueVariable.get(entry.getKey());
                for (String value: entry.getValue()){
                    if (!trueValues.contains(value)){
                        falseValues.add(value);
                    }
                }
                if (falseValues.size() != 0){
                    exceptionValues.put(entry.getKey(), falseValues);
                }
            }
        }
        falseVariable = AbandanTrueValueFilter.getFalseValue(traceResults, vars);
        exceptionValues = cleanVariables(exceptionValues);
        if (exceptionValues.size() == 0){
            for (Map.Entry<VariableInfo, List<String>> entry: falseVariable.entrySet()){
                if (entry.getKey().priority > 1){
                    exceptionValues.put(entry.getKey(), entry.getValue());
                }
                if (entry.getKey().getStringType().equals("BOOLEAN")){
                    if (trueVariable.containsKey(entry.getKey()) && falseVariable.containsKey(entry.getKey())){
                        if (trueVariable.get(entry.getKey()).size() == 1 && falseVariable.get(entry.getKey()).size() == 2){
                            if (trueVariable.get(entry.getKey()).get(0).equals("true")){
                                exceptionValues.put(entry.getKey(), Arrays.asList("false"));
                            }
                            else {
                                exceptionValues.put(entry.getKey(), Arrays.asList("true"));
                            }
                        }
                    }
                }
            }
        }
        return exceptionValues;
    }



    public static Map<VariableInfo, List<String>> filter(List<TraceResult> traceResults, List<VariableInfo> vars){
        Map<VariableInfo, List<String>> trueValues = filterTrueValue(traceResults, vars);
        Map<VariableInfo, List<String>> exceptionValues = new HashMap<VariableInfo, List<String>>();
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
                        //if (count < valueArray.length){
                        if (count == 0 ){
                            exceptionValues.get(infoKey).add(value);
                        }
                    }
                }
                if (CodeUtils.isForLoopParam(traceResult.get(key))!=-1){
                    exceptionValues.get(infoKey).addAll(traceResult.get(key));
                }
                //delete the blank key-value
                if (exceptionValues.get(infoKey).size() == 0){
                    exceptionValues.remove(infoKey);
                }
            }
        }
        return exceptionValues;
    }

    private static Map<VariableInfo, List<String>> cleanVariables(Map<VariableInfo, List<String>> exceptionVariable){
        Map<VariableInfo, List<String>> cleanedVariable = new HashMap<>();
        for (Map.Entry<VariableInfo, List<String>> var: exceptionVariable.entrySet()){
            List<String> unrepeatValue = new ArrayList(new HashSet(var.getValue()));
            if (var.getKey() == null){
                continue;
            }
            if (var.getKey().isSimpleType && var.getKey().variableSimpleType==null){
                continue;
            }
            if (!var.getKey().isSimpleType && var.getKey().otherType == null){
                continue;
            }
            if (var.getKey().getStringType().equals("BOOLEAB")){
                if (var.getValue().contains("true") && var.getValue().contains("false")){
                    continue;
                }
            }
            List<String> bannedValue = new ArrayList<>();
            for (String value: var.getValue()){
                if (MathUtils.isNumberType(var.getKey().getStringType())&&value.length()>10){
                    bannedValue.add(value);
                }
            }
            var.getValue().removeAll(bannedValue);
            //如果是for循环的计数的数据,取最大值.
            //if (CodeUtils.isForLoopParam(unrepeatValue)!=-1){
            //    unrepeatValue.clear();
            //    unrepeatValue.add(String.valueOf(CodeUtils.isForLoopParam(unrepeatValue)));
            //}
            if (var.getValue().size()== 0){
                continue;
            }
            if (var.getKey().variableName.equals("this") || var.getKey().variableName.equals("return")){
                cleanedVariable.clear();
                cleanedVariable.put(var.getKey(), unrepeatValue);
                break;
            }
            cleanedVariable.put(var.getKey(), unrepeatValue);
        }
        return cleanedVariable;
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

    public static Map<VariableInfo, List<String>> getFalseValue(List<TraceResult> traceResults, List<VariableInfo> vars){
        Map<VariableInfo, List<String>> falseValues = new HashMap<VariableInfo, List<String>>();
        for (TraceResult traceResult: traceResults){
            if (traceResult.getTestResult()) {
                continue;
            }
            Set<String> keys = traceResult.getResultMap().keySet();
            for (String key: keys){
                VariableInfo infoKey = getVariableInfoWithName(vars, key);
                if (infoKey == null){
                    continue;
                }
                List<String> value = falseValues.containsKey(infoKey)?appandList(falseValues.get(infoKey),traceResult.get(key)):traceResult.get(key);
                falseValues.put(infoKey, value);
            }
        }
        return falseValues;
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
        Collections.sort(infos, new Comparator<VariableInfo>() {
            @Override
            public int compare(VariableInfo variableInfo, VariableInfo t1) {
                return -Integer.valueOf(variableInfo.priority).compareTo(t1.priority);
            }
        });
        List<VariableInfo> addons = new ArrayList<>();
        for (VariableInfo info: infos){
            if (info.variableName.equals(name)){
                return info;
            }
            if (TypeUtils.isComplexType(info.getStringType())){
                addons.addAll(InfoUtils.changeObjectInfo(info));
            }
        }
        for (VariableInfo info: addons){
            info.isAddon = true;
            if (info.variableName.equals(name)){
                return info;
            }
        }
        return null;
    }


}
