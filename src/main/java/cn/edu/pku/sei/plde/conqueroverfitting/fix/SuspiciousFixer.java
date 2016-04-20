package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;


import java.util.*;

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
            Map<String, String> boundarys = new HashMap<>();
            for (Map.Entry<String,List<ExceptionVariable>> assertEchelon: classifyWithAssert(echelon).entrySet()){
                boundarys.put(assertEchelon.getKey(), getIfStrings(assertEchelon.getValue()));
            }
            if (fixMethodTwo(suspicious, boundarys.values(), project)){
                return true;
            }
            if (fixMethodOne(suspicious, boundarys, project)){
                return true;
            }
        }
        return false;
    }

    private Map<String, List<ExceptionVariable>> classifyWithAssert(List<ExceptionVariable> exceptionVariables){
        Map<String, List<ExceptionVariable>> result = new HashMap<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            if (!result.containsKey(exceptionVariable.getAssertMessage())){
                List<ExceptionVariable> variables = new ArrayList<>();
                variables.add(exceptionVariable);
                result.put(exceptionVariable.getAssertMessage(),variables);
            }
            else {
                result.get(exceptionVariable.getAssertMessage()).add(exceptionVariable);
            }
        }
        return result;
    }


    private String getIfStrings(List<ExceptionVariable> exceptionVariables){
        List<String> result = new ArrayList<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            result.addAll(getBoundary(exceptionVariable));
        }
        return getIfStatementFromBoundary(result);
    }

    private String getIfStatementFromBoundary(List<String> boundary){
        return "if ("+ StringUtils.join(boundary,"||")+")";
    }

    private List<String> getBoundary(ExceptionVariable exceptionVariable){
        if (!boundarysMap.containsKey(exceptionVariable)){
            List<String> boundarys = BoundaryGenerator.generate(exceptionVariable, trueValues, falseValues, project);
            boundarysMap.put(exceptionVariable, boundarys);
        }
        return boundarysMap.get(exceptionVariable);
    }

    public static boolean fixMethodTwo(Suspicious suspicious, Collection<String> ifStrings, String project){
        List<String> list = new ArrayList<>(ifStrings);
        return fixMethodTwo(suspicious, list, project);
    }

    public static boolean fixMethodTwo(Suspicious suspicious, List<String> ifStrings, String project){
        MethodTwoFixer fixer = new MethodTwoFixer(suspicious);
        return fixer.fix(ifStrings);
    }


    public static boolean fixMethodOne(Suspicious suspicious,Map<String, String> ifStrings, String project) {
        ReturnCapturer fixCapturer = new ReturnCapturer(suspicious._classpath,suspicious._srcPath, suspicious._testClasspath, suspicious._testSrcPath);
        MethodOneFixer methodOneFixer = new MethodOneFixer(suspicious, project);
        String code = FileUtils.getCodeFromFile(suspicious._srcPath, suspicious.classname());
        for (Map.Entry<String, String> ifString: ifStrings.entrySet()){
            String testClassName = ifString.getKey().split("#")[0];
            String testMethodName = ifString.getKey().split("#")[1];
            List<Integer> errorLine = suspicious._errorLineMap.get(testClassName+"#"+testMethodName);
            int assertLine = Integer.valueOf(ifString.getKey().split("#")[2]);
            if (!CodeUtils.getLineFromCode(code,assertLine).contains("assert")){
                assertLine = -1;
            }
            String fixString = fixCapturer.getFixFrom(testClassName, testMethodName, assertLine, suspicious.classname(), suspicious.functionnameWithoutParam());
            if (suspicious._isConstructor && fixString.contains("return")){
                continue;
            }
            if (fixString.equals("")){
                continue;
            }
            Patch patch = new Patch(testClassName, testMethodName, suspicious.classname(), errorLine, Arrays.asList(ifString.getValue()), fixString);
            boolean result = methodOneFixer.addPatch(patch);
            if (result){
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
