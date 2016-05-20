package it.unibo.alchemist.model.implementations.actions.local;

import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.actions.MoveNode;
import it.unibo.alchemist.model.implementations.molecules.DesireMolecule;
import it.unibo.alchemist.model.implementations.molecules.POIMolecule;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;

/**
 * @author Luca Mella
 *
 */
public class MoveToPOI extends MoveNode<Double, Double, Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8146023069474289674L;
	/**
	 * 
	 */
	private final double limit;
	/**
	 * 
	 */
	private final RandomEngine random;

	/**
	 * @param environment
	 * @param node
	 * @param limit
	 * @param rnd
	 */
	public MoveToPOI(final IEnvironment<Double, Double, Double> environment,
			final INode<Double> node, final double limit, final RandomEngine rnd) {
		super(environment, node, environment.getPosition(node), null);
		this.limit = Math.abs(limit) / Math.ceil(Math.abs(limit));
		this.random = rnd;
	}

	@Override
	public void execute() {
		DesireMolecule des = null;
		// Retrieve node's desire
		for (IMolecule m : getNode().getContents().keySet()) {
			if (m instanceof DesireMolecule
					&& getNode().getConcentration(m) > 0) {
				des = (DesireMolecule) m;
				break;
			}
		}
		if (des == null) {
			return;
		}
		// Find the poi you desire and move for it
		for (INode<Double> n : getEnvironment().getNodes()) {
			for (IMolecule m : n.getContents().keySet()) {
				if (m instanceof POIMolecule
						&& ((POIMolecule) m).sameInterest(des.getInterest())) {
					final IPosition<Double, Double> currpos = getEnvironment()
							.getPosition(getNode());
					final IPosition<Double, Double> targetpos = getEnvironment()
							.getPosition(n);
					double dx = (targetpos.getCartesianCoordinates()[0] - currpos
							.getCartesianCoordinates()[0]) * (limit);
					double dy = (targetpos.getCartesianCoordinates()[1] - currpos
							.getCartesianCoordinates()[1]) * (limit);
					// dx = dx > 0 ? Math.min(dx, limit) : Math.max(dx, -limit);
					// dy = dy > 0 ? Math.min(dy, limit) : Math.max(dy, -limit);
					dx += (random.nextDouble() - 0.5) * 10 * limit;
					dy += (random.nextDouble() - 0.5) * 10 * limit;

					final double nx = (targetpos.getCartesianCoordinates()[0] - currpos
							.getCartesianCoordinates()[0]);
					final double ny = (targetpos.getCartesianCoordinates()[1] - currpos
							.getCartesianCoordinates()[1]);

					if (Math.abs(nx) > 0 && Math.abs(ny) > 0
							&& (dx != 0 || dy != 0)) {
						setDirection(new Continuous2DEuclidean( dx, dy ));
						super.execute();
					}
					return;
				}
			}
		}
	}
}

