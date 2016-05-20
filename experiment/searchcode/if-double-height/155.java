/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at LICENSE.html or
 * http://www.sun.com/cddl.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this License Header
 * Notice in each file.
 *
 * If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre (Shura) Iline. (shurymury@gmail.com)
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 */
package org.jemmy;


import java.io.Serializable;


/**
 * Replacement for java.awt.Dimension
 * @author Alexander Kouznetsov <mrkam@mail.ru>
 */
public class Dimension implements Serializable {

    /**
     * The width dimension; negative values can be used.
     *
     * @serial
     * @see #getSize
     * @see #setSize
     */
    public int width;

    /**
     * The height dimension; negative values can be used.
     *
     * @serial
     * @see #getSize
     * @see #setSize
     */
    public int height;

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 4723952579491349524L;

    /**
     * Creates an instance of <code>Dimension</code> with a width
     * of zero and a height of zero.
     */
    public Dimension() {
        this(0, 0);
    }

    /**
     * Creates an instance of <code>Dimension</code> whose width
     * and height are the same as for the specified dimension.
     *
     * @param    d   the specified dimension for the
     *               <code>width</code> and
     *               <code>height</code> values
     */
    public Dimension(Dimension d) {
        this(d.width, d.height);
    }

    /**
     * Constructs a <code>Dimension</code> and initializes
     * it to the specified width and specified height.
     *
     * @param width the specified width
     * @param height the specified height
     */
    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Constructs a <code>Dimension</code> and initializes
     * it to the specified width and specified height. All {@code double}
     * values are rounded and stored as {@code int} values.
     *
     * @param width the specified width
     * @param height the specified height
     */
    public Dimension(double width, double height) {
        this.width = (int) Math.round(width);
        this.height = (int) Math.round(height);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public double getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     * @return 
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the size of this <code>Dimension</code> object to
     * the specified width and height in double precision.
     * Note that if <code>width</code> or <code>height</code>
     * are larger than <code>Integer.MAX_VALUE</code>, they will
     * be reset to <code>Integer.MAX_VALUE</code>.
     *
     * @param width  the new width for the <code>Dimension</code> object
     * @param height the new height for the <code>Dimension</code> object
     */
    public void setSize(double width, double height) {
        this.width = (int) Math.ceil(width);
        this.height = (int) Math.ceil(height);
    }

    /**
     * Gets the size of this <code>Dimension</code> object.
     * @return   the size of this dimension, a new instance of
     *           <code>Dimension</code> with the same width and height
     * @see      #setSize
     */
    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /**
     * Sets the size of this <code>Dimension</code> object to the specified size.
     * @param    d  the new size for this <code>Dimension</code> object
     * @see      Dimension#getSize
     */
    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    /**
     * Sets the size of this <code>Dimension</code> object
     * to the specified width and height.
     * @param    width   the new width for this <code>Dimension</code> object
     * @param    height  the new height for this <code>Dimension</code> object
     * @see      Dimension#getSize
     */
    public void setSize(int width, int height) {
    	this.width = width;
    	this.height = height;
    }

    /**
     * Checks whether two dimension objects have equal values.
     * @param obj 
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Dimension) {
            Dimension d = (Dimension)obj;
            return (width == d.width) && (height == d.height);
        }
        return false;
    }

    /**
     * Returns the hash code for this <code>Dimension</code>.
     *
     * @return    a hash code for this <code>Dimension</code>
     */
    @Override
    public int hashCode() {
        int sum = width + height;
        return sum * (sum + 1)/2 + width;
    }

    /**
     * Returns a string representation of the values of this
     * <code>Dimension</code> object's <code>height</code> and
     * <code>width</code> fields. This method is intended to be used only
     * for debugging purposes, and the content and format of the returned
     * string may vary between implementations. The returned string may be
     * empty but may not be <code>null</code>.
     *
     * @return  a string representation of this <code>Dimension</code>
     *          object
     */
    @Override
    public String toString() {
        return getClass().getName() + "[width=" + width + ",height=" + height + "]";
    }
}

