/*
 * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt for licensing information.
 */

package de.ailis.gramath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.regex.Matcher;


/**
 * Base class for colors with four float components.
 *
 * @author Klaus Reimer (k@ailis.de)
 */

public abstract class Color4f extends Color
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Color black. */
    public static final ImmutableColor4f BLACK = new ImmutableColor4f(0, 0, 0,
        1);

    /** Color blue. */
    public static final ImmutableColor4f BLUE = new ImmutableColor4f(0, 0, 1, 1);

    /** Color cyan. */
    public static final ImmutableColor4f GREEN = new ImmutableColor4f(0, 1, 0,
        1);

    /** Color black. */
    public static final ImmutableColor4f CYAN = new ImmutableColor4f(0, 1, 1, 1);

    /** Color red. */
    public static final ImmutableColor4f RED = new ImmutableColor4f(1, 0, 0, 1);

    /** Color purple. */
    public static final ImmutableColor4f PURPLE = new ImmutableColor4f(1, 0, 1,
        1);

    /** Color yellow. */
    public static final ImmutableColor4f YELLOW = new ImmutableColor4f(1, 1, 0,
        1);

    /** Color black. */
    public static final ImmutableColor4f WHITE = new ImmutableColor4f(1, 1, 1,
        1);

    /** The The red component. */
    protected float red;

    /** The green component. */
    protected float green;

    /** The blue component. */
    protected float blue;

    /** The alpha component */
    protected float alpha;

    /** The buffer representation of the color. */
    private transient FloatBuffer buffer;


    /**
     * Constructs an uninitialized color.
     */

    protected Color4f()
    {
        // Empty
    }


    /**
     * Constructs a new color with the specified components.
     *
     * @param red
     *            The red component.
     * @param green
     *            The green component.
     * @param blue
     *            The blue component.
     * @param alpha
     *            The alpha component.
     */

    public Color4f(final float red, final float green, final float blue,
        final float alpha)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }


    /**
     * Constructs a new color with the values of the specified color. The
     * alpha component is set to 1.
     *
     * @param color
     *            The color to copy the values from.
     */

    public Color4f(final Color3d color)
    {
        this.red = (float) color.red;
        this.green = (float) color.green;
        this.blue = (float) color.blue;
        this.alpha = 1;
    }


    /**
     * Constructs a new color with the values of the specified color. The
     * alpha component is set to 1.
     *
     * @param color
     *            The color to copy the values from.
     */

    public Color4f(final Color3f color)
    {
        this.red = color.red;
        this.green = color.green;
        this.blue = color.blue;
        this.alpha = 1;
    }


    /**
     * Constructs a new color with the values of the specified color.
     *
     * @param color
     *            The color to copy the values from.
     */

    public Color4f(final Color4f color)
    {
        this.red = color.red;
        this.green = color.green;
        this.blue = color.blue;
        this.alpha = color.alpha;
    }


    /**
     * Constructs a new color with the values of the specified color.
     *
     * @param color
     *            The color to copy the values from.
     */

    public Color4f(final Color4d color)
    {
        this.red = (float) color.red;
        this.green = (float) color.green;
        this.blue = (float) color.blue;
        this.alpha = (float) color.alpha;
    }


    /**
     * Constructs a new color from the specified HTML or CSS color.
     *
     * @param color
     *            The color in HTML or CSS notation.
     */

    public Color4f(final String color)
    {
        Matcher matcher;

        if ((matcher = CSS_HTML_PATTERN.matcher(color)).matches())
        {
            final int c = Integer.parseInt(matcher.group(1), 16);
            this.red = (((c & 0xf00) >> 8) | ((c & 0xf00) >> 4)) / 255f;
            this.green = (((c & 0xf0) >> 4) | ((c & 0xf0))) / 255f;
            this.blue = ((c & 0xf) | ((c & 0xf) << 4)) / 255f;
            this.alpha = 1f;
        }
        else if ((matcher = HTML_PATTERN.matcher(color)).matches())
        {
            final int c = Integer.parseInt(matcher.group(1), 16);
            this.red = ((c & 0xff0000) >> 16) / 255f;
            this.green = ((c & 0xff00) >> 8) / 255f;
            this.blue = (c & 0xff) / 255f;
            this.alpha = 1f;
        }
        else if ((matcher = CSS_RGB_PATTERN.matcher(color)).matches())
        {
            this.red = Integer.parseInt(matcher.group(1)) / 255f;
            this.green = Integer.parseInt(matcher.group(2)) / 255f;
            this.blue = Integer.parseInt(matcher.group(3)) / 255f;
            this.alpha = 1f;
        }
        else if ((matcher = CSS_RGBA_PATTERN.matcher(color)).matches())
        {
            this.red = Integer.parseInt(matcher.group(1)) / 255f;
            this.green = Integer.parseInt(matcher.group(2)) / 255f;
            this.blue = Integer.parseInt(matcher.group(3)) / 255f;
            this.alpha = Float.parseFloat(matcher.group(4));
        }
        else if ((matcher = CSS_RGB_PERCENT_PATTERN.matcher(color)).matches())
        {
            this.red = Integer.parseInt(matcher.group(1)) / 100f;
            this.green = Integer.parseInt(matcher.group(2)) / 100f;
            this.blue = Integer.parseInt(matcher.group(3)) / 100f;
            this.alpha = 1f;
        }
        else
            throw new IllegalArgumentException("Unable to parse color: "
                + color);
    }


    /**
     * @see java.lang.Object#toString()
     */

    @Override
    public final String toString()
    {
        return "[ " + this.red + ", " + this.green + ", " + this.blue + ", "
            + this.alpha + " ]";
    }


    /**
     * @see java.lang.Object#hashCode()
     */

    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(this.red);
        result = prime * result + Float.floatToIntBits(this.green);
        result = prime * result + Float.floatToIntBits(this.blue);
        result = prime * result + Float.floatToIntBits(this.alpha);
        return result;
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Color4f other = (Color4f) obj;
        if (Float.floatToIntBits(this.red) != Float.floatToIntBits(other.red))
            return false;
        if (Float.floatToIntBits(this.green) != Float
            .floatToIntBits(other.green))
            return false;
        if (Float.floatToIntBits(this.blue) != Float.floatToIntBits(other.blue))
            return false;
        if (Float.floatToIntBits(this.alpha) != Float
            .floatToIntBits(other.alpha))
            return false;
        return true;
    }


    /**
     * Returns the direct NIO float buffer in native ordering containing the
     * color elements. The returned float buffer is cached and mutable but
     * modifications do not modify the color class itself.
     *
     * @return The color as a direct NIO float buffer. Never null.
     */

    public final FloatBuffer getBuffer()
    {
        if (this.buffer == null)
            this.buffer = ByteBuffer.allocateDirect(4 * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.buffer.rewind();
        this.buffer.put(this.red).put(this.green).put(this.blue)
            .put(this.alpha);
        this.buffer.rewind();
        return this.buffer;
    }


    /**
     * Returns the red component.
     *
     * @return The red component.
     */

    public final float getRed()
    {
        return this.red;
    }


    /**
     * Returns the green component.
     *
     * @return The green component.
     */

    public final float getGreen()
    {
        return this.green;
    }


    /**
     * Returns the blue component.
     *
     * @return The blue component.
     */

    public final float getBlue()
    {
        return this.blue;
    }


    /**
     * Returns the alpha component.
     *
     * @return The alpha component.
     */

    public final float getAlpha()
    {
        return this.alpha;
    }


    /**
     * @see java.lang.Object#clone()
     */

    @Override
    public abstract Object clone();


    /**
     * Returns an immutable version of this color. If this color is already
     * immutable then it is just casted.
     *
     * @return This color as an immutable color.
     */

    public ImmutableColor4f asImmutable()
    {
        if (getClass() == ImmutableColor4f.class) return (ImmutableColor4f) this;
        return new ImmutableColor4f(this);
    }
}

