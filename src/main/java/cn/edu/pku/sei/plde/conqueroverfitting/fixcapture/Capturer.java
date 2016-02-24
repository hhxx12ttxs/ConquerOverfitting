package cn.edu.pku.sei.plde.conqueroverfitting.fixcapture;

import cn.edu.pku.sei.plde.conqueroverfitting.slice.StaticSlice;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.ShellUtils;
import com.gzoltar.core.GZoltar;
import com.gzoltar.core.instr.testing.TestResult;
import com.sun.org.apache.bcel.internal.generic.LUSHR;
import de.unisb.cs.st.javaslicer.slicing.Slicer;
import de.unisb.cs.st.javaslicer.tracer.TracerAgent;
import javassist.NotFoundException;

import org.apache.commons.lang.StringUtils;
import org.junit.runner.JUnitCore;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Created by yanrunfa on 16/2/16.
 */

public class Capturer {
    public final String _classpath;
    public final String _testclasspath;
    public final String _testsrcpath;
    public String _classname;
    public String _functionname;
    public String _fileaddress;
    public String _testTrace;
    public String _classCode;
    public String _functionCode;
    public int _errorLineNum;
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
     * @param classname The test's class name to be fixed
     * @param functionname the name of test function
     * @return the fix string
     */
    public String getFixFrom(String classname, String functionname) throws Exception{
        _classname = classname;
        _functionname = functionname;
        _fileaddress = _testsrcpath + System.getProperty("file.separator") + _classname.replace('.',System.getProperty("file.separator").charAt(0))+".java";
        return run();
    }

    private String run() throws Exception{
        _testTrace = runTest();
        _classCode =  getCodeFromFile(_fileaddress);
        _functionCode = getFunctionCodeFromCode(_classCode, _functionname);
        _errorLineNum = getErrorLineNumFromTestTrace();
        return fixTest();
    }

    private int getErrorLineNumFromTestTrace(){
        if (_testTrace == null){
            return -1;
        }
        String[] traces = _testTrace.split("\n");
        for (String trace: traces){
            if (trace.contains(_classname+"."+_functionname)){
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

    private String exceptionProcessing(String exceptionLine){
        String exceptionName = exceptionLine.substring(exceptionLine.indexOf("(")+1, exceptionLine.indexOf(")")).trim().split(" ")[0];
        return "throw new " + exceptionName + "();";
    }

    private String assertProcessing(String assertLine, String statements) throws Exception{
        String assertType = assertLine.substring(0, assertLine.indexOf('('));
        List<String> parameters = divideParameter(assertLine, 1);
        if (parameters.size() != 3){
            throw new Exception("Function divideParameter Error!");
        }

        if (assertType.contains("assertEquals")){
            String callExpression="";
            String returnExpression="";
            List<String> callParam;
            List<String> returnParam;
            String returnString;

            if (parameters.get(0).contains("(") && parameters.get(0).contains(")")){
                callExpression = parameters.get(0);
                returnExpression = parameters.get(1);
            }
            else {
                callExpression = parameters.get(1);
                returnExpression = parameters.get(0);
            }
            callParam = divideParameter(callExpression,1);
            returnParam = divideParameter(returnExpression, 1);
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



    private String runTest() throws Exception {
        ArrayList<String> classpaths = new ArrayList<String>();
        classpaths.add(_classpath);
        classpaths.add(_testclasspath);
        GZoltar gzoltar = new GZoltar(System.getProperty("user.dir"));
        gzoltar.setClassPaths(classpaths);
        gzoltar.addPackageNotToInstrument("org.junit");
        gzoltar.addPackageNotToInstrument("junit.framework");
        gzoltar.addTestPackageNotToExecute("junit.framework");
        gzoltar.addTestPackageNotToExecute("org.junit");
        gzoltar.addTestToExecute(_classname);
        gzoltar.addClassNotToInstrument(_classname);
        try{
            gzoltar.run();
        } catch (NullPointerException e){
            throw new NotFoundException("Test Class " + _classname +  " No Found in Test Class Path " + _testclasspath);
        }
        List<TestResult> testResults = gzoltar.getTestResults();
        for (TestResult testResult: testResults){
            if (testResult.getName().substring(testResult.getName().lastIndexOf('#')+1).equals(_functionname)){
                return testResult.getTrace();
            }
        }
        throw new NotFoundException("No Test Named "+_functionname + " Found in Test Class " + _classname);
    }



    private static String getCodeFromFile(String fileaddress) throws Exception{
        try {
            FileInputStream stream = new FileInputStream(new File(fileaddress));
            byte[] b=new byte[stream.available()];
            int len = stream.read(b);
            if (len <= 0){
                throw new IOException("Source code file "+fileaddress+" read fail!");
            }
            stream.close();
            return new String(b);
        } catch (Exception e){
            System.out.println(e.getMessage());
            throw e;
        }
    }




    private static List<String> divideTestFunction(String code){
        List<String> result = new ArrayList<String>();
        String[] items = code.split("public void");
        for (int j = 1; j<items.length; j++){
            String item = items[j];
            int startPoint = item.indexOf('{')+1;
            int braceCount = 1;
            for (int i=startPoint; i<item.length();i++){
                if (item.charAt(i) == '}'){
                    if (--braceCount == 0){
                        result.add(item.substring(0, i+1));
                        break;
                    }
                }
                if (item.charAt(i) == '{'){
                    braceCount++;
                }
            }
        }
        return result;
    }


    private static List<String> divideParameter(String line, int level){
        line = line.replace(" ", "");
        List<String> result = new ArrayList<String>();
        int bracketCount = 0;
        int startPoint = 0;
        for (int i=0;i<line.length();i++){
            char ch = line.charAt(i);
            if (ch == ',' && bracketCount <= level){
                if (startPoint != i ){
                    result.add(line.substring(startPoint,i));
                }
                startPoint = i+1;
            }
            else if (ch == '('){
                if (++bracketCount <= level){
                    startPoint = i + 1;
                }
            }
            else if (ch == '['){
                if (++bracketCount <= level){
                    if (startPoint != i){
                        result.add(line.substring(startPoint,i));
                    }
                    startPoint = i + 1;
                }
            }
            else if (ch == ')' || ch == ']'){
                if (bracketCount-- <= level){
                    if (startPoint != i){
                        result.add(line.substring(startPoint,i));
                    }
                    startPoint = i + 1;
                }
            }
            else if (ch == '+' || ch == '-' || ch == '/' || ch == '*') {
                if (bracketCount < level) {
                    if (startPoint != i) {
                        result.add(line.substring(startPoint, i));
                    }
                    startPoint = i + 1;
                }
            }
        }
        return result;
    }

    private static String getFunctionCodeFromCode(String code, String targetFunctionName) throws NotFoundException{
        if (code.contains("@Test")){
            String[] tests = code.split("@Test");
            for (String test: tests){
                if (test.contains("public void "+targetFunctionName+"()")){
                    return test;
                }
            }
        }
        else {
            List<String> tests = divideTestFunction(code);
            for (String test: tests){
                if (test.trim().startsWith(targetFunctionName+"()")){
                    return "public void"+ test.trim();
                }
            }
        }

        throw new NotFoundException("Target function: "+ targetFunctionName+ " No Found");
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
            result += staticSlice.getSliceStatements();
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
            throw new NotFoundException("Cannot Found Assert Line :"+assertLine+" in Class "+_classname);
        }
        List<String> args = new ArrayList<String>();
        List<Integer> lineToBeAdd = new ArrayList<Integer>();
        String slicePath = Slicer.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (System.getProperty("os.name").toLowerCase().startsWith("win") && slicePath.charAt(0) == '/'){
            slicePath = slicePath.substring(1);
        }
        for (String var: returnParam){
            String[] arg = {"java -Xmx2g -jar",slicePath,"-p",System.getProperty("user.dir")+"/temp/"+"/test.trace",_classname+"."+_functionname+":"+assertLineNum+":{"+var+"}"};
            args.add(StringUtils.join(arg," ")+'\n');
        }
        String slicingResult = slicing(_classpath,_testclasspath,_classname, args);
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
                if (line.contains(_classname+"."+_functionname)){
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

    private String slicing(String classpath, String testclasspath, String classname, List<String> sliceArgs) throws Exception{
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
                classname
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

