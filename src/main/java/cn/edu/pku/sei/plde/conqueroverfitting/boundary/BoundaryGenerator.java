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


import java.io.File;
import java.io.FileWriter;
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
            //keywords.add("factorial");
            String variableName = suspicious.functionnameWithoutParam();
            String keyword = "";
            for (Character ch: variableName.toCharArray()){
                if(!((ch<='Z')&&(ch>='A'))){
                    keyword += ch;
                    continue;
                }
                break;
            }
            keywords.add(keyword);
        }

        List<BoundaryInfo> variableBoundary = SearchBoundaryFilter.getBoundary(exceptionVariable, project, keywords);
        Map<String, String> intervals = exceptionVariable.getBoundaryIntervals(variableBoundary);
        if (intervals == null) {
            return new ArrayList<>();
        }

        List<String> returnList = new ArrayList<>();
        for (Map.Entry<String, String> entry: intervals.entrySet()){
            String interval = entry.getValue();
            String value = entry.getKey();
            String ifString = generateWithSingleWord(exceptionVariable,interval,value);
            ifString = replaceSpecialNumber(ifString);
            if (!ifString.equals("")){
                returnList.add(ifString);
            }
        }
        return returnList;
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


    private static String generateWithSingleWord(ExceptionVariable variable, String intervals,String valueString) {
        if (variable.variable.variableName.equals("this")){
            return intervals;
        }
        if (variable.variable.variableName.equals("return")){
            return intervals;
        }
        if (variable.variable.isAddon){
            if (variable.variable.variableName.endsWith(".Comparable")){
                String variableName = variable.variable.variableName.substring(0,variable.variable.variableName.lastIndexOf("."));
                switch (intervals){
                    case "true":
                        return variableName+" instanceof Comparable<?>";
                    case "false":
                        return "!("+variableName+" instanceof Comparable<?>)";
                }
            }
            if (variable.variable.variableName.endsWith(".null")){
                String variableName = variable.variable.variableName.substring(0,variable.variable.variableName.lastIndexOf("."));
                switch (intervals){
                    case "true":
                        return variableName+" == null";
                    case "false":
                        return variableName+" != null";
                }
            }
        }
        if (MathUtils.isNumberType(variable.variable.getStringType())) {
            if (!intervals.contains("-")){
                if (intervals.equals("NaN")){
                    return  MathUtils.getComplexOfNumberType(variable.variable.getStringType()) +".isNaN("+variable.variable.variableName+")";
                }
                else {
                    return variable.variable.variableName + "==" + intervals;
                }
            }
            boolean biggestClose = false;
            boolean smallestClose = false;
            String biggest = intervals.split("-")[1];
            String smallest = intervals.split("-")[0];
            if (biggest.endsWith("]")){
                biggestClose = true;
                biggest = biggest.substring(0, biggest.length()-1);
            }
            if (smallest.startsWith("[")){
                smallestClose = true;
                smallest = smallest.substring(1);
            }
            double biggestBoundary;
            double smallestBoundary;
            try {
                biggestBoundary = MathUtils.parseStringValue(biggest);
                smallestBoundary = MathUtils.parseStringValue(smallest);
            } catch (Exception e){
                return "";
            }

            if (biggestBoundary > smallestBoundary){
                double temp = biggestBoundary;
                biggestBoundary = smallestBoundary;
                smallestBoundary = temp;
            }
            String varType = MathUtils.getSimpleOfNumberType(variable.variable.getStringType());
            Map<String, String> interval = new HashMap<>();

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

            if (interval.size() == 0){
                return "";
            }
            if (interval.size() == 1) {
                return generateWithOneInterval(interval);
            }
            if (interval.size() == 2) {
                return generateWithTwoInterval(interval);
            }
        }

        if (variable.variable.variableName.contains("==") || variable.variable.variableName.contains(">") || variable.variable.variableName.contains("<")){
            if (intervals.equals("true")){
                return variable.variable.variableName;
            }
            if (intervals.equals("false")){
                return "!("+variable.variable.variableName+")";
            }
        }
        return variable.variable.variableName + "==" + intervals;
    }

    private static String generateWithOneInterval(Map<String, String> intervals) {
        return (String) intervals.values().toArray()[0];
    }

    private static String generateWithTwoInterval(Map<String, String> intervals) {
        return (String) intervals.values().toArray()[0] + "||" + (String) intervals.values().toArray()[1];
    }

    private static String lessSymbol(boolean close){
        return close?"<=":"<";
    }

    private static String greaterSymbol(boolean close){
        return close?">=":">";
    }

}