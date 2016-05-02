package heartdisease;

import java.util.LinkedList;
import java.util.List;

import org.uncommons.maths.binary.BitString;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.CachingFitnessEvaluator;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.factories.BitStringFactory;
import org.uncommons.watchmaker.framework.operators.BitStringCrossover;
import org.uncommons.watchmaker.framework.operators.BitStringMutation;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.StochasticUniversalSampling;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import weka.classifiers.Evaluation;

final class C45Evolve {
	
	private static final double mutationProbability = .001;
	private static final double crossoverProbability = .75;
	private static final int populationSize = 100;
	
	class Fitness implements FitnessEvaluator<BitString> {

		@Override
		public double getFitness(BitString arg0, List<? extends BitString> arg1) {
			C45Classifier C45 = new C45Classifier(arg0);
			C45.trainClassifier();
			Evaluation e = C45.evaluate();
			if(e != null) {
				return e.correct();
			}
			return 0;
		}

		@Override
		public boolean isNatural() {
			return true;
		}
		
	}
	
	void init() {
		//set Random Initial Population Generator
		BitStringFactory fact = new BitStringFactory(44);
		
		//set evaluation class
		CachingFitnessEvaluator<BitString> eval = new CachingFitnessEvaluator<BitString>(new Fitness());
		
		//create evolutionary operators
		List< EvolutionaryOperator<BitString> > pipeline = new LinkedList< EvolutionaryOperator<BitString> >();
		pipeline.add(new BitStringMutation(new Probability(mutationProbability)));
		pipeline.add(new BitStringCrossover(2, new Probability(crossoverProbability)));
		EvolutionPipeline<BitString> operator = new EvolutionPipeline<BitString>(pipeline);
		
		//create selection scheme
		SelectionStrategy<Object> sel = new StochasticUniversalSampling();
		
		//create evolution engine
		EvolutionEngine<BitString> a = new GenerationalEvolutionEngine<BitString>(fact, operator, eval, sel, new MersenneTwisterRNG());
		
		//evolve
		a.addEvolutionObserver(new EvolutionObserver<BitString>() {

			@Override
			public void populationUpdate(
					PopulationData<? extends BitString> arg0) {
				System.out.println("Generation " + arg0.getGenerationNumber() + ":" + arg0.getBestCandidateFitness() + " by " + arg0.getBestCandidate().toString());
				System.out.println("Mean " + arg0.getMeanFitness());
			}
		});
		a.evolve(populationSize, 0, new GenerationCount(100));
	}
}

