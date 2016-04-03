package cn.edu.pku.sei.plde.conqueroverfitting.Entirety;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundarySorter;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.MethodTwoFixer;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.ReturnCapturer;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.MethodOneFixer;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.Patch;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.junit.Test;

import java.io.File;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class EntiretyTest {
    private final  String PATH_OF_DEFECTS4J = "/Users/yanrunfa/Documents/defects4j/tmp/";
    private String classpath = System.getProperty("user.dir")+"/project/classpath/";
    private String classSrc = System.getProperty("user.dir")+"/project/classSrc/";
    private String testClasspath = System.getProperty("user.dir")+"/project/testClasspath";
    private String testClassSrc = System.getProperty("user.dir")+"/project/testClassSrc/";
    private List<String> libPath = new ArrayList<>();
    @Test

    public void testEntirety() throws Exception{
        setWorkDirectory("Time", 9);
        Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath);
        List<Suspicious> suspiciouses = localization.getSuspiciousLite();

        for (Suspicious suspicious: suspiciouses){
            suspicious._libPath = libPath;
            if (fixSuspicious(suspicious)){
                break;
            }
        }
    }

    public boolean fixSuspicious(Suspicious suspicious) throws Exception{
        List<TraceResult> traceResults = suspicious.getTraceResult();
        Map<VariableInfo, String> boundarys = BoundaryGenerator.generate(suspicious, traceResults);
        if (boundarys.size() == 0){
            return false;
        }
        BoundarySorter sorter = new BoundarySorter(suspicious, classSrc);
        List<String> ifStrings = sorter.sort(boundarys);
        //return fixMethodOne(suspicious, ifStrings);
        return fixMethodTwo(suspicious, ifStrings);
    }

    public boolean fixMethodTwo(Suspicious suspicious, List<String> ifStrings) throws Exception{
        MethodTwoFixer fixer = new MethodTwoFixer(suspicious);
        if (fixer.fix(ifStrings)){
            System.out.println("Fix success");
            return true;
        }
        else {
            System.out.println("Fix fail, Try next suspicious...");
            return false;
        }
    }

    public boolean fixMethodOne(Suspicious suspicious, List<String> ifStrings) throws Exception{
        ReturnCapturer fixCapturer = new ReturnCapturer(classpath,classSrc, testClasspath, testClassSrc);
        MethodOneFixer methodOneFixer = new MethodOneFixer(suspicious);
        for (String test: suspicious.getFailedTest()){
            String testClassName = test.split("#")[0];
            String testMethodName = test.split("#")[1];
            List<Integer> errorLine = suspicious._errorLineMap.get(test);

            for (int assertLine: suspicious._assertsMap.get(test)._errorLines){
                String fixString = fixCapturer.getFixFrom(testClassName, testMethodName, assertLine, suspicious.classname(), suspicious.functionnameWithoutParam());
                if (suspicious._isConstructor && fixString.contains("return")){
                    continue;
                }
                if (fixString.equals("")){
                    continue;
                }
                Patch patch = new Patch(testClassName, testMethodName, suspicious.classname(), errorLine, ifStrings, fixString);
                boolean result = methodOneFixer.addPatch(patch);
                if (result){
                    break;
                }
            }
        }
        int finalErrorNums = methodOneFixer.fix();
        if (finalErrorNums == -1){
            System.out.println("Fix fail, Try next suspicious...");
            return false;
        }
        if (finalErrorNums == 0){
            System.out.println("Fix success");
            return true;
        }
        return fixSuspicious(suspicious);
    }



    public void setWorkDirectory(String projectName, int number){
        File projectDir = new File(System.getProperty("user.dir")+"/project/");
        FileUtils.deleteDirNow(projectDir.getAbsolutePath());
        if (!projectDir.exists()){
            projectDir.mkdirs();
        }
        String project = projectName+"-"+number;
        /* 四个整个项目需要的参数 */

        if ((projectName.equals("Math") && number>=86) || (projectName.equals("Lang") && number == 39)){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/target/classes",classpath);
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/target/test-classes",testClasspath);
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J + project+"/src/java", classSrc);
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J + project +"/src/test", testClassSrc);
        }

        //Math,Time
        if (projectName.equals("Math") || projectName.equals("Time") || projectName.equals("Lang")){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/target/classes",classpath);
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/target/test-classes",testClasspath);
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J + project+"/src/main/java", classSrc);
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J + project +"/src/test/java", testClassSrc);
            return;
        }
        if (projectName.equals("Closure")){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/build/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/build/test/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/test/";
            File libPkg = new File(projectDir.getAbsolutePath()+"/"+project+"/lib/");
            for (String p: libPkg.list()){
                libPath.add(libPkg.getAbsolutePath()+"/"+p);
            }
        }

        //Closure
        //String classpath = PATH_OF_DEFECTS4J+project+"/build/classes";              //项目的.class文件路径
        //String testClasspath  = PATH_OF_DEFECTS4J+project+"/build/test";    //项目的test的.class文件路径
        //String classSrc = PATH_OF_DEFECTS4J + project+"/src";              //项目的源代码路径
        //String testClassSrc = PATH_OF_DEFECTS4J + project +"/test";///java"; //项目的test的源代码路径


        //Math 99,86...
        //String classpath = PATH_OF_DEFECTS4J+"Math-"+i+"/target/classes";              //项目的.class文件路径
        //String testClasspath  = PATH_OF_DEFECTS4J+"Math-"+i+"/target/test-classes";    //项目的test的.class文件路径
        //String classSrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/java";              //项目的源代码路径
        //String testClassSrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test";///java"; //项目的test的源代码路径


        //Lang
        //String classpath = PATH_OF_DEFECTS4J+"Lang-"+i+"/target/classes";              //项目的.class文件路径
        //String testClasspath  = PATH_OF_DEFECTS4J+"Lang-"+i+"/target/test-classes";    //项目的test的.class文件路径
        //String classSrc = PATH_OF_DEFECTS4J + "Lang-"+i+"/src/main/java";              //项目的源代码路径
        //String testClassSrc = PATH_OF_DEFECTS4J + "Lang-"+i+"/src/test/java";///java";          //项目的test的源代码路径

        //Lang49
        //String classpath = PATH_OF_DEFECTS4J+"Lang-"+i+"/target/classes";              //项目的.class文件路径
        //String testClasspath  = PATH_OF_DEFECTS4J+"Lang-"+i+"/target/test-classes";    //项目的test的.class文件路径
        //String classSrc = PATH_OF_DEFECTS4J + "Lang-"+i+"/src/java";              //项目的源代码路径
        //String testClassSrc = PATH_OF_DEFECTS4J + "Lang-"+i+"/src/test/";///java";          //项目的test的源代码路径

        //Chart
        //String classpath = PATH_OF_DEFECTS4J+"Chart-"+i+"/build";              //项目的.class文件路径
        //String testClasspath  = PATH_OF_DEFECTS4J+"Chart-"+i+"/build-tests";    //项目的test的.class文件路径
        //String classSrc = PATH_OF_DEFECTS4J + "Chart-"+i+"/source";              //项目的源代码路径
        //String testClassSrc = PATH_OF_DEFECTS4J + "Chart-"+i+"/tests";///java";          //项目的test的源代码路径

    }
}
