package com.teamdev.perin.calculator.functions;

import com.teamdev.perin.calculator.EvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.teamdev.perin.calculator.util.ClassNameGetter.getCurrentClassName;

public class Min implements Function {

    private static Logger logger = LoggerFactory.getLogger(getCurrentClassName());

    @Override
    public double evaluate(Double... args) throws EvaluationException {

        if (args.length < 2) {
            throw new EvaluationException("Min requires 2 and more arguments, but " + args.length + " was given.");
        }

        double min = args[0];
        for (double argument : args) {
            min = Math.min(min, argument);
        }
        logger.debug("Min function was evaluated: min({})={}.", Arrays.toString(args), min);

        return min;
    }
}

