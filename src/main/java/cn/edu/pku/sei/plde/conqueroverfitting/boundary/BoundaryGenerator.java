package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
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

    public static String generate(Suspicious suspicious, List<TraceResult> traceResults) throws IOException {
        Map<VariableInfo, List<String>> filteredVariable = ExceptionExtractor.extract(traceResults, suspicious.getAllInfo());
        Map<VariableInfo, List<String>> trueVariable = AbandanTrueValueFilter.getTrueValue(traceResults, suspicious.getAllInfo());
        Map<VariableInfo, List<String>> falseVariable = AbandanTrueValueFilter.getFalseValue(traceResults, suspicious.getAllInfo());

        if (filteredVariable.size() != 0) {
            return generate(filteredVariable, trueVariable, falseVariable);
        }
        return "";
    }



    private static String generate(Map<VariableInfo, List<String>> entrys, Map<VariableInfo, List<String>> trueValues, Map<VariableInfo, List<String>> falseValues) {
        if (entrys.size() < 1) {
            System.out.println("No Data in the Map");
            return "";
        }
        Iterator<Map.Entry<VariableInfo, List<String>>> iterator = entrys.entrySet().iterator();
        Map<VariableInfo, List<String>> arrayVariable = new HashMap<>();
        String result = "if ((";
        while (iterator.hasNext()) {
            Map.Entry<VariableInfo, List<String>> entry = iterator.next();
            if (MathUtils.isNumberArray(entry.getKey().getStringType()) && entry.getValue().size() == 1 && entry.getValue().get(0).equals("null")) {
                arrayVariable.put(entry.getKey(), entry.getValue());
                continue;
            }
            result += generateWithSingleWord(entry, trueValues, falseValues);
            if (iterator.hasNext()) {
                result += ")||(";
            }
        }
        result += "))";
        result = result.replace("||()","");
        result = result.replace("()||","");
        if (result.equals("if (())")) {
            result = "";
        }
        return result;
    }

    private static String generateWithSingleWord(Map.Entry<VariableInfo, List<String>> entry, Map<VariableInfo, List<String>> trueValues, Map<VariableInfo, List<String>> falseValues) {
        if (entry.getValue().size() == 1 && entry.getKey().isAddon){
            String variableName = entry.getKey().variableName.substring(0,entry.getKey().variableName.indexOf("."));
            if (entry.getKey().variableName.endsWith(".Comparable")){
                switch (entry.getValue().get(0)){
                    case "true":
                        return variableName+" instanceof Comparable<?>";
                    case "false":
                        return "!("+variableName+" instanceof Comparable<?>)";
                }
            }
            if (entry.getKey().variableName.endsWith(".null")){
                switch (entry.getValue().get(0)){
                    case "true":
                        return variableName+" == null";
                    case "false":
                        return variableName+" != null";
                }
            }
        }

        if (MathUtils.isNumberType(entry.getKey().getStringType())) {
            double biggestBoundary = MathUtils.parseStringValue(entry.getValue().get(1));
            double smallestBoundary = MathUtils.parseStringValue(entry.getValue().get(0));
            if (biggestBoundary > smallestBoundary){
                double temp = biggestBoundary;
                biggestBoundary = smallestBoundary;
                smallestBoundary = temp;
            }
            String varType = MathUtils.getSimpleOfNumberType(entry.getKey().getStringType());
            Map<String, String> intervals = new HashMap<>();
            //if (smallestBoundary < MathUtils.getMaxValueOfNumberType(entry.getKey().getStringType())){
            //    intervals.put("backwardInterval", entry.getKey().variableName + " > ("+ varType+")" + smallestBoundary);
            //}
            //intervals.put("innerInterval", "("+entry.getKey().variableName + " <= ("+varType+")" + smallestBoundary + " && " + entry.getKey().variableName + " >= ("+varType+")" + biggestBoundary+")");
            //if (biggestBoundary> MathUtils.getMinValueOfNumberType(entry.getKey().getStringType())){
            //    intervals.put("forwardInterval", entry.getKey().variableName + " < ("+varType+")" + biggestBoundary);
            //}
            if (falseValues.containsKey(entry.getKey())){
                List<String> falseValue = falseValues.get(entry.getKey());
                for (String valueString : falseValue) {
                    double value = MathUtils.parseStringValue(valueString);
                    if (value < biggestBoundary) {
                        intervals.put("forwardInterval", entry.getKey().variableName + " < ("+varType+")" + biggestBoundary);
                    }
                    if (value > smallestBoundary) {
                        intervals.put("backwardInterval", entry.getKey().variableName + " > ("+ varType+")" + smallestBoundary);
                    }
                    if (value < smallestBoundary && value > biggestBoundary) {
                        intervals.put("innerInterval", "("+entry.getKey().variableName + " <= ("+varType+")" + smallestBoundary + " && " + entry.getKey().variableName + " >= ("+varType+")" + biggestBoundary+")");
                    }
                }
                if (intervals.size() == 0){
                    return "";
                }
                if (intervals.size() == 1) {
                    return generateWithOneInterval(intervals);
                }
                if (intervals.size() == 2) {
                    return generateWithTwoInterval(intervals, trueValues.get(entry.getKey()), smallestBoundary, biggestBoundary);
                }
            }

            //如果值相差过大:
            if (Math.abs(biggestBoundary) / Math.abs(smallestBoundary) > 1000 || Math.abs(smallestBoundary) / Math.abs(biggestBoundary) > 1000) {
                if (Math.abs(biggestBoundary) < Math.abs(smallestBoundary)) {
                    return entry.getKey().variableName + " <= ("+varType+")" + smallestBoundary;
                } else {
                    return entry.getKey().variableName + " >= ("+varType+")" + biggestBoundary;
                }
            } else {
                return entry.getKey().variableName + " <= ("+varType+")" + smallestBoundary + " && " + entry.getKey().variableName + " >= ("+varType+")" + biggestBoundary;
            }
        }
        System.out.println("Nonsupport Condition for Create IF Expression");
        return "";
    }


    private static String generateWithOneInterval(Map<String, String> intervals) {
        return (String) intervals.values().toArray()[0];
    }


    private static String generateWithTwoInterval(Map<String, String> intervals, List<String> values, double smallestBoundry, double biggestBoundry) {
        //for (String value: values){
        //    if (MathUtils.parseStringValue(value) < biggestBoundry && intervals.containsKey("backwardInterval")){
        //        intervals.remove("backwardInterval");
        //        return generateWithOneInterval(intervals);
        //    }
        //    if (MathUtils.parseStringValue(value) > smallestBoundry && intervals.containsKey("forwardInterval")){
        //        intervals.remove("forwardInterval");
        //        return generateWithOneInterval(intervals);
        //    }
        //}
        return (String) intervals.values().toArray()[0] + "||" + (String) intervals.values().toArray()[1];
    }

    /*
    public static String generateTrueValueInterval(Map<VariableInfo, List<String>> filteredValues, Map<VariableInfo, List<String>> trueValues) {
        String result = "";
        for (Map.Entry<VariableInfo, List<String>> trueValue : filteredValues.entrySet()) {
            if (!MathUtils.isNumberType(trueValue.getKey().getStringType()) || trueValue.getValue().size() < 100) {
                continue;
            }
            List<Double> values = new ArrayList<>();
            for (String value : trueValue.getValue()) {
                try {
                    values.add(MathUtils.parseStringValue(value));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            Collections.sort(values);
            Double maxValue = Double.MIN_VALUE;
            Double minValue = Double.MAX_VALUE;
            for (double value : values) {
                if (value > maxValue) {
                    maxValue = value;
                }
                if (value < minValue) {
                    minValue = value;
                }
            }
            if (result.length() == 0) {
                result += "if (";
            } else {
                result += " || ";
            }
            result += trueValue.getKey().variableName + ">" + maxValue + " && " + trueValue.getKey().variableName + "<" + minValue;
        }
        result += ")";
        if (result.contains(">") || result.contains("<")) {
            return result;
        }
        return "";
    }
    */
}