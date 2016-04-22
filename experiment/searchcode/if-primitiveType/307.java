package com.wotifgroup.swaggerstandalonegenerator.model;

import java.util.Date;

/**
 * Enumerates the different supported primitive types.
 *
 * User: Adam
 * Date: 27/07/12
 * Time: 11:24 PM
 */
public enum PrimitiveType {

    STRING("string", String.class),
    INTEGER("int", Integer.class),
    LONG("long", Long.class),
    DOUBLE("double", Double.class),
    BOOLEAN("boolean", Boolean.class),
    DATE("Date", Date.class);

    private String swaggerId;
    private Class<?> clazz;

    PrimitiveType(String swaggerId, Class<?> clazz) {
        this.swaggerId = swaggerId;
        this.clazz = clazz;
    }

    public static PrimitiveType findByClass(Class<?> clazz) {
        for (PrimitiveType nextType : values()) {
            if (nextType.getClazz().equals(clazz)) {
                return nextType;
            }
        }

        return null;
    }

    public static PrimitiveType findBySwaggerId(String id) {
        for (PrimitiveType nextType : values()) {
            if (nextType.getSwaggerId().equals(id)) {
                return nextType;
            }
        }

        return null;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getSwaggerId() {
        return swaggerId;
    }
}

