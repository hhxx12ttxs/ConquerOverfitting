package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.Utils;
import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.*;

/**
 * Created by yanrunfa on 16/3/30.
 */
public class ErrorLineTracer {
    public Asserts asserts;
    public String classname;
    public String methodName;
    public int methodStartLine;
    public Map<String,Integer> _commentedTestClass = new HashMap<>();

    public ErrorLineTracer(Asserts asserts, String classname, String methodName){
        this.asserts = asserts;
        this.classname = classname;
        this.methodName = methodName;

    }

    public List<Integer> trace(int defaultErrorLine){
        List<Integer> result = new ArrayList<>();
        String code = FileUtils.getCodeFromFile(asserts._srcPath, classname);
        methodStartLine = CodeUtils.getMethodStartLine(defaultErrorLine, code, methodName);
        result.addAll(errorLineInConstructor(code));
        result.addAll(errorLineInForLoop(code, methodName));

        if (asserts._assertNums == 1){
            defaultErrorLine = errorLineOutOfSwitch(defaultErrorLine, code);
            if (!result.contains(defaultErrorLine)){
                result.add(defaultErrorLine);
            }
            return result;
        }
        List<Integer> lines = getErrorLineFromAssert(asserts);
        for (int line: lines){
            if (line>0 && !result.contains(line)){
                result.add(line);
            }
        }
        if (lines.size()> 0){
            return result;
        }
        result.add(defaultErrorLine);
        return result;
    }

    private List<Integer> errorLineInForLoop(String code, String methodName){
        List<Integer> result = new ArrayList<>();
        String methodCode = CodeUtils.getMethodString(code, methodName, methodStartLine);
        Map<String, String> methodParams = CodeUtils.getMethodParamsFromDefine(methodCode, methodName);
        Map<String, String> arrayParams = new HashMap<>();
        for (Map.Entry<String, String> entry: methodParams.entrySet()){
            if (TypeUtils.isSimpleArray(entry.getValue())){
                arrayParams.put(entry.getKey(),entry.getValue());
            }
        }
        for (String line: methodCode.split("\n")){
            if (LineUtils.isParameterTraversalForLoop(line, new ArrayList<String>(arrayParams.keySet()))) {
                result.add(CodeUtils.getLineNumOfLineString(code, line, methodStartLine) + 1);
            }
        }
        return result;
    }



    private List<Integer> errorLineInConstructor(String code){
        if (CodeUtils.isConstructor(classname, methodName)) {
            return CodeUtils.getReturnLine(code, methodName, CodeUtils.getConstructorParamsCount(methodName));
        }
        return new ArrayList<>();
    }

    private int errorLineOutOfSwitch(int defaultErrorLine, String code){
        String errorLineString = CodeUtils.getLineFromCode(code, defaultErrorLine);
        String lastLineString = CodeUtils.getLineFromCode(code, defaultErrorLine-1);
        if (errorLineString.contains("return ") && lastLineString.contains("case ")){
            for (int i = defaultErrorLine-2; i> asserts._methodStartLine; i++){
                if (CodeUtils.getLineFromCode(code, i).contains("switch ")){
                    defaultErrorLine = i;
                }
            }
        }
        return defaultErrorLine;
    }

    private List<Integer> getErrorLineFromAssert(Asserts asserts){
        List<Integer> assertLines = asserts._errorLines;
        List<Integer> result = new ArrayList<>();
        if (assertLines.size() < 1){
            return result;
        }
        for (int assertLine: assertLines){
            List<Suspicious> suspiciouses = new ArrayList<>();
            File originJavaFile = new File(FileUtils.getFileAddressOfJava(asserts._testSrcPath, asserts._testClassname));
            File originClassFile = new File(FileUtils.getFileAddressOfClass(asserts._testClasspath,asserts._testClassname));
            File backupJavaFile = FileUtils.copyFile(originJavaFile.getAbsolutePath(), originJavaFile.getAbsolutePath()+".ErrorTraceBackup");
            File backupClassFile = FileUtils.copyFile(originClassFile.getAbsolutePath(), originClassFile.getAbsolutePath()+".ErrorTraceBackup");
            try {
                FileOutputStream outputStream = new FileOutputStream(originJavaFile);
                BufferedReader reader = new BufferedReader(new FileReader(backupJavaFile));
                String lineString = null;
                List<Integer> functionLine = FileUtils.getTestFunctionLineFromCode(FileUtils.getCodeFromFile(backupJavaFile),asserts._testMethodName);
                int beginLine = functionLine.get(0)+1;
                int endLine = functionLine.get(1);
                int line = 0;
                int tryLine = -1;
                List<Integer> commitedAfter = new ArrayList<>();
                List<Integer> assertDependences = asserts.dependenceOfAssert(assertLine);
                int bracketCount = 0;
                while ((lineString = reader.readLine()) != null) {
                    line++;
                    if (lineString.trim().startsWith("try")){
                        tryLine = line+1;
                    }
                    if (lineString.trim().contains("catch")){
                        tryLine = -1;
                    }
                    if (line >= beginLine && line <= endLine && !assertDependences.contains(line) && !LineUtils.isBoundaryLine(lineString)){
                        if (lineString.trim().startsWith("fail(") && tryLine != -1){
                            for (int i= tryLine; i< line; i++){
                                commitedAfter.add(i);
                            }
                        }
                        bracketCount+= CodeUtils.countChar(lineString,'(');
                        bracketCount-= CodeUtils.countChar(lineString,')');
                        lineString = "//"+lineString;
                    }
                    else if (bracketCount > 0){
                        lineString = "//"+lineString;
                        bracketCount+= CodeUtils.countChar(lineString,'(');
                        bracketCount -= CodeUtils.countChar(lineString, ')');
                    }
                    outputStream.write((lineString+"\n").getBytes());

                }
                outputStream.close();
                reader.close();
                for (int num: commitedAfter){
                    SourceUtils.commentCodeInSourceFile(originJavaFile, num);
                }
                System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+ asserts._testClasspath+" "+ originJavaFile.getAbsolutePath())));
                new File(System.getProperty("user.dir")+"/temp/"+assertLine).mkdirs();
                System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+System.getProperty("user.dir")+"/temp/"+assertLine+"/ "+ originJavaFile.getAbsolutePath())));
                _commentedTestClass.put(FileUtils.getFileAddressOfClass(System.getProperty("user.dir")+"/temp/"+assertLine,asserts._testClassname), assertLine);
                Localization localization = new Localization(asserts._classpath, asserts._testClasspath, asserts._testSrcPath, asserts._srcPath, asserts._testClassname);
                suspiciouses = localization.getSuspiciousLite(false);

            } catch (NotFoundException e){
                System.out.println("ERROR: Cannot Find Source File: "+ asserts._testClassname +" in Source Path: "+ asserts._testSrcPath);
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            originJavaFile.delete();
            backupJavaFile.renameTo(originJavaFile);
            originClassFile.delete();
            backupClassFile.renameTo(originClassFile);

            for (Suspicious suspicious: suspiciouses){
                if (suspicious.functionnameWithoutParam().equals(methodName) && suspicious.classname().equals(classname)){
                    result.add(suspicious.getDefaultErrorLine());
                    break;
                }
            }
        }
        return result;
    }

    private String buildClasspath(List<String> additionalPath){
        if (asserts._libPath.size()!=0){
            additionalPath = new ArrayList<>(additionalPath);
            additionalPath.addAll(asserts._libPath);
        }
        String path = "\"";
        path += asserts._classpath;
        path += System.getProperty("path.separator");
        path += asserts._testSrcPath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

}
