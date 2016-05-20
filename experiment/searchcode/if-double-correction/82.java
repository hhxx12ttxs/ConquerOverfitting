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
 * Implementation of a bezier for gluon lines.
 *
 * @since 2.0
 */
public class JaxoGlBezier extends JaxoBezierObject implements JaxoWiggleObject {
    private static final long serialVersionUID = 314159L;
    private transient float freq;
    private transient double bx2;
    private transient double by2;
    private transient Point2D cp1;
    private transient Point2D cp1up;
    private transient Point2D cp1down;
    private transient Point2D cp2;
    private transient Point2D cp2up;
    private transient Point2D cp2down;
    private transient Point2D sp1;
    private transient Point2D sp2;
    private transient Point2D sp3;
    private transient Point2D sp4;
    private transient Point2D spbuffer1;
    private transient Point2D spbuffer2;
    private transient Point2D spbuffer3;
    private transient double tpar;
    private transient double tstep;
    private transient ArrayList renormsteps;

    /** Constructor: just calls super(), and initialize all points. */
    public JaxoGlBezier() {
        super();
        initParams();
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        freq = 0.f;
        initParams();
    }

    private void initParams() {
        cp1 = new Point2D.Double(0.d, 0.d);
        cp1up = new Point2D.Double(0.d, 0.d);
        cp1down = new Point2D.Double(0.d, 0.d);
        cp2 = new Point2D.Double(0.d, 0.d);
        cp2up = new Point2D.Double(0.d, 0.d);
        cp2down = new Point2D.Double(0.d, 0.d);
        sp1 = new Point2D.Double(0.d, 0.d);
        sp2 = new Point2D.Double(0.d, 0.d);
        sp3 = new Point2D.Double(0.d, 0.d);
        sp4 = new Point2D.Double(0.d, 0.d);
        spbuffer1 = new Point2D.Double(0.d, 0.d);
        spbuffer2 = new Point2D.Double(0.d, 0.d);
        spbuffer3 = new Point2D.Double(0.d, 0.d);
        renormsteps = new ArrayList(500);
    }

    /** Returns an exact copy of this JaxoFBezier.
     * @return A copy of this JaxoFArc.
     */
    public final JaxoObject copy() {
        JaxoGlBezier temp = new JaxoGlBezier();
        temp.copyFrom(this);
        return temp;
    }

    /** {@inheritDoc} */
    public final boolean isCopy(JaxoObject comp) {
        boolean isCopy = false;

        if (comp instanceof JaxoGlBezier) {
            isCopy = super.isCopy(comp);
        }

        return isCopy;
    }

    /** Sets all parameters from the given object to the current one.
     * @param temp The object to copy from.
     */
    public void copyFrom(JaxoGlBezier temp) {
        super.copyFrom(temp);
        this.freq = temp.getFrequency();
    }

    /** {@inheritDoc} */
    public void setState(JaxoObject o) {
        copyFrom((JaxoGlBezier) o);
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
        String command = "";
        double bx1;
        double by1;
        double length;
        double lptol;
        boolean firststep = true;
        boolean firstleftcurl = true;
        boolean laststep = false;

        // Initialize the vector of renormalized timesteps
        renormsteps.clear();

        // First thing: calculate the (approximate) length of the curve.
        length = getBezierLength();

        if (JaxoUtils.zero(freq)) {
            setFrequencyFromWiggles(getWiggles());
        } else {
            setWigglesFromFrequency();
        }

        int wnumb = (int) Math.round(length * getFrequency());

        // Second: define the time step
        tstep = 1.f / (float) wnumb;

        if (this.isNoFreqStretching()) {
            lptol = .015;
        } else {
            lptol = .0001;
        }

        // Third: draw the Bezier
        bx1 = (double) getX();
        by1 = (double) getY();

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

        // Reset x1,y1
        bx1 = (double) getX();
        by1 = (double) getY();

        for (int t = 0; t < renormsteps.size(); t++) {
            if (t == (renormsteps.size() - 1)) {
                laststep = true;
            }
            tryNextBezierPoint(((Double) renormsteps.get(t)).doubleValue());

            if (firststep) {
                setAllCP1(bx1, by1, 0.45d * getAmp(), firststep);
                setAllCP2(bx1, by1, 0.45d * getAmp(), laststep);
                this.sp2.setLocation(cp1);
                this.sp3.setLocation(cp1up);
                storePoints(firststep);
                firststep = false;
            } else {
                setAllCP1(bx1, by1, 0.45d * getAmp(), firststep);
                setAllCP2(bx1, by1, 0.45d * getAmp(), laststep);

                if (firstleftcurl) {
                    // Need optimization
                    float p2x = (float) ((sp4.getX() + spbuffer3.getX()) * 0.5d);
                    float p2y = (float) ((sp4.getY() + spbuffer3.getY()) * 0.5d);
                    float p1x = (float) ((spbuffer1.getX() + cp1up.getX()) * 0.5d);
                    float p1y = (float) ((spbuffer1.getY() + cp1up.getY()) * 0.5d);
                    float p3x = (float) (((float) spbuffer1.getX() + p2x) * 0.5d);
                    float p3y = (float) (((float) spbuffer1.getY() + p2y) * 0.5d);
                    float p4x = (float) ((spbuffer1.getX() - cp1up.getX())
                            * 0.75d + cp1up.getX());
                    float p4y = (float) ((spbuffer1.getY() - cp1up.getY())
                            * 0.75d + cp1up.getY());

                    float p11x = (float) ((sp1.getX() + cp1down.getX()) * 0.5d);
                    float p11y = (float) ((sp1.getY() + cp1down.getY()) * 0.5d);

                    Point2D latexP1 = getLatexPoint(p1x, p1y, scale, canvasHeight);
                    Point2D latexP2 = getLatexPoint(p2x, p2y, scale, canvasHeight);
                    Point2D latexP3 = getLatexPoint(p3x, p3y, scale, canvasHeight);
                    Point2D latexP4 = getLatexPoint(p4x, p4y, scale, canvasHeight);
                    Point2D latexP11 = getLatexPoint(p11x, p11y, scale, canvasHeight);
                    Point2D latexCp1up = getLatexPoint(cp1up.getX(), cp1up.getY(),
                        scale, canvasHeight);
                    Point2D latexCp1down = getLatexPoint(cp1down.getX(), cp1down.getY(),
                        scale, canvasHeight);

                    //left curl
                    command =
                        command + " \\Bezier" + "("
                        + D_FORMAT.format(latexP2.getX()) + ","
                        + D_FORMAT.format(latexP2.getY()) + ")" + "("
                        + D_FORMAT.format(latexP3.getX()) + ","
                        + D_FORMAT.format(latexP3.getY()) + ")" + "("
                        + D_FORMAT.format(latexP4.getX()) + ","
                        + D_FORMAT.format(latexP4.getY()) + ")" + "("
                        + D_FORMAT.format(latexP1.getX()) + ","
                        + D_FORMAT.format(latexP1.getY()) + ")";

//                    gp.moveTo(p2x, p2y);
//                    gp.curveTo(p3x, p3y, p4x, p4y, p1x, p1y);

                    //right curl
                    command =
                        command + " \\Bezier" + "("
                        + D_FORMAT.format(latexP1.getX()) + ","
                        + D_FORMAT.format(latexP1.getY()) + ")" + "("
                        + D_FORMAT.format(latexCp1up.getX()) + ","
                        + D_FORMAT.format(latexCp1up.getY()) + ")" + "("
                        + D_FORMAT.format(latexCp1down.getX()) + ","
                        + D_FORMAT.format(latexCp1down.getY()) + ")" + "("
                        + D_FORMAT.format(latexP11.getX()) + ","
                        + D_FORMAT.format(latexP11.getY()) + ")";

//                    gp.curveTo((float) cp1up.getX(), (float) cp1up.getY(),
//                            (float) cp1down.getX(), (float) cp1down.getY(),
//                            p11x, p11y);

                    firstleftcurl = false;
                } else {
                    float p1x = (float) ((spbuffer1.getX() - cp1up.getX()) * 0.5d
                            + cp1up.getX());
                    float p1y = (float) ((spbuffer1.getY() - cp1up.getY()) * 0.5d
                            + cp1up.getY());
                    float p11x = (float) ((sp1.getX() + cp1down.getX()) * 0.5d);
                    float p11y = (float) ((sp1.getY() + cp1down.getY()) * 0.5d);
                    float p2x = (float) ((sp4.getX() + spbuffer3.getX()) * 0.5d);
                    float p2y = (float) ((sp4.getY() + spbuffer3.getY()) * 0.5d);

                    Point2D latexP1 = getLatexPoint(p1x, p1y, scale, canvasHeight);
                    Point2D latexP2 = getLatexPoint(p2x, p2y, scale, canvasHeight);
                    Point2D latexP11 = getLatexPoint(p11x, p11y, scale, canvasHeight);
                    Point2D latexCp1up = getLatexPoint(cp1up.getX(), cp1up.getY(),
                        scale, canvasHeight);
                    Point2D latexCp1down = getLatexPoint(cp1down.getX(), cp1down.getY(),
                        scale, canvasHeight);
                    Point2D latexSpbuf1 =
                        getLatexPoint(spbuffer1.getX(), spbuffer1.getY(), scale,
                            canvasHeight);
                    Point2D latexSpbuf3 =
                        getLatexPoint(spbuffer3.getX(), spbuffer3.getY(), scale,
                            canvasHeight);

                    //left curls
                    command =
                        command + " \\Bezier" + "("
                        + D_FORMAT.format(latexP2.getX()) + ","
                        + D_FORMAT.format(latexP2.getY()) + ")" + "("
                        + D_FORMAT.format(latexSpbuf3.getX()) + ","
                        + D_FORMAT.format(latexSpbuf3.getY()) + ")" + "("
                        + D_FORMAT.format(latexSpbuf1.getX()) + ","
                        + D_FORMAT.format(latexSpbuf1.getY()) + ")" + "("
                        + D_FORMAT.format(latexP1.getX()) + ","
                        + D_FORMAT.format(latexP1.getY()) + ")";

//                    gp.moveTo(p2x, p2y);
//                    gp.curveTo((float) spbuffer3.getX(), (float) spbuffer3.getY(),
//                            (float) spbuffer1.getX(), (float) spbuffer1.getY(),
//                            p1x, p1y);

                    //right curls
                    command =
                        command + " \\Bezier" + "("
                        + D_FORMAT.format(latexP1.getX()) + ","
                        + D_FORMAT.format(latexP1.getY()) + ")" + "("
                        + D_FORMAT.format(latexCp1up.getX()) + ","
                        + D_FORMAT.format(latexCp1up.getY()) + ")" + "("
                        + D_FORMAT.format(latexCp1down.getX()) + ","
                        + D_FORMAT.format(latexCp1down.getY()) + ")" + "("
                        + D_FORMAT.format(latexP11.getX()) + ","
                        + D_FORMAT.format(latexP11.getY()) + ")";

//                    gp.curveTo((float) cp1up.getX(), (float) cp1up.getY(),
//                            (float) cp1down.getX(), (float) cp1down.getY(),
//                            p11x, p11y);
                }

                if (laststep) {
                    storePoints(firststep);
                    setAllCP2(bx1, by1, getBFStep(bx1, by1, 4.d), laststep);

                    float p1x = (float) ((spbuffer1.getX() + cp2up.getX())
                            * 0.5d);
                    float p1y = (float) ((spbuffer1.getY() + cp2up.getY())
                            * 0.5d);
                    float p2x = (float) ((sp4.getX() + spbuffer3.getX()) * 0.5d);
                    float p2y = (float) ((sp4.getY() + spbuffer3.getY()) * 0.5d);
                    float p3x = (float) ((spbuffer1.getX() - cp2up.getX())
                            * 0.25d + cp2up.getX());
                    float p3y = (float) ((spbuffer1.getY() - cp2up.getY())
                            * 0.25d + cp2up.getY());
                    float p4x = (float) ((cp2up.getX() + cp2.getX())
                            * 0.5d);
                    float p4y = (float) ((cp2up.getY() + cp2.getY())
                            * 0.5d);

                    Point2D latexP1 = getLatexPoint(p1x, p1y, scale, canvasHeight);
                    Point2D latexP2 = getLatexPoint(p2x, p2y, scale, canvasHeight);
                    Point2D latexP3 = getLatexPoint(p3x, p3y, scale, canvasHeight);
                    Point2D latexP4 = getLatexPoint(p4x, p4y, scale, canvasHeight);
                    Point2D latexCp2 = getLatexPoint(cp2.getX(), cp2.getY(),
                        scale, canvasHeight);
                    Point2D latexSpbuf1 =
                        getLatexPoint(spbuffer1.getX(), spbuffer1.getY(), scale,
                            canvasHeight);
                    Point2D latexSpbuf3 =
                        getLatexPoint(spbuffer3.getX(), spbuffer3.getY(), scale,
                            canvasHeight);

                    //left curl
                    command =
                        command + " \\Bezier" + "("
                        + D_FORMAT.format(latexP2.getX()) + ","
                        + D_FORMAT.format(latexP2.getY()) + ")" + "("
                        + D_FORMAT.format(latexSpbuf3.getX()) + ","
                        + D_FORMAT.format(latexSpbuf3.getY()) + ")" + "("
                        + D_FORMAT.format(latexSpbuf1.getX()) + ","
                        + D_FORMAT.format(latexSpbuf1.getY()) + ")" + "("
                        + D_FORMAT.format(latexP1.getX()) + ","
                        + D_FORMAT.format(latexP1.getY()) + ")";

//                    gp.moveTo(p2x, p2y);
//                    gp.curveTo((float) spbuffer3.getX(), (float) spbuffer3.getY(),
//                            (float) spbuffer1.getX(), (float) spbuffer1.getY(),
//                            p1x, p1y);

                    //right curl
                    command =
                        command + " \\Bezier" + "("
                        + D_FORMAT.format(latexP1.getX()) + ","
                        + D_FORMAT.format(latexP1.getY()) + ")" + "("
                        + D_FORMAT.format(latexP3.getX()) + ","
                        + D_FORMAT.format(latexP3.getY()) + ")" + "("
                        + D_FORMAT.format(latexP4.getX()) + ","
                        + D_FORMAT.format(latexP4.getY()) + ")" + "("
                        + D_FORMAT.format(latexCp2.getX()) + ","
                        + D_FORMAT.format(latexCp2.getY()) + ")";

//                    gp.curveTo(p3x, p3y, p4x, p4y, (float) cp2.getX(),
//                            (float) cp2.getY());
                }

                //Store points for the next step
                storePoints(firststep);
            }
            bx1 = bx2;
            by1 = by2;
        }

        // Construct the JDidentifier for import purposes

        String jdIdentifier = "%JaxoID:GlBez"
                + "(" + getX() + "," + getY() + ")"
                + "(" + getX2() + "," + getY2() + ")"
                + "(" + getX3() + "," + getY3() + ")"
                + "(" + getX4() + "," + getY4() + ")"
                + "{" + getAmp() + "}"
                + "{" + getWiggles() + "}"
                + "[" + isNoFreqStretching() + "]";

        return command + jdIdentifier;
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
        setWigglesFromFrequency(GLUON_FREQ);
    }

    /** {@inheritDoc} */
    public void prepareEditPanel(JaxoObjectEditPanel editPanel) {
        editPanel.add4PointsPanel(getPoints(), 0, 0, 3);
        editPanel.addLineColorPanel(getColor(), 3, 0);
        //editPanel.addDoubleLinePanel(bezier, 3, 0);
        editPanel.addStrokePanel(getStrokeWidth(), 0, 1);
        editPanel.addWigglePanel(getAmp(), getWiggles(), 1, 1);
        editPanel.addStretchingPanel(isNoFreqStretching(), 2, 1);

        editPanel.setTitleAndIcon("Gluon_bezier_parameters", "beziergl.png");
    }

      //
     //   private methods
    //

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
            reflength = 1.d / getFrequency();

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

    private double getBFStep(double bx1, double by1, double rfact) {
        // Determine the shift for the points sets CP1 and CP2 as a function
        // of the current segment length
        double length =
            Math.sqrt(((bx2 - bx1) * (bx2 - bx1))
                + ((by2 - by1) * (by2 - by1)));
        return (length / rfact);
    }

    private void setAllCP1(double bx1, double by1, double bfstep, boolean first) {
        // Sets the CP1 points set
        Point2D tmpcp1ud = new Point2D.Double(0.d, 0.d);
        AffineTransform at = new AffineTransform();
        double theta = Math.atan2(by2 - by1, bx2 - bx1);
        double lx = 0.5 * getAmp() * Math.cos(theta);
        double ly = 0.5 * getAmp() * Math.sin(theta);
        double lx1 = bfstep * Math.cos(theta);
        double ly1 = bfstep * Math.sin(theta);

        if (first) {
            this.cp1.setLocation(bx1, by1);
        } else {
            this.cp1.setLocation(bx1 + lx1, by1 + ly1);
        }

        tmpcp1ud.setLocation(cp1.getX() + lx, cp1.getY() + ly);

        at.rotate(-Math.PI / 2.d, cp1.getX(), cp1.getY());
        at.transform(tmpcp1ud, this.cp1up);

        at.setToRotation(Math.PI / 2.d, cp1.getX(), cp1.getY());
        at.transform(tmpcp1ud, this.cp1down);
    }

    private void setAllCP2(double bx1, double by1, double bfstep, boolean last) {
        // Sets the CP2 points set
        Point2D tmpcp2ud = new Point2D.Double(0.d, 0.d);
        AffineTransform at = new AffineTransform();
        double theta = Math.atan2(by2 - by1, bx2 - bx1);
        double lx = 0.5 * getAmp() * Math.cos(theta);
        double ly = 0.5 * getAmp() * Math.sin(theta);
        double lx1 = bfstep * Math.cos(theta);
        double ly1 = bfstep * Math.sin(theta);

        if (last) {
            this.cp2.setLocation(bx2, by2);
        } else {
            this.cp2.setLocation(bx2 - lx1, by2 - ly1);
        }

        tmpcp2ud.setLocation(cp2.getX() + lx, cp2.getY() + ly);

        at.rotate(-Math.PI / 2.d, cp2.getX(), cp2.getY());
        at.transform(tmpcp2ud, this.cp2up);

        at.setToRotation(Math.PI / 2.d, cp2.getX(), cp2.getY());
        at.transform(tmpcp2ud, this.cp2down);
    }

    private void storePoints(boolean first) {
        // Store the points for the next step
        this.spbuffer1.setLocation(this.sp3);
        this.spbuffer2.setLocation(this.sp2);
        if (first) {
            this.spbuffer3.setLocation(this.cp1);
        } else {
            this.spbuffer3.setLocation(this.sp1);
        }
        this.sp1.setLocation(this.cp2down);
        this.sp2.setLocation(this.cp2);
        this.sp3.setLocation(this.cp2up);
        if (first) {
            this.sp4.setLocation(this.cp1);
        } else {
            this.sp4.setLocation(this.cp1down);
        }
    }

    // TODO: the number of drawn wiggles is not exactly the same as the number derived from frequency
    private GeneralPath getObjectPath() {
        double bx1;
        double by1;
        double length;
        double lptol;
        boolean firststep = true;
        boolean firstleftcurl = true;
        boolean laststep = false;

        // Initialize the vector of renormalized timesteps
        renormsteps.clear();

        // First thing: calculate the (approximate) length of the curve.
        length = getBezierLength();

        if (JaxoUtils.zero(freq)) {
            setFrequencyFromWiggles(getWiggles());
        } else {
            setWigglesFromFrequency();
        }

        int wnumb = (int) Math.round(length * getFrequency());

        // Second: define the time step
        tstep = 1.f / ((float) wnumb + 1);

        if (this.isNoFreqStretching()) {
            lptol = .015;
        } else {
            lptol = .0001;
        }

        // Third: draw the Bezier
        GeneralPath gp = getGeneralPath(); // hold the final path
        gp.reset();

        bx1 = (double) getX();
        by1 = (double) getY();

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

        for (int t = 0; t < renormsteps.size(); t++) {
            if (t == (renormsteps.size() - 1)) {
                laststep = true;
            }
            tryNextBezierPoint(((Double) renormsteps.get(t)).doubleValue());

            if (firststep) {
                setAllCP1(bx1, by1, 0.45d * getAmp(), firststep);
                setAllCP2(bx1, by1, 0.45d * getAmp(), laststep);
                this.sp2.setLocation(cp1);
                this.sp3.setLocation(cp1up);
                storePoints(firststep);
                firststep = false;
            } else {
                setAllCP1(bx1, by1, 0.45d * getAmp(), firststep);
                setAllCP2(bx1, by1, 0.45d * getAmp(), laststep);

                if (firstleftcurl) {
                    // Need optimization
                    float p2x = (float) ((sp4.getX() + spbuffer3.getX()) * 0.5d);
                    float p2y = (float) ((sp4.getY() + spbuffer3.getY()) * 0.5d);
                    float p1x = (float) ((spbuffer1.getX() + cp1up.getX()) * 0.5d);
                    float p1y = (float) ((spbuffer1.getY() + cp1up.getY()) * 0.5d);
                    float p3x = (float) (((float) spbuffer1.getX() + p2x) * 0.5d);
                    float p3y = (float) (((float) spbuffer1.getY() + p2y) * 0.5d);
                    float p4x = (float) ((spbuffer1.getX() - cp1up.getX())
                            * 0.75d + cp1up.getX());
                    float p4y = (float) ((spbuffer1.getY() - cp1up.getY())
                            * 0.75d + cp1up.getY());

                    float p11x = (float) ((sp1.getX() + cp1down.getX()) * 0.5d);
                    float p11y = (float) ((sp1.getY() + cp1down.getY()) * 0.5d);

                    //left curl
                    gp.moveTo(p2x, p2y);
                    gp.curveTo(p3x, p3y, p4x, p4y, p1x, p1y);

                    //right curl
                    gp.curveTo((float) cp1up.getX(), (float) cp1up.getY(),
                            (float) cp1down.getX(), (float) cp1down.getY(),
                            p11x, p11y);

                    firstleftcurl = false;
                } else {

                    float p1x = (float) ((spbuffer1.getX() - cp1up.getX()) * 0.5d
                            + cp1up.getX());
                    float p1y = (float) ((spbuffer1.getY() - cp1up.getY()) * 0.5d
                            + cp1up.getY());
                    float p11x = (float) ((sp1.getX() + cp1down.getX()) * 0.5d);
                    float p11y = (float) ((sp1.getY() + cp1down.getY()) * 0.5d);
                    float p2x = (float) ((sp4.getX() + spbuffer3.getX()) * 0.5d);
                    float p2y = (float) ((sp4.getY() + spbuffer3.getY()) * 0.5d);

                    //left curls
                    gp.moveTo(p2x, p2y);
                    gp.curveTo((float) spbuffer3.getX(), (float) spbuffer3.getY(),
                            (float) spbuffer1.getX(), (float) spbuffer1.getY(),
                            p1x, p1y);

                    //right curls
                    gp.curveTo((float) cp1up.getX(), (float) cp1up.getY(),
                            (float) cp1down.getX(), (float) cp1down.getY(),
                            p11x, p11y);
                }

                if (laststep) {
                    storePoints(firststep);
                    setAllCP2(bx1, by1, 0.45d * getAmp(), laststep);

                    float p1x = (float) ((spbuffer1.getX() + cp2up.getX())
                            * 0.5d);
                    float p1y = (float) ((spbuffer1.getY() + cp2up.getY())
                            * 0.5d);
                    float p2x = (float) ((sp4.getX() + spbuffer3.getX()) * 0.5d);
                    float p2y = (float) ((sp4.getY() + spbuffer3.getY()) * 0.5d);
                    float p3x = (float) ((spbuffer1.getX() - cp2up.getX())
                            * 0.25d + cp2up.getX());
                    float p3y = (float) ((spbuffer1.getY() - cp2up.getY())
                            * 0.25d + cp2up.getY());
                    float p4x = (float) ((cp2up.getX() + cp2.getX())
                            * 0.5d);
                    float p4y = (float) ((cp2up.getY() + cp2.getY())
                            * 0.5d);

                    //left curl
                    gp.moveTo(p2x, p2y);
                    gp.curveTo((float) spbuffer3.getX(), (float) spbuffer3.getY(),
                            (float) spbuffer1.getX(), (float) spbuffer1.getY(),
                            p1x, p1y);

                    //right curl
                    gp.curveTo(p3x, p3y, p4x, p4y, (float) cp2.getX(),
                            (float) cp2.getY());
                }

                //Store points for the next step
                storePoints(firststep);
            }
            bx1 = bx2;
            by1 = by2;
        }
        return gp;
    }
}

