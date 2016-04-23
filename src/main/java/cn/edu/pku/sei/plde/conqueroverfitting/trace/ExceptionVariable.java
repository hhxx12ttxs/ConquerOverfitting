package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import java.lang.reflect.Array;
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


    public String getAssertMessage(){
        return traceResult._testClass+"#"+traceResult._testMethod+"#"+traceResult._assertLine;
    }

    public ExceptionVariable(VariableInfo variable,TraceResult traceResult){
        this.traceResult = traceResult;
        this.variable = variable;
        this.level = variable.priority;
        this.name = variable.variableName;
        this.type = variable.getStringType();
        if (traceResult.get(variable.variableName)!=null){
            this.values = new HashSet<>(traceResult.get(variable.variableName));
        }
    }

    public ExceptionVariable(VariableInfo variable, TraceResult traceResult, List<String> values){
        this(variable, traceResult);
        this.values = new HashSet<>(values);
    }

    public String toString(){
        return variable.getStringType()+" "+variable.variableName+" = "+values.toString();
    }


    public boolean equals(Object obj){
        if (!(obj instanceof ExceptionVariable)){
            return false;
        }
        ExceptionVariable exceptionVariable = (ExceptionVariable) obj;
        for (String value: exceptionVariable.values){
            if (!values.contains(value)){
                return false;
            }
        }
        return this.variable.equals(exceptionVariable.variable) && values.size() == exceptionVariable.values.size();
    }

    public boolean judgeTheSame(String[] newValues, String[] thisValues){
        if (newValues.length != thisValues.length){
            return false;
        }
        for (int i=0; i< newValues.length; i++){
            if (!newValues[i].equals(thisValues[i])){
                return false;
            }
        }
        return true;
    }


    public List<String> getBoundaryIntervals(List<BoundaryInfo> boundaryInfos){
        List<String> valueList = new ArrayList<>(values);
        //如果是for循环的参数，原封不动放回去
        //if (CodeUtils.isForLoopParam(new ArrayList<>(values))!=-1){
        //    return valueList;
        //}
        if (name.equals("this")){
            for (BoundaryInfo info: boundaryInfos){
                if (info.value.contains("new ") && info.value.contains(type) && info.value.contains("(") && info.value.contains(")")){
                    String newValue = info.value.substring(info.value.indexOf('('+1, info.value.lastIndexOf(')')));
                    String[] newValues = newValue.contains(",")?newValue.split(","):new String[]{newValue};
                    String thisValue = values.iterator().next();
                    thisValue = thisValue.substring(info.value.indexOf('('+1, info.value.lastIndexOf(')')));
                    String[] thisValues = thisValue.contains(",")?thisValue.split(","):new String[]{thisValue};
                    if (judgeTheSame(newValues, thisValues)){
                        if (traceResult.getTestResult()){
                            return Arrays.asList("!this.equals("+newValue+")");
                        }
                        else {
                            return Arrays.asList("!this.equals("+newValue+")");
                        }
                    }
                }
            }
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
            BoundaryInfo biggestInfo = null;
            BoundaryInfo smallestInfo = null;
            for (BoundaryInfo info : boundaryInfos) {
                try {
                    double doubleValue;
                    try {
                        doubleValue = MathUtils.parseStringValue(info.value);
                    } catch (NumberFormatException e){
                        System.out.println("ExceptionExtractor: Cannot parse numeric value "+info.value);
                        continue;
                    }
                    //小于最小值的最大边界值
                    if (doubleValue >= biggestBoundary && doubleValue <= smallestValue) {
                        biggestBoundary = doubleValue;
                        biggestInfo = info;
                        if (doubleValue == smallestValue && biggestInfo.value.equals(info.value)){
                            if (info.leftClose == 1){
                                biggestInfo = info;
                            }
                        }
                        if (doubleValue < smallestValue && biggestInfo.value.equals(info.value)){
                            if (info.rightClose == 1){
                                biggestInfo = info;
                            }
                        }
                    }
                    //大于最大值的最小边界值
                    if (doubleValue <= smallestBoundary && doubleValue >= biggestValue) {
                        smallestBoundary = doubleValue;
                        smallestInfo = info;
                        if (doubleValue == biggestValue && smallestInfo.value.equals(info.value)){
                            if (info.rightClose == 1){
                                smallestInfo = info;
                            }
                        }
                        if (doubleValue > biggestValue && smallestInfo.value.equals(info.value)){
                            if (info.leftClose == 1){
                                smallestInfo = info;
                            }
                        }
                    }
                } catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
            if (biggestBoundary == -Double.MAX_VALUE && smallestBoundary == Double.MAX_VALUE){
                for (BoundaryInfo info : boundaryInfos) {
                    try {
                        double doubleValue = MathUtils.parseStringValue(info.value);
                        //大于最小值的最小边界值
                        if (doubleValue <= smallestBoundary && doubleValue >= smallestValue) {
                            smallestInfo = info;
                            smallestBoundary = doubleValue;
                            if (doubleValue == smallestValue && smallestInfo.value.equals(info.value)){
                                if (info.rightClose == 1){
                                    smallestInfo = info;
                                }
                            }
                            if (doubleValue > smallestValue && smallestInfo.value.equals(info.value)){
                                if (info.leftClose == 1){
                                    smallestInfo = info;
                                }
                            }
                        }
                        //小于最大值的最大边界值
                        if (doubleValue >= biggestBoundary && doubleValue <= biggestValue) {
                            biggestBoundary = doubleValue;
                            biggestInfo = info;
                            if (doubleValue == biggestValue && biggestInfo.value.equals(info.value)){
                                if (info.leftClose == 1){
                                    biggestInfo = info;
                                }
                            }
                            if (doubleValue < biggestValue && biggestInfo.value.equals(info.value)){
                                if (info.rightClose == 1){
                                    biggestInfo = info;
                                }
                            }
                        }
                    } catch (NumberFormatException e){
                        e.printStackTrace();
                    }
                }
                if (biggestBoundary == -Double.MAX_VALUE || smallestBoundary == Double.MAX_VALUE){
                    return null;
                }
            }
            //if (isSymmetricalList(valueList)){
            //    if (Math.abs(biggestBoundary)>Math.abs(smallestBoundary)){
            //        return Arrays.asList(String.valueOf(-biggestBoundary), String.valueOf(biggestBoundary));
            //    }
            //    else {
            //        return Arrays.asList(String.valueOf(smallestBoundary), String.valueOf(-smallestBoundary));
            //    }
            //}
            //else {
            String small = String.valueOf(smallestBoundary);
            String big = String.valueOf(biggestBoundary);
            if (biggestInfo != null && biggestInfo.rightClose == 1){
                big = big +']';
            }
            if (smallestInfo != null && smallestInfo.leftClose == 1){
                small = '[' + small;
            }

            return Arrays.asList(String.valueOf(small), String.valueOf(big));
            //}
        }
        //else {
            return valueList;
        //}
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
