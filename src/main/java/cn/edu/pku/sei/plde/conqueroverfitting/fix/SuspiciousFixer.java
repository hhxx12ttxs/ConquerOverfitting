package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16-4-13.
 */
public class SuspiciousFixer {
    public Map<ExceptionVariable, List<String>> boundarysMap = new HashMap<>();
    private Map<VariableInfo, List<String>> trueValues;
    private Map<VariableInfo, List<String>> falseValues;
    private List<TraceResult> traceResults;
    private Suspicious suspicious;
    private List<ExceptionVariable> exceptionVariables;
    private String project;
    public SuspiciousFixer(Suspicious suspicious, String project){
        this.suspicious = suspicious;
        this.project = project;
        traceResults = suspicious.getTraceResult();
        trueValues = AbandanTrueValueFilter.getTrueValue(traceResults, suspicious.getAllInfo());
        falseValues = AbandanTrueValueFilter.getFalseValue(traceResults, suspicious.getAllInfo());
    }

    public boolean mainFixProcess(){
        ExceptionExtractor extractor = new ExceptionExtractor(suspicious);
        exceptionVariables = extractor.extract(traceResults);

        List<List<ExceptionVariable>> echelons = extractor.sort();
        for (List<ExceptionVariable> echelon: echelons){
            List<String> boundarys = getIfStrings(echelon);
            if (fixMethodTwo(suspicious, boundarys, project)){
                return true;
            }
            if (fixMethodOne(suspicious, boundarys, project)){
                return true;
            }
        }
        return false;
    }



    private List<String> getIfStrings(List<ExceptionVariable> exceptionVariables){
        List<String> result = new ArrayList<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            result.add(getIfStatementFromBoundary(getBoundary(exceptionVariable)));
        }
        return result;
    }

    private String getIfStatementFromBoundary(List<String> boundary){
        return "if ("+ StringUtils.join(boundary,"||")+"){";
    }

    private List<String> getBoundary(ExceptionVariable exceptionVariable){
        if (!boundarysMap.containsKey(exceptionVariable)){
            List<String> boundarys = BoundaryGenerator.generate(exceptionVariable, trueValues, falseValues, project);
            boundarysMap.put(exceptionVariable, boundarys);
        }
        return boundarysMap.get(exceptionVariable);
    }

    public static boolean fixMethodTwo(Suspicious suspicious, List<String> ifStrings, String project){
        MethodTwoFixer fixer = new MethodTwoFixer(suspicious);
        if (fixer.fix(ifStrings)){
            return true;
        }
        else {
            System.out.println("Fix fail, Try next suspicious...");
            return false;
        }
    }


    public static boolean fixMethodOne(Suspicious suspicious, List<String> ifStrings, String project) {
        ReturnCapturer fixCapturer = new ReturnCapturer(suspicious._classpath,suspicious._srcPath, suspicious._testClasspath, suspicious._testSrcPath);
        MethodOneFixer methodOneFixer = new MethodOneFixer(suspicious, project);
        boolean isSuccess = false;
        for (String test: suspicious.getFailedTest()){
            String testClassName = test.split("#")[0];
            String testMethodName = test.split("#")[1];
            List<Integer> errorLine = suspicious._errorLineMap.get(test);

            //如果全是抛出异常而没有错误Assert行的话
            if (suspicious._assertsMap.get(test)._errorAssertLines.size() == 0){
                suspicious._assertsMap.get(test)._errorAssertLines.add(-1);
            }
            for (int assertLine: suspicious._assertsMap.get(test)._errorAssertLines){
                String fixString = fixCapturer.getFixFrom(testClassName, testMethodName, assertLine, suspicious.classname(), suspicious.functionnameWithoutParam());
                if (suspicious._isConstructor && fixString.contains("return")){
                    continue;
                }
                if (fixString.equals("")){
                    continue;
                }
                Patch patch = new Patch(testClassName, testMethodName, suspicious.classname(), errorLine, ifStrings, fixString);
                boolean result = methodOneFixer.addPatch(patch);
                if (result){
                    isSuccess = true;
                    break;
                }
            }
            if (isSuccess){
                break;
            }
        }
        int finalErrorNums = methodOneFixer.fix();
        if (finalErrorNums == -1){
            System.out.println("Fix fail, Try next suspicious...");
            return false;
        }
        if (finalErrorNums == 0){
            System.out.println("Fix success");
            return true;
        }
        return false;
    }

}
