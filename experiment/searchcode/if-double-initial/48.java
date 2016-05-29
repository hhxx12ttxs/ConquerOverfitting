* @author Dan Klein
*/

public abstract class AbstractCachingDiffFunction implements DiffFunction, HasInitial {

double[] lastX = null;
protected void clearCache() {
if (lastX != null) lastX[0] = Double.NaN;
}

public double[] initial() {

