package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner;
import com.gzoltar.core.GZoltar;
import com.gzoltar.core.instr.testing.TestResult;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/3/8.
 */
public class TestUtils {
    /**
     *
     * @param classpath
     * @param testPath
     * @param classname
     * @return
     */
    public static String getTestTrace(List<String> classpath, String testPath, String classname, String functionname) throws NotFoundException{
        ArrayList<String> classpaths = new ArrayList<String>();
        for (String path: classpath){
            classpaths.add(path);
        }
        classpaths.add(testPath);
        GZoltar gzoltar;
        try {
            gzoltar = new GZoltar(System.getProperty("user.dir"));
            gzoltar.setClassPaths(classpaths);
            gzoltar.addPackageNotToInstrument("org.junit");
            gzoltar.addPackageNotToInstrument("junit.framework");
            gzoltar.addTestPackageNotToExecute("junit.framework");
            gzoltar.addTestPackageNotToExecute("org.junit");
            gzoltar.addTestToExecute(classname);
            gzoltar.addClassNotToInstrument(classname);
            gzoltar.run();
        } catch (NullPointerException e){
            throw new NotFoundException("Test Class " + classname +  " No Found in Test Class Path " + testPath);
        } catch (IOException e){
            return "";
        }
        List<TestResult> testResults = gzoltar.getTestResults();
        for (TestResult testResult: testResults){
            if (testResult.getName().substring(testResult.getName().lastIndexOf('#')+1).equals(functionname)){
                return testResult.getTrace();
            }
        }
        throw new NotFoundException("No Test Named "+functionname + " Found in Test Class " + classname);
    }
    public static String getTestTrace(String classpath, String testPath, String classname, String functionname) throws NotFoundException{
        return getTestTrace(Arrays.asList(classpath),testPath, classname, functionname);
    }


    public static String getTestTraceFromJunit(String classpath, String testPath, String className, String methodName) {
        String[] arg = {"java","-cp",buildClasspath(classpath, testPath, new ArrayList<String>(), new ArrayList<String>(Arrays.asList(PathUtils.getJunitPath()))) ,"cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner", className+"#"+methodName};
        try {
            return ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        } catch (IOException e){
            return null;
        }
    }

    public static String getTestTraceFromIntellij(String classpath, String testPath, List<String> libPath, String className, String methodName){
        String classpaths = buildClasspath(classpath, testPath, libPath, new ArrayList<String>(Arrays.asList(PathUtils.getJunitPath(), PathUtils.getIntellijJunutPath(), PathUtils.getIntellijAppMainPath())));
        String[] arg = {"java","-Didea.launcher.port=7533","-cp",classpaths,"com.intellij.rt.execution.application.AppMain", "com.intellij.rt.execution.junit.JUnitStarter -ideVersion5 -junit3", className+","+methodName};
        try {
            return ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        } catch (IOException e){
            return null;
        }
    }

    public static String getTestTraceFromJunit(String classpath, String testPath,List<String> libPath,  String className, String methodName) {

        String[] arg = {"java","-cp",buildClasspath(classpath, testPath, libPath, new ArrayList<String>(Arrays.asList(PathUtils.getJunitPath()))) ,"cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner", className+"#"+methodName};
        try {
            return ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        } catch (IOException e){
            return null;
        }
    }


    private static String buildClasspath(String classpath, String testClasspath, List<String> libPaths, List<String> additionalPath){
        if (libPaths.size()!=0){
            additionalPath = new ArrayList<>(additionalPath);
            additionalPath.addAll(libPaths);
        }
        String path = "\"";
        path += classpath;
        path += System.getProperty("path.separator");
        path += testClasspath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }
}
