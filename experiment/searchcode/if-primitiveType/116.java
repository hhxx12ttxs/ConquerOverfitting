package com.victormiranda.beanconverter.reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public enum PrimitiveType {
    BOOLEAN("bool", Boolean.class, new ArrayList<PrimitiveType>()),
    BYTE("byte",Byte.class, new ArrayList<PrimitiveType>()),
    SHORT("short",Short.class, Arrays.asList(BYTE)),
    INT("int",Integer.class, Arrays.asList(SHORT, BYTE)),
    LONG("long",Long.class, Arrays.asList(INT, SHORT, BYTE)),
    FLOAT("float",Float.class, Arrays.asList(LONG, INT, SHORT, BYTE)),
    DOUBLE("double",Double.class, Arrays.asList(FLOAT, LONG, INT, SHORT, BYTE));

    final Class wrapperClass;
    final String typeName;
    final transient List<PrimitiveType> allowedConversions;

    PrimitiveType(String typeName, Class wrapperClass, List<PrimitiveType> allowedConversions) {
        this.typeName = typeName;
        this.wrapperClass = wrapperClass;
        this.allowedConversions = allowedConversions;
    }

    public static PrimitiveType getByTypeName(final String typeName) {
        for (PrimitiveType primitiveType : PrimitiveType.values()) {
            if (primitiveType.typeName.equals(typeName)) {
                return primitiveType;
            }
        }

        throw new IllegalArgumentException();
    }

    public boolean allow(Class destinationType) {
        PrimitiveType destinationPrimitiveType = getByTypeName(destinationType.getName());

        return this.allowedConversions.contains(destinationPrimitiveType);
    }
}
