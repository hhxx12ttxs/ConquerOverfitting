package ru.ifmo.ctddev.petrichenko.calc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by IntelliJ IDEA.
 * User: glukos
 * E-mail: ivan.glukos@gmail.com
 * Date: 11.02.12
 * Time: 14:15
 */
public class Function {
    String expression;
    TreeNode root;
    double x;
    double y;

    protected enum TreeNodeType {
        EXPR, SUMMAND, FACTOR, VAL, VAR_X, VAR_Y
    }

    protected class TreeNode {
        TreeNodeType type;
        int sign;
        List<TreeNode> subNodes;
        double value;

        public TreeNode(TreeNodeType type, int sign) {
            this.type = type;
            this.sign = sign;
            subNodes = new ArrayList<TreeNode>();
            value = 0;
        }

        public TreeNode(TreeNodeType type, int sign, double value) {
            this.type = type;
            this.sign = sign;
            this.value = value;
            subNodes = new ArrayList<TreeNode>();

        }

        void addNode(TreeNode node) {
            subNodes.add(node);
        }

        public double getValue(double xArg, double yArg) throws FunctionException {
            x = xArg;
            y = yArg;
            return getValue();
        }

        private double getValue() throws FunctionException {
            switch (type) {
                case EXPR: {
                    double result = 0;
                    for (TreeNode t : subNodes) {
                        double subResult = t.getValue();
                        if (!checkSum(result, subResult)) {
                            throw new FunctionOverflowException("overflow");
                        }
                        result += subResult;
                    }
                    return sign * result;
                }
                case SUMMAND: {
                    double result = 1;
                    for (TreeNode t : subNodes) {
                        double subResult = t.getValue();
                        switch (t.sign) {
                            case '/': {
                                if (subResult == 0) {
                                    throw new FunctionDivisionByZeroException("division by zero");
                                }
                                result /= subResult;
                                break;
                            }
                            default: {
                                if (!checkMul(result, subResult)) {
                                    throw new FunctionOverflowException("overflow");
                                }
                                result *= subResult;
                            }
                        }

                    }
                    return sign * result;
                }
                case FACTOR: {
                    return subNodes.get(0).getValue();
                }
                case VAR_X: {
                    return x;
                }
                case VAR_Y: {
                    return y;
                }
                default: {
                    return value;
                }
            }
        }
    }

    public double getValue(double x, double y) throws FunctionException {
        return root.getValue(x, y);
    }

    public Function(String expr) throws FunctionException {
        expression = expr;
        root = new TreeNode(TreeNodeType.EXPR, 1);
        parseExpr(0, expr.length() - 1, root);
    }

    private void parseExpr(int begin, int end, TreeNode root) throws FunctionException {
        int boundsLevel = 0;
        int currentPos = begin;
        int currentSign = 1;
        currentPos = getStartPos(begin, end, currentPos);
        if (expression.charAt(currentPos) == '-') {
            currentPos++;
            currentSign *= -1;
        }
        if (currentPos == end + 1) {
            throw new FunctionIllegalExpressionException("illegal expression: unexpected end at index " + (end + 1));
        }
        ParseSummandInvoker parseSummandInvoker =
                new ParseSummandInvoker(end, root, boundsLevel, currentPos, currentSign).invoke();
        currentSign = parseSummandInvoker.getCurrentSign();
        currentPos = parseSummandInvoker.getCurrentPos();
        boundsLevel = parseSummandInvoker.getBoundsLevel();
        TreeNode t = new TreeNode(TreeNodeType.SUMMAND, currentSign);
        root.addNode(t);
        parseSummand(currentPos, end, t);
        if (boundsLevel > 0) {
            throw new FunctionIllegalExpressionException("illegal expression: \")\" expected at index" + (end + 1));
        }
    }

    private int getStartPos(int begin, int end, int currentPos) throws FunctionIllegalExpressionException {
        if (begin > end) {
            throw new FunctionIllegalExpressionException("illegal expression: unexpected end at index " + (end + 1));
        }
        currentPos = getStartPos(end, currentPos);
        if (expression.charAt(currentPos) == '+') {
            currentPos++;
        }
        return currentPos;
    }

    private void parseSummand(int begin, int end, TreeNode root) throws FunctionException {
        int boundsLevel = 0;
        int currentPos = begin;
        char currentAction = '*';
        if (begin > end) {
            throw new FunctionIllegalExpressionException("illegal expression: unexpected end at index " + (end + 1));
        }
        currentPos = getStartPos(end, currentPos);
        ParseFactorInvoker parseFactorInvoker = new ParseFactorInvoker(end, root, boundsLevel, currentPos, currentAction).invoke();
        currentAction = parseFactorInvoker.getCurrentAction();
        currentPos = parseFactorInvoker.getCurrentPos();
        boundsLevel = parseFactorInvoker.getBoundsLevel();
        TreeNode t = new TreeNode(TreeNodeType.FACTOR, currentAction);
        root.addNode(t);
        parseFactor(currentPos, end, t);
        if (boundsLevel > 0) {
            throw new FunctionIllegalExpressionException("illegal expression: \")\" expected at index " + (end + 1));
        }

    }

    private void parseFactor(int begin, int end, TreeNode root) throws FunctionException {
        int currentPos = begin;
        if (begin > end) {
            throw new FunctionIllegalExpressionException("illegal expression: unexpected token at index " + (end + 1));
        }
        currentPos = getStartPos(end, currentPos);
        if (expression.charAt(currentPos) == '(') {
            int i = end;
            while (expression.charAt(i) != ')') {
                if (expression.charAt(i) != ' ') {
                    throw new FunctionIllegalExpressionException("illegal expression: unexpected token at index " + i);
                }
                i--;
                if (i < currentPos) {
                    throw new FunctionIllegalExpressionException("illegal expression: unexpected \"(\" at index " + (currentPos));
                }
            }
            TreeNode t = new TreeNode(TreeNodeType.EXPR, 1);
            root.addNode(t);
            parseExpr(currentPos + 1, i - 1, t);
            return;
        }
        if (unaryParseFactorInvoker(end, root, currentPos)) return;
        String s = expression.substring(currentPos, end + 1).trim();
        try {
            substituteValues(end, root, s);
        } catch (NumberFormatException e) {
            throw new FunctionIllegalExpressionException("illegal expression: unexpected token at index " + end);
        }
    }

    private boolean unaryParseFactorInvoker(int end, TreeNode root, int currentPos) throws FunctionException {
        if (expression.charAt(currentPos) == '+') {
            TreeNode t = new TreeNode(TreeNodeType.FACTOR, '*');
            root.addNode(t);
            parseFactor(currentPos + 1, end, t);
            return true;
        }
        if (expression.charAt(currentPos) == '-') {
            TreeNode t = new TreeNode(TreeNodeType.SUMMAND, -1);
            root.addNode(t);
            parseSummand(currentPos + 1, end, t);
            return true;
        }
        return false;
    }

    private void substituteValues(int end, TreeNode root, String s) throws FunctionIllegalExpressionException {
        if (s.equals("x")) {
            TreeNode t = new TreeNode(TreeNodeType.VAR_X, 1);
            root.addNode(t);
        } else if (s.equals("y")) {
            TreeNode t = new TreeNode(TreeNodeType.VAR_Y, 1);
            root.addNode(t);
        } else {
            double value = Double.parseDouble(s);
            if (value >= 1e10 || value <= 1e-10) {
                throw new FunctionIllegalExpressionException("illegal expression:" +
                        " constant is out of range at index " + end);
            }
            TreeNode t = new TreeNode(TreeNodeType.VAL, 1, value);
            root.addNode(t);
        }
    }

    private boolean checkSum(double A, double B) {
        double C = A + B;
        return (C > -1e10 && C < 1e10);
    }

    private boolean checkMul(double A, double B) {
        double C = A * B;
        return (C > -1e10 && C < 1e10);
    }

    private int getStartPos(int end, int currentPos) throws FunctionIllegalExpressionException {
        while (expression.charAt(currentPos) == ' ') {
            currentPos++;
            if (currentPos == end + 1) {
                throw new FunctionIllegalExpressionException("illegal expression: unexpected end at index " + (end + 1));
            }
        }
        return currentPos;
    }

    public void write(PrintWriter pw) {
        if (pw == null) {
            return;
        }
        List<Function> thisList = new ArrayList<Function>();
        thisList.add(this);
        FunctionPrinter.printFunctions(pw, thisList);
    }

    private class ParseSummandInvoker {
        private int end;
        private TreeNode root;
        private int boundsLevel;
        private int currentPos;
        private int currentSign;

        public ParseSummandInvoker(int end, TreeNode root, int boundsLevel, int currentPos, int currentSign) {
            this.end = end;
            this.root = root;
            this.boundsLevel = boundsLevel;
            this.currentPos = currentPos;
            this.currentSign = currentSign;
        }

        public int getBoundsLevel() {
            return boundsLevel;
        }

        public int getCurrentPos() {
            return currentPos;
        }

        public int getCurrentSign() {
            return currentSign;
        }

        public ParseSummandInvoker invoke() throws FunctionException {
            for (int i = currentPos; i <= end; i++) {
                if (expression.charAt(i) == '(') {
                    boundsLevel++;
                }
                if (expression.charAt(i) == ')') {
                    boundsLevel--;
                }
                if (boundsLevel < 0) {
                    throw new FunctionIllegalExpressionException("illegal expression: unexpected \")\" at index " + i);
                }
                if (boundsLevel == 0 && expression.charAt(i) == '+' && expression.charAt(i - 1) != 'e') {
                    if (currentPos > i - 1) {
                        throw new FunctionIllegalExpressionException("illegal expression: unexpected token at index " + i);
                    }
                    TreeNode t = new TreeNode(TreeNodeType.SUMMAND, currentSign);
                    root.addNode(t);
                    parseSummand(currentPos, i - 1, t);
                    currentSign = 1;
                    currentPos = i + 1;
                }
                if (boundsLevel == 0 && expression.charAt(i) == '-' && expression.charAt(i - 1) != 'e') {
                    if (currentPos > i - 1) {
                        throw new FunctionIllegalExpressionException("illegal expression: unexpected token at index " + i);
                    }
                    TreeNode t = new TreeNode(TreeNodeType.SUMMAND, currentSign);
                    root.addNode(t);
                    parseSummand(currentPos, i - 1, t);
                    currentSign = -1;
                    currentPos = i + 1;
                }

            }
            if (currentPos > end) {
                throw new FunctionIllegalExpressionException("illegal expression: unexpected end at index " + (end + 1));
            }
            return this;
        }
    }

    private class ParseFactorInvoker {
        private int end;
        private TreeNode root;
        private int boundsLevel;
        private int currentPos;
        private char currentAction;

        public ParseFactorInvoker(int end, TreeNode root, int boundsLevel, int currentPos, char currentAction) {
            this.end = end;
            this.root = root;
            this.boundsLevel = boundsLevel;
            this.currentPos = currentPos;
            this.currentAction = currentAction;
        }

        public int getBoundsLevel() {
            return boundsLevel;
        }

        public int getCurrentPos() {
            return currentPos;
        }

        public char getCurrentAction() {
            return currentAction;
        }

        public ParseFactorInvoker invoke() throws FunctionException {
            for (int i = currentPos; i <= end; i++) {
                if (expression.charAt(i) == '(') {
                    boundsLevel++;
                }
                if (expression.charAt(i) == ')') {
                    boundsLevel--;
                }
                if (boundsLevel < 0) {
                    throw new FunctionIllegalExpressionException("illegal expression: unexpected \")\" at index " + i);
                }
                if (boundsLevel == 0 && (expression.charAt(i) == '*' || expression.charAt(i) == '/')) {
                    if (currentPos > i - 1) {
                        throw new FunctionIllegalExpressionException("illegal expression: unexpected token at index " + i);
                    }
                    TreeNode t = new TreeNode(TreeNodeType.FACTOR, currentAction);
                    root.addNode(t);
                    parseFactor(currentPos, i - 1, t);
                    currentAction = expression.charAt(i);
                    currentPos = i + 1;
                }
            }
            if (currentPos > end) {
                throw new FunctionIllegalExpressionException("illegal expression: unexpected end at index " + (end + 1));
            }
            return this;
        }
    }
}

