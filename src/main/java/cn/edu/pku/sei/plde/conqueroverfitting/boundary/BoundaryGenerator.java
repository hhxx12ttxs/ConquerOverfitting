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

    public static String generate(String classpath, String testClasspath, String classSrc, String testClassSrc, Suspicious suspicious) throws IOException {
        List<TraceResult> traceResults = suspicious.getTraceResult(classpath, testClasspath, classSrc, testClassSrc);
        if (traceResults.size() == 0) {
            System.out.println("Cannot trace any variable");
            return "";
        }
        Map<VariableInfo, List<String>> filteredVariable = ExceptionExtractor.extract(traceResults, suspicious.getAllInfo(classSrc));
        Map<VariableInfo, List<String>> trueVariable = AbandanTrueValueFilter.getTrueValue(traceResults, suspicious.getAllInfo(classSrc));
        if (filteredVariable.size() != 0) {
            return generate(filteredVariable, trueVariable);
        }
        //else {
        //    traceResults = suspicious.getTraceResultWithAllTest(classpath,testClasspath, classSrc);
        //    Map<VariableInfo, List<String>> trueValues = getAllTrueValue(traceResults, suspicious.getAllInfo(classSrc));
        //    return generateTrueValueInterval(trueValues);
        //}
        return "";
    }

    /*
    private static Map<VariableInfo,List<String>> getAllTrueValue(List<TraceResult> traceResults, List<VariableInfo> vars){
        Map<VariableInfo, List<String>> trueValues = new HashMap<VariableInfo, List<String>>();
        for (TraceResult traceResult: traceResults){
            if (!traceResult.getTestResult()) {
                continue;
            }
            Set<String> keys = traceResult.getResultMap().keySet();
            for (String key: keys){
                VariableInfo infoKey = ExceptionExtractor.getVariableInfoWithName(vars, key);
                List<String> value = trueValues.containsKey(infoKey)?ExceptionExtractor.appandList(trueValues.get(infoKey),traceResult.get(key)):traceResult.get(key);
                trueValues.put(infoKey, value);
            }
        }
        return trueValues;
    }*/

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


    private static String generate(Map<VariableInfo, List<String>> entrys, Map<VariableInfo, List<String>> trueValues) {
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
            result += generateWithSingleWord(entry, trueValues);
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
        int i = 0;
        for (Map.Entry<VariableInfo, List<String>> entry : arrayVariable.entrySet()) {
            result += "\n";
            String forBoundary = "for (" + MathUtils.getNumberTypeOfArray(entry.getKey().getStringType()) + " " + "forvar" + i + ": " + entry.getKey().variableName + ")";
            forBoundary += "if (" + "forvar" + i + "==" + MathUtils.getNumberTypeOfArray(entry.getKey().getStringType(), false) + "." + "NaN" + ")";
            result += forBoundary;
            i++;
        }
        return result;
    }

    private static String generateWithSingleWord(Map.Entry<VariableInfo, List<String>> entry, Map<VariableInfo, List<String>> trueValues) {
        if (entry.getValue().size() == 1 && !MathUtils.isNumberType(entry.getKey().getStringType())) {
            return entry.getKey().variableName + " == " + entry.getValue().get(0);
        }
        if (entry.getValue().size() == 1){
            if (entry.getValue().get(0).equals("+0")){
                return entry.getKey().variableName + " >= " + 0;
            }
            else if (entry.getValue().get(0).equals("-0")){
                return entry.getKey().variableName + " <= " + 0;
            }
        }
        /*
        else if (entry.getKey().interval) {
            String minValue = entry.getValue().get(0);
            String maxValue = entry.getValue().get(1);
            return entry.getKey().variableName + ">" + maxValue + " && " + entry.getKey().variableName + "<" + minValue;
        }*/
        if (MathUtils.isNumberType(entry.getKey().getStringType())) {
            List<String> keywords = InfoUtils.getSearchKeywords(entry.getKey());
            BoundaryCollect boundaryCollect = new BoundaryCollect("experiment/searchcode/" + StringUtils.join(keywords, "-"));
            List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
            List<BoundaryInfo> filteredList = BoundaryFilter.getBoundaryWithNameAndType(boundaryList, entry.getKey().variableName, entry.getKey().getStringType());

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

            double biggestBoundary = smallestValue;
            double smallestBoundary = biggestValue;

            for (BoundaryInfo info : filteredList) {
                if (info.value.endsWith(".NaN")){
                    continue;
                }
                try {
                    double doubleValue = MathUtils.parseStringValue(info.value);
                    if (doubleValue > biggestBoundary && doubleValue < smallestValue) {
                        biggestBoundary = doubleValue;
                    }
                    if (doubleValue <smallestBoundary && doubleValue > biggestValue) {
                        smallestBoundary = doubleValue;
                    }
                } catch (NumberFormatException e){
                    continue;
                }

            }
            String varType = MathUtils.getSimpleOfNumberType(entry.getKey().getStringType());
            Map<String, String> intervals = new HashMap<>();
            if (smallestBoundary < MathUtils.getMaxValueOfNumberType(entry.getKey().getStringType())){
                intervals.put("backwardInterval", entry.getKey().variableName + " > ("+ varType+")" + smallestBoundary);
            }
            intervals.put("innerInterval", "("+entry.getKey().variableName + " <= ("+varType+")" + smallestBoundary + " && " + entry.getKey().variableName + " >= ("+varType+")" + biggestBoundary+")");
            if (biggestBoundary> MathUtils.getMinValueOfNumberType(entry.getKey().getStringType())){
                intervals.put("forwardInterval", entry.getKey().variableName + " < ("+varType+")" + biggestBoundary);
            }
            if (trueValues.containsKey(entry.getKey())){
                List<String> trueValue = trueValues.get(entry.getKey());
                for (String valueString : trueValue) {
                    double value = MathUtils.parseStringValue(valueString);
                    if (value < biggestBoundary) {
                        intervals.remove("forwardInterval");
                    }
                    if (value > smallestBoundary) {
                        intervals.remove("backwardInterval");
                    }
                    if (value < smallestBoundary && value > biggestBoundary) {
                        intervals.remove("innerInterval");
                    }
                }
                if (intervals.size() == 0){
                    return "";
                }
                if (intervals.size() == 1) {
                    return generateWithOneInterval(intervals);
                }
                if (intervals.size() == 2) {
                    return generateWithTwoInterval(intervals);
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


    private static String generateWithTwoInterval(Map<String, String> intervals) {
        return (String) intervals.values().toArray()[0] + "||" + (String) intervals.values().toArray()[1];
    }

}