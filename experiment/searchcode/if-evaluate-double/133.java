package com.teamdev.perin.calculator.operators.unary;

import com.teamdev.perin.calculator.EvaluationException;
import com.teamdev.perin.calculator.operators.AbstractOperator;
import com.teamdev.perin.calculator.operators.Priority;

public abstract class AbstractUnaryOperator extends AbstractOperator{

    public AbstractUnaryOperator(Priority priority) {
        super(priority);
    }

    @Override
    public double evaluateOperator(double... operands) throws EvaluationException {
        if (operands.length != 1){
            throw new EvaluationException("Unary operator requires one argument for evaluation.");
        }
        double operand = operands[0];
        return evaluate(operand);
    }

    protected abstract double evaluate(double operand) throws EvaluationException;

    @Override
    public int getNumberOfArguments() {
        return 1;
    }

}

