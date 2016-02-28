package cn.edu.pku.sei.plde.conqueroverfitting.main;


import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/29.
 */
public class Config {

    public static boolean judgeResultOfFilterWithSearchBoundary(int totalCount, int theSameCount){
        if (((double)theSameCount/(double)totalCount) > 0.1){
            return true;
        }
        return false;
    }

    public static boolean judgeAsTheSameInFilter(String candidate ,String candidateType, String master, String masterType){
        List<String> numberType = Arrays.asList("INT","DOUBLE","FLOAT","SHORT","LONG");
        if (candidate.equals(master)){
            return true;
        }
        else if (numberType.contains(candidateType) && numberType.contains(masterType)){
            if (master.endsWith(".0")){
                master = master.substring(0, master.lastIndexOf("."));
            }
            if (candidate.endsWith(".0")){
                candidate = candidateType.substring(0, candidate.lastIndexOf("."));
            }
            if (StringUtils.isNumeric(candidate) && StringUtils.isNumeric(master)){
                if (Double.valueOf(master).equals(Double.valueOf(candidate))){
                    return true;
                }
            }
        }
        else if (master.equals("null") && (candidate.equals("NULL") || candidate.equals("null"))){
            return true;
        }
        return false;
    }
}
