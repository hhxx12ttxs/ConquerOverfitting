package com.dthielke.nnet.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataLoader {
    
    private double[][] inputs;
    private double[][] targets;
    
    /**
     * Loads a set of training data where each line is an element in the format:
     *   Input 1, Input 2, ..., Input N: Target 1, Target 2, ..., Target M
     * where N is the total number of inputs and M is the total number of outputs.
     */
    public void load(String filename) throws FileNotFoundException {
        List<double[]> inputs = new ArrayList<double[]>();
        List<double[]> targets = new ArrayList<double[]>();
        
        Scanner fileScanner = new Scanner(new File(filename));
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            if (!line.startsWith("%") && !line.trim().isEmpty()) {
                int index = line.indexOf(":");
                
                String[] inputStrings = line.substring(0, index).split("[ ,]");
                double[] inputDoubles = new double[inputStrings.length];
                for (int i = 0; i < inputDoubles.length; ++i) {
                    inputDoubles[i] = Double.parseDouble(inputStrings[i]);
                }
                inputs.add(inputDoubles);
                
                String[] targetStrings = line.substring(index + 1).split("[ ,]");
                double[] targetDoubles = new double[targetStrings.length];
                for (int i = 0; i < targetDoubles.length; ++i) {
                    targetDoubles[i] = Double.parseDouble(targetStrings[i]);
                }
                targets.add(targetDoubles);
            }
        }
        fileScanner.close();
        
        this.inputs = inputs.toArray(new double[0][]);
        this.targets = targets.toArray(new double[0][]);
    }
    
    private double[][] normalizeColumns(double[][] x) {
        double[] means = new double[x[0].length];
        double[] stdevs = new double[x[0].length];
        
        // calculate means
        for (int i = 0; i < means.length; ++i) {
            double sum = 0;
            for (int j = 0; j < x.length; ++j) {
                sum += x[j][i];
            }
            means[i] = sum / x.length;
        }
        
        // calculate standard deviations
        for (int i = 0; i < stdevs.length; ++i) {
            double sumSq = 0;
            for (int j = 0; j < x.length; ++j) {
                sumSq += (x[j][i] - means[i]) * (x[j][i] - means[i]);
            }
            stdevs[i] = Math.sqrt(sumSq / x.length);
        }
        
        // normalize
        double[][] norm = x.clone();
        for (int i = 0; i < x[0].length; ++i) {
            for (int j = 0; j < x.length; ++j) {
                norm[j][i] = (x[j][i] - means[i]) / stdevs[i];
            }
        }
        return norm;
    }
    
    public double[][] getNormalizedInputs() {
        return normalizeColumns(inputs);
    }
    
    public double[][] getNormalizedTargets() {
        return normalizeColumns(targets);
    }
    
    public double[][] getInputs() {
        return inputs;
    }
    
    public double[][] getTargets() {
        return targets;
    }
    
}

