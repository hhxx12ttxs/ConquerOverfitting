/*
 * ImageRotator.java
 *
 * Created on Utorok, 2005, november 22, 8:13
 *
 */

package eu.easyedu.jnetwalk.utils;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.image.BufferedImage;

/**
 * Class for rotating images.
 * @author hlavki
 */
public class ImageRotator {
    

    
    /** Creates a new instance of ImageRotator */
    
    private ImageRotator() {
    }
    
    /**
     * Rotates the specified image the specified number of degrees.  The
     * rotation is performed around the center point of the image.
     * @return the image
     * @see #NO_BOUNDING_BOX
     * @see #EXACT_BOUNDING_BOX
     * @see #LARGEST_BOUNDING_BOX
     * @param image Image to rotate
     * @param imageWidth Image width
     * @param imageHeight Image height
     * @param degrees the degrees to rotate
     * @param bbm the bounding box mode, default is EXACT_BOUNDING_BOX
     * @param background the background paint (texture, color or gradient),
     *                     can be null
     */
    public static BufferedImage rotateDegrees(Image image, int imageWidth, int imageHeight, 
            double degrees, BoundingBox bbm, Paint background) {
        return rotateRadians(image, imageWidth, imageHeight, Math.toRadians(degrees), bbm, background);
    }
    
    /**
     * Rotates the specified image the specified number of radians.  The
     * rotation is performed around the center point of the image.  This
     * method is provided for convenience of applications using radians.
     * For most people, degrees is simpler to use.
     * @return the image
     * @see #NO_BOUNDING_BOX
     * @see #EXACT_BOUNDING_BOX
     * @see #LARGEST_BOUNDING_BOX
     * @param image Image to rotate
     * @param imageWidth Image Width
     * @param imageHeight Image Height
     * @param radians the radians to rotate
     * @param bbm the bounding box mode, default is EXACT_BOUNDING_BOX
     * @param background the background paint (texture, color or gradient),
     *                     can be null
     */
    public static BufferedImage rotateRadians(Image image, int imageWidth, int imageHeight, 
            double radians, BoundingBox bbm, Paint background) {
        // get the original image's width and height
//        int iw = imageWidth; //image.getWidth(null);
//        int ih = imageHeight; //image.getHeight(null);
        // calculate the new image's size based on bounding box mode
        Dimension dim;
        if(bbm.equals(BoundingBox.NO_BOX)) {
            dim = new Dimension(imageWidth, imageHeight);
        } else if(bbm.equals(BoundingBox.LARGEST)) {
            dim = getLargestBoundingBox(imageWidth, imageHeight);
        } else { // EXACT_BOUNDING_BOX
            dim = getBoundingBox(imageWidth, imageHeight, Math.toDegrees(radians));
        }
        // get the new image's width and height
        int w = dim.width;
        int h = dim.height;
        // get the location to draw the original image on the new image
        int x = (w/2)-(imageWidth/2);
        int y = (h/2)-(imageHeight/2);
        // need to copy the given image to a new BufferedImage because
        // it is, in most cases, going to be a larger image so it
        // needs to be drawn centered on the larger image
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        // set some rendering hints for better looking images
        //g2d.setRenderingHints(renderingHints);
        // draw the background paint, if necessary
        if(background != null) {
            g2d.setPaint(background);
            g2d.fillRect(0, 0, w, h);
        }
        // if not rotating, just draw it normally, else create a transform
        if(radians == 0.0) {
            g2d.drawImage(image, x, y, imageWidth, imageHeight, null);
        } else {
            g2d.rotate(radians, w/2, h/2);
            g2d.translate(x, y);
            g2d.drawImage(image, 0, 0, imageWidth, imageHeight, null);
        }
        g2d.dispose();
        return bi;
    }
    
    /**
     * Gets the largest bounding box size that can hold an image of the
     * specified size at any angle of rotation.
     *
     * @param  width   the image width
     * @param  height  the image height
     * @return  the bounding box size
     */
    public static Dimension getLargestBoundingBox(int width, int height) {
        // The largest bounding box is the largest area needed to fit the
        // specified image at any angle or rotation.  This is simpler then
        // getting the bounding box for a given angle because the largest
        // box will put the corner of the image box directly along the
        // vertical or horizontal axis from the image center point.  The
        // distance between the image rectangle's center and any corner
        // is the hypotenuse of a right triangle who's other sides are
        // half the width (a) and half the height (b) of the rectangle.
        // A little a^2 + b^2 = c^2 calculation and we get the length of
        // the hypotenuse.  Double that to get a square within which the
        // image can be rotated at any angle without clipping the image.
        double a = (double)width / 2.0;
        double b = (double)height / 2.0;
        // use Math.ceil() to round up to an int value
        int c = (int)Math.ceil(Math.sqrt((a * a) + (b * b)) * 2.0);
        return new Dimension(c, c);
    }
    
    /**
     * Gets the optimal/smallest bounding box size that can hold an image of
     * the specified size at the specified angle of rotation.
     * @return the bounding box size
     * @param degrees Degrees
     * @param width the image width
     * @param height the image height
     */
    public static Dimension getBoundingBox(int width, int height, double degrees) {
//        degrees = normalizeDegrees(degrees);
        // if no rotation or 180 degrees, the size won't change
        if(degrees == 0.0 || degrees == 180.0) {
            return new Dimension(width, height);
        }
        // if 90 or 270 (quarter or 3-quarter rotations) the width becomes
        // the height, and vice versa
        if(degrees == 90.0 || degrees == 270.0) {
            return new Dimension(height, width);
        }
        // for any other rotation, we need to do some trigonometry,
        // derived from description found at:
        // http://www.codeproject.com/csharp/rotateimage.asp
        double radians = Math.toRadians(degrees);
        double aW = Math.abs(Math.cos(radians) * width);
        double oW = Math.abs(Math.sin(radians) * width);
        double aH = Math.abs(Math.cos(radians) * height);
        double oH = Math.abs(Math.sin(radians) * height);
        // use Math.ceil() to round up to an int value
        int w = (int)Math.ceil(aW + oH);
        int h = (int)Math.ceil(oW + aH);
        return new Dimension(w, h);
    }
}

