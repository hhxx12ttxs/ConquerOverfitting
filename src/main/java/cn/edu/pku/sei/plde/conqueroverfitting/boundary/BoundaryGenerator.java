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
            //keywords.add(suspicious.functionnameWithoutParam());
            keywords.add("factorial");
            keywords.add(exceptionVariable.values.iterator().next());
        }
        List<BoundaryInfo> variableBoundary = SearchBoundaryFilter.getBoundary(exceptionVariable, project, keywords);
        List<String> intervals = exceptionVariable.getBoundaryIntervals(variableBoundary);
        if (intervals == null) {
            return null;
        }
        String ifString = generateWithSingleWord(exceptionVariable,intervals,trueValues,falseValues);
        ifString = replaceSpecialNumber(ifString);
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

    private static String replaceSpecialNumber(String ifString){
        ifString = ifString.replace(String.valueOf(Integer.MIN_VALUE),"Integer.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Integer.MAX_VALUE),"Integer.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Long.MIN_VALUE),"Long.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Long.MAX_VALUE),"Long.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Double.MIN_VALUE),"Double.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Double.MAX_VALUE),"Double.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Short.MIN_VALUE),"Short.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Short.MAX_VALUE),"Short.MAX_VALUE");
        return ifString;
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
            boolean biggestClose = false;
            boolean smallestClose = false;
            String biggest = intervals.get(1);
            String smallest = intervals.get(0);
            if (intervals.get(1).endsWith("]")){
                biggestClose = true;
                biggest = biggest.substring(0, biggest.length()-1);
            }
            if (intervals.get(0).startsWith("[")){
                smallestClose = true;
                smallest = smallest.substring(1);
            }
            double biggestBoundary = MathUtils.parseStringValue(biggest);
            double smallestBoundary = MathUtils.parseStringValue(smallest);
            if (biggestBoundary > smallestBoundary){
                double temp = biggestBoundary;
                biggestBoundary = smallestBoundary;
                smallestBoundary = temp;
            }
            String varType = MathUtils.getSimpleOfNumberType(variable.variable.getStringType());
            Map<String, String> interval = new HashMap<>();

            if (falseValues.containsKey(variable.variable)){
                for (String valueString : variable.values) {
                    double value = MathUtils.parseStringValue(valueString);
                    if (value < biggestBoundary) {
                        interval.put("forwardInterval", variable.variable.variableName + lessSymbol(biggestClose)+"("+varType+")" + biggestBoundary);
                    }
                    else if (value > smallestBoundary) {
                        interval.put("backwardInterval", variable.variable.variableName + greaterSymbol(smallestClose)+"("+ varType+")" + smallestBoundary);
                    }
                    else if (value <= smallestBoundary && value >= biggestBoundary) {
                        interval.put("innerInterval", "("+variable.variable.variableName + lessSymbol(smallestClose)+"("+varType+")" + smallestBoundary + " && " + variable.variable.variableName + greaterSymbol(biggestClose)+"("+varType+")" + biggestBoundary+")");
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
        }
        if (intervals.size() == 1){
            if (variable.variable.variableName.contains("==") || variable.variable.variableName.contains(">") || variable.variable.variableName.contains("<")){
                if (intervals.get(0).equals("true")){
                    return variable.variable.variableName;
                }
                if (intervals.get(0).equals("false")){
                    return "!("+variable.variable.variableName+")";
                }
            }
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

    private static String lessSymbol(boolean close){
        return close?"<=":"<";
    }

    private static String greaterSymbol(boolean close){
        return close?">=":">";
    }

}