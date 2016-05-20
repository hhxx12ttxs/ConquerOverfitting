/*******************************************************************************
 * Copyright (c) 2012 Danilo Pianini.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.exceptions.UncomparableDistancesException;
import it.unibo.alchemist.model.implementations.environments.InfiniteHalls;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.IMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IPosition;
import it.unibo.alchemist.model.interfaces.IReaction;
import it.unibo.alchemist.model.interfaces.ITime;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JPanel;


/**
 * @author Danilo Pianini
 * @version 20111107
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class InfiniteHallsListDoubleDisplay extends JPanel implements OutputMonitor<Double, Double, List<Double>> {

	private static final long serialVersionUID = 334013456374890632L;
	private InfiniteHalls<List<Double>> env;
	private final float colorStep;
	private double time;
	private long step = 0;
	private final int border = 30, intensity;
	private final IMolecule[] toDisplay, sources;
	private final IMolecule isSensor;

	public boolean isDrawvect() {
		return drawvect;
	}

	public void setDrawvect(boolean drawvect) {
		this.drawvect = drawvect;
	}

	public boolean isDrawgrad() {
		return drawgrad;
	}

	public void setDrawgrad(boolean drawgrad) {
		this.drawgrad = drawgrad;
	}

	private boolean drawvect = false, drawgrad = false;
	double kx, ky;

	public InfiniteHallsListDoubleDisplay(IMolecule[] sources, IMolecule[] toDisplay, IMolecule isSensor, int si) {
		super();
		this.intensity = si * 2;
		this.toDisplay = toDisplay;
		this.sources = sources;
		this.isSensor = isSensor;
		colorStep = 1f / toDisplay.length;
	}

	@Override
	public void stepDone(IEnvironment<Double, Double, List<Double>> env, final IReaction<List<Double>> r, ITime time, long step) {
		this.env = (InfiniteHalls<List<Double>>) env;
		this.time = time.toDouble();
		this.step = step;
		if (step % 10000 == 0)
			repaint();
		// if(this.time>timestep){
		// timestep+=1d;
		// String desktopPath = System.getProperty("user.home") + "/Desktop";
		// try {
		// GUIUtilities.saveComponentAsImage(desktopPath+"/alchemist/"+time+".png",
		// this, "png");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
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
			((Graphics2D) g).setStroke(new BasicStroke(2));
			final double radius = calculateRadius();
			final int radx = translateX(radius, 0);
			final int rady = getHeight() - translateY(radius, 0);
			if (drawgrad && toDisplay.length == 3) {
				for (INode<List<Double>> n : env.getNodes()) {
					if (n.getConcentration(isSensor) != null) {
						final IPosition<Double, Double> mypos = env.getPosition(n);
						final Double[] pos = mypos.getCartesianCoordinates();
						final int x = translateX(pos[0], 0);
						final int y = translateY(pos[1], 0);
						final float[] rgb = nodeToRGB(n);
						final Color c1 = new Color(rgb[0], rgb[1], rgb[2]);
						g.setColor(c1);
						g.fillRect(x - radx / 2, y - rady / 2, radx, rady);
					}
				}
			}
			if (drawvect && toDisplay.length == 3) {
				for (INode<List<Double>> n : env.getNodes()) {
					if (n.getConcentration(isSensor) != null) {
						final Double[] pos = env.getPosition(n).getCartesianCoordinates();
						final int x = translateX(pos[0], 0);
						final int y = translateY(pos[1], 0);
						final float[] rgb = nodeToRGB(n);
						final Color c1 = new Color(rgb[0], rgb[1], rgb[2]);
						for (int i = 0; i < toDisplay.length; i++) {
							double max = Double.MIN_NORMAL;
							INode<List<Double>> neigh = null;
							for (INode<List<Double>> ne : env.getNeighborhood(n).getNeighbors()) {
								if (ne.getConcentration(isSensor) != null) {
									if (ne.getConcentration(toDisplay[i]) != null) {
										double nv = ne.getConcentration(toDisplay[i]).get(1);
										if (nv > max) {
											neigh = ne;
											max = nv;
										}
									}
								}
							}
							if (neigh != null) {
								// final float[] nrgb = nodeToRGB(neigh);
								final Double[] neighpos = env.getPosition(neigh).getCartesianCoordinates();
								int nx = translateX(neighpos[0], 0);
								nx += (x - nx) / 3;
								int ny = translateY(neighpos[1], 0) - rady / 3;
								ny += (y - ny) / 3;
								// final GradientPaint grad = new
								// GradientPaint(x, y, c1, nx, ny, c2);
								// Graphics2D g2d = (Graphics2D)g;
								// g2d.setPaint(grad);
								g.setColor(c1);
								g.drawLine(x, y, nx, ny);
								// g.setColor(c2);
								g.fillOval(nx - 3, ny - 3, 6, 6);
							}
						}
					}
				}
			}
			g.setColor(Color.BLACK);
			for (int j = 0; j < ms; j++) {
				for (int i = 0; i < ns; i++) {
					g.fillRect(translateX(i * s, 0), translateY(j * s + si, 0), (int) (ci * kx), (int) (si * ky));
					g.fillRect(translateX(i * s + cf, 0), translateY(j * s + si, 0), (int) (ci * kx), (int) (si * ky));
					g.fillRect(translateX(i * s + sf, 0), translateY(j * s + ci, 0), (int) (si * kx), (int) (ci * ky));
					g.fillRect(translateX(i * s + sf, 0), translateY(j * s + ex, 0), (int) (si * kx), (int) (env.isDoorsOpen() ? ci * ky : ci * ky * 2));
					g.fillRect(translateX(i * s + cf, 0), translateY(j * s + ex, 0), (int) (ci * kx), (int) (si * ky));
					g.fillRect(translateX(i * s, 0), translateY(j * s + ex, 0), (int) (ci * kx), (int) (si * ky));
					g.fillRect(translateX(i * s, 0), translateY(j * s + ex, 0), (int) (si * kx), (int) (env.isDoorsOpen() ? ci * ky : ci * ky * 2));
					g.fillRect(translateX(i * s, 0), translateY(j * s + ci, 0), (int) (si * kx), (int) (ci * ky));
				}
			}
			for (INode<List<Double>> n : env.getNodes()) {
				final Double[] pos = env.getPosition(n).getCartesianCoordinates();
				final int x = translateX(pos[0], 0);
				final int y = translateY(pos[1], 0);
				if (n.getConcentration(isSensor) == null) {
					float hue = 0f;
					for (int i = 0; n.getConcentration(toDisplay[i]) == null; i++) {
						hue += colorStep;
					}
					g.setColor(Color.getHSBColor(hue, 1.0f, 1.0f));
					g.fillOval(x - 10, y - 10, 20, 20);
					g.setColor(Color.BLACK);
					g.drawOval(x - 10, y - 10, 20, 20);
				} else {
					float hue = 0f;
					for (IMolecule m : sources) {
						if (n.getConcentration(m) != null) {
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

	public int translateY(double pos, double offset) {
		return (int) (getSize().getHeight() - ky * (pos - offset) - border);
	}

	public int translateX(double pos, double offset) {
		return (int) (kx * (pos - offset) + border);
	}

	public double translateInvY(double y, double offset) {
		return offset + (getSize().getHeight() - y - border) / ky;
	}

	public double translateInvX(double x, double offset) {
		return offset + (x - border) / kx;
	}

	private final float[] nodeToRGB(INode<List<Double>> n) {
		final float[] rgb = { 1f, 1f, 1f };
		for (int i = 0; i < toDisplay.length; i++) {
			if (n.getConcentration(toDisplay[i]) != null) {
				final float myval = n.getConcentration(toDisplay[i]).get(1).floatValue();
				for (int j = 0; j < rgb.length; j++) {
					if (j != i) {
						rgb[j] -= myval / intensity;
					}
				}
			}
		}
		return rgb;
	}

	private final double calculateRadius() {
		double result = Double.MAX_VALUE;
		external: for (INode<List<Double>> n : env.getNodes()) {
			if (n.getConcentration(isSensor) != null) {
				final IPosition<Double, Double> mypos = env.getPosition(n);
				final Double[] pos = mypos.getCartesianCoordinates();
				final int x = translateX(pos[0], 0);
				final int y = translateY(pos[1], 0);
				for (INode<List<Double>> neigh : env.getNeighborhood(n).getNeighbors()) {
					if (neigh.getConcentration(isSensor) != null) {
						final IPosition<Double, Double> npos = env.getPosition(neigh);
						final Double[] neighpos = npos.getCartesianCoordinates();
						final int nx = translateX(neighpos[0], 0);
						final int ny = translateY(neighpos[1], 0);
						try {
							final double dist = npos.getDistanceTo(mypos);
							if ((nx == x || ny == y) && dist < result) {
								result = dist;
								break external;
							}
						} catch (UncomparableDistancesException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return result;
	}
}

