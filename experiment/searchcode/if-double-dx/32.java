package it.unibo.alchemist.model.implementations.actions;

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
public class LsaMoreK extends SAPEREMoveNodeAgent<Double, Double> {

	private static final ILsaMolecule MOLFB = new LsaMolecule("fb, Type, CmpType, Space, Length, Velocity");
	private static final ILsaMolecule MOLPERSON = new LsaMolecule("person, Ctx");
	private RandomEngine random;
	private Double minGrad;
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
	private static final int GRAD = 4;
	private static final int SAT = 6;

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
	public LsaMoreK(final IReaction<List<? extends ILsaMolecule>> reaction, final IEnvironment<Double, Double, List<? extends ILsaMolecule>> environment, final ILsaNode node, final RandomEngine aRandom) {
		super(environment, node);
		this.r = reaction;
		random = aRandom;
		minGrad = Double.MAX_VALUE;
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
		final IPosition<Double, Double> mypos = getCurrentPosition();
		final double myx = mypos.getCartesianCoordinates()[0];
		final double myy = mypos.getCartesianCoordinates()[1];
		double x = 0;
		double y = 0;
		double valueGrad = 0;
		final INeighborhood<Double, List<? extends ILsaMolecule>> neigh = getLocalNeighborhood();
		List<IPosition<Double, Double>> targetPositions = new ArrayList<IPosition<Double, Double>>();
		LsaNode targetNode = new LsaNode();
		if (begin) {
			initialPosx = myx;
			initialPosy = myy;
			startTime = r.getTau().toDouble();
			final double[][] satisfaction = new double[2][];
			satisfaction[0] = new double[] { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.75, 1.0, 5, 10, 50 };
			satisfaction[1] = new double[] { 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25 };
			double kParameter = 0;
			double satValue = 0;
			for (final INode<List<? extends ILsaMolecule>> node : neigh.getNeighbors()) {
				LsaNode n = (LsaNode) node;
				for (int i = 0; i < n.getConcentration(new LsaMolecule("grad, St, K, Type, D, T, Sf")).size(); i++) {
					kParameter = (Double) n.getConcentration(new LsaMolecule("grad, St, K, Type, D, T, Sf")).get(i).getArg(2).calculate(null).getValue(null);
					satValue = (Double) n.getConcentration(new LsaMolecule("grad, St, K, Type, D, T, Sf")).get(i).getArg(SAT).calculate(null).getValue(null);
					for (int j = 0; j < satisfaction[0].length; j++) {
						if (kParameter == satisfaction[0][j]) {
							satisfaction[1][j] = satValue;
						}
					}
				}

			}
			double satisfactionTot = 0;
			for (int i = 0; i < satisfaction[0].length; i++) {
				satisfactionTot += satisfaction[1][i];
				System.out.print(+satisfaction[1][i] + " ");
			}

			final double randomNumber;
			if (run == 1) {
				try {
					user = (Double) getNode().getConcentration(MOLPERSON).get(0).getArg(1).calculate(null).getValue(null);
					run = 2;
				} catch (Exception e) {
					System.out.println("No person molecule available");
				}
			} else {
				randomNumber = random.nextDouble() * satisfactionTot;

				satisfactionTot = satisfaction[1][0];

				for (int i = 0; i < satisfaction[0].length - 1; i++) {
					if (randomNumber < satisfactionTot) {
						user = satisfaction[0][i];
						break;
					}
					satisfactionTot += satisfaction[1][i + 1];
				}

			}

			System.out.println(r.getTau().toDouble() + " " + user);
			begin = false;
		}

		for (final INode<List<? extends ILsaMolecule>> node : neigh.getNeighbors()) {
			LsaNode n = (LsaNode) node;

			List<ILsaMolecule> gradList = new ArrayList<ILsaMolecule>();

			gradList = n.getConcentration(new LsaMolecule("grad, St, " + user + ", Type, D, T, Sf"));

			if (gradList.size() != 0) {
				for (int i = 0; i < gradList.size(); i++) {
					valueGrad = (Double) gradList.get(i).getArg(GRAD).calculate(null).getValue(null);
					// System.out.println(""+valueGrad);
					if (valueGrad <= minGrad) {
						minGrad = valueGrad;
						targetPositions.add(getPosition(n));
						targetNode = n;
					}
				}

			}
		}

		if (targetPositions.size() != 0) {
			int intrnd = (int) (random.nextDouble() * (targetPositions.size() - 1));
			x = targetPositions.get(intrnd).getCartesianCoordinates()[0];
			y = targetPositions.get(intrnd).getCartesianCoordinates()[1];
			double dx = x - myx;
			double dy = y - myy;
			double beforex = dx;
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

				List<ILsaMolecule> fb = getNode().getConcentration(MOLFB);
				if (once && minGrad == 0.0 && fb.size() == 0 && beforex <= 0.2) {

					ILsaMolecule molNew = new LsaMolecule("fb,target," + user + "," + distanceWalked + "," + timeWalked + "," + (distanceWalked / timeWalked));
					getNode().setConcentration(molNew);
					// System.out.println("["+user+"] - start time: "+startTime+" arrival time: "+r.getTau().toDouble()+" walking distance = "+distanceWalked+" walking time = "+timeWalked+" velocity = "+(distanceWalked/timeWalked));
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

}

