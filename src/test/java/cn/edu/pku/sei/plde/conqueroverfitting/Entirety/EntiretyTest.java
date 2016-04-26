package cn.edu.pku.sei.plde.conqueroverfitting.Entirety;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundarySorter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.*;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.sort.VariableSort;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.SearchBoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.joda.convert.FromString;
import org.junit.Test;

import java.io.File;
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
    public List<Suspicious> triedSuspicious = new ArrayList<>();
    @Test
    public void testEntirety() throws Exception{
        String project = setWorkDirectory("Chart",26);
        Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath);
        List<Suspicious> suspiciouses = localization.getSuspiciousLite();
        suspiciousLoop(suspiciouses, project);
    }

    public void suspiciousLoop(List<Suspicious> suspiciouses, String project) {
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
        SuspiciousFixer fixer = new SuspiciousFixer(suspicious, project);
        if (fixer.mainFixProcess()){
            return isFixSuccess(suspicious, fixer.boundarysMap, project);
        }
        return false;
    }

    public boolean isFixSuccess(Suspicious suspicious, Map<ExceptionVariable,List<String>> boundarys, String project){
        System.out.println("Fix Success One Place");
        printCollectingMessage(suspicious, boundarys);
        if (TestUtils.getFailTestNumInProject(project) > 0){
            Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath);
            List<Suspicious> suspiciouses = localization.getSuspiciousLite(false);
            if (suspiciouses.size() == 0){
                return false;
            }
            suspiciousLoop(suspiciouses, project);
            return true;
        }
        else {
            System.out.println("Fix All Place Success");
            return true;
        }

    }

    public void printCollectingMessage(Suspicious suspicious, Map<ExceptionVariable, List<String>> boundarys){
        System.out.println("True Test Num: "+suspicious.trueTestNums());
        System.out.println("True Assert Num: "+suspicious.trueAssertNums());
        List<String> sv = new ArrayList<>();
        for (Map.Entry<ExceptionVariable, List<String>> entry: boundarys.entrySet()){
            sv.addAll(entry.getValue());
        }
        System.out.println("Suspicious Variable: "+ sv);
        System.out.println("Cost Time: "+(System.currentTimeMillis()-startMili)/1000);
    }


    public String setWorkDirectory(String projectName, int number){
        libPath.add(FromString.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        libPath.add(EasyMock.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        libPath.add(IOUtils.class.getProtectionDomain().getCodeSource().getLocation().getFile());
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
        if ((projectName.equals("Lang") && (number ==13))){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/target/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/target/tests/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/main/java/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/src/test/java/";
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/src/test/resources/",System.getProperty("user.dir")+"/src/test");
            return project;
        }
        if (projectName.equals("Time") && (number == 3||number == 9)){
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
            classpath = projectDir.getAbsolutePath()+"/"+project +"/target/classes/";
            testClasspath = projectDir.getAbsolutePath()+"/"+project +"/target/test-classes/";
            classSrc = projectDir.getAbsolutePath()+"/"+project +"/src/main/java/";
            testClassSrc = projectDir.getAbsolutePath()+"/"+project +"/src/test/java/";
            FileUtils.deleteDirNow(System.getProperty("user.dir")+"/src/test/resources");
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/src/test/resources/",System.getProperty("user.dir")+"/src/test/resources/");
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
            FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/src/test/resources/",System.getProperty("user.dir")+"/src/test");
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
            if (libPkg.list() != null){
                for (String p: libPkg.list()){
                    if (p.endsWith(".jar")){
                        libPath.add(libPkg.getAbsolutePath()+"/"+p);
                    }
                }
            }
            return project;
        }
        return project;
    }

}
