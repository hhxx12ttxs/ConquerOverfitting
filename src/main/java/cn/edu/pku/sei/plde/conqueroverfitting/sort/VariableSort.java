package cn.edu.pku.sei.plde.conqueroverfitting.sort;

import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.IdentifierCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import java.util.*;

/**
 * Created by yjxxtd on 4/4/16.
 */
public class VariableSort {
    private Set<String> suspiciousVariableSet;
    private String statements;

    private List<String> statementList;
    private Map<String, Set<String>> dependencyMap;
    private Map<String, Integer> inDegree;
    private List<List<String>> sortVariable;

    public VariableSort(Set<String> suspiciousVariableSet, String statements) {
        this.suspiciousVariableSet = suspiciousVariableSet;
        this.statements = statements;

        this.statementList = new ArrayList<String>();
        this.dependencyMap = new HashMap<String, Set<String>>();
        this.inDegree = new HashMap<String, Integer>();
        sortVariable = new ArrayList<List<String>>();
        for (String variable : suspiciousVariableSet) {
            inDegree.put(variable, 0);
        }

        preProcess();
        getDependency();
        topologicalSort();
    }

    public List<List<String>> getSortVariable() {
        return sortVariable;
    }

    private void preProcess() {
        String[] lines = statements.split("\n");
        for (String line : lines) {
            statementList.add(0, line);
        }
    }

    private ArrayList<String> getIdentifierList(String source, int kind) {
        ASTNode root = JDTUtils.createASTForSource(source, kind);
        IdentifierCollectVisitor identifierCollectVisitor = new IdentifierCollectVisitor();
        root.accept(identifierCollectVisitor);
        return identifierCollectVisitor.getIdentifierList();
    }

    private String getLeftHand(String statement, int index) {
        String left = statement.substring(0, index).trim();
        if (!left.contains(" ")) {
            return left;
        } else {
            int index2 = left.lastIndexOf(" ");
            return left.substring(index2 + 1, left.length());
        }
    }

    private String getRightHand(String statement, int index) {
        String right = statement.substring(index, statement.length()).trim();
        if (right.contains(";")) {
            right = right.replace(";", "");
        }
        return right;
    }

    private String getOper(String statement){
        if(statement.contains("+=")){
            return "+=";
        }
        else if(statement.contains("-=")){
            return "-=";
        }
        else if(statement.contains("*=")){
            return "*=";
        }
        else if(statement.contains("/=")){
            return "/=";
        }
        else if(statement.contains("=")){
            return "=";
        }
        return null;
    }
    private void getDependency() {
        Set<String> leftHands = new HashSet<String>();
        for (String statement : statementList) {
            if (statement == null) {
                continue;
            }
            if (!statement.contains("=") || statement.contains(">=") || statement.contains("<=") || statement.contains("==") || statement.contains("!=")) {
                continue;
            }

            String oper = getOper(statement);
            int index = statement.indexOf(oper);
            String leftHand = getLeftHand(statement, index);

            if (leftHands.contains(leftHand)) {
                continue;
            }

            String rightHandExpression = getRightHand(statement, index + oper.length());

            Set<String> rightHandSet = new HashSet<String>();
            List<String> identifierInRightHand = getIdentifierList(rightHandExpression, ASTParser.K_EXPRESSION);
            for (String identifier : identifierInRightHand) {
                if (leftHand.equals(identifier)) {
                    continue;
                }
                rightHandSet.add(identifier);
            }

            if (rightHandSet.size() == 0) {
                continue;
            }

            leftHands.add(leftHand);

            for (String rightHand : rightHandSet) {
                if (dependencyMap.containsKey(rightHand) && dependencyMap.get(rightHand).contains(leftHand)) {
                    continue;
                }
                if (dependencyMap.containsKey(leftHand)) {
                    dependencyMap.get(leftHand).add(rightHand);
                } else {
                    Set<String> set = new HashSet<String>();
                    set.add(rightHand);
                    dependencyMap.put(leftHand, set);
                }
                if (inDegree.containsKey(rightHand)) {
                    inDegree.put(rightHand, inDegree.get(rightHand) + 1);
                } else {
                    inDegree.put(rightHand, 1);
                }
                if(!inDegree.containsKey(leftHand)) {
                    inDegree.put(leftHand, 0);
                }
            }

        }
    }


    private void topologicalSort() {
        Queue<String> queue = new LinkedList<String>();

        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();
            if (value == 0) {
                queue.add(key);
            }
        }

        while (!queue.isEmpty()) {
            int size = queue.size();
            List<String> levelVariable = new ArrayList<String>();
            for (int i = 0; i < size; i++) {
                String key = queue.remove();
                if (suspiciousVariableSet.contains(key)) {
                    levelVariable.add(key);
                }
                if (!dependencyMap.containsKey(key)) {
                    continue;
                }
                Set<String> value = dependencyMap.get(key);
                for (String in : value) {
                    inDegree.put(in, inDegree.get(in) - 1);
                    if (inDegree.get(in) == 0) {
                        queue.add(in);
                    }
                }
            }
            if(levelVariable.size() != 0) {
                sortVariable.add(levelVariable);
            }
        }
    }
}
