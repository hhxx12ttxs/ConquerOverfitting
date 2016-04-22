package ru.bmstu.iu9.compiler.ir.type;

/**
 *
 * @author anton.bobukh
 */
public class PrimitiveType extends BaseType {
    public enum Type { 
        INT(4), BOOL(1), FLOAT(4), DOUBLE(8), CHAR(1), VOID(0), LONG(8),
        POINTER(8);
        
        private Type(long size) {
            this.size = size;
            this.value = 1 << this.ordinal();
        }
        
        private boolean is(PrimitiveType.Type[] types) {
            for (int i = 0; i < types.length; ++i) {
                if (this.is(types[i]))
                    return true;
            }
            return false;
        }
        private boolean is(Type typename) {
            return (this.value & typename.value) != 0;
        }
        
        public long size;
        private int value;
    };
    
    public PrimitiveType(Type primitive, boolean constancy) {
        super(BaseType.Type.PRIMITIVE_TYPE, constancy, primitive.size);
        this.primitive = primitive.ordinal();
    }
    public PrimitiveType(Type primitive) {
        super(BaseType.Type.PRIMITIVE_TYPE, primitive.size);
        this.primitive = primitive.ordinal();
    }
    
    @Override
    public boolean is(PrimitiveType.Type type) {
        return super.is(type) && this.primitive().is(type);
    }
    @Override
    public boolean is(PrimitiveType.Type[] types) {
        return super.is(types) && this.primitive().is(types);
    }
    
    public Type primitive() { return Type.values()[this.primitive]; }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && 
               this.primitive == ((PrimitiveType)obj).primitive;
    }
    @Override
    public String toString() {
        return ((constancy) ? "CONST " : "") + this.primitive().toString();
    }
    
    private final int primitive;
}

