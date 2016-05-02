package com.zed.quantcad.domain;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.Pow;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * author: ezachateyskiy
 */

public abstract class BaranovQuantizationFunction implements UnivariateFunction {

    private final float firstX;
    private final float lastX;


    protected BaranovQuantizationFunction(float firstX, float lastX) {
        this.firstX = firstX;
        this.lastX = lastX;
    }

    @Override
    public double value(double x) {
        SimpsonIntegrator integrator = new SimpsonIntegrator();
        final double normalizationIndex = integrator.integrate(Integer.MAX_VALUE, getInputPDF(), firstX, lastX);

        UnivariateFunction sqrt3InputPdf = new UnivariateFunction() {
            @Override
            public double value(double v) {
                return new Pow().value(getInputPDF().value(v) / normalizationIndex, (1 / 3));
            }
        };

        UnivariateFunction sqrt2InputPdf = new UnivariateFunction() {
            @Override
            public double value(double v) {
                return new Pow().value(getInputPDF().value(v) / normalizationIndex, (0.5));
            }
        };

        if (x == firstX)
            integrator.integrate(Integer.MAX_VALUE, getInputPDF(), firstX, lastX);

        return (lastX - firstX) / integrator.integrate(Integer.MAX_VALUE, sqrt3InputPdf, firstX, lastX)
                * (x==firstX ? 0 : integrator.integrate(Integer.MAX_VALUE, sqrt2InputPdf, firstX, x)) + firstX;
    }

    protected abstract UnivariateFunction getInputPDF();
}


