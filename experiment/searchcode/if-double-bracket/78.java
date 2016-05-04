package com.teamdev.perin.calculator;

import com.teamdev.perin.calculator.api.ParseException;
import com.teamdev.perin.calculator.functions.Function;
import com.teamdev.perin.calculator.operators.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Formatter;

import static com.teamdev.perin.calculator.util.ClassNameGetter.getCurrentClassName;


public class EvaluationStack {

    private static Logger logger = LoggerFactory.getLogger(getCurrentClassName());

    private final Deque<Double> operandStack = new ArrayDeque<Double>();
    private final Deque<Operator> operatorStack = new ArrayDeque<Operator>();

    private final EvaluationContext context;
    private EvaluationStack parent = null;

    public EvaluationStack(EvaluationContext context) {
        this.context = context;
    }

    public EvaluationStack(EvaluationContext context, EvaluationStack parent) {
        this.context = context;
        this.parent = parent;
    }

    public void pushLeftBracket() {
        logger.debug("Left bracket is pushing.");
        context.setEvaluationStack(new EvaluationStack(context, this));
    }

    public void pushRightBracket() throws ParseException, EvaluationException {
        logger.debug("Right bracket is pushing.");
        if (parent == null) {
            throw new ParseException("Left bracket is missed.", context.getExpressionReader().getCursor());
        }

        evaluateCurrentStack();

        parent.pushCurrentStack(operandStack);
        context.setEvaluationStack(parent);
    }

    public void pushNumber(double number) throws EvaluationException {
        logger.debug("Number is pushing.");
        operandStack.push(number);
    }

    public void pushBinaryOperator(Operator currentOperator) throws EvaluationException {
        logger.debug("Binary operator is pushing.");
        evaluateAvailableForEvaluationOperators(currentOperator);
        operatorStack.push(currentOperator);
    }

    public void pushPrefixUnaryOperator(Operator currentOperator){
        logger.debug("Prefix Unary operator is pushing.");
        operatorStack.push(currentOperator);
    }

    public void pushPostfixUnaryOperator(Operator currentOperator) throws EvaluationException {
        logger.debug("Postfix unary operator is pushing.");
        evaluateAvailableForEvaluationOperators(currentOperator);
        double[] arguments = buildArgumentsOfOperator(currentOperator);
        double result = currentOperator.evaluateOperator(arguments);
        operandStack.push(result);
    }

    public void pushFunction(Function currentFunction) {
        logger.debug("Function is pushing.");
        context.setEvaluationStack(new FunctionStack(context, this, currentFunction));
    }

    public void pushDelimiter() throws ParseException, EvaluationException {
        logger.debug("Delimiter is pushing to the common stack.");
        throw new ParseException("Delimiters aren't permitted externally the functions.",
                                 context.getExpressionReader().getCursor());
    }

    public void pushEndOfExpression() throws ParseException, EvaluationException {
        logger.debug("End of expression is pushing.");
        if (parent != null) {
            throw new ParseException("Right bracket is missed.", context.getExpressionReader().getCursor());
        }
        evaluateCurrentStack();
    }

    public void evaluateCurrentStack() throws EvaluationException {
        logger.debug("Current stack is evaluating.");
        while (!operatorStack.isEmpty()) {
            evaluateOperator();
        }
    }

    public Deque<Double> getOperandStack() {
        return operandStack;
    }

    public Double getResult() throws ParseException {
        logger.debug("Result is getting.");
        if (operandStack.peek().isInfinite() || operandStack.peek().isNaN()) {
            Formatter formatter = new Formatter();
            String limits = formatter.format("(%1.1e, %1.1e)", -Double.MAX_VALUE, Double.MAX_VALUE).toString();
            throw new ParseException("Result went outside the limits. Limits are: " + limits + ".");
        }
        return operandStack.pop();
    }

    public EvaluationStack getParent() {
        return parent;
    }

    public EvaluationContext getContext() {
        return context;
    }

    protected void pushCurrentStack(Deque<Double> operandStack) throws EvaluationException, ParseException {
        if (operandStack.isEmpty()) {
            throw new ParseException("There are any arguments inside brackets.",
                                     context.getExpressionReader().getCursor());
        }
        pushNumber(operandStack.pop());
    }

    private void evaluateOperator() throws EvaluationException {
        Operator operator = operatorStack.pop();
        double[] arguments = buildArgumentsOfOperator(operator);
        double result = operator.evaluateOperator(arguments);
        operandStack.push(result);
    }

    private double[] buildArgumentsOfOperator(Operator operator) {
        int numberOfArguments = operator.getNumberOfArguments();
        double[] arguments = new double[numberOfArguments];
        for (int index = numberOfArguments - 1; index >= 0; index--){
            arguments[index] = operandStack.pop();
        }
        return arguments;
    }

    private void evaluateAvailableForEvaluationOperators(Operator currentOperator) throws EvaluationException {

        while ((operatorStack.size() > 0) && (operatorStack.peek().compareTo(currentOperator) >= 0)) {
            logger.debug("Evaluating operator with higher or with the same priority "
                         + "as compared with current operator's priority.");
            evaluateOperator();
        }
    }

}

