* @return
*
*/
public static RealMatrix toRealMatrix(final double[] matrix, final int rowDimension, final int columnDimension) {

if(rowDimension < 0)
throw new IllegalArgumentException(&quot;rowDimension&quot;);
if(columnDimension < 0)

