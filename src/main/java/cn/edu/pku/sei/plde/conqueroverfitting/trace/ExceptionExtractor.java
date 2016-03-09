package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.SearchBoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class ExceptionExtractor {

    public static Map<VariableInfo, List<String>> extract(List<TraceResult> traceResults, List<VariableInfo> vars){
        Map<VariableInfo, List<String>> exceptionVariable = AbandanTrueValueFilter.filter(traceResults, vars);
        Map<VariableInfo, List<String>> trueVariable = AbandanTrueValueFilter.getTrueValue(traceResults, vars);
        Map<VariableInfo, List<String>> cleanedVariable = cleanVariables(exceptionVariable);
        exceptionVariable = SearchBoundaryFilter.filter(cleanedVariable, trueVariable);
        return exceptionVariable;
    }

    private static Map<VariableInfo, List<String>> cleanVariables(Map<VariableInfo, List<String>> exceptionVariable){
        Map<VariableInfo, List<String>> cleanedVariable = new HashMap<>();
        for (Map.Entry<VariableInfo, List<String>> var: exceptionVariable.entrySet()){
            List<String> unrepeatValue = new ArrayList(new HashSet(var.getValue()));
            if (var.getKey() == null){
                continue;
            }
            if (var.getKey().isSimpleType && var.getKey().variableSimpleType==null){
                continue;
            }
            if (!var.getKey().isSimpleType && var.getKey().otherType == null){
                continue;
            }
            //删掉疑似for循环的计数的数据
            if (isForLoopParam(unrepeatValue) || var.getValue().size()== 0){
                continue;
            }
            cleanedVariable.put(var.getKey(), unrepeatValue);
        }
        return cleanedVariable;
    }

    private static boolean isForLoopParam(List<String> vars){
        List<Integer> nums = new ArrayList<>();
        for (String var: vars){
            if (!StringUtils.isNumeric(var)){
                return false;
            }
            try {
                int num = Integer.parseInt(var);
                nums.add(num);
            }catch (Exception e){
                return false;
            }
        }
        if (nums.size() < 5){
            return false;
        }
        Collections.sort(nums);
        int first = nums.get(0);
        for (int i=1; i< nums.size();i++){
            int second = nums.get(i);
            if (Math.abs(first-second) != 1){
                return false;
            }
            first = second;
        }
        return true;
    }
}
