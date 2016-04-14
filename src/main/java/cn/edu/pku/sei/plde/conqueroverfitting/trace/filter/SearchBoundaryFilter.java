package cn.edu.pku.sei.plde.conqueroverfitting.trace.filter;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
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
    public static Map<VariableInfo, List<BoundaryInfo>> getBoundary(Map<VariableInfo, List<String>> exceptionVariable, List<VariableInfo> variableInfos){
        Map<VariableInfo, List<BoundaryInfo>> result = new HashMap<VariableInfo, List<BoundaryInfo>>();
        for (Map.Entry<VariableInfo, List<String>> entry: exceptionVariable.entrySet()){
            if (entry.getKey().variableName.equals("this") || entry.getKey().variableName.equals("return")){
                continue;
            }
            //String variableName = variableName(entry);
            List<BoundaryInfo> boundaryList = getSearchBoundaryInfo(entry.getKey());
            if (boundaryList != null && boundaryList.size()> 0){
                result.put(entry.getKey(),boundaryList);
            }
            if (entry.getKey().isAddon){
                result.put(entry.getKey(),boundaryList);
            }
        }
        //for (VariableInfo info: variableInfos){
        //    if (info.variableName.equals("this") && TypeUtils.isComplexType(info.getStringType())){
        //        List<BoundaryInfo> boundaryList = getSearchBoundaryInfo(info);
        //        result.put(info, boundaryList);
        //    }
        //}
        return result;
    }

    private static List<Double> generateTrueValueInterval(List<BoundaryInfo> trueValues){
        List<Double> values = new ArrayList<>();
        for (BoundaryInfo info: trueValues) {
            String value = info.value;
            try {
                values.add(MathUtils.parseStringValue(value));
            } catch (NumberFormatException e) {
                continue;
            }
        }
        Collections.sort(values);
        if (values.size() == 0){
            return new ArrayList<>();
        }
        Double maxValue = Double.MIN_VALUE;
        Double minValue = Double.MAX_VALUE;
        for (double value: values){
            if (value > maxValue){
                maxValue = value;
            }
            if (value < minValue){
                minValue = value;
            }
        }
        return Arrays.asList(minValue,maxValue);
    }


    private static List<BoundaryInfo> getSearchBoundaryInfo(VariableInfo info){
        String variableName = info.variableName;
        String valueType = info.getStringType();
        ArrayList<String> keywords = new ArrayList<String>();
        keywords.add("if");
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
            if (!info.variableName.equals("this")){
                keywords.add(info.variableName);
            }
        }

        File codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        File simpleCodePackage = new File("experiment/searchcode/" + StringUtils.join(Arrays.asList("if",variableName),"-"));
        File complexCodePackage = new File("experiment/searchcode/" + StringUtils.join(Arrays.asList("if",info.getStringType()),"-"));

        if (!simpleCodePackage.exists() && !complexCodePackage.exists()) {
            if (!codePackage.exists()){
                GathererJava gathererJava = new GathererJava(keywords, StringUtils.join(keywords, "-"));
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
                GathererJava gathererJava = new GathererJava(keywords, StringUtils.join(keywords,"-"));
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
        keywords.remove(valueType);
        codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        if (!codePackage.exists()){
            GathererJava gathererJava = new GathererJava(keywords, StringUtils.join(keywords,"-"));
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

    private static void addValueToResult(Map.Entry<VariableInfo, List<String>> entry, Map<VariableInfo, List<String>> result,List<String> value){
        for (String v: value){
            addValueToResult(entry, result,v);
        }
    }

}
