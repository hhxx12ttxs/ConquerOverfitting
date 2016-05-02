<<<<<<< HEAD
/*
 * @(#)SimplexTabular.java 1.0 12/10/2009
 * @charset "utf-8";
 * Copyright (c) pendiente.
 * 
 */
package mx.uacam.fdi.io.simplex.resolvedor;

import java.util.StringTokenizer;
import org.neocs.mate.fracciones.Fraccion;
import mx.uacam.fdi.io.simplex.resolvedor.mate.Ecuacion;
import mx.uacam.fdi.io.simplex.resolvedor.mate.Monomio;
import mx.uacam.fdi.io.simplex.resolvedor.mate.RationalNumber;

/**
 *
 * @author Neo Cs
 * @version 1.0, 12/10/2009
 */
public class SimplexTabular implements Simplex {

    /**indica en que paso estamos del m?todo */
    private int pasos;
    /**indica la cantidad de veces que se ha iterado*/
    private int ciclo;
    /**define cantidad de restricciones*/
    private int m;
    /**define numero de variables basicas*/
    private int n;
    private int r;
    private int s;
    private RationalNumber[][] a = new RationalNumber[10][20];
    int[] base = new int[10];

    @Override
    public double[] maximizar(Ecuacion fo, Ecuacion[] restricciones) {
        for (int i = 0; i < fo.getMonomios().length; i++) {
            fo.getMonomio(i).setCoeficiente(-1 * fo.getMonomio(i).getCoeficiente());
        }
        
        return minimizar(fo,restricciones);
    }

    @Override
    public double[] minimizar(Ecuacion fo, Ecuacion[] restricciones) {
        m = restricciones.length;
        n = fo.getMonomios().length - 1;
        String sdat = "";

        for (int i = 0; i < m; i++) {
            Monomio[] monomios = restricciones[i].getMonomios();
            for (int j = 0; j < n; j++) {
                double d = monomios[j].getCoeficiente();
                String cadena = Fraccion.valueOf(d).toString();
                sdat += cadena + ',';
            }
            sdat += restricciones[i].getResultado() + ",";
        }

        Monomio[] monomios = fo.getMonomios();
        for (int j = 0; j < n; j++) {
            double d = Double.valueOf(monomios[j].getCoeficiente());
            String cadena = Fraccion.valueOf(d).toString();
            sdat += cadena + ',';
        }

        sdat += fo.getResultado();

        StringTokenizer st = new StringTokenizer(sdat, ",");
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = new RationalNumber(st.nextToken());
            }
            base[i] = n + i;
            for (int j = n; j < n + m; j++) {
                RationalNumber rn = new RationalNumber(0);
                if (j == i + n) {
                    rn.set(1);
                }
                a[i][j] = rn;
            }
            a[i][n + m] = new RationalNumber(st.nextToken());
        }
        n += m;

        boolean esOptimal = false;
        boolean esResolvible = false;
        do {
            switch (pasos) {
                case 0:
                    esOptimal = pasos1();
                    if(!esOptimal){
                        System.out.println("varible salidad x" + (s + 1));
                    }
                    break;
                case 1:
                    esResolvible = paso2();
                    if(!esResolvible){
                        System.out.println("variable entrante x" + (base[r] + 1));
                    }
                    break;
                case 2:
                    paso3();
                    break;
                case 3:
                    paso4();
                    ciclo++;
                    break;
                default:
                    break;
            }
            pasos++;
            pasos %= 4;
        } while (!esOptimal && !esResolvible);

        double[] d = new double[m + 1];

        for (int i = 0; i <= m; i++) {
            d[i] = (double) a[i][n].numerator / (double) a[i][n].denominator;
        }

        return d;
    }

    /** search pivot s of (r, s) */
    private boolean pasos1() {
        RationalNumber c = new RationalNumber();

        s = 0;
        r = -1;
        c.set(a[m][s]);
        for (int j = 1; j < n; j++) {
            if (c.gt(a[m][j])) {
                s = j;
                c.set(a[m][s]);
            }
        }
        if (c.numerator >= 0) {
            s = -1;
            return true;
        } else {
            return false;
        }
    }

    /* search pivot r of (r, s) */
    private boolean paso2() {
        RationalNumber t = new RationalNumber();
        RationalNumber c = new RationalNumber();

        for (int i = 0; i < m; i++) {
            if (a[i][s].numerator <= 0) {
                continue;
            }
            t.set(a[i][n]);
            t.div(a[i][s]);
            if (r < 0 || t.lt(c)) {
                r = i;
                c.set(t);
            }
        }
        if (r < 0) {
            return true;
        } else {
            return false;
        }
    }

    /* pivote */
    private void paso3() {
        RationalNumber c = new RationalNumber();

        base[r] = s;
        c.set(a[r][s]);
        for (int j = 0; j <= n; j++) {
            a[r][j].div(c);
        }
        
        for (int i = 0; i < a.length; i++) {
            RationalNumber[] rationalNumbers = a[i];
            for (RationalNumber rationalNumber : rationalNumbers) {
                System.out.print(rationalNumber + "\t");
            }
            System.out.print('\n');
        }
    }

    private void paso4() {
        RationalNumber c = new RationalNumber();
        RationalNumber t = new RationalNumber();

        for (int i = 0; i <= m; i++) {
            if (i == r) {
                continue;
            }
            c.set(a[i][s]);
            for (int j = 0; j <= n; j++) {
                t.set(c);
                t.mul(a[r][j]);
                a[i][j].minus(t);
                System.out.print(a[i][j] + "\t");
            }
            System.out.print("\n");
        }
        r = s = -1;
    }
}
=======
/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.gef.editpolicies;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.AutomaticRouter;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.handles.BendpointCreationHandle;
import org.eclipse.gef.handles.BendpointMoveHandle;
import org.eclipse.gef.requests.BendpointRequest;

/**
 * Used to add bendpoint handles on a {@link ConnectionEditPart}.
 * <P>
 * BendpointEditPolicy will automatically observe the
 * {@link org.eclipse.draw2d.Connection} figure. If the number of bends in the
 * <code>Connection</code> changes, the handles will be updated.
 */
public abstract class BendpointEditPolicy extends SelectionHandlesEditPolicy
		implements PropertyChangeListener {

	private static final List NULL_CONSTRAINT = new ArrayList();

	private List originalConstraint;
	private boolean isDeleting = false;

	private static final Point ref1 = new Point();
	private static final Point ref2 = new Point();

	/**
	 * <code>activate()</code> is extended to add a listener to the
	 * <code>Connection</code> figure.
	 * 
	 * @see org.eclipse.gef.EditPolicy#activate()
	 */
	public void activate() {
		super.activate();
		getConnection().addPropertyChangeListener(Connection.PROPERTY_POINTS,
				this);
	}

	private List createHandlesForAutomaticBendpoints() {
		List list = new ArrayList();
		ConnectionEditPart connEP = (ConnectionEditPart) getHost();
		PointList points = getConnection().getPoints();
		for (int i = 0; i < points.size() - 1; i++)
			list.add(new BendpointCreationHandle(connEP, 0, i));

		return list;
	}

	private List createHandlesForUserBendpoints() {
		List list = new ArrayList();
		ConnectionEditPart connEP = (ConnectionEditPart) getHost();
		PointList points = getConnection().getPoints();
		List bendPoints = (List) getConnection().getRoutingConstraint();
		int bendPointIndex = 0;
		Point currBendPoint = null;

		if (bendPoints == null)
			bendPoints = NULL_CONSTRAINT;
		else if (!bendPoints.isEmpty())
			currBendPoint = ((Bendpoint) bendPoints.get(0)).getLocation();

		for (int i = 0; i < points.size() - 1; i++) {
			// Put a create handle on the middle of every segment
			list.add(new BendpointCreationHandle(connEP, bendPointIndex, i));

			// If the current user bendpoint matches a bend location, show a
			// move handle
			if (i < points.size() - 1 && bendPointIndex < bendPoints.size()
					&& currBendPoint.equals(points.getPoint(i + 1))) {
				list.add(new BendpointMoveHandle(connEP, bendPointIndex, i + 1));

				// Go to the next user bendpoint
				bendPointIndex++;
				if (bendPointIndex < bendPoints.size())
					currBendPoint = ((Bendpoint) bendPoints.get(bendPointIndex))
							.getLocation();
			}
		}

		return list;
	}

	/**
	 * Creates selection handles for the bendpoints. Explicit (user-defined)
	 * bendpoints will have {@link BendpointMoveHandle}s on them with a single
	 * {@link BendpointCreationHandle} between 2 consecutive explicit
	 * bendpoints. If implicit bendpoints (such as those created by the
	 * {@link AutomaticRouter}) are used, one {@link BendpointCreationHandle} is
	 * placed in the middle of the Connection.
	 * 
	 * @see SelectionHandlesEditPolicy#createSelectionHandles()
	 */
	protected List createSelectionHandles() {
		List list = new ArrayList();
		if (isAutomaticallyBending())
			list = createHandlesForAutomaticBendpoints();
		else
			list = createHandlesForUserBendpoints();
		return list;
	}

	/**
	 * <code>deactivate()</code> is extended to remove the property change
	 * listener on the <code>Connection</code> figure.
	 * 
	 * @see org.eclipse.gef.EditPolicy#deactivate()
	 */
	public void deactivate() {
		getConnection().removePropertyChangeListener(
				Connection.PROPERTY_POINTS, this);
		super.deactivate();
	}

	/**
	 * Erases all bendpoint feedback. Since the original <code>Connection</code>
	 * figure is used for feedback, we just restore the original constraint that
	 * was saved before feedback started to show.
	 * 
	 * @param request
	 *            the BendpointRequest
	 */
	protected void eraseConnectionFeedback(BendpointRequest request) {
		restoreOriginalConstraint();
		originalConstraint = null;
	}

	/**
	 * @see org.eclipse.gef.EditPolicy#eraseSourceFeedback(Request)
	 */
	public void eraseSourceFeedback(Request request) {
		if (REQ_MOVE_BENDPOINT.equals(request.getType())
				|| REQ_CREATE_BENDPOINT.equals(request.getType()))
			eraseConnectionFeedback((BendpointRequest) request);
	}

	/**
	 * Factors the Request into either a MOVE, a DELETE, or a CREATE of a
	 * bendpoint.
	 * 
	 * @see org.eclipse.gef.EditPolicy#getCommand(Request)
	 */
	public Command getCommand(Request request) {
		if (REQ_MOVE_BENDPOINT.equals(request.getType())) {
			if (isDeleting)
				return getDeleteBendpointCommand((BendpointRequest) request);
			return getMoveBendpointCommand((BendpointRequest) request);
		}
		if (REQ_CREATE_BENDPOINT.equals(request.getType()))
			return getCreateBendpointCommand((BendpointRequest) request);
		return null;
	}

	/**
	 * Convenience method for obtaining the host's <code>Connection</code>
	 * figure.
	 * 
	 * @return the Connection figure
	 */
	protected Connection getConnection() {
		return (Connection) ((ConnectionEditPart) getHost()).getFigure();
	}

	/**
	 * Implement this method to return a Command that will create a bendpoint.
	 * 
	 * @param request
	 *            the BendpointRequest
	 * @return a Command to create a bendpoint
	 */
	protected abstract Command getCreateBendpointCommand(
			BendpointRequest request);

	/**
	 * Implement this method to return a Command that will delete a bendpoint.
	 * 
	 * @param request
	 *            the BendpointRequest
	 * @return a Command to delete a bendpoint
	 */
	protected abstract Command getDeleteBendpointCommand(
			BendpointRequest request);

	/**
	 * Implement this method to return a Command that will move a bendpoint.
	 * 
	 * @param request
	 *            the BendpointRequest
	 * @return a Command to move a bendpoint
	 */
	protected abstract Command getMoveBendpointCommand(BendpointRequest request);

	private boolean isAutomaticallyBending() {
		List constraint = (List) getConnection().getRoutingConstraint();
		PointList points = getConnection().getPoints();
		return ((points.size() > 2) && (constraint == null || constraint
				.isEmpty()));
	}

	private boolean lineContainsPoint(Point p1, Point p2, Point p) {
		int tolerance = 7;
		Rectangle rect = Rectangle.SINGLETON;
		rect.setSize(0, 0);
		rect.setLocation(p1.x, p1.y);
		rect.union(p2.x, p2.y);
		rect.expand(tolerance, tolerance);
		if (!rect.contains(p.x, p.y))
			return false;

		int v1x, v1y, v2x, v2y;
		int numerator, denominator;
		double result = 0.0;

		if (p1.x != p2.x && p1.y != p2.y) {

			v1x = p2.x - p1.x;
			v1y = p2.y - p1.y;
			v2x = p.x - p1.x;
			v2y = p.y - p1.y;

			numerator = v2x * v1y - v1x * v2y;
			denominator = v1x * v1x + v1y * v1y;

			result = ((numerator << 10) / denominator * numerator) >> 10;
		}

		// if it is the same point, and it passes the bounding box test,
		// the result is always true.
		return result <= tolerance * tolerance;
	}

	/**
	 * If the number of bendpoints changes, handles are updated.
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// $TODO optimize so that handles aren't added constantly.
		if (getHost().getSelected() != EditPart.SELECTED_NONE)
			addSelectionHandles();
	}

	/**
	 * Restores the original constraint that was saved before feedback began to
	 * show.
	 */
	protected void restoreOriginalConstraint() {
		if (originalConstraint != null) {
			if (originalConstraint == NULL_CONSTRAINT)
				getConnection().setRoutingConstraint(null);
			else
				getConnection().setRoutingConstraint(originalConstraint);
		}
	}

	/**
	 * Since the original figure is used for feedback, this method saves the
	 * original constraint, so that is can be restored when the feedback is
	 * erased.
	 */
	protected void saveOriginalConstraint() {
		originalConstraint = (List) getConnection().getRoutingConstraint();
		if (originalConstraint == null)
			originalConstraint = NULL_CONSTRAINT;
		getConnection().setRoutingConstraint(new ArrayList(originalConstraint));
	}

	private void setReferencePoints(BendpointRequest request) {
		PointList points = getConnection().getPoints();
		int bpIndex = -1;
		List bendPoints = (List) getConnection().getRoutingConstraint();
		Point bp = ((Bendpoint) bendPoints.get(request.getIndex()))
				.getLocation();

		int smallestDistance = -1;

		// points include the bend points as well as start and end, which we may
		// leave out when searching for the bend point index
		for (int i = 1; i < points.size() - 1; i++) {
			if (smallestDistance == -1
					|| points.getPoint(i).getDistance2(bp) < smallestDistance) {
				bpIndex = i;
				smallestDistance = points.getPoint(i).getDistance2(bp);
				if (smallestDistance == 0)
					break;
			}
		}

		points.getPoint(ref1, bpIndex - 1);
		getConnection().translateToAbsolute(ref1);
		points.getPoint(ref2, bpIndex + 1);
		getConnection().translateToAbsolute(ref2);
	}

	/**
	 * Shows feedback when a bendpoint is being created. The original figure is
	 * used for feedback and the original constraint is saved, so that it can be
	 * restored when feedback is erased.
	 * 
	 * @param request
	 *            the BendpointRequest
	 */
	protected void showCreateBendpointFeedback(BendpointRequest request) {
		Point p = new Point(request.getLocation());
		List constraint;
		getConnection().translateToRelative(p);
		Bendpoint bp = new AbsoluteBendpoint(p);
		if (originalConstraint == null) {
			saveOriginalConstraint();
			constraint = (List) getConnection().getRoutingConstraint();
			constraint.add(request.getIndex(), bp);
		} else {
			constraint = (List) getConnection().getRoutingConstraint();
		}
		constraint.set(request.getIndex(), bp);
		getConnection().setRoutingConstraint(constraint);
	}

	/**
	 * Shows feedback when a bendpoint is being deleted. This method is only
	 * called once when the bendpoint is first deleted, not every mouse move.
	 * The original figure is used for feedback and the original constraint is
	 * saved, so that it can be restored when feedback is erased.
	 * 
	 * @param request
	 *            the BendpointRequest
	 */
	protected void showDeleteBendpointFeedback(BendpointRequest request) {
		if (originalConstraint == null) {
			saveOriginalConstraint();
			List constraint = (List) getConnection().getRoutingConstraint();
			constraint.remove(request.getIndex());
			getConnection().setRoutingConstraint(constraint);
		}
	}

	/**
	 * Shows feedback when a bendpoint is being moved. Also checks to see if the
	 * bendpoint should be deleted and then calls
	 * {@link #showDeleteBendpointFeedback(BendpointRequest)} if needed. The
	 * original figure is used for feedback and the original constraint is
	 * saved, so that it can be restored when feedback is erased.
	 * 
	 * @param request
	 *            the BendpointRequest
	 */
	protected void showMoveBendpointFeedback(BendpointRequest request) {
		Point p = new Point(request.getLocation());
		if (!isDeleting)
			setReferencePoints(request);

		if (lineContainsPoint(ref1, ref2, p)) {
			if (!isDeleting) {
				isDeleting = true;
				eraseSourceFeedback(request);
				showDeleteBendpointFeedback(request);
			}
			return;
		}
		if (isDeleting) {
			isDeleting = false;
			eraseSourceFeedback(request);
		}
		if (originalConstraint == null)
			saveOriginalConstraint();
		List constraint = (List) getConnection().getRoutingConstraint();
		getConnection().translateToRelative(p);
		Bendpoint bp = new AbsoluteBendpoint(p);
		constraint.set(request.getIndex(), bp);
		getConnection().setRoutingConstraint(constraint);
	}

	/**
	 * Shows feedback when appropriate. Calls a different method depending on
	 * the request type.
	 * 
	 * @see #showCreateBendpointFeedback(BendpointRequest)
	 * @see #showMoveBendpointFeedback(BendpointRequest)
	 * @param request
	 *            the Request
	 */
	public void showSourceFeedback(Request request) {
		if (REQ_MOVE_BENDPOINT.equals(request.getType()))
			showMoveBendpointFeedback((BendpointRequest) request);
		else if (REQ_CREATE_BENDPOINT.equals(request.getType()))
			showCreateBendpointFeedback((BendpointRequest) request);
	}

}

>>>>>>> 76aa07461566a5976980e6696204781271955163
