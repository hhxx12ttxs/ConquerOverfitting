* Translate the field name by stripping @, data, contentdata. All / (slash) are replaced by . (dot).
*/
public static String translateFieldName( String fieldName )
fieldName = fieldName.replace( &#39;.&#39;, &#39;/&#39; );

if ( fieldName.startsWith( &quot;/&quot; ) )
{
fieldName = fieldName.substring( 1 );

