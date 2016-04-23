package com.taoswork.tallybook.datadomain.base.presentation.typedcollection.entry;

/**
 * Created by Gao Yuan on 2016/2/20.
 */
public class PrimitiveEntries {
    public static Class<? extends IPrimitiveEntry> getDefaultPrimitiveEntry(Class primitiveType){
        if(String.class.equals(primitiveType)){
            return StringEntry.class;
        }
        return null;
    }
}

