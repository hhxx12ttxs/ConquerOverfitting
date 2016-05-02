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
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;


/**
 * @author Francesca Cioffi
 * @version 20120508
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class LsaGradientsVariance extends JPanel implements OutputMonitor<Double, Double, List<? extends ILsaMolecule>> {
	private static final long serialVersionUID = 334013456374890632L;
	private InfiniteHalls<List<? extends ILsaMolecule>> env;
	private double time;
	private long step;
	private static final int BORDER = 30;
	private static final int STEP = 10;
	private static final int SIZE_FONT = 16;
	private static final int X_POS = 10;
	private static final int Y_POS = 10;
	private static final int KX = 3;
	private static final int KY = 2;
	private static final int I_MIN = -100;
	private static final int I_MAX = 100;
	private static final int NX = 10;
	private static final int NY = 5;
	private static final int POS_OVAL = 5;
	private static final int SIZE_OVAL = 20;
	private static final int SPACE = 4;
	private static final int VALUE_DISPLAYED = 3;
	
	private LsaMolecule sensorTarget = new LsaMolecule("6,V"); //("id,V");	
	
	private final ISimulation<Double, Double, List<? extends ILsaMolecule>> sim;
	private final ILsaMolecule isPerson = new LsaMolecule("p"); //("person");	
	
//	private final ILsaMolecule isSum = new LsaMolecule("2, X"); // new LsaMolecule("s, X");
	
	private final ILsaMolecule isSum = new LsaMolecule("0,10,Ts,V"); // new LsaMolecule("f,b,Ts,V");

	/**
	 * GUI which draws: the links between sensor nodes to create the network and
	 * nodes sources with points. The node elected to ascend the gradient of
	 * barycenter is colored red, the others are blue.
	 * 
	 * @param s
	 *            simulation
	 */
	
	public LsaGradientsVariance(final ISimulation<Double, Double, List<? extends ILsaMolecule>> s) {
		super();
		this.sim = s;
	}

	@Override
	public void stepDone(final IEnvironment<Double, Double, List<? extends ILsaMolecule>> aEnv, final IReaction<List<? extends ILsaMolecule>> r, final ITime aTime, final long aStep) {
		this.env =  (InfiniteHalls<List<? extends ILsaMolecule>>) aEnv;
		this.time = aTime.toDouble();
		this.step = aStep;
		
		if (aStep % STEP == 0) {
			repaint();
		}
	}

	@Override
	protected void paintComponent(final Graphics graphics) {
		sim.pause();
		super.setBackground(Color.WHITE);
		super.paintComponent(graphics);
		Font fontDefault = graphics.getFont();
		Font fontToken = new Font("Helvetica", Font.BOLD, SIZE_FONT);

		if (env.getDimensions() == 2) {
			graphics.setColor(Color.BLACK);
			graphics.drawString("Time: " + time + " - Step: " + step, X_POS, Y_POS);
			final Double[] size = env.getSize();
			final Double[] offset = env.getOffset();
			final double kx = (getSize().getWidth() - KX * BORDER)
					/ size[0].doubleValue();
			final double ky = (getSize().getHeight() - KY * BORDER)
					/ size[1].doubleValue();
			final int zerox = (int) (kx * (-offset[0].doubleValue()) + BORDER);
			final int zeroy = (int) (getSize().getHeight() - ky
					* (-offset[1].doubleValue()) - BORDER);
			graphics.drawLine(zerox - (int) getSize().getWidth(), zeroy, zerox
					+ (int) getSize().getWidth(), zeroy);
			graphics.drawLine(zerox, zeroy - (int) getSize().getHeight(),
					zerox, zeroy + (int) getSize().getHeight());
			for (int i = I_MIN; i <= I_MAX; i++) {
				final int x = (int) (kx * (i - offset[0].doubleValue()) + BORDER);
				graphics.drawString("" + i, x, zeroy + NX);
			}
			for (int i = I_MIN; i <= I_MAX; i++) {
				final int y = (int) (getSize().getHeight() - ky
						* (i - offset[1].doubleValue()) - BORDER);
				graphics.drawString("" + i, zerox, y - NY);
			}
			for (INode<List<? extends ILsaMolecule>> nod : env.getNodes()) {
				LsaNode n = (LsaNode) nod;
				
				final Double[] pos = env.getPosition(nod)
						.getCartesianCoordinates();
				final int x = (int) (kx * (pos[0] - offset[0].doubleValue()) + BORDER);
				final int y = (int) (getSize().getHeight() - ky
						* (pos[1] - offset[1].doubleValue()) - BORDER);
				
				if (n.getConcentration(isPerson).size() != 0 && n.getConcentration(new LsaMolecule("poi")).size() == 0) {
						graphics.setColor(Color.BLUE);
					graphics.fillOval(x - POS_OVAL, y - POS_OVAL, SIZE_OVAL, SIZE_OVAL);
				} else if (n.getConcentration(isPerson).size() == 0)  {
					final List<? extends INode<List<? extends ILsaMolecule>>> neighlist = env.getNeighborhood(nod).getNeighbors();
					graphics.setColor(Color.BLACK);
					for (INode<List<? extends ILsaMolecule>> neigh : neighlist) {
						LsaNode neigLSA = (LsaNode) neigh;
						if ((neigLSA.getConcentration(isPerson).size() == 0)) {
							Double[] npos = env.getPosition(neigh).getCartesianCoordinates();
							final int nx = (int) (kx * (npos[0] - offset[0].doubleValue()) + BORDER);
							final int ny = (int) (getSize().getHeight() - ky * (npos[1] - offset[1].doubleValue()) - BORDER);
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
										// overwrite the old value with the same number in white
										graphics.setColor(Color.WHITE);
										graphics.drawString(stamp[i - 1], x - 2 * (sensors.size() - 1) + i * SPACE, (int) (y));
									}
									// write the new value
									graphics.setColor(Color.BLACK);		
//									s = n.getConcentration(isSum).get(0).getArg(1).toString(); //sum
									
									s = n.getConcentration(isSum).get(0).getArg(VALUE_DISPLAYED).toString(); //field token
									
									if (n.getConcentration(new LsaMolecule("k")).size() != 0) { //("token")).size() != 0) {
										//s = s + " - " + n.getConcentration(new LsaMolecule("token")).get(0).getArg(0).toString(); //token
										s = "T: " + s;
										graphics.setColor(Color.RED);
										graphics.setFont(fontToken);
										
									}
									graphics.drawString(s, x - 2 * (sensors.size() - 1) + i * SPACE, (int) (y));
									
									graphics.setColor(Color.BLACK);
									graphics.setFont(fontDefault);
									
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
