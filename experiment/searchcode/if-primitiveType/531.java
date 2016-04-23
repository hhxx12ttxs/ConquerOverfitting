package net.xavierm02.type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import net.xavierm02.ArrayUtils;

public abstract class PrimitiveType extends TerminalType {
    public static final BooleanPrimitiveType BOOLEAN = new BooleanPrimitiveType( );
    
    public static final SignedBytePrimitiveType BYTE = new SignedBytePrimitiveType( );
    
    public static final CharPrimitiveType CHAR = new CharPrimitiveType( );
    
    public static final SignedDoublePrimitiveType DOUBLE = new SignedDoublePrimitiveType( );
    
    public static final SignedFloatPrimitiveType FLOAT = new SignedFloatPrimitiveType( );
    
    public static final SignedIntPrimitiveType INT = new SignedIntPrimitiveType( );
    
    public static final SignedLongPrimitiveType LONG = new SignedLongPrimitiveType( );
    
    public static final SignedShortPrimitiveType SHORT = new SignedShortPrimitiveType( );
    
    public static final UnsignedBytePrimitiveType UBYTE = new UnsignedBytePrimitiveType( );
    
    public static final UnsignedIntPrimitiveType UINT = new UnsignedIntPrimitiveType( );
    
    public static final UnsignedShortPrimitiveType USHORT = new UnsignedShortPrimitiveType( );
    
    public static enum EnumerationConstant {
        BOOLEAN,
        BYTE,
        CHAR,
        DOUBLE,
        FLOAT,
        INT,
        LONG,
        SHORT,
        UBYTE,
        UINT,
        USHORT;
        public PrimitiveType toType( ) {
            switch ( this ) {
                case BOOLEAN:
                    return PrimitiveType.BOOLEAN;
                case BYTE:
                    return PrimitiveType.BYTE;
                case CHAR:
                    return PrimitiveType.CHAR;
                case DOUBLE:
                    return PrimitiveType.DOUBLE;
                case FLOAT:
                    return PrimitiveType.FLOAT;
                case INT:
                    return PrimitiveType.INT;
                case LONG:
                    return PrimitiveType.LONG;
                case SHORT:
                    return PrimitiveType.SHORT;
                case UBYTE:
                    return PrimitiveType.UBYTE;
                case UINT:
                    return PrimitiveType.UINT;
                case USHORT:
                    return PrimitiveType.USHORT;
                default:
                    throw new IllegalArgumentException( "If you see this message, the VM has a bug." );
            }
        }
    }
    
    public PrimitiveType( final Class< ? > classObject ) {
        super( classObject );
    }
    
    public abstract int getSizeInBits( );
    
    public int getSizeIn( final PrimitiveType unit ) {
        final int size = this.getSizeInBits( );
        final int unitSize = unit.getSizeInBits( );
        return size % unitSize == 0 ? size / unitSize : size / unitSize + 1;
    }
    
    public abstract EnumerationConstant toEnumerationConstant( );
    
    public byte[] readAsByteArrayFromInputStream( final InputStream inputStream ) throws IOException {
        final int size = this.getSizeIn( PrimitiveType.BYTE );
        final byte[] byteArray = new byte[ size ];
        int i = inputStream.read( byteArray );
        for ( ; i < size; ++ i ) {
            byteArray[ i ] = (byte) inputStream.read( );
        }
        return byteArray;
    }
    
    public byte[] readAsByteArrayFromInputStream( final InputStream inputStream, final ByteOrder byteOrder ) throws IOException {
        final byte[] byteArray = this.readAsByteArrayFromInputStream( inputStream );
        if ( byteOrder == ByteOrder.LITTLE_ENDIAN ) {
            ArrayUtils.reverse( byteArray );
        }
        return byteArray;
    }
    // public abstract long readFromInputStream( InputStream inputStream );
}

