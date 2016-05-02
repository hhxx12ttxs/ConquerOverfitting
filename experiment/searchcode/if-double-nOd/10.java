package it.unibo.alchemist.examples;

/*******************************************************************************
 * Copyright (c) 2012 Danilo Pianini.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
/**
 * 
 */

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
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
 * @author Giacomo Pronti
 * @version 20110727
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class LsaGradientsDisplay<T> extends JPanel implements OutputMonitor<Double, Double, T> {
	private final int VAL_SCALE = 1; // valore per cui vengono divisi i value
										// field da graficare
	private static final long serialVersionUID = 334013456374890632L;
	private InfiniteHalls<T> env;
	private double time;
	private long step;
	private final int border = 30;
	private final ILsaMolecule[] sensor_target; // qui dentro avr??:
												// <field,targ1,N,Type,T> , ...
												// <field,targn,N,Type,T>

	// private final ILsaMolecule isPerson;

	public LsaGradientsDisplay(final ILsaMolecule[] fields, final ILsaMolecule[] person_target, final ILsaMolecule isPerson) {
		super();
		this.sensor_target = fields;
	}

	@Override
	public void stepDone(final IEnvironment<Double, Double, T> env, final IReaction<T> r, final ITime time, final long step) {
		this.env = (InfiniteHalls<T>) env;
		this.time = time.toDouble();
		this.step = step;
		// if(step%1000==0){
		repaint();
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected synchronized void paintComponent(final Graphics g) {
		// long time=System.currentTimeMillis();
		super.setBackground(Color.WHITE);
		super.paintComponent(g);
		if (env.getDimensions() == 2) {
			g.setColor(Color.BLACK);
			g.drawString("Time: " + time + " - Step: " + step, 10, 10);
			final Double[] size = env.getSize();
			final Double[] offset = env.getOffset();
			final double kx = (getSize().getWidth() - 2 * border) / size[0].doubleValue();
			final double ky = (getSize().getHeight() - 2 * border) / size[1].doubleValue();
			final int zerox = (int) (kx * (-offset[0].doubleValue()) + border);
			final int zeroy = (int) (getSize().getHeight() - ky * (-offset[1].doubleValue()) - border);
			g.drawLine(zerox - (int) getSize().getWidth(), zeroy, zerox + (int) getSize().getWidth(), zeroy);
			g.drawLine(zerox, zeroy - (int) getSize().getHeight(), zerox, zeroy + (int) getSize().getHeight());
			for (int i = -100; i <= 100; i++) {
				final int x = (int) (kx * (i - offset[0].doubleValue()) + border);
				g.drawString("" + i, x, zeroy + 10);
			}
			for (int i = -100; i <= 100; i++) {
				final int y = (int) (getSize().getHeight() - ky * (i - offset[1].doubleValue()) - border);
				g.drawString("" + i, zerox, y - 5);
			}
			for (INode<T> nod : env.getNodes()) {
				LsaNode n = (LsaNode) nod;
				String st = "";
				if (n.getId() == 40) { // 45 || n.getId()==175){
					ILsaMolecule mol = sensor_target[0];
					List<ILsaMolecule> c = n.getConcentration(mol);
					for (int i = 0; i < c.size(); i++) {
						double val;
						val = (Double) (c.get(i).getArg(2).calculate(null).getValue(null));
						st += val + ";";
					}
					System.out.println("" + n.getId() + "- tempo:" + time + "=> " + st);

				}

				Double[] pos = env.getPosition(nod).getCartesianCoordinates();
				final int x = (int) (kx * (pos[0] - offset[0].doubleValue()) + border);
				final int y = (int) (getSize().getHeight() - ky * (pos[1] - offset[1].doubleValue()) - border);
				List<? extends INode<T>> neighlist = env.getNeighborhood(nod).getNeighbors();
				g.setColor(Color.BLACK);
				for (INode<T> neigh : neighlist) { // TODO Concurrent Exception
					Double[] npos = env.getPosition(neigh).getCartesianCoordinates();
					final int nx = (int) (kx * (npos[0] - offset[0].doubleValue()) + border);
					final int ny = (int) (getSize().getHeight() - ky * (npos[1] - offset[1].doubleValue()) - border);
					g.drawLine(x, y, nx, ny);

				}
				final int num = sensor_target.length;
				for (int i = 0; i < sensor_target.length; i++) {
					ILsaMolecule mol = sensor_target[i];
					List<ILsaMolecule> c = n.getConcentration(mol);
					if (c.size() != 0) {
						double val;
						val = (Double) (c.get(0).getArg(2).calculate(null).getValue(null)) / VAL_SCALE;
						String s = "";
						if (n.getConcentration(new LsaMolecule("source,X,Y")).size() != 0) {
							g.setColor(Color.BLUE);
							s = "S";
						} else if (val >= Integer.MAX_VALUE) {
							g.setColor(Color.getHSBColor(1f, 1f, 0));
							s = "inf";
						} else {
							g.setColor(new Color(255, 0, 0, 255));
							g.fillRect(x - 2 - 2 * (num - 1) + i * 4, (int) (y - val), 3, (int) val);
							s = Double.toString(val * VAL_SCALE);
						}
						g.drawString(s, x - 2 * (num - 1) + i * 4, (int) (y));
					}
				}
			}
		}
	}
	/*
	 * } else { g.setColor(Color.BLACK);
	 * g.drawString("Unable to draw such environment",
	 * (int)getSize().getWidth()/2, (int)getSize().getHeight()/2); }
	 */

}

