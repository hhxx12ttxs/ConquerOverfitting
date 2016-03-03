package cn.edu.pku.sei.plde.conqueroverfitting.Entirety;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.Capturer;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.JavaFixer;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import org.junit.Test;

import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class EntiretyTest {
    private final String PATH_OF_DEFECTS4J = "/Users/yanrunfa/Documents/defects4j/tmp/";

    @Test
    public void testEntirety() throws Exception{
        int i = 63;
        String project = "math63";
        /* 四个整个项目需要的参数 */
        String classpath = PATH_OF_DEFECTS4J+"Math-"+i+"/target/classes";              //项目的.class文件路径
        String testClasspath  = PATH_OF_DEFECTS4J+"Math-"+i+"/target/test-classes";    //项目的test的.class文件路径
        String classSrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/main/java";              //项目的源代码路径
        String testClassSrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test/java";///java";          //项目的test的源代码路径


        Localization localization = new Localization(classpath, testClasspath, testClassSrc, classSrc);
        List<Suspicious> suspiciouses = localization.getSuspiciousLite();

        for (Suspicious suspicious: suspiciouses){
            String ifString = BoundaryGenerator.generate(classpath,testClasspath, classSrc, suspicious, project);
            if (ifString.equals("")){
                continue;
            }
            Capturer fixCapturer = new Capturer(classpath, testClasspath, testClassSrc);
            System.out.println("if: "+ ifString);
            for (String test: suspicious.getTestClassAndFunction()){
                if (!test.contains("#")) {
                    continue;
                }
                String fixString = fixCapturer.getFixFrom(test.split("#")[0], test.split("#")[1]);
                if (fixString.equals("")){
                    continue;
                }
                System.out.println("fix: "+ fixString);
                JavaFixer javaFixer = new JavaFixer(classpath, testClasspath, classSrc);
                boolean result = javaFixer.fixWithIfStatement(suspicious.getTestClasses(),suspicious.classname(),suspicious.lastLine(),ifString,fixString);
                if (result){
                    System.out.println("Fix Success");
                }
            }


        }
    }
}
