private static final int NANGLES = 20;

private static final int NSTEPS = 50;

private MultigridVariable _variable;

private float[] _radius = new float[NSTEPS];
public float getValueAt(float r) {
int i;
for (i = 0; i < NSTEPS - 1; i++) {
if (_radius[i + 1] > r)

