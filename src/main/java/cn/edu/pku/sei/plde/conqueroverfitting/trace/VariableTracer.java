package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.RunTestAgent;
import cn.edu.pku.sei.plde.conqueroverfitting.agent.Utils;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.JavaLibrary;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar.GZoltarSuspiciousProgramStatements;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar.StatementExt;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Ochiai;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;

import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import com.gzoltar.core.components.Statement;
import com.sun.tools.corba.se.idl.constExpr.Not;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.junit.runner.JUnitCore;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/19.
 */

public class VariableTracer {

    private final String _classpath;
    private final String _testClasspath;
    private final String _srcPath;
    private String _testClassname;
    private String _classname;
    private String _functionname;
    private List<VariableInfo> _vars;
    private List<MethodInfo> _methods;
    private int _errorLine;
    private String _shellResult;
    private String _traceResult;
    private String _testMethodName;
    private String _testSrcPath;
    private int _errorAssertLine = 0;
    private String _commentedTestClass = "";
    private String _trueTestClass = "";
    private int trueAssert = 0;

    /**
     *
     * @param classpath the path of .class file
     * @param testClasspath the path of .class test file
     * @param srcPath the path of source file
     */
    public VariableTracer(String classpath, String testClasspath, String srcPath, String testSrcPath){
        _classpath = classpath;
        _testClasspath = testClasspath;
        _srcPath = srcPath;
        _testSrcPath = testSrcPath;
    }


    private String analysisShellResult(String shellResult){
        String result = shellResult.substring(shellResult.indexOf(">>")+2,shellResult.indexOf("Time:"));
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
    /**
     *
     * @param classname the tracing class name
     * @param testClassname the entry to tracing
     * @param errorLine the line number to be traced
     * @param vars the list of variable info to be traced
     * @param methods the list of method info to be traced
     * @return the list of trace result
     * @throws IOException
     */
    public List<TraceResult> trace(String classname,String functionname, String testClassname, String testMethodName,int errorLine, List<VariableInfo> vars, List<MethodInfo> methods)throws IOException{
        if (vars.size() == 0 && methods.size() == 0){
            System.out.println("No Variable or Method to trace");
            return new ArrayList<TraceResult>();
        }

        _classname = classname;
        _vars = vars;
        _methods = methods;
        _testClassname = testClassname;
        _testMethodName = testMethodName;
        _functionname = functionname;

        _errorLine = getErrorLine(errorLine);
        List<TraceResult> results = new ArrayList<>();


        _shellResult = traceShell(_testClassname,_classname,_functionname,_commentedTestClass,_vars,_methods);
        if (_shellResult.contains(">>") && _shellResult.contains("Time:")){
            _traceResult = analysisShellResult(_shellResult);
            results.addAll(traceAnalysis(_traceResult));
        }
        else {
            printErrorShell();
        }

        if (!_trueTestClass.equals("") && trueAssert > 0){
            _shellResult = traceShell(_testClassname, _classname, _functionname, _trueTestClass, _vars, _methods);
            if (_shellResult.contains(">>") && _shellResult.contains("Time:")){
                _traceResult = analysisShellResult(_shellResult);
                results.addAll(traceAnalysis(_traceResult));
            }
            else {
                printErrorShell();
            }
        }
        deleteTempFile();
        return results;
    };


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

    private void printErrorShell(){
        System.out.println("Trace Fial in Output:");
        System.out.println(_shellResult);
    }


    private int getErrorLine(int errorLine){
        int assertCount = CodeUtils.getAssertCountInTest(_testSrcPath,_testClassname,_testMethodName);
        if (assertCount < 2){
            return errorLine;
        }
        int line = getTrueErrorLine();
        errorLine = line==-1?errorLine:line;
        return errorLine;
    }


    private List<TraceResult> traceAnalysis(String traceResult){
        List<String> units = new ArrayList<>();
        //for (String unit: traceResult.split("\\|")){
        //    if (!units.contains(unit)){
        //        units.add(unit);
        //    }
        //}
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
            String[] pairs = trace.split("\\|");
            for (String pair: pairs){
                if (!pair.contains("=")){
                    continue;
                }
                if (pair.substring(pair.indexOf("=")+1).equals("\"+"+pair.substring(0, pair.indexOf('='))+"+")){
                    continue;
                }
                result.put(pair.substring(0, pair.indexOf('=')),pair.substring(pair.indexOf('=')+1));
            }
            if (result.getResultMap().size() != 0){
                results.add(result);
            }
        }
        return results;
    }


    private String traceShell(String testClassname, String classname, String functionname, String testClasspath, List<VariableInfo> vars, List<MethodInfo> methods) throws IOException{
        String tracePath = PathUtils.getAgentPath();
        String junitPath = PathUtils.getJunitPath();

        String agentArg = buildAgentArg(classname, functionname, testClasspath, vars, methods);
        String classpath = buildClasspath(Arrays.asList(junitPath));
        String[] arg = {"java","-javaagent:"+tracePath+"="+agentArg,"-cp",classpath,"org.junit.runner.JUnitCore", testClassname};
        System.out.print(StringUtils.join(arg," "));
        String shellResult = ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        if (shellResult.length() <= 0){
            throw new IOException("Shell Run Error, Shell Args:"+ StringUtils.join(arg," "));
        }
        return shellResult;
    }


    private String buildAgentArg(String classname,String functionname, String testClassPath, List<VariableInfo> vars, List<MethodInfo> methods){
        String agentClass = "class:"+ classname;
        String agentFunc = "func:" + functionname;
        String agentLine = "line:"+ _errorLine;
        String agentSrc = "src:" + _srcPath;
        String agentCp = "cp:" + _classpath;
        String agentTestSrc = testClassPath.equals("")?"":"testsrc: "+testClassPath.trim();
        //String agentAssert = "";
        String agentTest = testClassPath.equals("")?"":"test:" + _testClassname;
        //String agentTestSrc = "";
        //int assertLine = getErrorAssertLine();
        //if (assertLine!= -1){
        //    agentAssert = "assert:"+String.valueOf(assertLine);
        //    agentTest = "test:"+_testClassname+"/"+_testMethodName;
        //    agentTestSrc = "testsrc:"+_testSrcPath;
        //}

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
        return "\""+StringUtils.join(Arrays.asList(agentClass,agentFunc,agentLine,agentSrc,agentCp,agentVars,agentMethods, agentTest, agentTestSrc),",")+"\"";
    }

    private int getErrorAssertLine(){
        if (_errorAssertLine!= 0){
            return _errorAssertLine;
        }
        int lineNum = -1;
        try{
            String trace = TestUtils.getTestTrace(_classpath, _testClasspath, _testClassname, _testMethodName);
            if (trace == null){
                return -1;
            }
            for (String line : trace.split("\n")){
                if (line.contains(_testClassname) && line.contains(_testMethodName) && line.contains("(") && line.contains(")") && line.contains(":")){
                    lineNum =  Integer.valueOf(line.substring(line.lastIndexOf("(")+1,line.lastIndexOf(")")).split(":")[1]);
                }
            }
        }catch (NotFoundException e){
            System.out.println("ERROR: Cannot Find Source File: "+_testClassname+" in Source Path: "+ _testSrcPath);
        }
        String lineString = CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname)),_errorAssertLine).trim();
        if (!lineString.startsWith("Assert") && !lineString.startsWith("assert") && !lineString.startsWith("fail")){
            lineNum = -1;
        }
        _errorAssertLine = lineNum;
        return _errorAssertLine;
    }


    private int getTrueErrorLine(){
        int assertLine = getErrorAssertLine();
        if (assertLine == -1){
            return -1;
        }
        List<Suspicious> suspiciouses = new ArrayList<>();

        File originJavaFile = new File(FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname));
        File originClassFile = new File(FileUtils.getFileAddressOfClass(_testClasspath,_testClassname));
        File renameJavaFile = new File(FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname)+".temp");
        File renameClassFile = new File(FileUtils.getFileAddressOfClass(_testClasspath,_testClassname)+".temp");
        File trueJavaFile = new File(System.getProperty("user.dir")+"/temp/true/"+_testClassname.substring(_testClassname.lastIndexOf(".")+1)+".java");
        File trueClassFile = new File(System.getProperty("user.dir")+"/temp/true/"+_testClassname.substring(_testClassname.lastIndexOf(".")+1)+".class");
        originJavaFile.renameTo(renameJavaFile);
        originClassFile.renameTo(renameClassFile);
        try {
            if (!originJavaFile.exists()){
                originJavaFile.createNewFile();
            }
            if (!trueJavaFile.exists()){
                FileUtils.createFile(System.getProperty("user.dir")+"/temp/true/"+_testClassname.substring(_testClassname.lastIndexOf(".")+1)+".java");
            }
            FileOutputStream outputStream = new FileOutputStream(originJavaFile);
            FileOutputStream trueOutputStream = new FileOutputStream(trueJavaFile);
            BufferedReader reader = new BufferedReader(new FileReader(_testSrcPath+"/"+_testClassname.replace(".","/")+".java.temp"));
            String lineString = null;
            List<Integer> functionLine = FileUtils.getTestFunctionLineFromCode(FileUtils.getCodeFromFile(_testSrcPath+"/"+_testClassname.replace(".","/")+".java.temp"),_testMethodName);
            int beginLine = functionLine.get(0);
            int endLine = functionLine.get(1);
            int line = 0;
            while ((lineString = reader.readLine()) != null) {
                line++;
                if (line == assertLine){
                    lineString = "//"+lineString;
                    trueOutputStream.write((lineString+"\n").getBytes());
                    lineString = lineString.substring(3);
                }else {
                    trueOutputStream.write((lineString+"\n").getBytes());
                }
                if (line >= beginLine && line <= endLine && line!=assertLine && (lineString.trim().startsWith("assert") || lineString.trim().startsWith("Assert")|| lineString.trim().startsWith("fail("))){
                    lineString = "//"+lineString;
                    trueAssert++;
                }
                outputStream.write((lineString+"\n").getBytes());

            }
            outputStream.close();
            trueOutputStream.close();
            reader.close();
            System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.7 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+_testClasspath+" "+ originJavaFile.getAbsolutePath())));
            System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.7 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+System.getProperty("user.dir")+"/temp/"+" "+ originJavaFile.getAbsolutePath())));
            System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.7 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) + " " + trueJavaFile.getAbsolutePath())));
            _commentedTestClass = FileUtils.getFileAddressOfClass(System.getProperty("user.dir")+"/temp",_testClassname);
            _trueTestClass = trueClassFile.getAbsolutePath();
            Localization localization = new Localization(_classpath, _testClasspath, _testSrcPath, _srcPath);
            suspiciouses = localization.getSuspiciousLite(1);

        } catch (NotFoundException e){
            System.out.println("ERROR: Cannot Find Source File: "+ _testClassname +" in Source Path: "+ _testSrcPath);
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        originJavaFile.delete();
        renameJavaFile.renameTo(originJavaFile);
        originClassFile.delete();
        renameClassFile.renameTo(originClassFile);

        for (Suspicious suspicious: suspiciouses){
            if (suspicious.functionname().equals(_functionname) && suspicious.classname().equals(_classname)){
                return suspicious.lastLine();
            }
        }
        return -1;
    }

    private String buildClasspath(List<String> additionalPath){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClasspath;
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

    private String buildClasspath(){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClasspath;
        path += "\"";
        return path;
    }

}
