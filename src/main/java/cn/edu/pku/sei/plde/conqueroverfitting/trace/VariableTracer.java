package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.Utils;
import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.slice.StaticSlice;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;

import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import javassist.NotFoundException;
import org.apache.commons.beanutils.converters.IntegerArrayConverter;
import org.apache.commons.lang.StringUtils;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/19.
 */

public class VariableTracer {
    private final String _classpath;
    private final String _testClasspath;
    private final String _srcPath;
    private final Suspicious _suspicious;
    private String _testClassname;
    private String _classname;
    private String _functionname;
    private String _testMethodName;
    private String _testSrcPath;
    private Asserts _asserts;
    private String _shellResult;
    /**
     *
     * @param srcPath the path of source file
     */
    public VariableTracer(String srcPath, String testSrcPath, Suspicious suspicious){
        _classpath = suspicious._classpath;
        _testClasspath = suspicious._testClasspath;
        _srcPath = srcPath;
        _testSrcPath = testSrcPath;
        _suspicious = suspicious;
    }


    /**
     *
     * @param classname the tracing class name
     * @param testClassname the entry to tracing
     * @param errorLine the line number to be traced
     * @return the list of trace result
     * @throws IOException
     */
    public List<TraceResult> trace(String classname,String functionname, String testClassname, String testMethodName,int errorLine, boolean isSuccess)throws IOException{
        _classname = classname;
        _testClassname = testClassname;
        _testMethodName = testMethodName;
        _functionname = functionname;
        _asserts = new Asserts(_classpath,_srcPath,_testClasspath,_testSrcPath,testClassname,testMethodName, _suspicious._libPath);
        List<MethodInfo> methodInfos = _suspicious.getMethodInfo(_srcPath);
        ErrorLineTracer tracer = new ErrorLineTracer(_asserts, _classname, _functionname);
        Set<Integer> errorLines = tracer.trace(errorLine, isSuccess);
        List<TraceResult> results = new ArrayList<>();

        for (int line: errorLines){
            List<VariableInfo> variableInfos = _suspicious.getVariableInfo(_srcPath, line);
            variableInfos.removeAll(getBannedVariables(line));
            if (variableInfos.size() == 0 &&  methodInfos.size() == 0){
                continue;
            }
            Map<String, Integer> commentedTestClasses = tracer._commentedTestClass;
            if (commentedTestClasses.size() < 1){
                commentedTestClasses.put("", line);
            }
            if (_asserts.trueAssertNum() > 0 && _asserts.errorAssertNum() > 0 && !_asserts.getTrueTestFile().equals("")){
                commentedTestClasses.put(_asserts.getTrueTestFile(), -1);
            }
            for (Map.Entry<String, Integer> commentedTestClass: commentedTestClasses.entrySet()) {
                _shellResult = traceShell(_testClassname, _classname, functionname(), commentedTestClass.getKey(), variableInfos, methodInfos, line);

                if (_shellResult.contains(">>") && _shellResult.contains("<<")) {
                    String traceResult = analysisShellResult(_shellResult);
                    results.addAll(traceAnalysis(traceResult, commentedTestClass.getValue(), line));
                } else {
                    printErrorShell(_shellResult);
                }
            }
        }
        _suspicious._assertsMap.put(_testClassname+"#"+_testMethodName, _asserts);
        _suspicious._errorLineMap.put(_testClassname+"#"+_testMethodName, new ArrayList<>(errorLines));
        results.addAll(getAddonResult(_suspicious.getAllInfo()));
        deleteTempFile();
        return results;
    }

    private int methodCallNum(String shellResult){
        return shellResult.split("\\|into_method\\|").length-1;
    }

    private List<TraceResult> getAddonResult(List<VariableInfo> variableInfos){
        List<TraceResult> results = new ArrayList<>();
        if (_asserts._asserts.size() == 0){
            return results;
        }
        String firstAssert = _asserts._asserts.get(0);
        String classCode = FileUtils.getCodeFromFile(_srcPath, _classname);
        String methodCode = CodeUtils.getMethodBody(classCode, _suspicious.functionnameWithoutParam());
        if (methodCode.equals("")){
            return results;
        }
        String firstStatement = methodCode.substring(0,methodCode.length()).split("\n")[0];

        if (_asserts._asserts.size() == 1 && firstAssert.contains("Equals")){
            List<String> params = CodeUtils.divideParameter(firstAssert, 1, false);
            if (params.size() == 2){
                String param1 = params.get(0).contains(_functionname)?params.get(0):params.get(1);
                String param2 = params.get(0).contains(_functionname)?params.get(1):params.get(0);
                String className = _classname.substring(_classname.lastIndexOf(".")+1);
                if (param1.startsWith(className+".") && param2.startsWith(className+".") && param1.contains("()") && CodeUtils.countChar(param1,'.') >1){
                    TraceResult traceResult = new TraceResult(false);
                    traceResult._assertLine = _asserts._errorAssertLines.get(0);
                    traceResult._testClass = _testClassname;
                    traceResult._testMethod = _testMethodName;
                    traceResult.put("this", param1.substring(param1.indexOf(".")+1,param1.lastIndexOf(".")));
                    for (VariableInfo info: variableInfos){
                        if (info.variableName.equals("this")){
                            info.priority = 0;
                        }
                    }
                    results.add(traceResult);
                }
            }
        }
        if (_asserts._asserts.size() == 1 && firstAssert.contains("True") && firstAssert.contains(">=")){
            if (firstStatement.startsWith("return")){
                String returnString = methodCode.substring(firstStatement.indexOf(" ")+1, firstStatement.length()-1);
                String param = firstAssert.substring(firstAssert.indexOf('(')+1, firstAssert.lastIndexOf(')'));
                String numParam = param.split(">=")[0].contains(_functionname)?param.split(">=")[1]:param.split(">=")[0];
                if (numParam.trim().matches("^(-?\\d+)(\\.\\d+)?$")){
                    TraceResult traceResult = new TraceResult(false);
                    traceResult._assertLine = _asserts._errorAssertLines.get(0);
                    traceResult._testClass = _testClassname;
                    traceResult._testMethod = _testMethodName;
                    traceResult.put("return", "("+returnString+")" + "<" + numParam.trim());
                    results.add(traceResult);
                    for (VariableInfo info: variableInfos){
                        if (info.variableName.equals("return")){
                            info.priority = 0;
                        }
                    }
                }
            }
        }
        if (firstStatement.equals("return true;") || firstStatement.equals("return false;")){
            TraceResult traceResult = new TraceResult(false);
            traceResult._assertLine = -1;
            traceResult._testClass = _testClassname;
            traceResult._testMethod = _testMethodName;
            traceResult.put("return", "true");
            results.add(traceResult);
            for (VariableInfo info: variableInfos){
                if (info.variableName.equals("return")){
                    info.priority = 0;
                }
            }
        }
        return results;
    }

    private void deleteTempFile(){
        //clean temp file
        File tempPackage = new File(System.getProperty("user.dir")+"/temp/");
        for (String file: tempPackage.list()){
            File tempFile = new File(System.getProperty("user.dir")+"/temp/"+file);
            if (tempFile.exists()){
                if (tempFile.isDirectory()){
                    FileUtils.deleteDir(tempFile);
                }
                else {
                    tempFile.deleteOnExit();
                }
            }
        }
    }

    private void printErrorShell(String shellResult){
        System.out.println("Trace Fial in Output:");
        System.out.println(shellResult);
    }

    private List<VariableInfo> getBannedVariables(int errorLine){
        List<VariableInfo> bannedVariables = new ArrayList<>();
        if (_asserts._asserts.size() == 0){
            return bannedVariables;
        }
        String assertLine = _asserts._asserts.get(0);
        String code = FileUtils.getCodeFromFile(_srcPath,_classname);
        int methodParam = CodeUtils.getMethodParams(assertLine, functionname()).size();
        Map<List<String>, List<Integer>> methodLine = CodeUtils.getMethodLine(code,functionname());
        for (Map.Entry<List<String>, List<Integer>> entry: methodLine.entrySet()){
            if (entry.getKey().size() == methodParam){
                List<VariableInfo> variableInfos = _suspicious.getVariableInfo(_srcPath, errorLine);
                for (VariableInfo info: variableInfos){
                    if (info.isParameter && !entry.getKey().contains(info.variableName) && !TypeUtils.isArrayFromName(info.variableName)){
                        bannedVariables.add(info);
                    }
                }
            }
        }
        return bannedVariables;
    }






    private List<TraceResult> traceAnalysis(String traceResult, int assertLine, int errorLine){
        if (traceResult.equals("")){
            return new ArrayList<>();
        }

        List<String> traces = new ArrayList<String>();
        String line = "";
        for (String unit: traceResult.split("\\.")){
            if (unit.equals("")){
                continue;
            }
            if (unit.startsWith("|")){
                if (line.length() > 0){
                    traces.add(line);
                }
                line = unit;
            }
            if (!unit.startsWith("|")){
                line += "."+unit;
            }
        }
        if (line.length() > 0){
            traces.add(line);
        }
        List<TraceResult> results = new ArrayList<TraceResult>();
        for (String trace: traces){
            TraceResult result = new TraceResult(!trace.endsWith("E"));
            if (!trace.endsWith("E")){
                _suspicious.trueMethodCallNumFromTest += methodCallNum(_shellResult);
            }
            result._assertLine = assertLine;
            result._traceLine = errorLine;
            result._testClass = _testClassname;
            result._testMethod = _testMethodName;
            String[] pairs = trace.split("\\|");
            for (String pair: pairs){
                if (!pair.contains("=")){
                    continue;
                }
                if (pair.substring(pair.indexOf("=")+1).startsWith("\"+") && pair.substring(pair.indexOf("=")+1).endsWith("+\"")){
                    continue;
                }
                if (pair.length()>1000){
                    continue;
                }
                result.put(pair.substring(0, pair.lastIndexOf('=')),pair.substring(pair.lastIndexOf('=')+1));
            }
            if (result.getResultMap().size() != 0){
                results.add(result);
            }
        }
        return results;
    }


    private String traceShell(String testClassname, String classname, String functionname, String testClasspath, List<VariableInfo> vars, List<MethodInfo> methods, int errorLine) throws IOException{
        String tracePath = PathUtils.getAgentPath();
        String junitPath = PathUtils.getJunitPath();

        String agentArg = buildAgentArg(classname, functionname, testClasspath, vars, methods, errorLine);
        String encodedAgentArg = new BASE64Encoder().encode(agentArg.getBytes());
        encodedAgentArg = "\""+encodedAgentArg/*.substring(0, encodedAgentArg.length()-1)*/+"\"";
        String classpath = buildClasspath(Arrays.asList(junitPath));
        String[] arg = {"java","-javaagent:"+tracePath+"="+encodedAgentArg,"-cp",classpath,"cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner", testClassname+"#"+_testMethodName};
        String shellResult = ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        if (shellResult.length() <= 0){
            throw new IOException("Shell Run Error, Shell Args:"+ StringUtils.join(arg," "));
        }
        return shellResult;
    }


    private String buildAgentArg(String classname,String functionname, String testClassPath, List<VariableInfo> vars, List<MethodInfo> methods, int errorLine){
        String agentClass = "class:"+ classname;
        String agentFunc = "func:" + functionname;
        String agentLine = "line:"+ errorLine;
        String agentSrc = "src:" + _srcPath;
        String agentCp = "cp:" + "\""+_classpath+":"+StringUtils.join(_suspicious._libPath,":")+"/Users/yanrunfa/.m2/repository/org/joda/joda-convert/1.1/joda-convert-1.1.jar\"";
        String agentTestSrc = testClassPath.equals("")?"":"testsrc: "+testClassPath.trim();
        String agentTest = testClassPath.equals("")?"":"test:" + _testClassname;

        String agentVars = "";
        if (vars.size() > 0) {
            agentVars = "var:";
        }
        for (VariableInfo var: vars){
            agentVars += var.variableName;
            if (!var.isSimpleType){
                agentVars += "?";
                agentVars += var.otherType;
            }
            agentVars += "/";

        }
        String agentMethods = "";
        if (methods.size() > 0){
            agentMethods = "method:";
        }
        for (MethodInfo method: methods){
            agentMethods += method.methodName;
            if (!method.isSimpleType){
                agentMethods += "?";
                agentMethods += method.otherType;
            }
            agentMethods += "/";
        }
        return StringUtils.join(Arrays.asList(agentClass,agentFunc,agentLine,agentSrc,agentCp,agentVars,agentMethods, agentTest, agentTestSrc),",,");
    }

    private String buildClasspath(List<String> additionalPath){
        if (_suspicious._libPath.size()!=0){
            additionalPath = new ArrayList<>(additionalPath);
            additionalPath.addAll(_suspicious._libPath);
        }
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClasspath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }


    public String functionname(){
        return _functionname.substring(0, _functionname.indexOf("("));
    }

    private String analysisShellResult(String shellResult){
        String result = shellResult.substring(shellResult.indexOf(">>")+2,shellResult.lastIndexOf("<<"));
        if (result.equals("") ){
            return result;
        }
        // get the data segment of shell out
        if (result.contains("|")){
            result =  result.substring(result.indexOf("|"));
        }
        while (!(result.charAt(result.length()-1) == 'E' || result.charAt(result.length()-1) == '|')){
            result = result.substring(0, result.length()-1);
            if (result.length()== 0){
                break;
            }
        }
        return result;
    }

}
