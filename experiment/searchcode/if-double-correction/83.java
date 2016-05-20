/**
 *  Licensed under GPL. For more information, see
 *    http://jaxodraw.sourceforge.net/license.html
 *  or the LICENSE file in the jaxodraw distribution.
 */
package net.sf.jaxodraw.object.bezier;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.ArrayList;

import net.sf.jaxodraw.object.JaxoObject;
import net.sf.jaxodraw.object.JaxoObjectEditPanel;
import net.sf.jaxodraw.object.JaxoWiggleObject;
import net.sf.jaxodraw.util.JaxoUtils;
import net.sf.jaxodraw.util.graphics.JaxoGraphics2D;


/**
 * Implementation of a bezier for photon lines.
 *
 * @since 2.0
 */
public class JaxoPBezier extends JaxoBezierObject implements JaxoWiggleObject {
    private static final long serialVersionUID = 314159L;
    private transient float freq;
    private transient double bx2;
    private transient double by2;
    private transient double tpar;
    private transient double tstep;
    private transient ArrayList renormsteps;

    /** Constructor: just calls super(). */
    public JaxoPBezier() {
        super();
        renormsteps = new ArrayList(50);
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        freq = 0.f;
        bx2 = 0.d;
        by2 = 0.d;
        tpar = 0.d;
        tstep = 0.d;
        renormsteps = new ArrayList(50);
    }

    /** Returns an exact copy of this JaxoPBezier.
     * @return A copy of this JaxoPBezier.
     */
    public final JaxoObject copy() {
        JaxoPBezier temp = new JaxoPBezier();
        temp.copyFrom(this);
        return temp;
    }

    /** {@inheritDoc} */
    public final boolean isCopy(JaxoObject comp) {
        boolean isCopy = false;

        if (comp instanceof JaxoPBezier) {
            isCopy = super.isCopy(comp);
        }

        return isCopy;
    }

    /** Sets all parameters from the given object to the current one.
     * @param temp The object to copy from.
     */
    public void copyFrom(JaxoPBezier temp) {
        super.copyFrom(temp);
        this.freq = temp.getFrequency();
    }

    /** {@inheritDoc} */
    public void setState(JaxoObject o) {
        copyFrom((JaxoPBezier) o);
    }

    /** {@inheritDoc} */
    public final void paint(JaxoGraphics2D g2) {
        g2.setColor(getColor());
        g2.setStroke(getStroke());
        g2.draw(getObjectPath());
    }

    /**
     * Returns the bounding box of this object.
     *
     * @return the bounding box of this object.
     */
    public Rectangle getBounds() {
        // use Area for bounding box calculation instead of gp.getBounds(),
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4599407
        return getStroke().createStrokedShape(new Area(getObjectPath()))
                   .getBounds();
    }

    /** {@inheritDoc} */
    public final String latexCommand(float scale, Dimension canvasDim) {
        int canvasHeight = canvasDim.height;
        double bxp1, byp1, bxp2, byp2, lx, ly, theta;
        double arot = Math.PI / 2;
        double c = 4.d / (3.d * Math.PI);
        double amp =   (2.d / 3.d) * getAmp();
        Point2D parallel = new Point2D.Double();
        Point2D perp1 = new Point2D.Double();
        Point2D perp2 = new Point2D.Double();

        String command = "";

        //initialize the vector of renoemalized timesteps
        renormsteps.clear();

        // First thing: calculate the (approximate) length of the curve.
        double length = getBezierLength();

        if (JaxoUtils.zero(freq)) {
            setFrequencyFromWiggles(getWiggles());
        } else {
            setWigglesFromFrequency();
        }

        int wnumb = 2 * ((int) Math.round((length * getFrequency())));

        // Second: define the time step and the tolerance for the last point
        // according to frequency stretching;
        tstep = (float) 1 / wnumb;
        double lptol;
        if (this.isNoFreqStretching()) {
            lptol = .015;
        } else {
            lptol = .0001;
        }

        // Third: draw the Bezier
        double bx1 = (double) getX();
        double by1 = (double) getY();

        // First loop: fill
        // the vector of renormalized time steps to be used when drawing
        // the curve; if frequency stretching is allowed, the vector will
        // contain equal timesteps
        for (tpar = tstep; tpar <= (1 + tstep); tpar += tstep) {
            setNextPoint(bx1, by1, this.isNoFreqStretching());

            if (Math.abs(tpar - 1 - tstep) < lptol && ((Math.abs(bx2 - getX()) > .1)
                        || (Math.abs(by2 - getY()) > .1))) {
                break;
            }
            bx1 = bx2;
            by1 = by2;
        }

        // Overshooting will eventually occur only for the last point in
        // renormsteps; if this is the case remove the last element
        if ((
                    ((Double) renormsteps.get(renormsteps.size() - 1))
                    .doubleValue() - 1.d
                ) > .0000001) {
            renormsteps.remove(renormsteps.size() - 1);
        }

        // Calculate the correction to apply for each timestep, and
        // reset all the components of the renormalized timestep vector
        // in such a way that its last component will be 1.0
        double corr =
            (
                1.d
                - ((Double) renormsteps.get(renormsteps.size() - 1))
                .doubleValue()
            ) / (renormsteps.size());

        for (int i = 0; i < renormsteps.size(); i++) {
            double rvalue =
                ((Double) renormsteps.get(i)).doubleValue() + (
                    corr * (i + 1)
                );
            renormsteps.set(i, new Double(rvalue));
        }

        // LaTeX command routine
        // Reset x1,y1
        bx1 = (double) getX();
        by1 = (double) getY();

        AffineTransform at = new AffineTransform();

        for (int t = 0; t < renormsteps.size(); t++) {
            tryNextBezierPoint(((Double) renormsteps.get(t)).doubleValue());
            theta = Math.atan2(by2 - by1, bx2 - bx1);
            lx = amp * Math.cos(theta);
            ly = amp * Math.sin(theta);

            bxp1 = bx1 + (bx2 - bx1) * c;
            byp1 = by1 + (by2 - by1) * c;
            bxp2 = bx1 + (bx2 - bx1) * (1 - c);
            byp2 = by1 + (by2 - by1) * (1 - c);

            at.setToIdentity();
            at.rotate(arot, bxp1, byp1);
            parallel.setLocation(bxp1 - lx, byp1 - ly);
            perp1.setLocation(0.d, 0.d);
            at.transform(parallel, perp1);
            at.setToIdentity();
            at.rotate(arot, bxp2, byp2);
            parallel.setLocation(bxp2 - lx, byp2 - ly);
            perp2.setLocation(0.d, 0.d);
            at.transform(parallel, perp2);

            Point2D latexP1 = getLatexPoint(bx1, by1, scale, canvasHeight);
            Point2D latexP2 =
                getLatexPoint(perp1.getX(), perp1.getY(), scale, canvasHeight);
            Point2D latexP3 =
                getLatexPoint(perp2.getX(), perp2.getY(), scale, canvasHeight);
            Point2D latexP4 = getLatexPoint(bx2, by2, scale, canvasHeight);

            command =
                command + " \\Bezier" + "("
                + D_FORMAT.format(latexP1.getX()) + ","
                + D_FORMAT.format(latexP1.getY()) + ")" + "("
                + D_FORMAT.format(latexP2.getX()) + ","
                + D_FORMAT.format(latexP2.getY()) + ")" + "("
                + D_FORMAT.format(latexP3.getX()) + ","
                + D_FORMAT.format(latexP3.getY()) + ")" + "("
                + D_FORMAT.format(latexP4.getX()) + ","
                + D_FORMAT.format(latexP4.getY()) + ")";

            arot = -arot;

            bx1 = bx2;
            by1 = by2;
        }

        // Construct the JDidentifier for import purposes

        String jdIdentifier = "%JaxoID:PBez"
                + "(" + getX() + "," + getY() + ")"
                + "(" + getX2() + "," + getY2() + ")"
                + "(" + getX3() + "," + getY3() + ")"
                + "(" + getX4() + "," + getY4() + ")"
                + "{" + getAmp() + "}"
                + "{" + getWiggles() + "}"
                + "[" + isNoFreqStretching() + "]";

        return command + jdIdentifier;
    }

    private void setNextPoint(double bx1, double by1, boolean noFreqStretching) {
        double length = 0.d;
        int tolerance = 1;
        int l;

        double reflength;

        tryNextBezierPoint(tpar);

        if (noFreqStretching) {
            length =
                Math.sqrt(((bx2 - bx1) * (bx2 - bx1))
                    + ((by2 - by1) * (by2 - by1)));
            reflength = 1.d / getFrequency() / 2.d;

            if ((length - reflength) > tolerance) {
                double rtpar;
                int alter = -1;
                for (l = 1; l < 20; l++) {
                    rtpar =
                        tpar + ((alter * tstep) / Math.pow(2.d, (double) l));
                    tryNextBezierPoint(rtpar);
                    length =
                        Math.sqrt(((bx2 - bx1) * (bx2 - bx1))
                            + ((by2 - by1) * (by2 - by1)));
                    if (Math.abs(length - reflength) <= tolerance) {
                        this.tpar = rtpar;
                        break;
                    } else if ((length - reflength) > tolerance) {
                        alter = -1;
                        this.tpar = rtpar;
                    } else {
                        this.tpar = rtpar;
                        alter = +1;
                    }
                }
            } else if ((length - reflength) < -tolerance) {
                double rtpar;
                int alter = -1;
                boolean seconditer = true;

                //find step for overshooting
                for (l = 1; l < 10; l++) {
                    rtpar = tpar + (l * tstep);
                    tryNextBezierPoint(rtpar);
                    length =
                        Math.sqrt(((bx2 - bx1) * (bx2 - bx1))
                            + ((by2 - by1) * (by2 - by1)));
                    if (Math.abs(length - reflength) <= tolerance) {
                        seconditer = false;
                        this.tpar = rtpar;
                        break;
                    } else if ((length - reflength) > tolerance) {
                        seconditer = true;
                        alter = -1;
                        this.tpar = rtpar;
                        break;
                    } else {
                        this.tpar = rtpar;
                    }
                }

                if (seconditer) {
                    for (l = 1; l < 20; l++) {
                        rtpar =
                            tpar
                            + ((alter * tstep) / Math.pow(2.d, (double) l));
                        tryNextBezierPoint(rtpar);
                        length =
                            Math.sqrt(((bx2 - bx1) * (bx2 - bx1))
                                + ((by2 - by1) * (by2 - by1)));
                        if (Math.abs(length - reflength) <= tolerance) {
                            this.tpar = rtpar;
                            break;
                        } else if ((length - reflength) > tolerance) {
                            alter = -1;
                            this.tpar = rtpar;
                        } else {
                            this.tpar = rtpar;
                            alter = +1;
                        }
                    }
                }
            }
        }
        renormsteps.add(new Double(tpar));
    }

    private void tryNextBezierPoint(double t) {
        this.bx2 = (
                (
                    ((double) getX())
                    + (
                        t * (
                            (-((double) getX()) * 3)
                            + (
                                t * (
                                    (3 * ((double) getX()))
                                    - (((double) getX()) * t)
                                )
                            )
                        )
                    )
                )
                + (
                    t * (
                        (3 * ((double) getX2()))
                        + (
                            t * (
                                (-6 * ((double) getX2()))
                                + (((double) getX2()) * 3 * t)
                            )
                        )
                    )
                )
                + (
                    t * t * (
                        (((double) getX3()) * 3)
                        - (((double) getX3()) * 3 * t)
                    )
                ) + (((double) getX4()) * t * t * t)
            );

        this.by2 = (
                (
                    ((double) getY())
                    + (
                        t * (
                            (-((double) getY()) * 3)
                            + (
                                t * (
                                    (3 * ((double) getY()))
                                    - (((double) getY()) * t)
                                )
                            )
                        )
                    )
                )
                + (
                    t * (
                        (3 * ((double) getY2()))
                        + (
                            t * (
                                (-6 * ((double) getY2()))
                                + (((double) getY2()) * 3 * t)
                            )
                        )
                    )
                )
                + (
                    t * t * (
                        (((double) getY3()) * 3)
                        - (((double) getY3()) * 3 * t)
                    )
                ) + (((double) getY4()) * t * t * t)
            );
    }

    /** {@inheritDoc} */
    public float getFrequency() {
        return freq;
    }

    /** {@inheritDoc} */
    public void setWigglesFromFrequency(float frequency) {
        this.freq = frequency;
        setWigglesFromFrequency();
    }

    /** {@inheritDoc} */
    public void setWigglesFromFrequency() {
        int n = (int) Math.round(getBezierLength() * getFrequency());
        setWiggles(n);
    }

    /** {@inheritDoc} */
    public void setFrequencyFromWiggles(int wiggles) {
        setWiggles(wiggles);
        this.freq = (float) (wiggles / getBezierLength());
    }

    /** {@inheritDoc} */
    public void setPreferences() {
        super.setPreferences();
        setWigglesFromFrequency(PHOTON_FREQ);
    }

    /** {@inheritDoc} */
    public void prepareEditPanel(JaxoObjectEditPanel editPanel) {
        editPanel.add4PointsPanel(getPoints(), 0, 0, 3);
        editPanel.addLineColorPanel(getColor(), 3, 0);
        //editPanel.addDoubleLinePanel(bezier, 3, 0);
        editPanel.addStrokePanel(getStrokeWidth(), 0, 1);
        editPanel.addWigglePanel(getAmp(), getWiggles(), 1, 1);
        editPanel.addStretchingPanel(isNoFreqStretching(), 2, 1);

        editPanel.setTitleAndIcon("Photon_bezier_parameters", "bezierph.png");
    }

      //
     // private methods
    //

    private GeneralPath getObjectPath() {
        GeneralPath gp = getGeneralPath();
        gp.reset();

        double bxp1, byp1, bxp2, byp2, lx, ly, theta;
        double arot = Math.PI / 2;
        double c = 4.d / (3.d * Math.PI);
        double amp =   (2.d / 3.d) * getAmp();
        Point2D parallel = new Point2D.Double();
        Point2D perp1 = new Point2D.Double();
        Point2D perp2 = new Point2D.Double();

        // Initialize the vector of renormalized timesteps
        renormsteps.clear();

        // First thing: calculate the (approximate) length of the curve.
        double length = getBezierLength();

        if (JaxoUtils.zero(freq)) {
            setFrequencyFromWiggles(getWiggles());
        } else {
            setWigglesFromFrequency();
        }

        int wnumb = 2 * ((int) Math.round((length * getFrequency())));

        // Second: define the time step and the tolerance for the last point
        // according to frequency stretching;
        tstep = (float) 1 / wnumb;
        double lptol;
        if (this.isNoFreqStretching()) {
            lptol = .015;
        } else {
            lptol = .0001;
        }

        // Third: draw the Bezier
        double bx1 = (double) getX();
        double by1 = (double) getY();

        // First loop: fill
        // the vector of renormalized time steps to be used when drawing
        // the curve; if frequency stretching is allowed, the vector will
        // contain equal timesteps
        for (tpar = tstep; tpar <= (1 + tstep); tpar += tstep) {
            setNextPoint(bx1, by1, this.isNoFreqStretching());

            if (Math.abs(tpar - 1 - tstep) < lptol
                    && ((Math.abs(bx2 - getX()) > .1)
                        || (Math.abs(by2 - getY()) > .1))) {
                break;
            }
            bx1 = bx2;
            by1 = by2;
        }

        // Overshooting will eventually occur only for the last point in
        // renormsteps; if this is the case remove the last element
        if ((
                    ((Double) renormsteps.get(renormsteps.size() - 1))
                    .doubleValue() - 1.d
                ) > .0000001) {
            renormsteps.remove(renormsteps.size() - 1);
        }

        // Calculate the correction to apply for each timestep, and
        // reset all the components of the renormalized timestep vector
        // in such a way that its last component will be 1.0
        double corr =
            (
                1.d
                - ((Double) renormsteps.get(renormsteps.size() - 1))
                .doubleValue()
            ) / (renormsteps.size());

        for (int i = 0; i < renormsteps.size(); i++) {
            double rvalue =
                ((Double) renormsteps.get(i)).doubleValue() + (
                    corr * (i + 1)
                );
            renormsteps.set(i, new Double(rvalue));
        }

        // Drawing routine
        // Reset x1,y1
        bx1 = (double) getX();
        by1 = (double) getY();

        AffineTransform at = new AffineTransform();

        for (int t = 0; t < renormsteps.size(); t++) {
            tryNextBezierPoint(((Double) renormsteps.get(t)).doubleValue());

            theta = Math.atan2(by2 - by1, bx2 - bx1);
            lx = amp * Math.cos(theta);
            ly = amp * Math.sin(theta);

            bxp1 = bx1 + (bx2 - bx1) * c;
            byp1 = by1 + (by2 - by1) * c;
            bxp2 = bx1 + (bx2 - bx1) * (1 - c);
            byp2 = by1 + (by2 - by1) * (1 - c);

            at.setToIdentity();
            at.rotate(arot, bxp1, byp1);
            parallel.setLocation(bxp1 - lx, byp1 - ly);
            perp1.setLocation(0.d, 0.d);
            at.transform(parallel, perp1);
            at.setToIdentity();
            at.rotate(arot, bxp2, byp2);
            parallel.setLocation(bxp2 - lx, byp2 - ly);
            perp2.setLocation(0.d, 0.d);
            at.transform(parallel, perp2);

            gp.moveTo((float) bx1, (float) by1);
            gp.curveTo((float) perp1.getX(), (float) perp1.getY(),
                    (float) perp2.getX(), (float) perp2.getY(),
                    (float) bx2, (float) by2);

            arot = -arot;

            bx1 = bx2;
            by1 = by2;
        }

        return gp;
    }
}

