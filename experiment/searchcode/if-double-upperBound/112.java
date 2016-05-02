package pl.put.poznan.eda.learn;

import ec.Breeder;
import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.Species;
import ec.Subpopulation;
import ec.util.Parameter;
import ec.vector.BitVectorIndividual;
import ec.vector.VectorSpecies;

/**
 * 
 * The Univariate Marginal Distribution algorithm implementation using the simples possible methods. This is sample implementation of the
 * algorithm which has not valid extension points. It also does not allow for fluent modification of proportion of population based breeding
 * to model based breeding.
 * 
 * <p>
 * Supports only {@linkplain BitVectorIndividual} as the individuals used for optimization, providing any other {@linkplain Individual} will
 * lead to fatal error.
 * </p>
 * 
 * <p>
 * Supports only {@linkplain VectorSpecies} as the {@linkplain Species} in the {@linkplain Subpopulation} any other type of
 * {@linkplain Species} will result in fatal error.
 * 
 * <p>
 * Does not work with threads or any other optimization technique. Setting other than <b>one</b> number of breed threads will result in
 * fatal error.
 * </p>
 * 
 * @author Piotr Jessa
 * @version 1.0
 * 
 */
@Deprecated
public class SimpleUMDABreeder extends Breeder {

    private static final long serialVersionUID = -8973717855050241746L;

    // some values that describe this breeder
    protected static final int SUBPOP_INDEX = 0;
    protected static final int THREAD_NUMBER = 0;

    // this model should be stored somewhere else than the Breeder
    protected double[] genomeProbabilites;

    @Override
    public void setup(EvolutionState state, Parameter base) {

        // reading some values from the parameters configuration
        Parameter subPopSizeParam = new Parameter(Initializer.P_POP).push(Population.P_SIZE);
        int subPopSize = state.parameters.getInt(subPopSizeParam, null, 1);

        // safe check of the size of subpopulations
        if (subPopSize != 1) {
            state.output.fatal("Unsupported number of subpopulations");
        }

        // read the genome size something
        // pop.subpop.0.species.genome-size = [value]
        Parameter genomSizeParam = new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push("0").push(Subpopulation.P_SPECIES)
                .push(VectorSpecies.P_GENOMESIZE);
        int genomeSize = state.parameters.getInt(genomSizeParam, null);

        // initialize the genome probabilities (all with 0.5 - its fair for bit)
        genomeProbabilites = new double[genomeSize];
        for (int i = 0; i < genomeProbabilites.length; i++) {
            genomeProbabilites[i] = 0.5;
        }

        // safe check
        state.output.exitIfErrors();
    }

    private int getGenomeSize(Subpopulation subpopulation) {
        VectorSpecies vectorSpecies = (VectorSpecies) subpopulation.species;
        return vectorSpecies.genomeSize;
    }

    @Override
    public Population breedPopulation(EvolutionState state) {
        Population oldPopulation = (Population) state.population.emptyClone();

        // safe check for number of breed threads
        if (state.breedthreads != 1) {
            state.output.fatal("Unsupported number of breedthreads");
        }

        BreedingPipeline pipe = (BreedingPipeline) oldPopulation.subpops[SUBPOP_INDEX].species.pipe_prototype.clone();

        pipe.prepareToProduce(state, SUBPOP_INDEX, THREAD_NUMBER);

        // decide how many individuals are need to be produced in the breeding pipe
        int counter = 0;
        int upperbound = 0 + oldPopulation.subpops[SUBPOP_INDEX].individuals.length;

        while (counter < upperbound) {

            int currentUpperbound = upperbound - counter;
            Individual[] individials = oldPopulation.subpops[SUBPOP_INDEX].individuals;

            counter += pipe.produce(1, // produce at least one
                    currentUpperbound, // produce at most the same number of individuals
                    counter, SUBPOP_INDEX, individials, state, THREAD_NUMBER);
        }

        if (counter > upperbound) {
            state.output.fatal("Unexpected situation somethin in breeding pipeline overwrote the individuals");
        }

        pipe.finishProducing(state, SUBPOP_INDEX, THREAD_NUMBER);

        // up to this part of code there is nothing more in this than stripped breeder
        // once the selection is over return update some kind of model stored
        // note that population stored between generation is the sample generation
        Subpopulation modelSubpop = oldPopulation.subpops[SUBPOP_INDEX];

        // the place where the probability will be aggregated
        int genomeSize = this.getGenomeSize(modelSubpop);
        int[] frequenyCount = new int[genomeSize];

        for (int indIndex = 0; indIndex < modelSubpop.individuals.length; indIndex++) {

            // take the bits of the genome of VectorIndividual and update the model
            BitVectorIndividual ind = null;
            try {
                ind = (BitVectorIndividual) modelSubpop.individuals[indIndex];
            } catch (ClassCastException e) {
                // call to state output fatal - finishes the ECJ, so can return the null
                state.output.fatal("Unsupported type of the individual should be BitVectorIndividual");
                return null;
            }

            boolean[] genome = ind.genome;

            // UMDA uses the sample aggregation
            // TODO: refactor this information and create some better learning models
            for (int genIndex = 0; genIndex < genome.length; genIndex++) {
                if (genome[genIndex] == true) {
                    frequenyCount[genIndex] = frequenyCount[genIndex] + 1;
                }
            }
        }

        // the new model has been build basing on the previous sample
        // time to update this model with the model stored within Breeder object
        // TODO: this update model should also be pluggable one
        int numberOfIndividuals = modelSubpop.individuals.length;
        for (int i = 0; i < genomeSize; i++) {
            double iterProb = frequenyCount[i] / (double) numberOfIndividuals;

            // arithmetic average over the probabilities
            this.genomeProbabilites[i] = (this.genomeProbabilites[i] + iterProb) / 2.0;
        }

        // sample the model stored within the Breeder object to create new population
        // TODO: sampling method should also be pluggable one
        Population newPop = (Population) state.population.emptyClone();
        Individual[] newPopInd = newPop.subpops[SUBPOP_INDEX].individuals;
        for (int indIndex = 0; indIndex < numberOfIndividuals; indIndex++) {

            // create new individual (must have the evaluated flag to false)
            BitVectorIndividual ind = (BitVectorIndividual) oldPopulation.subpops[SUBPOP_INDEX].species.newIndividual(state, THREAD_NUMBER);
            for (int geneIndex = 0; geneIndex < ind.genome.length; geneIndex++) {

                // sample the genes using the provided model
                double probability = this.genomeProbabilites[geneIndex];
                boolean value = state.random[THREAD_NUMBER].nextBoolean(probability);
                ind.genome[geneIndex] = value;
            }

            // add the individual to the new population
            newPopInd[indIndex] = ind;
        }

        return newPop;
    }

}

