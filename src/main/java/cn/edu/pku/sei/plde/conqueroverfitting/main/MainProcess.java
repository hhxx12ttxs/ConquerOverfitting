package cn.edu.pku.sei.plde.conqueroverfitting.main;

import cn.edu.pku.sei.plde.conqueroverfitting.fix.SuspiciousFixer;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.PathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.RecordUtils;
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
        if (!checkProjectDirectory()){
            System.out.println("Main Process: set work directory error at project "+projectType+"-"+projectNumber);
            File recordPackage = new File(System.getProperty("user.dir")+"/patch/");
            recordPackage.mkdirs();
            File main = new File(recordPackage.getAbsolutePath()+"/"+"FixResult.log");
            try {
                if (!main.exists()) {
                    main.createNewFile();
                }
                FileWriter writer = new FileWriter(main, true);
                writer.write("project "+project+" path error\n");
                writer.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            return false;
        }
        Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath, project);
        List<Suspicious> suspiciouses = localization.getSuspiciousLite();
        return suspiciousLoop(suspiciouses, project);
    }


    private boolean checkProjectDirectory(){
            if (!new File(classpath).exists()){
                System.out.println("Classpath :"+classpath+" do not exist!");
                return false;
            }
            if (!new File(classSrc).exists()){
                System.out.println("ClassSourcePath :"+classSrc+" do not exist!");
                return false;
            }
            if (!new File(testClasspath).exists()){
                System.out.println("TestClassPath :"+testClasspath+" do not exist!");
                return false;
            }
            if (!new File(testClassSrc).exists()){
                System.out.println("TestSourcePath :"+testClassSrc+" do not exist!");
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
                if ((System.currentTimeMillis()-startLine)/1000 >3600){
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
            printCollectingMessage(project, suspicious);
            return isFixSuccess( project);
        }
        return false;
    }

    public boolean isFixSuccess( String project){
        System.out.println("Fix Success One Place");
        if (TestUtils.getFailTestNumInProject(project) > 0){
            Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc,libPath, project);
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

    public void printCollectingMessage(String project, Suspicious suspicious){
        RecordUtils writer = new RecordUtils(project,"RuntimeMessage");
        writer.write("Whole Cost Time: "+(System.currentTimeMillis()-startMili)/1000+"\n");
        writer.write("True Test Num: "+suspicious.trueTestNums()+"\n");
        writer.write("True Assert Num: "+suspicious.trueAssertNums()+"\n");
        writer.write("====================================================================\n\n");
        writer.close();
    }



    public String setWorkDirectory(String projectName, int number){
        libPath.add(FromString.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        libPath.add(EasyMock.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        libPath.add(IOUtils.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        File projectDir = new File(System.getProperty("user.dir")+"/project/");
        System.out.println("Project Dir: "+projectDir.getAbsolutePath());
        FileUtils.deleteDirNow(projectDir.getAbsolutePath());
        if (!projectDir.exists()){
            projectDir.mkdirs();
        }
        String project = projectName+"_"+number;
        /* 四个整个项目需要的参数 */
        FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project,projectDir.getAbsolutePath());
        List<String> paths = PathUtils.getSrcPath(project);
        classpath = projectDir+"/"+project+paths.get(0);
        testClasspath = projectDir+"/"+project+paths.get(1);
        classSrc = projectDir+"/"+project+paths.get(2);
        testClassSrc = projectDir+"/"+ project + paths.get(3);
        FileUtils.copyDirectory(PATH_OF_DEFECTS4J+project+"/src/test/resources/",System.getProperty("user.dir")+"/src/test");
        File libPkg = new File(projectDir.getAbsolutePath()+"/"+project+"/lib/");
        if (libPkg.exists() && libPkg.list() != null){
            for (String p: libPkg.list()){
                if (p.endsWith(".jar")){
                    libPath.add(libPkg.getAbsolutePath()+"/"+p);
                }
            }
        }
        libPkg = new File(projectDir.getAbsolutePath()+"/"+project+"/build/lib/");
        if (libPkg.exists() && libPkg.list() != null){
            for (String p: libPkg.list()){
                if (p.endsWith(".jar")){
                    libPath.add(libPkg.getAbsolutePath()+"/"+p);
                }
            }
        }
        return project;
    }


}
