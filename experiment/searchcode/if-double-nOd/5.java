/*******************************************************************************
 * Copyright (c) 2012 Danilo Pianini.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.model.implementations.environments.InfiniteHalls;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IReaction;
import it.unibo.alchemist.model.interfaces.ITime;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 * @author Giacomo Pronti
 * @version 20111124
 * 
 *          Display related to LsaMASS2 model
 */
//CHECKSTYLE:OFF
@Deprecated
public class LsaInfiniteHallsDisplay2<T> extends JPanel implements OutputMonitor<Double, Double, T> {

	private static final long serialVersionUID = 334013456374890632L;
	private InfiniteHalls<T> env;
	private final float colorStep;
	private double time;
	private long step = 0;
	private final int border = 30;
	private final ILsaMolecule[] person_target, sources;
	// private final IMolecule isSensor;
	double timestep = 0;
	double kx, ky;

	public LsaInfiniteHallsDisplay2(final ILsaMolecule[] sources, final ILsaMolecule[] personTarget, final ILsaMolecule isSensor) {
		super();
		this.sources = sources;
		this.person_target = personTarget;
		// this.isSensor = isSensor;
		colorStep = 1f / person_target.length;

	}

	@Override
	public void stepDone(final IEnvironment<Double, Double, T> env, final IReaction<T> r, final ITime time, final long step) {
		this.env = (InfiniteHalls<T>) env;
		this.time = time.toDouble();
		this.step = step;

		if (step % 50 == 0) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						repaint();
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			/*
			 * if(this.time>timestep){ timestep+=1d; String desktopPath =
			 * System.getProperty("user.home") + "/Desktop"; try {
			 * GUIUtilities.saveComponentAsImage
			 * (desktopPath+"/mass/"+time+".png", this, "png"); } catch
			 * (IOException e) { e.printStackTrace(); } }
			 */
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected synchronized void paintComponent(final Graphics g) {
		super.setBackground(Color.WHITE);
		super.paintComponent(g);
		if (env.getDimensions() == 2) {
			g.setColor(Color.BLACK);
			g.drawString("Time: " + time + " - Step: " + step, 10, 10);
			final Double[] size = env.getSize();
			kx = (getSize().getWidth() - 2 * border) / size[0].doubleValue();
			ky = (getSize().getHeight() - 2 * border) / size[1].doubleValue();
			double s = env.getHallSize();
			double ex = env.getEx();
			double ci = env.getCi();
			double cf = env.getCf();
			double si = env.getSi();
			double sf = env.getSf();
			final int ns = (int) (env.getSize()[0] / s + 1);
			final int ms = (int) (env.getSize()[1] / s + 1);
			for (int j = 0; j < ms; j++) {
				for (int i = 0; i < ns; i++) {
					g.fillRect(translateX(i * s, 0), translateY(j * s + si, 0), (int) (ci * kx), (int) (si * ky));
					g.fillRect(translateX(i * s + cf, 0), translateY(j * s + si, 0), (int) (ci * kx), (int) (si * ky));
					g.fillRect(translateX(i * s + sf, 0), translateY(j * s + ci, 0), (int) (si * kx), (int) (ci * ky));
					g.fillRect(translateX(i * s + sf, 0), translateY(j * s + ex, 0), (int) (si * kx), (int) (ci * ky));
					g.fillRect(translateX(i * s + cf, 0), translateY(j * s + ex, 0), (int) (ci * kx), (int) (si * ky));
					g.fillRect(translateX(i * s, 0), translateY(j * s + ex, 0), (int) (ci * kx), (int) (si * ky));
					g.fillRect(translateX(i * s, 0), translateY(j * s + ex, 0), (int) (si * kx), (int) (ci * ky));
					g.fillRect(translateX(i * s, 0), translateY(j * s + ci, 0), (int) (si * kx), (int) (ci * ky));
				}
			}
			((Graphics2D) g).setStroke(new BasicStroke(2));
			for (INode<T> n : env.getNodes()) {
				Double[] pos = env.getPosition(n).getCartesianCoordinates();
				final int x = translateX(pos[0], 0);
				final int y = translateY(pos[1], 0);
				LsaNode nod = (LsaNode) n; // quando voglio usarlo come LsaNode
											// uso nod

				if (nod.getConcentration(new LsaMolecule("field,Type")).size() != 0) { // caso
																						// persone
					float hue = 0f;
					for (int i = 0; i < person_target.length && nod.getConcentration(person_target[i]).size() == 0; i++) { // person
																															// color
						hue += colorStep;
					}
					g.setColor(Color.getHSBColor(hue, 1.0f, 1.0f));
					g.fillOval(x - 10, y - 10, 20, 20);
					g.setColor(Color.BLACK);
					g.drawOval(x - 10, y - 10, 20, 20);
				} else { // caso sensori sorgenti (i sensori non sorgenti non
							// vengono graficati)
					float hue = 0f;

					for (ILsaMolecule m : sources) {
						if (nod.getConcentration(m).size() != 0) {
							g.setColor(Color.getHSBColor(hue, 1.0f, 1.0f));
							g.fillRoundRect(x - 10, y - 10, 20, 20, 10, 10);
							g.setColor(Color.BLACK);
							g.drawRoundRect(x - 10, y - 10, 20, 20, 10, 10);
						}
						hue += colorStep;
					}
				}
			}
		} else {
			g.setColor(Color.BLACK);
			g.drawString("Unable to draw such environment", (int) getSize().getWidth() / 2, (int) getSize().getHeight() / 2);
		}
	}

	public int translateY(final double pos, final double offset) {
		return (int) (getSize().getHeight() - ky * (pos - offset) - border);
	}

	public int translateX(final double pos, final double offset) {
		return (int) (kx * (pos - offset) + border);
	}

	public double translateInvY(final double y, final double offset) {
		return offset + (getSize().getHeight() - y - border) / ky;
	}

	public double translateInvX(final double x, final double offset) {
		return offset + (x - border) / kx;
	}

}

