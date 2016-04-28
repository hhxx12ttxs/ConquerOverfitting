package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryWithFreq;
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
import cn.edu.pku.sei.plde.conqueroverfitting.utils.VariableUtils;
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

        Map<List<String>, String> intervals = new HashMap<>();
        if (MathUtils.isNumberType(exceptionVariable.type)){
            List<BoundaryWithFreq> variableBoundary = SearchBoundaryFilter.getBoundary(exceptionVariable, project, suspicious);
            for (String value: exceptionVariable.values){
                if (MathUtils.isMaxMinValue(value)){
                    intervals.put(Arrays.asList(value), value);
                    continue;
                }
                try {
                    ArrayList<BoundaryWithFreq> intervalss = MathUtils.generateInterval(variableBoundary, Double.valueOf(value));
                    if (intervalss == null){
                        continue;
                    }
                    intervals.put(Arrays.asList(value), intervalss.get(0).value);
                }catch (Exception e){
                    continue;
                }
                /*
                String left = intervalss.get(0).value;
                if (intervalss.get(0).leftClose >= intervalss.get(0).rightClose){
                    left = "["+left;
                }
                String right = intervalss.get(1).value;
                if (intervalss.get(1).rightClose >= intervalss.get(1).leftClose){
                    right = right + "]";
                }
                */

            }
        }
        else {
            List<BoundaryWithFreq> variableBoundary = SearchBoundaryFilter.getBoundary(exceptionVariable, project, suspicious);
            for (String value : exceptionVariable.values) {
                if (value.equals("true") || value.equals("false") || value.equals("null")) {
                    intervals.put(Arrays.asList(value), value);
                    continue;
                }
                intervals = exceptionVariable.getBoundaryIntervals(variableBoundary);
            }
        }
        List<String> returnList = new ArrayList<>();
        for (Map.Entry<List<String>, String> entry: intervals.entrySet()){
            String interval = entry.getValue();
            List<String> value = entry.getKey();
            String ifString = generateWithSingleWord(exceptionVariable,interval,value);
            if (!ifString.equals("")){
                returnList.add(ifString);
            }
        }
        return returnList;
    }


    private static String generateWithSingleWord(ExceptionVariable variable, String intervals,List<String> valueStrings) {
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
            String biggest = intervals.split("-")[0];
            String smallest = intervals.split("-")[1];
            if (biggest.startsWith("[")){
                biggestClose = true;
                biggest = biggest.substring(1);
            }
            if (smallest.endsWith("]")){
                smallestClose = true;
                smallest = smallest.substring(0, smallest.length()-1);
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

            for (String valueString: valueStrings){
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
                return generateWithTwoInterval(interval);
            }
        }

        if (variable.variable.variableName.contains("==")||variable.variable.variableName.contains("!=") || variable.variable.variableName.contains(">") || variable.variable.variableName.contains("<")){
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