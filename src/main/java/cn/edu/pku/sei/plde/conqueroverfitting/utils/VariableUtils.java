package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

/**
 * Created by yanrunfa on 16-4-19.
 */
public class VariableUtils {
    public static boolean isExpression(VariableInfo info){
        return (info.variableName.contains("+") ||
                info.variableName.contains("-") ||
                info.variableName.contains("*") ||
                info.variableName.contains("/") ||
                info.variableName.contains("%") ||
                info.variableName.contains("&") ||
                info.variableName.contains("(") ||
                info.variableName.contains(")") ||
                info.variableName.contains(">") ||
                info.variableName.contains("<")) && !info.variableName.contains("()") ;

    }

    public static boolean isJavaIdentifier(String name){
        if(name == null){
            return false;
        }
        int len = name.length();
        if(len == 0){
            return false;
        }
        if(!Character.isJavaIdentifierStart(name.charAt(0))){
            return false;
        }
        for(int i = 1; i < len; i ++){
            if(!Character.isJavaIdentifierPart(name.charAt(i))){
                return false;
            }
        }
        return true;
    }

}
