package it.unibo.alchemist.examples;

import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.modelchecker.ParallelAPMC;
import it.unibo.alchemist.modelchecker.implementations.EventProbability;
import it.unibo.alchemist.modelchecker.implementations.Or;
import it.unibo.alchemist.modelchecker.interfaces.Observation;
import it.unibo.alchemist.modelchecker.interfaces.Property;
import it.unibo.alchemist.modelchecker.interfaces.PropertyAggregator;

import java.util.List;


/**
 * @author Danilo Pianini
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public final class MuseumModelChecking {

	private MuseumModelChecking() {
	};

	private static class AllArrived implements Observation<Boolean, Double, Double, List<? extends ILsaMolecule>> {
		private static final long serialVersionUID = 1025489866L;
		final IPosition<Double, Double> FP = new Continuous2DEuclidean(24, 5);
		boolean done = false;

		@Override
		public Boolean observe(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> env) {
			try {
				int count = 0;
				for (final INode<List<? extends ILsaMolecule>> n : env) {
					if (env.getPosition(n).getDistanceTo(FP) < 0.5) {
						count++;
						if (count > 50) {
							done = true;
							return true;
						}
					}
				}
			} catch (UncomparableDistancesException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public boolean canChange() {
			return done;
		}

		@Override
		public Observation<Boolean, Double, Double, List<? extends ILsaMolecule>> clone() {
			return new AllArrived();
		}
	}

	/**
	 * @param args
	 *            ignored
	 * @throws Exception
	 *             if somthing nasty happens (it shouldn't)
	 */
	public static void main(final String[] args) throws Exception {

		// final int frameSize = 1080;

		final AllArrived obs = new AllArrived();

		Property<Double, Double, List<? extends ILsaMolecule>, Boolean, Boolean> prop = new Or<>();
		prop.addObservation(obs);

		int n = 1000;

		PropertyAggregator<Double, Boolean> aggr = new EventProbability();
		long time = System.currentTimeMillis();
		for (double t = 34; t < 62.5; t += 1) {
			final ParallelAPMC<Double, Double, List<? extends ILsaMolecule>, Boolean, Double> ex = new ParallelAPMC<>(n, 0.01d, prop, aggr);
			ex.execute(/* "museum.xml" */"src/main/resources/Museum/xml/museum.xml", Integer.MAX_VALUE, t);
			ex.waitForCompletion();
			System.out.println(ex.getResult());
		}
		time -= System.currentTimeMillis();
		System.out.println(ParallelAPMC.computeEpsilon(n, 0.01));
		System.out.println(-time + "ms");
		System.exit(0);
	}

}

