package cn.edu.pku.sei.plde.conqueroverfitting.assertCollect;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.CommentAssertTransformer;
import cn.edu.pku.sei.plde.conqueroverfitting.agent.Utils;
import cn.edu.pku.sei.plde.conqueroverfitting.slice.StaticSlice;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import com.sun.org.apache.bcel.internal.classfile.Code;
import com.sun.xml.internal.ws.api.ha.StickyFeature;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by yanrunfa on 16/3/11.
 */
public class Asserts {
    private String _trueClassPath = "";
    private String _classpath;
    private String _testClasspath;
    private String _testSrcPath;
    private String _testClassname;
    private String _testMethodName;
    private String _code;
    private List<String> _libPath;
    private String _methodCode;
    private int _methodStartLine;
    public List<Integer> _errorLines = new ArrayList<>();
    public int _assertNums;
    public List<String> _asserts;

    public Asserts(String classpath, String testClasspath, String testSrcPath, String testClassname, String testMethodName, List<String> libPath) {
        _libPath = libPath;
        _classpath = classpath;
        _testClassname = testClassname;
        _testClasspath = testClasspath;
        _testSrcPath = testSrcPath;
        _testMethodName = testMethodName;
        _code = FileUtils.getCodeFromFile(_testSrcPath, _testClassname);
        if (!_code.contains(_testMethodName) && _code.contains(" extends ")){
            String extendsClass = _code.split(" extends ")[1].substring(0, _code.split(" extends ")[1].indexOf("{"));
            String className = CodeUtils.getClassNameOfImportClass(_code, extendsClass);
            if (className.equals("")){
                className = CodeUtils.getPackageName(_code)+"."+extendsClass;
            }
            String extendsCode = FileUtils.getCodeFromFile(testSrcPath, className.trim());
            if (!extendsCode.equals("")){
                _code = extendsCode;
            }
        }
        _methodCode = FileUtils.getTestFunctionCodeFromCode(_code,_testMethodName, _testSrcPath);
        List<List<Integer>> methodLines = new ArrayList<>(CodeUtils.getMethodLine(_code,_testMethodName).values());
        if (methodLines.size() == 0){

        }
        _methodStartLine =methodLines.get(0).get(0);
        _asserts = CodeUtils.getAssertInTest(_code, testMethodName);
        _assertNums = _asserts.size();
        _errorLines = getErrorAssertLine();
    }

    public Asserts(String classpath, String testClasspath, String testSrcPath, String testClassname, String testMethodName){
        this(classpath, testClasspath, testSrcPath, testClassname, testMethodName, new ArrayList<String>());
    }

    private List<Integer> getErrorAssertLine(){
        List<Integer> result = new ArrayList<>();
        File tempJavaFile = FileUtils.copyFile(
                FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname),
                tempJavaPath(_testClassname));
        File originClassFile = new File(FileUtils.getFileAddressOfClass(_testClasspath, _testClassname));
        File backupClassFile = FileUtils.copyFile(originClassFile.getAbsolutePath(), originClassFile.getAbsolutePath()+".temp");
        String oldTrace = "";
        while (true){
            int lineNum = 0;
            try{
                List<String> classpaths = new ArrayList<>(_libPath);
                classpaths.add(_classpath);
                String trace = TestUtils.getTestTrace(classpaths, _testClasspath, _testClassname, _testMethodName);
                if (trace == null || trace.equals(oldTrace) || trace.contains("NoClassDefFoundError")){
                    break;
                }
                oldTrace = trace;
                for (String line : trace.split("\n")){
                    if (line.contains(_testClassname) && line.contains(_testMethodName) && line.contains("(") && line.contains(")") && line.contains(":")){
                        lineNum =  Integer.valueOf(line.substring(line.lastIndexOf("(")+1,line.lastIndexOf(")")).split(":")[1]);
                    }
                }
                if (result.contains(lineNum)) {
                    break;
                }
                String lineString = CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(tempJavaFile.getAbsolutePath()),lineNum).trim();
                String code = FileUtils.getCodeFromFile(tempJavaFile);
                if (AssertUtils.isAssertLine(lineString, code)){
                    result.add(lineNum);
                }
                if (lineString.startsWith("fail")){
                    int num = lineNum -1;
                    while (!CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(tempJavaFile),num).trim().startsWith("try")){
                        SourceUtils.commentCodeInSourceFile(tempJavaFile, num);
                        num--;
                    }
                }
                int bracketCount = 0;
                int i = 1;
                SourceUtils.commentCodeInSourceFile(tempJavaFile,lineNum);
                bracketCount += count(lineString,'(');
                bracketCount -= count(lineString,')');
                while (bracketCount > 0){
                    SourceUtils.commentCodeInSourceFile(tempJavaFile,lineNum+i);
                    bracketCount += count(CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(tempJavaFile),lineNum+i),'(');
                    bracketCount -= count(CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(tempJavaFile),lineNum+i),')');
                    i++;
                }
                System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath(),_testClasspath,_classpath)) +" -d "+_testClasspath+" "+ tempJavaFile.getAbsolutePath())));
            }
            catch (NotFoundException e){
                System.out.println("ERROR: Cannot Find Source File: " + _testClassname + " in temp file package\n");
                break;
            }
            catch (IOException e){
                e.printStackTrace();
                break;
            }
        }
        originClassFile.delete();
        tempJavaFile.delete();
        backupClassFile.renameTo(originClassFile);
        return result;
    }

    public List<Integer> dependenceOfAssert(int assertLine){
        List<Integer> dependences = new ArrayList<>();
        dependences.add(assertLine);
        List<Integer> result = new ArrayList<>();
        String assertString = CodeUtils.getLineFromCode(_code, assertLine);
        if (assertString.startsWith("fail(")){
            boolean tryed = false;
            boolean catched = false;
            for (int i=1; i<assertLine; i++){
                if (tryed && catched){
                    break;
                }
                String lastLine = CodeUtils.getLineFromCode(_code, assertLine-i);
                String nextLine = CodeUtils.getLineFromCode(_code, assertLine+i);

                if (!lastLine.contains("try") && !tryed){
                    dependences.add(assertLine-i);
                    result.add(assertLine-i);
                }else {
                    tryed = true;
                }
                if (!nextLine.contains("catch") && !catched){
                    dependences.add(assertLine+i);
                    result.add(assertLine+i);
                }
                else {
                    catched = true;
                }
            }
        }
        for (int dependence: dependences){
            result.addAll(lineStaticAnalysis(dependence));
        }
        return new ArrayList<>(new HashSet(result));
    }

    public List<Integer> lineStaticAnalysis(int analysisLine){
        List<Integer> dependences = new ArrayList<>();
        String lineString = CodeUtils.getLineFromCode(_code, analysisLine);
        if (lineString.contains("=")){
            lineString = lineString.substring(lineString.indexOf("=")+1);
        }
        List<String> params = CodeUtils.divideParameter(lineString, 1);
        for (String param: params){
            StaticSlice staticSlice = new StaticSlice(_methodCode, param);
            String result = staticSlice.getSliceStatements();
            if (result.equals("")){
                continue;
            }
            for (String line: result.split("\n")){
                if (AssertUtils.isAssertLine(line, _code)){
                    continue;
                }
                int lineNum = CodeUtils.getLineNumOfLineString(_code, line, _methodStartLine);
                if (lineNum!=-1 && lineNum < analysisLine){
                    dependences.add(lineNum);
                }
            }
        }
        return dependences;
    }

    public int errorAssertNum(){
        return _errorLines.size();
    }

    public int trueAssertNum(){
        return _assertNums - _errorLines.size();
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



    public String getTrueTestFile(){
        if (_errorLines.size()>0){
            return getTrueTestFile(_errorLines);
        }
        else {
            return "";
        }
    }


    public String getTrueTestFile(List<Integer> errorAssertLines){
        if (!_trueClassPath.equals("")){
            return _trueClassPath;
        }
        File tempJavaFile = FileUtils.copyFile(
                FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname),
                FileUtils.tempJavaPath(_testClassname, "Asserts"));
        List<String> assertLines = CodeUtils.getAssertInTest(_testSrcPath,_testClassname,_testMethodName);
        for (int assertLine: errorAssertLines){
            String assertString = CodeUtils.getLineFromCode(_code, assertLine);
            assertLines.remove(assertString);
        }
        Set<Integer> lineSet = new HashSet<>();
        for (String assrtString: assertLines){
            int line = CodeUtils.getLineNumOfLineString(_code, assrtString);
            lineSet.add(line);
            lineSet.addAll(lineStaticAnalysis(line));
        }
        try {
            List<Integer> functionLine = FileUtils.getTestFunctionLineFromCode(FileUtils.getCodeFromFile(tempJavaFile),_testMethodName);
            int beginLine = functionLine.get(0)+1;
            int endLine = functionLine.get(1);
            for (int i=beginLine; i<= endLine; i++){
                String lineString = CodeUtils.getLineFromCode(_code, i);
                if (!lineSet.contains(i)&& !lineString.contains("{") && !lineString.contains("}")){
                    SourceUtils.commentCodeInSourceFile(tempJavaFile, i);
                }
            }
            System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath(),_testClasspath,_classpath)) +" "+ tempJavaFile.getAbsolutePath())));
        } catch (NotFoundException e){
            e.printStackTrace();
            return "";
        } catch (IOException e){
            e.printStackTrace();
            return "";
        }
        tempJavaFile.delete();
        _trueClassPath = tempJavaFile.getAbsolutePath().substring(0,tempJavaFile.getAbsolutePath().lastIndexOf("."))+".class";
        return _trueClassPath;
    }

    public String buildClasspath(List<String> pathList){
        pathList = new ArrayList<>(pathList);
        if (_libPath!= null){
            pathList.addAll(_libPath);
        }
        String path = "\"";
        path += StringUtils.join(pathList,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

    public String buildClasspath(String path){
        return buildClasspath(new ArrayList<String>(Arrays.asList(path)));
    }

    private static String tempJavaPath(String classname){
        if (!new File(System.getProperty("user.dir")+"/temp/assert/").exists()){
            new File(System.getProperty("user.dir")+"/temp/assert/").mkdirs();
        }
        return System.getProperty("user.dir")+"/temp/assert/"+classname.substring(classname.lastIndexOf(".")+1)+".java";
    }

    private static String tempClassPath(String classname){
        if (!new File(System.getProperty("user.dir")+"/temp/assert/").exists()){
            new File(System.getProperty("user.dir")+"/temp/assert/").mkdirs();
        }
        return System.getProperty("user.dir")+"/temp/assert/"+classname.substring(classname.lastIndexOf(".")+1)+".class";
    }
}
