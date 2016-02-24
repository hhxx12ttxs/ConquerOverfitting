package cn.edu.pku.sei.plde.conqueroverfitting.Entirety;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.fix.Capturer;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.SuspiciousField;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.VariableTracer;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.MethodCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.VariableCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.junit.Test;

import java.util.*;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class EntiretyTest {
    private final String PATH_OF_DEFECTS4J = "/Users/yanrunfa/Documents/defects4j/tmp/";

    @Test
    public void testEntirety() throws Exception{
        int i = 5;
        String project = "math5";
        /* 四个整个项目需要的参数 */
        String classpath = PATH_OF_DEFECTS4J+"Math-"+i+"/target/classes";              //项目的.class文件路径
        String testClasspath  = PATH_OF_DEFECTS4J+"Math-"+i+"/target/test-classes";    //项目的test的.class文件路径
        String classSrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/main/java";              //项目的源代码路径
        String testClassSrc = PATH_OF_DEFECTS4J + "Math-"+i+"/src/test/java";          //项目的test的源代码路径


        Localization localization = new Localization(classpath, testClasspath);
        /* 从localization模块得到suspicious列表需要很长时间的运行,所以Debug状态用下面生成的参数代替  */
        List<HashMap<SuspiciousField, String>> suspiciousnesses = localization.getSuspiciousListLite();
                                                                  //new ArrayList<HashMap<SuspiciousField, String>>();
        //HashMap<SuspiciousField, String> example = new HashMap<SuspiciousField, String>();
        //example.put(SuspiciousField.line_number,     "816-846");
        //example.put(SuspiciousField.error_tests,     "org.apache.commons.math3.util.MathArraysTest#testLinearCombinationWithSingleElementArray");
        //example.put(SuspiciousField.class_address,   "org.apache.commons.math3.util.MathArrays");
        //example.put(SuspiciousField.target_function, "linearCombination([D[D\\)");
        //suspiciousnesses.add(example);


        for (HashMap<SuspiciousField, String> suspiciousness: suspiciousnesses){
            if (suspiciousness.get(SuspiciousField.error_tests).startsWith("check")){
                continue;
            }
            String testFailclassName = suspiciousness.get(SuspiciousField.class_address);                             //test失败的的类
            int testFailLineNumber = Integer.valueOf(suspiciousness.get(SuspiciousField.line_number).split("-")[1]);  //test失败所在的行
            String[] testFailTestClasses = suspiciousness.get(SuspiciousField.error_tests).split("-");                //所有在该处中失败test

        /*  聚集所有需要跟踪的变量与无参函数  */
            String classSrcPath = classSrc + "/" + testFailclassName.replace(".","/") + ".java";                      //test失败的时候所在的类的源码地址
            VariableCollect variableCollect = VariableCollect.GetInstance(classSrcPath.substring(0,classSrcPath.lastIndexOf("/")));
            List<VariableInfo> parameters = variableCollect.getVisibleParametersInMethodList(classSrcPath, testFailLineNumber-1);
            List<VariableInfo> locals = variableCollect.getVisibleLocalInMethodList(classSrcPath, testFailLineNumber-1);
            MethodCollect methodCollect = MethodCollect.GetInstance(classSrcPath.substring(0,classSrcPath.lastIndexOf("/")));
            LinkedHashMap<String, ArrayList<MethodInfo>> methods = methodCollect.getVisibleMethodInAllClassMap(classSrcPath);
            List<VariableInfo> variableInfos = new ArrayList<VariableInfo>();
            variableInfos.addAll(parameters);
            variableInfos.addAll(locals);
            List<MethodInfo> methodInfos = methods.get(classSrcPath);

            VariableTracer tracer = new VariableTracer(classpath, testClasspath, classSrc);
            List<TraceResult> traceResults = new ArrayList<TraceResult>();
            for (String testFailTestClass: testFailTestClasses){
                String testFailTestClassName = testFailTestClass.substring(0, testFailTestClass.lastIndexOf("#")); //testFailTestClass的格式为: 类名#函数名
                traceResults.addAll(tracer.trace(testFailclassName, testFailTestClassName, testFailLineNumber, variableInfos, methodInfos));
            }
            List<VariableInfo> allInfos = InfoUtils.AddMethodInfoListToVariableInfoList(variableInfos, methodInfos);
            Map<VariableInfo, List<String>> exceptionVariable = ExceptionExtractor.extractWithAbandonTrueValue(traceResults, allInfos);
            Map<VariableInfo, List<String>> filteredVariable = ExceptionExtractor.filterWithSearchBoundary(exceptionVariable,project,10);
            String ifString = BoundaryGenerator.generate(filteredVariable, project);
            Capturer fixCapturer = new Capturer(classpath, testClasspath, testClassSrc);
            String fixString = fixCapturer.getFixFrom(testFailclassName.split("#")[0], testFailclassName.split("#")[1]);



        }
    }
}
