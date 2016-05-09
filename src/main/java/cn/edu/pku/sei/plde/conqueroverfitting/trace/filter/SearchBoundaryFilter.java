package cn.edu.pku.sei.plde.conqueroverfitting.trace.filter;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.VariableUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by yanrunfa on 16/3/4.
 */
public class SearchBoundaryFilter {
    /**
     *
     * @param exceptionVariable the  key-value map to be filtered
     * @return the filtered key-value map
     */
    public static List<BoundaryWithFreq> getBoundary(ExceptionVariable exceptionVariable, String project, List<String> keywords, Suspicious suspicious){
        return getSearchBoundaryWithFreq(exceptionVariable.variable, project, keywords, suspicious);
    }


    public static List<BoundaryWithFreq> getBoundary(ExceptionVariable exceptionVariable, String project, Suspicious suspicious) {
        return getBoundary(exceptionVariable, project, new ArrayList<String>(), suspicious);
    }


    private static List<BoundaryWithFreq> getSearchBoundaryWithFreq(VariableInfo info, String project, List<String> addonKeywords, Suspicious suspicious){
        String variableName = info.variableName;
        if (variableName.endsWith("[i]")){
            variableName = variableName.substring(0, variableName.indexOf("["));
        }
        if (variableName.endsWith(".null")){
            variableName = variableName.substring(0, variableName.lastIndexOf("."));
        }
        if (variableName.endsWith(".Comparable")){
            variableName = variableName.substring(0, variableName.lastIndexOf("."));
        }
        String valueType = info.isSimpleType?info.getStringType().toLowerCase():info.getStringType();
        ArrayList<String> keywords = new ArrayList<String>();
        keywords.add("if");
        keywords.addAll(addonKeywords);
        keywords.add(valueType);
        if (VariableUtils.isExpression(info)){
            keywords.add(info.expressMethod);
            keywords.remove(variableName);
        }
        if (variableName.length() == 1){
            String methodName = suspicious.functionnameWithoutParam();
            String keyword = "";
            for (Character ch: methodName.toCharArray()){
                if(!((ch<='Z')&&(ch>='A'))){
                    keyword += ch;
                    continue;
                }
                break;
            }
            keywords.remove(keyword);
            keywords.remove(valueType);
        }
        if (!variableName.equals("this") && !VariableUtils.isExpression(info) && variableName.length() > 1){
            keywords.add(variableName.replace(" ",""));
        }

        File codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        if (!codePackage.exists()){
            searchCode(keywords, project);
        }
        if (codePackage.exists()) {
            if (codePackage.list().length > 30 || VariableUtils.isExpression(info)){
                if (TypeUtils.isSimpleType(info.getStringType())){
                    BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath(), false, null);
                    List<BoundaryWithFreq> boundaryList = boundaryCollect.getBoundaryWithFreqList();
                    return boundaryList;
                }
                else {
                    BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath(), true, info.getStringType());
                    List<BoundaryWithFreq> boundaryList = boundaryCollect.getBoundaryWithFreqList();
                    return boundaryList;
                }
            }
        }


        if (!info.isSimpleType) {
            keywords.remove(variableName);
            codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
            if (!codePackage.exists()){
                searchCode(keywords, project);
            }
            if (!codePackage.exists()){
                codePackage.mkdir();
            }
            BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath(), true, info.getStringType());
            List<BoundaryWithFreq> boundaryList = boundaryCollect.getBoundaryWithFreqList();
            return boundaryList;
        }

        keywords.remove(valueType);
        codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        if (!codePackage.exists()){
            searchCode(keywords, project);
        }
        if (!codePackage.exists()){
            codePackage.mkdir();
        }
        BoundaryCollect boundaryCollect = new BoundaryCollect(codePackage.getAbsolutePath(), false, null);
        List<BoundaryWithFreq> boundaryList = boundaryCollect.getBoundaryWithFreqList();
        return boundaryList;
    }


    private static void searchCode(ArrayList<String> keywords, String project){
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<Boolean> future = service.submit(new SearchCodeProcess(keywords, project));
        try {
            future.get(Config.SEARCH_BOUNDARY_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e){
            future.cancel(true);
            e.printStackTrace();
        } catch (TimeoutException e){
            future.cancel(true);
            e.printStackTrace();
        } catch (ExecutionException e){
            future.cancel(true);
            e.printStackTrace();
        }
    }




}

class SearchCodeProcess implements Callable<Boolean> {
    public ArrayList<String> keywords;
    public String project;

    public SearchCodeProcess(ArrayList<String> keywords, String project) {
        this.project = project;
        this.keywords = keywords;
    }

    public synchronized Boolean call() {
        GathererJava GathererJava = new GathererJava(keywords, StringUtils.join(keywords, "-"),getProjectFullName(project));
        try {
            GathererJava.searchCode();
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
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