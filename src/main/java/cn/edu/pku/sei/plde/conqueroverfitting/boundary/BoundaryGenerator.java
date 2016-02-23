package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.container.map.DoubleMap;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/2/23.
 */
public class BoundaryGenerator {


    public static String generate(Map.Entry<VariableInfo, List<String>> entry, String project){
        String varType = entry.getKey().isSimpleType?entry.getKey().variableSimpleType.toString():entry.getKey().otherType;
        if (entry.getValue().size() == 1){
            return "if (" + entry.getKey().variableName + " == " + entry.getValue().get(0) + ")";
        }
        else if (varType.equals("INT")
                || varType.equals("FLOAT")
                || varType.equals("DOUBLE")
                || varType.equals("LONG")
                || varType.equals("SHORT")
                ){
            BoundaryCollect boundaryCollect = new BoundaryCollect("experiment/searchcode/"+project+"-"+entry.getKey().variableName);
            List<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
            List<BoundaryInfo> filteredList = BoundaryFilter.getBoundaryWithNameAndType(boundaryList, entry.getKey().variableName, varType);

            double smallestValue = Double.valueOf(entry.getValue().get(0));
            for (String value: entry.getValue()){
                if (smallestValue > Double.valueOf(value)){
                    smallestValue = Double.valueOf(value);
                }
            }

            double biggestBoundary = Double.valueOf(filteredList.get(0).value);
            for (BoundaryInfo info: filteredList){
                if (biggestBoundary > Double.valueOf(info.value) && biggestBoundary <= smallestValue){
                    biggestBoundary = Double.valueOf(info.value);
                }
            }
            return "if (" + entry.getKey().variableName + " >= " + biggestBoundary + ")";
        }
        return null;
    }
}
