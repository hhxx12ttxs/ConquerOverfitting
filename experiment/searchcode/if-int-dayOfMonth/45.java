* Returns the ordinal value for the passed day of the month, e.g. &#39;th&#39;, &#39;nd&#39;, &#39;st&#39;.
*
* @param 	dayOfMonth	The day of the month to get the ordinal value for.
* @return	String		The ordinal value for the passed day of the month.
*/
public static final String getOrdinal( int dayOfMonth ) {

if (dayOfMonth >= 11 &amp;&amp; dayOfMonth <= 13) {

