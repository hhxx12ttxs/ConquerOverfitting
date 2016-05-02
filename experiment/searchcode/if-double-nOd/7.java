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


/***
 * 
 * @author Francesca Cioffi
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class LsaBealGradientMobileSource extends JPanel implements OutputMonitor<Double, Double, List<? extends ILsaMolecule>> {

	private static final long serialVersionUID = -7234901730811844127L;
	private final ISimulation<Double, Double, List<? extends ILsaMolecule>> sim;
	private InfiniteHalls<List<? extends ILsaMolecule>> env;
	private double time;
	private long step;
	private final int border = 30;
	private LsaMolecule sensorTarget = new LsaMolecule("id,Name");
	private final ILsaMolecule isField = new LsaMolecule("field,source,TimeStamp,Value");

	// private final ILsaMolecule isField = new
	// LsaMolecule("field,dest,TimeStamp,Value");

	/**
	 * 
	 * @param s
	 *            simulation
	 */
	public LsaBealGradientMobileSource(final ISimulation<Double, Double, List<? extends ILsaMolecule>> s) {
		super();
		this.sim = s;
	}

	@Override
	public void stepDone(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> env, final IReaction<List<? extends ILsaMolecule>> r, final ITime time, final long step) {
		this.env = (InfiniteHalls<List<? extends ILsaMolecule>>) env;
		this.time = time.toDouble();
		this.step = step;

		if (step % 10 == 0) {
			repaint();
		}

	}

	@Override
	protected void paintComponent(final Graphics graphics) { // synchronized
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

				final List<? extends INode<List<? extends ILsaMolecule>>> neighlist = env.getNeighborhood(nod).getNeighbors();
				graphics.setColor(Color.BLACK);
				for (INode<List<? extends ILsaMolecule>> neigh : neighlist) {
					Double[] npos = env.getPosition(neigh).getCartesianCoordinates();
					final int nx = (int) (kx * (npos[0] - offset[0].doubleValue()) + border);
					final int ny = (int) (getSize().getHeight() - ky * (npos[1] - offset[1].doubleValue()) - border);
					graphics.drawLine(x, y, nx, ny);

					/* Write the value of the field source for each sensor */

					final List<? extends ILsaMolecule> sensors = nod.getConcentration(sensorTarget);
					final int num = sensors.size();
					String[] stamp = new String[num];

					for (int i = 0; i < num; i++) {
						ILsaMolecule molecule = sensors.get(i);
						List<ILsaMolecule> nodes = n.getConcentration(molecule);
						if (!nodes.isEmpty()) {
							String s = "";

							if (n.getConcentration(isField).size() != 0) {

								if (i != 0) {
									// overwrite the old value with the same
									// number in white
									graphics.setColor(Color.WHITE);
									graphics.drawString(stamp[i - 1], x - 2 * (sensors.size() - 1) + i * 4, (int) (y));
								}
								// write the new value
								graphics.setColor(Color.BLACK);

								s = n.getConcentration(isField).get(0).getArg(3).toString();

								// if (n.getConcentration(new
								// LsaMolecule("move")).size() != 0) {
								// //s = s + " - " + n.getConcentration(new
								// LsaMolecule("token")).get(0).getArg(0).toString();
								// //token
								// s = "S - " + s;
								// }
								graphics.drawString(s, x - 2 * (sensors.size() - 1) + i * 4, (int) (y));

								stamp[i] = s;

							}
						}

					}

				}

			}
		}

		sim.play();

	}

}

