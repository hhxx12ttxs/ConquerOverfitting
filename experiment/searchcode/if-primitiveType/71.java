package com.solonarv.adts;

public abstract class Type {
    
    public static final Type Null = new Type(){
        @Override
        public String toString(){
            return "null";
        }
        
        @Override
        public boolean isSameType(Type other){
            return other == this;
        }
    };
    
    @Override
    public abstract String toString();
    
    @Override
    public final boolean equals(Object other){
        if (other instanceof Type) {
            return this.isSameType((Type) other);
        } else return false;
    }
    
    public abstract boolean isSameType(Type other);
    
    public static Type getType(Object o){
        if (o == null) {
            return Null;
        } else if (o instanceof Typed){
            return ((Typed) o).type();
        } else if (o instanceof Byte){
            return PrimitiveType.byteT;
        } else if (o instanceof Short){
            return PrimitiveType.shortT;
        } else if (o instanceof Integer){
            return PrimitiveType.intT;
        } else if (o instanceof Long){
            return PrimitiveType.longT;
        } else if (o instanceof Float){
            return PrimitiveType.floatT;
        } else if (o instanceof Double){
            return PrimitiveType.doubleT;
        } else if (o instanceof Boolean){
            return PrimitiveType.booleanT;
        } else if (o instanceof Character){
            return PrimitiveType.charT;
        } else if (o instanceof String){
            return PrimitiveType.stringT;
        }
        return OpaqueType.instance(o.getClass());
    }
}
