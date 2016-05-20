/*
 * CurvedStroke.java
 *
 * Copyright (C) 2011 Thomas Everingham
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 *
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * If you find this program useful, please tell me about it! I would be delighted
 * to hear from you at tom.ingatan@gmail.com.
 */

package org.ingatan.component.image.strokes;

import org.ingatan.image.ImageUtils;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

/**
 *
 * @author Thomas Everingham
 * @version 1.0
 */
public class CurvedStroke implements Stroke {

    public static final int COORD_CURVES = 0;
    public static final int TICK_CURVES = 1;
    public static final int SLIGHT_ESS = 2;
    private int type = 0;

    public CurvedStroke(int type) {
        if ((type < 0) || (type > 4)) {
            type = 0;
        }
        this.type = type;
    }

    public Shape createStrokedShape(Shape p) {
        PathIterator path = p.getPathIterator(null);
        GeneralPath result = new GeneralPath();
        float[] coords = new float[6];
        int type;

        float previousX = -1, previousY = -1;
        float currentX, currentY;
        //while we have still not completed the path traversal
        while (!path.isDone()) {
            type = path.currentSegment(coords);

            if (previousX == -1) {
                previousX = coords[0];
            }
            if (previousY == -1) {
                previousY = coords[1];
            }

            currentX = coords[0];
            currentY = coords[1];



            switch (type) {
                case PathIterator.SEG_LINETO:
                    drawLines(result, currentX, currentY, previousX, previousY);
                    break;
                case PathIterator.SEG_MOVETO:
                    break;
                case PathIterator.SEG_CLOSE:
                    result.closePath();
                    break;
                case PathIterator.SEG_CUBICTO:
                    drawLines(result, currentX, currentY, previousX, previousY);
                    break;
                case PathIterator.SEG_QUADTO:
                    drawLines(result, currentX, currentY, previousX, previousY);
                    break;
            }

            previousX = currentX;
            previousY = currentY;
            path.next();
        }

        return result;
    }

    void drawLines(GeneralPath path, float x, float y, float prevX, float prevY) {
        int ctrlSup = 20;
        BasicStroke b = new BasicStroke(1.0f);


        CubicCurve2D.Double cornerCurves = new CubicCurve2D.Double(prevX, prevY, (prevX > x) ? prevX + ctrlSup : prevX - ctrlSup, (prevY < y) ? prevY + ctrlSup : prevY - ctrlSup, (prevX > x) ? x + ctrlSup : x - ctrlSup, (prevY < y) ? y + ctrlSup : y - ctrlSup, x, y);
        CubicCurve2D.Double tickCurve = new CubicCurve2D.Double(prevX, prevY, prevX, prevY, (prevX > x) ? x + ctrlSup : x - ctrlSup, (prevY < y) ? y + ctrlSup : y - ctrlSup, x, y);
        CubicCurve2D.Double slightS = new CubicCurve2D.Double(prevX, prevY, (prevX > x) ? prevX + ctrlSup : prevX - ctrlSup, (prevY < y) ? prevY + ctrlSup : prevY - ctrlSup, (prevX > x) ? x - ctrlSup : x + ctrlSup, (prevY < y) ? y - ctrlSup : y + ctrlSup, x, y);
        CubicCurve2D.Double horizHump = new CubicCurve2D.Double(prevX, prevY, (prevX > x) ? prevX + ctrlSup / 2 : prevX - ctrlSup / 2, (prevY < y) ? prevY + ctrlSup * 2 : prevY - ctrlSup * 2, (prevX > x) ? x + ctrlSup / 2 : x - ctrlSup / 2, (prevY < y) ? y + ctrlSup * 2 : y - ctrlSup * 2, x, y);
        CubicCurve2D.Double vertHump = new CubicCurve2D.Double(prevX, prevY, (prevX > x) ? prevX + ctrlSup * 2 : prevX - ctrlSup * 2, (prevY < y) ? prevY + ctrlSup / 2 : prevY - ctrlSup / 2, (prevX > x) ? x + ctrlSup * 2 : x - ctrlSup * 2, (prevY < y) ? y + ctrlSup / 2 : y - ctrlSup / 2, x, y);


        switch (type) {
            case COORD_CURVES:
                boolean vertical = false;
                double slope = 0;

                if (prevX - x == 0) {
                    vertical = true;
                } else {
                    slope = ((prevY - y) / (prevX - x)); //sign is inverse of normal x-y plane... +ve slope is \, -ve is /
                }


                if (vertical) {
                    path.append(b.createStrokedShape(vertHump), false);
                } else if ((Math.abs(slope) >= 0.4) && (Math.abs(slope) <= 1.6)) {
                    path.append(b.createStrokedShape(cornerCurves), false);
                } else if (Math.abs(slope) > 1.6) {
                    path.append(b.createStrokedShape(vertHump), false);
                } else if (Math.abs(slope) < 0.4) {
                    path.append(b.createStrokedShape(horizHump), false);
                }
                break;
            case TICK_CURVES:
                path.append(b.createStrokedShape(tickCurve), false);
                break;
            case SLIGHT_ESS:
                path.append(b.createStrokedShape(slightS), false);
                break;
        }
    }
}

