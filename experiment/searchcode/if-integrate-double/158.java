package it.unibo.alchemist.model.implementations.actions.local;

import it.unibo.alchemist.model.implementations.actions.AbstractAction;
import it.unibo.alchemist.model.implementations.molecules.MessageMolecule;
import it.unibo.alchemist.model.implementations.molecules.Molecule;
import it.unibo.alchemist.model.implementations.molecules.MoleculeFactory;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.IAction;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IReaction;

/**
 * @author Luca Mella
 * 
 */
public class MessageReceived extends AbstractAction<Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6289902756830165218L;

	/**
	 * 
	 */
	private final IEnvironment<Double, Double, Double> env;
	/**
	 * 
	 */
	private final MoleculeFactory mf = MoleculeFactory.getInstance();

	/**
	 * @param node
	 * @param env
	 */
	public MessageReceived(final INode<Double> node,
			final IEnvironment<Double, Double, Double> env) {
		super(node);
		this.env = env;
	}

	@Override
	public IAction<Double> cloneOnNewNode(final INode<Double> n,
			final IReaction<Double> r) {
		return new MessageReceived(n, env);
	}

	@Override
	public void execute() {
		for (IMolecule m : getNode().getContents().keySet()) {
			if (m instanceof MessageMolecule
					&& ((MessageMolecule) m).getRecipient().equals(getNode())
					&& getNode().getConcentration(m) > 0) {

				IMolecule recvdMol = Molecule
						.getMoleculeByName(MoleculeFactory.MSGRCVDMOLPREFIX
								+ ((MessageMolecule) m).getId());

				if (recvdMol == null || !getNode().contains(recvdMol)) {

					if (recvdMol == null) {
						recvdMol = mf.newMsgRcvdMol(((MessageMolecule) m)
								.getRecipient().getId());
					}
					getNode().setConcentration(recvdMol, 1.0);
				}
				// Delete message
				getNode().setConcentration(m, 0.0);
				getNode().getContents().remove(m);
			}
		}
		// Delete all message copies (I'm doing that instead of
		// marking messMol with an ID)
		// Obviously further revision will integrate messageID, but
		// for the moment, and for
		// obtaining some measures it is not required.
		// for (INode<Double> n : env.getNodes()) {
		// for (IMolecule mol : n.getContents().keySet()) {
		// if (((MessageMolecule) mol).getRecipient().equals(getNode())
		// && n.getConcentration(mol) > 0) {
		// n.setConcentration(mol, 0.0);
		// n.getContents().remove(mol);
		// break;
		// }
		// }
		// }
	}

	@Override
	public Context getContext() {
		return Context.GLOBAL;
	}

}

