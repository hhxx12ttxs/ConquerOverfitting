package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by yanrunfa on 16-4-16.
 */
public class ExceptionSorter {
    private final String _classSrc;
    private final String _className;
    private final String _code;
    private final String[] _methodCode;
    private Suspicious _suspicious;
    private List<ExceptionVariable> _variables = new ArrayList<>();
    private List<ExceptionVariable> _paramVariables = new ArrayList<>();
    private List<ExceptionVariable> _fieldVariables = new ArrayList<>();
    private List<ExceptionVariable> _localVariables = new ArrayList<>();
    private List<ExceptionVariable> _sortedVariable = new ArrayList<>();
    private List<List<ExceptionVariable>> _variableCombinations = new ArrayList<>();

    public ExceptionSorter(Suspicious suspicious){
        _suspicious = suspicious;
        _classSrc = suspicious._srcPath;
        _className = suspicious.classname();
        _code = FileUtils.getCodeFromFile(_classSrc,_className);
        _methodCode = CodeUtils.getMethodString(_code, suspicious.functionnameWithoutParam(), suspicious.getDefaultErrorLine()).split("\n");
    }


    public List<List<ExceptionVariable>> sort(List<ExceptionVariable> exceptionVariables){
        _variables = exceptionVariables;
        List<List<ExceptionVariable>> result = new ArrayList<>();
        //如果怀疑变量只有一个，直接扔进第一梯队
        if (exceptionVariables.size() == 1){
            result.add(exceptionVariables);
            return result;
        }

        //如果怀疑变量有两个，这两个变量都是数组型函数参数的元素，而且类型相同，将这两个同时放入第一梯队，
        if (exceptionVariables.size() == 2){
            VariableInfo info1 = exceptionVariables.get(0).variable;
            VariableInfo info2 = exceptionVariables.get(1).variable;
            if (TypeUtils.isArrayFromName(info1.variableName) && TypeUtils.isArrayFromName(info2.variableName) &&
                    info1.getStringType().equals(info2.getStringType())){
                if (info1.isParameter && info2.isParameter){
                    result.add(exceptionVariables);
                    return result;
                }
            }
        }

        for (ExceptionVariable exceptionVariable: exceptionVariables){
            if (exceptionVariable.variable.isParameter){
                _paramVariables.add(exceptionVariable);
            }
            if (exceptionVariable.variable.isLocalVariable){
                _localVariables.add(exceptionVariable);
            }
            if (exceptionVariable.variable.isFieldVariable){
                _fieldVariables.add(exceptionVariable);
            }
        }
        sortBoundary();
        boundaryCombination();
        return _variableCombinations;
    }

    private void boundaryCombination(){
        _variableCombinations = subsets(_sortedVariable);
        Collections.sort(_variableCombinations, new Comparator<List<ExceptionVariable>>() {
            @Override
            public int compare(List<ExceptionVariable> o1, List<ExceptionVariable> o2) {
                return Integer.valueOf(o1.size()).compareTo(o2.size());
            }
        });
    }

    private <T> List<List<T>> subsets(List<T> nums) {
        List<List<T>> res = new ArrayList<>();
        List<T> each = new ArrayList<>();
        helper(res, each, 0, nums);
        return res;
    }

    private <T> void helper(List<List<T>> res, List<T> each, int pos, List<T> n) {
        if (pos <= n.size() && each.size()> 0) {
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
        TreeMap<Integer,ExceptionVariable> boundaryLevel = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        //根据最后赋值的行确定优先度
        for (ExceptionVariable exceptionVariable: _variables){
            int lastAssignLine = getLastAssignLine(exceptionVariable.variable);
            if (lastAssignLine == 0 && !exceptionVariable.variable.isLocalVariable){
                continue;
            }
            while (boundaryLevel.containsKey(lastAssignLine)){
                lastAssignLine++;
            }
            boundaryLevel.put(lastAssignLine,exceptionVariable);
        }
        for (ExceptionVariable exceptionVariable: boundaryLevel.values()){
            _sortedVariable.add(exceptionVariable);
        }

        //没有经过赋值的参数变量最优先,插入list最前端
        for (ExceptionVariable exceptionVariable: _paramVariables){
            _sortedVariable.add(0,exceptionVariable);
        }
        //没有经过赋值的类变量优先度最低，放在list的最胡
        for (ExceptionVariable exceptionVariable: _fieldVariables){
            _sortedVariable.add(exceptionVariable);
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
