package cn.edu.pku.sei.plde.conqueroverfitting.trace.filter;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.VariableUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by yanrunfa on 16/3/4.
 */
public class SearchBoundaryFilter {
    /**
     *
     * @param exceptionVariable the  key-value map to be filtered
     * @return the filtered key-value map
     */
    public static List<BoundaryWithFreq> getBoundary(ExceptionVariable exceptionVariable, String project, List<String> keywords){
        if (!MathUtils.isNumberType(exceptionVariable.type) && !exceptionVariable.name.equals("this")){
            return new ArrayList<>();
        }
        return getSearchBoundaryWithFreq(exceptionVariable.variable, project, keywords);
    }

    public static List<BoundaryInfo> getBoundaryInfo(ExceptionVariable exceptionVariable, String project, List<String> keywords){
        if (!MathUtils.isNumberType(exceptionVariable.type) && !exceptionVariable.name.equals("this")){
            return new ArrayList<>();
        }
        return getSearchBoundaryInfo(exceptionVariable.variable, project, keywords);
    }

    private static List<BoundaryInfo> getSearchBoundaryInfo(VariableInfo info,String project, List<String> addonKeywords){
        String variableName = info.variableName;
        String valueType = info.isSimpleType?info.getStringType().toLowerCase():info.getStringType();
        ArrayList<String> keywords = new ArrayList<String>();
        keywords.add("if");
        keywords.addAll(addonKeywords);
        keywords.add(valueType);
        if (VariableUtils.isExpression(info)){
            keywords.add(info.expressMethod);
            keywords.remove(valueType);
        }
        else if (!info.variableName.equals("this") && info.variableName.length()>1){
            keywords.add(info.variableName.replace(" ",""));
        }

        File codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        File simpleCodePackage = new File("experiment/searchcode/" + StringUtils.join(Arrays.asList("if",variableName),"-"));
        File complexCodePackage = new File("experiment/searchcode/" + StringUtils.join(Arrays.asList("if",info.getStringType()),"-"));

        if (!simpleCodePackage.exists() && !complexCodePackage.exists()) {
            if (!codePackage.exists()){
                GathererJava gathererJava = new GathererJava(keywords, StringUtils.join(keywords, "-"),getProjectFullName(project));
                gathererJava.searchCode();
            }
            if (!codePackage.exists()) {
                codePackage.mkdirs();
            }
            if (codePackage.list().length < 30 && !VariableUtils.isExpression(info)){
                FileUtils.deleteDir(codePackage);
            }
            else {
                BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath());
                List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
                //FileUtils.deleteDir(codePackage);
                return boundaryList;
            }
        }

        if (!info.isSimpleType) {
            keywords.remove(info.variableName);
            codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
            if (!codePackage.exists()){
                GathererJava gathererJava = new GathererJava(keywords, StringUtils.join(keywords,"-"),getProjectFullName(project));
                gathererJava.searchCode();
                if (!codePackage.exists()) {
                    codePackage.mkdirs();
                }

            }
            BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath());
            List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
            //FileUtils.deleteDir(complexCodePackage);
            return boundaryList;

        }
        if (!keywords.contains(info.variableName)){
            keywords.add(info.variableName);
        }
        //keywords.remove(valueType);
        codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        if (!codePackage.exists()){
            GathererJava gathererJava = new GathererJava(keywords, StringUtils.join(keywords,"-"),getProjectFullName(project));
            gathererJava.searchCode();
            if (!codePackage.exists()) {
                codePackage.mkdirs();
            }
        }
        BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath());
        List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
        FileUtils.deleteDir(simpleCodePackage); 
        return boundaryList;
    }

    public static List<BoundaryWithFreq> getBoundary(ExceptionVariable exceptionVariable, String project) {
        return getBoundary(exceptionVariable, project, new ArrayList<String>());
    }

    private static List<BoundaryWithFreq> getSearchBoundaryWithFreq(VariableInfo info,String project, List<String> addonKeywords){
        String variableName = info.variableName;
        String valueType = info.isSimpleType?info.getStringType().toLowerCase():info.getStringType();
        ArrayList<String> keywords = new ArrayList<String>();
        keywords.add("if");
        keywords.addAll(addonKeywords);
        keywords.add(valueType);
        if (VariableUtils.isExpression(info)){
            keywords.add(info.expressMethod);
            keywords.remove(valueType);
        }
        else if (!info.variableName.equals("this") && info.variableName.length()>1){
            keywords.add(info.variableName.replace(" ",""));
        }


        File codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        File simpleCodePackage = new File("experiment/searchcode/" + StringUtils.join(Arrays.asList("if",variableName),"-"));
        File complexCodePackage = new File("experiment/searchcode/" + StringUtils.join(Arrays.asList("if",info.getStringType()),"-"));

        if (!simpleCodePackage.exists() && !complexCodePackage.exists()) {
            if (!codePackage.exists()){
                GathererJava gathererJava = new GathererJava(keywords, StringUtils.join(keywords, "-"),getProjectFullName(project));
                gathererJava.searchCode();
            }
            if (!codePackage.exists()) {
                codePackage.mkdirs();
            }
            if (codePackage.list().length < 30 && !VariableUtils.isExpression(info)){
                FileUtils.deleteDir(codePackage);
            }
            else {
                BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath());
                List<BoundaryWithFreq> boundaryList = boundaryCollect.getBoundaryWithFreqList();
                //FileUtils.deleteDir(codePackage);
                return boundaryList;
            }
        }

        if (!info.isSimpleType) {
            keywords.remove(info.variableName);
            codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
            if (!codePackage.exists()){

                GathererJava gathererJava = new GathererJava(keywords, StringUtils.join(keywords,"-"),getProjectFullName(project));
                gathererJava.searchCode();
                if (!codePackage.exists()) {
                    codePackage.mkdirs();
                }

            }
            BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath());
            List<BoundaryWithFreq> boundaryList = boundaryCollect.getBoundaryWithFreqList();
            //FileUtils.deleteDir(complexCodePackage);
            return boundaryList;

        }
        if (!keywords.contains(info.variableName)){
            keywords.add(info.variableName);
        }
        //keywords.remove(valueType);
        codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        if (!codePackage.exists()){
            GathererJava gathererJava = new GathererJava(keywords, StringUtils.join(keywords,"-"),getProjectFullName(project));
            gathererJava.searchCode();
            if (!codePackage.exists()) {
                codePackage.mkdirs();
            }
        }
        BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath());
        List<BoundaryWithFreq> boundaryList = boundaryCollect.getBoundaryWithFreqList();
        //FileUtils.deleteDir(simpleCodePackage);
        return boundaryList;
    }





    private static String getProjectFullName(String project){
        if (project.startsWith("Math")){
            return "commons-math";
        }
        if (project.startsWith("Lang")){
            return "commons-lang";
        }
        if (project.startsWith("Closure")){
            return "closure-compiler";
        }
        if (project.startsWith("Chart")){
            return "jfreechart";
        }
        if (project.startsWith("Time")){
            return "joda-time";
        }
        return "";
    }
}
