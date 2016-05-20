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

/**
 *
 * @author lutusp
 */
final public class Lens {

    OpticalRayTracer parent;
    RayTraceComputer rayTraceComputer;
    double cx;
    double cy;
    double lensRadius;
    double leftSphereRadius;
    double rightSphereRadius;
    double userThickness;
    double internalThickness;
    double ior;
    double lcf;
    double rcf;
    double dispersion;
    boolean leftHyp;
    boolean rightHyp;
    boolean symmetrical;
    double leftOffset, rightOffset;
    boolean valid = false;
    final double delta = 1e-8;
    final double margin = .001;
    Box border = new Box();
    MutableDouble dx = new MutableDouble();
    MutableDouble dy = new MutableDouble();
    MutableDouble xa = new MutableDouble();
    MutableDouble xb = new MutableDouble();
    MutableInt ix0 = new MutableInt();
    MutableInt iy0 = new MutableInt();
    MutableInt ix1 = new MutableInt();
    MutableInt iy1 = new MutableInt();
    MutableInt ix2 = new MutableInt();
    MutableInt iy2 = new MutableInt();

    public Lens(
            OpticalRayTracer parent,
            RayTraceComputer rtc,
            double cx,
            double cy,
            double lensRadius,
            double leftSphereRadius,
            double rightSphereRadius,
            double userThickness,
            double ior,
            double lcf,
            double rcf,
            double dispersion,
            boolean leftHyp,
            boolean rightHyp,
            boolean symmetrical) {
        this.parent = parent;
        this.rayTraceComputer = rtc;
        this.cx = cx;
        this.cy = cy;
        this.lensRadius = lensRadius;
        this.leftSphereRadius = leftSphereRadius;
        this.lcf = lcf;
        this.rightSphereRadius = rightSphereRadius;
        this.rcf = rcf;
        this.userThickness = userThickness;
        this.ior = ior;
        this.dispersion = dispersion;
        this.leftHyp = leftHyp;
        this.rightHyp = rightHyp;
        this.symmetrical = symmetrical;
        setup();
    }

    public Lens(
            OpticalRayTracer parent,
            RayTraceComputer rtc,
            String desc) {
        this.parent = parent;
        this.rayTraceComputer = rtc;
        toVals(desc);
        setup();
    }
    
    void toVals(String desc) {
        try {
            String[] array = desc.split("\\|");
            if (array.length == 13) {
                int i = 0;
                cx = parent.getDouble(array[i++]);
                cy = parent.getDouble(array[i++]);
                lensRadius = parent.getDouble(array[i++]);
                leftSphereRadius = parent.getDouble(array[i++]);
                rightSphereRadius = parent.getDouble(array[i++]);
                userThickness = parent.getDouble(array[i++]);
                ior = parent.getDouble(array[i++]);
                lcf = parent.getDouble(array[i++]);
                rcf = parent.getDouble(array[i++]);
                dispersion = parent.getDouble(array[i++]);
                leftHyp = array[i++].matches("true");
                rightHyp = array[i++].matches("true");
                symmetrical = array[i++].matches("true");
                valid = true;
            } else {
                throw new Exception("malformed description string");
            }
        } catch (Exception e) {
            System.out.println(getClass().getName() + ": Error: " + e);
        }
    }

    @Override
    public String toString() {
        StringBuffer ss = new StringBuffer();
        ss.append(cx + "|");
        ss.append(cy + "|");
        ss.append(lensRadius + "|");
        ss.append(leftSphereRadius + "|");
        ss.append(rightSphereRadius + "|");
        ss.append(userThickness + "|");
        ss.append(ior + "|");
        ss.append(lcf + "|");
        ss.append(rcf + "|");
        ss.append(dispersion + "|");
        ss.append(leftHyp + "|");
        ss.append(rightHyp + "|");
        ss.append(symmetrical);
        return ss.toString();
    }

    void setup() {
        userThickness = (userThickness < 0)?0:userThickness;
        ior = (ior < 1)?1:ior;
        lensRadius = Math.abs(lensRadius);
        if (symmetrical) {
            rightSphereRadius = leftSphereRadius;
            rcf = lcf;
            rightHyp = leftHyp;
        }
        leftSphereRadius = checkRadius(leftSphereRadius);
        rightSphereRadius = checkRadius(rightSphereRadius);
        // must not aproach zero
        lcf = (lcf < delta) ? delta : lcf;
        rcf = (rcf < delta) ? delta : rcf;
        leftOffset = Math.sqrt(leftSphereRadius * leftSphereRadius - lensRadius * lensRadius);
        rightOffset = Math.sqrt(rightSphereRadius * rightSphereRadius - lensRadius * lensRadius);
        internalThickness = 0;
        border.left = 0;
        border.right = 0;
        border.top = 0;
        border.bottom = 0;
        dx.v = 0;
        // pass 1: compute thickness number
        computeBoxSize(-lensRadius, border, dx);
        computeBoxSize(0, border, dx);
        computeBoxSize(lensRadius, border, dx);
        // pass 2: compute borders with thickness applied
        border.left = 0;
        border.right = 0;
        dx.v += lensRadius * parent.sv_lensMinThickness;
        dx.v += userThickness;
        internalThickness = dx.v;
        computeBoxSize(-lensRadius, border, dx);
        computeBoxSize(0, border, dx);
        computeBoxSize(lensRadius, border, dx);
        internalThickness = dx.v;
        border.left -= margin;
        border.right += margin;
        border.top = lensRadius + margin;
        border.bottom = -lensRadius - margin;
    }

    double checkRadius(double v) {
        double sign = (v < 0) ? -1 : 1;
        v = Math.abs(v);
        v = Math.max(v, lensRadius);
        return v * sign;
    }

    void computeBoxSize(double y, Box border, MutableDouble thick) {
        lensXforY(y, 0, xa, xb);
        border.left = Math.min(border.left, xa.v);
        border.left = Math.min(border.left, xb.v);
        border.right = Math.max(border.right, xa.v);
        border.right = Math.max(border.right, xb.v);
        double q = xa.v - xb.v;
        if (q < 0) {
            thick.v = Math.max(thick.v, -q / 2.0);
        }
    }

    void drawBox(Graphics g, boolean focused) {
        if (focused) {
            double x = cx + border.left;
            double y = cy - border.bottom;
            double w = border.right - border.left;
            double h = border.top - border.bottom;
            rayTraceComputer.spaceToDisplay(x, y, ix1, iy1);
            w = w * parent.sv_dispScale * parent.ySize;
            h = h * parent.sv_dispScale * parent.ySize;
            int iw = (int)w;
            int ih = (int)h;
            g.setColor(parent.sv_lensSelColor.getColor());
            g.drawRect(ix1.v, iy1.v, iw,ih);
            g.drawRect(ix1.v+1, iy1.v+1, iw-2, ih-2);
        }
    //parent.p(dx.v + "," + dy.v + "," + w + "," + h);
    }

    void drawLens(Graphics g) {
        int resolution = 30;
        g.setColor(parent.sv_lensOutlineColor.getColor());
        for (int i = 0; i <= resolution; i++) {
            double y = lensRadius * ((2.0 * i / (double) resolution) - 1.0);
            lensXforY(y, cx, xa, xb);
            rayTraceComputer.drawScaledLine(xb.v, y + cy, ix1, iy1, g, 0, (i != 0));
            rayTraceComputer.drawScaledLine(xa.v, y + cy, ix2, iy2, g, 0, (i != 0));
            if (i == 0 || i == resolution) {
                rayTraceComputer.drawScaledLine(xa.v, y + cy, ix0, iy0, g, 0, false);
                rayTraceComputer.drawScaledLine(xb.v, y + cy, ix0, iy0, g, 0, true);
            }
        }
    }

    boolean inside(double x, double y) {
        return border.inside(x - cx, y - cy);
    }

    ComplexNum profile(int prof) {
        double lo = (leftSphereRadius < 0) ? -leftOffset : leftOffset;
        double ro = (rightSphereRadius < 0) ? -rightOffset : rightOffset;
        double x = ((prof == 0) ? cx - lo + internalThickness : cx + ro - internalThickness);
        return new ComplexNum(x, cy);
    }

    double tangent(int profile, double y) {
        y -= cy;
        double qy;
        double rad, off;
        rad = leftSphereRadius;
        off = leftOffset;
        double cf = lcf;
        if (profile == 1) {
            rad = -rightSphereRadius;
            off = rightOffset;
            cf = rcf;
        }
        if ((profile == 0 && leftHyp) || (profile == 1 && rightHyp)) {
            qy = rayTraceComputer.hyperbolicLensProfileDYforY(y, cx, cf, rad, off, -internalThickness);
        } else {
            qy = rayTraceComputer.sphericalLensProfileDYforY(y, cx, rad, off, -internalThickness);
        }
        return qy;
    }

    void computeIntersections(int profile, double x1, double y1, double x2, double y2, double ccx, double ccy, MutableDouble xa, MutableDouble ya, MutableDouble xb, MutableDouble yb) {
        double srad = (profile == 0) ? leftSphereRadius : rightSphereRadius;
        double sr = (srad < 0) ? -srad : srad;
        double offset = (profile == 0) ? leftOffset : rightOffset;
        double r = 0, off = 0, d = 0, th = 0;
        if ((profile == 0 && leftHyp) || (profile == 1 && rightHyp)) {
            r = Math.sqrt(sr * sr - offset * offset);
            off = offset;
            d = sr - off;
            th = internalThickness;
            double cf = rcf;
            if (profile == 0) {
                d = -d;
                th = -th;
                cf = lcf;
            }
            if (srad < 0) {
                d = -d;
            }
            rayTraceComputer.hyp_line_intersections(x1, y1, x2, y2, cf, r, d, cx - th, cy, xa, ya, xb, yb);
        } else {
            rayTraceComputer.circle_line_intersections(x1, y1, x2, y2, sr, ccx, ccy, xa, ya, xb, yb);
        }
    }

    void lensXforY(double y, double ccx, MutableDouble xa, MutableDouble xb) {
        if (leftHyp) {
            xa.v = rayTraceComputer.hyperbolicLensProfileXforY(y, ccx, lcf, -leftSphereRadius, leftOffset, internalThickness);
        } else {
            xa.v = rayTraceComputer.sphericalLensProfileXforY(y, ccx, -leftSphereRadius, leftOffset, internalThickness);
        }
        // right
        if (rightHyp) {
            xb.v = rayTraceComputer.hyperbolicLensProfileXforY(y, ccx, rcf, rightSphereRadius, rightOffset, -internalThickness);
        } else {
            xb.v = rayTraceComputer.sphericalLensProfileXforY(y, ccx, rightSphereRadius, rightOffset, -internalThickness);
        }
    }

    public String explain() {
        StringBuffer ss = new StringBuffer();
        ss.append("# X position = " + cx + "\n");
        ss.append("# Y position = " + cy + "\n");
        ss.append("# Lens Radius = " + lensRadius + "\n");
        ss.append("# Left Sphere Radius = " + leftSphereRadius + "\n");
        ss.append("# Right Sphere Radius = " + rightSphereRadius + "\n");
        ss.append("# Lens Thickness = " + userThickness + "\n");
        ss.append("# Index of Refraction = " + ior + "\n");
        ss.append("# Left Curvature Factor = " + lcf + "\n");
        ss.append("# Right Curvature Factor = " + rcf + "\n");
        ss.append("# Dispersion (abbe) = " + dispersion + "\n");
        ss.append("# Left Hyperboloid = " + leftHyp + "\n");
        ss.append("# Right Hyperboloid = " + rightHyp + "\n");
        ss.append("# Symmetrical = " + symmetrical + "\n");
        return ss.toString();
    }
}

