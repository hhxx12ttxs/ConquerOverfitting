package digitalmirror;

import java.awt.Color;
import java.util.Random;

import processing.core.PApplet;
import static processing.core.PApplet.*;
import static processing.core.PConstants.RGB;


public abstract class Utils {

	
	private static Random random = new Random();
	
	public static float random(double min, double max) {
		//if(r < min || r > max) System.out.println(min + " to " + max + "    -    " + r);
		return (float) PApplet.map(random.nextFloat(), 0, 1, (float) min, (float) max);
	}
	
	public static int randomSign() {
		return Math.round(random(0, 1)) == 0 ? -1 : 1;
	}
	
	public static double distanceBetweenTwoPoints(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}
	
	public static double distanceBetweenTwoPoints(double x1, double y1, double z1, double x2, double y2, double z2) {
		double xd = x2 - x1;
		double yd = y2 - y1;
		double zd = z2 - z1;
		return Math.sqrt(Math.pow(xd, 2) + Math.pow(yd, 2) + Math.pow(zd, 2));
	}

	public static double[] coordinatesOnLineBetweenTwoPoints(double lineStartX, double lineStartY, double lineEndX, double lineEndY, double magnitude) {
		//double distance = Math.sqrt(Math.pow(lineEndX - lineStartX, 2) + Math.pow(lineEndY - lineStartY, 2));
		double angle = Math.atan2(lineEndY - lineStartY, lineEndX - lineStartX);
		return new double[] { (lineStartX + Math.cos(angle) * magnitude), (lineStartY + Math.sin(angle) * magnitude)};
	}
	/**
	 * Rotating a vector using x1,y1 as point of rotation. The angle is in radians.
	 * 
	 * @param x1
	 * @param y1
	 * @param d
	 * @param y2
	 * @param angle	Radians.
	 * @return coords of the new vector from x1,y1 to the new point.
	 */
	public static double[] rotateVector(double x1, double y1, double d, double y2, double angle) {
		double ca = Math.cos(angle);
		double sa = Math.sin(angle);

		double relativeX = d - x1;
		double relativeY = y2 - y1;

		double newX = relativeX * ca - relativeY * sa;
		double newY = relativeX * sa + relativeY * ca;

		return new double[] { x1 + newX, y1 + newY };
	}


	public static double angleBetweenTwoVectors(double v1x1, double v1y1, double v1x2, double v1y2, double v2x1, double v2y1, double v2x2, double v2y2) {
		double v1dx = v1x2 - v1x1;
		double v1dy = v1y2 - v1y1;

		double v2dx = v2x2 - v2x1;
		double v2dy = v2y2 - v2y1;

		return Math.PI - (Math.atan2(v2dy, v2dx) - Math.atan2(v1dy, v1dx));
	}

	public static double[] coordinatesFromPointGivenAngleAndMagnitude(double x, double y, double angle, double magnitude) {
		double tmpX = x;
		double tmpY = y + magnitude;
		double tmpAngle = Math.toRadians(90) - angle;

		return rotateVector(x, y, tmpX, tmpY, tmpAngle);
	}
	
	/**
	 * Modify the given color using HSB values.
	 * @param color The color
	 * @param hue Change the hue. Eg. down 0.25 by giving -0.25
	 * @param saturation Saturation, same as hue.
	 * @param brightness Brightness, same as hue.
	 * @return
	 */
	public static Color modifyColorHSB(Color color, double hue, double saturation, double brightness) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		hsb[0] = constrain(hsb[0] + (float) hue, 0, 1);
		hsb[1] = constrain(hsb[1] + (float) saturation, 0, 1);
		hsb[2] = constrain(hsb[2] + (float) brightness, 0, 1);
		
		return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}

	public static Color interpolateColor(Color startColor, Color endColor, double amount) {
		int alpha = PApplet.constrain(Math.round(map((float) amount, 0, 1, startColor.getAlpha(), endColor.getAlpha())), 0, 255);
		int colorRGB = lerpColor(startColor.getRGB(), endColor.getRGB(), (float) amount, RGB);
		Color color = new Color(
				(colorRGB >> 16) & 0xFF,
				(colorRGB >> 8) & 0xFF,
				colorRGB & 0xFF,
				alpha);
		return color;
	}

	/*public static Color interpolateColor(final Color COLOR1, final Color COLOR2, double fraction)  
    {              
        final double INT_TO_double_CONST = 1f / 255f;  
        fraction = Math.min(fraction, 1f);  
        fraction = Math.max(fraction, 0f);  

        final double RED1 = COLOR1.getRed() * INT_TO_double_CONST;  
        final double GREEN1 = COLOR1.getGreen() * INT_TO_double_CONST;  
        final double BLUE1 = COLOR1.getBlue() * INT_TO_double_CONST;  
        final double ALPHA1 = COLOR1.getAlpha() * INT_TO_double_CONST;  

        final double RED2 = COLOR2.getRed() * INT_TO_double_CONST;  
        final double GREEN2 = COLOR2.getGreen() * INT_TO_double_CONST;  
        final double BLUE2 = COLOR2.getBlue() * INT_TO_double_CONST;  
        final double ALPHA2 = COLOR2.getAlpha() * INT_TO_double_CONST;  

        final double DELTA_RED = RED2 - RED1;  
        final double DELTA_GREEN = GREEN2 - GREEN1;  
        final double DELTA_BLUE = BLUE2 - BLUE1;  
        final double DELTA_ALPHA = ALPHA2 - ALPHA1;  

        double red = RED1 + (DELTA_RED * fraction);  
        double green = GREEN1 + (DELTA_GREEN * fraction);  
        double blue = BLUE1 + (DELTA_BLUE * fraction);  
        double alpha = ALPHA1 + (DELTA_ALPHA * fraction);  

        red = Math.min(red, 1f);  
        red = Math.max(red, 0f);  
        green = Math.min(green, 1f);  
        green = Math.max(green, 0f);  
        blue = Math.min(blue, 1f);  
        blue = Math.max(blue, 0f);  
        alpha = Math.min(alpha, 1f);  
        alpha = Math.max(alpha, 0f);  

        return new Color(red, green, blue, alpha);          
    }*/
}

