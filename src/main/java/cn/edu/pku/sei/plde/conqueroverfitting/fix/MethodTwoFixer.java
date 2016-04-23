package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.Utils;
import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.support.Factory;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by yanrunfa on 16/4/1.
 */
public class MethodTwoFixer {
    private final String _classpath;
    private final String _testClassPath;
    private final String _classSrcPath;
    private final String _testSrcPath;
    private final String _className;
    private final String _methodName;
    private Suspicious _suspicious;
    private String _code;
    private String _methodCode;
    private Set<Integer> _errorLines;
    private int _methodStartLine;
    private int _methodEndLine;

    public MethodTwoFixer(Suspicious suspicious){
        _suspicious = suspicious;
        _classpath = suspicious._classpath;
        _testClassPath = suspicious._testClasspath;
        _classSrcPath = suspicious._srcPath;
        _testSrcPath = suspicious._testSrcPath;
        _className = suspicious.classname();
        _methodName = suspicious.functionnameWithoutParam();
        _code = FileUtils.getCodeFromFile(_classSrcPath, suspicious.classname());
        _methodCode  = CodeUtils.getMethodString(_code, suspicious.functionnameWithoutParam());
        _errorLines = suspicious.errorLines();
        List<Integer> methodLines = CodeUtils.getSingleMethodLine(_code, _methodName, _errorLines.iterator().next());
        _methodStartLine = methodLines.get(0);
        _methodEndLine = methodLines.get(1);
    }

    public boolean fix(Map<String, List<String>> ifStrings){
        return fix(ifStrings, _errorLines);
    }

    public boolean fix(Map<String, List<String>> ifStrings, Set<Integer> errorLines){
        for (Map.Entry<String, List<String>> entry: ifStrings.entrySet()){
            for (int errorLine: errorLines){
                List<Integer> ifLines = getIfLine(errorLine);
                if (ifLines.size()!=2){
                    continue;
                }
                for (String ifString: entry.getValue()){
                    int startLine = ifLines.get(0);
                    int endLine = ifLines.get(1);
                    while (startLine < endLine) {
                        String lastLineString = CodeUtils.getLineFromCode(_code, startLine-1);
                        boolean result = false;
                        if (!LineUtils.isIfAndElseIfLine(lastLineString)) {
                            result = fixWithAddIf(startLine, endLine, getIfStatementFromString(ifString),entry.getKey(), false);
                        }
                        else {
                            String ifEnd = lastLineString.substring(lastLineString.lastIndexOf(')'));
                            lastLineString = lastLineString.substring(0, lastLineString.lastIndexOf(')'));
                            String ifStatement =lastLineString+ "&&" +getIfStringFromStatement(getIfStatementFromString(ifString)) + ifEnd;
                            result = fixWithAddIf(startLine-1, endLine, ifStatement,entry.getKey(),  true);
                            if (!result){
                                ifStatement =lastLineString+ "||" +getIfStringFromStatement(ifString) + ifEnd;
                                result = fixWithAddIf(startLine-1, endLine, ifStatement,entry.getKey(),  true);
                            }
                        }
                        if (result){
                            return true;
                        }
                        endLine --;
                    }
                }
            }
        }
        return false;
    }

    private String getIfStatementFromString(String ifString){
        String statement =  ifString.replace("if (","if (!(");
        statement += "){";
        return statement;
    }

    private String getIfStringFromStatement(String ifStatement){
        return ifStatement.substring(ifStatement.indexOf('(')+1, ifStatement.lastIndexOf(')'));
    }

    private boolean fixWithAddIf(int ifStartLine, int ifEndLine, String ifStatement,String testMessage, boolean replace){
        String testClassName = testMessage.split("#")[0];
        String testMethodName = testMessage.split("#")[1];
        int assertLine = Integer.valueOf(testMessage.split("#")[2]);

        File targetJavaFile = new File(FileUtils.getFileAddressOfJava(_classSrcPath, _className));
        File targetClassFile = new File(FileUtils.getFileAddressOfClass(_classpath, _className));
        File javaBackup = FileUtils.copyFile(targetJavaFile.getAbsolutePath(), FileUtils.tempJavaPath(_className,"MethodTwoFixer"));
        File classBackup = FileUtils.copyFile(targetClassFile.getAbsolutePath(), FileUtils.tempClassPath(_className,"MethodTwoFixer"));
        SourceUtils.insertIfStatementToSourceFile(targetJavaFile, ifStatement, ifStartLine, ifEndLine, replace);

        try {
            targetClassFile.delete();
            System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+_classpath+" "+ targetJavaFile.getAbsolutePath())));
        }
        catch (IOException e){
            FileUtils.copyFile(classBackup, targetClassFile);
            FileUtils.copyFile(javaBackup, targetJavaFile);
            return false;
        }
        if (!targetClassFile.exists()){ //编译不成功
            FileUtils.copyFile(classBackup, targetClassFile);
            FileUtils.copyFile(javaBackup, targetJavaFile);
            return false;
        }

        Asserts asserts = new Asserts(_classpath,_classSrcPath, _testClassPath, _testSrcPath, testClassName, testMethodName);
        int errAssertAfterFix = asserts.errorNum();
        int errAssertBeforeFix = _suspicious._assertsMap.get(testClassName+"#"+testMethodName).errorNum();
        System.out.print("Method 2 try patch: "+ifStatement);
        if (errAssertAfterFix < errAssertBeforeFix || errAssertAfterFix == 0) {
            System.out.println(" fix success");
            return true;
        }
        FileUtils.copyFile(classBackup, targetClassFile);
        FileUtils.copyFile(javaBackup, targetJavaFile);
        System.out.println(" fix fail");
        return false;
    }


    private List<Integer> getIfLine(int errorLine){
        int braceCount = 0;
        for (int i= errorLine-1; i>_methodStartLine; i--){
            String lineString = CodeUtils.getLineFromCode(_code, i);
            braceCount += CodeUtils.countChar(lineString, '{');
            braceCount -= CodeUtils.countChar(lineString, '}');
        }
        if (braceCount > 0){
            return getBraceArea(errorLine);
        }
        else {
            return Arrays.asList(errorLine-1, errorLine);
        }
    }

    private boolean changeIfArea(List<Integer> area, int errorLine){
        int startLine = area.get(0);
        int endLine = area.get(1);
        if (errorLine - startLine >= errorLine - endLine){
            area.set(1, endLine+1);
        }
        else {
            area.set(0, startLine-1);
        }
        return area.get(0) > _methodStartLine && area.get(1) < _methodEndLine;
    }

    private List<Integer> getBraceArea(int errorLine){
        List<Integer> result = new ArrayList<>();
        int bracket = 0;
        for (int i = 0; i < _methodEndLine-_methodStartLine; i++){
            String lineString = CodeUtils.getLineFromCode(_code, errorLine-i);
            if (lineString.contains("{") || lineString.contains("}")){
                if (bracket == 0){
                    result.add(errorLine-i+1);
                    break;
                }
            }
            bracket += CodeUtils.countChar(lineString, '}');
            bracket -= CodeUtils.countChar(lineString, '{');
        }
        bracket = 0;
        for (int i = 0; i<_methodEndLine-_methodStartLine; i++){
            String lineString = CodeUtils.getLineFromCode(_code, errorLine+i);
            if (result.size() != 0){
                if (lineString.contains("}")){
                    if (bracket == 0){
                        result.add(errorLine+i);
                        break;
                    }
                }
                bracket += CodeUtils.countChar(lineString, '{');
                bracket -= CodeUtils.countChar(lineString, '}');
            }
        }
        return result;
    }

    private String buildClasspath(List<String> additionalPath){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClassPath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += System.getProperty("path.separator");
        path += StringUtils.join(_suspicious._libPath,System.getProperty("path.separator"));
        return path;
    }



}
