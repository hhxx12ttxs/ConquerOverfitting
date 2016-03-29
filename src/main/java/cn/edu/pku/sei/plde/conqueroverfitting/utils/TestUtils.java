package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.instr.testing.TestResult;
import javassist.NotFoundException;

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
}
