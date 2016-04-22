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

import static com.google.common.base.Preconditions.checkNotNull;
import net.jcip.annotations.Immutable;

/**
 * Represents the type of a primitive value (e.g. the value of a node property).
 * 
 * @author Pierre-Charles David
 */
@Immutable
public class PrimitiveType implements Type {
    public static final PrimitiveType STRING = new PrimitiveType(String.class, "string");
    public static final PrimitiveType NUMBER = new PrimitiveType(Number.class, "number");
    public static final PrimitiveType BOOLEAN = new PrimitiveType(Boolean.class, "boolean");
    public static final PrimitiveType OBJECT = new PrimitiveType(Object.class, "object");

    private final Class<?> klass;
    private final String name;

    public PrimitiveType(Class<?> klass, String name) {
        checkNotNull(klass);
        checkNotNull(name);
        this.klass = klass;
        this.name = name;
    }

    public boolean checkValue(Object value) {
        return klass.isInstance(value);
    }
    
    @Override
    public String toString() {
        return name;
    }
}

