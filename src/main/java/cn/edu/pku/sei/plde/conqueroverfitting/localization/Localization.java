package cn.edu.pku.sei.plde.conqueroverfitting.localization;

import com.gzoltar.core.components.Statement;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.SuspiciousField;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Metric;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Ochiai;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.synth.TestClassesFinder;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar.GZoltarSuspiciousProgramStatements;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.library.JavaLibrary;
import java.net.URL;
import java.util.*;

/**
 * Created by localization on 16/1/23.
 */

public class Localization  {
    public String classpath;
    public String testClassPath;
    public String[] testClasses;

    /**
     * @param classPath the path of project's class file
     * @param testClassPath the path of project's test class file
     */
    public Localization(String classPath, String testClassPath){
        this.classpath = classPath;
        this.testClassPath = testClassPath;
        testClasses = new TestClassesFinder().findIn(JavaLibrary.classpathFrom(testClassPath), false);
    }

    public List<Statement> getSuspiciousList(){
        return this.getSuspiciousListWithMetric(new Ochiai());
    }


    public List<Statement> getSuspiciousListWithSuspiciousnessBiggerThanZero(){
        List<Statement> statements = this.getSuspiciousList();
        List<Statement> result = new ArrayList<Statement>();
        for (Statement statement: statements){
            if (statement.getSuspiciousness()>0){
                result.add(statement);
            }
        }
        return result;
    }

    public List<HashMap<SuspiciousField, String>> getSuspiciousListLite() {
        List<Statement> statements = this.getSuspiciousListWithSuspiciousnessBiggerThanZero();
        List<HashMap<SuspiciousField, String>> result = new ArrayList<HashMap<SuspiciousField, String>>();
        Statement firstline = statements.get(0);
        Statement lastline = statements.get(0);
        for (Statement statement: statements){
            if (getClassAddressFromStatement(statement).equals(getClassAddressFromStatement(firstline)) && getTargetFunctionFromStatement(statement).equals(getTargetFunctionFromStatement(firstline))){
                firstline = statement.getLineNumber() < firstline.getLineNumber() ? statement : firstline;
                lastline = statement.getLineNumber() > lastline.getLineNumber() ? statement : lastline;
            }else {
                HashMap<SuspiciousField, String> data = new HashMap<SuspiciousField, String>();
                data.put(SuspiciousField.class_address, getClassAddressFromStatement(firstline));
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

    public List<HashMap<SuspiciousField, String>> getSuspiciousListLiteWithSpecificLine(){
        List<Statement> statements = this.getSuspiciousListWithSuspiciousnessBiggerThanZero();
        List<HashMap<SuspiciousField, String>> result = new ArrayList<HashMap<SuspiciousField, String>>();
        Statement firstline = statements.get(0);
        Collection<String> lineNumbers = new ArrayList<String>();
        for (Statement statement: statements){
            if (getClassAddressFromStatement(statement).equals(getClassAddressFromStatement(firstline)) && getTargetFunctionFromStatement(statement).equals(getTargetFunctionFromStatement(firstline))){
                lineNumbers.add(String.valueOf(statement.getLineNumber()));
            }else {
                HashMap<SuspiciousField, String> data = new HashMap<SuspiciousField, String>();
                data.put(SuspiciousField.class_address, getClassAddressFromStatement(firstline));
                if (lineNumbers.size() == 1){
                    data.put(SuspiciousField.line_number, (String)lineNumbers.toArray()[0]);
                }else {
                    data.put(SuspiciousField.line_number, String.join("-",lineNumbers));
                }
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
    public List<Statement> getSuspiciousListWithMetric(Metric metric){
        URL[] classpaths = JavaLibrary.classpathFrom(testClassPath);
        classpaths = JavaLibrary.extendClasspathWith(classpath, classpaths);
        GZoltarSuspiciousProgramStatements gZoltar = GZoltarSuspiciousProgramStatements.create(classpaths, testClasses, new Ochiai());
        List<Statement> statements = gZoltar.sortBySuspiciousness(testClasses);

        return statements;
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
}
