package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.actions.SAPEREMoveNodeAgent;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.INeighborhood;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Francesca Cioffi
 * @author Danilo Pianini
 * 
 */
public class LsaBarycenterCrowd extends SAPEREMoveNodeAgent<Double, Double> {

	private static final double LIMIT = 0.1;
	private static final int MIN = 100;
	private static final long serialVersionUID = 1L;
	private Double probMoving;
	private RandomEngine random;

	/**
	 * Behavior of agents that follow the gradient of the agent chosen to reach
	 * the barycenter.
	 * 
	 * @param aEnvironment
	 *            environment
	 * @param node
	 *            node
	 * @param aRandom
	 *            random value
	 * @param p
	 *            probability for an agent of follow the right direction
	 */
	public LsaBarycenterCrowd(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> aEnvironment, final ILsaNode node, final RandomEngine aRandom, final Double p) {
		super(aEnvironment, node);
		random = aRandom;
		probMoving = p;
	}

	/* (non-Javadoc)
	 * @see alice.alchemist.model.interfaces.IAction#execute()
	 */
	@Override
	public void execute() {
		final IPosition<Double, Double> mypos = getCurrentPosition();
		final double myx = mypos.getCartesianCoordinates()[0];
		final double myy = mypos.getCartesianCoordinates()[1];
		double x = 0;
		double y = 0;
		final INeighborhood<Double, List<? extends ILsaMolecule>> neigh = getLocalNeighborhood();
		List<IPosition<Double, Double>> poss = new ArrayList<IPosition<Double, Double>>();
		double minBarycenterField = MIN;
		for (final INode<List<? extends ILsaMolecule>> nodo : neigh.getNeighbors()) {
			final ILsaNode n = (ILsaNode) nodo;
			final IPosition<Double, Double> pos = getPosition(n);
			List<? extends ILsaMolecule> barycenterList;
			try {
				barycenterList = n.getConcentration(new LsaMolecule("barycenter,V,T"));
			} catch (IndexOutOfBoundsException e) {
				barycenterList = null;
			}
			if (barycenterList != null && barycenterList.size() != 0) {
				double val, valMin = MIN;
				for (int i = 0; i < barycenterList.size(); i++) {
					val = getLSAArgumentAsDouble(barycenterList.get(i), 1);
					if (val < valMin) {
						valMin = val;
					}
				}
				if (valMin >= 0) {
					double barycenterConcentration = valMin;
					if (barycenterConcentration == minBarycenterField) {
						poss.add(pos);
					} else if (barycenterConcentration < minBarycenterField) {
						minBarycenterField = barycenterConcentration;
						poss.clear();
						poss.add(pos);
					}
				}
			}

		}
		double rnd = random.nextDouble();
		if (rnd < probMoving && poss.size() != 0) {
			int intrnd = (int) (random.nextDouble() * (poss.size() - 1));
			x = poss.get(intrnd).getCartesianCoordinates()[0];
			y = poss.get(intrnd).getCartesianCoordinates()[1];
			double dx = x - myx;
			double dy = y - myy;
			dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
			dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);
			boolean moveH = dx > 0 || dx < 0;
			boolean moveV = dy > 0 || dy < 0;
			if (moveH || moveV) {
				move(new Continuous2DEuclidean(moveH ? dx : 0, moveV ? dy : 0));
			}
		}
	}

}

