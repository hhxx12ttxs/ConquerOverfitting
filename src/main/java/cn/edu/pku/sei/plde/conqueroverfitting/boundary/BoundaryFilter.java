package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.main.Config;

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
     * @param boundaryInfos
     * @param name
     * @param type
     * @return
     */
    public static List<BoundaryInfo> getBoundaryWithNameAndType(List<BoundaryInfo> boundaryInfos, String name, String type){
        List<BoundaryInfo> result = new ArrayList<BoundaryInfo>();
        for (BoundaryInfo info: boundaryInfos){
            if (info.isSimpleType && info.variableSimpleType==null){
                continue;
            }
            if (!info.isSimpleType && info.otherType == null){
                continue;
            }
            String infoType = info.isSimpleType?info.variableSimpleType.toString():info.otherType;
            if (info.name.equals(name) && infoType.equals(type)){
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
            if (Config.judgeAsTheSameInFilter(info.value, infoType, value, type)){
                count ++;
            }
        }
        return count;
    }


    /**
     *
     * @param boundaryInfos
     * @param value
     * @param type
     * @return
     */
    public static List<BoundaryInfo> getBoundaryWithValueSmaller(List<BoundaryInfo> boundaryInfos, String value, String type){
        List<BoundaryInfo> result = new ArrayList<BoundaryInfo>();
        for (BoundaryInfo info: boundaryInfos){
            if (Double.valueOf(info.value) <= Double.valueOf(value)){
                result.add(info);
            }
        }
        return result;
    }
}
