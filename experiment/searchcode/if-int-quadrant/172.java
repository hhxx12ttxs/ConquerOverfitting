/*
 * ImageUtils.java
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
package org.ingatan.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.geotools.renderer.geom.Arrow2D;

/**
 * This class contains some methods used by the image editor as well as some common
 * IO operations (save/load image, etc.). Nothing special here. Clipboard stuf...
 * 
 * @author Thomas Everingham
 * @version 1.0
 */
public class ImageUtils {

    /**
     * Access to the system clipboard.
     */
    final static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    /**
     * Creates a rectangle describing the closest bounds that fit the given aspect ratio,
     * based on the specified bounds. The bounds are corrected by increasing either the
     * height or width.
     * @param bounds the bounds to be corrected.
     * @param aspectRatio the aspect ratio to which the given bounds should be corrected.
     * @return the corrected bounds with an aspect ratio equal to that provided.
     */
    public static Rectangle2D setBoundsToAspectRatio(Rectangle2D bounds, double aspectRatio) {
        //the current slope for the bounds
        double currentSlope = bounds.getHeight() / bounds.getWidth();
        //we want the slope to be equal to the aspect ratio.
        if (currentSlope == aspectRatio) {
            return bounds;
        }

        if (currentSlope < aspectRatio) {
            //make bounds taller
            return new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getWidth() * aspectRatio);
        } else if (currentSlope > aspectRatio) {
            //make bounds wider
            return new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getHeight() / aspectRatio, bounds.getHeight());
        }
        //will never reach this
        return bounds;
    }

    /**
     * Flood fills an area with the specified <code>newColour</code>. This flood fill is 4-way, not 8,
     * so diagonal lines of a different colour to <code>colourToReplace</code> will stop the
     * flood fill.
     * @param img the image upon which the flood fill should occur.
     * @param x the x coordinate of the base point of the flood fill (i.e. where the user clicked)
     * @param y the y coordinate of the base point of the flood fill (i.e. where the user clicked)
     * @param colourToReplace the colour to replace (the colour of pixel (x,y) of <code>img</code>)
     * @param newColour the colour to flood this area with.
     * @param tolerance the tolerance used for the colourIsSimilar method.
     */
    public static void floodFill(BufferedImage img, int x, int y, Color colourToReplace, Color newColour, int tolerance) {
        //if the base pixel is already that colour, then the flood fill is complete.
        if (colourToReplace.equals(newColour)) {
            return;
        }
        //Queue for points which must be processed again later
        LinkedList pointQueue = new LinkedList();
        //the current point which is being processed
        Point curPoint = new Point(x, y);

        pointQueue.push(curPoint);

        while (pointQueue.isEmpty() == false) {
            x = (int) curPoint.getX();
            y = (int) curPoint.getY();

            if (imageContainsPoint(img, x, y)) {
                img.setRGB(x, y, newColour.getRGB());
            }

            if ((imageContainsPoint(img, x, y - 1)) && (colourIsSimilar(img.getRGB(x, y - 1), colourToReplace.getRGB(), newColour, tolerance))) {
                pointQueue.push(curPoint);
                curPoint = new Point(x, y - 1);
            } else if ((imageContainsPoint(img, x + 1, y)) && (colourIsSimilar(img.getRGB(x + 1, y), colourToReplace.getRGB(), newColour, tolerance))) {
                pointQueue.push(curPoint);
                curPoint = new Point(x + 1, y);
            } else if ((imageContainsPoint(img, x, y + 1)) && (colourIsSimilar(img.getRGB(x, y + 1), colourToReplace.getRGB(), newColour, tolerance))) {
                pointQueue.push(curPoint);
                curPoint = new Point(x, y + 1);
            } else if ((imageContainsPoint(img, x - 1, y)) && (colourIsSimilar(img.getRGB(x - 1, y), colourToReplace.getRGB(), newColour, tolerance))) {
                pointQueue.push(curPoint);
                curPoint = new Point(x - 1, y);
            } else {
                //can't go any further, so take the next point on the queue
                curPoint = (Point) pointQueue.pop();
            }
        }

    }

    /**
     * Tests the similarity of the two specified colours by finding the distance between their
     * RGB values in 3D space. If the distance is less than the specified <code>tolerance</code>,
     * then the result is <code>true</code>.
     * @param col1 the RGB value of the first colour.
     * @param col2 the RGB value of the second colour.
     * @param notCol if colour 1 is equal to this colour, then the method will return false. This may be set to <code>null</code> if this
     * feature is not required.
     * @param tolerance the longest distance between the colours that will still allow the method to return true.
     * @return <code>true</code> if the distance between the colours in 3D space is less than the specified <code>tolerance</code>.
     */
    public static boolean colourIsSimilar(int col1, int col2, Color notCol, int tolerance) {
        Color colour1 = new Color(col1);
        Color colour2 = new Color(col2);

        if (notCol != null) {
            if (colour1.equals(notCol)) {
                return false;
            }
        }

        int r1 = colour1.getRed();
        int g1 = colour1.getGreen();
        int b1 = colour1.getBlue();
        int r2 = colour2.getRed();
        int g2 = colour2.getGreen();
        int b2 = colour2.getBlue();

        double distance = Math.sqrt(Math.pow(r2 - r1, 2) + Math.pow(g2 - g1, 2) + Math.pow(b2 - b1, 2));

        if (distance < tolerance) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convenience method which returns the value from the BufferedImage's <code>getRGB</code>
     * method, by using the parameters point.getX() and point.getY().
     * @param img the image from which the RGB value should be taken
     * @param point the coordinate of the pixel whose RGB value we want
     * @return the RGB value of the pixel at the specified point
     */
    public static int getRGBFromPoint(BufferedImage img, Point point) {
        return img.getRGB((int) point.getX(), (int) point.getY());
    }

    /**
     * Checks whether the given point exists within the images bounds.
     * @param img the image of interest.
     * @param x the x coordinate of the point to check.
     * @param y the y cooridnate of the point to check.
     * @return whether or not (x,y) exists within the image bounds.
     */
    public static boolean imageContainsPoint(BufferedImage img, int x, int y) {
        Rectangle bounds = new Rectangle(0, 0, img.getWidth(), img.getHeight());
        return bounds.contains(x, y);
    }

    /**
     * Convenience method for the flood fill method which does not require the 'old' colour
     * to be specified.
     * @param img the image upon which the flood fill should occur.
     * @param point the base point of the flood fill (i.e. where the user clicked)
     * @param fillColour the colour to fill the area with
     * @param tolerance the tolerance as used by the colourIsSimilar method.
     */
    public static void floodFill(BufferedImage img, Point point, Color fillColour, int tolerance) {
        Color pointColour = new Color(img.getRGB((int) point.getX(), (int) point.getY()));

        floodFill(img, (int) point.getX(), (int) point.getY(), pointColour, fillColour, tolerance);
    }

    /**
     * returns the sub image of <code>imgParent</code> specified by the Shape <code>shapeSubImage</code>. Regardless of
     * the shape, the subimage will actually be the bounds of the shape. Any image that lays outside of the shape, but within
     * the shape's bounds will be painted with the background colour specified.
     * @param imgParent the image to take the sub image from.
     * @param shapeSubImage the shape defining what area of the parent image that the sub image should contain.
     * @param background the background colour to paint any image area laying between the bounds of the shape and the shape itself.
     * @return the sub image from <code>imgParent</code> contained by the <code>shapeSubImage</code> shape.
     */
    public static BufferedImage getSubImage(BufferedImage imgParent, Shape shapeSubImage, Color background) {
        /*
         * This method works as follows:
         * 1) get the sub-image of the parent image that is the rectangular bounds
         *    of the specified sub image shape.
         * 2) create an area which is the subtraction of the sub image shape from
         *    a rectangular shape of the size of the shape bounds.
         * 3) fill the resulting area with the specified background colour
         * 4) return the image.
         */
        Rectangle2D shapeBounds = shapeSubImage.getBounds2D();

        BufferedImage subImg = new BufferedImage((int) shapeBounds.getWidth(), (int) shapeBounds.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) subImg.createGraphics();
        g2d.drawImage(imgParent, 0, 0, (int) shapeBounds.getWidth(), (int) shapeBounds.getHeight(),
                (int) shapeBounds.getX(), (int) shapeBounds.getY(), (int) (shapeBounds.getX() + shapeBounds.getWidth()),
                (int) (shapeBounds.getY() + shapeBounds.getHeight()), null);

        //get rid of any area that is not contained by shapeSubImage.
        Rectangle2D transBounds = new Rectangle2D.Double(0.0, 0.0, shapeBounds.getWidth(), shapeBounds.getHeight());
        Shape transShape = AffineTransform.getTranslateInstance(-1 * shapeBounds.getX(), -1 * shapeBounds.getY()).createTransformedShape(shapeSubImage);

        Area bounds = new Area(transBounds);
        Area shape = new Area(transShape);
        bounds.subtract(shape);

        for (int i = 0; i < bounds.getBounds().getHeight(); i++) {
            for (int j = 0; j < bounds.getBounds().getWidth(); j++) {
                if (bounds.contains(j, i)) {
                    try {
                        subImg.setRGB(j, i, 0x8F1C1D);
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                }
            }
        }

        return subImg;
    }

    /**
     * Sets all pixels of the specified colour to tranpsarent.
     * @param image the image which should have its pixels replaced.
     * @param toReplace the colour which should be replaced by transparency.
     */
    public static void setColourTransparent(BufferedImage image, Color toReplace) {
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                if (image.getRGB(j, i) == toReplace.getRGB()) {
                    image.setRGB(j, i, 0x8F1C1C);
                }
            }
        }
    }

    /**
     * Undos the <code>setColourTransparent</code> method by replacing all transparent pixels
     * with the specified colour.
     * @param image the image which should have its transparent pixels replaced.
     * @param toReplace the colour with which the transparency should be replaced.
     */
    public static void setTransparencyOpaque(BufferedImage image, Color toReplace) {
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                if (image.getRGB(j, i) == 0x8F1C1C) {
                    image.setRGB(j, i, toReplace.getRGB());
                }
            }
        }
    }

    /**
     * Replaces all pixels in the specified area which are of colour <code>oldColour</code> with the
     * specified replacement colour <code>newColour</code>.
     * @param img the image on which to replace the colour.
     * @param area the area in which the colour should be replaced.
     * @param oldColour the colour to replace.
     * @param newColour the colour to replace the old colour.
     */
    public static void replaceColour(BufferedImage img, Shape area, Color oldColour, Color newColour) {
        for (int i = (int) area.getBounds().getX(); i < area.getBounds().getX() + area.getBounds().getWidth(); i++) {
            for (int j = (int) area.getBounds().getY(); j < area.getBounds().getY() + area.getBounds().getHeight(); j++) {
                try {
                    if (img.getRGB(i, j) == oldColour.getRGB()) {
                        img.setRGB(i, j, newColour.getRGB());
                    }
                } catch (ArrayIndexOutOfBoundsException ignore) {
                }
            }
        }
    }

    /**
     * Returns the angle formed by the points: vector1,origin,vector2. Which is the
     * angle between the lines vector1 and vector2. The origin is the common point shared
     * by these two lines, so each line is: (origin, vector1), and (origin, vector2).
     * @param origin the common point shared by both lines.
     * @param vector1 the point representing the first line (origin, vector1).
     * @param vector2 the point representing the second line (origin, vector2).
     * @return the angle formed by the points: vector 1, origin, vector2.
     */
    public static double getAngleBetweenLines(Point origin, Point vector1, Point vector2) {
        //get x,y components for each vector
        double vector1X = vector1.getX() - origin.getX();
        double vector1Y = vector1.getY() - origin.getY();

        double vector2X = vector2.getX() - origin.getX();
        double vector2Y = vector2.getY() - origin.getY();

        //get the vector magnitudes
        double vector1Length = Math.sqrt(Math.pow(vector1X, 2.0) + Math.pow(vector1Y, 2.0));
        double vector2Length = Math.sqrt(Math.pow(vector2X, 2.0) + Math.pow(vector2Y, 2.0));

        //get dot product
        double dotProduct = vector1X * vector2X + vector1Y * vector2Y;

        double angle = -1 * Math.acos(dotProduct / (vector1Length * vector2Length));

        //1st quadrant SE
        if ((vector2.x >= origin.x) && (vector2.y >= origin.y)) {
            return angle;
        } //2nd quadrant NE
        else if ((vector2.x > origin.x) && (vector2.y < origin.y)) {
            return angle;
        }//3rd quadrant NW
        else if ((vector2.x < origin.x) && (vector2.y < origin.y)) {
            return angle;//(angle + Math.PI/2);
        }//4th quadrant SW
        else if ((vector2.x < origin.x) && (vector2.y > origin.y)) {
            return (angle + Math.PI);
        }

        return angle;
    }

    /**
     * The <code>Arrow2D</code> class does not support arrow rotation, only horizontal, right facing arrows.
     * This method rotates an instance of <code>Arrow2D</code>, and translates and scales it so that it sits along the
     * line that has been provided.
     * @param arrowIn the arrow to be resized
     * @param line the line which sets the bounds for the rotate/scaling of the arrow. The arrow head
     *        will be placed at point 2 of the line (x2,y2), the end point.
     * @return a Path2D instance representing the rotated/scaled line geometry.
     */
    public static Path2D setArrowAlongLine(Arrow2D arrowIn, Line2D line) {
        Arrow2D arrow = (Arrow2D) arrowIn.clone();
        Point2D point1 = line.getP1();
        Point2D point2 = line.getP2();
        Path2D returnVal;
        int lineLength = 10;
        double angle = 0;
        double proportionalChangeInSize = 1.0;

        double requiredXOffset = 0;
        double requiredYOffset = 0;

        //get the length of the line and set the arrow to be that length BEFORE
        //carrying out the affine transform.
        lineLength = (int) (Math.sqrt(Math.pow(point2.getX() - point1.getX(), 2.0) + Math.pow((point2.getY() - point1.getY()), 2)));


        //if the line is too short, it will destroy the arrow - reason unknown
        if (lineLength < 4) {
            point2.setLocation(point1.getX() + 4, point1.getY() + 4);
            lineLength = (int) (Math.sqrt(Math.pow(point2.getX() - point1.getX(), 2.0) + Math.pow((point2.getY() - point1.getY()), 2)));
        }
        //before setting the new arrow frame, we should get the proportion increase in width and apply this also to the height -
        //this will ensure that the arrow head remains the same as it was before this operation.
        proportionalChangeInSize = ((double) lineLength) / arrow.getFrame().getWidth();

        //the proportion of the tail should be 1 - [the proportion of the original arrowhead length of the new line]
        arrow.setTailProportion(1 - (arrow.getHeadLength() / lineLength), arrow.getTailHeightProportion(), arrow.getTailHeightAtHeadProportion());

        arrow.setFrame(arrow.getFrame().getX(), arrow.getFrame().getY(), arrow.getFrame().getWidth() * proportionalChangeInSize,
                arrow.getFrame().getHeight());



        //get the angle of the line
        angle = Math.asin((point2.getY() - point1.getY()) / (lineLength));

        if ((point2.getX() < point1.getX()) && (point2.getY() <= point1.getY())) {
            angle = Math.asin((point1.getY() - point2.getY()) / (lineLength)) + Math.PI;
            //then we are in the upper left quadrant of the screen coorinate space
        } else if ((point2.getX() < point1.getX()) && (point2.getY() > point1.getY())) {
            //then we are in the lower left quadrant of the screen coorinate space
            angle = Math.asin((point1.getY() - point2.getY()) / (lineLength)) + Math.PI;
        }

        //rotate the arrow to the angle of the line.
        returnVal = (Path2D) AffineTransform.getRotateInstance(angle).createTransformedShape(arrow);

        //translate the arrow so that its start point and the line's start point coincide.
        requiredXOffset = (int) (line.getBounds().getX() - returnVal.getBounds().getX());
        requiredYOffset = (int) (line.getBounds().getY() - returnVal.getBounds().getY());

        returnVal = (Path2D) AffineTransform.getTranslateInstance(requiredXOffset, requiredYOffset).createTransformedShape(returnVal);


        return returnVal;
    }

    /**
     * Adjusts the coordinates of this line so that the line is rounded to the nearest
     * 45 degree angle.
     * @param x1 x coordinate of point 1 of the line
     * @param y1 y coordinate of point 1 of the line
     * @param x2 x cooridnate of point 2 of the line
     * @param y2 y coordinate of point 2 of the line
     * @param performOperation whether or not the operation should be performed. If this value is false,
     *          then this method returns the line that was described by (x1,y1)->(x2,y2). This is a convenience
     *          field which means you do not have to specify two line construction statements, one for straight,
     *          and one for normal line drawing.
     *
     * @return the straight version of <code>line</code> iff <code>performOperation</code> is true, otherwise returns the line
     *          represented by (x1,y1)->(x2,y2).
     */
    public static Line2D makeLineStraight(double x1, double y1, double x2, double y2, double threshold, boolean performOperation) {
        if (!performOperation) {
            return new Line2D.Double(x1, y1, x2, y2);
        }

        double xRun = Math.sqrt(Math.pow(x2 - x1, 2));
        double yRise = Math.sqrt(Math.pow(y2 - y1, 2));

        double average = ((xRun) + (yRise)) / 2;


        if ((xRun > threshold) && (yRise > threshold)) {
            //Then make the line diagonal.

            if ((y2 < y1) && (x2 > x1)) {
                x2 = x1 + average;
                y2 = y1 - average;
            } else if ((y2 > y1) && (x2 < x1)) {
                x2 = x1 - average;
                y2 = y1 + average;
            } else if ((y2 > y1) && (x2 > x1)) {
                x2 = x1 + average;
                y2 = y1 + average;
            } else if ((y2 < y1) && (x2 < x1)) {
                x2 = x1 - average;
                y2 = y1 - average;
            }
        } else if ((xRun <= threshold) && (yRise > threshold)) {
            //then make the line vertical
            x2 = x1;
        } else if ((xRun > threshold) && (yRise <= threshold)) {
            //then make the line horizontal
            y2 = y1;
        }

        return new Line2D.Double(x1, y1, x2, y2);
    }

    /**
     * When drawing a shape using the mouse, the user may draw rectangular bounds for the
     * shape that have negative widths or heights. This method corrects such a rectangle
     * so that widths and heights are positive. It does this by swapping the x,y and width,height
     * points were neccessary.<br>
     * <br>
     * The method can adjust further and ensure that the bounds are square. This is done by setting
     * <code>makeBoundsSquare</code>.<br>
     * <br>
     * The bounds are made square by finding the average side length, and setting this length as the
     * width and height. If point 2 is not below and to the right of point 1, then x and y of point 2
     * must be offset by the change in the height/width of the bounds. This is because point 2 is the mobile point,
     * and the desired predictable behaviour of drawing a shape from a point to a point will not be acheived if
     * point 1 moves.
     * @param point1 the point which currently represents the location of the bounds
     * @param point2 the point which currently represents the width and height relative to point 1
     * @param makeBoundsSquare whether or not the method should also modify the bounds so that it is square
     * @return the modified bounds
     */
    public static Rectangle2D mouseRectToLogicalRect(Point point1, Point point2, boolean makeBoundsSquare) {
        //point2 is the second position to have been generated
        if (makeBoundsSquare) {
            if ((point2.getX() < point1.getX()) && (point2.getY() < point1.getY())) {
                double average = ((point1.getX() - point2.getX()) + (point1.getY() - point2.getY())) / 2;
                //the xchange and ychange values are the difference between actual current side lenght and
                //the average side length.
                double xChange = (point1.getX() - point2.getX()) - average;
                double yChange = (point1.getY() - point2.getY()) - average;
                return new Rectangle2D.Double(point2.getX() + xChange, point2.getY() + yChange, average, average);
            } else if ((point2.getX() > point1.getX()) && (point2.getY() > point1.getY())) {
                double average = ((point2.getX() - point1.getX()) + (point2.getY() - point1.getY())) / 2;
                return new Rectangle2D.Double(point1.getX(), point1.getY(), average, average);
            } else if ((point2.getX() < point1.getX()) && (point2.getY() > point1.getY())) {
                double average = ((point1.getX() - point2.getX()) + (point2.getY() - point1.getY())) / 2;
                double xChange = (point1.getX() - point2.getX()) - average;
                return new Rectangle2D.Double(point2.getX() + xChange, point1.getY(), average, average);
            } else if ((point2.getX() > point1.getX()) && (point2.getY() < point1.getY())) {
                double average = ((point2.getX() - point1.getX()) + (point1.getY() - point2.getY())) / 2;
                double yChange = (point1.getY() - point2.getY() - average);
                return new Rectangle2D.Double(point1.getX(), point2.getY() + yChange, average, average);
            } else {
                return new Rectangle2D.Double(point1.getX(), point2.getY(), 1, 1);
            }
        } else {
            if ((point2.getX() < point1.getX()) && (point2.getY() < point1.getY())) {
                return new Rectangle2D.Double(point2.getX(), point2.getY(), (point1.getX() - point2.getX()), (point1.getY() - point2.getY()));
            } else if ((point2.getX() > point1.getX()) && (point2.getY() > point1.getY())) {
                return new Rectangle2D.Double(point1.getX(), point1.getY(), (point2.getX() - point1.getX()), (point2.getY() - point1.getY()));
            } else if ((point2.getX() < point1.getX()) && (point2.getY() > point1.getY())) {
                return new Rectangle2D.Double(point2.getX(), point1.getY(), (point1.getX() - point2.getX()), (point2.getY() - point1.getY()));
            } else if ((point2.getX() > point1.getX()) && (point2.getY() < point1.getY())) {
                return new Rectangle2D.Double(point1.getX(), point2.getY(), (point2.getX() - point1.getX()), (point1.getY() - point2.getY()));
            } else {
                return new Rectangle2D.Double(point1.getX(), point2.getY(), 1, 1);
            }
        }
    }

    /**
     * Copies the specified image to the clipboard with the imageFlavor data flavor. It may
     * not be possible for this data to be pasted in all other applications.
     * @param img the image to be copied to the clipboard.
     */
    public static void setImageToClipboard(BufferedImage img) {
        clipboard.setContents(new ImgTxtTransferable(img), null);
    }

    /**
     * Gets the current image from the clipboard. If the clipboard currently holds
     * text, this will be rendered to a buffered image first, in the defualt font.
     * To specify the font, use the <code>getImageFromClipboard(Font font)</code> method.
     * @param stringToImage Whether or not a string data flavour should be converted into an image and returned. <code>true</code> if
     * any string data on the clipboard should be drawn as an image and returned, <code>false</code> to do nothing with string data.
     * @param urlToImage if the data on the clipboard is string flavour, and this flag is <code>true</code>, the method will attempt to pull
     * the image resource from the provided URL.
     * @return returns the image currently held by the clipboard, or the render of the current
     * clipboard text.
     */
    public static BufferedImage getImageFromClipboard(boolean stringToImage, boolean urlToImage) throws UnsupportedFlavorException, IOException {

        //the return image
        Image imgTemp = null;
        BufferedImage img = null;


        final Transferable transferable = clipboard.getContents(null);
        if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            img = (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);

            return img;
        } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            String data = String.valueOf(transferable.getTransferData(DataFlavor.stringFlavor));

            //if we have a URL, then we might be able to download a picture
            URL url;
            try {
                url = new URL(data);
            } catch (MalformedURLException e) {
                url = null;
            }

            if ((url != null) && (urlToImage)) {
                img = ImageIO.read(url);
            }

            if (((url == null) || (img == null) || (urlToImage == false)) && (stringToImage)) {
                img = new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB);
                Rectangle2D textSize = img.getGraphics().getFontMetrics().getStringBounds(data, img.getGraphics());
                img = new BufferedImage((int) textSize.getWidth(), (int) textSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = img.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.black);
                g2d.drawString(data, 0, 10);
                return img;
            } else {
                return img;
            }
        } else {
            return null;
        }
    }

    /**
     * Save the given image to the file system
     * @param image the image to save.
     * @return true if the file has been saved successfully.
     */
    public static boolean saveImage(BufferedImage image, String filetype, String filename, Component parent) {
        try {
            ImageIO.write(image, filetype, new File(filename));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "An error occurred while attempting to save to '" + filename + "'.\n\n(class: ImageUtils)", "Save Image", JOptionPane.OK_OPTION);
            return false;
        }
        return true;
    }

    /**
     * Load an image from the specified file.
     * @param filename the filename of the image to load.
     * @return the BufferedImage containing the loaded image.
     */
    public static BufferedImage loadImage(String filename, Component parent) {
        BufferedImage retVal = null;
        try {

            retVal = ImageIO.read(new File(filename));
        } catch (Exception ignore) {
            Logger.getLogger(ImageUtils.class.getName()).log(Level.SEVERE, "While attempting to load image: " + filename,ignore);
        } catch (OutOfMemoryError e) {
            Logger.getLogger(ImageUtils.class.getName()).log(Level.SEVERE, "While attempting to load image: " + filename,e);
        }
        return retVal;
    }

    /**
     * Transferable implementation which takes either an image or text.
     */
    public static class ImgTxtTransferable implements Transferable {

        Image img = null;
        String txt = "";

        /**
         * Creates a new instance of this <code>Transferable</code> implementation.
         * @param img the image data to supply to the clipboard upon request.
         */
        public ImgTxtTransferable(Image img) {
            this.img = img;
        }

        /**
         * Creates a new instance of this <code>Transferable</code> implementation.
         * @param txt the text data to supply to the clipboard upon request.
         */
        public ImgTxtTransferable(String txt) {
            this.txt = txt;
        }
        /* returns the data flavors supported by this data transfer
         * we are dealing with images here and will support the image flavour
         * as well as the text flavour - text will be returned rendered to a
         * buffered image in either the font specified, or the default font
         * if none is specified.
         */

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.stringFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if ((flavor.equals(DataFlavor.imageFlavor)) || (flavor.equals(DataFlavor.stringFlavor))) {
                return true;
            } else {
                return false;
            }
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(DataFlavor.imageFlavor)) {
                return img;
            } else if (flavor.equals(DataFlavor.stringFlavor)) {
                return txt;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }
}

