package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.expressions.implementations.NumTreeNode;
import it.unibo.alchemist.expressions.utils.FasterString;
import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.INeighborhood;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.model.interfaces.IReaction;

import java.util.ArrayList;

import java.util.List;


/**
 * This agent to move people towards the ascending direction of a gradient,
 * following the composition choose once entering the system.
 * 
 * @author Sara Montagna
 * 
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class LsaCreatingFeedback extends SAPEREMoveNodeAgent<Double, Double> {

	private static final ILsaMolecule MOLPERSON = new LsaMolecule("person, Ctx");
	private static final ILsaMolecule MOLGRAD = new LsaMolecule("grad, St, K, Type, D, T, Sf");
	private static final FasterString K = new FasterString("K");
	private static final ILsaMolecule MOLSOURCE = new LsaMolecule("source, Type, Time,  Sf0, SfK");
	private RandomEngine random;
	private IReaction<List<? extends ILsaMolecule>> r;
	private static final double LIMIT = 0.1;
	private double startTime, timeWalked;
	private double distanceWalked;
	private boolean begin;
	private boolean once;
	private double user;
	private double initialPosx;
	private double initialPosy;
	private int run;
	private static final int GRAD_DIST_POS = 4;
	private static final int SAT_POS = 6;
	private static final int K_POS = 2;

	/**
	 * @param reaction
	 *            firing reaction
	 * @param environment
	 *            environment
	 * @param node
	 *            firing node
	 * @param aRandom
	 *            random number
	 */
	public LsaCreatingFeedback(final IReaction<List<? extends ILsaMolecule>> reaction, final IEnvironment<Double, Double, List<? extends ILsaMolecule>> environment, final ILsaNode node, final RandomEngine aRandom) {
		super(environment, node);
		this.r = reaction;
		random = aRandom;
		begin = true;
		startTime = 0;
		timeWalked = 0;
		distanceWalked = 0;
		once = true;
		run = 1;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public LsaNode getNode() {
		return (LsaNode) (super.getNode());
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
		IPosition<Double, Double> targetPosition = null;
		ILsaNode targetNode = null;
		ILsaNode sourceNode = null;

		user_initialisation(myx, myy);

		for (final INode<List<? extends ILsaMolecule>> node : neigh.getNeighbors()) {
			ILsaNode n = (ILsaNode) node;
			List<ILsaMolecule> gradList = new ArrayList<ILsaMolecule>();
			List<ILsaMolecule> sourceList = new ArrayList<ILsaMolecule>();

			addMatch(K, new NumTreeNode(user));
			gradList = n.getConcentration(new LsaMolecule(MOLGRAD.allocateVar(getMatches())));
			sourceList = n.getConcentration(MOLSOURCE);

			if (gradList.size() != 0) {
				for (int i = 0; i < gradList.size(); i++) {
					double valueGrad = getLSAArgumentAsDouble(gradList.get(i), GRAD_DIST_POS);
					if (valueGrad <= minGrad) {
						minGrad = valueGrad;
						targetPosition = getPosition(n);
						targetNode = n;
					}
				}

			}
			if (sourceList.size() != 0) {
				sourceNode = n;
			}
		}

		if (targetPosition != null) {
			x = targetPosition.getCartesianCoordinates()[0];
			y = targetPosition.getCartesianCoordinates()[1];
			double dx = x - myx;
			double dy = y - myy;
			if (targetNode.getConcentration(new LsaMolecule("crowd, Level")).size() != 0) {
				dx = dx > 0 ? Math.min(0.02, dx) : Math.max(-0.02, dx);
				dy = dy > 0 ? Math.min(0.02, dy) : Math.max(-0.02, dy);
			} else {
				dx = dx > 0 ? Math.min(LIMIT, dx) : Math.max(-LIMIT, dx);
				dy = dy > 0 ? Math.min(LIMIT, dy) : Math.max(-LIMIT, dy);
			}
			boolean moveH = dx > 0 || dx < 0;
			boolean moveV = dy > 0 || dy < 0;

			if (moveH || moveV) {
				move(new Continuous2DEuclidean(moveH ? dx : 0, moveV ? dy : 0));

				IPosition<Double, Double> myNewpos = getCurrentPosition();
				double myNewx = myNewpos.getCartesianCoordinates()[0];
				double myNewy = myNewpos.getCartesianCoordinates()[1];

				distanceWalked = distanceWalked + Math.sqrt(Math.pow((myNewx - myx), 2) + Math.pow((myNewy - myy), 2));
				timeWalked = r.getTau().toDouble() - startTime;

				// List<ILsaMolecule> fb = getNode().getConcentration(MOLFB);
				if (sourceNode != null && once) {
					ILsaMolecule molNew = new LsaMolecule("fb,target," + user + "," + distanceWalked + "," + timeWalked + "," + (distanceWalked / timeWalked));
					sourceNode.setConcentration(molNew);
					// System.out.println(sourceNode.toString()+" "+"["+user+"] - start time: "+startTime+" arrival time: "+r.getTau().toDouble()+" walking distance = "+distanceWalked+" walking time = "+timeWalked+" velocity = "+(distanceWalked/timeWalked));
					once = false;
				}

			} else {

				move(new Continuous2DEuclidean(initialPosx - myx, initialPosy - myy));
				begin = true;
				once = true;
				distanceWalked = 0;
				timeWalked = 0;
				minGrad = Double.MAX_VALUE;
			}

		}

	}

	protected void user_initialisation(final double myx, final double myy) {
		if (begin) {
			initialPosx = myx;
			initialPosy = myy;
			startTime = r.getTau().toDouble();
			ArrayList<Double[]> satisfaction = new ArrayList<>();
			double satisfactionTot = 0;
			double kParameter = 0;
			double satValue = 0;

			ILsaNode n = (ILsaNode) getLocalNeighborhood().getNeighbors().get(0);
			for (ILsaMolecule m : n.getConcentration(MOLGRAD)) {
				kParameter = getLSAArgumentAsDouble(m, K_POS);
				satValue = getLSAArgumentAsDouble(m, SAT_POS);
				satisfaction.add(new Double[] { kParameter, satValue });
				satisfactionTot += satValue;
				System.out.print(+kParameter + "  " + satValue + "  ");
			}

			final double randomNumber;
			if (run == 1) {
				try {
					user = getLSAArgumentAsDouble(getNode().getConcentration(MOLPERSON).get(0), 1);
					run = 2;
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("No person molecule available" + getNode());
				}
			} else {
				randomNumber = random.nextDouble() * satisfactionTot;

				satisfactionTot = satisfaction.get(0)[1];

				for (int i = 0; i < satisfaction.size() - 1; i++) {
					if (randomNumber < satisfactionTot) {
						user = satisfaction.get(i)[0];
						break;
					}
					satisfactionTot += satisfaction.get(i + 1)[1];
				}

			}

			System.out.println(r.getTau().toDouble() + " " + user);
			begin = false;
		}
	}

}

