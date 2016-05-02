package com.teamdev.perin.calculator.operators.binary;

import com.teamdev.perin.calculator.EvaluationException;
import com.teamdev.perin.calculator.operators.AbstractOperator;
import com.teamdev.perin.calculator.operators.Priority;

public abstract class AbstractBinaryOperator extends AbstractOperator {

    public AbstractBinaryOperator(Priority priority) {
        super(priority);
    }

    @Override
    public double evaluateOperator(double... operands) throws EvaluationException {
        if (operands.length != 2){
            throw new EvaluationException("Binary operator requires two arguments for evaluation.");
        }
        double leftOperand = operands[0];
        double rightOperand = operands[1];
        return evaluate(leftOperand, rightOperand);
    }

    protected abstract double evaluate(double leftOperand, double rightOperand) throws EvaluationException;

    @Override
    public int getNumberOfArguments() {
        return 2;
    }
}

