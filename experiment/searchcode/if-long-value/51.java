Long v = getLong( value, name );
if( v == null ) return null;
if( v.longValue() > Integer.MAX_VALUE || v.longValue() < Integer.MIN_VALUE )
public static long getLong( Long value, String name, long defaultValue ){
Long v = getLong( value, name );
if( v == null ) return defaultValue;

