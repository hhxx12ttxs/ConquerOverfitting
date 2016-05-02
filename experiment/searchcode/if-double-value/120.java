/*
<<<<<<< HEAD
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */
/*
 * Copyright 2010 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 *
 */

package com.sun.solaris.service.pools;

/**
 * The <code>Value</code> class represents a pools value.
 */
public class Value {

	private long _this;

	/**
	 * Constructor. Only for use from native code.
	 * @param pointer A pointer to a C value.
	 */
	private Value(long pointer)
	{
		_this = pointer;
	}

	/**
	 * Constructor
	 * @param name The name of the value.
	 * @throws PoolsException If there is an error whilst
	 * allocating the value.
	 */
	public Value(String name) throws PoolsException
	{
		if ((_this = PoolInternal.pool_value_alloc()) == 0)
			throw new PoolsException();
		setName(name);
	}

	/**
	 * Constructor
	 * @param name The name of the value.
	 * @param value The value of the value.
	 * @throws PoolsException If there is an error whilst
	 * allocating the value.
	 */
	public Value(String name, long value) throws PoolsException
	{
		this(name);
		setValue(value);
	}

	/**
	 * Constructor
	 * @param name The name of the value.
	 * @param value The value of the value.
	 * @param s Indicates if the value is signed or not.
	 * @throws PoolsException If there is an error whilst
	 * allocating the value.
	 */
	public Value(String name, long value, boolean s) throws PoolsException
	{
		this(name);
		setValue(value, s);
	}

	/**
	 * Constructor
	 * @param name The name of the value.
	 * @param value The value of the value.
	 * @throws PoolsException If there is an error whilst
	 * allocating the value.
	 */
	public Value(String name, String value) throws PoolsException
	{
		this(name);
		setValue(value);
	}

	/**
	 * Constructor
	 * @param name The name of the value.
	 * @param value The value of the value.
	 * @throws PoolsException If there is an error whilst
	 * allocating the value.
	 */
	public Value(String name, boolean value) throws PoolsException
	{
		this(name);
		setValue(value);
	}

	/**
	 * Constructor
	 * @param name The name of the value.
	 * @param value The value of the value.
	 * @throws PoolsException If there is an error whilst
	 * allocating the value.
	 */
	public Value(String name, double value) throws PoolsException
	{
		this(name);
		setValue(value);
	}


	private boolean _locked = false;

	/**
	 * Check whether the value is locked or not
	 * @return returns the value of _locked
	 */
	public boolean islocked() throws PoolsException
        {
                return (_locked);
        }

	/**
	 * Lock the value
	 */
	public void lock() throws PoolsException
	{
		_locked = true;
	}

	/**
	 * Unlock the value
	 */
	public void unlock() throws PoolsException
	{
		_locked = false;
	}

	/**
	 * Explicitly reclaim the memory (if not locked)
	 * allocated for this value by the C proxy.
	 */
	public void close()
	{
		if (_locked == false) {
			if (_this != 0) {
				PoolInternal.pool_value_free(_this);
				_this = 0;
			}
		}
	}

	/**
	 * Reclaim the memory allocated for this value by the C
	 * proxy.
	 *
	 * @throws Throwable If freeing this configuration fails.
	 */
	protected void finalize() throws Throwable
	{
		try
		{
			unlock();
			close();
		}
		finally
		{
			super.finalize();
		}
	}

	/**
	 * Name this value.
	 *
	 * @param name The name to set for this value.
	 */
	public void setName(String name)
	{
		PoolInternal.pool_value_set_name(_this, name);
	}

	/**
	 * Set this value to take the supplied signed long value.
	 *
	 * @param value The value to which this value should be set.
	 */
	public void setValue(long value)
	{
		PoolInternal.pool_value_set_int64(_this, value);
	}

	/**
	 * Set this value to take the supplied long value.
	 *
	 * @param value The value to which this value should be set.
	 * @param s Is the value signed or unsigned.
	 */
	public void setValue(long value, boolean s)
	{
		if (s)
			setValue(value);
		PoolInternal.pool_value_set_uint64(_this, value);
	}

	/**
	 * Set this value to take the supplied string value.
	 *
	 * @param value The value to which this value should be set.
	 * @throws PoolsExecption If the setting of the value fails.
	 */
	public void setValue(String value) throws PoolsException
	{
		if (PoolInternal.pool_value_set_string(_this, value) !=
		    PoolInternal.PO_SUCCESS)
			throw new PoolsException();
	}

	/**
	 * Set this value to take the supplied boolean value.
	 *
	 * @param value The value to which this value should be set.
	 */
	public void setValue(boolean value)
	{
		if (value == true)
			PoolInternal.pool_value_set_bool(_this, (short)1);
		else
			PoolInternal.pool_value_set_bool(_this, (short)0);
	}

	/**
	 * Set this value to take the supplied double value.
	 *
	 * @param value The value to which this value should be set.
	 */
	public void setValue(double value)
	{
		PoolInternal.pool_value_set_double(_this, value);
	}

	/**
	 * Returns the name of the value.
	 *
	 * @return the name of the value.
	 */
	public String getName()
	{
		return (PoolInternal.pool_value_get_name(_this));
	}

	/**
	 * Returns the pointer to the native value represented by this
	 * object.
	 *
	 * @return the pointer to the native value represented by this
	 * object.
	 */
	public long getValue()
	{
		return (_this);
	}

	/**
	 * Returns the type of this object.
	 *
	 * @return the type of this object.
	 */
	public int getType()
	{
		return (PoolInternal.pool_value_get_type(_this));
	}

	/**
	 * Returns a string representation of this value.
	 *
	 * @return a string representation of this value.
	 */
	public String toString()
	{
		int type = PoolInternal.pool_value_get_type(_this);

		try {
			if (type == PoolInternal.POC_INT ||
			    type == PoolInternal.POC_UINT)
				return (String.valueOf(getLong()));
			if (type == PoolInternal.POC_STRING)
				return getString();
			if (type == PoolInternal.POC_BOOL)
				return (String.valueOf(getBool()));
			if (type == PoolInternal.POC_DOUBLE)
				return (String.valueOf(getDouble()));
		}
		catch (PoolsException pe) {
			return pe.toString();
		}
		return "";	/* Stop the compiler complaining */
	}

        /**
         * Returns the value as a UnsignedInt64.
         *
         * @return the value as a UnsignedInt64.
         * @throws PoolsException if the value is not an
         * UnsignedInt64.
         */
	public final UnsignedInt64 getUnsignedInt64() throws PoolsException
	{
		return (getUnsignedInt64Value(_this));
	}

        /**
         * Returns the value as a long.
         *
         * @return the value as a long.
         * @throws PoolsException if the value is not a long.
         */
	public final long getLong() throws PoolsException
	{
		return (getLongValue(_this));
	}

        /**
         * Returns the value as a String.
         *
         * @return the value as a String.
         * @throws PoolsException if the value is not a String.
         */
	public final String getString() throws PoolsException
	{
		return (getStringValue(_this));
	}

        /**
         * Returns the value as a boolean.
         *
         * @return the value as a boolean.
         * @throws PoolsException if the value is not a boolean.
         */
	public final boolean getBool() throws PoolsException
	{
		return (getBoolValue(_this));
	}

        /**
         * Returns the value as a double.
         *
         * @return the value as a double.
         * @throws PoolsException if the value is not a double.
         */
	public final double getDouble() throws PoolsException
	{
		return (getDoubleValue(_this));
	}

        /**
         * Returns the value as a UnsignedInt64.
         *
         * @param pointer the native value to be accessed.
         * @return the value as a UnsignedInt64.
         */
	private final static native UnsignedInt64 getUnsignedInt64Value(
	    long pointer);

        /**
         * Returns the value as a long.
         *
         * @param pointer the native value to be accessed.
         * @return the value as a long.
         */
	private final static native long getLongValue(long pointer);

        /**
         * Returns the value as a String.
         *
         * @param pointer the native value to be accessed.
         * @return the value as a String.
         */
	private final static native String getStringValue(long pointer);

        /**
         * Returns the value as a boolean.
         *
         * @param pointer the native value to be accessed.
         * @return the value as a boolean.
         */
	private final static native boolean getBoolValue(long pointer);

        /**
         * Returns the value as a double.
         *
         * @param pointer the native value to be accessed.
         * @return the value as a double.
         */
	private final static native double getDoubleValue(long pointer);
=======
 * This file is part of Math.
 *
 * Copyright (c) 2011 Spout LLC <http://www.spout.org/>
 * Math is licensed under the Spout License Version 1.
 *
 * Math is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Math is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spout.math.test;

import org.junit.Test;

import org.spout.math.TrigMath;
import org.spout.math.vector.Vector2;
import org.spout.math.vector.Vector3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrigMathTest {
	private void testValue(float angle, float result, float realValue) {
		assertTrue("angle=" + angle + " expected " + realValue + " but got " + result, Math.abs(result - realValue) < 0.0001);
	}

	private void testSin(float value) {
		testValue(value, TrigMath.sin(value), (float) Math.sin(value));
	}

	private void testCos(float value) {
		testValue(value, TrigMath.cos(value), (float) Math.cos(value));
	}

	@Test
	public void testSinCos() {
		float step = (float) (TrigMath.TWO_PI / 100.0); //100 steps in the circle
		for (float i = (float) -TrigMath.PI; i < TrigMath.TWO_PI; i += step) {
			testSin(i);
			testCos(i);
		}
	}

	private void assert2D(float angle, Vector2 vector, float x, float y) {
		String msg = "angle=" + angle + " expected [" + x + ", " + y + "] but got [" + vector.getX() + ", " + vector.getY() + "]";
		assertTrue(msg, vector.sub(x, y).lengthSquared() < 0.001);
	}

	private void assert3D(float yaw, float pitch, Vector3 vector, float x, float y, float z) {
		String msg = "[yaw=" + yaw + ", pitch=" + pitch + "] expected [" + x + ", " + y + ", " + z + "] but got [" + vector.getX() + ", " + vector.getY() + ", " + vector.getZ() + "]";
		assertTrue(msg, vector.sub(x, y, z).lengthSquared() < 0.001);
	}

	private void test2D(float angle, float x, float y) {
		assert2D(angle, Vector2.createDirection(angle), x, y);
	}

	private void test3D(float yaw, float pitch, float x, float y, float z) {
		assert3D(yaw, pitch, Vector3.createDirection(yaw, pitch), x, y, z);
	}

	@Test
	public void test3DAxis() {
		test3D(0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		test3D((float) TrigMath.HALF_PI, (float) TrigMath.PI, 0.0f, 0.0f, -1.0f);
		test3D((float) TrigMath.QUARTER_PI, (float) TrigMath.QUARTER_PI, 0.5f, (float) TrigMath.HALF_SQRT_OF_TWO, 0.5f);
		test3D(0.0f, (float) TrigMath.HALF_PI, 0.0f, 1.0f, 0.0f);
		test3D(0.0f, (float) TrigMath.THREE_PI_HALVES, 0.0f, -1.0f, 0.0f);
		// verify that the 2D axis are the same for 3D axis without pitch
		float step = (float) (TrigMath.TWO_PI / 50.0); //50 steps in the circle
		for (float i = (float) -TrigMath.PI; i < TrigMath.TWO_PI; i += step) {
			Vector2 vec2D = Vector2.createDirection(i);
			Vector3 vec3D = Vector3.createDirection(i, 0);
			assertEquals(vec2D.getX(), vec3D.getX(), 0.001f);
			assertEquals(vec2D.getY(), vec3D.getZ(), 0.001f);
		}
	}

	@Test
	public void test2DAxis() {
		test2D(0.0f, 1.0f, 0.0f);
		test2D((float) TrigMath.HALF_PI, 0.0f, 1.0f);
		test2D((float) TrigMath.PI, -1.0f, 0.0f);
		test2D((float) TrigMath.THREE_PI_HALVES, 0.0f, -1.0f);
		test2D((float) TrigMath.QUARTER_PI, (float) TrigMath.HALF_SQRT_OF_TWO, (float) TrigMath.HALF_SQRT_OF_TWO);
	}

	private void testValue(double value, double result, double realValue) {
		assertTrue("value=" + value + " expected " + realValue + " but got " + result, Math.abs(result - realValue) < 0.0000001);
	}

	private void testAsin(double value) {
		testValue(value, TrigMath.asin(value), Math.asin(value));
	}

	private void testAcos(double value) {
		testValue(value, TrigMath.acos(value), Math.acos(value));
	}

	private void testAsec(double value) {
		testValue(value, TrigMath.asec(value), Math.acos(1 / value));
	}

	private void testAcosec(double value) {
		testValue(value, TrigMath.acsc(value), Math.asin(1 / value));
	}

	private void testAtan(double value) {
		testValue(value, TrigMath.atan(value), Math.atan(value));
	}

	private void testAtan2(double y, double x) {
		double realValue = Math.atan2(y, x);
		double result = TrigMath.atan2(y, x);
		assertTrue("x=" + x + ",y=" + y + " expected " + realValue + " but got " + result, Math.abs(result - realValue) < 0.0000001);
	}

	@Test
	public void testAsinAcos() {
		double step = 2.0 / 100.0;
		for (double i = -1.0; i <= 1.0; i += step) {
			testAsin(i);
			testAcos(i);
		}
	}

	@Test
	public void testAsecAcosec() {
		double step = 4.0 / 100.0;
		for (double i = -2.0; i <= -1; i += step) {
			testAsec(i);
			testAcosec(i);
		}
		for (double i = 1; i <= 2; i += step) {
			testAsec(i);
			testAcosec(i);
		}
	}

	@Test
	public void testAtan() {
		double step = 0.1;
		for (double i = -10.0; i <= 10.0; i += step) {
			testAtan(i);
		}
	}

	@Test
	public void testAtan2() {
		double step = 0.2;
		for (double x = -5.0; x <= 5.0; x += step) {
			for (double y = -5.0; y <= 5.0; y += step) {
				testAtan2(y, x);
			}
		}
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

