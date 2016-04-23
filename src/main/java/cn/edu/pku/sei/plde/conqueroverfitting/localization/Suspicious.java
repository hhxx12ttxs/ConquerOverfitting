package cn.edu.pku.sei.plde.conqueroverfitting.localization;

import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.JavaLibrary;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.synth.TestClassesFinder;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
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
    public final String _classpath;
    public List<String> _libPath = new ArrayList<>();
    public final String _testClasspath;
    public final String _classname;
    public final String _function;
    public final String _srcPath;
    public final String _testSrcPath;
    public final boolean _isConstructor;
    public final double _suspiciousness;
    public final List<String> _tests;
    public final List<String> _failTests;
    public final List<String> _lines;
    private List<VariableInfo> _variableInfo;
    private List<MethodInfo> _methodInfo;
    public Map<String, Asserts> _assertsMap = new HashMap<>();
    public Map<String, List<Integer>> _errorLineMap = new HashMap<>();
    private int _defaultErrorLine = -1;
    public int trueMethodCallNumFromTest = 0;
    public Suspicious(String classpath,
                      String testClasspath,
                      String srcPath,
                      String testSrcPath,
                      String classname,
                      String function,
                      double suspiciousness,
                      List<String> tests,
                      List<String> failTests,
                      List<String> lines) {
        this(classpath, testClasspath,srcPath, testSrcPath, classname, function, suspiciousness, tests, failTests, lines, new ArrayList<String>());
    }

    public Suspicious(String classpath,
                      String testClasspath,
                      String srcPath,
                      String testSrcPath,
                      String classname,
                      String function,
                      double suspiciousness,
                      List<String> tests,
                      List<String> failTests,
                      List<String> lines,
                      List<String> libPaths){
        _classpath = classpath;
        _testClasspath = testClasspath;
        _classname = classname;
        _srcPath = srcPath;
        _testSrcPath = testSrcPath;
        _libPath = libPaths;
        _function = function;
        _suspiciousness = suspiciousness;
        _tests = new ArrayList(new HashSet(tests));
        _failTests = new ArrayList(new HashSet(failTests));
        _lines = lines;
        _isConstructor = CodeUtils.isConstructor(_classname, _function);
    }

    public int errorAssertNums(){
        int errAssertNum = 0;
        for (Asserts asserts: _assertsMap.values()){
            errAssertNum += asserts.errorAssertNum();
        }
        return errAssertNum;
    }

    public int trueAssertNums(){
        return trueMethodCallNumFromTest;
    }

    public int trueTestNums(){
        return _tests.size() - _failTests.size();
    }

    public Set<Integer> errorLines(){
        if (_assertsMap.size() == 0){
            return new HashSet<>();
        }
        Set<Integer> result = new HashSet<>();
        for (Map.Entry<String, List<Integer>> entry: _errorLineMap.entrySet()){
            result.addAll(entry.getValue());
        }
        return result;
    }



    public int getDefaultErrorLine() {
        //如果是构造函数,则重新寻找错误行
        if (_classname.substring(_classname.lastIndexOf(".") + 1).equals(_function.substring(0, _function.indexOf('(')))) {
            for (String test : _tests) {
                try {
                    String testTrace = TestUtils.getTestTrace(_classpath, _testClasspath, test.split("#")[0], test.split("#")[1]);
                    for (String line : testTrace.split("\n")) {
                        if (line.contains(classname()+".") && line.contains("(") && line.contains(")")) {
                            if (_defaultErrorLine!= -1){
                                continue;
                            }
                            _defaultErrorLine = Integer.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf(")")).split(":")[1]);
                        }
                    }
                }catch(NotFoundException e){
                    continue;
                }
            }
        }
        if (_defaultErrorLine == -1) {
            for (String line : _lines) {
                if (_defaultErrorLine < Integer.valueOf(line)) {
                    _defaultErrorLine = Integer.valueOf(line);
                }
            }
        }

        return _defaultErrorLine;
    }

    public String classname(){
        String className =  _classname.trim().replace(";","");
        if (className.contains("$")){
            className = className.substring(0, className.indexOf("$"));
        }
        return className;
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
        return result.replace(" ","").replace("//","/");
    }


    public String getClassSrcIndex(String classSrc){
        String classSrcPath = getClassSrcPath(classSrc);
        return classSrcPath.substring(0,classSrcPath.lastIndexOf(System.getProperty("file.separator"))).replace(" ","").replace("//","/");
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
        List<VariableInfo> locals = variableCollect.getVisibleLocalInMethodList(classSrcPath, line);
        if (locals.size() == 0){
            locals = variableCollect.getVisibleLocalInMethodList(classSrcPath, line-1);
        }
        for (VariableInfo local: locals){
            local.isLocalVariable = true;
            List<VariableInfo> subVariableInfo = InfoUtils.getSubInfoOfComplexVariable(local,classSrc, classSrcPath);
            for (VariableInfo info: subVariableInfo){
                info.isLocalVariable = true;
            }
            variableInfos.addAll(subVariableInfo);

        }
        variableInfos.addAll(locals);
        variableCollect = VariableCollect.GetInstance(getClassSrcIndex(classSrc));
        List<VariableInfo> parameters = variableCollect.getVisibleParametersInMethodList(classSrcPath, line);
        int simpleVariableCount = 0;
        for (VariableInfo param: parameters){
            if (param.isSimpleType){
                simpleVariableCount ++;
            }
        }
        for (VariableInfo param: parameters){
            param.isParameter = true;
            if (simpleVariableCount == 1 && locals.size() == 0 && MathUtils.isNumberType(param.getStringType())){
                param.priority = 2;
            }
            List<VariableInfo> subVariableInfo = InfoUtils.getSubInfoOfComplexVariable(param,classSrc, classSrcPath);
            for (VariableInfo info: subVariableInfo){
                info.isParameter = true;
            }
            variableInfos.addAll(subVariableInfo);
        }
        variableInfos.addAll(parameters);

        if (!_isConstructor){
            variableCollect = VariableCollect.GetInstance(getClassSrcIndex(classSrc));
            LinkedHashMap<String, ArrayList<VariableInfo>> classvars = variableCollect.getVisibleFieldInAllClassMap(classSrcPath);
            if (classvars.containsKey(classSrcPath)){
                List<VariableInfo> fields = classvars.get(classSrcPath);
                //不能在静态函数中追踪非静态变量
                List<VariableInfo> staticVars = new ArrayList<>();
                if (MethodCollect.checkIsStaticMethod(getClassSrcPath(classSrc),_function.substring(0, _function.indexOf("(")))){
                    for (VariableInfo info: fields){
                        if (!info.isStatic){
                            staticVars.add(info);
                        }
                    }
                }
                String code = FileUtils.getCodeFromFile(_srcPath, _classname);
                List<VariableInfo> innerVars = new ArrayList<>();
                if (!MethodUtils.isInnerMethod(code,functionnameWithoutParam())){
                    for (VariableInfo info: fields){
                        if (!CodeUtils.hasField(code, info.variableName)){
                            innerVars.add(info);
                        }
                    }
                }
                fields.removeAll(staticVars);
                fields.removeAll(innerVars);
                for (VariableInfo field: fields){
                    field.isFieldVariable = true;
                }
                variableInfos.addAll(fields);
            }
        }
        variableInfos.addAll(addonVariableInfos(line, parameters));
        _variableInfo.removeAll(variableInfos);
        _variableInfo.addAll(variableInfos);
        _variableInfo = InfoUtils.filterBannedVariable(_variableInfo);
        return InfoUtils.filterBannedVariable(variableInfos);
    }

    private List<VariableInfo> addonVariableInfos(int line, List<VariableInfo> parameters){
        List<VariableInfo> infos = new ArrayList<>();
        String code = FileUtils.getCodeFromFile(_srcPath, _classname);
        //this变量
        VariableInfo thisInfo = new VariableInfo("this", null, false, classname().substring(classname().lastIndexOf(".")+1));
        thisInfo.isAddon = true;
        infos.add(thisInfo);
        //返回的变量
        VariableInfo returnInfo = new VariableInfo("return", null, false, "returnType");
        returnInfo.isAddon = true;
        infos.add(returnInfo);
        //for循环的变量
        String lineString = CodeUtils.getLineFromCode(code, line-1);
        List<String> parameterNames = new ArrayList<>();
        for (VariableInfo info: parameters){
            if (TypeUtils.isSimpleArray(info.getStringType())){
                parameterNames.add(info.variableName);
            }
        }
        if (LineUtils.isParameterTraversalForLoop(lineString, parameterNames)){
            for (VariableInfo info: parameters){
                if (!TypeUtils.isSimpleArray(info.getStringType())){
                    continue;
                }
                VariableInfo traversalInfo = new VariableInfo(
                        info.variableName+"[i]",
                        TypeUtils.getTypeEnumOfSimpleType(info.getStringType()),
                        true,
                        null
                        );
                traversalInfo.isParameter = true;
                infos.add(traversalInfo);

            }
        }
        String methodCode = CodeUtils.getMethodString(code, functionnameWithoutParam(),line);
        String methodCodeBeforeLine = CodeUtils.getMethodBodyBeforeLine(code, functionnameWithoutParam(),line)+CodeUtils.getLineFromCode(code,line);
        Set<String> expresses = ExpressionUtils.getExpressionsInMethod(methodCode);
        for (String express: expresses){
            if (methodCodeBeforeLine.replace(" ","").contains(express.replace(" ",""))){
                if (express.contains("==") || express.contains(">") || express.contains("<")){
                    VariableInfo info = new VariableInfo(express,TypeEnum.BOOLEAN,true,null);
                    info.expressMethod = functionnameWithoutParam();
                    info.isExpression = true;
                    infos.add(info);
                }
                else {
                    VariableInfo info = new VariableInfo(express,TypeEnum.DOUBLE,true,null);
                    info.expressMethod = functionnameWithoutParam();
                    info.isExpression = true;
                    infos.add(info);
                }
            }
        }
        return infos;
    }


    public List<MethodInfo> getMethodInfo(String classSrc){
        if (_methodInfo != null){
            return _methodInfo;
        }
        if (_isConstructor){
            _methodInfo = new ArrayList<>();
            return _methodInfo;
        }
        MethodCollect methodCollect = MethodCollect.GetInstance(getClassSrcIndex(classSrc));
        LinkedHashMap<String, ArrayList<MethodInfo>> methods = methodCollect.getVisibleMethodWithoutParametersInAllClassMap(getClassSrcPath(classSrc));
        _methodInfo = methods.get(getClassSrcPath(classSrc));
        if (_methodInfo == null){
            _methodInfo = new ArrayList<>();
        }
        //remove static methods when the suspicious method is not static
        List<MethodInfo> staticMethod = new ArrayList<>();
        if (MethodCollect.checkIsStaticMethod(getClassSrcPath(classSrc),_function.substring(0, _function.indexOf("(")))){
            for (MethodInfo info: _methodInfo){
                if (!info.isStatic){
                    staticMethod.add(info);
                }
            }
        }

        //remove inner class methods
        List<MethodInfo> innerMethod = new ArrayList<>();
        String code = FileUtils.getCodeFromFile(_srcPath, classname());
        for (MethodInfo info: _methodInfo){
            if (MethodUtils.isInnerMethod(code, info.methodName)){
                innerMethod.add(info);
            }
        }

        //remove methods would call suspicious method in its statement.
        List<MethodInfo> loopCallMethod = new ArrayList<>();
        for (MethodInfo info: _methodInfo){
            if (MethodUtils.isLoopCall(functionnameWithoutParam(),info.methodName,code)){
                loopCallMethod.add(info);
            }
        }
        _methodInfo.removeAll(staticMethod);
        _methodInfo.removeAll(innerMethod);
        _methodInfo.removeAll(loopCallMethod);
        _methodInfo = InfoUtils.filterBannedMethod(_methodInfo);
        return _methodInfo;
    }

    public List<TraceResult> getTraceResult() {
        VariableTracer tracer = new VariableTracer(_srcPath, _testSrcPath, this);
        List<TraceResult> traceResults = new ArrayList<TraceResult>();
        List<String> trueTests = new ArrayList<>(_tests);
        trueTests.removeAll(_failTests);
        int tracedTestCount = 0;
        for (String testclass: trueTests){
            if (!_failTests.contains(testclass) && !testFilter(_testSrcPath, testclass.split("#")[0], testclass.split("#")[1])){
                continue;
            }
            try{
                traceResults.addAll(tracer.trace(classname(), functionname(), testclass.split("#")[0], testclass.split("#")[1], getDefaultErrorLine(), true));
            } catch (IOException e){
                e.printStackTrace();
            }
            tracedTestCount ++;
            if (traceResults.size()> 50 || tracedTestCount > 10){
                break;
            }
        }
        for (String testclass: _failTests){
            try{
                traceResults.addAll(tracer.trace(classname(), functionname(), testclass.split("#")[0], testclass.split("#")[1], getDefaultErrorLine(), false));
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return traceResults;
    }

    private boolean testFilter(String testSrcPath, String testClassname, String testMethodName){
        String code = FileUtils.getCodeFromFile(testSrcPath, testClassname);
        String methodCode = FileUtils.getTestFunctionCodeFromCode(code, testMethodName,testSrcPath);
        if (methodCode.equals("")){
            return false;
        }
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

}
