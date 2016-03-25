package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/3/24.
 */
public class BoundarySorter {
    private final String _classSrc;
    private final String _className;
    private final String _code;
    private Suspicious _suspicious;

    public BoundarySorter(Suspicious suspicious, String classSrc){
        _suspicious = suspicious;
        _classSrc = classSrc;
        _className = suspicious.classname();
        _code = FileUtils.getCodeFromFile(classSrc,_className);
    }

    public String sort(Map<String, String> boundary){
        return "";
    }

    private int getBoundaryLevel(){
        return 1;
    }
}
