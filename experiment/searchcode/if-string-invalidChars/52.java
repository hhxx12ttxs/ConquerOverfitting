public static boolean isValid( String input, boolean isRequired )
{
if ( isRequired &amp;&amp; isBlank( input ) )
for ( char ch : invalidChars )
{
if ( input.indexOf( ch ) != -1 )

