package com.intel.picklepot.serialization;

import com.google.common.primitives.Primitives;
import parquet.schema.PrimitiveType;

public enum Type {
  DOUBLE(PrimitiveType.PrimitiveTypeName.DOUBLE),

  FLOAT(PrimitiveType.PrimitiveTypeName.FLOAT),

  LONG(PrimitiveType.PrimitiveTypeName.INT64),

  INT(PrimitiveType.PrimitiveTypeName.INT32),

  STRING(PrimitiveType.PrimitiveTypeName.BINARY),

  BOOLEAN(PrimitiveType.PrimitiveTypeName.BOOLEAN),

  /**
   * [[ is not supported
   */
  ARRAY(null),

  /**
   * 1.Primitive types excluding above types, like double
   * 2.Array, like Int[]
   */
  UNSUPPORTED(null),

  /**
   * Any type that can not be classified as above types, like Tuple2
   */
  NESTED(null);

  private PrimitiveType.PrimitiveTypeName parquetType;

  Type(PrimitiveType.PrimitiveTypeName parquetType) {
    this.parquetType = parquetType;
  }

  public PrimitiveType.PrimitiveTypeName toParquetType() {
    return parquetType;
  }

  public static Type typeOf(Class clazz) {
    if(clazz == String.class) {
      return STRING;
    }
    if(clazz == Integer.class || clazz == int.class) {
      return INT;
    }
    if(clazz == long.class || clazz == Long.class) {
      return LONG;
    }
    if(clazz == float.class || clazz == Float.class) {
      return FLOAT;
    }
    if(clazz == double.class || clazz == Double.class) {
      return DOUBLE;
    }
    if (clazz == boolean.class || clazz == Boolean.class) {
      return BOOLEAN;
    }
    if(clazz.isArray()) {
      Type componentType = typeOf(clazz.getComponentType());
      if(componentType != ARRAY && componentType != UNSUPPORTED && componentType != NESTED) {
        return ARRAY;
      }
    }
    if(clazz.isPrimitive() || Primitives.isWrapperType(clazz) || clazz.isArray() || Utils.isException(clazz)) {
      return UNSUPPORTED;
    }
    return NESTED;
  }
}

