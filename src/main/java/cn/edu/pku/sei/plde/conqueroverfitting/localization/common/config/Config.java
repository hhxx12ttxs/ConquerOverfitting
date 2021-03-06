package cn.edu.pku.sei.plde.conqueroverfitting.localization.common.config;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.common.synth.StatementType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by Thomas Durieux on 23/03/15.
 */
public class Config {

    public enum NopolMode {
        REPAIR,
        RANKING
    }

    public enum NopolOracle {
        ANGELIC,
        SYMBOLIC
    }

    public enum NopolSynthesis {
        SMT,
        BRUTPOL
    }

    public enum NopolSolver {
        Z3,
        CVC4
    }

    public static Config INSTANCE = new Config();

    private final String filename = "config.ini";
    private final Properties p;


    private int synthesisDepth;
    private boolean collectStaticMethods;
    private boolean collectStaticFields;
    private boolean collectLiterals;
    private boolean onlyOneSynthesisResult;
    private boolean sortExpressions;
    private int maxLineInvocationPerTest;
    private int timeoutMethodInvocation;
    private int maxTime = 60;

    private double addWeight;
    private double subWeight;
    private double mulWeight;
    private double divWeight;
    private double andWeight;
    private double orWeight;
    private double eqWeight;
    private double nEqWeight;
    private double lessEqWeight;
    private double lessWeight;
    private double methodCallWeight;
    private double fieldAccessWeight;
    private double constantWeight;
    private double variableWeight;
    private long timeoutTestExecution;
    private long maxTimeBuildPatch;

    private NopolMode mode = NopolMode.REPAIR;
    private StatementType type = StatementType.CONDITIONAL;
    private NopolSynthesis synthesis = NopolSynthesis.SMT;
    private NopolOracle oracle = NopolOracle.ANGELIC;
    private NopolSolver solver = NopolSolver.Z3;
    private String solverPath;
    private String[] projectSourcePath;
    private String projectClasspath;
    private String[] projectTests;

    private int complianceLevel;

    Config() {
        p = new Properties();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            p.load(classLoader.getResourceAsStream(filename));

            synthesisDepth = Integer.parseInt(p.getProperty("depth", "3"));
            collectStaticMethods = Boolean.parseBoolean(p.getProperty("collectStaticMethod", "false"));
            collectStaticFields = Boolean.parseBoolean(p.getProperty("collectStaticFields", "false"));
            collectLiterals = Boolean.parseBoolean(p.getProperty("collectLiteral", "false"));
            onlyOneSynthesisResult = Boolean.parseBoolean(p.getProperty("onlyOneSynthesisResult", "true"));
            sortExpressions = Boolean.parseBoolean(p.getProperty("sortExpression", "true"));
            maxLineInvocationPerTest = Integer.parseInt(p.getProperty("maxLineInvocationPerTest", "150"));
            timeoutMethodInvocation = Integer.parseInt(p.getProperty("timeoutMethodInvocation", "2000"));

            addWeight = Double.parseDouble(p.getProperty("addOp", "0"));
            subWeight = Double.parseDouble(p.getProperty("subOp", "0"));
            mulWeight = Double.parseDouble(p.getProperty("mulOp", "0"));
            divWeight = Double.parseDouble(p.getProperty("divOp", "0"));
            andWeight = Double.parseDouble(p.getProperty("andOp", "0"));
            orWeight = Double.parseDouble(p.getProperty("orOp", "0"));
            eqWeight = Double.parseDouble(p.getProperty("eqOp", "0"));
            nEqWeight = Double.parseDouble(p.getProperty("neqOpOp", "0"));
            lessEqWeight = Double.parseDouble(p.getProperty("lessEqOp", "0"));
            lessWeight = Double.parseDouble(p.getProperty("lessOp", "0"));
            methodCallWeight = Double.parseDouble(p.getProperty("methodCall", "0"));
            fieldAccessWeight = Double.parseDouble(p.getProperty("fieldAccess", "0"));
            constantWeight = Double.parseDouble(p.getProperty("constant", "0"));
            variableWeight = Double.parseDouble(p.getProperty("variable", "0"));
            timeoutTestExecution = Long.parseLong(p.getProperty("timeoutTestExecution", "5"));
            maxTimeBuildPatch = Long.parseLong(p.getProperty("maxTimeBuildPatch", "15L"));
            complianceLevel = Integer.parseInt(p.getProperty("complianceLevel", "7"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config file", e);
        }
    }

    public int getSynthesisDepth() {
        return synthesisDepth;
    }

    public void setSynthesisDepth(int synthesisDepth) {
        this.synthesisDepth = synthesisDepth;
    }

    public boolean isCollectStaticMethods() {
        return collectStaticMethods;
    }

    public void setCollectStaticMethods(boolean collectStaticMethods) {
        this.collectStaticMethods = collectStaticMethods;
    }

    public boolean isCollectStaticFields() {
        return collectStaticFields;
    }

    public void setCollectStaticFields(boolean collectStaticFields) {
        this.collectStaticFields = collectStaticFields;
    }

    public boolean isCollectLiterals() {
        return collectLiterals;
    }

    public void setCollectLiterals(boolean collectLiterals) {
        this.collectLiterals = collectLiterals;
    }

    public boolean isOnlyOneSynthesisResult() {
        return onlyOneSynthesisResult;
    }

    public void setOnlyOneSynthesisResult(boolean onlyOneSynthesisResult) {
        this.onlyOneSynthesisResult = onlyOneSynthesisResult;
    }

    public boolean isSortExpressions() {
        return sortExpressions;
    }

    public void setSortExpressions(boolean sortExpressions) {
        this.sortExpressions = sortExpressions;
    }

    public int getMaxLineInvocationPerTest() {
        return maxLineInvocationPerTest;
    }

    public void setMaxLineInvocationPerTest(int maxLineInvocationPerTest) {
        this.maxLineInvocationPerTest = maxLineInvocationPerTest;
    }

    public int getTimeoutMethodInvocation() {
        return timeoutMethodInvocation;
    }

    public void setTimeoutMethodInvocation(int timeoutMethodInvocation) {
        this.timeoutMethodInvocation = timeoutMethodInvocation;
    }

    public double getAddWeight() {
        return addWeight;
    }

    public void setAddWeight(double addWeight) {
        this.addWeight = addWeight;
    }

    public double getSubWeight() {
        return subWeight;
    }

    public void setSubWeight(double subWeight) {
        this.subWeight = subWeight;
    }

    public double getMulWeight() {
        return mulWeight;
    }

    public void setMulWeight(double mulWeight) {
        this.mulWeight = mulWeight;
    }

    public double getDivWeight() {
        return divWeight;
    }

    public void setDivWeight(double divWeight) {
        this.divWeight = divWeight;
    }

    public double getAndWeight() {
        return andWeight;
    }

    public void setAndWeight(double andWeight) {
        this.andWeight = andWeight;
    }

    public double getOrWeight() {
        return orWeight;
    }

    public void setOrWeight(double orWeight) {
        this.orWeight = orWeight;
    }

    public double getEqWeight() {
        return eqWeight;
    }

    public void setEqWeight(double eqWeight) {
        this.eqWeight = eqWeight;
    }

    public double getnEqWeight() {
        return nEqWeight;
    }

    public void setnEqWeight(double nEqWeight) {
        this.nEqWeight = nEqWeight;
    }

    public double getLessEqWeight() {
        return lessEqWeight;
    }

    public void setLessEqWeight(double lessEqWeight) {
        this.lessEqWeight = lessEqWeight;
    }

    public double getLessWeight() {
        return lessWeight;
    }

    public void setLessWeight(double lessWeight) {
        this.lessWeight = lessWeight;
    }

    public double getMethodCallWeight() {
        return methodCallWeight;
    }

    public void setMethodCallWeight(double methodCallWeight) {
        this.methodCallWeight = methodCallWeight;
    }

    public double getFieldAccessWeight() {
        return fieldAccessWeight;
    }

    public void setFieldAccessWeight(double fieldAccessWeight) {
        this.fieldAccessWeight = fieldAccessWeight;
    }

    public double getConstantWeight() {
        return constantWeight;
    }

    public void setConstantWeight(double constantWeight) {
        this.constantWeight = constantWeight;
    }

    public double getVariableWeight() {
        return variableWeight;
    }

    public void setVariableWeight(double variableWeight) {
        this.variableWeight = variableWeight;
    }

    public NopolMode getMode() {
        return mode;
    }

    public void setMode(NopolMode mode) {
        this.mode = mode;
    }

    public StatementType getType() {
        return type;
    }

    public void setType(StatementType type) {
        this.type = type;
    }

    public NopolSynthesis getSynthesis() {
        return synthesis;
    }

    public void setSynthesis(NopolSynthesis synthesis) {
        this.synthesis = synthesis;
    }

    public NopolOracle getOracle() {
        return oracle;
    }

    public void setOracle(NopolOracle oracle) {
        this.oracle = oracle;
    }

    public NopolSolver getSolver() {
        return solver;
    }

    public void setSolver(NopolSolver solver) {
        this.solver = solver;
    }

    public String getSolverPath() {
        return solverPath;
    }

    public void setSolverPath(String solverPath) {
        this.solverPath = solverPath;
    }

    public String[] getProjectSourcePath() {
        return projectSourcePath;
    }

    public void setProjectSourcePath(String[] projectSourcePath) {
        this.projectSourcePath = projectSourcePath;
    }

    public String getProjectClasspath() {
        return projectClasspath;
    }

    public void setProjectClasspath(String projectClasspath) {
        this.projectClasspath = projectClasspath;
    }

    public String[] getProjectTests() {
        return projectTests;
    }

    public void setProjectTests(String[] projectTests) {
        this.projectTests = projectTests;
    }

    public int getComplianceLevel() {
        return complianceLevel;
    }

    public void setComplianceLevel(int complianceLevel) {
        this.complianceLevel = complianceLevel;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }


    public long getTimeoutTestExecution() {
        return timeoutTestExecution;
    }

    public void setTimeoutTestExecution(long timeoutTestExecution) {
        this.timeoutTestExecution = timeoutTestExecution;
    }

    public long getMaxTimeBuildPatch() {
        return maxTimeBuildPatch;
    }

    public void setMaxTimeBuildPatch(long maxTimeBuildPatch) {
        this.maxTimeBuildPatch = maxTimeBuildPatch;
    }

    @Override
    public String toString() {
        return "Config{" +
                "synthesisDepth=" + synthesisDepth +
                ", collectStaticMethods=" + collectStaticMethods +
                ", collectStaticFields=" + collectStaticFields +
                ", collectLiterals=" + collectLiterals +
                ", onlyOneSynthesisResult=" + onlyOneSynthesisResult +
                ", sortExpressions=" + sortExpressions +
                ", maxLineInvocationPerTest=" + maxLineInvocationPerTest +
                ", timeoutMethodInvocation=" + timeoutMethodInvocation +
                ", addWeight=" + addWeight +
                ", subWeight=" + subWeight +
                ", mulWeight=" + mulWeight +
                ", divWeight=" + divWeight +
                ", andWeight=" + andWeight +
                ", orWeight=" + orWeight +
                ", eqWeight=" + eqWeight +
                ", nEqWeight=" + nEqWeight +
                ", lessEqWeight=" + lessEqWeight +
                ", lessWeight=" + lessWeight +
                ", methodCallWeight=" + methodCallWeight +
                ", fieldAccessWeight=" + fieldAccessWeight +
                ", constantWeight=" + constantWeight +
                ", variableWeight=" + variableWeight +
                ", mode=" + mode +
                ", type=" + type +
                ", synthesis=" + synthesis +
                ", oracle=" + oracle +
                ", solver=" + solver +
                ", solverPath='" + solverPath + '\'' +
                ", projectSourcePath=" + Arrays.toString(projectSourcePath) +
                ", projectClasspath='" + projectClasspath + '\'' +
                ", projectTests=" + Arrays.toString(projectTests) +
                ", complianceLevel=" + complianceLevel +
                '}';
    }
}
