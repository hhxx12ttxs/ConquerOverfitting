/*
<<<<<<< HEAD
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.sun.javafx.stage.StageHelper;
import javafx.geometry.NodeOrientation;

/**
 * Some basic utilities which need to be in java (for shifting operations or
 * other reasons), which are not toolkit dependent.
 *
 */
public class Utils {

    /***************************************************************************
     *                                                                         *
     * Math-related utilities                                                  *
     *                                                                         *
     **************************************************************************/

    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static float clamp(float min, float value, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static int clamp(int min, int value, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static double clamp(double min, double value, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * above the min value.
     */
    public static double clampMin(double value, double min) {
        if (value < min) return min;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * above the min value.
     */
    public static int clampMin(int value, int min) {
        if (value < min) return min;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * under the max value.
     */
    public static float clampMax(float value, float max) {
        if (value > max) return max;
        return value;
    }

    /**
     * Simple utility function which clamps the given value to be strictly
     * under the max value.
     */
    public static int clampMax(int value, int max) {
        if (value > max) return max;
        return value;
    }

    /**
     * Utility function which returns either {@code less} or {@code more}
     * depending on which {@code value} is closer to. If {@code value}
     * is perfectly between them, then either may be returned.
     */
    public static double nearest(double less, double value, double more) {
        double lessDiff = value - less;
        double moreDiff = more - value;
        if (lessDiff < moreDiff) return less;
        return more;
    }

    /***************************************************************************
     *                                                                         *
     * String-related utilities                                                *
     *                                                                         *
     **************************************************************************/

    /**
     * Simple helper function which works on both desktop and mobile for
     * stripping newlines. The problem we encountered when attempting this in
     * FX was that there is no character literal in FX and no way that I could
     * see to efficiently create characters representing newline and so forth.
     */
    public static String stripNewlines(String s) {
        if (s == null) return null;
        return s.replace('\n', ' ');
    }

    /**
     * Helper to remove leading and trailing quotes from a string.
     * Works with single or double quotes. 
     */
    public static String stripQuotes(String str) {
        if (str == null) return str;
        if (str.length() == 0) return str;

        int beginIndex = 0;
        final char openQuote = str.charAt(beginIndex);        
        if ( openQuote == '\"' || openQuote=='\'' ) beginIndex += 1;

        int endIndex = str.length();
        final char closeQuote = str.charAt(endIndex - 1);
        if ( closeQuote == '\"' || closeQuote=='\'' ) endIndex -= 1;

        if ((endIndex - beginIndex) < 0) return str;

        // note that String.substring returns "this" if beginIndex == 0 && endIndex == count
        // or a new string that shares the character buffer with the original string.
        return str.substring(beginIndex, endIndex);
    }

    /**
     * Because mobile doesn't have string.split(s) function, this function
     * was written.
     */
    public static String[] split(String str, String separator) {
        if (str == null || str.length() == 0) return new String[] { };
        if (separator == null || separator.length() == 0) return new String[] { };
        if (separator.length() > str.length()) return new String[] { };

        java.util.List<String> result = new java.util.ArrayList<String>();

        int index = str.indexOf(separator);
        while (index >= 0) {
            String newStr = str.substring(0, index);
            if (newStr != null && newStr.length() > 0) {
                result.add(newStr);
            }
            str = str.substring(index + separator.length());
            index = str.indexOf(separator);
        }

        if (str != null && str.length() > 0) {
            result.add(str);
        }

        return result.toArray(new String[] { });
    }

    /**
     * Because mobile doesn't have string.contains(s) function, this function
     * was written.
     */
    public static boolean contains(String src, String s) {
        if (src == null || src.length() == 0) return false;
        if (s == null || s.length() == 0) return false;
        if (s.length() > src.length()) return false;

        return src.indexOf(s) > -1;
    }

    /***************************************************************************
     *                                                                         *
     * Color-related utilities                                                 *
     *                                                                         *
     **************************************************************************/

    /**
     * Calculates a perceptual brightness for a color between 0.0 black and 1.0 while
     */
    public static double calculateBrightness(Color color) {
          return  (0.3*color.getRed()) + (0.59*color.getGreen()) + (0.11*color.getBlue());
    }

    /**
     * Derives a lighter or darker of a given color.
     *
     * @param c           The color to derive from
     * @param brightness  The brightness difference for the new color -1.0 being 100% dark which is always black, 0.0 being
     *                    no change and 1.0 being 100% lighter which is always white
     */
    public static Color deriveColor(Color c, double brightness) {
        double baseBrightness = calculateBrightness(c);
        double calcBrightness = brightness;
        // Fine adjustments to colors in ranges of brightness to adjust the contrast for them
        if (brightness > 0) {
            if (baseBrightness > 0.85) {
                calcBrightness = calcBrightness * 1.6;
            } else if (baseBrightness > 0.6) {
                // no change
            } else if (baseBrightness > 0.5) {
                calcBrightness = calcBrightness * 0.9;
            } else if (baseBrightness > 0.4) {
                calcBrightness = calcBrightness * 0.8;
            } else if (baseBrightness > 0.3) {
                calcBrightness = calcBrightness * 0.7;
            } else {
                calcBrightness = calcBrightness * 0.6;
            }
        } else {
            if (baseBrightness < 0.2) {
                calcBrightness = calcBrightness * 0.6;
            }
        }
        // clamp brightness
        if (calcBrightness < -1) { calcBrightness = -1; } else if (calcBrightness > 1) {calcBrightness = 1;}
        // window two take the calculated brightness multiplyer and derive color based on source color
        double[] hsb = RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue());
        // change brightness
        if (calcBrightness > 0) { // brighter
            hsb[1] *= 1 - calcBrightness;
            hsb[2] += (1 - hsb[2]) * calcBrightness;
        } else { // darker
            hsb[2] *=  calcBrightness + 1;
        }
        // clip saturation and brightness
        if (hsb[1] < 0) { hsb[1] = 0;} else if (hsb[1] > 1) {hsb[1] = 1;}
        if (hsb[2] < 0) { hsb[2] = 0;} else if (hsb[2] > 1) {hsb[2] = 1;}
        // convert back to color
        Color c2 = Color.hsb((int)hsb[0], hsb[1], hsb[2],c.getOpacity());
        return Color.hsb((int)hsb[0], hsb[1], hsb[2],c.getOpacity());

     /*   var hsb:Number[] = RGBtoHSB(c.red,c.green,c.blue);
        // change brightness
        if (brightness > 0) {
            //var bright:Number = brightness * (1-calculateBrightness(c));
            var bright:Number = if (calculateBrightness(c)<0.65 and brightness > 0.5) {
                    if (calculateBrightness(c)<0.2) then brightness * 0.55 else brightness * 0.7
            } else brightness;
            // brighter
            hsb[1] *= 1 - bright;
            hsb[2] += (1 - hsb[2]) * bright;
        } else {
            // darker
            hsb[2] *= brightness+1;
        }
        // clip saturation and brightness
        if (hsb[1] < 0) { hsb[1] = 0;} else if (hsb[1] > 1) {hsb[1] = 1}
        if (hsb[2] < 0) { hsb[2] = 0;} else if (hsb[2] > 1) {hsb[2] = 1}
        // convert back to color
        return Color.hsb(hsb[0],hsb[1],hsb[2]) */
    }

    /**
     * interpolate at a set {@code position} between two colors {@code color1} and {@code color2}.
     * The interpolation is done is linear RGB color space not the default sRGB color space.
     */
    private static Color interpolateLinear(double position, Color color1, Color color2) {
        Color c1Linear = convertSRGBtoLinearRGB(color1);
        Color c2Linear = convertSRGBtoLinearRGB(color2);
        return convertLinearRGBtoSRGB(Color.color(
            c1Linear.getRed()     + (c2Linear.getRed()     - c1Linear.getRed())     * position,
            c1Linear.getGreen()   + (c2Linear.getGreen()   - c1Linear.getGreen())   * position,
            c1Linear.getBlue()    + (c2Linear.getBlue()    - c1Linear.getBlue())    * position,
            c1Linear.getOpacity() + (c2Linear.getOpacity() - c1Linear.getOpacity()) * position
        ));
    }

    /**
     * Get the color at the give {@code position} in the ladder of color stops
     */
    private static Color ladder(final double position, final Stop[] stops) {
        Stop prevStop = null;
        for (int i=0; i<stops.length; i++) {
            Stop stop = stops[i];
            if(position <= stop.getOffset()){
                if (prevStop == null) {
                    return stop.getColor();
                } else {
                    return interpolateLinear((position-prevStop.getOffset())/(stop.getOffset()-prevStop.getOffset()), prevStop.getColor(), stop.getColor());
                }
            }
            prevStop = stop;
        }
        // position is greater than biggest stop, so will we biggest stop's color
        return prevStop.getColor();
    }

    /**
     * Get the color at the give {@code position} in the ladder of color stops
     */
    public static Color ladder(final Color color, final Stop[] stops) {
        return ladder(calculateBrightness(color), stops);
    }

    public static double[] HSBtoRGB(double hue, double saturation, double brightness) {
        // normalize the hue
        double normalizedHue = ((hue % 360) + 360) % 360;
        hue = normalizedHue/360;

        double r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = brightness;
        } else {
            double h = (hue - Math.floor(hue)) * 6.0;
            double f = h - java.lang.Math.floor(h);
            double p = brightness * (1.0 - saturation);
            double q = brightness * (1.0 - saturation * f);
            double t = brightness * (1.0 - (saturation * (1.0 - f)));
            switch ((int) h) {
                case 0:
                    r = brightness;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = brightness;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = brightness;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = brightness;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = brightness;
                    break;
                case 5:
                    r = brightness;
                    g = p;
                    b = q;
                    break;
            }
        }
        double[] f = new double[3];
        f[0] = r;
        f[1] = g;
        f[2] = b;
        return f;
    }

    public static double[] RGBtoHSB(double r, double g, double b) {
        double hue, saturation, brightness;
        double[] hsbvals = new double[3];
        double cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        double cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        brightness = cmax;
        if (cmax != 0)
            saturation = (double) (cmax - cmin) / cmax;
        else
            saturation = 0;

        if (saturation == 0) {
            hue = 0;
        } else {
            double redc = (cmax - r) / (cmax - cmin);
            double greenc = (cmax - g) / (cmax - cmin);
            double bluec = (cmax - b) / (cmax - cmin);
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0 + redc - bluec;
            else
                hue = 4.0 + greenc - redc;
            hue = hue / 6.0;
            if (hue < 0)
                hue = hue + 1.0;
        }
        hsbvals[0] = hue * 360;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    /**
     * Helper function to convert a color in sRGB space to linear RGB space.
     */
    public static Color convertSRGBtoLinearRGB(Color color) {
        double[] colors = new double[] { color.getRed(), color.getGreen(), color.getBlue() };
        for (int i=0; i<colors.length; i++) {
            if (colors[i] <= 0.04045) {
                colors[i] = colors[i] / 12.92;
            } else {
                colors[i] = Math.pow((colors[i] + 0.055) / 1.055, 2.4);
            }
        }
        return Color.color(colors[0], colors[1], colors[2], color.getOpacity());
    }

    /**
     * Helper function to convert a color in linear RGB space to SRGB space.
     */
    public static Color convertLinearRGBtoSRGB(Color color) {
        double[] colors = new double[] { color.getRed(), color.getGreen(), color.getBlue() };
        for (int i=0; i<colors.length; i++) {
            if (colors[i] <= 0.0031308) {
                colors[i] = colors[i] * 12.92;
            } else {
                colors[i] = (1.055 * Math.pow(colors[i], (1.0 / 2.4))) - 0.055;
            }
        }
        return Color.color(colors[0], colors[1], colors[2], color.getOpacity());
    }

    public static <E extends Node> List<E> getManaged(List<E>nodes) {
        List<E> managed = new ArrayList<E>();
        for (E e : nodes) {
            if (e != null && e.isManaged()) {
                managed.add(e);
            }
        }
        return managed;
    }

    /** helper function for calculating the sum of a series of numbers */
    public static double sum(double[] values) {
   	double sum = 0;
    	for (double v : values) sum = sum+v;
    	return sum / values.length;
}

    /**
     * Returns a Point2D that represents an x,y location that should safely position
     * the given node relative to the given parent node.
     *
     * If reposition is set to be false, then the node will be positioned with no
     * regard to it's position being offscreen. Conversely, setting reposition to be
     * true will result in the point being shifted such that the entire node is onscreen.
     *
     * How this works is largely based on the provided hpos and vpos parameters, with
     * the repositioned node trying not to overlap the parent unless absolutely necessary.
     */
    public static Point2D pointRelativeTo(Node parent, Node node, HPos hpos, VPos vpos, boolean reposition) {
        final double nodeWidth = node.getLayoutBounds().getWidth();
        final double nodeHeight = node.getLayoutBounds().getHeight();
        return pointRelativeTo(parent, nodeWidth, nodeHeight, hpos, vpos, 0, 0, reposition);
    }

    public static Point2D pointRelativeTo(Node parent, double anchorWidth, double anchorHeight,
             HPos hpos, VPos vpos, boolean reposition)
    {
        return pointRelativeTo(parent, anchorWidth, anchorHeight, hpos, vpos, 0, 0, reposition);
    }

    public static Point2D pointRelativeTo(Node parent, Node node, HPos hpos,
            VPos vpos, double dx, double dy, boolean reposition)
    {
        final double nodeWidth = node.getLayoutBounds().getWidth();
        final double nodeHeight = node.getLayoutBounds().getHeight();
        return pointRelativeTo(parent, nodeWidth, nodeHeight, hpos, vpos, dx, dy, reposition);
    }

    public static Point2D pointRelativeTo(Node parent, double anchorWidth,
            double anchorHeight, HPos hpos, VPos vpos, double dx, double dy,
            boolean reposition)
    {
        double parentXOffset = getOffsetX(parent);
        final double parentYOffset = getOffsetY(parent);
        final Bounds parentBounds = getBounds(parent);
        Scene scene = parent.getScene();
        NodeOrientation orientation = parent.getEffectiveNodeOrientation();

        if (orientation == NodeOrientation.RIGHT_TO_LEFT) {
            if (hpos == HPos.LEFT) {
                hpos = HPos.RIGHT;
            } else if (hpos == HPos.RIGHT) {
                hpos = HPos.LEFT;
            }
        }

        double layoutX = positionX(parentXOffset, parentBounds, anchorWidth, hpos) + dx;
        final double layoutY = positionY(parentYOffset, parentBounds, anchorHeight, vpos) + dy;

        if (orientation == NodeOrientation.RIGHT_TO_LEFT && hpos == HPos.CENTER) {
            //TODO - testing for an instance of Stage seems wrong but works for menus
            if (scene.getWindow() instanceof Stage) {
                layoutX = layoutX + parentBounds.getWidth() - anchorWidth + (dx * 2);
            } else {
                layoutX = layoutX - parentBounds.getWidth() - anchorWidth;
            }
        }

        if (reposition) {
            return pointRelativeTo(parent, anchorWidth, anchorHeight, layoutX, layoutY, hpos, vpos);
        } else {
            return new Point2D(layoutX, layoutY);
        }
    }

    /**
     * Returns a Point2D that represents an x,y location that should safely position
     * the given node relative to the given parent node.
     *
     * The provided x and y values are offsets from the parent node. This allows for
     * the node to be positioned relative to the parent using exact coordinates.
     *
     * If reposition is set to be false, then the node will be positioned with no
     * regard to it's position being offscreen. Conversely, setting reposition to be
     * true will result in the point being shifted such that the entire node is onscreen.
     */
    public static Point2D pointRelativeTo(Node parent, Node node, double x, double y, boolean reposition) {
        final Bounds bounds = parent.localToScreen(parent.getBoundsInLocal());
        final double layoutX = x + bounds.getMinX();
        final double layoutY = y + bounds.getMinY();

        if (reposition) {
            return pointRelativeTo(parent, node, layoutX, layoutY, null, null);
        } else {
            return new Point2D(layoutX, layoutY);
        }
    }

    /**
     * Returns a Point2D that represents an x,y location that should safely position
     * the given node relative to the given parent node.
     *
     * <b>Note</b>: Unlike other functions provided in this class, the provided x
     * and y values are <b>not</b> offsets from the parent node - they are relative
     * to the screen. This reduces the utility of this function, and in many cases
     * you're better off using the more specific functions provided.
     *
     * How this works is largely based on the provided hpos and vpos parameters, with
     * the repositioned node trying not to overlap the parent unless absolutely necessary.
     *
     * This function implicitly has the reposition argument set to true, which means
     * that the returned Point2D be such that the node will be fully on screen.
     *
     * Don't use the BASELINE vpos, it doesn't make sense and would produce wrong result.
     */
    public static Point2D pointRelativeTo(Node parent, Node node, double screenX,
            double screenY, HPos hpos, VPos vpos)
    {
        final double nodeWidth = node.getLayoutBounds().getWidth();
        final double nodeHeight = node.getLayoutBounds().getHeight();

        return pointRelativeTo(parent, nodeWidth, nodeHeight, screenX, screenY, hpos, vpos);
    }

    /**
     * This is the fallthrough function that most other functions fall into. It takes
     * care specifically of the repositioning of the item such that it remains onscreen
     * as best it can, given it's unique qualities.
     *
     * As will all other functions, this one returns a Point2D that represents an x,y
     * location that should safely position the item onscreen as best as possible.
     *
     * Note that <code>width</code> and <height> refer to the width and height of the
     * node/popup that is needing to be repositioned, not of the parent.
     *
     * Don't use the BASELINE vpos, it doesn't make sense and would produce wrong result.
     */
    public static Point2D pointRelativeTo(Object parent, double width,
            double height, double screenX, double screenY, HPos hpos, VPos vpos)
    {
        double finalScreenX = screenX;
        double finalScreenY = screenY;
        final double parentOffsetX = getOffsetX(parent);
        final double parentOffsetY = getOffsetY(parent);
        final Bounds parentBounds = getBounds(parent);

        // ...and then we get the bounds of this screen
        final Screen currentScreen = getScreen(parent);
        final Rectangle2D screenBounds =
                hasFullScreenStage(currentScreen)
                        ? currentScreen.getBounds()
                        : currentScreen.getVisualBounds();

        // test if this layout will force the node to appear outside
        // of the screens bounds. If so, we must reposition the item to a better position.
        // We firstly try to do this intelligently, so as to not overlap the parent if
        // at all possible.
        if (hpos != null) {
            // Firstly we consider going off the right hand side
            if ((finalScreenX + width) > screenBounds.getMaxX()) {
                finalScreenX = positionX(parentOffsetX, parentBounds, width, getHPosOpposite(hpos, vpos));
            }

            // don't let the node go off to the left of the current screen
            if (finalScreenX < screenBounds.getMinX()) {
                finalScreenX = positionX(parentOffsetX, parentBounds, width, getHPosOpposite(hpos, vpos));
            }
        }

        if (vpos != null) {
            // don't let the node go off the bottom of the current screen
            if ((finalScreenY + height) > screenBounds.getMaxY()) {
                finalScreenY = positionY(parentOffsetY, parentBounds, height, getVPosOpposite(hpos,vpos));
            }

            // don't let the node out of the top of the current screen
            if (finalScreenY < screenBounds.getMinY()) {
                finalScreenY = positionY(parentOffsetY, parentBounds, height, getVPosOpposite(hpos,vpos));
            }
        }

        // --- after all the moving around, we do one last check / rearrange.
        // Unlike the check above, this time we are just fully committed to keeping
        // the item on screen at all costs, regardless of whether or not that results
        /// in overlapping the parent object.
        if ((finalScreenX + width) > screenBounds.getMaxX()) {
            finalScreenX -= (finalScreenX + width - screenBounds.getMaxX());
        }
        if (finalScreenX < screenBounds.getMinX()) {
            finalScreenX = screenBounds.getMinX();
        }
        if ((finalScreenY + height) > screenBounds.getMaxY()) {
            finalScreenY -= (finalScreenY + height - screenBounds.getMaxY());
        }
        if (finalScreenY < screenBounds.getMinY()) {
            finalScreenY = screenBounds.getMinY();
        }

        return new Point2D(finalScreenX, finalScreenY);
    }

    /**
     * Returns a Point2D that represents an x,y location that should safely position
     * a node on screen assuming its width and height values are equal to the arguments given
     * to this function.
     *
     * In this situation, the provided screenX and screenY values are in screen coordinates, so
     * the reposition value is implicitly set to true. This means that after calling
     * this function you'll have a Point2D object representing new screen coordinates.
     */
    public static Point2D pointRelativeTo(Window parent, double width, double height, double screenX, double screenY) {
        return pointRelativeTo(parent, width, height, screenX, screenY, null, null);
    }

    /**
     * To facilitate multiple types of parent object, we unfortunately must allow for
     * Objects to be passed in. This method handles determining the x-axis offset of the
     * given Object from the screens (0,0) position. If the Object type is not supported,
     * 0 will be returned.
     */
    private static double getOffsetX(Object obj) {
        if (obj instanceof Node) {
            Scene scene = ((Node)obj).getScene();
            if ((scene == null) || (scene.getWindow() == null)) {
                return 0;
            }
            return scene.getX() + scene.getWindow().getX();
        } else if (obj instanceof Window) {
            return ((Window)obj).getX();
        } else {
            return 0;
        }
    }

    /**
     * To facilitate multiple types of parent object, we unfortunately must allow for
     * Objects to be passed in. This method handles determining the y-axis offset of the
     * given Object from the screens (0,0) position. If the Object type is not supported,
     * 0 will be returned.
     */
    private static double getOffsetY(Object obj) {
        if (obj instanceof Node) {
            Scene scene = ((Node)obj).getScene();
            if ((scene == null) || (scene.getWindow() == null)) {
                return 0;
            }
            return scene.getY() + scene.getWindow().getY();
        } else if (obj instanceof Window) {
            return ((Window)obj).getY();
        } else {
            return 0;
        }
    }

    /**
     * Utility function that returns the x-axis position that an object should be positioned at,
     * given the parent x-axis offset, the parents bounds, the width of the object, and
     * the required HPos.
     */
    private static double positionX(double parentXOffset, Bounds parentBounds, double width, HPos hpos) {
        if (hpos == HPos.CENTER) {
            // this isn't right, but it is needed for root menus to show properly
            return parentXOffset + parentBounds.getMinX();
        } else if (hpos == HPos.RIGHT) {
            return parentXOffset + parentBounds.getMaxX();
        } else if (hpos == HPos.LEFT) {
            return parentXOffset + parentBounds.getMinX() - width;
        } else {
            return 0;
=======
 * Copyright (c) 2008-2013 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package org.whattf.checker.schematronequiv;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import org.whattf.checker.AttributeUtil;
import org.whattf.checker.Checker;
import org.whattf.checker.LocatorImpl;
import org.whattf.checker.TaintableLocatorImpl;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class Assertions extends Checker {

    private static boolean w3cBranding = "1".equals(System.getProperty("nu.validator.servlet.w3cbranding")) ? true
            : false;

    private static boolean lowerCaseLiteralEqualsIgnoreAsciiCaseString(
            String lowerCaseLiteral, String string) {
        if (string == null) {
            return false;
        }
        if (lowerCaseLiteral.length() != string.length()) {
            return false;
        }
        for (int i = 0; i < lowerCaseLiteral.length(); i++) {
            char c0 = lowerCaseLiteral.charAt(i);
            char c1 = string.charAt(i);
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsIgnoreAsciiCase(String one, String other) {
        if (other == null) {
            if (one == null) {
                return true;
            } else {
                return false;
            }
        }
        if (one.length() != other.length()) {
            return false;
        }
        for (int i = 0; i < one.length(); i++) {
            char c0 = one.charAt(i);
            char c1 = other.charAt(i);
            if (c0 >= 'A' && c0 <= 'Z') {
                c0 += 0x20;
            }
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }

    private static final String trimSpaces(String str) {
        return trimLeadingSpaces(trimTrailingSpaces(str));
    }

    private static final String trimLeadingSpaces(String str) {
        if (str == null) {
            return null;
        }
        for (int i = str.length(); i > 0; --i) {
            char c = str.charAt(str.length() - i);
            if (!(' ' == c || '\t' == c || '\n' == c || '\f' == c || '\r' == c)) {
                return str.substring(str.length() - i, str.length());
            }
        }
        return "";
    }

    private static final String trimTrailingSpaces(String str) {
        if (str == null) {
            return null;
        }
        for (int i = str.length() - 1; i >= 0; --i) {
            char c = str.charAt(i);
            if (!(' ' == c || '\t' == c || '\n' == c || '\f' == c || '\r' == c)) {
                return str.substring(0, i + 1);
            }
        }
        return "";
    }

    private static final Map<String, String> OBSOLETE_ELEMENTS = new HashMap<String, String>();

    static {
        OBSOLETE_ELEMENTS.put("center", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("font", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("big", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("strike", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("tt", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put("acronym",
                "Use the \u201Cabbr\u201D element instead.");
        OBSOLETE_ELEMENTS.put("dir", "Use the \u201Cul\u201D element instead.");
        OBSOLETE_ELEMENTS.put("applet",
                "Use the \u201Cobject\u201D element instead.");
        OBSOLETE_ELEMENTS.put("basefont", "Use CSS instead.");
        OBSOLETE_ELEMENTS.put(
                "frameset",
                "Use the \u201Ciframe\u201D element and CSS instead, or use server-side includes.");
        OBSOLETE_ELEMENTS.put(
                "noframes",
                "Use the \u201Ciframe\u201D element and CSS instead, or use server-side includes.");
        if (w3cBranding) {
            OBSOLETE_ELEMENTS.put(
                    "hgroup",
                    "To mark up subheadings, consider either just putting the "
                            + "subheading into a \u201Cp\u201D element after the "
                            + "\u201Ch1\u201D-\u201Ch6\u201D element containing the "
                            + "main heading, or else putting the subheading directly "
                            + "within the \u201Ch1\u201D-\u201Ch6\u201D element "
                            + "containing the main heading, but separated from the main "
                            + "heading by punctuation and/or within, for example, a "
                            + "\u201Cspan class=\"subheading\"\u201D element with "
                            + "differentiated styling. "
                            + "To group headings and subheadings, alternative titles, "
                            + "or taglines, consider using the \u201Cheader\u201D or "
                            + "\u201Cdiv\u201D elements.");
        }
    }

    private static final Map<String, String[]> OBSOLETE_ATTRIBUTES = new HashMap<String, String[]>();

    static {
        OBSOLETE_ATTRIBUTES.put("abbr", new String[] { "td", "th" });
        OBSOLETE_ATTRIBUTES.put("archive", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("axis", new String[] { "td", "th" });
        OBSOLETE_ATTRIBUTES.put("charset", new String[] { "link", "a" });
        OBSOLETE_ATTRIBUTES.put("classid", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("code", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("codebase", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("codetype", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("coords", new String[] { "a" });
        OBSOLETE_ATTRIBUTES.put("datafld", new String[] { "span", "div",
                "object", "input", "select", "textarea", "button", "table" });
        OBSOLETE_ATTRIBUTES.put("dataformatas", new String[] { "span", "div",
                "object", "input", "select", "textarea", "button", "table" });
        OBSOLETE_ATTRIBUTES.put("datasrc", new String[] { "span", "div",
                "object", "input", "select", "textarea", "button", "table" });
        OBSOLETE_ATTRIBUTES.put("datapagesize", new String[] { "table" });
        OBSOLETE_ATTRIBUTES.put("declare", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("event", new String[] { "script" });
        OBSOLETE_ATTRIBUTES.put("for", new String[] { "script" });
        OBSOLETE_ATTRIBUTES.put("language", new String[] { "script" });
        if (!w3cBranding) {
            OBSOLETE_ATTRIBUTES.put("longdesc",
                    new String[] { "img", "iframe" });
        }
        OBSOLETE_ATTRIBUTES.put("methods", new String[] { "link", "a" });
        OBSOLETE_ATTRIBUTES.put("name", new String[] { "img", "embed", "option" });
        OBSOLETE_ATTRIBUTES.put("nohref", new String[] { "area" });
        OBSOLETE_ATTRIBUTES.put("profile", new String[] { "head" });
        OBSOLETE_ATTRIBUTES.put("rev", new String[] { "link", "a" });
        OBSOLETE_ATTRIBUTES.put("scheme", new String[] { "meta" });
        OBSOLETE_ATTRIBUTES.put("scope", new String[] { "td" });
        OBSOLETE_ATTRIBUTES.put("shape", new String[] { "a" });
        OBSOLETE_ATTRIBUTES.put("standby", new String[] { "object" });
        OBSOLETE_ATTRIBUTES.put("target", new String[] { "link" });
        OBSOLETE_ATTRIBUTES.put("type", new String[] { "param" });
        OBSOLETE_ATTRIBUTES.put("urn", new String[] { "a", "link" });
        OBSOLETE_ATTRIBUTES.put("usemap", new String[] { "input" });
        OBSOLETE_ATTRIBUTES.put("valuetype", new String[] { "param" });
        OBSOLETE_ATTRIBUTES.put("version", new String[] { "html" });
    }

    private static final Map<String, String> OBSOLETE_ATTRIBUTES_MSG = new HashMap<String, String>();

    static {
        OBSOLETE_ATTRIBUTES_MSG.put(
                "abbr",
                "Consider instead beginning the cell contents with concise text, followed by further elaboration if needed.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "archive",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Carchive\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("axis",
                "Use the \u201Cscope\u201D attribute.");
        OBSOLETE_ATTRIBUTES_MSG.put("charset",
                "Use an HTTP Content-Type header on the linked resource instead.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "classid",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Cclassid\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "code",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccode\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "codebase",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccodebase\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "codetype",
                "Use the \u201Cdata\u201D and \u201Ctype\u201D attributes to invoke plugins. To set a parameter with the name \u201Ccodetype\u201D, use the \u201Cparam\u201D element.");
        OBSOLETE_ATTRIBUTES_MSG.put("coords",
                "Use \u201Carea\u201D instead of \u201Ca\u201D for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put("datapagesize", "You can safely omit it.");
        OBSOLETE_ATTRIBUTES_MSG.put("datafld", "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("dataformatas", "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("datasrc", "Use script and a mechanism such as XMLHttpRequest to populate the page dynamically");
        OBSOLETE_ATTRIBUTES_MSG.put("for",
                "Use DOM Events mechanisms to register event listeners.");
        OBSOLETE_ATTRIBUTES_MSG.put("event",
                "Use DOM Events mechanisms to register event listeners.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "declare",
                "Repeat the \u201Cobject\u201D element completely each time the resource is to be reused.");
        OBSOLETE_ATTRIBUTES_MSG.put("language",
                "Use the \u201Ctype\u201D attribute instead.");
        if (!w3cBranding) {
            OBSOLETE_ATTRIBUTES_MSG.put("longdesc",
                    "Use a regular \u201Ca\u201D element to link to the description.");
        }
        OBSOLETE_ATTRIBUTES_MSG.put("methods",
                "Use the HTTP OPTIONS feature instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("name",
                "Use the \u201Cid\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("nohref",
                "Omitting the \u201Chref\u201D attribute is sufficient.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "profile",
                "To declare which \u201Cmeta\u201D terms are used in the document, instead register the names as meta extensions. To trigger specific UA behaviors, use a \u201Clink\u201D element instead.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "rev",
                "Use the \u201Crel\u201D attribute instead, with a term having the opposite meaning.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "scheme",
                "Use only one scheme per field, or make the scheme declaration part of the value.");
        OBSOLETE_ATTRIBUTES_MSG.put("scope",
                "Use the \u201Cscope\u201D attribute on a \u201Cth\u201D element instead.");
        OBSOLETE_ATTRIBUTES_MSG.put("shape",
                "Use \u201Carea\u201D instead of \u201Ca\u201D for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "standby",
                "Optimise the linked resource so that it loads quickly or, at least, incrementally.");
        OBSOLETE_ATTRIBUTES_MSG.put("target", "You can safely omit it.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "type",
                "Use the \u201Cname\u201D and \u201Cvalue\u201D attributes without declaring value types.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "urn",
                "Specify the preferred persistent identifier using the \u201Chref\u201D attribute instead.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "usemap",
                "Use the \u201Cimg\u201D element instead of the \u201Cinput\u201D element for image maps.");
        OBSOLETE_ATTRIBUTES_MSG.put(
                "valuetype",
                "Use the \u201Cname\u201D and \u201Cvalue\u201D attributes without declaring value types.");
        OBSOLETE_ATTRIBUTES_MSG.put("version", "You can safely omit it.");
    }

    private static final Map<String, String[]> OBSOLETE_STYLE_ATTRS = new HashMap<String, String[]>();

    static {
        OBSOLETE_STYLE_ATTRS.put("align", new String[] { "caption", "iframe",
                "img", "input", "object", "embed", "legend", "table", "hr",
                "div", "h1", "h2", "h3", "h4", "h5", "h6", "p", "col",
                "colgroup", "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("alink", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("allowtransparency", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("background", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("bgcolor", new String[] { "table", "tr", "td",
                "th", "body" });
        OBSOLETE_STYLE_ATTRS.put("border", new String[] { "object" });
        OBSOLETE_STYLE_ATTRS.put("cellpadding", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("cellspacing", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("char", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("charoff", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("clear", new String[] { "br" });
        OBSOLETE_STYLE_ATTRS.put("color", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("compact", new String[] { "dl", "menu", "ol",
                "ul" });
        OBSOLETE_STYLE_ATTRS.put("frameborder", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("frame", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("height", new String[] { "td", "th" });
        OBSOLETE_STYLE_ATTRS.put("hspace", new String[] { "img", "object", "embed" });
        OBSOLETE_STYLE_ATTRS.put("link", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginbottom", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginheight", new String[] { "iframe", "body" });
        OBSOLETE_STYLE_ATTRS.put("marginleft", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginright", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("margintop", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("marginwidth", new String[] { "iframe", "body" });
        OBSOLETE_STYLE_ATTRS.put("noshade", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("nowrap", new String[] { "td", "th" });
        OBSOLETE_STYLE_ATTRS.put("rules", new String[] { "table" });
        OBSOLETE_STYLE_ATTRS.put("scrolling", new String[] { "iframe" });
        OBSOLETE_STYLE_ATTRS.put("size", new String[] { "hr" });
        OBSOLETE_STYLE_ATTRS.put("text", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("type", new String[] { "li", "ul" });
        OBSOLETE_STYLE_ATTRS.put("valign", new String[] { "col", "colgroup",
                "tbody", "td", "tfoot", "th", "thead", "tr" });
        OBSOLETE_STYLE_ATTRS.put("vlink", new String[] { "body" });
        OBSOLETE_STYLE_ATTRS.put("vspace", new String[] { "img", "object", "embed" });
        OBSOLETE_STYLE_ATTRS.put("width", new String[] { "hr", "table", "td",
                "th", "col", "colgroup", "pre" });
    }

    private static final String[] SPECIAL_ANCESTORS = { "a", "address",
            "button", "caption", "dfn", "dt", "figcaption", "figure", "footer",
            "form", "header", "label", "map", "noscript", "th", "time",
            "progress", "meter", "article", "aside", "nav" };

    private static int specialAncestorNumber(String name) {
        for (int i = 0; i < SPECIAL_ANCESTORS.length; i++) {
            if (name == SPECIAL_ANCESTORS[i]) {
                return i;
            }
        }
        return -1;
    }

    private static Map<String, Integer> ANCESTOR_MASK_BY_DESCENDANT = new HashMap<String, Integer>();

    private static void registerProhibitedAncestor(String ancestor,
            String descendant) {
        int number = specialAncestorNumber(ancestor);
        if (number == -1) {
            throw new IllegalStateException("Ancestor not found in array: "
                    + ancestor);
        }
        Integer maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(descendant);
        int mask = 0;
        if (maskAsObject != null) {
            mask = maskAsObject.intValue();
        }
        mask |= (1 << number);
        ANCESTOR_MASK_BY_DESCENDANT.put(descendant, new Integer(mask));
    }

    static {
        registerProhibitedAncestor("form", "form");
        registerProhibitedAncestor("time", "time");
        registerProhibitedAncestor("progress", "progress");
        registerProhibitedAncestor("meter", "meter");
        registerProhibitedAncestor("dfn", "dfn");
        registerProhibitedAncestor("noscript", "noscript");
        registerProhibitedAncestor("label", "label");
        registerProhibitedAncestor("address", "address");
        registerProhibitedAncestor("address", "section");
        registerProhibitedAncestor("address", "nav");
        registerProhibitedAncestor("address", "article");
        registerProhibitedAncestor("address", "aside");
        registerProhibitedAncestor("header", "header");
        registerProhibitedAncestor("footer", "header");
        registerProhibitedAncestor("address", "header");
        registerProhibitedAncestor("header", "footer");
        registerProhibitedAncestor("footer", "footer");
        registerProhibitedAncestor("dt", "header");
        registerProhibitedAncestor("dt", "footer");
        registerProhibitedAncestor("dt", "article");
        registerProhibitedAncestor("dt", "aside");
        registerProhibitedAncestor("dt", "nav");
        registerProhibitedAncestor("dt", "section");
        registerProhibitedAncestor("dt", "h1");
        registerProhibitedAncestor("dt", "h2");
        registerProhibitedAncestor("dt", "h2");
        registerProhibitedAncestor("dt", "h3");
        registerProhibitedAncestor("dt", "h4");
        registerProhibitedAncestor("dt", "h5");
        registerProhibitedAncestor("dt", "h6");
        registerProhibitedAncestor("dt", "hgroup");
        registerProhibitedAncestor("th", "header");
        registerProhibitedAncestor("th", "footer");
        registerProhibitedAncestor("th", "article");
        registerProhibitedAncestor("th", "aside");
        registerProhibitedAncestor("th", "nav");
        registerProhibitedAncestor("th", "section");
        registerProhibitedAncestor("th", "h1");
        registerProhibitedAncestor("th", "h2");
        registerProhibitedAncestor("th", "h2");
        registerProhibitedAncestor("th", "h3");
        registerProhibitedAncestor("th", "h4");
        registerProhibitedAncestor("th", "h5");
        registerProhibitedAncestor("th", "h6");
        registerProhibitedAncestor("th", "hgroup");
        registerProhibitedAncestor("address", "footer");
        registerProhibitedAncestor("address", "h1");
        registerProhibitedAncestor("address", "h2");
        registerProhibitedAncestor("address", "h3");
        registerProhibitedAncestor("address", "h4");
        registerProhibitedAncestor("address", "h5");
        registerProhibitedAncestor("address", "h6");
        registerProhibitedAncestor("a", "a");
        registerProhibitedAncestor("button", "a");
        registerProhibitedAncestor("a", "details");
        registerProhibitedAncestor("button", "details");
        registerProhibitedAncestor("a", "button");
        registerProhibitedAncestor("button", "button");
        registerProhibitedAncestor("a", "textarea");
        registerProhibitedAncestor("button", "textarea");
        registerProhibitedAncestor("a", "select");
        registerProhibitedAncestor("button", "select");
        registerProhibitedAncestor("a", "keygen");
        registerProhibitedAncestor("button", "keygen");
        registerProhibitedAncestor("a", "embed");
        registerProhibitedAncestor("button", "embed");
        registerProhibitedAncestor("a", "iframe");
        registerProhibitedAncestor("button", "iframe");
        registerProhibitedAncestor("a", "label");
        registerProhibitedAncestor("button", "label");
        registerProhibitedAncestor("caption", "table");
        registerProhibitedAncestor("article", "main");
        registerProhibitedAncestor("aside", "main");
        registerProhibitedAncestor("header", "main");
        registerProhibitedAncestor("footer", "main");
        registerProhibitedAncestor("nav", "main");
    }

    private static final int A_BUTTON_MASK = (1 << specialAncestorNumber("a"))
            | (1 << specialAncestorNumber("button"));

    private static final int FIGCAPTION_MASK = (1 << specialAncestorNumber("figcaption"));

    private static final int FIGURE_MASK = (1 << specialAncestorNumber("figure"));

    private static final int MAP_MASK = (1 << specialAncestorNumber("map"));

    private static final int HREF_MASK = (1 << 30);

    private static final int LABEL_FOR_MASK = (1 << 28);

    private static final Map<String, Set<String>> REQUIRED_ROLE_PARENT_BY_CHILD = new HashMap<String, Set<String>>();

    private static void registerRequiredParentRole(String parent, String child) {
        Set<String> parents = REQUIRED_ROLE_PARENT_BY_CHILD.get(child);
        if (parents == null) {
            parents = new HashSet<String>();
        }
        parents.add(parent);
        REQUIRED_ROLE_PARENT_BY_CHILD.put(child, parents);
    }

    static {
        registerRequiredParentRole("listbox", "option");
        registerRequiredParentRole("menu", "menuitem");
        registerRequiredParentRole("menu", "menuitemcheckbox");
        registerRequiredParentRole("menu", "menuitemradio");
        registerRequiredParentRole("menubar", "menuitem");
        registerRequiredParentRole("menubar", "menuitemcheckbox");
        registerRequiredParentRole("menubar", "menuitemradio");
        registerRequiredParentRole("tablist", "tab");
        registerRequiredParentRole("tree", "treeitem");
        registerRequiredParentRole("list", "listitem");
        registerRequiredParentRole("row", "gridcell");
        registerRequiredParentRole("row", "columnheader");
        registerRequiredParentRole("row", "rowheader");
    }

    private static final Set<String> MUST_NOT_DANGLE_IDREFS = new HashSet<String>();

    static {
        MUST_NOT_DANGLE_IDREFS.add("aria-controls");
        MUST_NOT_DANGLE_IDREFS.add("aria-describedby");
        MUST_NOT_DANGLE_IDREFS.add("aria-flowto");
        MUST_NOT_DANGLE_IDREFS.add("aria-labelledby");
        MUST_NOT_DANGLE_IDREFS.add("aria-owns");
    }

    private static final Map<String, Set<String>> ALLOWED_CHILD_ROLE_BY_PARENT = new HashMap<String, Set<String>>();

    private static void registerAllowedChildRole(String parent, String child) {
        Set<String> children = ALLOWED_CHILD_ROLE_BY_PARENT.get(parent);
        if (children == null) {
            children = new HashSet<String>();
        }
        children.add(child);
        ALLOWED_CHILD_ROLE_BY_PARENT.put(parent, children);
    }

    static {
        registerAllowedChildRole("listbox", "option");
        registerAllowedChildRole("menu", "menuitem");
        registerAllowedChildRole("menu", "menuitemcheckbox");
        registerAllowedChildRole("menu", "menuitemradio");
        registerAllowedChildRole("menubar", "menuitem");
        registerAllowedChildRole("menubar", "menuitemcheckbox");
        registerAllowedChildRole("menubar", "menuitemradio");
        registerAllowedChildRole("tree", "treeitem");
        registerAllowedChildRole("list", "listitem");
        registerAllowedChildRole("radiogroup", "radio");
        registerAllowedChildRole("tablist", "tab");
        registerAllowedChildRole("row", "gridcell");
        registerAllowedChildRole("row", "columnheader");
        registerAllowedChildRole("row", "rowheader");
    }

    private class IdrefLocator {
        private final Locator locator;

        private final String idref;

        private final String additional;

        /**
         * @param locator
         * @param idref
         */
        public IdrefLocator(Locator locator, String idref) {
            this.locator = new LocatorImpl(locator);
            this.idref = idref;
            this.additional = null;
        }

        public IdrefLocator(Locator locator, String idref, String additional) {
            this.locator = new LocatorImpl(locator);
            this.idref = idref;
            this.additional = additional;
        }

        /**
         * Returns the locator.
         * 
         * @return the locator
         */
        public Locator getLocator() {
            return locator;
        }

        /**
         * Returns the idref.
         * 
         * @return the idref
         */
        public String getIdref() {
            return idref;
        }

        /**
         * Returns the additional.
         * 
         * @return the additional
         */
        public String getAdditional() {
            return additional;
        }
    }

    private class StackNode {
        private final int ancestorMask;

        private final String name; // null if not HTML

        private final String role;

        private final String activeDescendant;

        private final String forAttr;

        private Set<Locator> imagesLackingAlt = new HashSet<Locator>();

        private Locator nonEmptyOption = null;

        private boolean children = false;

        private boolean selectedOptions = false;

        private boolean labeledDescendants = false;

        private boolean trackDescendants = false;

        private boolean textNodeFound = false;

        private boolean imgFound = false;

        private boolean embeddedContentFound = false;

        private boolean figcaptionNeeded = false;

        private boolean figcaptionContentFound = false;

        private boolean optionNeeded = false;

        private boolean optionFound = false;

        private boolean noValueOptionFound = false;

        private boolean emptyValueOptionFound = false;

        /**
         * @param ancestorMask
         */
        public StackNode(int ancestorMask, String name, String role,
                String activeDescendant, String forAttr) {
            this.ancestorMask = ancestorMask;
            this.name = name;
            this.role = role;
            this.activeDescendant = activeDescendant;
            this.forAttr = forAttr;
        }

        /**
         * Returns the ancestorMask.
         * 
         * @return the ancestorMask
         */
        public int getAncestorMask() {
            return ancestorMask;
        }

        /**
         * Returns the name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the children.
         * 
         * @return the children
         */
        public boolean isChildren() {
            return children;
        }

        /**
         * Sets the children.
         * 
         * @param children
         *            the children to set
         */
        public void setChildren() {
            this.children = true;
        }

        /**
         * Returns the selectedOptions.
         * 
         * @return the selectedOptions
         */
        public boolean isSelectedOptions() {
            return selectedOptions;
        }

        /**
         * Sets the selectedOptions.
         * 
         * @param selectedOptions
         *            the selectedOptions to set
         */
        public void setSelectedOptions() {
            this.selectedOptions = true;
        }

        /**
         * Returns the labeledDescendants.
         * 
         * @return the labeledDescendants
         */
        public boolean isLabeledDescendants() {
            return labeledDescendants;
        }

        /**
         * Sets the labeledDescendants.
         * 
         * @param labeledDescendants
         *            the labeledDescendants to set
         */
        public void setLabeledDescendants() {
            this.labeledDescendants = true;
        }

        /**
         * Returns the trackDescendants.
         * 
         * @return the trackDescendants
         */
        public boolean isTrackDescendant() {
            return trackDescendants;
        }

        /**
         * Sets the trackDescendants.
         * 
         * @param trackDescendants
         *            the trackDescendants to set
         */
        public void setTrackDescendants() {
            this.trackDescendants = true;
        }

        /**
         * Returns the role.
         * 
         * @return the role
         */
        public String getRole() {
            return role;
        }

        /**
         * Returns the activeDescendant.
         * 
         * @return the activeDescendant
         */
        public String getActiveDescendant() {
            return activeDescendant;
        }

        /**
         * Returns the forAttr.
         * 
         * @return the forAttr
         */
        public String getForAttr() {
            return forAttr;
        }

        /**
         * Returns the textNodeFound.
         * 
         * @return the textNodeFound
         */
        public boolean hasTextNode() {
            return textNodeFound;
        }

        /**
         * Sets the textNodeFound.
         */
        public void setTextNodeFound() {
            this.textNodeFound = true;
        }

        /**
         * Returns the imgFound.
         * 
         * @return the imgFound
         */
        public boolean hasImg() {
            return imgFound;
        }

        /**
         * Sets the imgFound.
         */
        public void setImgFound() {
            this.imgFound = true;
        }

        /**
         * Returns the embeddedContentFound.
         * 
         * @return the embeddedContentFound
         */
        public boolean hasEmbeddedContent() {
            return embeddedContentFound;
        }

        /**
         * Sets the embeddedContentFound.
         */
        public void setEmbeddedContentFound() {
            this.embeddedContentFound = true;
        }

        /**
         * Returns the figcaptionNeeded.
         * 
         * @return the figcaptionNeeded
         */
        public boolean needsFigcaption() {
            return figcaptionNeeded;
        }

        /**
         * Sets the figcaptionNeeded.
         */
        public void setFigcaptionNeeded() {
            this.figcaptionNeeded = true;
        }

        /**
         * Returns the figcaptionContentFound.
         * 
         * @return the figcaptionContentFound
         */
        public boolean hasFigcaptionContent() {
            return figcaptionContentFound;
        }

        /**
         * Sets the figcaptionContentFound.
         */
        public void setFigcaptionContentFound() {
            this.figcaptionContentFound = true;
        }

        /**
         * Returns the imagesLackingAlt
         * 
         * @return the imagesLackingAlt
         */
        public Set<Locator> getImagesLackingAlt() {
            return imagesLackingAlt;
        }

        /**
         * Adds to the imagesLackingAlt
         */
        public void addImageLackingAlt(Locator locator) {
            this.imagesLackingAlt.add(locator);
        }

        /**
         * Returns the optionNeeded.
         * 
         * @return the optionNeeded
         */
        public boolean isOptionNeeded() {
            return optionNeeded;
        }

        /**
         * Sets the optionNeeded.
         */
        public void setOptionNeeded() {
            this.optionNeeded = true;
        }

        /**
         * Returns the optionFound.
         * 
         * @return the optionFound
         */
        public boolean hasOption() {
            return optionFound;
        }

        /**
         * Sets the optionFound.
         */
        public void setOptionFound() {
            this.optionFound = true;
        }

        /**
         * Returns the noValueOptionFound.
         * 
         * @return the noValueOptionFound
         */
        public boolean hasNoValueOption() {
            return noValueOptionFound;
        }

        /**
         * Sets the noValueOptionFound.
         */
        public void setNoValueOptionFound() {
            this.noValueOptionFound = true;
        }

        /**
         * Returns the emptyValueOptionFound.
         * 
         * @return the emptyValueOptionFound
         */
        public boolean hasEmptyValueOption() {
            return emptyValueOptionFound;
        }

        /**
         * Sets the emptyValueOptionFound.
         */
        public void setEmptyValueOptionFound() {
            this.emptyValueOptionFound = true;
        }

        /**
         * Returns the nonEmptyOption.
         * 
         * @return the nonEmptyOption
         */
        public Locator nonEmptyOptionLocator() {
            return nonEmptyOption;
        }

        /**
         * Sets the nonEmptyOption.
         */
        public void setNonEmptyOption(Locator locator) {
            this.nonEmptyOption = locator;
        }

    }

    private StackNode[] stack;

    private int currentPtr;

    public Assertions() {
        super();
    }

    private void push(StackNode node) {
        currentPtr++;
        if (currentPtr == stack.length) {
            StackNode[] newStack = new StackNode[stack.length + 64];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[currentPtr] = node;
    }

    private StackNode pop() {
        return stack[currentPtr--];
    }

    private StackNode peek() {
        return stack[currentPtr];
    }

    private Map<StackNode, Locator> openSingleSelects = new HashMap<StackNode, Locator>();

    private Map<StackNode, Locator> openLabels = new HashMap<StackNode, Locator>();

    private Map<StackNode, TaintableLocatorImpl> openMediaElements = new HashMap<StackNode, TaintableLocatorImpl>();

    private Map<StackNode, Locator> openActiveDescendants = new HashMap<StackNode, Locator>();

    private LinkedHashSet<IdrefLocator> contextmenuReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> menuIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> formControlReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> formControlIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> listReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> listIds = new HashSet<String>();

    private LinkedHashSet<IdrefLocator> ariaReferences = new LinkedHashSet<IdrefLocator>();

    private Set<String> allIds = new HashSet<String>();

    private int currentFigurePtr;

    private boolean hasMain;

    /**
     * @see org.whattf.checker.Checker#endDocument()
     */
    @Override public void endDocument() throws SAXException {
        // contextmenu
        for (IdrefLocator idrefLocator : contextmenuReferences) {
            if (!menuIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201Ccontextmenu\u201D attribute must refer to a \u201Cmenu\u201D element.",
                        idrefLocator.getLocator());
            }
        }

        // label for
        for (IdrefLocator idrefLocator : formControlReferences) {
            if (!formControlIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201Cfor\u201D attribute of the \u201Clabel\u201D element must refer to a form control.",
                        idrefLocator.getLocator());
            }
        }

        // input list
        for (IdrefLocator idrefLocator : listReferences) {
            if (!listIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201Clist\u201D attribute of the \u201Cinput\u201D element must refer to a \u201Cdatalist\u201D element.",
                        idrefLocator.getLocator());
            }
        }

        // ARIA idrefs
        for (IdrefLocator idrefLocator : ariaReferences) {
            if (!allIds.contains(idrefLocator.getIdref())) {
                err(
                        "The \u201C"
                                + idrefLocator.getAdditional()
                                + "\u201D attribute must point to an element in the same document.",
                        idrefLocator.getLocator());
            }
        }

        reset();
        stack = null;
    }

    private static double getDoubleAttribute(Attributes atts, String name) {
        String str = atts.getValue("", name);
        if (str == null) {
            return Double.NaN;
        } else {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    /**
<<<<<<< HEAD
     * Utility function that returns the y-axis position that an object should be positioned at,
     * given the parent y-axis offset, the parents bounds, the height of the object, and
     * the required VPos.
     *
     * The BASELINE vpos doesn't make sense here, 0 is returned for it.
     */
    private static double positionY(double parentYOffset, Bounds parentBounds, double height, VPos vpos) {
        if (vpos == VPos.BOTTOM) {
            return parentYOffset + parentBounds.getMaxY();
        } else if (vpos == VPos.CENTER) {
            return parentYOffset + parentBounds.getMinY();
        } else if (vpos == VPos.TOP) {
            return parentYOffset + parentBounds.getMinY() - height;
        } else {
            return 0;
=======
     * @see org.whattf.checker.Checker#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override public void endElement(String uri, String localName, String name)
            throws SAXException {
        StackNode node = pop();
        Locator locator = null;
        openSingleSelects.remove(node);
        openLabels.remove(node);
        openMediaElements.remove(node);
        if ("http://www.w3.org/1999/xhtml" == uri) {
            if ("figure" == localName) {
                if ((node.needsFigcaption() && !node.hasFigcaptionContent())
                        || node.hasTextNode() || node.hasEmbeddedContent()) {
                    for (Locator imgLocator : node.getImagesLackingAlt()) {
                        err("An \u201Cimg\u201D element must have an"
                                + " \u201Calt\u201D attribute, except under"
                                + " certain conditions. For details, consult"
                                + " guidance on providing text alternatives"
                                + " for images.", imgLocator);
                    }
                }
            } else if ("select" == localName && node.isOptionNeeded()) {
                if (!node.hasOption()) {
                    err("A \u201Cselect\u201D element with a"
                            + " \u201Crequired\u201D attribute and without a"
                            + " \u201Cmultiple\u201D attribute, and whose size"
                            + " is \u201C1\u201D, must have a child"
                            + " \u201Coption\u201D element.");
                }
                if (node.nonEmptyOptionLocator() != null) {
                    err("The first child \u201Coption\u201D element of a"
                            + " \u201Cselect\u201D element with a"
                            + " \u201Crequired\u201D attribute and without a"
                            + " \u201Cmultiple\u201D attribute, and whose size"
                            + " is \u201C1\u201D, must have either an empty"
                            + " \u201Cvalue\u201D attribute, or must have no"
                            + " text content.", node.nonEmptyOptionLocator());
                }
            } else if ("option" == localName && !stack[currentPtr].hasOption()) {
                stack[currentPtr].setOptionFound();
            }
        }
        if ((locator = openActiveDescendants.remove(node)) != null) {
            warn(
                    "Attribute \u201Caria-activedescendant\u201D value should "
                    + "either refer to a descendant element, or should "
                    + "be accompanied by attribute \u201Caria-owns\u201D "
                    + "that includes the same value.",
                    locator);
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    /**
<<<<<<< HEAD
     * To facilitate multiple types of parent object, we unfortunately must allow for
     * Objects to be passed in. This method handles determining the bounds of the
     * given Object. If the Object type is not supported, a default Bounds will be returned.
     */
    private static Bounds getBounds(Object obj) {
        if (obj instanceof Node) {
            return ((Node)obj).localToScene(((Node)obj).getBoundsInLocal());
        } else if (obj instanceof Window) {
            final Window window = (Window)obj;
            return new BoundingBox(0, 0, window.getWidth(), window.getHeight());
        } else {
            return new BoundingBox(0, 0, 0, 0);
        }
    }

    /*
     * Simple utitilty function to return the 'opposite' value of a given HPos, taking
     * into account the current VPos value. This is used to try and avoid overlapping.
     */
    private static HPos getHPosOpposite(HPos hpos, VPos vpos) {
        if (vpos == VPos.CENTER) {
            if (hpos == HPos.LEFT){
                return HPos.RIGHT;
            } else if (hpos == HPos.RIGHT){
                return HPos.LEFT;
            } else if (hpos == HPos.CENTER){
                return HPos.CENTER;
            } else {
                // by default center for now
                return HPos.CENTER;
            }
        } else {
            return HPos.CENTER;
        }
    }

    /*
     * Simple utitilty function to return the 'opposite' value of a given VPos, taking
     * into account the current HPos value. This is used to try and avoid overlapping.
     */
    private static VPos getVPosOpposite(HPos hpos, VPos vpos) {
        if (hpos == HPos.CENTER) {
            if (vpos == VPos.BASELINE){
                return VPos.BASELINE;
            } else if (vpos == VPos.BOTTOM){
                return VPos.TOP;
            } else if (vpos == VPos.CENTER){
                return VPos.CENTER;
            } else if (vpos == VPos.TOP){
                return VPos.BOTTOM;
            } else {
                // by default center for now
                return VPos.CENTER;
            }
        } else {
            return VPos.CENTER;
        }
    }

    public static boolean hasFullScreenStage(final Screen screen) {
        final List<Stage> allStages = StageHelper.getStages();

        for (final Stage stage: allStages) {
            if (stage.isFullScreen() && (getScreen(stage) == screen)) {
                return true;
            }
        }

        return false;
    }

    /*
     * Returns true if the primary Screen has VGA dimensions, in landscape or portrait mode.
     */
    public static boolean isVGAScreen() {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        return ((bounds.getWidth() == 640 && bounds.getHeight() == 480) ||
                (bounds.getWidth() == 480 && bounds.getHeight() == 640));
    }

    /*
     * Returns true if the primary Screen has QVGA dimensions, in landscape or portrait mode.
     */
    public static boolean isQVGAScreen() {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        return ((bounds.getWidth() == 320 && bounds.getHeight() == 240) ||
                (bounds.getWidth() == 240 && bounds.getHeight() == 320));
    }

    /**
     * This function attempts to determine the best screen given the parent object
     * from which we are wanting to position another item relative to. This is particularly
     * important when we want to keep items from going off screen, and for handling
     * multiple monitor support.
     */
    public static Screen getScreen(Object obj) {
        // handle dual monitors (be careful of minX/minY vs width/height).
        // we create a rectangle representing the menubar menu item...
        final double offsetX = getOffsetX(obj);
        final double offsetY = getOffsetY(obj);
        final Bounds parentBounds = getBounds(obj);

        final Rectangle2D rect = new Rectangle2D(
                offsetX + parentBounds.getMinX(),
                offsetY + parentBounds.getMinY(),
                parentBounds.getWidth(),
                parentBounds.getHeight());

        return getScreenForRectangle(rect);
    }

    public static Screen getScreenForRectangle(final Rectangle2D rect) {
        final List<Screen> screens = Screen.getScreens();

        final double rectX0 = rect.getMinX();
        final double rectX1 = rect.getMaxX();
        final double rectY0 = rect.getMinY();
        final double rectY1 = rect.getMaxY();

        Screen selectedScreen;

        selectedScreen = null;
        double maxIntersection = 0;
        for (final Screen screen: screens) {
            final Rectangle2D screenBounds = screen.getBounds();
            final double intersection =
                    getIntersectionLength(rectX0, rectX1,
                                          screenBounds.getMinX(),
                                          screenBounds.getMaxX())
                        * getIntersectionLength(rectY0, rectY1,
                                                screenBounds.getMinY(),
                                                screenBounds.getMaxY());

            if (maxIntersection < intersection) {
                maxIntersection = intersection;
                selectedScreen = screen;
            }
        }

        if (selectedScreen != null) {
            return selectedScreen;
        }

        selectedScreen = Screen.getPrimary();
        double minDistance = Double.MAX_VALUE;
        for (final Screen screen: screens) {
            final Rectangle2D screenBounds = screen.getBounds();
            final double dx = getOuterDistance(rectX0, rectX1,
                                               screenBounds.getMinX(),
                                               screenBounds.getMaxX());
            final double dy = getOuterDistance(rectY0, rectY1,
                                               screenBounds.getMinY(),
                                               screenBounds.getMaxY());
            final double distance = dx * dx + dy * dy;

            if (minDistance > distance) {
                minDistance = distance;
                selectedScreen = screen;
            }
        }

        return selectedScreen;
    }

    public static Screen getScreenForPoint(final double x, final double y) {
        final List<Screen> screens = Screen.getScreens();

        // first check whether the point is inside some screen
        for (final Screen screen: screens) {
            // can't use screen.bounds.contains, because it returns true for
            // the min + width point
            final Rectangle2D screenBounds = screen.getBounds();
            if ((x >= screenBounds.getMinX())
                    && (x < screenBounds.getMaxX())
                    && (y >= screenBounds.getMinY())
                    && (y < screenBounds.getMaxY())) {
                return screen;
            }
        }

        // the point is not inside any screen, find the closest screen now
        Screen selectedScreen = Screen.getPrimary();
        double minDistance = Double.MAX_VALUE;
        for (final Screen screen: screens) {
            final Rectangle2D screenBounds = screen.getBounds();
            final double dx = getOuterDistance(screenBounds.getMinX(),
                                               screenBounds.getMaxX(),
                                               x);
            final double dy = getOuterDistance(screenBounds.getMinY(),
                                               screenBounds.getMaxY(),
                                               y);
            final double distance = dx * dx + dy * dy;
            if (minDistance >= distance) {
                minDistance = distance;
                selectedScreen = screen;
            }
        }

        return selectedScreen;
    }

    private static double getIntersectionLength(
            final double a0, final double a1,
            final double b0, final double b1) {
        // (a0 <= a1) && (b0 <= b1)
        return (a0 <= b0) ? getIntersectionLengthImpl(b0, b1, a1)
                          : getIntersectionLengthImpl(a0, a1, b1);
    }

    private static double getIntersectionLengthImpl(
            final double v0, final double v1, final double v) {
        // (v0 <= v1)
        if (v <= v0) {
            return 0;
        }

        return (v <= v1) ? v - v0 : v1 - v0;
    }

    private static double getOuterDistance(
            final double a0, final double a1,
            final double b0, final double b1) {
        // (a0 <= a1) && (b0 <= b1)
        if (a1 <= b0) {
            return b0 - a1;
        }

        if (b1 <= a0) {
            return b1 - a0;
        }

        return 0;
    }

    private static double getOuterDistance(final double v0,
                                           final double v1,
                                           final double v) {
        // (v0 <= v1)
        if (v <= v0) {
            return v0 - v;
        }

        if (v >= v1) {
            return v - v1;
        }

        return 0;
    }

    /***************************************************************************
     *                                                                         *
     * Miscellaneous utilities                                                 *
     *                                                                         *
     **************************************************************************/

    public static boolean assertionEnabled() {
        boolean assertsEnabled = false;
        assert assertsEnabled = true;  // Intentional side-effect !!!

        return assertsEnabled;
    }

    /**
     * Returns true if the operating system is a form of Windows.
     */
    public static boolean isWindows(){
        return PlatformUtil.isWindows();
    }

    /**
     * Returns true if the operating system is a form of Mac OS.
     */
    public static boolean isMac(){
        return PlatformUtil.isMac();
    }

    /**
     * Returns true if the operating system is a form of Unix, including Linux.
     */
    public static boolean isUnix(){
        return PlatformUtil.isUnix();
    }

    /***************************************************************************
     *                                                                         *
     * Unicode-related utilities                                               *
     *                                                                         *
     **************************************************************************/

    public static String convertUnicode(String src) {
        /** The input buffer, index of next character to be read,
         *  index of one past last character in buffer.
         */
        char[] buf;
        int bp;
        int buflen;

        /** The current character.
         */
        char ch;

        /** The buffer index of the last converted unicode character
         */
        int unicodeConversionBp = -1;
        
        buf = src.toCharArray();
        buflen = buf.length;
        bp = -1;

        char[] dst = new char[buflen];
        int dstIndex = 0;

        while (bp < buflen - 1) {
            ch = buf[++bp];
            if (ch == '\\') {
                if (unicodeConversionBp != bp) {
                    bp++; ch = buf[bp];
                    if (ch == 'u') {
                        do {
                            bp++; ch = buf[bp];
                        } while (ch == 'u');
                        int limit = bp + 3;
                        if (limit < buflen) {
                            char c = ch;
                            int result = Character.digit(c, 16);
                            if (result >= 0 && c > 0x7f) {
                                //lexError(pos+1, "illegal.nonascii.digit");
                                ch = "0123456789abcdef".charAt(result);
                            }
                            int d = result;
                            int code = d;
                            while (bp < limit && d >= 0) {
                                bp++; ch = buf[bp];
                                char c1 = ch;
                                int result1 = Character.digit(c1, 16);
                                if (result1 >= 0 && c1 > 0x7f) {
                                    //lexError(pos+1, "illegal.nonascii.digit");
                                    ch = "0123456789abcdef".charAt(result1);
                                }
                                d = result1;
                                code = (code << 4) + d;
                            }
                            if (d >= 0) {
                                ch = (char)code;
                                unicodeConversionBp = bp;
                            }
                        }
                        //lexError(bp, "illegal.unicode.esc");
                    } else {
                        bp--;
                        ch = '\\';
                    }
                }
            }
            dst[dstIndex++] = ch;
        }
        
        return new String(dst, 0, dstIndex);
    }
=======
     * @see org.whattf.checker.Checker#startDocument()
     */
    @Override public void startDocument() throws SAXException {
        reset();
        stack = new StackNode[32];
        currentPtr = 0;
        currentFigurePtr = -1;
        stack[0] = null;
        hasMain = false;
    }

    public void reset() {
        openSingleSelects.clear();
        openLabels.clear();
        openMediaElements.clear();
        openActiveDescendants.clear();
        contextmenuReferences.clear();
        menuIds.clear();
        formControlReferences.clear();
        formControlIds.clear();
        listReferences.clear();
        listIds.clear();
        ariaReferences.clear();
        allIds.clear();
    }

    /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override public void startElement(String uri, String localName,
            String name, Attributes atts) throws SAXException {
        Set<String> ids = new HashSet<String>();
        String role = null;
        String activeDescendant = null;
        String owns = null;
        String forAttr = null;
        boolean href = false;
        boolean activeDescendantInAriaOwns = false;

        StackNode parent = peek();
        int ancestorMask = 0;
        String parentRole = null;
        String parentName = null;
        if (parent != null) {
            ancestorMask = parent.getAncestorMask();
            parentName = parent.getName();
            parentRole = parent.getRole();
        }
        if ("http://www.w3.org/1999/xhtml" == uri) {
            boolean controls = false;
            boolean hidden = false;
            boolean add = false;
            boolean toolbar = false;
            boolean usemap = false;
            boolean ismap = false;
            boolean selected = false;
            boolean itemid = false;
            boolean itemref = false;
            boolean itemscope = false;
            boolean itemtype = false;
            boolean languageJavaScript = false;
            boolean typeNotTextJavaScript = false;
            String xmlLang = null;
            String lang = null;
            String id = null;
            String contextmenu = null;
            String list = null;

            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                String attUri = atts.getURI(i);
                if (attUri.length() == 0) {
                    String attLocal = atts.getLocalName(i);
                    if ("href" == attLocal) {
                        href = true;
                    } else if ("controls" == attLocal) {
                        controls = true;
                    } else if ("type" == attLocal && "param" != localName
                            && "ol" != localName && "ul" != localName
                            && "li" != localName) {
                        String attValue = atts.getValue(i);
                        if (lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "hidden", attValue)) {
                            hidden = true;
                        } else if (lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "toolbar", attValue)) {
                            toolbar = true;
                        }

                        if (!lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                "text/javascript", attValue)) {
                            typeNotTextJavaScript = true;
                        }
                    } else if ("role" == attLocal) {
                        role = atts.getValue(i);
                    } else if ("aria-activedescendant" == attLocal) {
                        activeDescendant = atts.getValue(i);
                    } else if ("aria-owns" == attLocal) {
                        owns = atts.getValue(i);
                    } else if ("list" == attLocal) {
                        list = atts.getValue(i);
                    } else if ("lang" == attLocal) {
                        lang = atts.getValue(i);
                    } else if ("id" == attLocal) {
                        id = atts.getValue(i);
                    } else if ("for" == attLocal && "label" == localName) {
                        forAttr = atts.getValue(i);
                        ancestorMask |= LABEL_FOR_MASK;
                    } else if ("contextmenu" == attLocal) {
                        contextmenu = atts.getValue(i);
                    } else if ("ismap" == attLocal) {
                        ismap = true;
                    } else if ("selected" == attLocal) {
                        selected = true;
                    } else if ("usemap" == attLocal && "input" != localName) {
                        usemap = true;
                    } else if ("itemid" == attLocal) {
                        itemid = true;
                    } else if ("itemref" == attLocal) {
                        itemref = true;
                    } else if ("itemscope" == attLocal) {
                        itemscope = true;
                    } else if ("itemtype" == attLocal) {
                        itemtype = true;
                    } else if ("language" == attLocal
                            && lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                    "javascript", atts.getValue(i))) {
                        languageJavaScript = true;
                    } else if (OBSOLETE_ATTRIBUTES.containsKey(attLocal)
                            && "ol" != localName && "ul" != localName
                            && "li" != localName) {
                        String[] elementNames = OBSOLETE_ATTRIBUTES.get(attLocal);
                        Arrays.sort(elementNames);
                        if (Arrays.binarySearch(elementNames, localName) >= 0) {
                            String suggestion = OBSOLETE_ATTRIBUTES_MSG.containsKey(attLocal) ? " "
                                    + OBSOLETE_ATTRIBUTES_MSG.get(attLocal)
                                    : "";
                            err("The \u201C" + attLocal
                                    + "\u201D attribute on the \u201C"
                                    + localName + "\u201D element is obsolete."
                                    + suggestion);
                        }
                    } else if (OBSOLETE_STYLE_ATTRS.containsKey(attLocal)) {
                        String[] elementNames = OBSOLETE_STYLE_ATTRS.get(attLocal);
                        Arrays.sort(elementNames);
                        if (Arrays.binarySearch(elementNames, localName) >= 0) {
                            err("The \u201C"
                                    + attLocal
                                    + "\u201D attribute on the \u201C"
                                    + localName
                                    + "\u201D element is obsolete. Use CSS instead.");
                        }
                    } else if ("dropzone" == attLocal) {
                        String[] tokens = atts.getValue(i).toString().split(
                                "[ \\t\\n\\f\\r]+");
                        Arrays.sort(tokens);
                        for (int j = 0; j < tokens.length; j++) {
                            String keyword = tokens[j];
                            if (j > 0 && keyword.equals(tokens[j - 1])) {
                                err("Duplicate keyword " + keyword
                                        + ". Each keyword must be unique.");
                            }
                        }
                    }
                } else if ("http://www.w3.org/XML/1998/namespace" == attUri) {
                    if ("lang" == atts.getLocalName(i)) {
                        xmlLang = atts.getValue(i);
                    }
                }

                if (atts.getType(i) == "ID") {
                    String attVal = atts.getValue(i);
                    if (attVal.length() != 0) {
                        ids.add(attVal);
                    }
                }
            }

            if ("figure" == localName) {
                currentFigurePtr = currentPtr + 1;
            }
            if ((ancestorMask & FIGURE_MASK) != 0) {
                if ("img" == localName) {
                    if (stack[currentFigurePtr].hasImg()) {
                        stack[currentFigurePtr].setEmbeddedContentFound();
                    } else {
                        stack[currentFigurePtr].setImgFound();
                    }
                } else if ("audio" == localName || "canvas" == localName
                        || "embed" == localName || "iframe" == localName
                        || "math" == localName || "object" == localName
                        || "svg" == localName || "video" == localName) {
                    stack[currentFigurePtr].setEmbeddedContentFound();
                }
            }

            if ("option" == localName && !parent.hasOption()) {
                if (atts.getIndex("", "value") < 0) {
                    parent.setNoValueOptionFound();
                } else if (atts.getIndex("", "value") > -1
                        && "".equals(atts.getValue("", "value"))) {
                    parent.setEmptyValueOptionFound();
                } else {
                    parent.setNonEmptyOption((new LocatorImpl(
                            getDocumentLocator())));
                }
            }

            // Obsolete elements
            if (OBSOLETE_ELEMENTS.get(localName) != null) {
                err("The \u201C" + localName + "\u201D element is obsolete. "
                        + OBSOLETE_ELEMENTS.get(localName));
            }

            // Exclusions
            Integer maskAsObject;
            int mask = 0;
            String descendantUiString = "";
            if ((maskAsObject = ANCESTOR_MASK_BY_DESCENDANT.get(localName)) != null) {
                mask = maskAsObject.intValue();
                descendantUiString = localName;
            } else if ("video" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "video\u201D with the attribute \u201Ccontrols";
            } else if ("audio" == localName && controls) {
                mask = A_BUTTON_MASK;
                descendantUiString = "audio\u201D with the attribute \u201Ccontrols";
            } else if ("menu" == localName && toolbar) {
                mask = A_BUTTON_MASK;
                descendantUiString = "menu\u201D with the attribute \u201Ctype=toolbar";
            } else if ("img" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "img\u201D with the attribute \u201Cusemap";
            } else if ("object" == localName && usemap) {
                mask = A_BUTTON_MASK;
                descendantUiString = "object\u201D with the attribute \u201Cusemap";
            } else if ("input" == localName && !hidden) {
                mask = A_BUTTON_MASK;
                descendantUiString = "input";
            }
            if (mask != 0) {
                int maskHit = ancestorMask & mask;
                if (maskHit != 0) {
                    for (int j = 0; j < SPECIAL_ANCESTORS.length; j++) {
                        if ((maskHit & 1) != 0) {
                            err("The element \u201C"
                                    + descendantUiString
                                    + "\u201D must not appear as a descendant of the \u201C"
                                    + SPECIAL_ANCESTORS[j] + "\u201D element.");
                        }
                        maskHit >>= 1;
                    }
                }
            }

            // Ancestor requirements/restrictions
            if ("area" == localName && ((ancestorMask & MAP_MASK) == 0)) {
                err("The \u201Carea\u201D element must have a \u201Cmap\u201D ancestor.");
            } else if ("img" == localName) {
                String titleVal = atts.getValue("", "title");
                if (ismap && ((ancestorMask & HREF_MASK) == 0)) {
                    err("The \u201Cimg\u201D element with the "
                            + "\u201Cismap\u201D attribute set must have an "
                            + "\u201Ca\u201D ancestor with the "
                            + "\u201Chref\u201D attribute.");
                }
                if (atts.getIndex("", "alt") < 0) {
                    if (w3cBranding || (titleVal == null || "".equals(titleVal))) {
                        if ((ancestorMask & FIGURE_MASK) == 0) {
                            err("An \u201Cimg\u201D element must have an"
                                    + " \u201Calt\u201D attribute, except under"
                                    + " certain conditions. For details, consult"
                                    + " guidance on providing text alternatives"
                                    + " for images.");
                        } else {
                            stack[currentFigurePtr].setFigcaptionNeeded();
                            stack[currentFigurePtr].addImageLackingAlt(new LocatorImpl(
                                    getDocumentLocator()));
                        }
                    }
                }
            } else if ("input" == localName || "button" == localName
                    || "select" == localName || "textarea" == localName
                    || "keygen" == localName) {
                for (Map.Entry<StackNode, Locator> entry : openLabels.entrySet()) {
                    StackNode node = entry.getKey();
                    Locator locator = entry.getValue();
                    if (node.isLabeledDescendants()) {
                        err("The \u201Clabel\u201D element may contain at most one \u201Cinput\u201D, \u201Cbutton\u201D, \u201Cselect\u201D, \u201Ctextarea\u201D, or \u201Ckeygen\u201D descendant.");
                        warn(
                                "\u201Clabel\u201D element with multiple labelable descendants.",
                                locator);
                    } else {
                        node.setLabeledDescendants();
                    }
                }
                if ((ancestorMask & LABEL_FOR_MASK) != 0) {
                    boolean hasMatchingFor = false;
                    for (int i = 0; (stack[currentPtr - i].getAncestorMask() & LABEL_FOR_MASK) != 0; i++) {
                        String forVal = stack[currentPtr - i].getForAttr();
                        if (forVal != null && forVal.equals(id)) {
                            hasMatchingFor = true;
                            break;
                        }
                    }
                    if (id == null || !hasMatchingFor) {
                        err("Any \u201C"
                                + localName
                                + "\u201D descendant of a \u201Clabel\u201D element with a \u201Cfor\u201D attribute must have an ID value that matches that \u201Cfor\u201D attribute.");
                    }
                }
            } else if ("table" == localName) {
                if (atts.getIndex("", "summary") >= 0) {
                    err("The \u201Csummary\u201D attribute is obsolete."
                            + " Consider describing the structure of the"
                            + " \u201Ctable\u201D in a \u201Ccaption\u201D "
                            + " element or in a \u201Cfigure\u201D element "
                            + " containing the \u201Ctable\u201D; or,"
                            + " simplify the structure of the"
                            + " \u201Ctable\u201D so that no description"
                            + " is needed.");
                }
                if (atts.getIndex("", "border") > -1
                        && (!("".equals(atts.getValue("", "border")) || "1".equals(atts.getValue(
                                "", "border"))))) {
                    err("The value of the \u201Cborder\u201D attribute"
                            + " on the \u201Ctable\u201D element"
                            + " must be either \u201C1\u201D or"
                            + " the empty string. To regulate the"
                            + " thickness of table borders, Use CSS instead.");
                }
            } else if ("track" == localName && atts.getIndex("", "default") >= 0) {
                for (Map.Entry<StackNode, TaintableLocatorImpl> entry : openMediaElements.entrySet()) {
                    StackNode node = entry.getKey();
                    TaintableLocatorImpl locator = entry.getValue();
                    if (node.isTrackDescendant()) {
                        err("The \u201Cdefault\u201D attribute must not occur"
                                + " on more than one \u201Ctrack\u201D element"
                                + " within the same \u201Caudio\u201D or"
                                + " \u201Cvideo\u201D element.");
                        if (!locator.isTainted()) {
                            warn("\u201Caudio\u201D or \u201Cvideo\u201D element"
                                    + " has more than one \u201Ctrack\u201D child"
                                    + " element with a \u201Cdefault\u201D attribute.",
                                    locator);
                            locator.markTainted();
                        }
                    } else {
                        node.setTrackDescendants();
                    }
                }
            } else if ("main" == localName) {
                if (hasMain) {
                    err("A document must not include more than one"
                            + " \u201Cmain\u201D element.");
                }
                hasMain = true;
            }

            // progress
            else if ("progress" == localName) {
                double value = getDoubleAttribute(atts, "value");
                if (!Double.isNaN(value)) {
                    double max = getDoubleAttribute(atts, "max");
                    if (Double.isNaN(max)) {
                        if (!(value <= 1.0)) {
                            err("The value of the  \u201Cvalue\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                        }
                    } else {
                        if (!(value <= max)) {
                            err("The value of the  \u201Cvalue\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                        }
                    }
                }
            }

            // meter
            else if ("meter" == localName) {
                double value = getDoubleAttribute(atts, "value");
                double min = getDoubleAttribute(atts, "min");
                double max = getDoubleAttribute(atts, "max");
                double optimum = getDoubleAttribute(atts, "optimum");
                double low = getDoubleAttribute(atts, "low");
                double high = getDoubleAttribute(atts, "high");

                if (!Double.isNaN(min) && !Double.isNaN(value)
                        && !(min <= value)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Cvalue\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(value) && !(0 <= value)) {
                    err("The value of the \u201Cvalue\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(value) && !Double.isNaN(max)
                        && !(value <= max)) {
                    err("The value of the \u201Cvalue\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(value) && Double.isNaN(max) && !(value <= 1)) {
                    err("The value of the \u201Cvalue\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(max) && !(min <= max)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(max) && !(0 <= max)) {
                    err("The value of the \u201Cmax\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && Double.isNaN(max) && !(min <= 1)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(low) && !(min <= low)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Clow\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(low) && !(0 <= low)) {
                    err("The value of the \u201Clow\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(high) && !(min <= high)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Chigh\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(high) && !(0 <= high)) {
                    err("The value of the \u201Chigh\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(low) && !Double.isNaN(high) && !(low <= high)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to the value of the \u201Chigh\u201D attribute.");
                }
                if (!Double.isNaN(high) && !Double.isNaN(max) && !(high <= max)) {
                    err("The value of the \u201Chigh\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(high) && Double.isNaN(max) && !(high <= 1)) {
                    err("The value of the \u201Chigh\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(low) && !Double.isNaN(max) && !(low <= max)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(low) && Double.isNaN(max) && !(low <= 1)) {
                    err("The value of the \u201Clow\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
                if (!Double.isNaN(min) && !Double.isNaN(optimum)
                        && !(min <= optimum)) {
                    err("The value of the \u201Cmin\u201D attribute must be less than or equal to the value of the \u201Coptimum\u201D attribute.");
                }
                if (Double.isNaN(min) && !Double.isNaN(optimum)
                        && !(0 <= optimum)) {
                    err("The value of the \u201Coptimum\u201D attribute must be greater than or equal to zero when the \u201Cmin\u201D attribute is absent.");
                }
                if (!Double.isNaN(optimum) && !Double.isNaN(max)
                        && !(optimum <= max)) {
                    err("The value of the \u201Coptimum\u201D attribute must be less than or equal to the value of the \u201Cmax\u201D attribute.");
                }
                if (!Double.isNaN(optimum) && Double.isNaN(max)
                        && !(optimum <= 1)) {
                    err("The value of the \u201Coptimum\u201D attribute must be less than or equal to one when the \u201Cmax\u201D attribute is absent.");
                }
            }

            // map required attrs
            else if ("map" == localName && id != null) {
                String nameVal = atts.getValue("", "name");
                if (nameVal != null && !nameVal.equals(id)) {
                    err("The \u201Cid\u201D attribute on a \u201Cmap\u201D element must have an the same value as the \u201Cname\u201D attribute.");
                }
            }

            // script
            else if ("script" == localName) {
                // script language
                if (languageJavaScript && typeNotTextJavaScript) {
                    err("A \u201Cscript\u201D element with the \u201Clanguage=\"JavaScript\"\u201D attribute set must not have a \u201Ctype\u201D attribute whose value is not \u201Ctext/javascript\u201D.");
                }
                // src-less script
                if (atts.getIndex("", "src") < 0) {
                    if (atts.getIndex("", "charset") >= 0) {
                        err("Element \u201Cscript\u201D must not have attribute \u201Ccharset\u201D unless attribute \u201Csrc\u201D is also specified.");
                    }
                    if (atts.getIndex("", "defer") >= 0) {
                        err("Element \u201Cscript\u201D must not have attribute \u201Cdefer\u201D unless attribute \u201Csrc\u201D is also specified.");
                    }
                    if (atts.getIndex("", "async") >= 0) {
                        err("Element \u201Cscript\u201D must not have attribute \u201Casync\u201D unless attribute \u201Csrc\u201D is also specified.");
                    }
                }
            }

            // bdo required attrs
            else if ("bdo" == localName && atts.getIndex("", "dir") < 0) {
                err("Element \u201Cbdo\u201D must have attribute \u201Cdir\u201D.");
            }

            // lang and xml:lang for XHTML5
            if (lang != null && xmlLang != null
                    && !equalsIgnoreAsciiCase(lang, xmlLang)) {
                err("When the attribute \u201Clang\u201D in no namespace and the attribute \u201Clang\u201D in the XML namespace are both present, they must have the same value.");
            }

            // contextmenu
            if (contextmenu != null) {
                contextmenuReferences.add(new IdrefLocator(new LocatorImpl(
                        getDocumentLocator()), contextmenu));
            }
            if ("menu" == localName) {
                menuIds.addAll(ids);
            }
            if ("datalist" == localName) {
                listIds.addAll(ids);
            }

            // label for
            if ("label" == localName) {
                String forVal = atts.getValue("", "for");
                if (forVal != null) {
                    formControlReferences.add(new IdrefLocator(new LocatorImpl(
                            getDocumentLocator()), forVal));
                }
            }
            if (("input" == localName && !hidden) || "textarea" == localName
                    || "select" == localName || "button" == localName
                    || "keygen" == localName || "output" == localName) {
                formControlIds.addAll(ids);
            }

            // input list
            if ("input" == localName && list != null) {
                listReferences.add(new IdrefLocator(new LocatorImpl(
                        getDocumentLocator()), list));
            }

            // input@type=button
            if ("input" == localName
                    && lowerCaseLiteralEqualsIgnoreAsciiCaseString("button",
                            atts.getValue("", "type"))) {
                if (atts.getValue("", "value") == null
                        || "".equals(atts.getValue("", "value"))) {
                    err("Element \u201Cinput\u201D with attribute \u201Ctype\u201D whose value is \u201Cbutton\u201D must have non-empty attribute \u201Cvalue\u201D.");
                }
            }

            // track
            if ("track" == localName) {
                if ("".equals(atts.getValue("", "label"))) {
                    err("Attribute \u201Clabel\u201D for element \u201Ctrack\u201D must have non-empty value.");
                }
            }

            // multiple selected options
            if ("option" == localName && selected) {
                for (Map.Entry<StackNode, Locator> entry : openSingleSelects.entrySet()) {
                    StackNode node = entry.getKey();
                    if (node.isSelectedOptions()) {
                        err("The \u201Cselect\u201D element cannot have more than one selected \u201Coption\u201D descendant unless the \u201Cmultiple\u201D attribute is specified.");
                    } else {
                        node.setSelectedOptions();
                    }
                }
            }
            if ("meta" == localName) {
                if (lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                        "content-language", atts.getValue("", "http-equiv"))) {
                    err("Using the \u201Cmeta\u201D element to specify the"
                        + " document-wide default language is obsolete."
                        + " Consider specifying the language on the root"
                        + " element instead.");
                }
            }

            // microdata
            if (itemid && !(itemscope && itemtype)) {
                err("The \u201Citemid\u201D attribute must not be specified on elements that do not have both an \u201Citemscope\u201D attribute and an \u201Citemtype\u201D attribute specified.");
            }
            if (itemref && !itemscope) {
                err("The \u201Citemref\u201D attribute must not be specified on elements that do not have an \u201Citemscope\u201D attribute specified.");
            }
            if (itemtype && !itemscope) {
                err("The \u201Citemtype\u201D attribute must not be specified on elements that do not have an \u201Citemscope\u201D attribute specified.");
            }
        } else {
            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                if (atts.getType(i) == "ID") {
                    String attVal = atts.getValue(i);
                    if (attVal.length() != 0) {
                        ids.add(attVal);
                    }
                }
                String attLocal = atts.getLocalName(i);
                if (atts.getURI(i).length() == 0) {
                    if ("role" == attLocal) {
                        role = atts.getValue(i);
                    } else if ("aria-activedescendant" == attLocal) {
                        activeDescendant = atts.getValue(i);
                    } else if ("aria-owns" == attLocal) {
                        owns = atts.getValue(i);
                    }
                }
            }

            allIds.addAll(ids);
        }

        // ARIA required parents
        Set<String> requiredParents = REQUIRED_ROLE_PARENT_BY_CHILD.get(role);
        if (requiredParents != null && !"presentation".equals(parentRole)
               && !"tbody".equals(localName) && !"tfoot".equals(localName)
               && !"thead".equals(localName)) {
            if (!requiredParents.contains(parentRole)) {
                err("An element with \u201Crole=" + role + "\u201D requires "
                        + renderRoleSet(requiredParents) + " on the parent.");
            }
        }

        // ARIA only allowed children
        Set<String> allowedChildren = ALLOWED_CHILD_ROLE_BY_PARENT.get(parentRole);
        if (allowedChildren != null && !"presentation".equals(parentRole) && !"presentation".equals(role)
               && !"tbody".equals(localName) && !"tfoot".equals(localName)
               && !"thead".equals(localName)) {
            if (!allowedChildren.contains(role)) {
                err("Only elements with "
                        + renderRoleSet(allowedChildren)
                        + " or \u201Crole=presentation\u201D"
                        + " are allowed as children of an element with \u201Crole="
                        + parentRole + "\u201D.");
            }
        }

        // ARIA row
        if ("row".equals(role)) {
            if (!("grid".equals(parentRole) || "treegrid".equals(parentRole) || (currentPtr > 1
                    && "grid".equals(stack[currentPtr - 1].getRole()) || "treegrid".equals(stack[currentPtr - 1].getRole())))) {
                err("An element with \u201Crole=row\u201D requires \u201Crole=treegrid\u201D or \u201Crole=grid\u201D on the parent or grandparent.");
            }
        }

        // ARIA IDREFS
        for (String att : MUST_NOT_DANGLE_IDREFS) {
            String attVal = atts.getValue("", att);
            if (attVal != null) {
                String[] tokens = AttributeUtil.split(attVal);
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i];
                    ariaReferences.add(new IdrefLocator(getDocumentLocator(),
                            token, att));
                }
            }
        }
        allIds.addAll(ids);

        // aria-activedescendant in aria-owns
        if (activeDescendant != null && !"".equals(activeDescendant)) {
            String activeDescendantVal = atts.getValue("",
                    "aria-activedescendant");
            if (owns != null && !"".equals(owns)) {
                String[] tokens = AttributeUtil.split(owns);
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i];
                    if (token.equals(activeDescendantVal)) {
                        activeDescendantInAriaOwns = true;
                        break;
                    }
                }
            }
        }
        // activedescendant
        for (Iterator<Map.Entry<StackNode, Locator>> iterator = openActiveDescendants.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<StackNode, Locator> entry = iterator.next();
            if (ids.contains(entry.getKey().getActiveDescendant())) {
                iterator.remove();
            }
        }

        if ("http://www.w3.org/1999/xhtml" == uri) {
            int number = specialAncestorNumber(localName);
            if (number > -1) {
                ancestorMask |= (1 << number);
            }
            if ("a" == localName && href) {
                ancestorMask |= HREF_MASK;
            }
            StackNode child = new StackNode(ancestorMask, localName, role,
                    activeDescendant, forAttr);
            if (activeDescendant != null && !activeDescendantInAriaOwns) {
                openActiveDescendants.put(child, new LocatorImpl(
                        getDocumentLocator()));
            }
            if ("select" == localName && atts.getIndex("", "multiple") == -1) {
                openSingleSelects.put(child, getDocumentLocator());
            } else if ("label" == localName) {
                openLabels.put(child, new LocatorImpl(getDocumentLocator()));
            } else if ("video" == localName || "audio" == localName ) {
                openMediaElements.put(child, new TaintableLocatorImpl(getDocumentLocator()));
            }
            push(child);
            if ("select" == localName && atts.getIndex("", "required") > -1
                    && atts.getIndex("", "multiple") < 0) {
                if (atts.getIndex("", "size") > -1) {
                    String size = trimSpaces(atts.getValue("", "size"));
                    if (!"".equals(size)) {
                        try {
                            if ((size.length() > 1 && size.charAt(0) == '+' && Integer.parseInt(size.substring(1)) == 1)
                                    || Integer.parseInt(size) == 1) {
                                child.setOptionNeeded();
                            } else {
                                // do nothing
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                } else {
                    // default size is 1
                    child.setOptionNeeded();
                }
            }
        } else {
            StackNode child = new StackNode(ancestorMask, null, role,
                    activeDescendant, forAttr);
            if (activeDescendant != null) {
                openActiveDescendants.put(child, new LocatorImpl(
                        getDocumentLocator()));
            }
            push(child);
        }

    }

    private void processChildContent(StackNode parent) throws SAXException {
        if (parent == null) {
            return;
        }
        parent.setChildren();
    }

    /**
     * @see org.whattf.checker.Checker#characters(char[], int, int)
     */
    @Override public void characters(char[] ch, int start, int length)
            throws SAXException {
        StackNode node = peek();
        for (int i = start; i < start + length; i++) {
            char c = ch[i];
            switch (c) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                default:
                    if ("figcaption".equals(node.name)
                            || (node.ancestorMask & FIGCAPTION_MASK) != 0) {
                        if ((node.ancestorMask & FIGURE_MASK) != 0) {
                            stack[currentFigurePtr].setFigcaptionContentFound();
                        }
                        // for any ancestor figures of the parent figure
                        // of this figcaption, the content of this
                        // figcaption counts as a text node descendant
                        for (int j = 1; j < currentFigurePtr; j++) {
                            if ("figure".equals(stack[currentFigurePtr - j].getName())) {
                                stack[currentFigurePtr - j].setTextNodeFound();
                            }
                        }
                    } else if ("figure".equals(node.name)
                            || (node.ancestorMask & FIGURE_MASK) != 0) {
                        stack[currentFigurePtr].setTextNodeFound();
                        // for any ancestor figures of this figure, this
                        // also counts as a text node descendant
                        for (int k = 1; k < currentFigurePtr; k++) {
                            if ("figure".equals(stack[currentFigurePtr - k].getName())) {
                                stack[currentFigurePtr - k].setTextNodeFound();
                            }
                        }
                    } else if ("option".equals(node.name)
                            && !stack[currentPtr - 1].hasOption()
                            && (!stack[currentPtr - 1].hasEmptyValueOption() || stack[currentPtr - 1].hasNoValueOption())
                            && stack[currentPtr - 1].nonEmptyOptionLocator() == null) {
                        stack[currentPtr - 1].setNonEmptyOption((new LocatorImpl(
                                getDocumentLocator())));
                    }
                    processChildContent(node);
                    return;
            }
        }
    }

    private CharSequence renderRoleSet(Set<String> requiredParents) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String role : requiredParents) {
            if (first) {
                first = false;
            } else {
                sb.append(" or ");
            }
            sb.append("\u201Crole=");
            sb.append(role);
            sb.append('\u201D');
        }
        return sb;
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

