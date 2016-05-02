package it.unibo.alchemist.examples;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.ISimulation;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IReaction;
import it.unibo.alchemist.model.interfaces.ITime;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;


/**
 * @author Francesca Cioffi
 * @version 20120718
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class LsaBarycenterGradientNotUniformEnv extends JPanel implements OutputMonitor<Double, Double, List<? extends ILsaMolecule>> {

	private final boolean debugMode = false;

	private static final long serialVersionUID = 334013456374890632L;
	private static final double INTERVAL_TIME = 25d;
	private static final double STEP_TIME = 1d / INTERVAL_TIME;
	private IEnvironment<Double, Double, List<? extends ILsaMolecule>> env;
	private double time, lastRefresh = 0;
	private long step;
	private static final int BORDER = 30;
	// private LsaMolecule sensorTarget = new LsaMolecule("field,Type,V");

	private LsaMolecule sensorTarget = new LsaMolecule("n,V"); // ("id,V");

	private final ISimulation<Double, Double, List<? extends ILsaMolecule>> sim;
	private final ILsaMolecule isPerson = new LsaMolecule("p"); // ("person");

	// private final ILsaMolecule isSum = new LsaMolecule("sum, X"); //

	// gradiente somma delle distanze al quadrato

	// sum
	// private final ILsaMolecule isSum = new LsaMolecule("s, X");

	// field
	private final ILsaMolecule isSum = new LsaMolecule("f,b,Ts,V"); // ("field,barycenter,TimeStamp,Value");
																	// // field
																	// token

	// private final ILsaMolecule isSum = new LsaMolecule("f,t1,Ts,V");

	/**
	 * GUI which draws: the links between sensor nodes to create the network and
	 * nodes sources with points. The node elected to ascend the gradient of
	 * barycenter is colored red, the others are blue.
	 * 
	 * @param s
	 *            simulation
	 */

	public LsaBarycenterGradientNotUniformEnv(final ISimulation<Double, Double, List<? extends ILsaMolecule>> s) {
		super();
		this.sim = s;
	}

	@Override
	public void stepDone(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> aEnv, final IReaction<List<? extends ILsaMolecule>> r, final ITime aTime, final long aStep) {
		this.time = aTime.toDouble();
		if (lastRefresh <= time) {
			this.env = aEnv;
			this.step = aStep;
			repaint();
			lastRefresh = time + STEP_TIME;
		}
	}

	@SuppressWarnings("unused")
	@Override
	protected void paintComponent(final Graphics graphics) {
		final float color = 0.1f, color1 = 0.7f, color2 = 1.0f;
		final float miterLimit = 10.0f;
		final float dash1 = 4.0f, dash2 = 12.0f;
		final float minValue = 16f;
		final float widthStroke = 65.0f;
		final float multKy = 0.9f;
		final float colorK1 = 0.9f, colorK2 = 0.5f, colorK3 = 0.2f;
		final int sizeXBorder = 3, sizeYBorder = 2;
		final int xSpace = 4;
		final int argSum = 3;
		final int sizeRect = 14, widthRect = 28;
		final int gridInterval = 100;
		final int spaceS = 4;
		final int spaceZerox = 15, spaceZeroy = 17;
		final int dimensionOval = 10, widthHeightOval = 20;
		final int dimensionK = 15, widthHeightK = 30;
		final int dimensionK2 = 5, widthHeightK2 = 10;
		final int width = 35;
		final int rectTimeWidth = 25, rectTimeWidth2 = 7, rectTimeHeight = 22;

		sim.pause();
		super.setBackground(Color.WHITE);
		super.paintComponent(graphics);
		// Font fontDefault = graphics.getFont();
		// Font fontToken = new Font("Helvetica", Font.BOLD, 16);
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

		if (!debugMode) {
			g2d.setColor(new Color(0.0f, color, color));
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

		if (env.getDimensions() == 2) {
			final Double[] size = env.getSize();
			final Double[] offset = env.getOffset();
			final double kx = (getSize().getWidth() - sizeXBorder * BORDER) / size[0].doubleValue();
			final double ky = (getSize().getHeight() - sizeYBorder * BORDER) / size[1].doubleValue();
			final int zerox = (int) (kx * (-offset[0].doubleValue()) + BORDER);
			final int zeroy = (int) (getSize().getHeight() - ky * (-offset[1].doubleValue()) - BORDER);

			for (INode<List<? extends ILsaMolecule>> nod : env.getNodes()) {
				LsaNode n = (LsaNode) nod;

				final Double[] pos = env.getPosition(nod).getCartesianCoordinates();
				final int x = (int) (kx * (pos[0] - offset[0].doubleValue()) + BORDER);
				final int y = (int) (getSize().getHeight() - ky * (pos[1] - offset[1].doubleValue()) - BORDER);

				if (debugMode) {
					if (n.getConcentration(isPerson).size() == 0) {
						final List<? extends INode<List<? extends ILsaMolecule>>> neighlist = env.getNeighborhood(nod).getNeighbors();
						for (INode<List<? extends ILsaMolecule>> neigh : neighlist) {
							LsaNode neigLSA = (LsaNode) neigh;
							if ((neigLSA.getConcentration(isPerson).size() == 0)) {
								Double[] npos = env.getPosition(neigh).getCartesianCoordinates();
								final int nx = (int) (kx * (npos[0] - offset[0].doubleValue()) + BORDER);
								final int ny = (int) (getSize().getHeight() - ky * (npos[1] - offset[1].doubleValue()) - BORDER);
								g2d.setColor(Color.ORANGE);
								g2d.setStroke(new BasicStroke(1.0f, // Width
										BasicStroke.CAP_ROUND, // End cap
										BasicStroke.JOIN_BEVEL, // Join style
										miterLimit, // Miter limit
										new float[] { dash1, dash2 }, 0.0f)); // Dash
																				// phase);
								g2d.drawLine(x, y, nx, ny);
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
											// overwrite the old value with the
											// same
											// number in white
											g2d.setColor(Color.WHITE);
											g2d.drawString(stamp[i - 1], x - 2 * (sensors.size() - 1) + i * xSpace, (int) (y));
										}
										// write the new value
										g2d.setColor(Color.BLACK);
										// s =
										// n.getConcentration(isSum).get(0).getArg(1).toString();
										// sum

										s = n.getConcentration(isSum).get(0).getArg(argSum).toString();
										float value = Float.parseFloat(s);

										value = Math.min(value, minValue);
										value /= minValue;
										g2d.setColor(new Color(value, color1, color2 - value));
										// g2d.setColor(Color.RED);
										g2d.fillRect((int) (x - sizeRect), (int) (y - value * (ky * multKy)), widthRect, (int) (value * (ky * multKy)));

										stamp[i] = s;

									}
								}
							}
						}
					}
				} else {
					if (n.getConcentration(isPerson).size() == 0) {
						final List<? extends INode<List<? extends ILsaMolecule>>> neighlist = env.getNeighborhood(nod).getNeighbors();
						for (INode<List<? extends ILsaMolecule>> neigh : neighlist) {
							LsaNode neigLSA = (LsaNode) neigh;
							if ((neigLSA.getConcentration(isPerson).size() == 0)) {
								Double[] npos = env.getPosition(neigh).getCartesianCoordinates();
								final int nx = (int) (kx * (npos[0] - offset[0].doubleValue()) + BORDER);
								final int ny = (int) (getSize().getHeight() - ky * (npos[1] - offset[1].doubleValue()) - BORDER);
								g2d.setColor(Color.WHITE);
								g2d.setStroke(new BasicStroke(widthStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
								g2d.drawLine(x, y, nx, ny);
							}
						}
					}
				}
			}

			if (debugMode) {
				g2d.setColor(Color.BLACK);
				g2d.setStroke(new BasicStroke());
				g2d.drawLine(zerox - (int) getSize().getWidth(), zeroy, zerox + (int) getSize().getWidth(), zeroy);
				g2d.drawLine(zerox, zeroy - (int) getSize().getHeight(), zerox, zeroy + (int) getSize().getHeight());
				int sx, sy;
				for (int i = -gridInterval; i <= gridInterval; i++) {
					sx = (int) (kx * (i - offset[0].doubleValue()) + BORDER);
					g2d.drawString("" + i, sx - spaceS, zeroy + spaceZeroy);
				}

				for (int i = -gridInterval; i <= gridInterval; i++) {
					sy = (int) (getSize().getHeight() - ky * (i - offset[1].doubleValue()) - BORDER);
					g2d.drawString("" + i, zerox - spaceZerox, sy + spaceS);
				}
			}

			for (INode<List<? extends ILsaMolecule>> nod : env.getNodes()) {
				LsaNode n = (LsaNode) nod;

				final Double[] pos = env.getPosition(nod).getCartesianCoordinates();
				final int x = (int) (kx * (pos[0] - offset[0].doubleValue()) + BORDER);
				final int y = (int) (getSize().getHeight() - ky * (pos[1] - offset[1].doubleValue()) - BORDER);

				if (n.getConcentration(isPerson).size() != 0 /*
															 * &&
															 * n.getConcentration
															 * (new
															 * LsaMolecule("poi"
															 * )).size() == 0
															 */) {
					g2d.setColor(Color.BLUE);
					g2d.fillOval(x - dimensionOval, y - dimensionOval, widthHeightOval, widthHeightOval);
				} else {
					final List<? extends INode<List<? extends ILsaMolecule>>> neighlist = env.getNeighborhood(nod).getNeighbors();

					for (INode<List<? extends ILsaMolecule>> neigh : neighlist) {
						LsaNode neigLSA = (LsaNode) neigh;
						if (n.getConcentration(new LsaMolecule("k")).size() != 0) {
							g2d.setColor(new Color(colorK1, colorK2, colorK3, colorK2));
							g2d.fillOval(x - dimensionK, y - dimensionK, widthHeightK, widthHeightK);
							g2d.setColor(new Color(1.0f, 1.0f, colorK2));
							g2d.fillOval(x - dimensionK2, y - dimensionK2, widthHeightK2, widthHeightK2);
						} else if (debugMode && (neigLSA.getConcentration(isPerson).size() == 0)) {
							g2d.setColor(Color.RED);
							g2d.fillOval(x - dimensionK2, y - dimensionK2, widthHeightK2, widthHeightK2);
						}
					}
				}
			}

			if (!debugMode) {
				g2d.setColor(new Color(0.0f, color, color));
				g2d.fillRect(0, 0, this.getWidth(), widthHeightK2);
				g2d.fillRect(0, 0, widthHeightK2, this.getHeight());
				g2d.fillRect(0, this.getHeight() - widthHeightK2, this.getWidth(), widthHeightK2);
				g2d.fillRect(this.getWidth() - width, 0, widthHeightK2, this.getHeight());
			}

			String text = "Time: " + time + " - Step: " + step;
			// g2d.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));
			g2d.setColor(new Color(0.0f, color, color));
			g2d.fillRect(0, 0, rectTimeWidth + text.length() * rectTimeWidth2, rectTimeHeight);
			g2d.setColor(Color.WHITE);
			g2d.drawString(text, dimensionK, dimensionK);
		}

		sim.play();
	}
}

