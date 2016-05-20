/***************************************************************************
 *   Copyright (C) 2009 by Paul Lutus                                      *
 *   lutusp@arachnoid.com                                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opticalraytracer;

import java.awt.*;
import java.util.*;

/**
 *
 * @author lutusp
 */
final public class RayTraceComputer {

    OpticalRayTracer parent;
    final double toRadians = Math.PI / 180.0;
    final double toDegrees = 180.0 / Math.PI;
    // sodium yellow wavelength nm
    final double dispersionPivot = 589.3;
    final double dispersionFactor = 500000;
    final double redWvlNm = 650;
    final double vioWvlNm = 400;
    final double epsilon = .001;
    final ColorPos colors[] = {
        new ColorPos(1.0, 0.0, 0.0, 650.0), // red
        new ColorPos(1.0, 1.0, 0.0, 570.0), // yellow
        new ColorPos(0.0, 1.0, 0.0, 510.0), // green
        new ColorPos(0.0, 0.0, 1.0, 475.0), // blue
        new ColorPos(.5, 0.0, 1.0, 445.0), // indigo
        new ColorPos(.5, 0.0, .5, 400.0), // violet
        new ColorPos(.5, 0.0, .5, 300.0) // terminator
    };
    MutableInt ipx = new MutableInt();
    MutableInt ipy = new MutableInt();
    MutableDouble dx1 = new MutableDouble();
    MutableDouble dx2 = new MutableDouble();
    MutableDouble dy1 = new MutableDouble();
    MutableDouble dy2 = new MutableDouble();
    MutableDouble dx = new MutableDouble();
    MutableDouble dy = new MutableDouble();
    MutableInt ix = new MutableInt();
    MutableInt iy = new MutableInt();
    MutableDouble xa = new MutableDouble();
    MutableDouble ya = new MutableDouble();
    MutableDouble xb = new MutableDouble();
    MutableDouble yb = new MutableDouble();

    public RayTraceComputer(OpticalRayTracer p) {
        parent = p;
    }

    void drawLenses(Graphics g) {
        Iterator<Lens> it = parent.sv_lensList.iterator();
        while (it.hasNext()) {
            it.next().drawLens(g);
        }
    }

    void drawBoxes(Graphics g) {
        Iterator<Lens> it = parent.sv_lensList.iterator();
        while (it.hasNext()) {
            Lens lens = it.next();
            lens.drawBox(g, lens == parent.selectedLens);
        }
    }

    // create two generic default lenses
    void makeDefaultLenses() {
        if (parent.sv_lensList.size() == 0) {
            double ior = 1.52;
            double disp = 59;
            double cf = 0.03;
            boolean leftHyp = false;
            boolean rightHyp = false;
            boolean symmetrical = true;

            parent.sv_lensList.add(new Lens(parent, this, 0, 0, 2, 6, 6, 0, ior, cf, cf, disp, leftHyp, rightHyp, symmetrical));
            parent.sv_lensList.add(new Lens(parent, this, 4, 0, 2, -6, -6, 0, ior, cf, cf, disp, leftHyp, rightHyp, symmetrical));
        //rayTraceProcess(true);
        }
    }

    public void traceRays(Graphics g, int width, int height) {
        // generate some rays
        Color pbcol = parent.sv_beamColor.getColor();
        double min = parent.yStartBeamPos;
        double max = parent.yEndBeamPos;
        double xs = parent.xBeamSourceRefPlane;
        double xt = parent.xBeamTargetRefPlane;
        g.setColor(parent.sv_barrierColor.getColor());
        drawScaledLine(xs, min * 100, ix, iy, g, parent.beamWidth, false);
        drawScaledLine(xs, max * 100, ix, iy, g, parent.beamWidth, true);
        drawScaledLine(xt, min * 100, ix, iy, g, parent.beamWidth, false);
        drawScaledLine(xt, max * 100, ix, iy, g, parent.beamWidth, true);
        if (parent.beamCount > 1) {
            double fact = (max - min) / ((double) parent.beamCount - 1);
            double y;
            double xSource = parent.xBeamSourceRefPlane;
            double xTarget = parent.xBeamTargetRefPlane;
            double ba = -parent.beamAngle * toRadians;
            // this value must not be zero
            ba = (ba == 0.0) ? 1e-12 : ba;
            for (int i = 0; (y = min + i * fact) <= max; i++) {
                double my = (parent.divergingSource) ? 0 : y;
                my -= (xTarget - xSource) * Math.tan(ba);
                if (parent.dispersionBeams > 1) {
                    for (int j = 0; j < parent.dispersionBeams; j++) {
                        double wvl = (((double) j / (parent.dispersionBeams - 1)) * (650.0 - 400.0)) + 400.0;
                        ColorPos cp = createColorForWvl(wvl);
                        MyColor beamColor = new MyColor(cp.r, cp.g, cp.b);
                        traceOneRay(width, height, xSource, my, xTarget, y, g, parent.sv_lensList, beamColor, wvl);
                    }
                } else { // no dispersion
                    traceOneRay(width, height, xSource, my, xTarget, y, g, parent.sv_lensList, pbcol, 0);
                }
            }
        }
    }

    void traceOneRay(
            int width,
            int height,
            double x1,
            double y1,
            double x2,
            double y2,
            Graphics g,
            Vector<Lens> lensVec,
            Color beamColor,
            double wavelength) {
        ComplexNum v;
        Lens lens;
        RayLensIntersection rayLensIntersection;
        Color intCol = parent.sv_intersectionColor.getColor();

        ComplexNum linea = new ComplexNum(x1, y1);
        ComplexNum lineb = new ComplexNum(x2, y2);
        double air_ior = 1.0;
        boolean intersecting = true;
        int rays = 0;
        while (intersecting && rays++ < parent.maxIntersections) {
            Vector<RayLensIntersection> intersections = new Vector<RayLensIntersection>();
            Iterator<Lens> vli = lensVec.iterator();
            while (vli.hasNext()) {
                lens = vli.next();
                for (int prof = 0; prof <= 1; prof++) {
                    v = lens.profile(prof);
                    lens.computeIntersections(prof, linea.x, linea.y, lineb.x, lineb.y, v.x, v.y, xa, ya, xb, yb);
                    if (lens.inside(xa.v, ya.v)) {
                        intersections.add(new RayLensIntersection(prof, xa.v, ya.v, lens));
                    }
                    if (lens.inside(xb.v, yb.v)) {
                        intersections.add(new RayLensIntersection(prof, xb.v, yb.v, lens));
                    }
                }
            }
            int pointRad = (int) parent.intersectionDotRadius;
            drawScaledPoint(linea.x, linea.y, pointRad, g, intCol);
            Collections.sort(intersections, new IntersectionSortComparator());
            // search for the next lens surface to the right (+x)
            Iterator<RayLensIntersection> it = intersections.iterator();
            intersecting = false;
            rayLensIntersection = null;
            while (it.hasNext()) {
                rayLensIntersection = it.next();
                if (rayLensIntersection.x > linea.x + epsilon) {
                    intersecting = true;
                    break;
                }
            }
            g.setColor(beamColor);
            if (intersecting) {
                drawScaledLine(linea.x, linea.y, ix, iy, g, parent.beamWidth, false);
                drawScaledLine(rayLensIntersection.x, rayLensIntersection.y, ix, iy, g, parent.beamWidth, true);

                // beam incident angle
                double ia = Math.atan2(lineb.y - linea.y, lineb.x - linea.x);

                // surface is tangent to lens intersection point
                // and therefore is the first derivative of that point
                double my = rayLensIntersection.lens.tangent(rayLensIntersection.profile, rayLensIntersection.y);
                // surface angle
                double sa = Math.atan(my);

                // Snell's Law calculation block
                double abbe = rayLensIntersection.lens.dispersion;
                double mediaIor = (wavelength == 0 || abbe == 0) ? rayLensIntersection.lens.ior : dispersionIndex(rayLensIntersection.lens.ior, wavelength, abbe);
                double n1, n2;
                if (rayLensIntersection.profile == 1) {
                    // if air -> lens transition
                    n1 = air_ior;
                    n2 = mediaIor;
                } else {
                    // lens -> air transition
                    n1 = mediaIor;
                    n2 = air_ior;
                }

                // 1. add surface angle to incident angle
                double a1 = ia + sa;
                // 2. perform Snell's Law calculation
                double a2 = Math.asin(n1 * Math.sin(a1) / n2);
                // 3. subtract surface angle
                a2 -= sa;

                // create new rectangular vector using result angle
                double nlx = Math.cos(a2);
                double nly = Math.sin(a2);

                // update vectors
                linea.x = rayLensIntersection.x;
                linea.y = rayLensIntersection.y;
                lineb.x = linea.x + nlx;
                lineb.y = linea.y + nly;

            } else { // no intersection, so draw terminating lines at right
                double x = parent.xBeamTargetRefPlane;
                double y = 0;
                //displayToSpaceOffset(x, y, dx, dy);
                double yy = y_intercept(linea.x, linea.y, lineb.x, lineb.y, x);
                drawScaledLine(linea.x, linea.y, ix, iy, g, parent.beamWidth, false);
                drawScaledLine(x, yy, ix, iy, g, parent.beamWidth, true);
                drawScaledPoint(x, yy, pointRad, g, intCol);
            }
        } // while rays++ < parent.maxIntersections
    }

    void drawBaseline(Graphics g) {
        double x1 = 0;
        double x2 = parent.xSize;
        double y = 0;
        displayToSpaceOffset(x1, y, dx1, dy);
        displayToSpaceOffset(x2, y, dx2, dy);
        g.setColor(parent.sv_yBaselineColor.getColor());
        drawScaledLine(dx1.v, y, ix, iy, g, 1, false);
        drawScaledLine(dx2.v, y, ix, iy, g, 1, true);
    }

    void drawGrid(Graphics g) {
        double x1 = 0;
        double x2 = parent.xSize;
        double y1 = 0;
        double y2 = parent.ySize;
        displayToSpaceOffset(x1, y1, dx1, dy1);
        displayToSpaceOffset(x2, y2, dx2, dy2);
        double fact = 2.0; // increases the density of grid lines
        double e = Math.log10(parent.sv_dispScale * fact) - 100.0;
        e = e - (e % 1.0) + 100.0;
        double step = Math.pow(10.0, -e);
        double xstart = gridRound(dx1.v, step) - step;
        double xend = dx2.v + step;
        double ystart = gridRound(dy2.v, step) - step;
        double yend = dy1.v + step;
        g.setColor(parent.sv_gridColor.getColor());
        double x, y;
        for (int j = 0; (y = ystart + j * step) <= yend; j++) {
            for (int k = 0; (x = xstart + k * step) <= xend; k++) {
                drawScaledLine(x, ystart, ix, iy, g, 1, false);
                drawScaledLine(x, yend, ix, iy, g, 1, true);
            }
        }
        for (int j = 0; (x = xstart + j * step) <= xend; j++) {
            for (int k = 0; (y = ystart + k * step) <= yend; k++) {
                drawScaledLine(xstart, y, ix, iy, g, 0, false);
                drawScaledLine(xend, y, ix, iy, g, 0, true);
            }
        }
    }

    double gridRound(double v, double modulus, boolean roundUp) {
        int sign = (v < 0) ? -1 : 1;
        v = Math.abs(v);
        if (roundUp) {
            v = v + modulus;
        }
        v = v - v % modulus;
        v *= sign;
        return v;
    }

    double gridRound(double v, double modulus) {
        return gridRound(v, modulus, false);
    }

    ColorPos createColorForWvl(double w) {
        int j;
        for (j = 0; j < 6; j++) {
            if (w > colors[j + 1].p && w <= colors[j].p) {
                break;
            }
        }
        double r = ntrp(w, colors[j].p, colors[j + 1].p, colors[j].r, colors[j + 1].r);
        double g = ntrp(w, colors[j].p, colors[j + 1].p, colors[j].g, colors[j + 1].g);
        double b = ntrp(w, colors[j].p, colors[j + 1].p, colors[j].b, colors[j + 1].b);
        return new ColorPos(r, g, b, 0);
    }

    // dior = ior + ((dp - w) * 500000) / (abbe * dp * w^2)
    double dispersionIndex(double ior, double wavelength, double abbe) {
        return ior + ((dispersionPivot - wavelength) * dispersionFactor) / (abbe * dispersionPivot * wavelength * wavelength);
    }

    // line (x1,y1 ... x2,y2) circle (radius, center x center y),
    // intersection points xa,ya ... xb,yb
    void circle_line_intersections(
            double x1,
            double y1,
            double x2,
            double y2,
            double r,
            double cx,
            double cy,
            MutableDouble xa,
            MutableDouble ya,
            MutableDouble xb,
            MutableDouble yb) {
        if (y1 == y2) {
            y2 += 1e-15;
        }
        x1 -= cx;
        x2 -= cx;
        y1 -= cy;
        y2 -= cy;

        double term1 = Math.sqrt(Math.pow(y1 - y2, 2) * (Math.pow(r, 2) * (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) - Math.pow(x2 * y1 - x1 * y2, 2)));
        double term2 = (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) * (y1 - y2);
        double term3 = (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));

        xa.v = (x2 * y1 * Math.pow(y1 - y2, 2) - x2 * term1 + x1 * (-(Math.pow(y1 - y2, 2) * y2) + term1)) / (term2);
        xb.v = (-(x1 * Math.pow(y1 - y2, 2) * y2) - x1 * term1 + x2 * (y1 * Math.pow(y1 - y2, 2) + term1)) / (term2);

        ya.v = ((x1 - x2) * (-(x2 * y1) + x1 * y2) + term1) / term3;
        yb.v = ((x1 - x2) * (-(x2 * y1) + x1 * y2) - term1) / term3;

        // preserve these non-optimized originals in case of difficulty
        //xa.v = (x2 * y1 * Math.pow(y1 - y2, 2) - x2 * Math.sqrt(Math.pow(y1 - y2, 2) * (Math.pow(r, 2) * (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) - Math.pow(x2 * y1 - x1 * y2, 2))) + x1 * (-(Math.pow(y1 - y2, 2) * y2) + Math.sqrt(Math.pow(y1 - y2, 2) * (Math.pow(r, 2) * (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) - Math.pow(x2 * y1 - x1 * y2, 2))))) / ((Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) * (y1 - y2));
        //ya.v = ((x1 - x2) * (-(x2 * y1) + x1 * y2) + Math.sqrt(Math.pow(y1 - y2, 2) * (Math.pow(r, 2) * (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) - Math.pow(x2 * y1 - x1 * y2, 2)))) / (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        //xb.v = (-(x1 * Math.pow(y1 - y2, 2) * y2) - x1 * Math.sqrt(Math.pow(y1 - y2, 2) * (Math.pow(r, 2) * (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) - Math.pow(x2 * y1 - x1 * y2, 2))) + x2 * (y1 * Math.pow(y1 - y2, 2) + Math.sqrt(Math.pow(y1 - y2, 2) * (Math.pow(r, 2) * (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) - Math.pow(x2 * y1 - x1 * y2, 2))))) / ((Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) * (y1 - y2));
        //yb.v = ((x1 - x2) * (-(x2 * y1) + x1 * y2) - Math.sqrt(Math.pow(y1 - y2, 2) * (Math.pow(r, 2) * (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) - Math.pow(x2 * y1 - x1 * y2, 2)))) / (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));

        xa.v += cx;
        xb.v += cx;
        ya.v += cy;
        yb.v += cy;
    }

    // line (x1,y1 ... x2,y2) curvature factor (cf),
    // hyp radius & depth (r, d), center x and y (cx,cy)
    // intersection points xa,ya ... xb,yb
    void hyp_line_intersections(
            double x1,
            double y1,
            double x2,
            double y2,
            double cf,
            double r,
            double d,
            double cx,
            double cy,
            MutableDouble xa,
            MutableDouble ya,
            MutableDouble xb,
            MutableDouble yb) {

        x1 -= cx;
        x2 -= cx;
        y1 -= cy;
        y2 -= cy;

        double term1 = 2 * Math.pow(d, 2) * (y1 - y2) * (x2 * y1 - x1 * y2);
        double term2 = Math.sqrt(-(Math.pow(d, 2) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * ((-1 + 4 * cf) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - 4 * (Math.pow(d, 2) * Math.pow(y1 - y2, 2) + d * (y1 - y2) * (x2 * y1 - x1 * y2) + cf * Math.pow(x2 * y1 - x1 * y2, 2)))));
        double term3 = Math.sqrt(-(Math.pow(d, 2) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * ((-1 + 4 * cf) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - 4 * Math.pow(d, 2) * Math.pow(y1 - y2, 2) - 4 * d * (y1 - y2) * (x2 * y1 - x1 * y2) - 4 * cf * Math.pow(x2 * y1 - x1 * y2, 2))));
        double term4 = (2. * (cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - Math.pow(d, 2) * Math.pow(y1 - y2, 2)));
        double term5 = (2. * (x1 - x2) * (cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - Math.pow(d, 2) * Math.pow(y1 - y2, 2)));

        xa.v = (-(d * Math.pow(r, 2) * Math.pow(x1 - x2, 2)) - term1 + term2) / term4;
        xb.v = -(d * Math.pow(r, 2) * Math.pow(x1 - x2, 2) + term1 + term2) / term4;
        ya.v = (d * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * (-y1 + y2) + 2 * cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * (-(x2 * y1) + x1 * y2) + (y1 - y2) * term3) / term5;
        yb.v = (d * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * (-y1 + y2) + 2 * cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * (-(x2 * y1) + x1 * y2) + (-y1 + y2) * term3) / term5;


        // preserve these non-optimized originals in case of difficulty
        //xa.v = (-(d * Math.pow(r, 2) * Math.pow(x1 - x2, 2)) - 2 * Math.pow(d, 2) * (y1 - y2) * (x2 * y1 - x1 * y2) + Math.sqrt(-(Math.pow(d, 2) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * ((-1 + 4 * cf) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - 4 * (Math.pow(d, 2) * Math.pow(y1 - y2, 2) + d * (y1 - y2) * (x2 * y1 - x1 * y2) + cf * Math.pow(x2 * y1 - x1 * y2, 2)))))) / (2. * (cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - Math.pow(d, 2) * Math.pow(y1 - y2, 2)));
        //ya.v = (d * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * (-y1 + y2) + 2 * cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * (-(x2 * y1) + x1 * y2) + (y1 - y2) * Math.sqrt(-(Math.pow(d, 2) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * ((-1 + 4 * cf) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - 4 * Math.pow(d, 2) * Math.pow(y1 - y2, 2) - 4 * d * (y1 - y2) * (x2 * y1 - x1 * y2) - 4 * cf * Math.pow(x2 * y1 - x1 * y2, 2))))) / (2. * (x1 - x2) * (cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - Math.pow(d, 2) * Math.pow(y1 - y2, 2)));
        //xb.v = -(d * Math.pow(r, 2) * Math.pow(x1 - x2, 2) + 2 * Math.pow(d, 2) * (y1 - y2) * (x2 * y1 - x1 * y2) + Math.sqrt(-(Math.pow(d, 2) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * ((-1 + 4 * cf) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - 4 * (Math.pow(d, 2) * Math.pow(y1 - y2, 2) + d * (y1 - y2) * (x2 * y1 - x1 * y2) + cf * Math.pow(x2 * y1 - x1 * y2, 2)))))) / (2. * (cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - Math.pow(d, 2) * Math.pow(y1 - y2, 2)));
        //yb.v = (d * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * (-y1 + y2) + 2 * cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * (-(x2 * y1) + x1 * y2) + (-y1 + y2) * Math.sqrt(-(Math.pow(d, 2) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) * ((-1 + 4 * cf) * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - 4 * Math.pow(d, 2) * Math.pow(y1 - y2, 2) - 4 * d * (y1 - y2) * (x2 * y1 - x1 * y2) - 4 * cf * Math.pow(x2 * y1 - x1 * y2, 2))))) / (2. * (x1 - x2) * (cf * Math.pow(r, 2) * Math.pow(x1 - x2, 2) - Math.pow(d, 2) * Math.pow(y1 - y2, 2)));

        xa.v += cx;
        xb.v += cx;
        ya.v += cy;
        yb.v += cy;
    }

    //      x1 (x4 (-y2 + y3) + x3 (y2 - y4)) + x2 (x4 (y1 - y3) + x3 (-y1 + y4))
    // xi = ---------------------------------------------------------------------
    //                 -((x3 - x4) (y1 - y2)) + (x1 - x2) (y3 - y4)
    double line_line_intersection(
            double ax1, double ay1, double ax2, double ay2, double bx1, double by1, double bx2, double by2) {
        return (ax1 * (ay2 * (bx1 - bx2) + bx2 * by1 - bx1 * by2) + ax2 * (ay1 * (-bx1 + bx2) - bx2 * by1 + bx1 * by2)) / (-((ay1 - ay2) * (bx1 - bx2)) + (ax1 - ax2) * (by1 - by2));
    }

    //           x2 (-y1 + y2)   xi (-y1 + y2)
    // yi = y2 - ------------- + -------------
    //            -x1 + x2        -x1 + x2
    double y_intercept(
            double x1, double y1, double x2, double y2, double xi) {
        return y1 - (x1 * (-y1 + y2)) / (-x1 + x2) + (xi * (-y1 + y2)) / (-x1 + x2);
    }

    double ntrp(double x, double xa, double xb, double ya, double yb) {
        return ((x - xa) / (xb - xa)) * (yb - ya) + ya;
    }

    void spaceToDisplay(double x, double y, MutableDouble dx, MutableDouble dy) {
        dx.v = ((x - parent.sv_xOffset) * parent.sv_dispScale * parent.ySize) + parent.xCenter;
        dy.v = parent.yCenter - ((y - parent.sv_yOffset) * parent.sv_dispScale * parent.ySize);
    }

    void spaceToDisplay(double x, double y, MutableInt dx, MutableInt dy) {
        dx.v = (int) (((x - parent.sv_xOffset) * parent.sv_dispScale * parent.ySize) + parent.xCenter);
        dy.v = (int) (parent.yCenter - ((y - parent.sv_yOffset) * parent.sv_dispScale * parent.ySize));
    }

    void displayToSpaceOffset(double dx, double dy, MutableDouble x, MutableDouble y) {
        x.v = ((dx - parent.xCenter) / (parent.sv_dispScale * parent.ySize)) + parent.sv_xOffset;
        y.v = ((parent.yCenter - dy) / (parent.sv_dispScale * parent.ySize)) + parent.sv_yOffset;
    }

    void displayToSpace(double dx, double dy, MutableDouble x, MutableDouble y) {
        x.v = ((dx - parent.xCenter) / (parent.sv_dispScale * parent.ySize));
        y.v = ((parent.yCenter - dy) / (parent.sv_dispScale * parent.ySize));
    }

    void scalePoint(double x, double y, MutableInt dx, MutableInt dy) {
        //CartesianInt ci = new CartesianInt();
        spaceToDisplay(x, y, dx, dy);
    //ci.x = (int) cd.x;
    //ci.y = (int) cd.y;
    }

    void drawScaledPoint(double x, double y, double radius, Graphics g, Color col) {
        boolean filled = (radius < 0);
        radius = Math.abs(radius);
        g.setColor(col);
        //int ix,iy;
        scalePoint(x, y, ipx, ipy);
        int r = (int) radius;
        if (filled) {
            g.fillOval(ipx.v - r / 2, ipy.v - r / 2, r, r);
        } else {
            g.drawOval(ipx.v - r / 2, ipy.v - r / 2, r, r);
        }
    }

    void drawScaledLine(double x, double y, MutableInt opx, MutableInt opy, Graphics g, double width, boolean draw) {

        scalePoint(x, y, ipx, ipy);
        if (draw) {
            g.drawLine(opx.v, opy.v, ipx.v, ipy.v);
        }
        opx.v = ipx.v;
        opy.v = ipy.v;
    }

    double sphericalLensProfileXforY(double y, double cx, double sphereRadius, double offset, double thickness) {
        double v = Math.sqrt(sphereRadius * sphereRadius - y * y);
        double q = ((sphereRadius < 0) ? -offset + v + thickness : offset - v + thickness);
        return q + cx;
    }

    // 1st derivative
    double sphericalLensProfileDYforY(double y, double cx, double sphereRadius, double offset, double thickness) {
        double r = sphereRadius;
        double v = -(y / Math.sqrt(Math.pow(r, 2) - Math.pow(y, 2)));
        if (sphereRadius < 0) {
            v = -v;
        }
        return v;
    }

    double hyperbolicLensProfileXforY(double y, double cx, double cf, double sphereRadius, double offset, double thickness) {
        double r = Math.sqrt(sphereRadius * sphereRadius - offset * offset);
        double rad = sphereRadius;
        rad = (rad < 0) ? -rad : rad;
        double d = rad - offset;
        double v = (-(d * Math.pow(r, 2)) + Math.sqrt(Math.pow(d, 2) * Math.pow(r, 2) * ((1 - 4 * cf) * Math.pow(r, 2) + 4 * cf * Math.pow(y, 2)))) / (2. * cf * Math.pow(r, 2));
        if (sphereRadius < 0) {
            v = -v;
        }
        return thickness + v + cx;
    }

    // 1st derivative
    double hyperbolicLensProfileDYforY(double y, double cx, double cf, double sphereRadius, double offset, double thickness) {
        double r = Math.sqrt(sphereRadius * sphereRadius - offset * offset);
        double rad = sphereRadius;
        rad = (rad < 0) ? -rad : rad;
        double d = rad - offset;
        double v = -(2 * Math.pow(d, 2) * y) / Math.sqrt(Math.pow(d, 2) * Math.pow(r, 2) * ((1 - 4 * cf) * Math.pow(r, 2) + 4 * cf * Math.pow(y, 2)));
        if (sphereRadius < 0) {
            v = -v;
        }
        return v;
    }
}

