package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

/**
 * Created by yanrunfa on 16-4-19.
 */
public class VariableUtils {
    public static boolean isExpression(VariableInfo info){
        return info.variableName.contains("+") ||
                info.variableName.contains("-") ||
                info.variableName.contains("*") ||
                info.variableName.contains("/") ||
                info.variableName.contains("%") ||
                info.variableName.contains("&") ||
                info.variableName.contains("(") ||
                info.variableName.contains(")") ||
                info.variableName.contains(">") ||
                info.variableName.contains("<");

    }

}
