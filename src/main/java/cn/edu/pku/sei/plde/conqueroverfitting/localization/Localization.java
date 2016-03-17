package cn.edu.pku.sei.plde.conqueroverfitting.localization;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar.StatementExt;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import com.gzoltar.core.components.Statement;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.SuspiciousField;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Metric;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Ochiai;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.synth.TestClassesFinder;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar.GZoltarSuspiciousProgramStatements;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.JavaLibrary;
import com.sun.glass.ui.EventLoop;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by localization on 16/1/23.
 */

public class Localization  {
    public String classpath;
    public String testClassPath;
    public String[] testClasses;
    public String testSrcPath;
    public String srcPath;

    /**
     * @param classPath the path of project's class file
     * @param testClassPath the path of project's test class file
     */
    public Localization(String classPath, String testClassPath, String testSrcPath, String srcPath){
        this.classpath = classPath;
        this.testClassPath = testClassPath;
        this.testSrcPath = testSrcPath;
        this.srcPath = srcPath;
        testClasses = new TestClassesFinder().findIn(JavaLibrary.classpathFrom(testClassPath), false);
        Arrays.sort(testClasses);
    }

    public Localization(String classPath, String testClassPath, String testSrcPath, String srcPath, String testClass){
        this.classpath = classPath;
        this.testClassPath = testClassPath;
        this.testSrcPath = testSrcPath;
        this.srcPath = srcPath;
        testClasses = new String[]{testClass};
    }

    public List<StatementExt> getSuspiciousList(){
        return this.getSuspiciousListWithMetric(new Ochiai());
    }


    public List<StatementExt> getSuspiciousListWithSuspiciousnessBiggerThanZero(){
        List<StatementExt> statements = this.getSuspiciousList();
        List<StatementExt> result = new ArrayList<StatementExt>();
        for (StatementExt statement: statements){
            if (statement.getSuspiciousness()>0){
                result.add(statement);
            }
        }
        return result;
    }

    public List<HashMap<SuspiciousField, String>> getSuspiciousListLite() {
        List<StatementExt> statements = this.getSuspiciousListWithSuspiciousnessBiggerThanZero();
        List<HashMap<SuspiciousField, String>> result = new ArrayList<HashMap<SuspiciousField, String>>();
        StatementExt firstline = statements.get(0);
        StatementExt lastline = statements.get(0);
        for (StatementExt statement: statements){
            if (getClassAddressFromStatement(statement).equals(getClassAddressFromStatement(firstline)) && getTargetFunctionFromStatement(statement).equals(getTargetFunctionFromStatement(firstline))){
                firstline = statement.getLineNumber() < firstline.getLineNumber() ? statement : firstline;
                lastline = statement.getLineNumber() > lastline.getLineNumber() ? statement : lastline;
            }else {
                HashMap<SuspiciousField, String> data = new HashMap<SuspiciousField, String>();
                data.put(SuspiciousField.class_address, getClassAddressFromStatement(firstline));
                data.put(SuspiciousField.error_tests, getErrorTestsStringFromStatement(firstline));
                data.put(SuspiciousField.line_number, getLineNumberFromStatement(firstline)+"-"+getLineNumberFromStatement(lastline));
                data.put(SuspiciousField.suspiciousness, getSupiciousnessFromStatement(firstline));
                data.put(SuspiciousField.target_function, getTargetFunctionFromStatement(firstline));
                result.add(data);
                firstline = statement;
                lastline = statement;
            }
        }
        return result;
    }

    public List<Suspicious> getSuspiciousLite(){
        return getSuspiciousLite(true);
    }

    public List<Suspicious> getSuspiciousLite(boolean jump){
        File suspicousFile = new File(System.getProperty("user.dir")+"/suspicious/"+ FileUtils.getMD5(StringUtils.join(testClasses,"")+classpath+testClassPath+srcPath+testSrcPath)+".sps");

        if (suspicousFile.exists() && jump){
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(suspicousFile));
                List<Suspicious> result = (List<Suspicious>) objectInputStream.readObject();
                return result;
            }catch (Exception e){
                System.out.println("Reloading Localization Result...");
            }
        }

        List<StatementExt> statements = statementFilter(this.getSuspiciousListWithSuspiciousnessBiggerThanZero());
        List<Suspicious> result = new ArrayList<Suspicious>();
        StatementExt firstline = statements.get(0);
        List<String> lineNumbers = new ArrayList<String>();
        for (StatementExt statement: statements){
            if (getClassAddressFromStatement(statement).equals(getClassAddressFromStatement(firstline)) && getTargetFunctionFromStatement(statement).equals(getTargetFunctionFromStatement(firstline))){
                lineNumbers.add(String.valueOf(statement.getLineNumber()));
            }else {
                if (firstline.getTests().size()<30) {
                    result.add(new Suspicious(classpath, testClassPath, getClassAddressFromStatement(firstline), getTargetFunctionFromStatement(firstline), firstline.getSuspiciousness(), firstline.getTests(),firstline.getFailTests(), new ArrayList<String>(lineNumbers)));
                }
                firstline = statement;
                lineNumbers.clear();
                if (!lineNumbers.contains(String.valueOf(statement.getLineNumber()))){
                    lineNumbers.add(String.valueOf(statement.getLineNumber()));
                }
            }
        }
        if (lineNumbers.size() != 0 && firstline.getTests().size()< 40){
            result.add(new Suspicious(classpath, testClassPath, getClassAddressFromStatement(firstline), getTargetFunctionFromStatement(firstline), firstline.getSuspiciousness(), firstline.getTests(),firstline.getFailTests(), new ArrayList<String>(lineNumbers)));
        }
        if (!jump){
            return result;
        }
        try {
            boolean createResult = suspicousFile.createNewFile();
            if (!createResult){
                System.out.println("File Create Error");
            }
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(suspicousFile));
            objectOutputStream.writeObject(result);
            objectOutputStream.close();
        } catch (IOException e){
            e.printStackTrace();
            return new ArrayList<Suspicious>();
        }
        return result;
    }

    private List<StatementExt> statementFilter(List<StatementExt> statements){
        List<StatementExt> result = new ArrayList<>();
        for (StatementExt statement: statements){
            if (getFunctionNameFromStatement(statement).equals("valueOf")){
                continue;
            }
            if (statement.getName().contains("exception") || statement.getName().contains("Exception")){
                continue;
            }
            for (String test: statement.getFailTests()) {
                String testClass = test.split("#")[0];
                String testMethod = test.split("#")[1];
                String code = FileUtils.getCodeFromFile(testSrcPath, testClass);
                String methodCode = FileUtils.getTestFunctionCodeFromCode(code, testMethod);
                if (methodCode.contains(getFunctionNameFromStatement(statement))) {
                    result.add(statement);
                }
            }
        }
        return result;
    }

    public List<HashMap<SuspiciousField, String>> getSuspiciousListLiteWithSpecificLine(){
        List<StatementExt> statements = this.getSuspiciousListWithSuspiciousnessBiggerThanZero();
        List<HashMap<SuspiciousField, String>> result = new ArrayList<HashMap<SuspiciousField, String>>();
        StatementExt firstline = statements.get(0);
        Collection<String> lineNumbers = new ArrayList<String>();
        for (StatementExt statement: statements){
            if (getClassAddressFromStatement(statement).equals(getClassAddressFromStatement(firstline)) && getTargetFunctionFromStatement(statement).equals(getTargetFunctionFromStatement(firstline))){
                lineNumbers.add(String.valueOf(statement.getLineNumber()));
            }else {
                HashMap<SuspiciousField, String> data = new HashMap<SuspiciousField, String>();
                data.put(SuspiciousField.class_address, getClassAddressFromStatement(firstline));
                if (lineNumbers.size() == 1){
                    data.put(SuspiciousField.line_number, (String)lineNumbers.toArray()[0]);
                }else {
                    data.put(SuspiciousField.line_number, StringUtils.join(lineNumbers,"-"));
                }
                data.put(SuspiciousField.error_tests, getErrorTestsStringFromStatement(firstline));
                data.put(SuspiciousField.suspiciousness, getSupiciousnessFromStatement(firstline));
                data.put(SuspiciousField.target_function, getTargetFunctionFromStatement(firstline));
                result.add(data);
                firstline = statement;
                lineNumbers.clear();
                lineNumbers.add(String.valueOf(statement.getLineNumber()));
            }
        }
        return result;
    }


    /**
     *
     * @param metric the suspiciousness calculate metric
     * @return the list of suspicious statement
     */
    public List<StatementExt> getSuspiciousListWithMetric(Metric metric){
        URL[] classpaths = JavaLibrary.classpathFrom(testClassPath);
        classpaths = JavaLibrary.extendClasspathWith(classpath, classpaths);
        GZoltarSuspiciousProgramStatements gZoltar = GZoltarSuspiciousProgramStatements.create(classpaths, testClasses, new Ochiai(),testSrcPath, srcPath);
        return gZoltar.sortBySuspiciousness(testClasses);
    }

    /**
     *
     * @param statement target statement
     * @return class address of statement
     */
    public static String getClassAddressFromStatement(Statement statement){
        return statement.getLabel().split("\\{")[0];
    }

    /**
     *
     * @param statement target statement
     * @return class address of statement
     */
    public static String getLineNumberFromStatement(Statement statement){
        return String.valueOf(statement.getLineNumber());
    }

    /**
     *
     * @param statement target statement
     * @return class address of statement
     */
    public static String getSupiciousnessFromStatement(Statement statement){
        return String.valueOf(statement.getSuspiciousness());
    }

    /**
     *
     * @param statement target statement
     * @return class address of statement
     */
    public static String getTargetFunctionFromStatement(Statement statement){
        return statement.getLabel().split("\\{")[1].split("\\)")[0]+"\\)";
    }

    public static String getFunctionNameFromStatement(Statement statement){
        return getTargetFunctionFromStatement(statement).split("\\(")[0];
    }

    public static String getErrorTestsStringFromStatement(StatementExt statementExt){
        return StringUtils.join(statementExt.getTests(),"-");
    }
}
