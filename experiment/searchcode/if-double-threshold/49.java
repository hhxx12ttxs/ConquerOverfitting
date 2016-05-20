package loesung;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ea.EvoAlg;

import okoelopoly.Individuum;
import okoelopoly.Punktverteilung;

/**
 * @author Sebastian Nuck
 * 
 */

public class SnuckIndividuum implements Individuum, Serializable, Cloneable,
        Comparable<SnuckIndividuum> {

    private static final long serialVersionUID = 1766633117460198061L;
    private static final int HIDDEN_LAYER_NEURONS = 10;
    private double weights[][];
    private double thresholds[][];
    private double lq; // 0 Lebensqualitaet
    private double au; // 1 Aufklaerung
    private double pr; // 2 Produktion
    private double sa; // 3 Sanierung
    private double vr; // 4 Vermehrungsrate
    private double ub; // 5 Umweltbelastung
    private double bv; // 6 Bevlkerung
    private double po; // 7 Politik
    private double ap; // 8 Aktionspunkte

    private double lqPart; // 0 Lebensqualitaet
    private double auPart; // 1 Aufklaerung
    private double prPart; // 2 Produktion
    private double saPart; // 3 Sanierung
    private double vrPart; // 4 Vermehrungsrate

    private double result;

    private double bilance;

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public SnuckIndividuum() {
    }

    @Override
    public SnuckIndividuum clone() {
        try {
            return (SnuckIndividuum) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public double[][] getWeights() {
        return weights;
    }

    public void setWeights(double[][] weights) {
        this.weights = weights;
    }

    public double[][] getThresholds() {
        return thresholds;
    }

    public void setThresholds(double[][] thresholds) {
        this.thresholds = thresholds;
    }

    public void wendeDieStrategieAn(Punktverteilung simulatorstatus) {
        init(simulatorstatus);

        ArrayList<Neuron> inputLayerNeurons = initInputNeurons();

        ArrayList<Neuron> hiddenLayer1Neurons = initHiddenLayer(inputLayerNeurons, 1);
        ArrayList<Neuron> hiddenLayer2Neurons = initHiddenLayer(hiddenLayer1Neurons, 2);
        ArrayList<Neuron> hiddenLayer3Neurons = initHiddenLayer(hiddenLayer2Neurons, 3);
        ArrayList<Neuron> hiddenLayer4Neurons = initHiddenLayer(hiddenLayer3Neurons, 4);
        ArrayList<Neuron> hiddenLayer5Neurons = initHiddenLayer(hiddenLayer4Neurons, 5);
        ArrayList<Neuron> hiddenLayer6Neurons = initHiddenLayer(hiddenLayer5Neurons, 6);
        ArrayList<Neuron> hiddenLayer7Neurons = initHiddenLayer(hiddenLayer6Neurons, 7);
        ArrayList<Neuron> hiddenLayer8Neurons = initHiddenLayer(hiddenLayer7Neurons, 8);

        ArrayList<Neuron> outputLayer = initOutputLayer(hiddenLayer8Neurons);
        double sum = 0.0;

        // sum everyting except investment
        for (int i = 0; i < 5; i++) {
            sum += outputLayer.get(i).getValue();
        }

        lqPart = (outputLayer.get(0).getValue() / sum) * ap;
        auPart = (outputLayer.get(1).getValue() / sum) * ap;
        double production = outputLayer.get(2).getValue();
        prPart = (production / sum) * ap;
        saPart = (outputLayer.get(3).getValue() / sum) * ap;
        vrPart = (outputLayer.get(4).getValue() / sum) * ap;

        simulatorstatus.investiereInLebensqualitaet((int) Math.round(lqPart));
        simulatorstatus.investiereInAufklaerung((int) Math.round(auPart));
        if (production > 0) {
            simulatorstatus.investiereInProduktion((int) Math.round(prPart));
        } else {
            simulatorstatus.investiereGegenProduktion((int) Math.round(prPart));
        }
        simulatorstatus.investiereInSanierung((int) Math.round(saPart));
        simulatorstatus.investiereInVermehrungsrate((int) Math.round(vrPart));

        simulatorstatus.nutzeAufklaerungFuerBevoelkerungsWachstum(outputLayer.get(5).getValue());

    }

    /**
     * @param simulatorstatus
     */
    private void init(Punktverteilung simulatorstatus) {
        // this.simulatorstatus = simulatorstatus;
        this.ap = simulatorstatus.getAktionspunkte();
        this.au = simulatorstatus.getAufklaerung();
        this.lq = simulatorstatus.getLebensqualitaet();
        this.pr = simulatorstatus.getProduktion();
        this.sa = simulatorstatus.getSanierung();
        this.vr = simulatorstatus.getVermehrungsrate();
        this.ub = simulatorstatus.getUmweltbelastung();
        this.bv = simulatorstatus.getBevoelkerung();
        this.po = simulatorstatus.getPolitik();
    }

    /**
     * @return
     */
    private ArrayList<Neuron> initInputNeurons() {
        Neuron lqInput = new Neuron(lq, weights[0][0]);
        Neuron auInput = new Neuron(au, weights[0][1]);
        Neuron prInput = new Neuron(pr, weights[0][2]);
        Neuron saInput = new Neuron(sa, weights[0][3]);
        Neuron vrInput = new Neuron(vr, weights[0][4]);
        Neuron ubInput = new Neuron(ub, weights[0][5]);
        Neuron bvInput = new Neuron(bv, weights[0][6]);
        Neuron poInput = new Neuron(po, weights[0][7]);
        Neuron apInput = new Neuron(ap, weights[0][8]);

        ArrayList<Neuron> inputNeurons = new ArrayList<Neuron>();
        inputNeurons.add(lqInput);
        inputNeurons.add(auInput);
        inputNeurons.add(prInput);
        inputNeurons.add(saInput);
        inputNeurons.add(vrInput);
        inputNeurons.add(ubInput);
        inputNeurons.add(bvInput);
        inputNeurons.add(poInput);
        inputNeurons.add(apInput);
        return inputNeurons;
    }

    private ArrayList<Neuron> initOutputLayer(ArrayList<Neuron> inputNeurons) {
        ArrayList<Neuron> outputLayerNeurons = new ArrayList<Neuron>();
        Neuron n0 = new Neuron(weights[EvoAlg.TOTAL_LAYERS][0], thresholds[EvoAlg.TOTAL_LAYERS][0],
                inputNeurons);
        Neuron n1 = new Neuron(weights[EvoAlg.TOTAL_LAYERS][1], thresholds[EvoAlg.TOTAL_LAYERS][1],
                inputNeurons);
        Neuron n2 = new Neuron(weights[EvoAlg.TOTAL_LAYERS][2], thresholds[EvoAlg.TOTAL_LAYERS][2],
                inputNeurons, true);
        Neuron n3 = new Neuron(weights[EvoAlg.TOTAL_LAYERS][3], thresholds[EvoAlg.TOTAL_LAYERS][3],
                inputNeurons);
        Neuron n4 = new Neuron(weights[EvoAlg.TOTAL_LAYERS][4], thresholds[EvoAlg.TOTAL_LAYERS][4],
                inputNeurons);
        Neuron n5 = new Neuron(weights[EvoAlg.TOTAL_LAYERS][5], thresholds[EvoAlg.TOTAL_LAYERS][5],
                inputNeurons, true);
        // true);

        outputLayerNeurons.add(n0);
        outputLayerNeurons.add(n1);
        outputLayerNeurons.add(n2);
        outputLayerNeurons.add(n3);
        outputLayerNeurons.add(n4);
        outputLayerNeurons.add(n5);
        return outputLayerNeurons;
    }

    /**
     * Inits the hidden layer with weights, thresholds and inputNeurons;
     * 
     * @param inputNeurons
     */
    private ArrayList<Neuron> initHiddenLayer(ArrayList<Neuron> inputNeurons, int level) {
        ArrayList<Neuron> hiddenLayerNeurons = new ArrayList<Neuron>();

        for (int i = 0; i < HIDDEN_LAYER_NEURONS; i++) {
            hiddenLayerNeurons.add(new Neuron(weights[level][i], thresholds[level][i],
                    inputNeurons));
        }
        return hiddenLayerNeurons;
    }

    public class Neuron {
        private List<Neuron> inputs;
        private double weight;
        private double threshold;
        private double value;
        private boolean isExtended;

        public Neuron(double weight, double value) {
            this.weight = weight;
            this.value = value;
            this.inputs = new ArrayList<Neuron>();
        }

        public Neuron(double weight, double threshold, List<Neuron> inputs) {
            this.weight = weight;
            this.threshold = threshold;
            this.inputs = inputs;
            calc();
        }

        public Neuron(double weight, double threshold, List<Neuron> inputs, boolean isExtended) {
            this.weight = weight;
            this.threshold = threshold;
            this.inputs = inputs;
            this.isExtended = isExtended;
            calc();
        }

        private void calc() {
            double sum = 0.0;
            for (Neuron n : inputs) {
                sum += n.getWeight() * n.getValue();
            }
            sum = sum - threshold;
            if (!isExtended) {
                this.value = sigmoid(sum);
            } else {
                this.value = sigmoidExtended(sum);
            }
        }

        public double sigmoid(double x) {
            return 1 / (1 + Math.exp(-x));
        }

        public double sigmoidExtended(double x) {
            return Math.tanh(x);
        }

        public double getThreshold() {
            return threshold;
        }

        public double getWeight() {
            return weight;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        public void setWeight(double newWeight) {
            weight = newWeight;
        }

        public void connect(Neuron... ns) {
            for (Neuron n : ns)
                inputs.add(n);
        }

        public void connect(ArrayList<Neuron> inputs) {
            this.inputs = inputs;
        }

    }

    @Override
    public int compareTo(SnuckIndividuum ind) {
        return ind.getResult() > result ? 1 : (ind.getResult() == result ? 0 : -1);
    }

    public double getBilance() {
        return bilance;
    }

    public void setBilance(double bilance) {
        this.bilance = bilance;
    }
}

