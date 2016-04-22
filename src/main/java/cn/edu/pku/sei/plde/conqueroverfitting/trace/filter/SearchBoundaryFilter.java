package cn.edu.pku.sei.plde.conqueroverfitting.trace.filter;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJavaGithub;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
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
    public static List<BoundaryInfo> getBoundary(ExceptionVariable exceptionVariable, String project, List<String> keywords){
        //对于名字为这两个的怀疑变量，一般是钦定的显而易见修复，不需要search boundary。
        if (exceptionVariable.name.equals("this") || exceptionVariable.name.equals("return")){
            return new ArrayList<>();
        }
        if (!MathUtils.isNumberType(exceptionVariable.type)){
            return new ArrayList<>();
        }
        return getSearchBoundaryInfo(exceptionVariable.variable, project, keywords);
    }

    public static List<BoundaryInfo> getBoundary(ExceptionVariable exceptionVariable, String project) {
        return getBoundary(exceptionVariable, project, new ArrayList<String>());
    }

    private static List<BoundaryInfo> getSearchBoundaryInfo(VariableInfo info,String project, List<String> addonKeywords){
        String variableName = info.variableName;
        String valueType = info.isSimpleType?info.getStringType().toLowerCase():info.getStringType();
        ArrayList<String> keywords = new ArrayList<String>();
        keywords.add("if");
        keywords.addAll(addonKeywords);
        keywords.add(valueType);
        if (info.variableName.startsWith("is") && info.variableName.endsWith("()")){
            variableName = info.variableName.substring(0, info.variableName.lastIndexOf("("));
            keywords.add(variableName);
        }
        else if (info.variableName.contains("(")) {
            variableName = info.variableName.substring(0, info.variableName.indexOf("("));
            keywords.add(variableName);
        }
        else if (info.variableName.contains("[")){
            variableName = info.variableName.substring(0, info.variableName.indexOf("["));
            keywords.add(variableName);
        }
        else {
            if (!info.variableName.equals("this") && info.variableName.length()>1){
                keywords.add(info.variableName.replace(" ",""));
            }
        }

        File codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        File simpleCodePackage = new File("experiment/searchcode/" + StringUtils.join(Arrays.asList("if",variableName),"-"));
        File complexCodePackage = new File("experiment/searchcode/" + StringUtils.join(Arrays.asList("if",info.getStringType()),"-"));

        if (!simpleCodePackage.exists() && !complexCodePackage.exists()) {
            if (!codePackage.exists()){
                GathererJavaGithub gathererJava = new GathererJavaGithub(keywords, StringUtils.join(keywords, "-"),project);
                gathererJava.searchCode();
            }
            if (!codePackage.exists()) {
                codePackage.mkdirs();
            }
            if (codePackage.list().length < 30){
                FileUtils.deleteDir(codePackage);
            }
            else {
                BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath());
                List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
                List<BoundaryInfo> filteredList = BoundaryFilter.getBoundaryWithName(boundaryList, variableName);
                if (filteredList.size() == 0 && !info.isSimpleType){
                    filteredList = BoundaryFilter.getBoundaryWithType(boundaryList, info.getStringType());
                }
                return filteredList;
            }
        }

        if (!info.isSimpleType) {
            keywords.remove(info.variableName);
            codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
            if (!codePackage.exists()){

                GathererJavaGithub gathererJava = new GathererJavaGithub(keywords, StringUtils.join(keywords,"-"),getProjectFullName(project));
                gathererJava.searchCode();
                if (!codePackage.exists()) {
                    codePackage.mkdirs();
                }

            }
            BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath());
            List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
            List<BoundaryInfo> filteredList = BoundaryFilter.getBoundaryWithName(boundaryList, variableName);
            if (filteredList.size() == 0 && !info.isSimpleType){
                filteredList = BoundaryFilter.getBoundaryWithType(boundaryList, info.getStringType());
            }
            if (filteredList.size() != 0){
                return filteredList;
            }

        }
        if (!keywords.contains(info.variableName)){
            keywords.add(info.variableName);
        }
        //keywords.remove(valueType);
        codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        if (!codePackage.exists()){
            GathererJavaGithub gathererJava = new GathererJavaGithub(keywords, StringUtils.join(keywords,"-"),"joda-time");
            gathererJava.searchCode();
            if (!codePackage.exists()) {
                codePackage.mkdirs();
            }
        }
        BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath());
        List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
        List<BoundaryInfo> filteredList = BoundaryFilter.getBoundaryWithName(boundaryList, variableName);
        //if (filteredList.size() == 0 && !info.isSimpleType){
        //    filteredList = BoundaryFilter.getBoundaryWithType(boundaryList, info.getStringType());
        //}
        return filteredList;
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

    private static String variableName(Map.Entry<VariableInfo, List<String>> entry){
        return entry.getKey().variableName.contains(".")?entry.getKey().variableName.substring(entry.getKey().variableName.lastIndexOf(".")+1):entry.getKey().variableName;
    }

    private static void addValueToResult(Map.Entry<VariableInfo, List<String>> entry, Map<VariableInfo, List<String>> result,String value){
        // isNaN=true在修补中总是对的
        if (variableName(entry).equals("isNaN")){
            value = "true";
        }
        if (result.containsKey(entry.getKey())){
            result.get(entry.getKey()).add(value);
        }
        else {
            result.put(entry.getKey(),new ArrayList<String>(Arrays.asList(value)));
        }
    }


}
