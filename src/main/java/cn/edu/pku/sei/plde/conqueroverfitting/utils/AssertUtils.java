package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.Utils;
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

    public AssertUtils(String classpath, String testClasspath, String testSrcPath, String testClassname, String testMethodName){
        _classpath = classpath;
        _testClassname = testClassname;
        _testClasspath = testClasspath;
        _testSrcPath = testSrcPath;
        _testMethodName = testMethodName;
    }

    public List<Integer> getErrorAssertLine(){
        List<Integer> result = new ArrayList<>();
        File tempJavaFile = FileUtils.copyFile(
                FileUtils.getFileAddressOfJava(_testSrcPath, _testClassname),
                tempJavaPath(_testClassname));
        File originClassFile = new File(FileUtils.getFileAddressOfClass(_testClasspath, _testClassname));
        File backupClassFile = FileUtils.copyFile(originClassFile.getAbsolutePath(), originClassFile.getAbsolutePath()+".temp");
        while (true){
            int lineNum = 0;
            try{
                String trace = TestUtils.getTestTrace(_classpath, _testClasspath, _testClassname, _testMethodName);
                if (trace == null){
                    break;
                }
                for (String line : trace.split("\n")){
                    if (line.contains(_testClassname) && line.contains(_testMethodName) && line.contains("(") && line.contains(")") && line.contains(":")){
                        lineNum =  Integer.valueOf(line.substring(line.lastIndexOf("(")+1,line.lastIndexOf(")")).split(":")[1]);
                    }
                }
                if (result.contains(lineNum)) {
                    break;
                }
                String lineString = CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(tempJavaFile.getAbsolutePath()),lineNum).trim();
                if (lineString.startsWith("Assert") || lineString.startsWith("assert") || lineString.startsWith("fail")){
                    result.add(lineNum);
                }
                else if (lineString.contains("(") && lineString.contains(")") && !lineString.contains("=")){
                    String callMethod = lineString.substring(0, lineString.indexOf("(")).trim();
                    if (FileUtils.getCodeFromFile(tempJavaFile).contains("void "+callMethod+"(")){
                        result.add(lineNum);
                    }
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
        if (_trueClassPath.equals("")){
            _trueClassPath = getTrueTestFile(_classpath,_testClasspath,_testSrcPath,_testClassname,_testMethodName);
        }
        return _trueClassPath;
    }

    public static String getTrueTestFile(String classpath, String testClasspath, String testSrcPath, String testClassname, String testMethodName){
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
