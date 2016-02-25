package cn.edu.pku.sei.plde.conqueroverfitting.localization;

import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.VariableTracer;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.MethodCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.VariableCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/25.
 */
public class Suspicious implements Serializable{
    public final String _classname;
    public final String _function;
    public final double _suspiciousness;
    public final List<String> _tests;
    public final List<String> _lines;
    private List<VariableInfo> _variableInfo;
    private List<MethodInfo> _methodInfo;
    public int _lastLine = -1;


    public Suspicious(String classname, String function, double suspiciousness, List<String> tests, List<String> lines){
        _classname = classname;
        _function = function;
        _suspiciousness = suspiciousness;
        _tests = tests;
        _lines = lines;

    }

    public int lastLine(){
        if (_lastLine == -1){
            for (String line: _lines){
                if (_lastLine < Integer.valueOf(line)){
                    _lastLine = Integer.valueOf(line);
                }
            }
        }
        return _lastLine;
    }

    public String classname(){
        return _classname;
    }

    public List<String> getTestClassAndFunction(){
        return _tests;
    }

    public List<String> getTestClasses(){
        List<String> classes = new ArrayList<String>();
        for (String test: _tests){
            if (!test.contains("#")){
                continue;
            }
            classes.add(test.split("#")[0]);
        }
        return classes;
    }

    public String getClassSrcPath(String classSrc){
        return classSrc + System.getProperty("file.separator") + _classname.replace(".",System.getProperty("file.separator")) + ".java";
    }

    public String getClassSrcIndex(String classSrc){
        String classSrcPath = getClassSrcPath(classSrc);
        return classSrcPath.substring(0,classSrcPath.lastIndexOf(System.getProperty("file.separator")));
    }



    public List<VariableInfo> getAllInfo(String classSrc){
        return InfoUtils.AddMethodInfoListToVariableInfoList(getVariableInfo(classSrc), getMethodInfo(classSrc));
    }

    public List<VariableInfo> getVariableInfo(String classSrc){
        if (_variableInfo != null){
            return _variableInfo;
        }
        String classSrcPath = getClassSrcPath(classSrc);
        VariableCollect variableCollect = VariableCollect.GetInstance(getClassSrcIndex(classSrc));
        List<VariableInfo> parameters = variableCollect.getVisibleParametersInMethodList(classSrcPath, lastLine()-1);
        List<VariableInfo> locals = variableCollect.getVisibleLocalInMethodList(classSrcPath, lastLine()-1);
        LinkedHashMap<String, ArrayList<VariableInfo>> classvars = variableCollect.getVisibleFieldInAllClassMap(classSrcPath);
        List<VariableInfo> variableInfos = new ArrayList<VariableInfo>();
        variableInfos.addAll(parameters);
        variableInfos.addAll(locals);
        variableInfos.addAll(classvars.get(classSrcPath));
        _variableInfo = variableInfos;
        return _variableInfo;
    }


    public List<MethodInfo> getMethodInfo(String classSrc){
        if (_methodInfo != null){
            return _methodInfo;
        }
        MethodCollect methodCollect = MethodCollect.GetInstance(getClassSrcIndex(classSrc));
        LinkedHashMap<String, ArrayList<MethodInfo>> methods = methodCollect.getVisibleMethodInAllClassMap(getClassSrcPath(classSrc));
        _methodInfo = methods.get(getClassSrcPath(classSrc));
        return _methodInfo;
    }

    public List<TraceResult> getTraceResult(String classpath, String testClasspath, String classSrc) throws IOException{
        VariableTracer tracer = new VariableTracer(classpath, testClasspath, classSrc);
        List<TraceResult> traceResults = new ArrayList<TraceResult>();
        for (String testclass: getTestClasses()){
            traceResults.addAll(tracer.trace(classname(), testclass, lastLine(), getVariableInfo(classSrc), getMethodInfo(classSrc)));
        }
        return traceResults;
    }
}
