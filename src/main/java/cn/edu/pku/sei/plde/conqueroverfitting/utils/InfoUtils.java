package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.MethodCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.VariableCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import com.sun.xml.internal.ws.api.model.MEP;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/23.
 */
public class InfoUtils {
    public static final List<String> BANNED_VAR_NAME = Arrays.asList("serialVersionUID","toString()");
    public static final List<String> BANNED_METHOD_NAME = Arrays.asList("toString","hashCode");

    /**
     *
     * @param methodInfo
     * @return
     */
    public static VariableInfo changeMethodInfoToVariableInfo(MethodInfo methodInfo){
        if (methodInfo.isSimpleType){
            return new VariableInfo(methodInfo.methodName+"()", methodInfo.variableSimpleType, true, null);
        }
        else {
            return new VariableInfo(methodInfo.methodName+"()", null, false, methodInfo.otherType);
        }
    }

    /**
     *
     * @param methodInfos
     * @return
     */
    public static List<VariableInfo> changeMethodInfoToVariableInfo(List<MethodInfo> methodInfos){
        List<VariableInfo> variableInfos = new ArrayList<VariableInfo>();
        for (MethodInfo info: methodInfos){
            variableInfos.add(changeMethodInfoToVariableInfo(info));
        }
        return variableInfos;
    }

    /**
     *
     * @param variableInfos
     * @param methodInfos
     * @return
     */
    public static List<VariableInfo> AddMethodInfoListToVariableInfoList(List<VariableInfo> variableInfos, List<MethodInfo> methodInfos){
        List<VariableInfo> result = new ArrayList<VariableInfo>();
        if (variableInfos!= null){
            result.addAll(variableInfos);
        }
        if (methodInfos!= null){
            result.addAll(changeMethodInfoToVariableInfo(methodInfos));
        }
        return result;
    }


    public static List<VariableInfo> changeObjectInfo(VariableInfo info){
        if (!TypeUtils.isComplexType(info.getStringType())){
            return Arrays.asList(info);
        }
        VariableInfo info1 = new VariableInfo(info.variableName+".null",TypeEnum.BOOLEAN,true,null);
        info1.isLocalVariable = info.isLocalVariable;
        info1.isFieldVariable = info.isFieldVariable;
        info1.isParameter = info.isParameter;
        info1.isPublic = info.isPublic;
        info1.isFinal = info.isFinal;
        info1.isStatic = info.isStatic;
        VariableInfo info2 = new VariableInfo(info.variableName+".Comparable",TypeEnum.BOOLEAN,true,null);
        info2.isLocalVariable = info.isLocalVariable;
        info2.isFieldVariable = info.isFieldVariable;
        info2.isParameter = info.isParameter;
        info2.isPublic = info.isPublic;
        info2.isFinal = info.isFinal;
        info2.isStatic = info.isStatic;
        return Arrays.asList(info1, info2);
    }


    public static List<VariableInfo> filterBannedVariable(List<VariableInfo> infos){
        List<VariableInfo> result = new ArrayList<>();
        for (VariableInfo info: infos){
            if (BANNED_VAR_NAME.contains(info.variableName)){
                continue;
            }
            if (info.getStringType().contains("?")){
                continue;
            }
            if (info.isFieldVariable && info.isSimpleType && info.variableSimpleType == TypeEnum.BOOLEAN && !info.variableName.startsWith("is") && !info.variableName.contains(".is")){
                continue;
            }
            if (info.variableName.contains("<") || info.variableName.contains(">")){
                continue;
            }
            if (info.getStringType().contains("<") || info.getStringType().contains(">")){
                info.otherType = info.otherType.substring(0, info.otherType.indexOf("<"));
            }
            if (info.variableName.toUpperCase().equals(info.variableName)){
                continue;
            }
            result.add(info);
        }
        return result;
    }

    public static List<MethodInfo> filterBannedMethod(List<MethodInfo> infos) {
        List<MethodInfo> result = new ArrayList<>();
        for (MethodInfo info: infos){
            if (BANNED_METHOD_NAME.contains(info.methodName)){
                continue;
            }
            if (info.methodName.contains("Next")){
                continue;
            }
            //if (info.methodName.startsWith("get")){
            //    continue;
            //}
            result.add(info);
        }
        return result;
    }


    public static List<String> getSearchKeywords(VariableInfo info){
        List<String> keywords = new ArrayList<>(Arrays.asList("if", info.getStringType(), info.variableName));
        if (!new File("experiment/searchcode/" + StringUtils.join(keywords, "-")).exists()) {
            keywords.remove(info.getStringType());
        }
        if (!new File("experiment/searchcode/" + StringUtils.join(keywords, "-")).exists()) {
            keywords.add(info.getStringType());
            keywords.remove(info.variableName);
        }
        return keywords;
    }

    public static List<VariableInfo> getSubInfoOfComplexVariable(VariableInfo info, String classSrc, String parentInfoSrcPath){
        if (!TypeUtils.isComplexType(info.getStringType())) {
            return new ArrayList<>();
        }
        String paramClass = CodeUtils.getClassNameOfVariable(info, parentInfoSrcPath);
        if (paramClass.equals("")){
            return new ArrayList<>();
        }
        List<VariableInfo> variableInfos = new ArrayList<>();
        String paramClassSrcPath =  (classSrc + System.getProperty("file.separator") + paramClass.replace(".",System.getProperty("file.separator")) + ".java").replace("//","/");
        if (!new File(paramClassSrcPath).exists()){
            return new ArrayList<>();
        }
        VariableCollect paramFieldCollect = VariableCollect.GetInstance(paramClassSrcPath.substring(0, paramClassSrcPath.lastIndexOf("/")));
        LinkedHashMap<String, ArrayList<VariableInfo>> classvars = paramFieldCollect.getVisibleFieldInAllClassMap(paramClassSrcPath);
        if (classvars.containsKey(paramClassSrcPath)) {
            List<VariableInfo> fields = InfoUtils.filterBannedVariable(classvars.get(paramClassSrcPath));
            for (VariableInfo field: fields){
                VariableInfo newField = VariableInfo.copy(field);
                if (!newField.isPublic && !paramClassSrcPath.equals(parentInfoSrcPath) || (newField.isStatic && paramClassSrcPath.equals(parentInfoSrcPath))){
                    continue;
                }
                newField.variableName = info.variableName+"."+newField.variableName;
                variableInfos.add(newField);
            }
        }
        MethodCollect methodCollect = MethodCollect.GetInstance(paramClassSrcPath.substring(0, paramClassSrcPath.lastIndexOf("/")));
        LinkedHashMap<String, ArrayList<MethodInfo>> methods = methodCollect.getVisibleMethodWithoutParametersInAllClassMap(paramClassSrcPath);
        if (methods.containsKey(paramClassSrcPath)){
            List<MethodInfo> methodInfos = InfoUtils.filterBannedMethod(methods.get(paramClassSrcPath));
            for (MethodInfo methodInfo: methodInfos){
                if (methodInfo.isPublic && !methodInfo.isStatic){
                    VariableInfo newField = VariableInfo.copy(InfoUtils.changeMethodInfoToVariableInfo(methodInfo));
                    newField.variableName = info.variableName+"."+newField.variableName;
                    variableInfos.add(newField);
                }
            }
        }
        return variableInfos;
    }


    public static VariableInfo getVariableInIfStatement(String ifString){
        if (!ifString.contains("if") || !ifString.contains("(") || !ifString.contains(")")){
            return null;
        }
        if (ifString.contains("&&") || ifString.contains("||")){
            return null;
        }
        String ifStatement = ifString.substring(ifString.indexOf("(")+1,ifString.lastIndexOf(")"));
        String var1 = "";
        String var2 = "";
        if (ifStatement.contains(">=") && ifStatement.split(">=").length == 2){
            var1 = ifStatement.split(">=")[0];
            var2 = ifStatement.split(">=")[1];
        }
        else if (ifStatement.contains(">") && ifStatement.split(">").length == 2){
            var1 = ifStatement.split(">")[0];
            var2 = ifStatement.split(">")[1];
        }
        else if (ifStatement.contains("<=") && ifStatement.split("<=").length == 2){
            var1 = ifStatement.split("<=")[0];
            var2 = ifStatement.split("<=")[1];
        }
        else if (ifStatement.contains("<") && ifStatement.split("<").length == 2){
            var1 = ifStatement.split("<")[0];
            var2 = ifStatement.split("<")[1];
        }
        else {
            VariableInfo info = new VariableInfo(ifStatement.trim(),TypeEnum.BOOLEAN,true, null);
            info.isAddon = true;
            return info;
        }
        try {
            MathUtils.parseStringValue(var2);
        } catch (Exception e1){
            try {
                MathUtils.parseStringValue(var1);
                VariableInfo info = new VariableInfo(var2.trim(),TypeEnum.DOUBLE,true, null);
                info.isAddon = true;
                info.priority = 2;
                return info;
            } catch (Exception e2){
                return null;
            }
        }
        VariableInfo info = new VariableInfo(var1.trim(),TypeEnum.DOUBLE,true, null);
        info.isAddon = true;
        info.priority = 2;
        return info;
    }


}
