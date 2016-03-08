package cn.edu.pku.sei.plde.conqueroverfitting.main;


import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/29.
 */
public class Config {

    public static boolean judgeResultOfFilterWithSearchBoundary(int totalCount, int theSameCount, String value, String variableName, VariableInfo info){
        double percentage = (double)theSameCount/(double)totalCount;
        List<String> systemValues = Arrays.asList("Integer.MAX_VALUE","Integer.MIN_VALUE",String.valueOf(Integer.MAX_VALUE), String.valueOf(Integer.MIN_VALUE));
        List<String> specialValues = Arrays.asList("0","1");
        if (percentage > 0.05 && ((systemValues.contains(value) || specialValues.contains(value))&&!info.isFieldVariable)){
            return true;
        }
        if (percentage > 0.1){
            return true;
        }
        if (variableName.equals("isNaN")){
            return true;
        }
        return false;
    }

    public static boolean judgeAsTheSameInFilter(String candidate ,String candidateType, String master, String masterType){
        List<String> minInteger = Arrays.asList("Integer.MIN_VALUE","-2147483648");
        List<String> maxInteger = Arrays.asList("Integer.MAX_VALUE","2147483647");
        if (candidate.equals(master)){
            return true;
        }
        if (MathUtils.isNumberType(candidateType) && MathUtils.isNumberType(masterType)){
            if (master.endsWith(".0")){
                master = master.substring(0, master.lastIndexOf("."));
            }
            if (candidate.endsWith(".0")){
                candidate = candidate.substring(0, candidate.lastIndexOf("."));
            }
            if (StringUtils.isNumeric(candidate) && StringUtils.isNumeric(master)){
                if (Double.valueOf(master).equals(Double.valueOf(candidate))){
                    return true;
                }
            }
        }
        if (master.equals("null") && (candidate.equals("NULL") || candidate.equals("null"))){
            return true;
        }
        if (minInteger.contains(master) && minInteger.contains(candidate)){
            return true;
        }
        if (maxInteger.contains(master) && maxInteger.contains(candidate)){
            return true;
        }
        return false;
    }
}
