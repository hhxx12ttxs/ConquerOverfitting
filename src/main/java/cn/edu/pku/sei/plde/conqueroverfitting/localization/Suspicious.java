package cn.edu.pku.sei.plde.conqueroverfitting.localization;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.JavaLibrary;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.synth.TestClassesFinder;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.VariableTracer;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.MethodCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.VariableCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/25.
 */
public class Suspicious implements Serializable{
    private final String _classpath;
    private final String _testClasspath;
    public final String _classname;
    public final String _function;
    public final double _suspiciousness;
    public final List<String> _tests;
    public final List<String> _failTests;
    private final List<String> _lines;
    private List<VariableInfo> _variableInfo;
    private List<MethodInfo> _methodInfo;
    private int _lastLine = -1;

    public Suspicious(String classpath, String testClasspath, String classname, String function, double suspiciousness, List<String> tests,List<String> failTests, List<String> lines){
        _classpath = classpath;
        _testClasspath = testClasspath;
        _classname = classname;
        _function = function;
        _suspiciousness = suspiciousness;
        _tests = new ArrayList(new HashSet(tests));
        _failTests = new ArrayList(new HashSet(failTests));
        _lines = lines;

    }



    public int lastLine() {
        //如果是构造函数,则重新寻找错误行
        if (_classname.substring(_classname.lastIndexOf(".") + 1).equals(_function.substring(0, _function.indexOf('(')))) {
            for (String test : _tests) {
                try {
                    String testTrace = TestUtils.getTestTrace(_classpath, _testClasspath, test.split("#")[0], test.split("#")[1]);
                    for (String line : testTrace.split("\n")) {
                        if (line.contains(classname()) && line.contains("(") && line.contains(")")) {
                            if (_lastLine!= -1){
                                continue;
                            }
                            _lastLine = Integer.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(":")[1]);
                        }
                    }
                }catch(NotFoundException e){
                    continue;
                }
            }
        }
        if (_lastLine == -1) {
            for (String line : _lines) {
                if (_lastLine < Integer.valueOf(line)) {
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

    public String functionnameWithoutParam(){
        return _function.substring(0, _function.indexOf("("));
    }


    public List<String> getTestClassAndFunction(){
        return _tests;
    }

    public List<String> getFailedTest(){
        return _failTests;
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



    public List<VariableInfo> getAllInfo(){
        return InfoUtils.filterBannedVariable(InfoUtils.AddMethodInfoListToVariableInfoList(_variableInfo, _methodInfo));
    }

    public List<VariableInfo> getVariableInfo(String classSrc, int line){
        if (_variableInfo == null){
            _variableInfo = new ArrayList<>();
        }
        List<VariableInfo> variableInfos = new ArrayList<VariableInfo>();
        String classSrcPath = getClassSrcPath(classSrc);
        VariableCollect variableCollect = VariableCollect.GetInstance(getClassSrcIndex(classSrc));
        List<VariableInfo> parameters = variableCollect.getVisibleParametersInMethodList(classSrcPath, line);
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
                        VariableInfo newField = VariableInfo.copy(field);
                        if (!newField.isPublic && !paramClassSrcPath.equals(getClassSrcPath(classSrc))){
                            continue;
                        }
                        newField.isParameter = true;
                        newField.variableName = param.variableName+"."+newField.variableName;
                        variableInfos.add(newField);
                    }
                }
            }
        }
        List<VariableInfo> locals = variableCollect.getVisibleLocalInMethodList(classSrcPath, line);
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
        _variableInfo.removeAll(variableInfos);
        _variableInfo.addAll(variableInfos);
        _variableInfo = InfoUtils.filterBannedVariable(_variableInfo);
        return InfoUtils.filterBannedVariable(variableInfos);
    }


    public List<MethodInfo> getMethodInfo(String classSrc){
        if (_methodInfo != null){
            return _methodInfo;
        }
        MethodCollect methodCollect = MethodCollect.GetInstance(getClassSrcIndex(classSrc));
        LinkedHashMap<String, ArrayList<MethodInfo>> methods = methodCollect.getVisibleMethodWithoutParametersInAllClassMap(getClassSrcPath(classSrc));
        _methodInfo = methods.get(getClassSrcPath(classSrc));
        List<MethodInfo> staticMethod = new ArrayList<>();
        if (MethodCollect.checkIsStaticMethod(getClassSrcPath(classSrc),_function.substring(0, _function.indexOf("(")))){
            for (MethodInfo info: _methodInfo){
                if (!info.isStatic){
                    staticMethod.add(info);
                }
            }
        }
        _methodInfo.removeAll(staticMethod);
        return _methodInfo;
    }

    public List<TraceResult> getTraceResult(String classpath, String testClasspath, String classSrc, String testClassSrc) throws IOException{
        VariableTracer tracer = new VariableTracer(classpath, testClasspath, classSrc, testClassSrc, this);
        List<TraceResult> traceResults = new ArrayList<TraceResult>();
        if (_tests.size() > 10){
            for (String testclass: _failTests){
                traceResults.addAll(tracer.trace(classname(), functionname(), testclass.split("#")[0], testclass.split("#")[1], lastLine()));
            }
        }
        else {
            for (String testclass: _tests){
                if (!_failTests.contains(testclass) && !testFilter(testClassSrc, testclass.split("#")[0], testclass.split("#")[1])){
                    continue;
                }
                traceResults.addAll(tracer.trace(classname(), functionname(), testclass.split("#")[0], testclass.split("#")[1], lastLine()));
            }
        }
        return traceResults;
    }

    private boolean testFilter(String testSrcPath, String testClassname, String testMethodName){
        List<String> assertLines = CodeUtils.getAssertInTest(testSrcPath, testClassname, testMethodName);
        if (assertLines.size() != 1){
            return true;
        }
        String assertLine = assertLines.get(0);
        if (!assertLine.trim().startsWith("Assert.assertEquals(") && !assertLine.trim().startsWith("TestUtils.assertEquals(") ){
            return true;
        }
        List<String> params = CodeUtils.divideParameter(assertLine,1);
        String param1 = params.get(0);
        String param2 = params.get(1);
        if (param1.contains(".") && param1.contains("(") && param2.contains(".") && param2.contains("(")){
            if (param1.substring(param1.indexOf("."), param1.indexOf("(")).equals(param2.substring(param2.indexOf("."), param2.indexOf("(")))){
                return false;
            }
        }
        return true;
    }

    /*
    private boolean isSwitch(){
        if (_lines.size()<3){
            return false;
        }
        int lineNum = Integer.valueOf(_lines.get(0));
        for (int i=1; i< _lines.size(); i++){
            if (Math.abs(Integer.valueOf(_lines.get(i))-lineNum) != 2){
                return false;
            }
            lineNum = Integer.valueOf(_lines.get(i));
        }
        return true;
    }

    /*
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
    */
}
