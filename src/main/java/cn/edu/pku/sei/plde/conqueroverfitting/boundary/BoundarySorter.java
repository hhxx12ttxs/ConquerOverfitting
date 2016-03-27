package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.InfoUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by yanrunfa on 16/3/24.
 */
public class BoundarySorter {
    private final String _classSrc;
    private final String _className;
    private final String _code;
    private final String[] _methodCode;
    private Suspicious _suspicious;
    private Map<VariableInfo, String> _paramVarBoundary = new HashMap<>();
    private Map<VariableInfo, String> _fieldVarBoundary = new HashMap<>();
    private Map<VariableInfo, String> _localVarBoundary = new HashMap<>();
    private LinkedHashMap<VariableInfo, String> _sortedBoundary = new LinkedHashMap<>();
    private List<List<String>> _boundaryCombinations = new ArrayList<>();

    public BoundarySorter(Suspicious suspicious, String classSrc){
        _suspicious = suspicious;
        _classSrc = classSrc;
        _className = suspicious.classname();
        _code = FileUtils.getCodeFromFile(classSrc,_className);
        _methodCode = CodeUtils.getMethodString(_code, suspicious.functionnameWithoutParam()).split("\n");
    }

    public List<String> sort(Map<VariableInfo, String> boundary){
        if (boundary.size() == 1){
            return Arrays.asList("if("+boundary.values().toArray()[0]+")");
        }
        for (Map.Entry<VariableInfo, String> entry: boundary.entrySet()){
            if (entry.getKey().isParameter){
                _paramVarBoundary.put(entry.getKey(), entry.getValue());
            }
            if (entry.getKey().isLocalVariable){
                _localVarBoundary.put(entry.getKey(), entry.getValue());
            }
            if (entry.getKey().isFieldVariable){
                _fieldVarBoundary.put(entry.getKey(), entry.getValue());
            }
        }
        sortBoundary();
        boundaryCombination();
        return getIfList();
    }

    private List<String> getIfList(){
        List<String> ifStrings = new ArrayList<>();
        for (List<String> boundarys: _boundaryCombinations){
            String ifString = getIfStringFromBoundarys(boundarys);
            ifStrings.add(ifString);
        }
        return ifStrings;
    }

    private String getIfStringFromBoundarys(List<String> boundarys){
        return "if (("+ StringUtils.join(boundarys, ")||(") +"))";
    }

    private void boundaryCombination(){
        _boundaryCombinations = subsets(new ArrayList<String>(_sortedBoundary.values()));
    }

    private List<List<String>> subsets(List<String> nums) {
        List<List<String>> res = new ArrayList<>();
        List<String> each = new ArrayList<>();
        helper(res, each, 0, nums);
        return res;
    }
    private void helper(List<List<String>> res, List<String> each, int pos, List<String> n) {
        if (pos <= n.size()) {
            res.add(each);
        }
        for (int i = pos; i < n.size(); i++) {
            each.add(n.get(i));
            helper(res, new ArrayList<>(each), i + 1, n);
            each.remove(each.size() - 1);
        }
        return;
    }

    private void sortBoundary(){
        for (Map.Entry<VariableInfo, String> entry: _paramVarBoundary.entrySet()){
            _sortedBoundary.put(entry.getKey(), entry.getValue());
        }
        TreeMap<Integer,Map.Entry<VariableInfo, String>> boundaryLevel = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        for (Map.Entry<VariableInfo, String> entry: _localVarBoundary.entrySet()){
            boundaryLevel.put(getLastAssignLine(entry.getKey()),entry);
        }
        for (Map.Entry<VariableInfo, String> entry: boundaryLevel.values()){
            _sortedBoundary.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<VariableInfo, String> entry: _fieldVarBoundary.entrySet()){
            _sortedBoundary.put(entry.getKey(), entry.getValue());
        }
    }

    private int getLastAssignLine(VariableInfo info){
        for (int i = 0; i < _methodCode.length; i++){
            if (_methodCode[i].trim().matches(info.variableName+"\\s*=.*")){
                return i;
            }
        }
        return 0;
    }
}
