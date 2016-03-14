package cn.edu.pku.sei.plde.conqueroverfitting.trace.filter;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.gatherer.GathererJava;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.io.File;
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
    public static Map<VariableInfo, List<String>> filter(Map<VariableInfo, List<String>> exceptionVariable, Map<VariableInfo, List<String>> trueValues){
        Map<VariableInfo, List<String>> result = new HashMap<VariableInfo, List<String>>();
        for (Map.Entry<VariableInfo, List<String>> entry: exceptionVariable.entrySet()){
            String variableName = entry.getKey().variableName.contains(".")?entry.getKey().variableName.substring(entry.getKey().variableName.lastIndexOf(".")+1):entry.getKey().variableName;

            List<BoundaryInfo> boundaryList = getSearchBoundaryInfo(entry.getKey());
            if (boundaryList == null){
                continue;
            }
            //根据值在搜索中出现的次数来确定怀疑目标
            for (String value: entry.getValue()){
                int valueCount = BoundaryFilter.countTheValueOccurs(boundaryList,value, entry.getKey().getStringType());
                if (Config.judgeResultOfFilterWithSearchBoundary(boundaryList.size(),valueCount, value, variableName, entry.getKey())){
                    // isNaN=true在修补中总是对的
                    if (variableName.equals("isNaN")){
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
            if (result.containsKey(entry.getKey())){
                continue;
            }

            //如果错误的值较多,直接生成错误值区间

            if (entry.getValue().size()> 30 && entry.getValue().size() < 100) {
                result.put(entry.getKey(),entry.getValue());
                continue;
            }


            //如果是数字变量,将搜索到的值生成区间,如果怀疑变量的值都不在该区间内,则生成该区间
            if (MathUtils.isNumberType(entry.getKey().getStringType())){
                List<Double> interval = generateTrueValueInterval(BoundaryFilter.getBoundaryWithNameAndType(boundaryList, entry.getKey().variableName, entry.getKey().getStringType()));
                if (interval.size() != 2 || entry.getValue().size() < 3){
                    continue;
                }
                //如果区间的最大值与最小值差距过大,则怀疑度减小.
                if ((interval.get(0) / interval.get(1) > 100 || interval.get(1) / interval.get(0) > 100) && Math.abs(interval.get(0)-interval.get(1)) > 100){
                    continue;
                }
                int count = 0;
                for (String value: entry.getValue()){
                    try {
                        Double doubleValue = MathUtils.parseStringValue(value);
                        if (doubleValue < interval.get(0) || doubleValue > interval.get(1)){
                            count ++;
                        }
                    }catch (Exception e){
                        continue;
                    }
                }
                if (count == entry.getValue().size()){
                    entry.getKey().interval = true;
                    result.put(entry.getKey(),new ArrayList<>(Arrays.asList(String.valueOf(interval.get(0)),String.valueOf(interval.get(1)))));
                }
            }
            //如果参数是数字数组,保证每个数组元素都不为NaN
            if (MathUtils.isNumberArray(entry.getKey().getStringType()) && ! result.containsKey(entry.getKey()) && entry.getKey().isParameter){
                result.put(entry.getKey(),new ArrayList<>(Arrays.asList("null")));
                continue;
            }
            /*
            if (!result.containsKey(entry.getKey()) && !entry.getKey().isSimpleType && !entry.getKey().variableName.endsWith("()")){
                //对于复杂的数据结构,不等于null总是一个好的方法
                result.put(entry.getKey(),new ArrayList<String>(Arrays.asList("null")));
            }*/

            if (entry.getKey().isParameter && trueValues.containsKey(entry.getKey()) && MathUtils.isNumberType(entry.getKey().getStringType())){
                List<String> values = new ArrayList<>(entry.getValue());
                values.removeAll(trueValues.get(entry.getKey()));
                if (values.size() != 1){
                    continue;
                }
                String value = values.get(0);
                int count = 0;
                for (String trueValue: trueValues.get(entry.getKey())){
                    if (Double.valueOf(trueValue) < 0 && Double.valueOf(value) >= 0 || Double.valueOf(trueValue) > 0 && Double.valueOf(value) <= 0){
                        count++;
                    }
                }
                if (count == trueValues.get(entry.getKey()).size()){
                    if (Double.valueOf(value) < 0){
                        result.put(entry.getKey(), Arrays.asList("-0"));
                    }
                    else {
                        result.put(entry.getKey(), Arrays.asList("+0"));
                    }
                }
            }

        }
        if (result.size() == 0){
            int boolCount = 0;
            Map.Entry<VariableInfo, List<String>> boolEntry = null;
            for (Map.Entry<VariableInfo, List<String>> entry: exceptionVariable.entrySet()){
                if (entry.getKey().getStringType().equals("BOOLEAN")){
                    boolCount++;
                    boolEntry = entry;
                }
            }
            if (boolCount == 1 && boolEntry!= null && trueValues.containsKey(boolEntry.getKey())){
                result.put(boolEntry.getKey(),boolEntry.getValue());
            }
        }
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
        String variableName = info.variableName.contains(".")?info.variableName.substring(info.variableName.lastIndexOf(".")+1):info.variableName;
        String valueType = info.getStringType();
        ArrayList<String> keywords = new ArrayList<String>();
        keywords.add("if");
        keywords.add(valueType);
        if (info.variableName.startsWith("is") && info.variableName.endsWith("()")){
            keywords.add(info.variableName.substring(0, info.variableName.lastIndexOf("(")));
        }
        else {
            keywords.add(info.variableName);
        }

        File codePackage = new File("experiment/searchcode/" + StringUtils.join(keywords,"-"));
        File simpleCodePackage = new File("experiment/searchcode/" + StringUtils.join(Arrays.asList("if",info.variableName),"-"));
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
        if (filteredList.size() == 0 && !info.isSimpleType){
            filteredList = BoundaryFilter.getBoundaryWithType(boundaryList, info.getStringType());
        }
        return filteredList;
    }



}
