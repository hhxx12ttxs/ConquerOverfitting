package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.container.various.Table;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
import cn.edu.pku.sei.plde.conqueroverfitting.sort.VariableSort;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.SearchBoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.KerberosCredentials;

import java.io.File;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class ExceptionExtractor {
    private Suspicious suspicious;
    private List<TraceResult> traceResults;
    private List<ExceptionVariable> exceptionVariables;
    public ExceptionExtractor(Suspicious suspicious){
        this.suspicious = suspicious;
    }

    public List<ExceptionVariable> extract(List<TraceResult> traceResults){
        this.traceResults = traceResults;
        if (exceptionVariables == null){
            exceptionVariables = AbandanTrueValueFilter.abandon(traceResults, suspicious.getAllInfo());
        }
        return exceptionVariables;
    }

    public List<List<ExceptionVariable>> sort(){
        if (hasTrueTraceResult(traceResults)){
            return sortWithMethodOne(exceptionVariables, traceResults, suspicious);
        }
        else {
            return sortWithMethodTwo(exceptionVariables, traceResults, suspicious);
        }
    }

    private static boolean hasTrueTraceResult(List<TraceResult> traceResults){
        for (TraceResult traceResult: traceResults){
            if (traceResult.getTestResult()){
                return true;
            }
        }
        return false;
    }

    private static List<List<ExceptionVariable>> sortWithMethodTwo(List<ExceptionVariable> exceptionVariables, List<TraceResult> traceResults, Suspicious suspicious){
        ExceptionSorter sorter = new ExceptionSorter(suspicious);
        return sorter.sort(exceptionVariables);
    }



    private static List<List<ExceptionVariable>> sortWithMethodOne(List<ExceptionVariable> exceptionVariables,List<TraceResult> traceResults, Suspicious suspicious){
        String code = FileUtils.getCodeFromFile(suspicious._srcPath, suspicious.classname());
        String statement = CodeUtils.getMethodBodyBeforeLine(code, suspicious.functionnameWithoutParam(), lastLineOfTraceResults(traceResults));
        Set<String> variables = new HashSet<>();
        for (ExceptionVariable variable: exceptionVariables){
            if (variable.name.endsWith(".null") || variable.name.endsWith(".Comparable")){
                variables.add(variable.name.substring(0, variable.name.lastIndexOf(".")));
            }
            else {
                variables.add(variable.name);
            }
        }
        VariableSort variableSort = new VariableSort(variables, statement);
        List<List<String>> sortedVariable = variableSort.getSortVariable();
        return variableConverse(sortedVariable, exceptionVariables);
    }

    private static List<List<ExceptionVariable>> variableConverse(List<List<String>> sortedVariable, List<ExceptionVariable> exceptionVariables){
        List<List<ExceptionVariable>> result = new ArrayList<>();
        for (List<String> echelon: sortedVariable){
            List<ExceptionVariable> variableEchelon = getExceptionVariableWithName(echelon, exceptionVariables);
            if (variableEchelon.size() != 0){
                result.add(variableEchelon);
            }
        }
        return result;
    }
    private static List<ExceptionVariable> getExceptionVariableWithName(List<String> variableNames, List<ExceptionVariable> exceptionVariables){
        List<ExceptionVariable> result = new ArrayList<>();
        for (String variableName: variableNames){
            result.addAll(getExceptionVariableWithName(variableName, exceptionVariables));
        }
        return result;
    }

    private static List<ExceptionVariable> getExceptionVariableWithName(String variableName, List<ExceptionVariable> exceptionVariables){
        List<ExceptionVariable> result = new ArrayList<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            if (exceptionVariable.name.equals(variableName) || exceptionVariable.name.contains(variableName+".")){
                result.add(exceptionVariable);
            }
        }
        return result;
    }

    private static int lastLineOfTraceResults(List<TraceResult> traceResults){
        int lastLine = 0;
        for (TraceResult traceResult: traceResults){
            if (traceResult._traceLine > lastLine){
                lastLine = traceResult._traceLine;
            }
        }
        return lastLine;
    }

}
