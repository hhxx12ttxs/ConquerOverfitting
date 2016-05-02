package com.teamdev.perin.calculator.functions;

import com.teamdev.perin.calculator.EvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.teamdev.perin.calculator.util.ClassNameGetter.getCurrentClassName;

public class Sum implements Function {

    private static Logger logger = LoggerFactory.getLogger(getCurrentClassName());


    @Override
    public double evaluate(Double... args) throws EvaluationException {

        if (args.length < 2) {
            throw new EvaluationException("Sum requires 2 and more arguments, but " + args.length + " was given.");
        }

        double sum = 0;
        for (double argument : args) {
            sum += argument;
        }
        logger.debug("Sum function was evaluated: sum({})={}.", Arrays.toString(args), sum);

        return sum;
    }
}

