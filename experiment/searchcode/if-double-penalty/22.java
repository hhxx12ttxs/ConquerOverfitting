/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trussoptimizater.Truss.Optimize;

import java.util.prefs.Preferences;

/**
 * GAModel represents a genetic algorithm (GA) model and stores all the neccessary values such as
 * penalty weighting, evolutions and population counts.
 * @author Chris
 */
public class GAModel extends OptimizeModel {

    /**
     * Prefereneces is used to store selected values
     */
    private Preferences prefs;
    /**
     * Default value for compression penalty if compression penalty preference cannot be located
     */
    public static final double DEAFULT_COMPRESSION_PENALTY = 1000;
    /**
     * Default value for stress penalty if stress penalty preference cannot be located
     */
    public static final double DEFAULT_STRESS_PENALTY = 1000;
    /**
     * Default value for weight penalty if weight penalty preference cannot be located
     */
    public static final double DEFAULT_WEIGHT_PENALTY = 500;
    /**
     * Default value for length penalty if length penalty preference cannot be located
     */
    public static final double DEFAULT_LENGTH_PENALTY = 1000;
    /**
     * Default value for deflection penalty if deflection penalty preference cannot be located
     */
    public static final double DEFAULT_DEFLECTION_PENALTY = 1;
    /**
     * Default value for max number of evolution if respective preference cannot be located
     */
    public static final int DEFAULT_MAX_ALLLOWED_EVOLUTIIONS = 60;
    /**
     * Default value for population size if respective preference cannot be located
     */
    public static final int DEFAULT_POPULATION_SIZE = 400;
    /**
     * Preferece key to locate compression penalty value
     */
    public static final String COMPRESSION_PENALTY_PREF_KEY = "COMPRESSION_PENALTY_PREF_KEY";
    /**
     * Preferece key to locate stress penalty value
     */
    public static final String STRESS_PENALTY_PREF_KEY = "STRESS_PENALTY_PREF_KEY";
    /**
     * Preferece key to locate weight penalty value
     */
    public static final String WEIGHT_PENALTY_PREF_KEY = "WEIGHT_PENALTY_PREF_KEY";
    /**
     * Preferece key to locate length penalty value
     */
    public static final String LENGTH_PENALTY_PREF_KEY = "LENGTH_PENALTY_PREF_KEY";
    /**
     * Preferece key to locate deflection penalty value
     */
    public static final String DEFLECTION_PENALTY_PREF_KEY = "DEFLECTION_PENALTY_PREF_KEY";
    /**
     * Preferece key to locate max allowed evolutions value
     */
    public static final String MAX_ALLLOWED_EVOLUTIIONS_PREF_KEY = "MAX_ALLLOWED_EVOLUTIIONS_PREF_KEY";
    /**
     * Preferece key to locate population size value
     */
    public static final String POPULATION_SIZE_PREF_KEY = "POPULATION_SIZE_PREF_KEY";
    /**
     * The value that a truss object should be penalized by if a bar does not meet compresion criteria
     */
    private double compressionPenatly;
    /**
     * The value that a truss object should be penalized by if a bar does not meet stress criteria
     */
    private double stressPenalty;
    /**
     * The value that a truss object should be penalized fro the amount it weighs
     */
    private double weightPenalty;
    /**
     * The value that a truss object should be penalized by if a bar does not meet length criteria
     */
    private double lengthPenatly;
    /**
     * The value that a truss object should be penalized by if a node does not meet deflection criteria
     */
    private double deflectionPenatly;
    /**
     * The number of the evolutions that the algorithm should evolve for
     */
    private int maxAllowedEvolutions;
    /**
     * The population size
     */
    private int populationSize;

    public GAModel() {
        super();
        prefs = Preferences.userNodeForPackage(this.getClass());
        maxAllowedEvolutions = prefs.getInt(MAX_ALLLOWED_EVOLUTIIONS_PREF_KEY, DEFAULT_MAX_ALLLOWED_EVOLUTIIONS);
        populationSize = prefs.getInt(POPULATION_SIZE_PREF_KEY, DEFAULT_POPULATION_SIZE);

        compressionPenatly = prefs.getDouble(COMPRESSION_PENALTY_PREF_KEY, DEAFULT_COMPRESSION_PENALTY);
        stressPenalty = prefs.getDouble(STRESS_PENALTY_PREF_KEY, DEFAULT_STRESS_PENALTY);
        weightPenalty = prefs.getDouble(WEIGHT_PENALTY_PREF_KEY, DEFAULT_WEIGHT_PENALTY);
        lengthPenatly = prefs.getDouble(LENGTH_PENALTY_PREF_KEY, DEFAULT_LENGTH_PENALTY);
        deflectionPenatly = prefs.getDouble(DEFLECTION_PENALTY_PREF_KEY, DEFAULT_DEFLECTION_PENALTY);
    }

    public double getLengthPenatly() {
        return lengthPenatly;
    }

    public double getCompressionPenatly() {
        return compressionPenatly;
    }

    public double getDeflectionPenatly() {
        return deflectionPenatly;
    }

    public double getStressPenalty() {
        return stressPenalty;
    }

    public double getWeightPenalty() {
        return weightPenalty;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getMaxAllowedEvolutions() {
        return maxAllowedEvolutions;
    }

    public void setWeightPenalty(double weightPenalty) {
        this.weightPenalty = weightPenalty;
        prefs.put(WEIGHT_PENALTY_PREF_KEY, Double.toString(weightPenalty));
    }

    public void setCompressionPenatly(double compressionPenatly) {
        this.compressionPenatly = compressionPenatly;
        prefs.put(COMPRESSION_PENALTY_PREF_KEY, Double.toString(compressionPenatly));
    }

    public void setDeflectionPenatly(double deflectionPenatly) {
        this.deflectionPenatly = deflectionPenatly;
        prefs.put(DEFLECTION_PENALTY_PREF_KEY, Double.toString(deflectionPenatly));
    }

    public void setLengthPenatly(double lengthPenatly) {
        this.lengthPenatly = lengthPenatly;
        prefs.put(LENGTH_PENALTY_PREF_KEY, Double.toString(lengthPenatly));
    }

    public void setStressPenalty(double stressPenalty) {
        this.stressPenalty = stressPenalty;
        prefs.put(STRESS_PENALTY_PREF_KEY, Double.toString(stressPenalty));
    }

    public void setpopulationSize(int populationSize) {
        this.populationSize = populationSize;
        prefs.put(POPULATION_SIZE_PREF_KEY, Integer.toString(populationSize));
    }

    public void setMaxAllowedEvolutions(int maxAllowedEvolutions) {
        this.maxAllowedEvolutions = maxAllowedEvolutions;
        prefs.put(MAX_ALLLOWED_EVOLUTIIONS_PREF_KEY, Double.toString(maxAllowedEvolutions));
    }
}

