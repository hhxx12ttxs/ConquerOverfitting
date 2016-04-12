package cn.edu.pku.sei.plde.conqueroverfitting.Entirety;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundarySorter;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.MethodTwoFixer;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.ReturnCapturer;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.MethodOneFixer;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.Patch;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.sort.VariableSort;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.ShellUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class EntiretyTest {
    private final  String PATH_OF_DEFECTS4J = "/home/yanrunfa/Documents/defects4j/tmp/";
    private String classpath = System.getProperty("user.dir")+"/project/classpath/";
    private String classSrc = System.getProperty("user.dir")+"/project/classSrc/";
    private String testClasspath = System.getProperty("user.dir")+"/project/testClasspath";
    private String testClassSrc = System.getProperty("user.dir")+"/project/testClassSrc/";
    private List<String> libPath = new ArrayList<>();

    @Test
    public void testEntirety() throws Exception{
        String project = setWorkDirectory("Closure", 1);
        Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath);
        List<Suspicious> suspiciouses = localization.getSuspiciousLite();
        for (Suspicious suspicious: suspiciouses){
            suspicious._libPath = libPath;
            if (fixSuspicious(suspicious, project)){
                break;
            }
        }
    }

    public boolean fixSuspicious(Suspicious suspicious, String project) throws Exception{
        List<TraceResult> traceResults = suspicious.getTraceResult();
        Map<VariableInfo, List<String>> boundarys = BoundaryGenerator.generate(suspicious, traceResults);
        if (boundarys.size() == 0){
            return false;
        }
        String code = FileUtils.getCodeFromFile(suspicious._srcPath, suspicious.classname());
        for (int errorLine: suspicious.errorLines()){
            String statement = CodeUtils.getMethodBodyBeforeLine(code, suspicious.functionnameWithoutParam(), errorLine);
            Set<String> variables = new HashSet<>();
            for (Map.Entry<VariableInfo, List<String>> entry: boundarys.entrySet()){
                if (entry.getKey().variableName.endsWith(".null") || entry.getKey().variableName.endsWith(".Comparable")){
                    variables.add(entry.getKey().variableName.substring(0, entry.getKey().variableName.lastIndexOf(".")));
                }
                else {
                    variables.add(entry.getKey().variableName);
                }
            }
            VariableSort variableSort = new VariableSort(variables, statement);
            List<List<String>> sortedVariable = variableSort.getSortVariable();
            BoundarySorter sorter = new BoundarySorter(suspicious, classSrc);
            List<String> ifStrings;
            if (sortedVariable.size() == 1 && variables.size() > 2){
                ifStrings = sorter.sortList(boundarys);
            }
            else {
                ifStrings = sorter.getIfStringFromBoundarys(boundarys.values());
            }
            //return fixMethodOne(suspicious, ifStrings);
            if (fixMethodTwo(suspicious, ifStrings, project)){
                System.out.println("Fix Success One Place");
                if (TestUtils.getFailTestNumInProject(project) > 0){
                    return false;
                }
                else {
                    System.out.println("Fix All Place Success");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean fixMethodTwo(Suspicious suspicious, List<String> ifStrings, String project) throws Exception{
        MethodTwoFixer fixer = new MethodTwoFixer(suspicious);
        if (fixer.fix(ifStrings)){
            return true;
        }
        else {
            System.out.println("Fix fail, Try next suspicious...");
            return false;
        }
    }

    public boolean fixMethodOne(Suspicious suspicious, List<String> ifStrings, String project) throws Exception{
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
        return fixSuspicious(suspicious, project);
    }



    public String setWorkDirectory(String projectName, int number){
        File projectDir = new File(System.getProperty("user.dir")+"/project/");
        FileUtils.deleteDirNow(projectDir.getAbsolutePath());
        if (!projectDir.exists()){
            projectDir.mkdirs();
        }
        String project = projectName+"-"+number;
        /* 四个整个项目需要的参数 */

        if ((projectName.equals("Math") && number>=85) || (projectName.equals("Lang") && (number == 39 || number == 49))){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/target/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/target/test-classes/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/java/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/src/test/";
            return project;
        }

        if ((projectName.equals("Lang") && number == 55)){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/target/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/target/tests/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/java/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/src/test/";
            return project;
        }
        if ((projectName.equals("Lang") && number == 7)){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/target/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/target/tests/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/main/java/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/src/test/java/";
            return project;
        }
        if (projectName.equals("Time") && number == 3){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/target/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/target/test-classes/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/main/java/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/src/test/java/";
            FileUtils.deleteDirNow(System.getProperty("user.dir")+"/src/test/resources");
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/src/test/resources/",System.getProperty("user.dir")+"/src/test");
            return project;
        }
        if (projectName.equals("Time")){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/build/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/build/tests/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/main/java/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/src/test/java/";
            FileUtils.deleteDirNow(System.getProperty("user.dir")+"/src/test/resources");
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/src/test/resources/",System.getProperty("user.dir")+"/src/test");
            return project;
        }
        //Math,Time
        if (projectName.equals("Math") || projectName.equals("Lang")){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/target/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/target/test-classes/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/main/java/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/src/test/java/";
            return project;
        }
        if (projectName.equals("Closure")){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/build/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/build/test/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/test/";
            File libPkg = new File(projectDir.getAbsolutePath()+"/"+project+"/lib/");
            for (String p: libPkg.list()){
                if (p.endsWith(".jar")){
                    libPath.add(libPkg.getAbsolutePath()+"/"+p);
                }
            }
            libPkg = new File(projectDir.getAbsolutePath()+"/"+project+"/build/lib/");
            for (String p: libPkg.list()){
                if (p.endsWith(".jar")){
                    libPath.add(libPkg.getAbsolutePath()+"/"+p);
                }
            }
            return project;
        }
        if (projectName.equals("Chart")){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/build/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/build-tests/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/source/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/tests/";
            File libPkg = new File(PATH_OF_DEFECTS4J+project+"/lib/");
            for (String p: libPkg.list()){
                if (p.endsWith(".jar")){
                    libPath.add(libPkg.getAbsolutePath()+"/"+p);
                }
            }
            return project;
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
        return project;
    }

    @Test
    public void testDefects4jTest(){
        String projectName = "Time-3";
        System.out.println(TestUtils.getFailTestNumInProject(projectName));
    }

}
