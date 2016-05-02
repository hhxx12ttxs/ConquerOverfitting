package it.unibo.alchemist.examples;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.ISimulation;
import it.unibo.alchemist.model.implementations.environments.InfiniteHalls;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IReaction;
import it.unibo.alchemist.model.interfaces.ITime;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;


/**
 * @author Francesca Cioffi
 * @version 20120712
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class LsaBarycenterSapere extends JPanel implements OutputMonitor<Double, Double, List<? extends ILsaMolecule>> {
	private static final long serialVersionUID = 334013456374890632L;

	private final ISimulation<Double, Double, List<? extends ILsaMolecule>> sim;
	private InfiniteHalls<List<? extends ILsaMolecule>> env;
	private double time;
	private long step;
	private final int border = 30;
	private LsaMolecule sensorTarget = new LsaMolecule("n");
	private final ILsaMolecule isPerson = new LsaMolecule("p");
	private final ILsaMolecule isSum = new LsaMolecule("s, X");
	// private final ILsaMolecule isSum = new LsaMolecule("f,t1,V,Ts");
	// //("f,b,V,Ts");
	private final ILsaMolecule pump = new LsaMolecule("pump,V");

	/**
	 * GUI which draws: the links between sensor nodes to create the network and
	 * nodes sources with points. The node elected to ascend the gradient of
	 * barycenter is colored red, the others are blue.
	 * 
	 * @param s
	 *            simulation
	 */

	public LsaBarycenterSapere(final ISimulation<Double, Double, List<? extends ILsaMolecule>> s) {
		super();
		this.sim = s;
	}

	@Override
	public void stepDone(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> aEnv, final IReaction<List<? extends ILsaMolecule>> r, final ITime aTime, final long aStep) {
		this.env = (InfiniteHalls<List<? extends ILsaMolecule>>) aEnv;
		this.time = aTime.toDouble();
		this.step = aStep;

		if (aStep % 10 == 0) {
			repaint();
		}
	}

	@Override
	protected void paintComponent(final Graphics graphics) {
		sim.pause();
		super.setBackground(Color.WHITE);
		super.paintComponent(graphics);

		if (env.getDimensions() == 2) {
			graphics.setColor(Color.BLACK);
			graphics.drawString("Time: " + time + " - Step: " + step, 10, 10);
			final Double[] size = env.getSize();
			final Double[] offset = env.getOffset();
			final double kx = (getSize().getWidth() - 3 * border) / size[0].doubleValue();
			final double ky = (getSize().getHeight() - 2 * border) / size[1].doubleValue();
			final int zerox = (int) (kx * (-offset[0].doubleValue()) + border);
			final int zeroy = (int) (getSize().getHeight() - ky * (-offset[1].doubleValue()) - border);
			graphics.drawLine(zerox - (int) getSize().getWidth(), zeroy, zerox + (int) getSize().getWidth(), zeroy);
			graphics.drawLine(zerox, zeroy - (int) getSize().getHeight(), zerox, zeroy + (int) getSize().getHeight());
			for (int i = -100; i <= 100; i++) {
				final int x = (int) (kx * (i - offset[0].doubleValue()) + border);
				graphics.drawString("" + i, x, zeroy + 10);
			}
			for (int i = -100; i <= 100; i++) {
				final int y = (int) (getSize().getHeight() - ky * (i - offset[1].doubleValue()) - border);
				graphics.drawString("" + i, zerox, y - 5);
			}
			for (INode<List<? extends ILsaMolecule>> nod : env.getNodes()) {
				LsaNode n = (LsaNode) nod;

				final Double[] pos = env.getPosition(nod).getCartesianCoordinates();
				final int x = (int) (kx * (pos[0] - offset[0].doubleValue()) + border);
				final int y = (int) (getSize().getHeight() - ky * (pos[1] - offset[1].doubleValue()) - border);

				if (n.getConcentration(isPerson).size() != 0 && n.getConcentration(new LsaMolecule("poi")).size() == 0) {
					graphics.setColor(Color.BLUE);
					graphics.fillOval(x - 5, y - 5, 20, 20);
				} else if (n.getConcentration(isPerson).size() == 0) {
					final List<? extends INode<List<? extends ILsaMolecule>>> neighlist = env.getNeighborhood(nod).getNeighbors();
					graphics.setColor(Color.BLACK);
					for (INode<List<? extends ILsaMolecule>> neigh : neighlist) {
						LsaNode neigh_lsa = (LsaNode) neigh;
						if ((neigh_lsa.getConcentration(isPerson).size() == 0)) {
							Double[] npos = env.getPosition(neigh).getCartesianCoordinates();
							final int nx = (int) (kx * (npos[0] - offset[0].doubleValue()) + border);
							final int ny = (int) (getSize().getHeight() - ky * (npos[1] - offset[1].doubleValue()) - border);
							graphics.drawLine(x, y, nx, ny);
						}

						/* Write the value of the sum for each sensor */

						final List<? extends ILsaMolecule> sensors = nod.getConcentration(sensorTarget);
						final int num = sensors.size();
						String[] stamp = new String[num];

						for (int i = 0; i < num; i++) {
							ILsaMolecule molecule = sensors.get(i);
							List<ILsaMolecule> nodes = n.getConcentration(molecule);
							if (!nodes.isEmpty()) {
								String s = "";
								if (n.getConcentration(isSum).size() != 0) {
									if (i != 0) {
										// overwrite the old value with the same
										// number in white
										graphics.setColor(Color.WHITE);
										graphics.drawString(stamp[i - 1], x - 2 * (sensors.size() - 1) + i * 4, (int) (y));
									}
									// write the new value
									graphics.setColor(Color.BLACK);
									s = n.getConcentration(isSum).get(0).getArg(1).toString(); // sum

									// s =
									// n.getConcentration(isSum).get(0).getArg(2).toString();
									// //field token

									if (n.getConcentration(pump).size() != 0) { // ("token")).size()
																				// !=
																				// 0)
																				// {
										// s = s + " - " +
										// n.getConcentration(new
										// LsaMolecule("token")).get(0).getArg(0).toString();
										// //token
										s = "T: " + s;
										graphics.setColor(Color.RED);
									}
									graphics.drawString(s, x - 2 * (sensors.size() - 1) + i * 4, (int) (y));

									graphics.setColor(Color.BLACK);

									stamp[i] = s;

								}
							}
						}

					}

				}

			}
		}

		sim.play();

	}
}

