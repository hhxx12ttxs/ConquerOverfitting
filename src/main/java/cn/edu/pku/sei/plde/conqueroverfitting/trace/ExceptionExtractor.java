package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.SearchBoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class ExceptionExtractor {

    public static Map<VariableInfo, List<String>> extract(List<TraceResult> traceResults, List<VariableInfo> vars){
        Map<VariableInfo, List<String>> exceptionVariable = AbandanTrueValueFilter.filter(traceResults, vars);
        Map<VariableInfo, List<String>> cleanedVariable = cleanVariables(exceptionVariable);
        Map<VariableInfo, List<BoundaryInfo>> variableBoundary = SearchBoundaryFilter.getBoundary(cleanedVariable);
        return getBoundaryIntervals(cleanedVariable, variableBoundary);
    }

    private static Map<VariableInfo, List<String>> getBoundaryIntervals(Map<VariableInfo, List<String>> variableValue, Map<VariableInfo, List<BoundaryInfo>> variableBoundary){
        Map<VariableInfo, List<String>> result = new HashMap<>();
        for (Map.Entry<VariableInfo, List<String>> entry: variableValue.entrySet()){
            if (!variableBoundary.containsKey(entry.getKey()) || variableBoundary.get(entry.getKey()).size() == 0){
                continue;
            }
            List<BoundaryInfo> boundaryList = variableBoundary.get(entry.getKey());
            if (MathUtils.isNumberType(entry.getKey().getStringType())) {
                double smallestValue = MathUtils.parseStringValue(entry.getValue().get(0));
                double biggestValue = MathUtils.parseStringValue(entry.getValue().get(0));
                for (String value : entry.getValue()) {
                    double doubleValue = Double.valueOf(value);
                    if (Double.compare(doubleValue, smallestValue) < 0) {
                        smallestValue = doubleValue;
                    }
                    if (Double.compare(doubleValue, biggestValue) > 0) {
                        biggestValue = doubleValue;
                    }
                }

                double biggestBoundary = -Double.MAX_VALUE;
                double smallestBoundary = Double.MAX_VALUE;

                for (BoundaryInfo info : boundaryList) {
                    try {
                        double doubleValue = MathUtils.parseStringValue(info.value);
                        if (doubleValue >= biggestBoundary && doubleValue <= smallestValue) {
                            biggestBoundary = doubleValue;
                        }
                        if (doubleValue <= smallestBoundary && doubleValue >= biggestValue) {
                            smallestBoundary = doubleValue;
                        }
                    } catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }
                if (biggestBoundary == -Double.MAX_VALUE && smallestBoundary == Double.MAX_VALUE){
                    for (BoundaryInfo info : boundaryList) {
                        try {
                            double doubleValue = MathUtils.parseStringValue(info.value);
                            if (doubleValue <= smallestBoundary && doubleValue >= smallestValue) {
                                smallestBoundary = doubleValue;
                            }
                            if (doubleValue >= biggestBoundary && doubleValue <= biggestValue) {
                                biggestBoundary = doubleValue;
                            }
                        } catch (NumberFormatException e){
                            e.printStackTrace();
                        }
                    }
                    if (biggestBoundary == -Double.MAX_VALUE || smallestBoundary == Double.MAX_VALUE){
                        continue;
                    }
                }
                if (isSymmetricalList(entry.getValue())){
                    if (Math.abs(biggestBoundary)>Math.abs(smallestBoundary)){
                        result.put(entry.getKey(),Arrays.asList(String.valueOf(-biggestBoundary), String.valueOf(biggestBoundary)));
                    }
                    else {
                        result.put(entry.getKey(),Arrays.asList(String.valueOf(smallestBoundary), String.valueOf(-smallestBoundary)));
                    }
                }
                else {
                    result.put(entry.getKey(),Arrays.asList(String.valueOf(smallestBoundary), String.valueOf(biggestBoundary)));
                }
            }
            else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static boolean isSymmetricalList(List<String> list){
        List<Double> values = new ArrayList<>();
        for (String value: list){
            try {
                values.add(MathUtils.parseStringValue(value));
            } catch (Exception e){}
        }

        for (double value: values){
            if (!values.contains(-value)){
                return false;
            }
        }
        return true;
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
            if (CodeUtils.isForLoopParam(unrepeatValue)!=-1){
                unrepeatValue.clear();
                unrepeatValue.add(String.valueOf(CodeUtils.isForLoopParam(unrepeatValue)));
            }
            if (var.getValue().size()== 0){
                 continue;
            }
            cleanedVariable.put(var.getKey(), unrepeatValue);
        }
        return cleanedVariable;
    }


}
