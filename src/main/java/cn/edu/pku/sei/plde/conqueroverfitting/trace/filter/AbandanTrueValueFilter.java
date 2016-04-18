package cn.edu.pku.sei.plde.conqueroverfitting.trace.filter;

import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
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
    public static List<ExceptionVariable> abandon(List<TraceResult> traceResults, List<VariableInfo> vars) {
        List<ExceptionVariable> exceptionValues = new ArrayList<>();
        Map<VariableInfo, List<String>> trueVariable = AbandanTrueValueFilter.getTrueValue(traceResults, vars);
        Map<VariableInfo, List<String>> falseVariable = AbandanTrueValueFilter.getFalseValue(traceResults, vars);

        List<ExceptionVariable> levelTwoCandidate = new ArrayList<>();
        for (TraceResult traceResult: traceResults){
            //跳过正确的traceResult
            if (traceResult.getTestResult()){
                continue;
            }

            for (Map.Entry<String, List<String>> entry: traceResult.getResultMap().entrySet()){
                VariableInfo variableInfo = getVariableInfoWithName(vars, entry.getKey());
                //可能没有找到
                if (variableInfo == null){
                    System.out.println("WARNING: AbandonTrueValueFilter#abandon: Connot Find VariableInfo With Variable Name "+entry.getKey());
                    continue;
                }
                //对于数组，把没在正确值中出现的元素加入怀疑值列表
                if (TypeUtils.isArrayFromName(variableInfo.variableName)){
                    List<String> falseValues = new ArrayList<>();
                    List<String> trueValues = trueVariable.get(variableInfo);
                    for (String value: entry.getValue()){
                        if (!trueValues.contains(value)){
                            falseValues.add(value);
                        }
                    }

                    if (falseValues.size() != 0){
                        ExceptionVariable variable = new ExceptionVariable(variableInfo, traceResult, falseValues);
                        if (!exceptionValues.contains(variable)){
                            exceptionValues.add(variable);
                        }
                        continue;
                    }
                }
                //跳过与正确值有交集的variable,加入第二等级怀疑变量候选列表
                if (trueVariable.containsKey(variableInfo)){
                    List<String> trueValues = trueVariable.get(variableInfo);
                    if (MathUtils.hasInterSection(trueValues, entry.getValue())){
                        ExceptionVariable variable = new ExceptionVariable(variableInfo, traceResult);
                        if (!levelTwoCandidate.contains(variable)){
                            levelTwoCandidate.add(variable);
                        }
                        continue;
                    }
                }
                ExceptionVariable variable = new ExceptionVariable(variableInfo, traceResult);
                if (!exceptionValues.contains(variable)){
                    exceptionValues.add(variable);
                }

            }
        }

        exceptionValues = cleanVariables(exceptionValues);
        if (exceptionValues.size() != 0){
            return exceptionValues;
        }
        //如果没有第一等级怀疑变量，寻找第二等级怀疑变量
        for (ExceptionVariable variable: levelTwoCandidate){
            //优先级大于1的第二等级怀疑变量
            if (variable.variable.priority > 1){
                if (!exceptionValues.contains(variable)){
                    exceptionValues.add(variable);
                }            }
            if (variable.variable.getStringType().equals("BOOLEAN")
                    && trueVariable.containsKey(variable.variable)
                    && falseVariable.containsKey(variable.variable)){
                //对于bool变量，如过正确值只有一个而错误值有两个，将与正确值相反的那个作为第二等级怀疑变量。
                if (trueVariable.get(variable.variable).size() == 1 && variable.values.size() == 2){
                    variable.values.clear();
                    if (trueVariable.get(variable.variable).get(0).equals("true")){
                        variable.values.add("false");
                    }
                    else {
                        variable.values.add("true");
                    }
                    variable.level = 2;
                    if (!exceptionValues.contains(variable)){
                        exceptionValues.add(variable);
                    }                }
                //对于bool变量，如过正确值只有两个而错误值有一个，将该错误值作为第二等级怀疑变量。
                if (trueVariable.get(variable.variable).size() == 2 && variable.values.size() == 1){
                    variable.level = 2;
                    if (!exceptionValues.contains(variable)){
                        exceptionValues.add(variable);
                    }
                }
                //如果值中有max，min之类的值，将该错误值作为第二等级变量
                for (String value: variable.values){
                    if (MathUtils.isMaxMinValue(value)){
                        variable.values.clear();
                        variable.values.add(value);
                        variable.level = 2;
                        if (!exceptionValues.contains(variable)){
                            exceptionValues.add(variable);
                        }
                        break;
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

    private static List<ExceptionVariable> cleanVariables(List<ExceptionVariable> exceptionVariable){
        List<ExceptionVariable> cleanedVariable = new ArrayList<>();
        for (ExceptionVariable var: exceptionVariable){
            //去掉不符合规则的Variable
            if (var.variable.isSimpleType && var.variable.variableSimpleType==null){
                continue;
            }
            if (!var.variable.isSimpleType && var.variable.otherType == null){
                continue;
            }
            if (var.values.size() == 0){
                continue;
            }

            //当值中出现非常不规则的数据时(作为数字变量值的长度大于10)，过滤该variable
            List<String> bannedValue = new ArrayList<>();
            for (String value: var.values){
                if (MathUtils.isNumberType(var.variable.getStringType())&& (value.length()>10 && !MathUtils.isMaxMinValue(value))){
                    bannedValue.add(value);
                }
            }
            if (bannedValue.size()== 1){
                continue;
            }
            //vip通道，出现这两个变量的时候，通常时显而易见的修复，只保留此怀疑变量即可，清除其他所有怀疑变量
            if (var.variable.variableName.equals("this") || var.variable.variableName.equals("return")){
                cleanedVariable.clear();
                cleanedVariable.add(var);
                break;
            }
            cleanedVariable.add(var);
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
