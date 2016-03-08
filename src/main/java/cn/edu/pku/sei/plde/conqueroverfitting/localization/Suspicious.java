package cn.edu.pku.sei.plde.conqueroverfitting.localization;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.JavaLibrary;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.synth.TestClassesFinder;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.VariableTracer;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.MethodCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.VariableCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.io.*;
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
        return _classname.trim().replace(";","");
    }

    public String functionname(){
        if (_function.contains("[")){
            return _function.substring(0, _function.lastIndexOf("["));
        }
        return _function.replace("\\","");
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
        String classname = classname();
        if (_classname.contains("$")){
            classname = _classname.substring(0, _classname.lastIndexOf('$'));
        }
        String result =  classSrc + System.getProperty("file.separator") + classname.replace(".",System.getProperty("file.separator")) + ".java";
        return result.replace(" ","");
    }

    public String getClassSrcIndex(String classSrc){
        String classSrcPath = getClassSrcPath(classSrc);
        return classSrcPath.substring(0,classSrcPath.lastIndexOf(System.getProperty("file.separator"))).replace(" ","");
    }



    public List<VariableInfo> getAllInfo(String classSrc){
        return InfoUtils.filterBannedVariable(InfoUtils.AddMethodInfoListToVariableInfoList(getVariableInfo(classSrc), getMethodInfo(classSrc)));
    }

    public List<VariableInfo> getVariableInfo(String classSrc){
        if (_variableInfo != null){
            return _variableInfo;
        }
        List<VariableInfo> variableInfos = new ArrayList<VariableInfo>();
        String classSrcPath = getClassSrcPath(classSrc);
        VariableCollect variableCollect = VariableCollect.GetInstance(getClassSrcIndex(classSrc));
        List<VariableInfo> parameters = variableCollect.getVisibleParametersInMethodList(classSrcPath, lastLine());
        variableInfos.addAll(parameters);
        for (VariableInfo param: parameters){
            param.isParameter = true;
            if (TypeUtils.isComplexType(param.getStringType())){
                String paramClass = CodeUtils.getClassNameOfVariable(param, classSrcPath);
                if (paramClass.equals("")){
                    continue;
                }
                String paramClassSrcPath =  classSrc + System.getProperty("file.separator") + paramClass.replace(".",System.getProperty("file.separator")) + ".java";
                LinkedHashMap<String, ArrayList<VariableInfo>> classvars = variableCollect.getVisibleFieldInAllClassMap(paramClassSrcPath);
                if (classvars.containsKey(paramClassSrcPath)) {
                    List<VariableInfo> fields = classvars.get(classSrcPath);
                    for (VariableInfo field: fields){
                        if (!field.isPublic && !paramClassSrcPath.equals(getClassSrcPath(classSrc))){
                            continue;
                        }
                        field.isParameter = true;
                        field.variableName = param.variableName+"."+field.variableName;
                    }
                    variableInfos.addAll(fields);
                }
            }
        }
        List<VariableInfo> locals = variableCollect.getVisibleLocalInMethodList(classSrcPath, lastLine());
        for (VariableInfo local: locals){
            local.isLocalVariable = true;
        }
        variableInfos.addAll(locals);

        LinkedHashMap<String, ArrayList<VariableInfo>> classvars = variableCollect.getVisibleFieldInAllClassMap(classSrcPath);
        if (classvars.containsKey(classSrcPath)){
            List<VariableInfo> fields = classvars.get(classSrcPath);
            for (VariableInfo field: fields){
                field.isFieldVariable = true;
            }
            variableInfos.addAll(fields);
        }

        List<VariableInfo> addon = new ArrayList<>();
        for (VariableInfo info: variableInfos){
            if (TypeUtils.isComplexType(info.getStringType()) && info.isParameter){
                //VariableInfo newInfo = new VariableInfo(info.variableName+".isNaN",TypeEnum.BOOLEAN,true,null);
                //if (info.isParameter){
                //    newInfo.isParameter = true;
                //}
                //addon.add(newInfo);
            }
        }
        variableInfos.addAll(addon);
        _variableInfo = variableInfos;
        return InfoUtils.filterBannedVariable(_variableInfo);
    }


    public List<MethodInfo> getMethodInfo(String classSrc){
        if (_methodInfo != null){
            return _methodInfo;
        }
        MethodCollect methodCollect = MethodCollect.GetInstance(getClassSrcIndex(classSrc));
        LinkedHashMap<String, ArrayList<MethodInfo>> methods = methodCollect.getVisibleMethodWithoutParametersInAllClassMap(getClassSrcPath(classSrc));
        _methodInfo = methods.get(getClassSrcPath(classSrc));
        if (MethodCollect.checkIsStaticMethod(getClassSrcPath(classSrc),_function.substring(0, _function.indexOf("(")))){
            for (MethodInfo info: _methodInfo){
                if (!info.isStatic){
                    _methodInfo.remove(info);
                }
            }
        }
        return _methodInfo;
    }

    public List<TraceResult> getTraceResult(String classpath, String testClasspath, String classSrc) throws IOException{
        VariableTracer tracer = new VariableTracer(classpath, testClasspath, classSrc);
        List<TraceResult> traceResults = new ArrayList<TraceResult>();
        for (String testclass: getTestClasses()){
            traceResults.addAll(tracer.trace(classname(), functionname(), testclass, lastLine(), getVariableInfo(classSrc), getMethodInfo(classSrc)));
        }
        return traceResults;
    }

    public List<TraceResult> getTraceResultWithAllTest(String classpath, String testClasspath, String classSrc) throws IOException{
        File traceFile = new File(System.getProperty("user.dir")+"/traceresult/"+ FileUtils.getMD5(classpath+testClasspath+classname()+functionname()+lastLine())+".sps");
        if (traceFile.exists()){
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(traceFile));
                List<TraceResult> result = (List<TraceResult>) objectInputStream.readObject();
                return result;
            }catch (Exception e){
                System.out.println("Reloading Localization Result...");
            }
        }
        VariableTracer tracer = new VariableTracer(classpath, testClasspath, classSrc);
        List<TraceResult> traceResults = new ArrayList<TraceResult>();
        String[] testClasses = new TestClassesFinder().findIn(JavaLibrary.classpathFrom(testClasspath), false);
        for (String testclass: testClasses){
            traceResults.addAll(tracer.trace(classname(), functionname(), testclass, lastLine(), getVariableInfo(classSrc), getMethodInfo(classSrc)));
        }
        try {
            boolean createResult = traceFile.createNewFile();
            if (!createResult){
                System.out.println("File Create Error");
            }
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(traceFile));
            objectOutputStream.writeObject(traceResults);
            objectOutputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return traceResults;
    }
}
