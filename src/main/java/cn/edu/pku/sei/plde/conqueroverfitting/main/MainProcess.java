package cn.edu.pku.sei.plde.conqueroverfitting.main;

import cn.edu.pku.sei.plde.conqueroverfitting.fix.SuspiciousFixer;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.joda.convert.FromString;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/4/23.
 */
public class MainProcess {

    private String PATH_OF_DEFECTS4J = "/Users/yanrunfa/Documents/defects4j/tmp/";
    private String classpath = System.getProperty("user.dir")+"/project/classpath/";
    private String classSrc = System.getProperty("user.dir")+"/project/classSrc/";
    private String testClasspath = System.getProperty("user.dir")+"/project/testClasspath";
    private String testClassSrc = System.getProperty("user.dir")+"/project/testClassSrc/";
    private List<String> libPath = new ArrayList<>();
    public long startMili=System.currentTimeMillis();
    public List<Suspicious> triedSuspicious = new ArrayList<>();

    private long startLine;

    public MainProcess(String path){
        if (!path.endsWith("/")){
            path += "/";
        }
        PATH_OF_DEFECTS4J = path;
    }

    public boolean mainProcess(String projectType, int projectNumber) throws Exception{
        String project = setWorkDirectory(projectType,projectNumber);
        startLine = System.currentTimeMillis();
        libPath.add(FromString.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        if (!checkProjectDirectory()){
            System.out.println("Main Process: set work directory error at project "+projectType+"-"+projectNumber);
            return false;
        }
        Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath);
        List<Suspicious> suspiciouses = localization.getSuspiciousLite();
        return suspiciousLoop(suspiciouses, project);
    }

    private boolean checkProjectDirectory(){
        if (!new File(classpath).exists()){
            return false;
        }
        if (!new File(classSrc).exists()){
            return false;
        }
        if (!new File(testClasspath).exists()){
            return false;
        }
        if (!new File(testClassSrc).exists()){
            return false;
        }
        return true;
    }

    public boolean suspiciousLoop(List<Suspicious> suspiciouses, String project) {
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
                if ((System.currentTimeMillis()-startLine)/1000 >1800){
                    return false;
                }
                if (fixSuspicious(suspicious, project)){
                    return true;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            triedSuspicious.add(suspicious);
        }
        return false;
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

        if (TestUtils.getFailTestNumInProject(project) > 0){
            Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath);
            List<Suspicious> suspiciouses = localization.getSuspiciousLite(false);
            suspiciousLoop(suspiciouses, project);
            return true;
        }
        else {
            printCollectingMessage(project, suspicious);
            System.out.println("Fix All Place Success");
            return true;
        }
    }

    public void printCollectingMessage(String project, Suspicious suspicious){
        File recordPackage = new File(System.getProperty("user.dir")+"/patch/");
        recordPackage.mkdirs();
        File recordFile = new File(recordPackage.getAbsolutePath()+"/"+project);
        try {
            if (!recordFile.exists()){
                recordFile.createNewFile();
            }
            FileWriter writer = new FileWriter(recordFile,true);
            writer.write("===========================================\n");
            writer.write("True Test Num: "+suspicious.trueTestNums()+"\n");
            writer.write("True Assert Num: "+suspicious.trueAssertNums()+"\n");
            writer.write("Whole Cost Time: "+(System.currentTimeMillis()-startMili)/1000+"\n");
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
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
        if (projectName.equals("Time") && (number == 3||number == 9|| number ==15)){
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
