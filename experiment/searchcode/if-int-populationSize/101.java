/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anorien.dmo.gui.optimization;

import eu.anorien.dmo.Function;
import eu.anorien.dmo.Parameter;
import eu.anorien.dmo.Server;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author David Miguel Antunes <davidmiguel [ at ] antunes.net>
 */
public class GeneticOptimization extends Optimization {

    public static enum CrossoverType {

        CROSSOVER_50_50_DIVISION,
        CROSSOVER_RANDOM_DIVISION,
        CROSSOVER_RANDOM,
        NO_CROSSOVER,}
    private GeneticOptimizationConfigPanel configPanel;
    private Map<ArrayList<Integer>, Double> population;
    private final Object searchThreadWait = new Object();
    private final Object coordinatorThreadWait = new Object();
    // Genetic algorithm parameters:
    private double mutationProbability;
    private int populationSize;
    private int eliteSize;
    private int usedForCrossoverSize;
    private int survivingCrossedSize;
    private int newRandomSize;
    private CrossoverType crossoverType;

    class SearchThread extends Thread {

        @Override
        public void run() {
            while (!terminate) {
                ArrayList<Integer> nextChromosome = nextChromosome();
                if (nextChromosome == null) {
                    synchronized (searchThreadWait) {
                        try {
                            if (terminate || Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                            searchThreadWait.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                } else {
                    processChromosome(nextChromosome);
                }
            }
        }

        private ArrayList<Integer> nextChromosome() {
            ArrayList<Integer> neighborPoint = null;
            synchronized (GeneticOptimization.this) {
                for (Map.Entry<ArrayList<Integer>, Double> entry : population.entrySet()) {
                    if (entry.getValue() == null) {
                        neighborPoint = entry.getKey();
                        population.put(neighborPoint, new Double(-1));
                        break;
                    }
                }
            }
            return neighborPoint;
        }

        private void processChromosome(ArrayList<Integer> neighborPoint) {
            double error = runFunction(pointToParams(neighborPoint));
            output.write("Chromosome [" + paramsToString(pointToParams(neighborPoint)) + "] has a value of " + (1 / error));
            synchronized (GeneticOptimization.this) {
                population.put(neighborPoint, error);
            }
            synchronized (coordinatorThreadWait) {
                coordinatorThreadWait.notifyAll();
            }
        }
    }

    class CoordinatorThread extends Thread {

        @Override
        public void run() {
            outter:
            while (!terminate) {
                synchronized (GeneticOptimization.this) {
                    for (Map.Entry<ArrayList<Integer>, Double> entry : population.entrySet()) {
                        if (entry.getValue() == null || entry.getValue() == -1) {
                            continue outter;
                        }
                    }
                    // All chromosomes are processed:
                    ArrayList<ArrayList<Integer>> rankedPopulation = new ArrayList<ArrayList<Integer>>(population.keySet());
                    // Decreasing order:
                    Collections.sort(rankedPopulation, new Comparator<ArrayList<Integer>>() {

                        @Override
                        public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
                            return population.get(o1).compareTo(population.get(o2));
                        }
                    });
                    List<ArrayList<Integer>> surviving = rankedPopulation.subList(0, usedForCrossoverSize);

                    output.write("All chromosomes where processed, population performance:");
                    for (ArrayList<Integer> chromosome : rankedPopulation) {
                        output.write("\t" + ((int) ((1 / population.get(chromosome)) * 1000)) / 1000d);
                    }

                    output.write("Generating a new population...");
                    population = new HashMap<ArrayList<Integer>, Double>();

                    // Crossover:
                    List<ArrayList<Integer>> survivorsCrossedChromosome;
                    switch (crossoverType) {
                        case CROSSOVER_50_50_DIVISION:
                            survivorsCrossedChromosome = crossover5050(surviving, survivingCrossedSize, false);
                            break;
                        case CROSSOVER_RANDOM_DIVISION:
                            survivorsCrossedChromosome = crossover5050(surviving, survivingCrossedSize, true);
                            break;
                        case CROSSOVER_RANDOM:
                            survivorsCrossedChromosome = crossoverRandom(surviving, survivingCrossedSize);
                            break;
                        case NO_CROSSOVER:
                            survivorsCrossedChromosome = noCrossover(surviving, survivingCrossedSize);
                            break;
                        default:
                            throw new RuntimeException("BUG");
                    }

                    // Mutation:
                    mutate(survivorsCrossedChromosome);

                    // New random chromosomes:
                    List<ArrayList<Integer>> randomChromosome = randomChromosomes(newRandomSize);

                    for (ArrayList<Integer> chromosome : rankedPopulation.subList(0, eliteSize)) {
                        population.put(chromosome, null);
                    }
                    for (ArrayList<Integer> chromosome : survivorsCrossedChromosome) {
                        population.put(chromosome, null);
                    }
                    for (ArrayList<Integer> chromosome : randomChromosome) {
                        population.put(chromosome, null);
                    }
                }
                synchronized (searchThreadWait) {
                    searchThreadWait.notifyAll();
                }
                synchronized (coordinatorThreadWait) {
                    try {
                        if (terminate || Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                        coordinatorThreadWait.wait();
                    } catch (InterruptedException ex) {
                    }
                }

            }
        }

        private List<ArrayList<Integer>> randomChromosomes(int n) {
            ArrayList<ArrayList<Integer>> newChromosomes = new ArrayList<ArrayList<Integer>>();
            for (int i = 0; i < n; i++) {
                newChromosomes.add(newRandomPoint());
            }
            return newChromosomes;
        }

        private void mutate(List<ArrayList<Integer>> chromosomes) {
            for (ArrayList<Integer> chromosome : chromosomes) {
                for (int i = 0; i < chromosome.size(); i++) {
                    if (random.nextDouble() < mutationProbability) {
                        chromosome.set(i, random.nextInt(function.getParameters().get(i).getNSteps()));
                    }
                }
            }
        }

        private List<ArrayList<Integer>> crossover5050(List<ArrayList<Integer>> chromosomes, int n, boolean randomDivision) {
            ArrayList<ArrayList<Integer>> newChromosomes = new ArrayList<ArrayList<Integer>>();
            if (!chromosomes.isEmpty()) {
                if (chromosomes.size() == 1) {
                    for (int i = 0; i < n; i++) {
                        newChromosomes.add(chromosomes.get(0));
                    }
                } else {
                    for (int i = 0; i < n; i++) {
                        int i1 = random.nextInt(chromosomes.size());
                        int i2 = random.nextInt(chromosomes.size());
                        while (i1 == i2) {
                            i2 = random.nextInt(chromosomes.size());
                        }
                        int midPoint;
                        if (randomDivision) {
                            midPoint = random.nextInt(function.getParameters().size());
                        } else {
                            midPoint = function.getParameters().size() / 2;
                        }
                        ArrayList<Integer> newChromosome = new ArrayList<Integer>();
                        newChromosome.addAll(chromosomes.get(i1).subList(0, midPoint));
                        newChromosome.addAll(chromosomes.get(i2).subList(midPoint, chromosomes.get(i2).size()));
                        newChromosomes.add(newChromosome);
                    }
                }
            }
            return newChromosomes;
        }

        private List<ArrayList<Integer>> crossoverRandom(List<ArrayList<Integer>> chromosomes, int n) {
            ArrayList<ArrayList<Integer>> newChromosomes = new ArrayList<ArrayList<Integer>>();
            if (!chromosomes.isEmpty()) {
                if (chromosomes.size() == 1) {
                    for (int i = 0; i < n; i++) {
                        newChromosomes.add(chromosomes.get(0));
                    }
                } else {
                    for (int i = 0; i < n; i++) {
                        int i1 = random.nextInt(chromosomes.size());
                        int i2 = random.nextInt(chromosomes.size());
                        while (i1 == i2) {
                            i2 = random.nextInt(chromosomes.size());
                        }
                        ArrayList<Integer> newChromosome = new ArrayList<Integer>();
                        for (int j = 0; j < function.getParameters().size(); j++) {
                            newChromosome.add(chromosomes.get(random.nextBoolean() ? i1 : i2).get(j));
                        }
                        newChromosomes.add(newChromosome);
                    }
                }
            }
            return newChromosomes;
        }

        private List<ArrayList<Integer>> noCrossover(List<ArrayList<Integer>> chromosomes, int n) {
            ArrayList<ArrayList<Integer>> newChromosomes = new ArrayList<ArrayList<Integer>>();
            for (int i = 0; i < n; i++) {
                newChromosomes.add(chromosomes.get(i % chromosomes.size()));
            }
            return newChromosomes;
        }
    }
    private static final Logger logger = Logger.getLogger(HillClimbingOptimization.class);

    public GeneticOptimization(Function function, ArrayList<Server> servers) {
        super(function, servers);
        this.function = function;
        configPanel = new GeneticOptimizationConfigPanel(this);
        tabbedPane.add(configPanel, 0);
        tabbedPane.setTitleAt(0, "Configuration");
        tabbedPane.setSelectedIndex(0);
    }

    @Override
    public void stop() {
        configPanel.getStopButton().setEnabled(false);
        super.stop();
    }

    @Override
    public void start() {
        super.start();
        configPanel.getStartButton().setEnabled(false);
        output.write("Starting optimization..." + "\n");

        populationSize = configPanel.getPopulationSize();
        usedForCrossoverSize = (int) (populationSize * configPanel.getPercentUsedForCrossover());
        crossoverType = configPanel.getCrossoverType();
        mutationProbability = configPanel.getMutationProb();

        eliteSize = (int) (populationSize * configPanel.getPercentElite());
        newRandomSize = (int) (populationSize * configPanel.getPercentRandom());
        survivingCrossedSize = populationSize - eliteSize - newRandomSize;
        
        random = new Random(configPanel.getRandomSeed());
        stopAfter = configPanel.getStopAfter();

        // Create initial population:
        population = new HashMap<ArrayList<Integer>, Double>();
        for (int i = 0; i < populationSize; i++) {
            population.put(newRandomPoint(), null);
        }

        output.write("Population size: " + populationSize);
        output.write("Elite size: " + eliteSize);
        output.write("Crossover result size: " + survivingCrossedSize);
        output.write("New random chromosomes size: " + newRandomSize);
        output.write("Number of chromosomes selected for crossover/mutation: " + usedForCrossoverSize);
        output.write("Crossover type: " + crossoverType.toString());
        output.write("Mutation probability: " + ((int) ((mutationProbability) * 1000)) / 1000d);

        for (int i = 0; i < configPanel.getNumServersPerProbe(); i++) {
            SearchThread t = new SearchThread();
            t.start();
            threads.add(t);
        }
        CoordinatorThread t = new CoordinatorThread();
        t.start();
        threads.add(t);

        configPanel.getStopButton().setEnabled(true);
    }

    @Override
    public String toString() {
        return function.getName() + " Genetic Algorithm";
    }
}

