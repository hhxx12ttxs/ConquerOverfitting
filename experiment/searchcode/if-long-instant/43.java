if ( java.util.Date.class.isAssignableFrom( type ) ) {
return (X) Date.from( instant );
}

if ( Long.class.isAssignableFrom( type ) ) {
return (X) Long.valueOf( instant.toEpochMilli() );
if ( Long.class.isInstance( value ) ) {
return Instant.ofEpochMilli( (Long) value );
}

if ( Calendar.class.isInstance( value ) ) {

