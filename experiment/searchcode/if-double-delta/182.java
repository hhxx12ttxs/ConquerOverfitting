/*
 * Copyright [2013-2014] eBay Software Foundation
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ml.shifu.guagua.mapreduce.example.nn;

/**
 * {@link Weight} is used to update NN weights according to propagation option. Which is also copied from Encog.
 * 
 * <p>
 * We'd like to reuse code from Encog but unfortunately the methods are private:(.
 */
public class Weight {
    /**
     * The zero tolerance to use.
     */
    private static final double ZERO_TOLERANCE = 0.00000000000000001;

    private double learningRate;

    private String algorithm;

    // for quick propagation
    private double decay = 0.0001d;
    private double[] lastDelta = null;
    private double[] lastGradient = null;
    private double outputEpsilon = 0.35;
    private double eps = 0.0;
    private double shrink = 0.0;

    // for back propagation
    private double momentum = 0.0;

    // for resilient propagation
    private double[] updateValues = null;
    private static final double DEFAULT_INITIAL_UPDATE = 0.1;
    private static final double DEFAULT_MAX_STEP = 50;

    public Weight(int numWeight, double numTrainSize, double rate, String algorithm) {

        this.lastDelta = new double[numWeight];
        this.lastGradient = new double[numWeight];
        this.eps = this.outputEpsilon / numTrainSize;
        this.shrink = rate / (1.0 + rate);
        this.learningRate = rate;
        this.algorithm = algorithm;
        this.updateValues = new double[numWeight];

        for(int i = 0; i < this.updateValues.length; i++) {
            this.updateValues[i] = DEFAULT_INITIAL_UPDATE;
            this.lastDelta[i] = 0;
        }
    }

    public double[] calculateWeights(double[] weights, double[] gradients) {
        for(int i = 0; i < gradients.length; i++) {
            weights[i] += updateWeight(i, weights, gradients);
        }

        return weights;
    }

    private double updateWeight(int index, double[] weights, double[] gradients) {

        if(this.algorithm.equalsIgnoreCase(NNConstants.BACK_PROPAGATION)) {
            return updateWeightBP(index, weights, gradients);
        } else if(this.algorithm.equalsIgnoreCase(NNConstants.QUICK_PROPAGATION)) {
            return updateWeightQBP(index, weights, gradients);
        } else if(this.algorithm.equalsIgnoreCase(NNConstants.MANHATTAN_PROPAGATION)) {
            return updateWeightMHP(index, weights, gradients);
        } else if(this.algorithm.equalsIgnoreCase(NNConstants.SCALEDCONJUGATEGRADIENT)) {
            return updateWeightSCG(index, weights, gradients);
        } else if(this.algorithm.equalsIgnoreCase(NNConstants.RESILIENTPROPAGATION)) {
            return updateWeightRLP(index, weights, gradients);
        }

        return 0.0;

    }

    private double updateWeightBP(int index, double[] weights, double[] gradients) {
        double delta = (gradients[index] * this.learningRate) + (this.lastDelta[index] * this.momentum);
        this.lastDelta[index] = delta;
        return delta;
    }

    private double updateWeightQBP(int index, double[] weights, double[] gradients) {

        final double w = weights[index];
        final double d = this.lastDelta[index];
        final double s = -gradients[index] + this.decay * w;
        final double p = -lastGradient[index];
        double nextStep = 0.0;

        // The step must always be in direction opposite to the slope.
        if(d < 0.0) {
            // If last step was negative...
            if(s > 0.0) {
                // Add in linear term if current slope is still positive.
                nextStep -= this.eps * s;
            }
            // If current slope is close to or larger than prev slope...
            if(s >= (this.shrink * p)) {
                // Take maximum size negative step.
                nextStep += this.learningRate * d;
            } else {
                // Else, use quadratic estimate.
                nextStep += d * s / (p - s);
            }
        } else if(d > 0.0) {
            // If last step was positive...
            if(s < 0.0) {
                // Add in linear term if current slope is still negative.
                nextStep -= this.eps * s;
            }
            // If current slope is close to or more neg than prev slope...
            if(s <= (this.shrink * p)) {
                // Take maximum size negative step.
                nextStep += this.learningRate * d;
            } else {
                // Else, use quadratic estimate.
                nextStep += d * s / (p - s);
            }
        } else {
            // Last step was zero, so use only linear term.
            nextStep -= this.eps * s;
        }

        // update global data arrays
        this.lastDelta[index] = nextStep;
        this.lastGradient[index] = gradients[index];

        return nextStep;
    }

    private double updateWeightMHP(int index, double[] weights, double[] gradients) {

        if(Math.abs(gradients[index]) < ZERO_TOLERANCE) {
            return 0;
        } else if(gradients[index] > 0) {
            return this.learningRate;
        } else {
            return -this.learningRate;
        }
    }

    private double updateWeightSCG(int index, double[] weights, double[] gradients) {
        // TODO
        return 0;
    }

    private double updateWeightRLP(int index, double[] weights, double[] gradients) {
        // multiply the current and previous gradient, and take the
        // sign. We want to see if the gradient has changed its sign.
        final int change = NNUtils.sign(gradients[index] * lastGradient[index]);
        double weightChange = 0;

        // if the gradient has retained its sign, then we increase the
        // delta so that it will converge faster
        if(change > 0) {
            double delta = this.updateValues[index] * NNConstants.POSITIVE_ETA;
            delta = Math.min(delta, DEFAULT_MAX_STEP);
            weightChange = NNUtils.sign(gradients[index]) * delta;
            this.updateValues[index] = delta;
            lastGradient[index] = gradients[index];
        } else if(change < 0) {
            // if change<0, then the sign has changed, and the last
            // delta was too big
            double delta = this.updateValues[index] * NNConstants.NEGATIVE_ETA;
            delta = Math.max(delta, NNConstants.DELTA_MIN);
            this.updateValues[index] = delta;
            weightChange = -this.lastDelta[index];
            // set the previous gradent to zero so that there will be no
            // adjustment the next iteration
            lastGradient[index] = 0;
        } else if(change == 0) {
            // if change==0 then there is no change to the delta
            final double delta = this.updateValues[index];
            weightChange = NNUtils.sign(gradients[index]) * delta;
            lastGradient[index] = gradients[index];
        }

        this.lastDelta[index] = weightChange;
        // apply the weight change, if any
        return weightChange;
    }

}

