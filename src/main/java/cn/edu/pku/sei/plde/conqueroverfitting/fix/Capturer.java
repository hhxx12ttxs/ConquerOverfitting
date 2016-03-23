package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.slice.StaticSlice;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.*;
import com.gzoltar.core.GZoltar;
import com.gzoltar.core.instr.testing.TestResult;
import de.unisb.cs.st.javaslicer.slicing.Slicer;
import de.unisb.cs.st.javaslicer.tracer.TracerAgent;
import javassist.NotFoundException;

import org.apache.commons.lang.StringUtils;
import org.junit.runner.JUnitCore;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


/**
 * Created by yanrunfa on 16/2/16.
 */

public class Capturer {
    public final String _classpath;
    public final String _testclasspath;
    public final String _testsrcpath;
    public String _testClassName;
    public String _testMethodName;
    public String _fileaddress;
    public String _testTrace;
    public String _classCode;
    public String _functionCode;
    public int _errorLineNum;
    public int _assertLine = -1;
    public String _classname;
    public String _methodName;
    /**
     *
     * @param classpath The class path
     * @param testclasspath The test's class path
     * @param testsrcpath the test's source path
     */
    public Capturer(String classpath, String testclasspath, String testsrcpath){
        _classpath = classpath;
        _testclasspath = testclasspath;
        _testsrcpath = testsrcpath;

    }
    /**
     *
     * @param testClassName The test's class name to be fixed
     * @param testMethodName the name of test function
     * @return the fix string
     */
    public String getFixFrom(String testClassName, String testMethodName, String classname, String methodName){
        _testClassName = testClassName;
        _testMethodName = testMethodName;
        _classname = classname;
        _methodName = methodName;
        _fileaddress = _testsrcpath + System.getProperty("file.separator") + _testClassName.replace('.',System.getProperty("file.separator").charAt(0))+".java";
        try {
            return run();
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }


    public String getFixFrom(String testClassName, String testMethodName, int assertLine, String classname, String methodName){
        _assertLine = assertLine;

        return getFixFrom(testClassName, testMethodName, classname, methodName);
    }

    private String run() throws Exception{
        _testTrace = TestUtils.getTestTrace(_classpath, _testclasspath,_testClassName,_testMethodName);
        _classCode = FileUtils.getCodeFromFile(_fileaddress);
        _functionCode = FileUtils.getTestFunctionCodeFromCode(_classCode, _testMethodName);
        _errorLineNum = getErrorLineNumFromTestTrace();
        if (_assertLine == -1){
            return fixTest();
        }
        else {
            return getFixFromLine(_assertLine);
        }
    }

    private int getErrorLineNumFromTestTrace(){
        if (_testTrace == null){
            return -1;
        }
        String[] traces = _testTrace.split("\n");
        for (String trace: traces){
            if (trace.contains(_testClassName+"."+_testMethodName)){
                return Integer.valueOf(trace.substring(trace.lastIndexOf(':')+1, trace.lastIndexOf(')')));
            }
        }
        return -1;
    }

    private String fixTest() throws Exception{
        String functionBody = _functionCode.substring(_functionCode.indexOf('{')+1,_functionCode.lastIndexOf('}'));
        String[] functionLines = functionBody.split("\n");
        boolean annotationFlag = false;
        int errorLineIndex = 0;
        if (_errorLineNum > 0){
            String errorLine = _classCode.split("\n")[_errorLineNum -1].trim();
            for (int i=0; i< functionLines.length; i++){
                String functionLine = functionLines[i].replace("\n","").trim();
                if (functionLine.equals(errorLine)){
                    errorLineIndex = i;
                }
            }
        }
        String statements= ""; //for static slicing
        for (int i=errorLineIndex; i<functionLines.length; i++){
            String detectingLine = functionLines[i].replace("\n","").trim();
            //clean the annotation
            if (detectingLine.contains("*/")){
                detectingLine = detectingLine.substring(detectingLine.indexOf("*/")+1);
                annotationFlag = false;
            }
            if (annotationFlag || detectingLine.startsWith("//")){
                continue;
            }
            if (detectingLine.contains("//")){
                detectingLine = detectingLine.substring(0,detectingLine.indexOf("//"));
            }
            if (detectingLine.contains("/*")){
                detectingLine = detectingLine.substring(0,detectingLine.indexOf("/*"));
                annotationFlag = true;
            }

            if (detectingLine.contains("Exception") && detectingLine.contains("catch")){
                return exceptionProcessing(detectingLine);
            }
            if (detectingLine.contains("assert")){
                return assertProcessing(detectingLine, statements);
            }
            statements += detectingLine+"\n";
        }
        //No Assert And Throw Exception Found
        if (_functionCode.startsWith("(expected")){
            String expectedClass = _functionCode.substring(_functionCode.indexOf("=")+1,_functionCode.indexOf(")"));
            return "throw new " +expectedClass.replace(".class","").trim() + "();";
        }

        throw new Exception("No Fix Found for This Test");
    }

    private String getFixFromLine(int assertLine){
        String lineString = CodeUtils.getLineFromCode(_classCode, assertLine);
        if (lineString.startsWith("fail(")){
            for (int i = 1; i < _functionCode.split("\n").length; i++){
                String nextLine = CodeUtils.getLineFromCode(_classCode, assertLine+i);
                if (!nextLine.contains("Exception") || !nextLine.contains("catch")){
                    continue;
                }
                return exceptionProcessing(lineString);
            }
        }
        else if (lineString.contains("assert")){
            try {
                String functionBody = _functionCode.substring(_functionCode.indexOf('{')+1,_functionCode.lastIndexOf('}'));
                return assertProcessing(lineString, functionBody.split(lineString)[0]);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (_functionCode.startsWith("(expected")){
            String expectedClass = _functionCode.substring(_functionCode.indexOf("=")+1,_functionCode.indexOf(")"));
            return "throw new " +expectedClass.replace(".class","").trim() + "();";
        }
        else if (lineString.contains("(") && lineString.contains(")") && !lineString.contains("=")){
            String callMethod = lineString.substring(0, lineString.indexOf("(")).trim();
            if (_classCode.contains("void "+callMethod+"(")){
                _functionCode = FileUtils.getTestFunctionCodeFromCode(_classCode, callMethod);
                try {
                    return fixTest();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    private String exceptionProcessing(String exceptionLine){
        String exceptionName = exceptionLine.substring(exceptionLine.indexOf("(")+1, exceptionLine.indexOf(")")).trim().split(" ")[0];
        return "throw new " + exceptionName + "();";
    }

    private String assertProcessing(String assertLine, String statements) throws Exception{
        String assertType = assertLine.substring(0, assertLine.indexOf('('));
        List<String> parameters = CodeUtils.divideParameter(assertLine, 1);
        if (parameters.size() > 3 && parameters.size() <2){
            System.out.println(Arrays.toString(parameters.toArray()));
            throw new Exception("Function divideParameter Error!");
        }

        if (assertType.contains("assertEquals") || assertType.contains("assertSame")){
            String callExpression="";
            String returnExpression="";
            List<String> callParam;
            List<String> returnParam;
            String returnString;

            if (parameters.get(0).contains("(") && parameters.get(0).contains(")") && parameters.get(0).contains(_methodName)){
                callExpression = parameters.get(0);
                returnExpression = parameters.get(1);
            }
            else {
                callExpression = parameters.get(1);
                returnExpression = parameters.get(0);
            }
            callParam = CodeUtils.divideParameter(callExpression,1);
            if (callParam.size() == 0){
                callParam.add(callExpression);
            }
            returnParam = CodeUtils.divideParameter(returnExpression, 1);
            if (returnParam.size() == 0){
                returnParam.add(returnExpression);
            }
            returnString = "return "+returnExpression;
            if (callExpression.contains(".")){
                String testClass = callExpression.substring(0,callExpression.indexOf("."));
                if (returnExpression.contains(".")){
                    if (returnExpression.substring(0, returnExpression.indexOf(".")).equals(testClass)){
                        returnString = "return " + returnExpression.substring(returnExpression.indexOf(".")+1);
                    }

                }
            }
            //String attachLines = slicingProcess(returnParam, callParam, assertLine);
            String attachLines = staticSlicingProcess(returnParam, callParam, statements);
            return attachLines + returnString + ";";
        }
        else if (assertType.contains("assertNull")){
            return "return null;";
        }
        else if (assertType.contains("assertFalse")){
            return "return false;";
        }
        else if (assertType.contains("assertTrue")){
            return "return true;";
        }
        throw new Exception("Unknown assert type");
    }


    private String staticSlicingProcess(List<String> returnParam, List<String> callParam, String statements){
        for (int i=0; i<returnParam.size(); i++){
            if (StringUtils.isNumeric(returnParam.get(i))){
                returnParam.remove(i);
            }
        }
        for (int i=0; i<callParam.size(); i++){
            if (StringUtils.isNumeric(callParam.get(i))){
                callParam.remove(i);
            }
        }
        for (int i=0; i<callParam.size(); i++){
            if (returnParam.contains(callParam.get(i))){
                returnParam.remove(callParam.get(i));
            }
        }
        if (returnParam.size() == 0){
            return "";
        }
        String result = "";
        for (String param: returnParam){
            StaticSlice staticSlice = new StaticSlice(statements, param);
            String sliceResult = staticSlice.getSliceStatements();
            List<String> trueResult = new ArrayList<>();
            for (String line: sliceResult.split("\n")){
                if (AssertUtils.isAssertLine(line,_classCode)){
                    continue;
                }
                trueResult.add(line);
            }
            result += StringUtils.join(trueResult,"\n");
        }
        return result;
    }

    private String slicingProcess(List<String> returnParam, List<String> callParam, String assertLine) throws Exception{
        for (int i=0; i<returnParam.size(); i++){
            if (StringUtils.isNumeric(returnParam.get(i))){
                returnParam.remove(i);
            }
        }
        for (int i=0; i<callParam.size(); i++){
            if (StringUtils.isNumeric(callParam.get(i))){
                callParam.remove(i);
            }
        }
        for (int i=0; i<callParam.size(); i++){
           if (returnParam.contains(callParam.get(i))){
               returnParam.remove(callParam.get(i));
          }
        }
        if (returnParam.size() == 0){
            return "";
        }
        int assertLineNum = 0;
        String[] codeLines = _classCode.split("\n");
        for (int i=0;i<codeLines.length; i++){
            if (codeLines[i].contains(assertLine)) {
                assertLineNum = i+1;
            }
        }
        if (assertLineNum == 0){
            throw new NotFoundException("Cannot Found Assert Line :"+assertLine+" in Class "+_testClassName);
        }
        List<String> args = new ArrayList<String>();
        List<Integer> lineToBeAdd = new ArrayList<Integer>();
        String slicePath = Slicer.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && slicePath.charAt(0) == '/'){
            slicePath = slicePath.substring(1);
        }
        for (String var: returnParam){
            String[] arg = {"java -Xmx2g -jar",slicePath,"-p",System.getProperty("user.dir")+"/temp/"+"/test.trace",_testClassName+"."+_testMethodName+":"+assertLineNum+":{"+var+"}"};
            args.add(StringUtils.join(arg," ")+'\n');
        }
        String slicingResult = slicing(_classpath,_testclasspath,_testClassName, args);
        //System.out.print(slicingResult);
        for (String var: returnParam){
            if (slicingResult.contains("Error occurred during initialization of VM")){
                throw new Exception("Slice Initialization Error: \n"+slicingResult);
            }
            if (slicingResult.contains("There was an error while tracing:")){
                throw new Exception("There was an error occurs because of slice, Maybe you should use linux or mac instead of windows.");
            }
            String[] sliceResult = slicingResult.substring(slicingResult.indexOf("{"+var+"}"), slicingResult.indexOf("Computation took",slicingResult.indexOf("{"+var+"}"))).split("\n");
            for (String line: sliceResult){
                if (line.contains(_testClassName+"."+_testMethodName)){
                    lineToBeAdd.add(Integer.valueOf(line.substring(line.indexOf(':')+1,line.indexOf(' '))));
                }
            }
        }
        String result = "";
        lineToBeAdd = new ArrayList<Integer>(new HashSet<Integer>(lineToBeAdd));
        for (int line: lineToBeAdd){
            result += _classCode.split("\n")[line-1].trim() + "\n";
        }
        return result;
    }

    private String slicing(String classpath, String testclasspath, String testClassName, List<String> sliceArgs) throws Exception{
        String tracePath = TracerAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        String junitPath = JUnitCore.class.getProtectionDomain().getCodeSource().getLocation().getFile();

        //small bug of get jar path from class in windows
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && tracePath.charAt(0) == '/' && junitPath.charAt(0) == '/'){
            tracePath = tracePath.substring(1);
            junitPath = junitPath.substring(1);
        }
        String[] args = {
                "java", "-javaagent:"+tracePath+"=tracefile:"+System.getProperty("user.dir")+"/temp/"+"/test.trace",
                "-cp","\""+classpath+System.getProperty("path.separator")+testclasspath+System.getProperty("path.separator")+junitPath+"\"",
                "org.junit.runner.JUnitCore",
                testClassName
        };
        String arg = StringUtils.join(args, ' ')+'\n';
        sliceArgs.add(arg);
        String result = ShellUtils.shellRun(sliceArgs);
        File traceFile = new File(System.getProperty("user.dir")+"/temp/"+"/test.trace");
        if (traceFile.exists()){
            traceFile.deleteOnExit();
        }
        return result;
    }




}

