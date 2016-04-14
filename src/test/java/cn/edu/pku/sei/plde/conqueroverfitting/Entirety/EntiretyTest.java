package cn.edu.pku.sei.plde.conqueroverfitting.Entirety;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundarySorter;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.*;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.sort.VariableSort;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.ShellUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.eclipse.jdt.core.dom.VariableDeclaration;
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
    public long startMili=System.currentTimeMillis();
    @Test
    public void testEntirety() throws Exception{

        String project = setWorkDirectory("Math", 5);
        Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath);
        List<Suspicious> suspiciouses = localization.getSuspiciousLite();
        suspiciousLoop(suspiciouses, project);
    }

    public void suspiciousLoop(List<Suspicious> suspiciouses, String project) {
        List<Suspicious> triedSuspicious = new ArrayList<>();
        for (Suspicious suspicious: suspiciouses){
            suspicious._libPath = libPath;
            boolean tried = false;
            for (Suspicious _suspicious: triedSuspicious){
                if (_suspicious._function.equals(suspicious._function) && _suspicious._classname.equals(suspicious._classname)){
                    tried = true;
                }
            }
            if (tried){
                continue;
            }
            try {
                if (fixSuspicious(suspicious, project)){
                    break;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            triedSuspicious.add(suspicious);
        }
    }


    public boolean fixSuspicious(Suspicious suspicious, String project) throws Exception{
        List<TraceResult> traceResults = suspicious.getTraceResult();
        Map<VariableInfo, List<String>> boundarys = BoundaryGenerator.generate(suspicious, traceResults);
        if (boundarys.size() == 0){
            return false;
        }
       List<String> ifStrings = getIfStrings(suspicious, boundarys, traceResults);
        if (JavaFixer.fixMethodOne(suspicious, ifStrings, project)){
            return isFixSuccess(suspicious, boundarys, project);
        }
        if (JavaFixer.fixMethodTwo(suspicious, ifStrings, project)){
            return isFixSuccess(suspicious, boundarys, project);
        }
        return false;
    }

    public boolean isFixSuccess(Suspicious suspicious, Map<VariableInfo,List<String>> boundarys, String project){
        System.out.println("Fix Success One Place");
        if (TestUtils.getFailTestNumInProject(project) > 0){
            Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath);
            List<Suspicious> suspiciouses = localization.getSuspiciousLite(false);
            suspiciousLoop(suspiciouses, project);
            return true;
        }
        else {
            System.out.println("Fix All Place Success");
            printCollectingMessage(suspicious, boundarys);
            return true;
        }
    }

    public void printCollectingMessage(Suspicious suspicious, Map<VariableInfo, List<String>> boundarys){
        System.out.println("True Test Num: "+suspicious.trueTestNums());
        System.out.println("True Assert Num: "+suspicious.trueAssertNums());
        List<String> sv = new ArrayList<>();
        for (Map.Entry<VariableInfo, List<String>> entry: boundarys.entrySet()){
            sv.addAll(entry.getValue());
        }
        System.out.println("Suspicious Variable: "+ sv);
        System.out.println("Cost Time: "+(System.currentTimeMillis()-startMili)/1000);
    }

    public List<String> getIfStrings(Suspicious suspicious, Map<VariableInfo, List<String>> boundarys, List<TraceResult> traceResults){
        String code = FileUtils.getCodeFromFile(suspicious._srcPath, suspicious.classname());
        int maxLine = 0;
        for (int errorLine: suspicious.errorLines()) {
            if (errorLine > maxLine) {
                maxLine = errorLine;
            }
        }
        String statement = CodeUtils.getMethodBodyBeforeLine(code, suspicious.functionnameWithoutParam(), maxLine);
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

        for (TraceResult traceResult: traceResults){
            if (traceResult.getTestResult()){
                return sorter.getIfStringFromBoundarys(changeVariableToIfStatement(sortedVariable, boundarys));
            }
        }
        return sorter.sortList(boundarys);
    }

    private List<List<String>> changeVariableToIfStatement(List<List<String>> variablesList, Map<VariableInfo, List<String>> boundarys){
        List<List<String>> results = new ArrayList<>();
        for (List<String> variables: variablesList){
            results.add(new ArrayList<String>());
            for (String variable: variables){
                for (Map.Entry<VariableInfo, List<String>> entry: boundarys.entrySet()){
                    if (entry.getKey().variableName.equals(variable) || entry.getKey().variableName.contains(variable+".")){
                        if (entry.getValue().size() == 1){
                            for (List<String> result: results){
                                result.addAll(entry.getValue());
                            }
                        }
                        else {
                            List<List<String>> oldResult = new ArrayList<>(results);
                            for (List<String> result: results){
                                for (String value: entry.getValue()){
                                    List<String> newResult = new ArrayList<>(result);
                                    newResult.add(value);
                                    results.add(newResult);
                                }
                            }
                            results.removeAll(oldResult);
                        }
                    }
                }
            }

        }
        return results;
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
        if ((projectName.equals("Lang") && (number == 7 || number ==13))){
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
