package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanrunfa on 16/2/22.
 */
public class BoundaryFilter {

    /**
     *
     * @param boundaryInfos The boundary info list
     * @param name the specific name
     * @return the boundary info with specific name.
     */
    public static List<BoundaryInfo> getBoundaryWithName(List<BoundaryInfo> boundaryInfos, String name){
        List<BoundaryInfo> result = new ArrayList<BoundaryInfo>();
        for (BoundaryInfo info: boundaryInfos){
            if (info.name.equals(name)){
                result.add(info);
            }
        }
        return result;
    }

    /**
     *
     * @param boundaryInfos The boundary info list
     * @param value the specific value
     * @param type the specific type
     * @return the count of boundary info has the specific value
     */
    public static int countTheValueOccurs(List<BoundaryInfo> boundaryInfos, String value, String type){
        int count = 0;
        for (BoundaryInfo info: boundaryInfos){
            if (info.isSimpleType && info.variableSimpleType==null){
                continue;
            }
            if (!info.isSimpleType && info.otherType == null){
                continue;
            }
            String infoType = info.isSimpleType?info.variableSimpleType.toString():info.otherType;
            if (info.value.equals(value) && infoType.equals(type)){
                count ++;
            }
        }
        return count;
    }

}
