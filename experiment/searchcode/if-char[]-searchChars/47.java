* @return the {@code true} if any of the chars are found, {@code false} if no match or null input
*/
public static boolean containsAny(CharSequence cs, CharSequence searchChars) {
if (searchChars == null) {
// missing low surrogate, fine, like String.indexOf(String)
return true;
}
if (i < csLast &amp;&amp; searchChars[j + 1] == cs.charAt(i + 1)) {

