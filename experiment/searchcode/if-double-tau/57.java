/* ANTPANEL */
/* Part of the AntTSP project */
/* Dr Mark C. Sinclair, NPIC, Cambodia, v1.0, November 2006, mcs@ieee.org */
/* You may make use of this code for any purpose, but if you use it, */
/* you must cite the use: Sinclair, M.C., AntTSP Java Applet v1.0, */
/* http://uk.geocities.com/markcsinclair/aco.html, November, 2006 */

import java.awt.*;
import java.util.*;

class AntPanel extends Panel implements Observer {
	public AntPanel(Network net) {
		if (net == null) throw new NullPointerException("net is null");
		Contract.require("net is complete", net.isComplete());
		Contract.require("WID > 2*NodeView.RAD", WID > 2*NodeView.RAD);
		Contract.require("HGT > 2*NodeView.RAD", HGT > 2*NodeView.RAD);

		setBackground(Color.white);

		this.net = net;
		tauScale = new double[TAUSTEPS];

		// calculate scaleRatio
		double minX = net.getMinX();
		double maxX = net.getMaxX();
		double minY = net.getMinY();
		double maxY = net.getMaxY();
		double srx  = 1.0;
		double sry  = 1.0;

		srx /= (maxX - minX);
		srx *= (WID - 2*NodeView.RAD);
		sry /= (maxY - minY);
		sry *= (HGT - 2*NodeView.RAD);

		// take smallest
		if (srx < sry) {
			scaleRatio = srx;
			wid        = WID;
			double h   = Math.rint((HGT - 2*NodeView.RAD)*srx/sry);
			hgt        = (int) h + 2*NodeView.RAD;
		} else {
			scaleRatio = sry;
			double w   = Math.rint((WID - 2*NodeView.RAD)*sry/srx);
			wid        = (int) w + 2*NodeView.RAD;
			hgt        = HGT;
		}
		minSize = new Dimension(wid, hgt);

		// range of colours for Links
		tauColors    = new Color[TAUSTEPS];
		tauColors[0] = Color.white;
		tauColors[1] = Color.darkGray;
		tauColors[2] = Color.black;
		tauColors[3] = Color.blue;
		tauColors[4] = Color.cyan;
		tauColors[5] = Color.green;
		tauColors[6] = Color.yellow;
		tauColors[7] = Color.orange;
		tauColors[8] = new Color(255, 100, 0); //orange-red
		tauColors[9] = Color.red;

		// scale taus
		scaleTaus();

		// deactive the LayoutManager
		setLayout(null);

		// add NodeViews
		int nn = net.numNodes();
		int nodeId;
		for (nodeId=0; nodeId<nn; nodeId++) {
			Node      n  = net.getNode(nodeId);
			NodeView  nv = new NodeView(n, this);
			add(nv);
			nv.setBounds(new Rectangle(nv.getX(),
					nv.getY(),
					2*NodeView.RAD,
					2*NodeView.RAD));
		}

		// start observing net and Nodes
		net.addObserver(this);
		for (nodeId=0; nodeId<nn; nodeId++)
			net.getNode(nodeId).addObserver(this);
	}

	public void paint(Graphics g) {
		// Draw Links
		int nn = net.numNodes();
		for (int nodeIdS=0; nodeIdS<nn; nodeIdS++) {
			Node nodeS = net.getNode(nodeIdS);
			int  xs    = scaleX(nodeS.getX());
			int  ys    = scaleY(nodeS.getY());
			for (int nodeIdT=nodeIdS+1; nodeIdT<nn; nodeIdT++) {
				Node   nodeT = net.getNode(nodeIdT);
				int    xt    = scaleX(nodeT.getX());
				int    yt    = scaleY(nodeT.getY());
				double tau   = net.getLink(nodeIdS, nodeIdT).getTau();
				Color  gc    = g.getColor();
				g.setColor(tauColor(tau));
				if (g.getColor() != getBackground())
					g.drawLine(xs, ys, xt, yt);
				g.setColor(gc);
			}
		}
	}

	public Color tauColor(double tau) {
		Contract.require("tau >= 0.0", tau >= 0.0);
		int color;
		for (color=0; color<TAUSTEPS-1; color++)
			if (tau <= tauScale[color])
				break;
		return(tauColors[color]);
	}

	public final Dimension getPreferredSize() {
		return(new Dimension(minSize));
	}

	public final Dimension getMinimumSize() {
		return(new Dimension(minSize));
	}

	public void update(Observable o, Object arg) {
		if (o instanceof Network)
			scaleTaus();
		repaint();
	}

	public int scaleX(double x) {
		double minX = net.getMinX();
		double maxX = net.getMaxX();
		Contract.require("minX <= x <= maxX", ((x >= minX) && (x <= maxX)));
		x -= minX;
		x *= scaleRatio;
		x += NodeView.RAD;
		Contract.ensure("RAD <= x <= wid-RAD",
				(((int) x >= NodeView.RAD) &&
						((int) x <= wid-NodeView.RAD)));
		return((int) x);
	}

	public int scaleY(double y) {
		double minY = net.getMinY();
		double maxY = net.getMaxY();
		Contract.require("minY <= y <= maxY", ((y >= minY) && (y <= maxY)));
		y -= minY;
		y *= scaleRatio;
		y += NodeView.RAD;
		Contract.ensure("RAD <= y <= hgt-RAD",
				(((int) y >= NodeView.RAD) &&
						((int) y <= hgt-NodeView.RAD)));
		return((int) y);
	}

	public double invScaleX(int x) {
		double minX = net.getMinX();
		double maxX = net.getMaxX();
		Contract.require("RAD <= x <= wid-RAD",
				((x >= NodeView.RAD) && (x <= wid-NodeView.RAD)));
		double dx = (double) x;
		dx -= NodeView.RAD;
		dx /= scaleRatio;
		dx += minX;
		Contract.ensure("minX <= dx <= maxX", ((dx >= minX) && (dx <= maxX)));
		return(dx);
	}

	public final double invScaleY(int y) {
		double minY = net.getMinY();
		double maxY = net.getMaxY();
		Contract.require("RAD <= y <= hgt-RAD",
				((y >= NodeView.RAD) && (y <= hgt-NodeView.RAD)));
		double dy = (double) y;
		dy -= NodeView.RAD;
		dy /= scaleRatio;
		dy += minY;
		Contract.require("minY <= dy <= maxY", ((dy >= minY) && (dy <= maxY)));
		return(dy);
	}

	private void scaleTaus() {
		// Establish scaling for Link taus
		double minTau = Double.POSITIVE_INFINITY;
		double maxTau = 0.0;
		int    nn     = net.numNodes();
		for (int nodeIdS=0; nodeIdS<nn; nodeIdS++) {
			for (int nodeIdT=nodeIdS+1; nodeIdT<nn; nodeIdT++) {
				double tau = net.getLink(nodeIdS, nodeIdT).getTau();
				if (tau < minTau)
					minTau = tau;
				else if (tau > maxTau)
					maxTau = tau;
			}
		}
		double step = (maxTau-minTau)/TAUSTEPS;
		double base = maxTau - (TAUSTEPS-1)*step;
		for (int i=TAUSTEPS-1; i>=0; i--)
			tauScale[i] = base + i*step;
	}

	private final int TAUSTEPS = 10;
	private final int WID      = 400;
	private final int HGT      = 300;

	private Network         net;
	private final Dimension minSize;
	private final double    scaleRatio;
	public  final int       wid;
	public  final int       hgt;
	private Color[]         tauColors;
	private double[]        tauScale;

	private static final long serialVersionUID = 1L;
}

