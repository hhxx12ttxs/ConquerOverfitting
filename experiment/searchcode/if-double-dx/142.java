package it.unibo.alchemist.model.implementations.actions;

import it.unibo.alchemist.expressions.utils.FasterString;

import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.ILsaNode;
import it.unibo.alchemist.model.interfaces.INeighborhood;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;

import java.util.List;

/**
 * @author Sara Montagna
 * 
 */
public class CrowdSteeringService extends SAPEREMoveNodeAgent<Double, Double> {

	/*
	 * an agent can move at most of LIMIT along each axis
	 */
	private static final double LIMIT = 0.1;
	private static final long serialVersionUID = 228276533881360456L;
	private static final ILsaMolecule GRADID = new LsaMolecule("gradId, GRADID");
	private static final ILsaMolecule FB = new LsaMolecule("feedback, ID");
	private static final ILsaMolecule P = new LsaMolecule("person");
	private static final ILsaMolecule GO = new LsaMolecule("go");
	private final ILsaMolecule template;
	private final int gradDistPos;
	private final int gradIdPos;


	/**
	 * @param environment
	 *            the current environment
	 * @param node
	 *            the current node
	 * @param molecule final LsaMolecule molecule, 
	 *            the LSA to inspect once moving (typically a gradient)
	 * @param idPos
	 *            the position in the LSA of the value to read for identifying
	 *            the gradient to consider
	 * @param distPos
	 * 			  the position in the LSA of the distance value 
	 * 			  to be read for identifying the direction of movement
	 */
	public CrowdSteeringService(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> environment, final ILsaNode node, final LsaMolecule molecule, final int idPos, final int distPos) {
		super(environment, node);
		addModifiedMolecule(GRADID);
		addModifiedMolecule(FB);
		addModifiedMolecule(P);
		addModifiedMolecule(GO);
		this.template = molecule;
		this.gradDistPos = distPos;
		this.gradIdPos = idPos;
	}

	@Override
	public void execute() {
		double minGrad = Double.MAX_VALUE;
		final IPosition<Double, Double> mypos = getCurrentPosition();
		final double myx = mypos.getCartesianCoordinates()[0];
		final double myy = mypos.getCartesianCoordinates()[1];
		double x = 0;
		double y = 0;

		FasterString idValue = getNode().getConcentration(GRADID).get(0).getArg(1).getAST().toFasterString();

		final INeighborhood<Double, List<? extends ILsaMolecule>> neigh = getLocalNeighborhood();
		IPosition<Double, Double> targetPositions = null;
		INode<List<? extends ILsaMolecule>> bestNode = null;
		for (final INode<List<? extends ILsaMolecule>> node : neigh.getNeighbors()) {
			final ILsaNode n = (ILsaNode) node;
			final List<ILsaMolecule> gradList;
			gradList = n.getConcentration(template);
			if (gradList.size() != 0 && !n.contains(new LsaMolecule("person"))) {
				for (int i = 0; i < gradList.size(); i++) {
					if (gradList.get(i).getArg(gradIdPos).getAST().toFasterString().equals(idValue)) {
						double valueGrad = getLSAArgumentAsDouble(gradList.get(i), gradDistPos);
						if (valueGrad <= minGrad) {
							minGrad = valueGrad;
							targetPositions = getPosition(n);
							bestNode = n;
						}
					}
				}

			}
		}
		if (bestNode == null) {
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
				move(new Continuous2DEuclidean(moveH ? dx : 0, moveV ? dy : 0));
			} else {
				if (minGrad != 0) {
					getNode().setConcentration(GO);
				}
				getNode().setConcentration(new LsaMolecule("feedback, " + idValue));
				getNode().removeConcentration(GRADID);
				if (minGrad == 0) {
					getNode().removeConcentration(P);
				}
			}
		}

	}
}
