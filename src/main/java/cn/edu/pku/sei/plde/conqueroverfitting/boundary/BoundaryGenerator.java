package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import com.sun.org.apache.bcel.internal.generic.LUSHR;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by yanrunfa on 16/2/23.
 */
public class BoundaryGenerator {

    public static String generate(String classpath, String testClasspath, String classSrc, Suspicious suspicious, String project) throws IOException{
        List<TraceResult> traceResults = suspicious.getTraceResult(classpath,testClasspath, classSrc);
        if (traceResults.size() == 0){
            System.out.println("Cannot trace any variable");
            return "";
        }
        Map<VariableInfo, List<String>> filteredVariable = ExceptionExtractor.extract(traceResults, suspicious.getAllInfo(classSrc),project);
        if (filteredVariable.size() != 0){
            return generate(filteredVariable, project);
        }
        //else {
        //    traceResults = suspicious.getTraceResultWithAllTest(classpath,testClasspath, classSrc);
        //    Map<VariableInfo, List<String>> trueValues = getAllTrueValue(traceResults, suspicious.getAllInfo(classSrc));
        //    return generateTrueValueInterval(trueValues);
        //}
        return "";
    }

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
    }

    public static String generateTrueValueInterval(Map<VariableInfo, List<String>> trueValues){
        String result = "";
        for (Map.Entry<VariableInfo, List<String>> trueValue: trueValues.entrySet()){
            if (!MathUtils.isNumberType(trueValue.getKey().getStringType()) || trueValue.getValue().size() < 100){
                continue;
            }
            List<Double> values = new ArrayList<>();
            for (String value: trueValue.getValue()){
                try{
                    values.add(MathUtils.parseStringValue(value));
                } catch (NumberFormatException e){
                    continue;
                }
            }
            Collections.sort(values);
            Double maxValue = Double.MIN_VALUE;
            Double minValue = Double.MAX_VALUE;
            for (double value: values){
                if (value > maxValue){
                    maxValue = value;
                }
                if (value < minValue){
                    minValue = value;
                }
            }
            if (result.length() == 0){
                result += "if (";
            }
            else {
                result += " || ";
            }
            result += trueValue.getKey().variableName + ">" + maxValue + " && " +trueValue.getKey().variableName + "<" + minValue;
        }
        result+= ")";
        if (result.contains(">") || result.contains("<")){
            return result;
        }
        return "";
    }


    private static String generate(Map<VariableInfo, List<String>> entrys, String project){
        if (entrys.size() < 1){
            System.out.println("No Data in the Map");
            return "";
        }
        Iterator<Map.Entry<VariableInfo, List<String>>> iterator = entrys.entrySet().iterator();
        Map<VariableInfo, List<String>> arrayVariable = new HashMap<>();
        String result = "if (";
        while (iterator.hasNext()){
            Map.Entry<VariableInfo, List<String>> entry = iterator.next();
            if (MathUtils.isNumberArray(entry.getKey().getStringType()) && entry.getValue().size() == 1 && entry.getValue().get(0).equals("null")){
                arrayVariable.put(entry.getKey(), entry.getValue());
                continue;
            }
            result += generateWithSingleWord(entry, project);
            if (iterator.hasNext()){
                result += "||";
            }
        }
        result += ")";
        if (result.equals("if ()")){
            result = "";
        }
        int i = 0;
        for (Map.Entry<VariableInfo, List<String>> entry: arrayVariable.entrySet()){
            result += "\n";
            String forBoundary = "for ("+MathUtils.getNumberTypeOfArray(entry.getKey().getStringType())+" "+"forvar"+i+": "+entry.getKey().variableName+")";
            forBoundary += "if ("+"forvar"+i+"==" + MathUtils.getNumberTypeOfArray(entry.getKey().getStringType(),false)+"."+"NaN"+")";
            result += forBoundary;
            i++;
        }
        return result;
    }

    private static String generateWithSingleWord(Map.Entry<VariableInfo, List<String>> entry, String project){
        String varType = entry.getKey().isSimpleType?entry.getKey().variableSimpleType.toString():entry.getKey().otherType;
        if (entry.getValue().size() == 1){
            return  entry.getKey().variableName + " == " + entry.getValue().get(0);
        }
        else if (entry.getKey().interval){
            String minValue = entry.getValue().get(0);
            String maxValue = entry.getValue().get(1);
            return entry.getKey().variableName + ">" + maxValue + " && " + entry.getKey().variableName + "<" + minValue;
        }
        else if (varType.equals("INT") || varType.equals("FLOAT") || varType.equals("DOUBLE") || varType.equals("LONG") || varType.equals("SHORT")){
            BoundaryCollect boundaryCollect = new BoundaryCollect("experiment/searchcode/"+project+"-"+entry.getKey().variableName);
            List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
            List<BoundaryInfo> filteredList = BoundaryFilter.getBoundaryWithNameAndType(boundaryList, entry.getKey().variableName, varType);

            double smallestValue = Double.valueOf(entry.getValue().get(0));
            double biggestValue = Double.valueOf(entry.getValue().get(0));
            for (String value: entry.getValue()){
                double doubleValue = Double.valueOf(value);
                if (doubleValue < smallestValue){
                    smallestValue = doubleValue;
                }
                if (doubleValue > biggestValue){
                    biggestValue = doubleValue;
                }
            }

            double biggestBoundary = Double.MIN_VALUE;
            double smallestBoundary = Double.MAX_VALUE;

            for (BoundaryInfo info: filteredList){
                double doubleValue;
                if (info.value.equals("Integer.MIN_VALUE")){
                    doubleValue = Integer.MIN_VALUE;
                }
                else if (info.value.equals("Integer.MAX_VALUE")){
                    doubleValue = Integer.MAX_VALUE;
                }
                else {
                    try {
                        doubleValue = Double.valueOf(info.value);
                    } catch (NumberFormatException e){
                        System.out.println("ERROR VALUE PRASING: "+info.value);
                        continue;
                    }
                }
                if (doubleValue >biggestBoundary && doubleValue <= smallestValue){
                    biggestBoundary = doubleValue;
                }
                if (doubleValue < smallestBoundary && doubleValue >= biggestValue){
                    smallestBoundary = doubleValue;
                }
            }
            if (Math.abs(biggestBoundary) < Math.abs(smallestBoundary)){
                return  entry.getKey().variableName + " <= " + smallestBoundary;
            }
            else {
                return  entry.getKey().variableName + " >= " + biggestBoundary;
            }
        }
        System.out.println("Nonsupport Condition for Create IF Expression");
        return "";
    }
}
