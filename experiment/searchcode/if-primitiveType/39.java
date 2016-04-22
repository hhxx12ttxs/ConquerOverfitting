/*
 * Copyright (c) 2007-2008 ARMINES
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: fractal@objectweb.org
 */
package org.objectweb.fractal.fscript.types;

import static org.junit.Assert.*;

import org.junit.Test;

public class PrimitiveTypesTest {
    private void assertType(Type t, Object value) {
        assertTrue(t.checkValue(value));
    }
    
    private void assertNotType(Type t, Object value) {
        assertFalse(t.checkValue(value));
    }
    
    @Test
    public void validStrings() {
        assertType(PrimitiveType.STRING, "");
        assertType(PrimitiveType.STRING, "foo");
    }
    
    @Test
    public void validNumbers() {
        assertType(PrimitiveType.NUMBER, 0.0);
        assertType(PrimitiveType.NUMBER, 0.0d);
        assertType(PrimitiveType.NUMBER, 0);
        assertType(PrimitiveType.NUMBER, 1.0);
        assertType(PrimitiveType.NUMBER, -1.0);
        assertType(PrimitiveType.NUMBER, 1);
        assertType(PrimitiveType.NUMBER, -1);
        assertType(PrimitiveType.NUMBER, new Double(42.0));
        assertType(PrimitiveType.NUMBER, new Integer(42));
        assertType(PrimitiveType.NUMBER, new Short((short) 42));
        assertType(PrimitiveType.NUMBER, Double.NaN);
        assertType(PrimitiveType.NUMBER, Double.NEGATIVE_INFINITY);
        assertType(PrimitiveType.NUMBER, Double.POSITIVE_INFINITY);
    }
    
    @Test
    public void validBooleans() {
        assertType(PrimitiveType.BOOLEAN, true);
        assertType(PrimitiveType.BOOLEAN, false);
        assertType(PrimitiveType.BOOLEAN, new Boolean(true));
        assertType(PrimitiveType.BOOLEAN, new Boolean(false));
    }
    
    @Test
    public void validObjects() {
        assertType(PrimitiveType.OBJECT, "");
        assertType(PrimitiveType.OBJECT, "foo");
        assertType(PrimitiveType.OBJECT, 0.0);
        assertType(PrimitiveType.OBJECT, 42);
        assertType(PrimitiveType.OBJECT, true);
        assertType(PrimitiveType.OBJECT, false);
        assertType(PrimitiveType.OBJECT, new Object());
        assertType(PrimitiveType.OBJECT, Object.class);
    }
    
    @Test
    public void invalidStrings() {
        assertNotType(PrimitiveType.STRING, 0);
        assertNotType(PrimitiveType.STRING, new Double(42.0));
        assertNotType(PrimitiveType.STRING, true);
        assertNotType(PrimitiveType.STRING, new Boolean(true));
        assertNotType(PrimitiveType.STRING, new Object());
        assertNotType(PrimitiveType.STRING, null);
    }

    @Test
    public void invalidNumbers() {
        assertNotType(PrimitiveType.NUMBER, "");
        assertNotType(PrimitiveType.NUMBER, "1");
        assertNotType(PrimitiveType.NUMBER, true);
        assertNotType(PrimitiveType.NUMBER, false);
        assertNotType(PrimitiveType.NUMBER, new Boolean(true));
        assertNotType(PrimitiveType.NUMBER, new Object());
        assertNotType(PrimitiveType.NUMBER, null);
    }
    
    @Test
    public void invalidBooleans() {
        assertNotType(PrimitiveType.BOOLEAN, "true");
        assertNotType(PrimitiveType.BOOLEAN, "false");
        assertNotType(PrimitiveType.BOOLEAN, 0);
        assertNotType(PrimitiveType.BOOLEAN, new Object());
        assertNotType(PrimitiveType.BOOLEAN, null);
    }
    
    @Test
    public void invalidObjects() {
        assertNotType(PrimitiveType.OBJECT, null);
    }
}

