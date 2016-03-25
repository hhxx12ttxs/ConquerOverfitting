package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.Utils;
import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;

import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;

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
    private Map<String, Integer> _commentedTestClass = new HashMap<>();
    private String _testMethodName;
    private String _testSrcPath;
    private Asserts _asserts;

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
        _commentedTestClass.clear();
        _asserts = new Asserts(_classpath,_testClasspath,_testSrcPath,testClassname,testMethodName);

        List<MethodInfo> methodInfos = _suspicious.getMethodInfo(_srcPath);
        List<Integer> errorLines = isSuccess?Arrays.asList(errorLine):getErrorLine(errorLine);
        List<TraceResult> results = new ArrayList<>();

        for (int line: errorLines){
            List<VariableInfo> variableInfos = _suspicious.getVariableInfo(_srcPath, line);
            variableInfos.removeAll(getBannedVariables(line));
            if (variableInfos.size() == 0 &&  methodInfos.size() == 0){
                continue;
            }
            if (_commentedTestClass.size() <= 1){
                _commentedTestClass.put("", line);
            }
            if (_asserts.trueAssertNum() > 0 && _asserts.errorAssertNum() > 1 && !_asserts.getTrueTestFile().equals("")){
                _commentedTestClass.put(_asserts.getTrueTestFile(), -1);
            }
            for (Map.Entry<String, Integer> commentedTestClass: _commentedTestClass.entrySet()) {
                String shellResult = traceShell(_testClassname, _classname, _functionname, commentedTestClass.getKey(), variableInfos, methodInfos, line);
                if (shellResult.contains(">>") && shellResult.contains("<<")) {
                    String traceResult = analysisShellResult(shellResult);
                    results.addAll(traceAnalysis(traceResult, commentedTestClass.getValue()));
                } else {
                    printErrorShell(shellResult);
                }
            }
        }
        _suspicious._assertsMap.put(_testClassname+"#"+_testMethodName, _asserts);
        _suspicious._errorLineMap.put(_testClassname+"#"+_testMethodName, errorLines);
        deleteTempFile();
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
        String assertLine = _asserts._asserts.get(0);
        String code = FileUtils.getCodeFromFile(_srcPath,_classname);
        int methodParam = CodeUtils.getMethodParams(assertLine, functionname()).size();
        Map<List<String>, List<Integer>> methodLine = CodeUtils.getMethodLine(code,functionname());
        for (Map.Entry<List<String>, List<Integer>> entry: methodLine.entrySet()){
            if (entry.getKey().size() == methodParam){
                List<VariableInfo> variableInfos = _suspicious.getVariableInfo(_srcPath, errorLine);
                for (VariableInfo info: variableInfos){
                    if (info.isParameter && !entry.getKey().contains(info.variableName)){
                        bannedVariables.add(info);
                    }
                }
            }
        }
        return bannedVariables;
    }


    private List<Integer> getErrorLine(int errorLine){
        List<Integer> result = new ArrayList<>();
        String code = FileUtils.getCodeFromFile(_srcPath,_classname);
        if (CodeUtils.isConstructor(_classname,_functionname)){
            List<Integer> returnLine = CodeUtils.getReturnLine(code, functionname(),CodeUtils.getConstructorParamsCount(_functionname));
            result.addAll(returnLine);
        }
        if (_asserts._assertNums < 2){
            if (!result.contains(errorLine)){
                result.add(errorLine);
            }
            return result;
        }
        List<Integer> lines = getTrueErrorLine();
        for (int line: lines){
            if (line>0 && !result.contains(line)){
                result.add(line);
            }
        }
        if (result.size()> 0){
            return result;
        }
        result.add(errorLine);
        return result;
    }



    private List<TraceResult> traceAnalysis(String traceResult, int assertLine){
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
            result._assertLine = assertLine;
            result._testClass = _testClassname;
            result._testMethod = _testMethodName;
            String[] pairs = trace.split("\\|");
            for (String pair: pairs){
                if (!pair.contains("=")){
                    continue;
                }
                if (pair.substring(pair.indexOf("=")+1).equals("\"+"+pair.substring(0, pair.indexOf('='))+"+\"")){
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


    private String traceShell(String testClassname, String classname, String functionname, String testClasspath, List<VariableInfo> vars, List<MethodInfo> methods, int errorLine) throws IOException{
        String tracePath = PathUtils.getAgentPath();
        String junitPath = PathUtils.getJunitPath();

        String agentArg = buildAgentArg(classname, functionname, testClasspath, vars, methods, errorLine);
        String classpath = buildClasspath(Arrays.asList(junitPath));
        String[] arg = {"java","-javaagent:"+tracePath+"="+agentArg,"-cp",classpath,"cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner", testClassname+"#"+_testMethodName};
        System.out.print(StringUtils.join(arg," "));
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
        String agentCp = "cp:" + "\""+_classpath+":"+"/Users/yanrunfa/.m2/repository/org/joda/joda-convert/1.2/joda-convert-1.2.jar\"";
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
        return "\""+StringUtils.join(Arrays.asList(agentClass,agentFunc,agentLine,agentSrc,agentCp,agentVars,agentMethods, agentTest, agentTestSrc),",")+"\"";
    }


    private List<Integer> getTrueErrorLine(){
        List<Integer> assertLines = _asserts._errorLines;
        List<Integer> result = new ArrayList<>();
        if (assertLines.size() <= 1){
            return result;
        }
        for (int assertLine: assertLines){
            List<Suspicious> suspiciouses = new ArrayList<>();
            File originJavaFile = new File(FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname));
            File originClassFile = new File(FileUtils.getFileAddressOfClass(_testClasspath,_testClassname));
            File renameJavaFile = new File(FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname)+".temp");
            File renameClassFile = new File(FileUtils.getFileAddressOfClass(_testClasspath,_testClassname)+".temp");
            originJavaFile.renameTo(renameJavaFile);
            originClassFile.renameTo(renameClassFile);
            try {
                if (!originJavaFile.exists()){
                    originJavaFile.createNewFile();
                }
                FileOutputStream outputStream = new FileOutputStream(originJavaFile);
                BufferedReader reader = new BufferedReader(new FileReader(_testSrcPath+"/"+_testClassname.replace(".","/")+".java.temp"));
                String lineString = null;
                List<Integer> functionLine = FileUtils.getTestFunctionLineFromCode(FileUtils.getCodeFromFile(_testSrcPath+"/"+_testClassname.replace(".","/")+".java.temp"),_testMethodName);
                int beginLine = functionLine.get(0)+1;
                int endLine = functionLine.get(1);
                int line = 0;
                int tryLine = -1;
                List<Integer> commitedAfter = new ArrayList<>();
                List<Integer> assertDependences = _asserts.dependenceOfAssert(assertLine);
                assertDependences.add(assertLine);
                int bracketCount = 0;
                while ((lineString = reader.readLine()) != null) {
                    line++;
                    if (lineString.trim().startsWith("try")){
                        tryLine = line+1;
                    }
                    if (lineString.trim().contains("catch")){
                        tryLine = -1;
                    }
                    if (line >= beginLine && line <= endLine && !assertDependences.contains(line) && !lineString.contains("{") && !lineString.contains("}")){
                        if (lineString.trim().startsWith("fail(") && tryLine != -1){
                            for (int i= tryLine; i< line; i++){
                                commitedAfter.add(i);
                            }
                        }
                        bracketCount+= count(lineString,'(');
                        bracketCount-= count(lineString,')');
                        lineString = "//"+lineString;
                    }
                    else if (bracketCount > 0){
                        lineString = "//"+lineString;
                        bracketCount+= count(lineString,'(');
                        bracketCount -= count(lineString, ')');
                    }
                    outputStream.write((lineString+"\n").getBytes());

                }
                outputStream.close();
                reader.close();
                for (int num: commitedAfter){
                    SourceUtils.commentCodeInSourceFile(originJavaFile, num);
                }
                System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+_testClasspath+" "+ originJavaFile.getAbsolutePath())));
                new File(System.getProperty("user.dir")+"/temp/"+assertLine).mkdirs();
                System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+System.getProperty("user.dir")+"/temp/"+assertLine+"/ "+ originJavaFile.getAbsolutePath())));
                _commentedTestClass.put(FileUtils.getFileAddressOfClass(System.getProperty("user.dir")+"/temp/"+assertLine,_testClassname), assertLine);
                Localization localization = new Localization(_classpath, _testClasspath, _testSrcPath, _srcPath, _testClassname);
                suspiciouses = localization.getSuspiciousLite(false);

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
                    result.add(suspicious.getDefaultErrorLine());
                    break;
                }
            }
        }
        return result;
    }

    private int count(String s,char c){
        int count= 0;
        for (int i=0; i< s.length(); i++){
            if (s.charAt(i)==c){
                count++;
            }
        }
        return count;
    }

    private String buildClasspath(List<String> additionalPath){
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

    private String buildClasspath(){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClasspath;
        path += "\"";
        return path;
    }

    private String getClassSrcPath(String classSrc){
        String classname = _classname;
        if (_classname.contains("$")){
            classname = _classname.substring(0, _classname.lastIndexOf('$'));
        }
        String result =  classSrc + System.getProperty("file.separator") + classname.replace(".",System.getProperty("file.separator")) + ".java";
        return result.replace(" ","");
    }


    private String getClassSrcIndex(String classSrc){
        String classSrcPath = getClassSrcPath(classSrc);
        return classSrcPath.substring(0,classSrcPath.lastIndexOf(System.getProperty("file.separator"))).replace(" ","");
    }


    public String functionname(){
        return _functionname.substring(0, _functionname.indexOf("("));
    }

    private String analysisShellResult(String shellResult){
        String result = shellResult.substring(shellResult.indexOf(">>")+2,shellResult.indexOf("<<"));
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
