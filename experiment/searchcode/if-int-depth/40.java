* any existing activation depth configuration settings.
*/
public class FixedActivationDepth extends ActivationDepthImpl {

private final int _depth;

public FixedActivationDepth(int depth, ActivationMode mode) {
super(mode);
_depth = depth;

