public abstract class AbstractCachingDiffFunction implements DiffFunction, HasInitial {

double[] lastX = null;
* Clears the cache in a way that doesn&#39;t require reallocation :-)
*/
protected void clearCache() {
if (lastX != null) lastX[0] = Double.NaN;
}

public double[] initial() {

