/**
 * 
 */
package org.pentaho.pat.client.util.factory.charts.util;

import org.gwtwidgets.client.style.Color;

/**
 * 
 * @author tom(at)wamonline.org.uk
 *
 */
public class ChartUtils {

    /**
     * 
     * Checks if a string is able to be parsed to an integer.
     * 
     * @param i
     *            The string.
     * @return A boolean.
     */
    public static boolean isParsableToInt(final String i) {
        try {
            Integer.parseInt(i);
            return true;
        } catch (final NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * 
     * Create a random color for various chart objects.
     * 
     * @return A hex color value.
     */
    public static String getRandomColor() {
        final double r = Math.random() * 256;
        final double g = Math.random() * 256;
        final double b = Math.random() * 256;
        final Color myColor = new Color((int) r, (int) g, (int) b);
        return myColor.getHexValue();
    }
}

