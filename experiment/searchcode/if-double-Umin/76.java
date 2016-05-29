* The domain of the immersion is a rectangle in (u,v) space specified by the four methods {@link #setUMin(double)}, {@link #setUMax(double)}, etc.
public class ParametricSurfaceFactory extends AbstractQuadMeshFactory {

final OoNode uMin = node( new Double(0), &quot;uMin&quot; );
final OoNode uMax = node( new Double(1), &quot;uMax&quot; );

