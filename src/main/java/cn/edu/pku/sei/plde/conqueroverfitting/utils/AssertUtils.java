package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.CommentAssertTransformer;
import cn.edu.pku.sei.plde.conqueroverfitting.agent.Utils;
import cn.edu.pku.sei.plde.conqueroverfitting.slice.StaticSlice;
import com.sun.xml.internal.ws.api.ha.StickyFeature;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/3/11.
 */
public class AssertUtils {
    private String _trueClassPath = "";
    private String _classpath;
    private String _testClasspath;
    private String _testSrcPath;
    private String _testClassname;
    private String _testMethodName;
    private String _code;
    private String _methodCode;
    private int _methodStartLine;
    public AssertUtils(String classpath, String testClasspath, String testSrcPath, String testClassname, String testMethodName){
        _classpath = classpath;
        _testClassname = testClassname;
        _testClasspath = testClasspath;
        _testSrcPath = testSrcPath;
        _testMethodName = testMethodName;
        _code = FileUtils.getCodeFromFile(_testSrcPath, _testClassname);
        _methodCode = FileUtils.getTestFunctionCodeFromCode(_code,_testMethodName);
        _methodStartLine =((List<Integer>)CodeUtils.getMethodLine(_code,_testMethodName).values().toArray()[0]).get(0);
    }

    public List<Integer> getErrorAssertLine(){
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
                String trace = TestUtils.getTestTrace(_classpath, _testClasspath, _testClassname, _testMethodName);
                if (trace == null || trace.equals(oldTrace)){
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
                if (isAssertLine(lineString, code)){
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
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        File tempClassFile = FileUtils.copyFile(originClassFile.getAbsolutePath(), tempClassPath(_testClassname));
        _trueClassPath = tempClassFile.getAbsolutePath();
        originClassFile.delete();
        tempJavaFile.delete();
        backupClassFile.renameTo(originClassFile);
        return result;
    }

    public List<Integer> dependenceOfAssert(int assertLine){
        List<Integer> dependences = new ArrayList<>();
        String assertString = CodeUtils.getLineFromCode(_code, assertLine);
        List<String> params = CodeUtils.divideParameter(assertString,1);
        for (String param: params){
            StaticSlice staticSlice = new StaticSlice(_methodCode, param);
            String result = staticSlice.getSliceStatements();
            if (result.equals("")){
                continue;
            }
            for (String line: result.split("\n")){
                if (isAssertLine(line, _code)){
                    continue;
                }
                int lineNum = CodeUtils.getLineNumOfLineString(_code, line, _methodStartLine);
                if (lineNum!=-1 && lineNum < assertLine){
                    dependences.add(lineNum);
                }
            }
        }
        return dependences;
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

    public static boolean isAssertLine(String lineString, String code){
        lineString = lineString.trim();
        if (lineString.startsWith("Assert") || lineString.contains(".assert") || lineString.startsWith("fail")){
            return true;
        }
        else if (lineString.contains("(") && lineString.contains(")") && !lineString.contains("=")){
            String callMethod = lineString.substring(0, lineString.indexOf("(")).trim();
            if (code.contains("void "+callMethod+"(")){
                return true;
            }
        }
        return false;
    }

    public String getTrueTestFile(){
        if (_trueClassPath.equals("")){
            _trueClassPath = getTrueTestFile(_classpath,_testClasspath,_testSrcPath,_testClassname,_testMethodName);
        }
        return _trueClassPath;
    }

    public String getTrueTestFile(String classpath, String testClasspath, String testSrcPath, String testClassname, String testMethodName){
        File tempJavaFile = FileUtils.copyFile(
                FileUtils.getFileAddressOfJava(testSrcPath, testClassname),
                FileUtils.tempJavaPath(testClassname));
        File originClassFile = new File(FileUtils.getFileAddressOfClass(testClasspath, testClassname));
        File backupClassFile = FileUtils.copyFile(originClassFile.getAbsolutePath(), originClassFile.getAbsolutePath()+".temp");
        while (true){
            int lineNum = 0;
            try{
                String trace = TestUtils.getTestTrace(classpath, testClasspath, testClassname, testMethodName);
                if (trace == null){
                    break;
                }
                for (String line : trace.split("\n")){
                    if (line.contains(testClassname) && line.contains(testMethodName) && line.contains("(") && line.contains(")") && line.contains(":")){
                        int newlineNum =  Integer.valueOf(line.substring(line.lastIndexOf("(")+1,line.lastIndexOf(")")).split(":")[1]);
                        if (newlineNum == lineNum){
                            break;
                        }
                        else {
                            lineNum = newlineNum;
                        }
                    }
                }
                if (CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(tempJavaFile.getAbsolutePath()),lineNum).trim().startsWith("fail(")){
                    int num = lineNum -1;
                    while (!CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(tempJavaFile.getAbsolutePath()),num).trim().startsWith("try")){
                        SourceUtils.commentCodeInSourceFile(tempJavaFile, num);
                        num--;
                    }
                }
                SourceUtils.commentCodeInSourceFile(tempJavaFile,lineNum);
                System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath(),testClasspath,classpath)) +" -d "+testClasspath+" "+ tempJavaFile.getAbsolutePath())));
            }
            catch (NotFoundException e){
                System.out.println("ERROR: Cannot Find Source File: " + testClassname + " in temp file package\n");
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        File tempClassFile = FileUtils.copyFile(originClassFile.getAbsolutePath(), tempClassPath(testClassname));
        originClassFile.delete();
        tempJavaFile.delete();
        backupClassFile.renameTo(originClassFile);
        return tempClassFile.getAbsolutePath();
    }

    public static String buildClasspath(List<String> pathList){
        String path = "\"";
        path += StringUtils.join(pathList,System.getProperty("path.separator"));
        path += "\"";
        return path;
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
