package base;
import java.awt.Color;
import java.awt.color.ColorSpace;

/**
 * The ApproximateColor class is used to encapsulate colors in the default sRGB
 * color space or colors in arbitrary color spaces identified by a ColorSpace,
 * along with a tolerance radius around that color in the
 * hue-saturation-brightness color space. Every color has an implicit alpha
 * value of 1.0 or an explicit one provided in the constructor. The alpha value
 * defines the transparency of a color and can be represented by a float value
 * in the range 0.0 - 1.0 or 0 - 255. An alpha value of 1.0 or 255 means that
 * the color is completely opaque and an alpha value of 0 or 0.0 means that the
 * color is completely transparent. When constructing a Color with an explicit
 * alpha or getting the color/alpha components of a Color, the color components
 * are never premultiplied by the alpha component.
 * 
 * @author Tim
 */
public class ApproximateColor extends Color
{
	private int tolerance;

	/**
	 * Creates an sRGB approximate color with the specified combined RGBA value
	 * consisting of the alpha component in bits 24-31, the red component in
	 * bits 16-23, the green component in bits 8-15, and the blue component in
	 * bits 0-7, and the specified tolerance in HSB space.
	 * 
	 * @param rgba the combined RGBA components
	 * @param tolerance tolerance radius in the HSB space.
	 */
	public ApproximateColor(int rgba, int tolerance)
	{
		super(rgba, true);
		this.tolerance = tolerance;
	}

	/**
	 * Creates an opaque sRGB approximate color with the specified red, green,
	 * and blue values in the range (0 - 255) and the specified tolerance in
	 * HSB space. The actual color used in rendering depends on finding the
	 * best match given the color space available for a given output device.
	 * Alpha is defaulted to 255.
	 * 
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param tolerance tolerance radius in the HSB space.
	 */
	public ApproximateColor(int r, int g, int b, int tolerance)
	{
		super(r, g, b);
		this.tolerance = tolerance;
	}

	/**
	 * Creates an opaque sRGB approximate color with the specified red, green,
	 * and blue values in the range (0.0 - 1.0) and the specified tolerance in
	 * HSB space. Alpha is defaulted to 1.0. The actual color used in rendering
	 * depends on finding the best match given the color space available for a
	 * particular output device.
	 * 
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param tolerance tolerance radius in the HSB space.
	 */
	public ApproximateColor(float r, float g, float b, int tolerance)
	{
		super(r, g, b);
		this.tolerance = tolerance;
	}

	/**
	 * Creates an approximate color in the specified ColorSpace with the color
	 * components specified in the float array and the specified alpha and
	 * tolerance. The number of components is determined by the type of the
	 * ColorSpace. For example, RGB requires 3 components, but CMYK requires 4
	 * components.
	 * 
	 * @param cspace
	 * @param components
	 * @param alpha
	 * @param tolerance tolerance radius in the HSB space.
	 */
	public ApproximateColor(ColorSpace cspace, float[] components, float alpha, int tolerance)
	{
		super(cspace, components, alpha);
		this.tolerance = tolerance;
	}

	/**
	 * Creates an sRGB approximate color with the specified red, green, blue,
	 * and alpha values in the range (0 - 255), and the specified tolerance in
	 * HSB space.
	 * 
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component
	 * @param tolerance tolerance radius in the HSB space.
	 */
	public ApproximateColor(int r, int g, int b, int a, int tolerance)
	{
		super(r, g, b, a);
		this.tolerance = tolerance;
	}

	/**
	 * Creates an sRGB approximate color with the specified red, green, blue,
	 * and alpha values in the range (0.0 - 1.0), and the specified tolerance
	 * in HSB space. The actual color used in rendering depends on finding the
	 * best match given the color space available for a particular output
	 * device.
	 * 
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component
	 * @param tolerance tolerance radius in the HSB space.
	 */
	public ApproximateColor(float r, float g, float b, float a, int tolerance)
	{
		super(r, g, b, a);
		this.tolerance = tolerance;
	}

	/**
	 * Gets the tolerance radius in the hue-saturation-brightness space.
	 * 
	 * @return the tolerance radius in the HSB space.
	 */
	public int getTolerance()
	{
		return this.tolerance;
	}
	
	/**
	 * Returns a value indicating whether the specified color is within the
	 * tolerance of this approximate color.
	 * 
	 * @param color the color to check against this approximate color.
	 * @return true if the specified color is within the tolerance of this
	 * approximate color.
	 */
	public boolean matches(Color color)
	{
		float[] colorComponent = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		float[] thisComponent = Color.RGBtoHSB(this.getRed(), this.getGreen(), this.getBlue(), null);
		float hueDistance;
		if (Math.abs(colorComponent[0] - thisComponent[0]) < 0.5)
			hueDistance = colorComponent[0] - thisComponent[0];
		else
			hueDistance = 1 - (colorComponent[0] - thisComponent[0]);
		double distance = Math.sqrt(Math.pow(hueDistance, 2) + Math.pow(colorComponent[1] - thisComponent[1], 2) + Math.pow(colorComponent[2] - thisComponent[2], 2));
		return distance < ((double)this.tolerance) / 100.0;
	}
}

