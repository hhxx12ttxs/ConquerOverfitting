/**
 * Name: GeneticEngine
 * Goal: runs the evolution of a generation of a population
 * Methods:
 *      - Selection: chooses a pool of parents in the population for crossover.
 *                   selection method depends on the algorithm specified by the user
 *      - crossover: crosses two individuals to produce two offspring to replace
 *                   the parents in the population
 *      - mutate: mutates an individual
 *      - start: Launches the evolution of this generation. made by a thread to
 *               allow parallelism.
 * Usage: this class is used by the coevolution to run a generation of an algorithm.
 *        It should only be used internally by the coevolution
 * @see Coevolution
 * @author Numa Trezzini
 */
package org.cheminfo.scripting.JFuge.CoEvolution;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cheminfo.scripting.JFuge.JFuge;
import org.cheminfo.scripting.Utils.Tools;


public class GeneticEngine {
    
    //the population to evolve
    private LinkedList<Gene> population;
    
    //the crossover rate
    private double crossover_rate;
    
    //selection rate
    private double selection_rate;
    
    //the mutation rate
    private double mutation_rate;
    
    //the number of individuals in the population
    private int pop_size;
    
    //the method for selecting parents
    private String selection_algorithm;
    
    //the rate of elite individuals to keep into the next generation
    private double elitism_rate;
    
    //in case of tounament selection, indicates the number of participants
    private int tournament_size;
    
    //the elite of the population. Thses genomes are added into the next generation
    private LinkedList<Gene> elite = new LinkedList<Gene>();
    
    //the evolution thread
    private FutureTask<LinkedList<Gene>> evol;
    
    /**
     * Name: GeneticEngine
     * Goal: creates and sets parameters for the evolution of a population
     * @param crossover_rate
     * @param selection_rate
     * @param selection_algorithm
     * @param elitism_rate
     * @param tournament_size
     * @param mutation_rate
     * @param pop_size
     * @param gene_type
     * @param min: the min value a membership function may take
     * @param max: the max value a membership function may take
     * @param max_var_index: the number of variables
     * @param max_input_index: the number of terms an input variable may take
     * @param max_output_index: the number of terms the output variable may take
     * @param line_number: indicates the number rules
     * @param column_number: indicates the number of variables per rules
     */
    public GeneticEngine(double crossover_rate, double selection_rate, String selection_algorithm, double elitism_rate, int tournament_size, double mutation_rate, int pop_size, String gene_type, double[][] min_max, int max_var_index, int max_input_index, int max_output_index, int line_number, int column_number){
        this.elitism_rate = elitism_rate;
        this.tournament_size = tournament_size;
        this.selection_algorithm = selection_algorithm;
        this.pop_size = pop_size;
        this.crossover_rate = crossover_rate;
        this.mutation_rate = mutation_rate;
        this.selection_rate = selection_rate;
        //generate a random population upon creation
        if(gene_type.equals(Coevolution.MF))
            this.population = makeRandomMFPop(pop_size, min_max, line_number, column_number, max_var_index-1);
        else
            this.population = makeRandomRulePop(pop_size, max_var_index, max_input_index, max_output_index, line_number, column_number);
    }/*end GeneticEngine*/
    
    /**
     * Name: makeRandomMFPop
     * Gola: generates a random MF population as a basis for evolution
     * @param pop_size: the size of the population to generate
     * @param min: the minimum value a MF may take
     * @param max: the maximum value a MF may take
     * @param line_number: the number of rules
     * @param column_number: the number of variables per rule
     * @return linkedlist<Gene>: the random population
     */
    private LinkedList<Gene> makeRandomMFPop(int pop_size, double[][] min_max, int line_number, int column_number, int ante_count){
        LinkedList<Gene> mf_pop = new LinkedList<Gene>();
        MFGene g;
        for(int i = 0; i < pop_size; i++){
            g = new MFGene(min_max, ante_count);
            g.makeRandom(line_number, column_number);
            mf_pop.add(g);
        }
        return mf_pop;
    }/*end makeRandomMFPop*/
    
    /**
     * Name: makeRandomRulePop
     * Goal: generates a random rule population as a basis for evolution
     * @param pop_size: the number of individuals to generate
     * @param max_var_index: the number of variables per rule
     * @param max_input_index: the number of terms an input variable may take
     * @param max_output_index: the number of terms the output variable may take
     * @param line_length: number of rules
     * @param column_length: number of variables per rule
     * @return linkedlist<Gene>: the random population
     */
    private LinkedList<Gene> makeRandomRulePop(int pop_size, int max_var_index, int max_input_index, int max_output_index, int line_length, int column_length){
        LinkedList<Gene> rule_pop = new LinkedList<Gene>();
        RuleGene g;
        for(int i = 0; i < pop_size; i++){
            g = new RuleGene(max_var_index, max_input_index, max_output_index);
            g.makeRandom(line_length, column_length);
            rule_pop.add(g);
        }
        return rule_pop;
    }/*end makeRandomRulePop*/
    
    /**
     * Name: crossover
     * Goal: crosses two individuals
     * @param parent1
     * @param parent2
     * @return Gene[]: both offspring
     */
    public Gene[] crossover(Gene parent1, Gene parent2){
        return parent1.crossover(parent2);
    }/*end crossover*/
    
    /**
     * Name: mutates an individual randomly
     * @param individual_index : the index of the individual to mutate in the population
     */
    public void mutate(int individual_index){
        population.get(individual_index).mutate();
        //System.out.println(population.get(individual_index));
    }/*end mutate*/
    
    /**
     * Name: selection
     * Goal: chooses the parents of the new from the old one
     */
    public void selection(){
        //if elitism is activated, choose the elite
        if(elitism_rate > 0){
            elitism();
        }
        if(selection_algorithm.equals(JFuge.ROULETTE_SELECTION)){
            rouletteSelection();
        }
        else if(selection_algorithm.equals(JFuge.TOURNAMENT_SELECTION)){
            tournamentSelection();
        }
        else
            randomSelection();
    }/*end selection*/
    
    /**
     * Name: elitism
     * Goal: chooses the elite (the fittest) from the current population for
     *       reintroduction in next generation
     */
    private void elitism(){
        //clear previous elite
        elite.clear();
        //compute number of elite to choose
        int elite_count = (int)Math.round(pop_size*elitism_rate);
        //copy population list to prevent messing with acutal population order
        LinkedList<Gene> sorted_pop = (LinkedList<Gene>)population.clone();
        //sort population according to their fitness
        Collections.sort(sorted_pop, new Comparator<Gene>(){
            public int compare(Gene o1, Gene o2){
                return Double.compare(o1.getFitness(), o1.getFitness());
            }
        });
        //get the elite from sorted population
        for(int i = 0; i < elite_count; i++)
            elite.add(sorted_pop.pop());
    }/*end elitism*/
    
    /**
     * Name: rouletteSelection
     * Goal: Performs roulette selection on current population for parents of
     *       next generation
     */
    private void rouletteSelection(){
        double fit_sum = 0;
        double[] select_proba = new double[population.size()];
        LinkedList<Gene> new_pop = new LinkedList<Gene>();
        int i = 0;
        //compute fitness sum and "selection probability" for each individual
        for(Gene g : population){
            fit_sum += g.getFitness();
            select_proba[i++] = fit_sum;
        }
        //return if fitness sum is 0 (should happen only in first generation)
        if(fit_sum == 0)
            return;
        //selects individuals
        double random;
        while(new_pop.size() < pop_size){
            random = Tools.randomDouble(0, fit_sum);
            i = 0;
            while(select_proba[i] < random)
                i++;
            new_pop.add(population.get(i));
        }
        population = new_pop;
    }/*end rouletteSelection*/
    
    /**
     * Name: tournamentSelection
     * Goal: performs tournament selection on the population for the parents of
     *       the next generation
     */
    private void tournamentSelection(){
        LinkedList<Gene> pool = new LinkedList<Gene>();
        LinkedList<Gene> new_pop = new LinkedList<Gene>();
        double max_fitness;
        int max_fitness_index;
        while(new_pop.size() < pop_size){
            //choose a pool for a tournament
            for(int i = 0; i < tournament_size; i++){
                pool.add(population.get(Tools.randomInt(0, pop_size-1)));
            }
            //choose winner of tournament (fittest individual)
            max_fitness = pool.getFirst().getFitness();
            max_fitness_index = 0;
            for(int i = 0; i < pool.size(); i++){
                if(pool.get(i).getFitness() > max_fitness){
                    max_fitness = pool.get(i).getFitness();
                    max_fitness_index = i;
                }
            }
            new_pop.add(pool.get(max_fitness_index));
            //empty pool for next tournament
            pool.clear();
        }
        population = new_pop;
    }/*end tournamentSelection*/
    
    /**
     * Name: randomSelection
     * Goal: randomly selects individuals as parents for the next generation
     */
    private void randomSelection(){
        LinkedList<Gene> new_pop = new LinkedList<Gene>();
        while(new_pop.size() < population.size()){
            new_pop.add(population.get(Tools.randomInt(0, pop_size-1)));
        }
        population = new_pop;
    }/*end randomSelection*/

    public double getCrossoverRate() {
        return crossover_rate;
    }/*end getCrossoverRate*/

    public double getMutationRate() {
        return mutation_rate;
    }/*end getMutationRate*/

    public LinkedList<Gene> getPopulation() {
        return population;
    }/*end getPopulation*/

    public void setPopulation(LinkedList<Gene> population) {
        this.population = population;
    }/*end setPopulation*/

    public double getSelectionRate() {
        return selection_rate;
    }/*end getSelectionRate*/

    public int getPopSize() {
        return pop_size;
    }/*end getPopSize*/

    public LinkedList<Gene> getElite() {
        return elite;
    }/*end getElite*/
    
    /**
     * Name: start
     * Goal: creates and runs a new thread for evolving a generation of genes
     * @return LinkedList<Gene>: the new generation bred from the current one
     */
    public LinkedList<Gene> start(){
        //System.out.println("running evolution...");
        evol = new FutureTask<LinkedList<Gene>>(new GeneEvolver(this));
        evol.run();
        try {
            return evol.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(GeneticEngine.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (ExecutionException ex) {
            Logger.getLogger(GeneticEngine.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }/*end start*/
    
    
}/*end GeneticEngine*/

