package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.SearchBoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import com.sun.corba.se.impl.util.SUNVMCID;
import com.sun.org.apache.bcel.internal.generic.LUSHR;
import com.sun.org.apache.bcel.internal.generic.Type;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by yanrunfa on 16/2/23.
 */
public class BoundaryGenerator {

    public static List<String> generate(Suspicious suspicious, ExceptionVariable exceptionVariable, Map<VariableInfo, List<String>> trueValues, Map<VariableInfo, List<String>> falseValues, String project) {
        List<String> keywords = new ArrayList<>();
        if (exceptionVariable.name.length() == 1){
            keywords.add(suspicious.functionnameWithoutParam());
        }
        List<BoundaryInfo> variableBoundary = SearchBoundaryFilter.getBoundary(exceptionVariable, project, keywords);
        List<String> intervals = exceptionVariable.getBoundaryIntervals(variableBoundary);
        if (intervals == null) {
            return null;
        }
        String ifString = generateWithSingleWord(exceptionVariable,intervals,trueValues,falseValues);
        //如果怀疑变量的等级大于1,并且没有与之区间匹配的if生成方法，则分别对区间中的每个值生成if进行枚举。
        if ((exceptionVariable.variable.priority > 1 || trueValues.size() ==0) && ifString.equals("") && CodeUtils.isForLoopParam(intervals)==-1){
            List<String> result = new ArrayList<>();
            for (String value: intervals){
                String _ifString = generateWithSingleWord(exceptionVariable, Arrays.asList(value), trueValues, falseValues);
                if (!_ifString.equals("")){
                    result.add(_ifString);
                }
            }
            return result;
        }
        if (!ifString.equals("")){
            return Arrays.asList(ifString);
        }
        return new ArrayList<>();
    }


    private static String generateWithSingleWord(ExceptionVariable variable, List<String> intervals, Map<VariableInfo, List<String>> trueValues, Map<VariableInfo, List<String>> falseValues) {
        if (CodeUtils.isForLoopParam(intervals)!=-1){
            int falseMax = Collections.max(MathUtils.changeStringListToInteger(falseValues.get(variable.variable)));
            int falseMin = Collections.min(MathUtils.changeStringListToInteger(falseValues.get(variable.variable)));
            int trueMax = Collections.max(MathUtils.changeStringListToInteger(trueValues.get(variable.variable)));
            int trueMin = Collections.min(MathUtils.changeStringListToInteger(trueValues.get(variable.variable)));
            if (falseMax < trueMin){
                return variable.variable.variableName + " < "+trueMin;
            }
            if (falseMin > trueMax){
                return variable.variable.variableName + " > " + trueMax;
            }
            return "";
        }
        if (variable.variable.variableName.equals("this")){
            return "this.equals("+intervals.get(0)+")";
        }
        if (variable.variable.variableName.equals("return")){
            return intervals.get(0);
        }
        if (intervals.size() == 1 && variable.variable.isAddon && (trueValues.containsKey(variable.variable) || trueValues.size() == 0)){
            if (variable.variable.variableName.endsWith(".Comparable")){
                String variableName = variable.variable.variableName.substring(0,variable.variable.variableName.lastIndexOf("."));
                switch (intervals.get(0)){
                    case "true":
                        return variableName+" instanceof Comparable<?>";
                    case "false":
                        return "!("+variableName+" instanceof Comparable<?>)";
                }
            }
            if (variable.variable.variableName.endsWith(".null")){
                String variableName = variable.variable.variableName.substring(0,variable.variable.variableName.lastIndexOf("."));
                switch (intervals.get(0)){
                    case "true":
                        return variableName+" == null";
                    case "false":
                        return variableName+" != null";
                }
            }

        }
        if (MathUtils.isNumberType(variable.variable.getStringType())) {
            if (intervals.size() == 1){
                if (intervals.get(0).equals("NaN")){
                    return  MathUtils.getComplexOfNumberType(variable.variable.getStringType()) +".isNaN("+variable.variable.variableName+")";
                }
                else {
                    return variable.variable.variableName + "==" + intervals.get(0);
                }
            }
            if (intervals.size()!=2){
                return "";
            }
            double biggestBoundary = MathUtils.parseStringValue(intervals.get(1));
            double smallestBoundary = MathUtils.parseStringValue(intervals.get(0));
            if (biggestBoundary > smallestBoundary){
                double temp = biggestBoundary;
                biggestBoundary = smallestBoundary;
                smallestBoundary = temp;
            }
            String varType = MathUtils.getSimpleOfNumberType(variable.variable.getStringType());
            Map<String, String> interval = new HashMap<>();

            if (falseValues.containsKey(variable.variable)){
                List<String> falseValue = falseValues.get(variable.variable);
                for (String valueString : falseValue) {
                    double value = MathUtils.parseStringValue(valueString);
                    if (value < biggestBoundary) {
                        interval.put("forwardInterval", variable.variable.variableName + " < ("+varType+")" + biggestBoundary);
                    }
                    if (value > smallestBoundary) {
                        interval.put("backwardInterval", variable.variable.variableName + " > ("+ varType+")" + smallestBoundary);
                    }
                    if (value <= smallestBoundary && value >= biggestBoundary) {
                        interval.put("innerInterval", "("+variable.variable.variableName + " <= ("+varType+")" + smallestBoundary + " && " + variable.variable.variableName + " >= ("+varType+")" + biggestBoundary+")");
                    }
                }
                if (interval.size() == 0){
                    return "";
                }
                if (interval.size() == 1) {
                    return generateWithOneInterval(interval);
                }
                if (interval.size() == 2) {
                    return generateWithTwoInterval(interval, trueValues.get(variable.variable), smallestBoundary, biggestBoundary);
                }
            }

            //如果值相差过大:
            if (Math.abs(biggestBoundary) / Math.abs(smallestBoundary) > 1000 || Math.abs(smallestBoundary) / Math.abs(biggestBoundary) > 1000) {
                if (Math.abs(biggestBoundary) < Math.abs(smallestBoundary)) {
                    return variable.variable.variableName + " <= ("+varType+")" + smallestBoundary;
                } else {
                    return variable.variable.variableName + " >= ("+varType+")" + biggestBoundary;
                }
            } else {
                return variable.variable.variableName + " <= ("+varType+")" + smallestBoundary + " && " + variable.variable.variableName + " >= ("+varType+")" + biggestBoundary;
            }
        }
        if (intervals.size() == 1){
            return variable.variable.variableName + "==" + intervals.get(0);
        }
        System.out.println("Nonsupport Condition for Create IF Expression");
        return "";
    }

    private static String generateWithOneInterval(Map<String, String> intervals) {
        return (String) intervals.values().toArray()[0];
    }

    private static String generateWithTwoInterval(Map<String, String> intervals, List<String> values, double smallestBoundry, double biggestBoundry) {
        return (String) intervals.values().toArray()[0] + "||" + (String) intervals.values().toArray()[1];
    }
    /*
    private static List<String> generate(Map<VariableInfo, List<String>> entrys, Map<VariableInfo, List<String>> trueValues, Map<VariableInfo, List<String>> falseValues, List<VariableInfo> variableInfos) {
        if (entrys.size() < 1) {
            System.out.println("No Data in the Map");
            return new HashMap<>();
        }
        Iterator<Map.Entry<VariableInfo, List<String>>> iterator = entrys.entrySet().iterator();
        Map<VariableInfo, List<String>> arrayVariable = new HashMap<>();
        Map<VariableInfo, List<String>> result = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<VariableInfo, List<String>> entry = iterator.next();
            if (MathUtils.isNumberArray(variable.variable.getStringType()) && intervals.size() == 1 && intervals.get(0).equals("null")) {
                arrayVariable.put(variable.variable, intervals);
                continue;
            }



            if (TypeUtils.isArrayFromName(variable.variable.variableName) && variable.variable.isParameter){
                for (VariableInfo info: variableInfos){
                    if (TypeUtils.isArrayFromName(info.variableName) &&
                            info.isParameter &&
                            info.getStringType().equals(variable.variable.getStringType()) && !variable.variable.variableName.equals(info.variableName)){
                        Map<VariableInfo, List<String>> tempMap = new HashMap<>();
                        tempMap.put(info, intervals);
                        result.put(info, Arrays.asList(generateWithSingleWord(tempMap.entrySet().iterator().next(),trueValues,falseValues)));
                    }
                }
            }
        }
        return result;
    }*/
}