* http://www.ken-soft.com
* 2009 January 9
*
*/
public class Rect4D {

private int dimX;
private int dimY;
* @return object
*/
public Object get(int x, int y, int z, int u) {
if(x >= 0 &amp;&amp; x < dimX &amp;&amp; y >= 0 &amp;&amp; y < dimY &amp;&amp; z >= 0 &amp;&amp; z < dimZ &amp;&amp; u >= 0 &amp;&amp; u < dimU) {

