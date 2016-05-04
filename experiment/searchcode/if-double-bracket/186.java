package com.teamdev.perin.calculator;

import com.teamdev.perin.calculator.api.ParseException;
import com.teamdev.perin.calculator.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.teamdev.perin.calculator.util.ClassNameGetter.getCurrentClassName;

public class FunctionStack extends EvaluationStack {

    private static Logger logger = LoggerFactory.getLogger(getCurrentClassName());

    private Function function = null;
    private boolean isFunctionsBracket = true;


    public FunctionStack(EvaluationContext context, EvaluationStack parent, Function function) {
        super(context, parent);
        this.function = function;
    }

    @Override
    public void pushLeftBracket() {
        if (isFunctionsBracket) {
            logger.debug("Function's left bracket is pushing.");
            isFunctionsBracket = false;
        } else {
            super.pushLeftBracket();
        }
    }

    @Override
    public void pushRightBracket() throws ParseException, EvaluationException {
        logger.debug("Right bracket of function is pushing.");
        evaluateCurrentStack();
        evaluateFunction();

        if (getParent() == null) {
            throw new ParseException("Left bracket is missed.", getContext().getExpressionReader().getCursor());
        }

        getParent().pushCurrentStack(getOperandStack());
        getContext().setEvaluationStack(getParent());
    }

    @Override
    public void pushDelimiter() throws EvaluationException {
        evaluateCurrentStack();
    }

    private void evaluateFunction() throws EvaluationException {
        Double[] arguments = getOperandStack().toArray(new Double[getOperandStack().size()]);
        Double result = function.evaluate(arguments);
        getOperandStack().clear();
        getOperandStack().push(result);
    }
}

