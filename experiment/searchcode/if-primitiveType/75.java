package utils;

import model.PrimitiveType;

import java.lang.reflect.Field;

/**
 * Created by igladush on 14.03.16.
 */
public class PrimitiveTypeService {
    public static final String ERROR_TYPE_FIELD="It isn't primitive type";

    public static boolean isPrimitive(Field field){
        for(PrimitiveType pt:PrimitiveType.values()){
            if(pt.isThisType(field)){
                return true;
            }
        }
        return false;
    }
    public static PrimitiveType getPrimateType(Field field) {
        for (PrimitiveType pt : PrimitiveType.values()) {
            if (pt.isThisType(field)) {
                return pt;
            }
        }
        throw new IllegalArgumentException(ERROR_TYPE_FIELD);
    }
}

