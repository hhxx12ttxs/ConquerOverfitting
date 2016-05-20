package pl.magosa.microbe;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Class represents sigle neuron
 *
 * (c) 2014 Krzysztof Magosa
 */
public class Neuron {
    protected double threshold;
    protected double output;
    protected double sum;
    protected boolean hasBias;
    protected ArrayList<Input> inputs;
    protected TransferFunction transferFunction;

    public Neuron() {
        threshold = -0.5 + Math.random();
        inputs = new ArrayList<>();
    }

    /**
     * Sets transfer function for this neuron
     */
    public void setTransferFunction(TransferFunction transferFunction) {
        this.transferFunction = transferFunction;
    }

    public TransferFunction getTransferFunction() {
        return transferFunction;
    }

    /**
     * Gets input omitting bias (if any)
     * @param index Index of input, bias is not counted, so 0 is always first input
     * @return Input object
     */
    public Input getInput(final int index) {
        return inputs.get(hasBias() ? index+1 : index);
    }

    /**
     * Creates inputs
     * @param count How many inputs should be created
     */
    public void createInputs(final int count) {
        for (int i = 1; i <= count; i++) {
            createInput((Input input) -> {});
        }
    }

    /**
     * Creates inputs and initialise them using specified lambda
     * @param count How many inputs should be created
     * @param initFunction Lambda which initialise each input
     */
    public void createInputs(final int count, Consumer<Input> initFunction) {
        for (int i = 1; i <= count; i++) {
            createInput(initFunction);
        }
    }

    /**
     * Creates one input initialise it using specified lambda
     * @param initFunction Lambda which initialise input
     */
    public void createInput(Consumer<Input> initFunction) {
        Input input = new Input();
        initFunction.accept(input);
        inputs.add(input);
    }

    public ArrayList<Input> getInputs() {
        return inputs;
    }

    public void setThreshold(final double threshold) {
        this.threshold = threshold;
    }

    public void applyThresholdCorrection(final double correction) {
        this.threshold += correction;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getOutput() {
        return output;
    }

    public double getSum() {
        return sum;
    }

    /**
     * Calculates output of neuron.
     */
    public void activate() {
        sum = -threshold;

        for (Input input : inputs) {
            sum += (input.getWeight() * input.getValue());
        }

        output = transferFunction.function(sum);
    }

    public void createBias() {
        if (!inputs.isEmpty()) {
            throw new RuntimeException("Bias must be created before inputs.");
        }

        if (hasBias) {
            throw new RuntimeException("Neuron can have just one bias.");
        }

        createInput((Input input) -> {
            input.setValue(1);
        });

        hasBias = true;
    }

    public boolean hasBias() {
        return hasBias;
    }
}

