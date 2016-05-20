/**
 * Name: Coevolution
 * Goal: Evolves two populations of individuals to compose fuzzy systems. The
 *       populations are rules and membership functions. Evolution is made through
 *       a custom genetic algorithm. This class should only be used internally by JFuge.
 * Mathods:
 *      - evolveSystem: evolves the populations to create fuzzy systems. The system
 *        of the last generation with the highest fitness is chosen and returned for
 *        use.The parameters of the evolution are all given by JFuge.
 * @see JFuge
 * @author Numa Trezzini
 */
package org.cheminfo.scripting.JFuge.CoEvolution;

import java.util.LinkedList;
import org.cheminfo.scripting.JFuge.FuzzyLogic.FuzzySystem;
import org.cheminfo.scripting.JFuge.JFuge;
import weka.core.Instance;
import weka.core.Instances;


public class Coevolution {
    
    //indicates if population is composed of membership functions or rules.
    protected static final String MF = "mf";
    
    protected static final String RULE = "rule";
    
    //genetic algorithm threads
    private GeneticEngine mf_evolver;
    
    private GeneticEngine rule_evolver;
    
    //data used for evaaluating systems
    private Instances train_data;
    
    //gene mutation rate
    private double mutation_rate;
    
    //gene crossover rate
    private double crossover_rate;
    
    //gene selection rate
    private double selection_rate;
    
    //population size
    private int pop_size;
    
    //number of generations
    private int num_generations;
    
    
    //maximum number of rules
    private int rule_count = 6;
    
    //minimum and maximum values each membership function may take
    private double[][] min_max;
    
    //indicates which type of selection algorithm the evolution will uses
    private String selection_algo;
    
    //indicates the rate of elitism of the algorithm
    private double elitism_rate = 0;
    
    //in case of tournament selection, indicates the size of a tournament
    private int tournament_size = 2;
    
    //indicates the weight of the classification ratio in fitness computation
    private double classification_weight = 1;
    
    //indicates the weight of the error rate in fitness computation
    private double error_weight = 1;
    
    //indicates which error computation algorithm is used
    private String error_algo = JFuge.ERROR_RMSE;
    
    //indicates the weight of the number of rules in fitness computation
    private double rule_number_weight = 1;
    
    //indicates the weight of the number of variables per rule in fitness computation
    private double var_per_rule_weight = 1;
    
    /**
     * Name: Coevolution
     * Goal: instanciaties a Coevolution algorithm with given parameterss
     * @param train_data: data used for fitness computation
     * @param mutation_rate: mutation rate of genes
     * @param crossover_rate: crossover rate of genes
     * @param selection_rate: selection rate for genes
     * @param population_size: size of MF and rule populations
     * @param num_generations: number of generations
     * @param is_binary: indicates if variables are base on binary or tertiary terms
     * @param selection_algorithm: indicates which selection algorithm the evolution uses
     * @param error_algorithm: indicates which error computation algorithm the evolution uses
     * @param elitism_rate: indicates how much of a population survives into the next generation
     * @param tournament_size: if selection is made by tournament, indicates the number of participants
     * @param classification_weight: indicates the weight of the classification rate during fitness computation
     * @param error_weight: indicates the weight of the error rate during fitness computation
     * @param rule_number_weight: indicates the weight of the number of rules during fitness computation
     * @param var_per_rule_weight: indicates the weight of the number of variables per rule during fitness computation
     */
    public Coevolution(Instances train_data, double mutation_rate,
                       double crossover_rate, double selection_rate,
                       int population_size, int num_generations,
                       boolean is_binary, String selection_algorithm,
                       String error_algorithm, double elitism_rate,
                       int tournament_size, double classification_weight,
                       double error_weight, double rule_number_weight,
                       double var_per_rule_weight, int rule_count){
        this.elitism_rate = elitism_rate;
        this.tournament_size = tournament_size;
        this.selection_algo = selection_algorithm;
        this.error_algo = error_algorithm;
        this.train_data = train_data;
        this.classification_weight = classification_weight;
        this.error_weight = error_weight;
        this.rule_number_weight = rule_number_weight;
        this.var_per_rule_weight = var_per_rule_weight;
        this.rule_count = rule_count;
        
        if(mutation_rate != -1)
            this.mutation_rate = mutation_rate;
        if(crossover_rate != -1)
            this.crossover_rate = crossover_rate;
        if(selection_rate != -1)
            this.selection_rate = selection_rate;
        if(population_size != -1)
            pop_size = population_size;
        if(num_generations != -1)
            this.num_generations = num_generations;
        //find the min and max values from the data set
        min_max = findMinMax(train_data);
        
        int max_var_index = train_data.numAttributes();
        int max_input_index = is_binary?2:3;
        int max_output_index = train_data.classAttribute().numValues();

        //System.out.println("parameters found. threads init");
        
        //creation of both genetic engines
        //max_var_index = data.numAttributes
        //max_term_index = 2-3
        //line count: nb variables+nb classes (output) = data.numAttributes+data.classAttribute.numValues()
        //column count: nb termes (2-3)
        this.mf_evolver = new GeneticEngine(this.crossover_rate, this.selection_rate, this.selection_algo, this.elitism_rate, this.tournament_size, this.mutation_rate, pop_size, MF, min_max, max_var_index, max_input_index, max_output_index, train_data.numAttributes()+train_data.classAttribute().numValues()-1, is_binary?2:3);
        
        //line count: nb regles (5-6)
        //column count: nb variables+nb classes(out) = data.numAttributes+data.classAttribute.numValues()
        this.rule_evolver = new GeneticEngine(this.crossover_rate, this.selection_rate, this.selection_algo, this.elitism_rate, this.tournament_size, this.mutation_rate, pop_size, RULE, min_max, max_var_index, max_input_index, max_output_index, rule_count, train_data.numAttributes()+train_data.classAttribute().numValues()-1);
    }/*end Coevolution*/
    
    /**
     * Name: findMinMax
     * Goal: looks into data to find minimum and maximum values MFs may take.
     * @param data1: the data in which to look
     * @return double[]: first result is min, second is max
     */
    private double[][] findMinMax(Instances data1){
        double[][] result = new double[data1.numAttributes()+data1.classAttribute().numValues()-1][2];
        result[0][0] = data1.instance(0).value(0);
        result[0][1] = data1.instance(0).value(0);
        Instance current; 
        int num_attributes = data1.numAttributes();
        for(int i = 0; i < data1.numInstances(); i++){
            current = data1.instance(i);
            //result[i][0] = current.value(0);
            for(int j = 0; j < num_attributes-1; j++){
                if(current.value(j) < result[j][0])
                    result[j][0] = current.value(j);
                if(current.value(j) > result[j][1])
                    result[j][1] = current.value(j);
            }
        }
        for(int i = data1.numAttributes()-1; i < result.length;i++){
            result[i][0] = 0;
            result[i][1] = 1;
        }
        return result;
    }/*end findMinMax*/
    
    /**
     * Name: evolveSystem
     * Goal: launches the evolution for specified number of generation, and
     *       computes fitness for each individual. Once evolution is over, the
     *       best (fittest) system is returned
     * @return FuzzySystem: the best evolved fuzzy system
     */
    public FuzzySystem evolveSystem(){
        LinkedList<Gene> mf_genes;
        LinkedList<Gene> rule_genes;
        LinkedList<FuzzySystem> systems = new LinkedList<FuzzySystem>();
        double[] fitnesses = new double[pop_size];
        for(int i = 0; i < num_generations; i++){
            
            System.out.println("=======================================================================");
            System.out.println("generation "+i);
            //run garbage collector to prevent memory over allocation problems
            System.gc();
            //clear previous generation systems
            systems.clear();
            //run evolution
            mf_genes = mf_evolver.start();
            rule_genes = rule_evolver.start();
            //evaluate all individuals of current generation
            for(int j = 0; j < pop_size; j++){
                systems.add(GeneTranslator.geneToFuzzy((MFGene)mf_genes.get(j), (RuleGene)rule_genes.get(j), train_data, false, min_max));
                fitnesses[j] = fitness(systems.get(j));
                mf_genes.get(j).setFitness(fitnesses[j]);
                rule_genes.get(j).setFitness(fitnesses[j]);
                systems.get(j).setFitness(fitnesses[j]);
            }
            
        }
        //return fittest individual after all evolutions
        return selectBest(systems, fitnesses);
    }/*end evolveSystem*/
    
    /**
     * Name: fitness
     * Goal: computes the fitness of a system according to its performance with
     *       given data.
     * @param system: the system to evaluate
     * @return double: the system's fitness
     */
    private double fitness(FuzzySystem system){
        double[][] activation = new double[train_data.numInstances()][train_data.classAttribute().numValues()];
        for(int i = 0; i < train_data.numInstances(); i++){
            activation[i] = system.distributionForInstance(train_data.instance(i));
        }
        
        //compute classification rate
        double correctly_classified_percent = computeClassificationRate(system.preclassifyInstances(train_data));
        double fitness=classification_weight*correctly_classified_percent;
        //compute error between prediction and actual result
        double error = computeError(activation);
        fitness += error_weight*Math.pow(2, -error);
        //compute number of rules
        double rule_number = system.getRules().size();
        if(rule_number == 0.0)
            return 0;
        else
            fitness += rule_number_weight*1/rule_number;
        //compute mean number of variables per rule
        double var_per_rule_number = computeVarPerRuleNumber(system);
        if(var_per_rule_number == 0.0)
            return 0;
        else
            fitness += var_per_rule_weight*1/var_per_rule_number;
        //System.out.println("fitness: "+fitness);
        return fitness;
    }/*end fitness*/
    
    /**
     * Name: computeClassificationRate
     * Goal: computes the proportion of correctly classified data instances
     * @param system: the system to evaluate
     * @return double: the percentage of correctly classified instances
     */
    private double computeClassificationRate(double[][] activation){
        int count = 0;
        
        for(int i = 0; i < train_data.numInstances(); i++){
            for(int j = 0; j < activation[i].length; j++){
                if(activation[i][j] == 1 && j == train_data.instance(i).classValue())
                    count++;
                else if(activation[i][j] == 0 && j != train_data.instance(i).classValue())
                    count++;
            }
            //System.out.println(count);
        }
        return ((double)count)/(train_data.numInstances()*train_data.classAttribute().numValues());
    }/*end computeClassificationRate*/
    
    /**
     * Name: computeError
     * Goal: computes the quantity of error the system made when classifying
     * @param system: the system to evaluate
     * @return double: the quantity of error
     */
    private double computeError(double[][] activation){
        double error;
        if(error_algo.equals(JFuge.ERROR_RMSE)){
            error = computeRMSE(activation);
        }
        else if(error_algo.equals(JFuge.ERROR_MSE)){
            error = computeMSE(activation);
        }
        else if(error_algo.equals(JFuge.ERROR_RRSE)){
            error = computeRRSE(activation);
        }
        else if(error_algo.equals(JFuge.ERROR_RAE)){
            error = computeRAE(activation);
        }
        else{
            System.out.println("error computation algorithm not supported: "+error_algo);
            return -1;
        }
        return error;
    }/*end computeError*/
    
    /**
     * Name: computeVarPerRuleNumber
     * Goal: computes the mean of variables per rule in a system
     * @param system: the system to evaluate
     * @return double: the mean number of variables per rule
     */
    private double computeVarPerRuleNumber(FuzzySystem system){
        double var_count = 0;
        //ignore default rule
        for(int i = 0; i < system.getRules().size()-1; i++){
            if(system.getRules().get(i).getAntecedents() != null)
                var_count += system.getRules().get(i).getAntecedents().size();
        }
        return var_count/(system.getRules().size()-1);
    }/*en computeVarPerRuleNumber*/
    
    /**
     * Name: selectBest
     * Goal: chooses the best system of a list according to their fitness
     * @param systems: the systems from which to choose
     * @param fitnesses: the fitness of each system from the list. indexes of a 
     *                   system and its fitness must be the same
     * @return FuzzySystem: the system with the best fitness from given  list
     */
    private FuzzySystem selectBest(LinkedList<FuzzySystem> systems, double[] fitnesses){
        int max = 0;
        for(int i = 1; i < fitnesses.length; i++){
            if(fitnesses[i] > fitnesses[max])
                max = i;
        }
        return systems.get(max);  
    }/*end selectBest*/

    /**
     * Name: computeRMSE
     * Goal: computes the Root Mean Square Error between the system's prediction
     *       and the trainig data's value
     * @param system: the system to evaluate
     * @return double: the RMSE of the system
     */
    private double computeRMSE(double[][] activation) {
        double error;
        double square_error = 0;
        for(int i = 0; i < train_data.numInstances(); i++){
            for(int j = 0; j < activation[i].length; j++){
                error = activation[i][j]-(j==train_data.instance(i).classValue()?1:0);
                square_error+=error*error;
            }   
        }
        return Math.sqrt(square_error/train_data.numInstances());
        
    }/*end computeRMSE*/

    /**
     * Name: computeMSE
     * Goal: computes the Mean Square Error between the system's prediction
     *       and the trainig data's value
     * @param system: the system to evaluate
     * @return double: the MSE of the system
     */
    private double computeMSE(double[][] activation) {
        double error;
        double square_error = 0;
        for(int i = 0; i < train_data.numInstances(); i++){
            for(int j = 0; j < activation[i].length; j++){
                error = activation[i][j]-(j==train_data.instance(i).classValue()?1:0);
                square_error+=error*error;
            }
        }
        return square_error/train_data.numInstances();
    }/*end computeMSE*/

    /**
     * Name: computeRRSE
     * Goal: computes the Root Relative Square Error between the system's prediction
     *       and the trainig data's value
     * @param system: the system to evaluate
     * @return double: the RRSE of the system
     */
    private double computeRRSE(double[][] activation) {
        double error;
        double square_error = 0;
        double mean_error;
        for(int i = 0; i < train_data.numInstances(); i++){
            for(int j = 0; j < activation[i].length; j++){
                error = activation[i][j]-(j==train_data.instance(i).classValue()?1:0);
                mean_error = (activation[i][j]+(j==train_data.instance(i).classValue()?1:0))/2;
                square_error+=(error/mean_error)*(error/mean_error);
            }
            
            
        }
        return Math.sqrt(square_error/train_data.numInstances());
    }/*end computeRRSE*/

    /**
     * Name: computeRAE
     * Goal: computes the Relative Absolute Error between the system's prediction
     *       and the trainig data's value
     * @param system: the system to evaluate
     * @return double: the RAE of the system
     */
    private double computeRAE(double[][] activation) {
        double error;
        double abs_error = 0;
        double mean_error;
        for(int i = 0; i < train_data.numInstances(); i++){
            for(int j = 0; j < activation[i].length; j++){
                error = activation[i][j]-(j==train_data.instance(i).classValue()?1:0);
                mean_error = (activation[i][j]+(j==train_data.instance(i).classValue()?1:0))/2;
                abs_error+=Math.abs(error)/mean_error;
            }
            
        }
        return abs_error/train_data.numInstances();
    }/*end computeRAE*/   
}/*end Coevolution*/

