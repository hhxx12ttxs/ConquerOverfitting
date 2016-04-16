package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import java.util.*;

/**
 * Created by yanrunfa on 16-4-16.
 */
public class ExceptionVariable {
    public VariableInfo variable;
    public String name;
    public String type;
    public Set<String> values;
    public int level = 1;
    private TraceResult traceResult;



    public ExceptionVariable(VariableInfo variable,TraceResult traceResult){
        this.traceResult = traceResult;
        this.variable = variable;
        this.level = variable.priority;
        this.name = variable.variableName;
        this.type = variable.getStringType();
        this.values = new HashSet<>(traceResult.get(variable.variableName));
    }

    public ExceptionVariable(VariableInfo variable, TraceResult traceResult, List<String> values){
        this(variable, traceResult);
        this.values = new HashSet<>(values);
    }

    public String toString(){
        return variable.getStringType()+" "+variable.variableName+" = "+values.toString();
    }


    public List<String> getBoundaryIntervals(List<BoundaryInfo> boundaryInfos){
        List<String> valueList = new ArrayList<>(values);
        //如果是for循环的参数，原封不动放回去
        if (CodeUtils.isForLoopParam(new ArrayList<>(values))!=-1){
            return valueList;
        }
        if (MathUtils.isNumberType(type)) {
            if (valueList.size() == 1 && (valueList.get(0).equals("NaN") || boundaryInfos.size() == 0)){
                return valueList;
            }
            if (boundaryInfos.size() == 0 && level == 2){
                return valueList;
            }
            
            double smallestValue = MathUtils.parseStringValue(valueList.get(0));
            double biggestValue = MathUtils.parseStringValue(valueList.get(0));
            for (String value : valueList) {
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

            for (BoundaryInfo info : boundaryInfos) {
                try {
                    double doubleValue;
                    try {
                        doubleValue = MathUtils.parseStringValue(info.value);
                    } catch (NumberFormatException e){
                        System.out.println("ExceptionExtractor: Cannot parse numeric value "+info.value);
                        continue;
                    }
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
                for (BoundaryInfo info : boundaryInfos) {
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
                    return null;
                }
            }
            if (isSymmetricalList(valueList)){
                if (Math.abs(biggestBoundary)>Math.abs(smallestBoundary)){
                    return Arrays.asList(String.valueOf(-biggestBoundary), String.valueOf(biggestBoundary));
                }
                else {
                    return Arrays.asList(String.valueOf(smallestBoundary), String.valueOf(-smallestBoundary));
                }
            }
            else {
                return Arrays.asList(String.valueOf(smallestBoundary), String.valueOf(biggestBoundary));
            }
        }
        else {
            return valueList;
        }

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
}
