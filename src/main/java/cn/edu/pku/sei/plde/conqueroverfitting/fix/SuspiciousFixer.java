package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.BoundaryGenerator;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.Interval;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Localization;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionExtractor;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.ExceptionVariable;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.TraceResult;
import cn.edu.pku.sei.plde.conqueroverfitting.trace.filter.AbandanTrueValueFilter;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.MathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.TestUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import com.google.common.collect.Sets;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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
    private int errorTestNum;
    private long searchTime = 0;
    public SuspiciousFixer(Suspicious suspicious, String project){
        this.suspicious = suspicious;
        this.project = project;
        traceResults = suspicious.getTraceResult();
        trueValues = AbandanTrueValueFilter.getTrueValue(traceResults, suspicious.getAllInfo());
        falseValues = AbandanTrueValueFilter.getFalseValue(traceResults, suspicious.getAllInfo());
        errorTestNum = TestUtils.getFailTestNumInProject(project);
    }

    public boolean mainFixProcess(){
        ExceptionExtractor extractor = new ExceptionExtractor(suspicious);
        Map<Integer, List<TraceResult>> traceResultWithLine = traceResultClassify(traceResults);
        Map<Integer, List<TraceResult>> firstToGo = new TreeMap<Integer, List<TraceResult>>(new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return integer.compareTo(t1);
            }
        });
         for (Map.Entry<Integer, List<TraceResult>> entry: traceResultWithLine.entrySet()){
            if (suspicious.tracedErrorLine.contains(entry.getKey())){
                firstToGo.put(entry.getKey(), entry.getValue());
            }
         }
        for (Map.Entry<Integer, List<TraceResult>> entry: firstToGo.entrySet()){
            if (fixInLineWithTraceResult(entry.getKey(), entry.getValue(), extractor)){
                return true;
            }
        }
        for (Map.Entry<Integer, List<TraceResult>> entry: traceResultWithLine.entrySet()){
            if (firstToGo.containsKey(entry.getKey())){
                continue;
            }
            if (fixInLineWithTraceResult(entry.getKey(), entry.getValue(), extractor)){
                return true;
            }
        }
        return false;
    }

    private boolean fixInLineWithTraceResult(int line, List<TraceResult> traceResults, ExceptionExtractor extractor){
        trueValues = AbandanTrueValueFilter.getTrueValue(traceResults, suspicious.getAllInfo());
        falseValues = AbandanTrueValueFilter.getFalseValue(traceResults, suspicious.getAllInfo());
        exceptionVariables = extractor.extract(suspicious,traceResults);
        List<List<ExceptionVariable>> echelons = extractor.sort();
        for (List<ExceptionVariable> echelon: echelons) {
            Map<String, List<String>> boundarys = new HashMap<>();
            for (Map.Entry<String, List<ExceptionVariable>> assertEchelon : classifyWithAssert(echelon).entrySet()) {
                boundarys.put(assertEchelon.getKey(), getIfStrings(echelon));
            }
            if (fixMethodTwo(suspicious, boundarys, project, line, errorTestNum, false)) {
                printPatchMessage(suspicious, project, getAllBoundarys(boundarys.values()), exceptionVariables, echelon);
                return true;
            }
            if (fixMethodOne(suspicious, boundarys, project, line, errorTestNum, false)) {
                printPatchMessage(suspicious, project, getAllBoundarys(boundarys.values()), exceptionVariables, echelon);
                return true;

            }
        }
        return false;
    }

    private List<ExceptionVariable> sortWithVariable(List<ExceptionVariable> variables){
        Map<String,ExceptionVariable> result = new HashMap<>();
        for (ExceptionVariable exceptionVariable: variables){
            if (!result.containsKey(exceptionVariable.name)){
                result.put(exceptionVariable.name, exceptionVariable);
            }
            else {
                result.get(exceptionVariable.name).values.addAll(exceptionVariable.values);
            }
        }
        return new ArrayList<>(result.values());
    }

    private Set<String> getAllBoundarys(Collection<List<String>> boundarys){
        Set<String> sets = new HashSet<>();
        for (List<String> list: boundarys){
            sets.addAll(list);
        }
        return sets;
    }

    private void printPatchMessage(Suspicious suspicious,String project, Set<String> boundarys, List<ExceptionVariable> exceptionVariables, List<ExceptionVariable> echelon){
        File recordPackage = new File(System.getProperty("user.dir")+"/patch/");
        recordPackage.mkdirs();
        File recordFile = new File(recordPackage.getAbsolutePath()+"/"+project);
        try {
            if (!recordFile.exists()){
                recordFile.createNewFile();
            }
            FileWriter writer = new FileWriter(recordFile,true);
            writer.write("==================================\n");
            writer.write("boundary of suspicious: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"\n");
            for (String boundary: boundarys){
                writer.write(boundary+"\n");
            }
            writer.write("\n");
            writer.write("suspicious variable of suspicious before sort: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"\n");
            for (ExceptionVariable variable: exceptionVariables){
                writer.write(variable.name+" = "+variable.values.toString()+"\n");
            }
            writer.write("\n");
            writer.write("variable echelon of suspicious before search: "+suspicious.classname()+"#"+suspicious.functionnameWithoutParam()+"\n");
            for (ExceptionVariable variable: echelon){
                writer.write(variable.name+" = "+variable.values.toString()+"\n");
            }
            writer.write("==================================\n");
            writer.write("Search Boundary Cost Time: "+searchTime+"\n");
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }



    private Map<Integer, List<TraceResult>> traceResultClassify(List<TraceResult> traceResults){
        Map<Integer, List<TraceResult>> result = new TreeMap<Integer, List<TraceResult>>(new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return integer.compareTo(t1);
            }
        });
        for (TraceResult traceResult: traceResults){
            if (!result.containsKey(traceResult._traceLine)){
                List<TraceResult> results = new ArrayList<>();
                results.add(traceResult);
                result.put(traceResult._traceLine, results);
            }
            else {
                result.get(traceResult._traceLine).add(traceResult);
            }
        }
        return result;
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


    private List<String> getIfStrings(List<ExceptionVariable> exceptionVariables){
        List<String> returnList = new ArrayList<>();
        Map<ExceptionVariable, ArrayList<String>> result = new HashMap<>();
        for (ExceptionVariable exceptionVariable: exceptionVariables){
            ArrayList<String> boundarys = new ArrayList<>(getBoundary(exceptionVariable));
            boolean addedFlag = false;
            for (Map.Entry<ExceptionVariable, ArrayList<String>> entry: result.entrySet()){
                if (entry.getKey().name.equals(exceptionVariable.name)){
                    entry.getValue().removeAll(boundarys);
                    entry.getValue().addAll(boundarys);
                    addedFlag = true;
                    break;
                }
            }
            if (!addedFlag){
                result.put(exceptionVariable, boundarys);
            }
        }
        for (List<String> list: combineIntervals(result)){
            returnList.add(replaceSpecialNumber(getIfStatementFromBoundary(list)));
        }
        return returnList;
    }

    private List<List<String>> combineIntervals(Map<ExceptionVariable, ArrayList<String>> boundarysMap){
        List<List<String>> returnList = new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (Map.Entry<ExceptionVariable, ArrayList<String>> entry: boundarysMap.entrySet()){
            if (!MathUtils.isNumberType(entry.getKey().type) || entry.getValue().size() <= 1){
                for (String value: entry.getValue()){
                    returnList.add(Arrays.asList(value));
                }
                result.addAll(entry.getValue());
                continue;
            }
            ArrayList<Interval> intervals = new ArrayList<>();
            for (String interval: entry.getValue()){
                if (interval.contains("||")){
                    intervals.add(new Interval(interval.split("||")[0]));
                    intervals.add(new Interval(interval.split("||")[1]));
                }
                else {
                    intervals.add(new Interval(interval));
                }
                if (entry.getValue().size()>1){
                    returnList.add(Arrays.asList(interval));
                }
            }
            List<Interval> mergeResult;
            if (entry.getKey().type.toLowerCase().equals("int")){
                mergeResult = MathUtils.mergetIntInterval(intervals);
            }
            else {
                mergeResult = MathUtils.mergetDoubleInterval(intervals);
            }
            for (Interval interval: mergeResult){
                result.add("("+interval.toString(entry.getKey().name, entry.getKey().type)+")");
            }
        }
        returnList.add(result);
        return returnList;
    }



    private String getIfStatementFromBoundary(List<String> boundary){
        if (boundary.size() == 0){
            return "";
        }
        return "if ("+ StringUtils.join(boundary,"||")+")";
    }

    private List<String> getBoundary(ExceptionVariable exceptionVariable){
        if (!boundarysMap.containsKey(exceptionVariable)){
            long startTime = System.currentTimeMillis();
            List<String> boundarys = BoundaryGenerator.generate(suspicious,exceptionVariable, trueValues, falseValues, project);
            searchTime += System.currentTimeMillis()-startTime;
            boundarysMap.put(exceptionVariable, boundarys);
        }
        return boundarysMap.get(exceptionVariable);
    }


    public static boolean fixMethodTwo(Suspicious suspicious, Map<String, List<String>> ifStrings, String project, int errorLine, int errorTestNum, boolean debug){
        if (ifStrings.size() == 0){
            return false;
        }
        MethodTwoFixer fixer = new MethodTwoFixer(suspicious, errorTestNum);
        return fixer.fix(ifStrings, Sets.newHashSet(errorLine), project, debug);
    }


    public static boolean fixMethodOne(Suspicious suspicious,Map<String, List<String>> ifStrings, String project, int errorLine, int errorTestNum, boolean debug) {
        if (ifStrings.size() == 0){
            return false;
        }
        ReturnCapturer fixCapturer = new ReturnCapturer(suspicious._classpath,suspicious._srcPath, suspicious._testClasspath, suspicious._testSrcPath);
        MethodOneFixer methodOneFixer = new MethodOneFixer(suspicious, project,errorTestNum);
        for (Map.Entry<String, List<String>> ifString: ifStrings.entrySet()){
            String testClassName = ifString.getKey().split("#")[0];
            String testMethodName = ifString.getKey().split("#")[1];
            int assertLine = Integer.valueOf(ifString.getKey().split("#")[2]);
            if (assertLine == -1){
                testClassName = suspicious._failTests.get(0).split("#")[0];
                testMethodName = suspicious._failTests.get(0).split("#")[1];
                if (suspicious._assertsMap.containsKey(suspicious._failTests.get(0))){
                    if (suspicious._assertsMap.get(suspicious._failTests.get(0))._errorAssertLines.size()>0){
                        assertLine = suspicious._assertsMap.get(suspicious._failTests.get(0))._errorAssertLines.get(0);
                    }
                }
            }
            if (!CodeUtils.getLineFromCode(FileUtils.getCodeFromFile(suspicious._testSrcPath, testClassName),assertLine).contains("assert")){
                assertLine = -1;
            }
            String fixString = fixCapturer.getFixFrom(testClassName, testMethodName, assertLine, suspicious.classname(), suspicious.functionnameWithoutParam());

            if (CodeUtils.isValue(fixString)){
                List<String> ifStatement = ifString.getValue();
                List<String> bannedStatement = new ArrayList<>();
                for (String statemnt: ifStatement){
                    if (!ifStringFilter(statemnt)){
                        bannedStatement.add(statemnt);
                    }
                }
                ifStatement.removeAll(bannedStatement);
                if (ifStatement.size() == 0){
                    return false;
                }


            }
            if (suspicious._isConstructor && fixString.contains("return")){
                continue;
            }
            if (fixString.equals("")){
                continue;
            }
            Patch patch;
            if (errorLine == 0){
                patch = new Patch(testClassName, testMethodName, suspicious.classname(), suspicious._errorLineMap.get(testClassName+"#"+testMethodName), ifString.getValue(), fixString);
            }
            else {
                patch = new Patch(testClassName, testMethodName, suspicious.classname(), Arrays.asList(errorLine), ifString.getValue(), fixString);
            }
            boolean result = methodOneFixer.addPatch(patch);
            if (result){
                if (debug){
                    return true;
                }
                break;
            }
        }
        if (!debug){
            int finalErrorNums = methodOneFixer.fix();
            return finalErrorNums != -1;
        }
        return false;
    }


    private static boolean ifStringFilter(String ifStatement){
        if (ifStatement.contains("==") || ifStatement.contains("!=") || ifStatement.contains("equals") || ifStatement.contains("instanceof")){
            return true;
        }
        if (ifStatement.contains("MAX_VALUE") || ifStatement.contains("MIN_VALUE")){
            return true;
        }
        if (ifStatement.contains(">") && !ifStatement.contains("<")){
            return true;
        }
        if (ifStatement.contains("<") && !ifStatement.contains(">")){
            return true;
        }
        if (!ifStatement.contains("(") || !ifStatement.contains(")")){
            return false;
        }
        ifStatement = ifStatement.substring(ifStatement.indexOf("(")+1, ifStatement.lastIndexOf(")"));
        if (ifStatement.startsWith("(")){
            ifStatement = ifStatement.substring(1);
        }
        if (ifStatement.endsWith(")")){
            ifStatement = ifStatement.substring(0, ifStatement.length()-1);
        }
        if (ifStatement.contains("&&") && ifStatement.contains(">=") && ifStatement.contains("<=") && !ifStatement.contains("||")){
            String statement1 = ifStatement.split("&&")[0];
            String statement2 = ifStatement.split("&&")[1];
            statement1 = statement1.contains(">=")?statement1.split(">=")[1]:statement1.split("<=")[1];
            statement2 = statement2.contains(">=")?statement2.split(">=")[1]:statement2.split("<=")[1];
            return statement1.trim().equals(statement2);
        }
        return false;
    }

    private static String replaceSpecialNumber(String ifString){
        ifString = ifString.replace(String.valueOf(Integer.MIN_VALUE),"Integer.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Integer.MAX_VALUE),"Integer.MAX_VALUE");
        ifString = ifString.replace("-2.147483648E9","Integer.MIN_VALUE");
        ifString = ifString.replace("2.147483647E9","Integer.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Long.MIN_VALUE),"Long.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Long.MAX_VALUE),"Long.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Double.MIN_VALUE),"Double.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Double.MAX_VALUE),"Double.MAX_VALUE");
        ifString = ifString.replace(String.valueOf(Short.MIN_VALUE),"Short.MIN_VALUE");
        ifString = ifString.replace(String.valueOf(Short.MAX_VALUE),"Short.MAX_VALUE");
        return ifString;
    }
}
