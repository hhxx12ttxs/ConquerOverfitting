package com.solonarv.adts;

public final class PrimitiveType extends Type {
    public static final PrimitiveType byteT = new PrimitiveType("byte");
    public static final PrimitiveType shortT = new PrimitiveType("short");
    public static final PrimitiveType intT = new PrimitiveType("int");
    public static final PrimitiveType longT = new PrimitiveType("long");
    public static final PrimitiveType floatT = new PrimitiveType("float");
    public static final PrimitiveType doubleT = new PrimitiveType("double");
    public static final PrimitiveType booleanT = new PrimitiveType("boolean");
    public static final PrimitiveType charT = new PrimitiveType("char");
    public static final PrimitiveType stringT = new PrimitiveType("String");
    
    private final String name;
    
    private PrimitiveType(String name){
        this.name = name;
    }
    
    @Override
    public String toString(){
        return this.name;
    }
    
    @Override
    public boolean isSameType(Type other){
        if (other instanceof PrimitiveType){
            PrimitiveType otherPrim = (PrimitiveType) other;
            return this.name == otherPrim.name;
        } else return false;
    }
}
