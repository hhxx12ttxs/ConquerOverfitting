package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.model.implementations.actions.SAPEREMoveNodeAgent;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.INeighborhood;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.model.interfaces.IReaction;

import java.util.List;


/**
 * @author Sara Montagna
 * @author Danilo Pianini
 * 
 */
public class LsaAscendingAgent extends SAPEREMoveNodeAgent<Double, Double> {

	/*
	 * an agent can move at most of LIMIT along each axis
	 */
	private static final double LIMIT = 0.1;
	private static final long serialVersionUID = 228276533881360456L;
	private static final ILsaMolecule ACTIVE = new LsaMolecule("active");
	private IReaction<List<? extends ILsaMolecule>> r;
	private final ILsaMolecule template;
	private final int gradDistPos;

	private boolean firstRun = true;
	private double startTimeSIMU = 0;
	private long startTimeREAL = 0;

	/**
	 * @param reaction
	 *            firing reaction
	 * @param environment
	 *            the current environment
	 * @param node
	 *            the current node
	 * @param molecule
	 *            the LSA to inspect once moving (typically a gradient)
	 * @param pos
	 *            the position in the LSA of the value to read for identifying
	 *            the new position
	 */
	public LsaAscendingAgent(final IReaction<List<? extends ILsaMolecule>> reaction, final IEnvironment<Double, Double, List<? extends ILsaMolecule>> environment, final ILsaNode node, final LsaMolecule molecule, final int pos) {
		super(environment, node);
		this.r = reaction;
		this.template = molecule;
		this.gradDistPos = pos;
	}

	@Override
	public void execute() {
		double minGrad = Double.MAX_VALUE;		
		
		final IPosition<Double, Double> mypos = getCurrentPosition();
		final double myx = mypos.getCartesianCoordinates()[0];
		final double myy = mypos.getCartesianCoordinates()[1];
		double x = 0;
		double y = 0;
		final INeighborhood<Double, List<? extends ILsaMolecule>> neigh = getLocalNeighborhood();
		IPosition<Double, Double> targetPositions = null;
		INode<List<? extends ILsaMolecule>> bestNode = null;
		for (final INode<List<? extends ILsaMolecule>> node : neigh.getNeighbors()) {
			final ILsaNode n = (ILsaNode) node;
			final List<ILsaMolecule> gradList;
			gradList = n.getConcentration(template);
			if (gradList.size() != 0) {
				for (int i = 0; i < gradList.size(); i++) {
					double valueGrad = getLSAArgumentAsDouble(gradList.get(i), gradDistPos);
					if (valueGrad <= minGrad) {
						minGrad = valueGrad;
						targetPositions = getPosition(n);
						bestNode = n;
					}
				}

			}
		}
		if (bestNode == null || bestNode.contains(ACTIVE)) {
			return;
		}

		if (targetPositions != null) {
			x = targetPositions.getCartesianCoordinates()[0];
			y = targetPositions.getCartesianCoordinates()[1];
			double dx = x - myx;
			double dy = y - myy;
			dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
			dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);
			
			boolean moveH = dx > 0 || dx < 0;
			boolean moveV = dy > 0 || dy < 0;
			if (moveH || moveV) {
				// System.out.println("Walked Time simu = " +
				// computeWalkedTime()[0] + "  Walked Time real = " +
				// computeWalkedTime()[1 ]);
				move(new Continuous2DEuclidean(moveH ? dx : 0, moveV ? dy : 0));
				// System.out.println("Moving from " + mypos + " of [" + dx +
				// ", " + dy + "] - ended up in " + getCurrentPosition());
			}
		}

	}

	/**
	 * @return simulated time and real time at which agent reaches the source of
	 *         gradient
	 */
	protected double[] computeWalkedTime() {

		double[] walkedTime = new double[2];
		if (firstRun) {
			startTimeSIMU = r.getTau().toDouble();
			startTimeREAL = System.currentTimeMillis();
			// System.out.println("START Time SIMU = " + startTimeSIMU);
			// System.out.println("START Time REAL = " + startTimeREAL);
			firstRun = false;
		}
		walkedTime[0] = r.getTau().toDouble() - startTimeSIMU;
		walkedTime[1] = System.currentTimeMillis() - startTimeREAL;
		return walkedTime;
	}

}
