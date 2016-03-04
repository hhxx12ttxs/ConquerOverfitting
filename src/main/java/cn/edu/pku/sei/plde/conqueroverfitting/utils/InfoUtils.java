package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/23.
 */
public class InfoUtils {
    public static final List<String> BANNED_VAR_NAME = Arrays.asList("serialVersionUID","toString()");

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
        result.addAll(variableInfos);
        result.addAll(changeMethodInfoToVariableInfo(methodInfos));
        return result;
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
            result.add(info);
        }
        return result;
    }
}
