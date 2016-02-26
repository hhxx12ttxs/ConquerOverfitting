package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        return generate(filteredVariable, project);
    }


    private static String generate(Map<VariableInfo, List<String>> entrys, String project){
        if (entrys.size() < 1){
            System.out.println("No Data in the Map");
            return "";
        }
        Iterator<Map.Entry<VariableInfo, List<String>>> iterator = entrys.entrySet().iterator();
        String result = "if (";
        while (iterator.hasNext()){
            result += generateWithSingleWord(iterator.next(), project);
            if (iterator.hasNext()){
                result += "||";
            }
        }
        result += ")";
        return result;
    }

    private static String generateWithSingleWord(Map.Entry<VariableInfo, List<String>> entry, String project){
        String varType = entry.getKey().isSimpleType?entry.getKey().variableSimpleType.toString():entry.getKey().otherType;
        if (entry.getValue().size() == 1){
            return  entry.getKey().variableName + " == " + entry.getValue().get(0);
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
                double doubleValue = Double.valueOf(info.value);
                if (doubleValue >biggestBoundary && doubleValue <= smallestValue){
                    biggestBoundary = doubleValue;
                }
                if (doubleValue < smallestBoundary && doubleValue >= biggestValue){
                    smallestBoundary = doubleValue;
                }
            }
            return  entry.getKey().variableName + " >= " + biggestBoundary +"&&" + entry.getKey().variableName + " <= " + smallestBoundary;
        }
        System.out.println("Nonsupport Condition for Create IF Expression");
        return "";
    }
}
