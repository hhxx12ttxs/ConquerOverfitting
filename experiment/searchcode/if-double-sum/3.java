package com.teamdev.operator;

import java.util.LinkedList;

public class Sum extends Operator {

    public Sum(int priority, boolean leftAssociative) {
        super(priority, leftAssociative);
    }

    @Override
    public double execute(LinkedList<Double> args) {
        if (args.size() == 1) {
            return args.get(0);
        }
        double sum = 0;
        while (!args.isEmpty()) {
            sum += args.removeLast();
        }
        return sum;
    }
}

