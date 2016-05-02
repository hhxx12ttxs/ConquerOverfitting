package pl.put.poznan.eda;

import java.util.List;

import pl.put.poznan.eda.exchanger.TopExchanger;

import ec.Breeder;
import ec.BreedingPipeline;
import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.Prototype;
import ec.SelectionMethod;
import ec.Species;
import ec.Subpopulation;
import ec.breed.ReproductionPipeline;
import ec.simple.SimpleBreeder;
import ec.util.Parameter;

/**
 * The universal implementation of the {@linkplain Breeder} class which uses the {@linkplain IModelSampler} and {@linkplain IModelUpdater}
 * for EDA related tasks of modifying the distribution and generating the new populations.
 * <p>
 * Supports only the {@linkplain ModelSubpopulation}, providing any other {@linkplain Subpopulation} will result in fatal error.
 * </p>
 * 
 * <p>
 * Does not work with threads or any other optimization technique. Setting other than <b>one</b> number of breed threads will result in
 * fatal error. Main reason being not using {@linkplain SimpleBreeder} was fact that implementing <b>EDA</b> would require a lot of thread
 * safe operations which could lead to throttling, also {@linkplain BreedingSource} is a form of a {@linkplain Prototype} whereas its number
 * cannot be controlled easily.
 * </p>
 * 
 * <p>
 * This class supports parameterization in case of introducing the number (percentage) of individuals which should be saved from the
 * previous result of {@linkplain BreedingPipeline}. <b>Default value of <i>exchange ratio</i> is 0.5</b>. If there is a case that
 * {@linkplain BreedingPipeline} is only the {@linkplain SelectionMethod} with some sort of {@linkplain ReproductionPipeline} then samples
 * from EDA algorithms will be mixed in this proportion to the result pool - creating new population for next generation.
 * </p>
 * 
 * @author Piotr Jessa
 * @since 0.0.1
 * 
 */
public class SimpleEDABreeder extends Breeder {

    private static final long serialVersionUID = 6382050352200573976L;

    // configuration of the simple EDA breeder
    public static final String P_UPDATER = "updater";
    public static final String P_SAMPLER = "sampler";

    public static final String P_EXCHANGE_RATIO = "exchange-ratio";

    // other parameter outside this class which are needed to determine the final look of this instance
    public static final Parameter U_MODEL = new Parameter(Initializer.P_POP).push(Subpopulation.P_SUBPOPULATION).push("0")
            .push(ModelSubpopulation.P_MODEL);

    public static final Parameter U_INDIVIDUAL = new Parameter(Initializer.P_POP).push(Subpopulation.P_SUBPOPULATION).push("0")
            .push(Subpopulation.P_SPECIES).push(Species.P_INDIVIDUAL);

    public static final Parameter U_SUB_POPULATION_SIZE = new Parameter(Initializer.P_POP).push(Population.P_SIZE);

    // some constants
    protected static final int C_SUBPOP_INDEX = 0;
    protected static final int C_THREAD_NUMBER = 0;

    // pluggable things for the EDA algorithms
    private IModelUpdater updater;
    private IModelSampler sampler;

    private double exchangeRatio;

    private ModelSubpopulation modelSubpopulation;
    private Population oldPopulation;

    private List<Individual> samples;

    // TODO: this top exchanger feature should be removed from the target version
    private IExchanger exchanger = new TopExchanger();

    @Override
    public void setup(EvolutionState state, Parameter base) {

        // reading some values from the parameters configuration
        int subPopSize = state.parameters.getInt(U_SUB_POPULATION_SIZE, null, 1);

        // safe checks of (because setup is only done once):
        // size of subpopulation number of breed threads matchability of updater and sampler with chosen model
        if (subPopSize != 1) {
            state.output.fatal("Unsupported number of subpopulations");
        }
        if (state.breedthreads != 1) {
            state.output.fatal("Unsupported number of breedthreads");
        }

        // prepare the exchange ratio
        Parameter exchangeParameter = base.push(P_EXCHANGE_RATIO);
        this.exchangeRatio = state.parameters.getDouble(exchangeParameter, null, 0.0);
        if (exchangeRatio == -1.0) {
            exchangeRatio = 0.5;
        }

        if (exchangeRatio < 0.0 || exchangeRatio > 1.0) {
            state.output.fatal("Unsupported value of exchange ration, should be [0.0,1.0] but was " + exchangeRatio);
        }

        // prepare the updater
        Parameter updaterParameter = base.push(P_UPDATER);
        this.updater = (IModelUpdater) state.parameters.getInstanceForParameter(updaterParameter, null, IModelUpdater.class);
        this.updater.setup(state, updaterParameter);

        // prepare the sampler
        Parameter samplerParameter = base.push(P_SAMPLER);
        this.sampler = (IModelSampler) state.parameters.getInstanceForParameter(samplerParameter, null, IModelSampler.class);
        this.sampler.setup(state, samplerParameter);

        // perform the check of the models in the subpopulation
        // this can be done without creating the instance of the model
        // but it will require impact onto ParametersDatabase class
        Model model = (Model) state.parameters.getInstanceForParameter(U_MODEL, null, Model.class);
        Class<? extends Model> modelCls = model.getClass();
        if (this.sampler.matchModelType(modelCls) == false) {
            state.output.fatal("The " + sampler + "does not match with the type of" + model);
        }
        if (this.updater.matchModelType(modelCls) == false) {
            state.output.fatal("The " + updater + " does not match with the type of" + model);
        }

        // perform the check of representation of subpopulation
        Individual ind = (Individual) state.parameters.getInstanceForParameter(U_INDIVIDUAL, null, Individual.class);
        Class<? extends Individual> indCls = ind.getClass();
        if (this.sampler.matchIndividualType(indCls) == false) {
            state.output.fatal("The " + sampler + "does not match with the type of" + ind);
        }
        if (this.updater.matchIndividualType(indCls) == false) {
            state.output.fatal("The " + updater + "does not match with the type of " + ind);
        }

        // mark the classes for use
        sampler.markIndividualType(indCls);
        updater.markIndividualType(indCls);

        // safe check
        state.output.exitIfErrors();
    }

    @Override
    public Population breedPopulation(EvolutionState state) {

        Population newPop = (Population) state.population.emptyClone();
        oldPopulation = (Population) state.population;

        // run breeding pipe
        this.breedClassicaly(state);

        modelSubpopulation = (ModelSubpopulation) state.population.subpops[C_SUBPOP_INDEX];

        // take the model from population
        this.updater.retrieveModel(modelSubpopulation, state);

        // update the main model using the taken model from population
        this.updater.applyModel(modelSubpopulation, state);

        int breedingOutput = modelSubpopulation.individuals.length;
        int exchangeAmount = (int) Math.floor(breedingOutput * exchangeRatio);

        // of individuals to be probed into subpopulation
        Species species = modelSubpopulation.species;
        Model model = modelSubpopulation.getModel();
        samples = this.sampler.sampleModel(model, exchangeAmount, species, state);

        // change the current
        ModelSubpopulation newSubPopulation = (ModelSubpopulation) newPop.subpops[C_SUBPOP_INDEX];
        this.exchanger.incorporate(samples, modelSubpopulation, newSubPopulation, state);

        return newPop;
    }

    protected void breedClassicaly(EvolutionState state) {

        // the breeding pipe allows to create mixing of the normal population based methods
        // and EDA based methods
        BreedingPipeline pipe = (BreedingPipeline) oldPopulation.subpops[C_SUBPOP_INDEX].species.pipe_prototype.clone();

        pipe.prepareToProduce(state, C_SUBPOP_INDEX, C_THREAD_NUMBER);

        // decide how many individuals are need to be produced in the breeding pipe
        int counter = 0;
        int upperbound = 0 + oldPopulation.subpops[C_SUBPOP_INDEX].individuals.length;

        while (counter < upperbound) {

            int currentUpperbound = upperbound - counter;
            Individual[] individials = oldPopulation.subpops[C_SUBPOP_INDEX].individuals;

            counter += pipe.produce(1, // produce at least one
                    currentUpperbound, // produce at most the same number of individuals
                    counter, C_SUBPOP_INDEX, individials, state, C_THREAD_NUMBER);
        }

        if (counter > upperbound) {
            state.output.fatal("Unexpected situation somethin in breeding pipeline overwrote the individuals");
        }

        pipe.finishProducing(state, C_SUBPOP_INDEX, C_THREAD_NUMBER);
    }

    public IModelUpdater getUpdater() {
        return updater;
    }

    public IModelSampler getSampler() {
        return sampler;
    }

    public double getExchangeRatio() {
        return exchangeRatio;
    }

}

