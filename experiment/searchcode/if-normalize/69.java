public static int interpolateAngle256(GamePlatform platform, int a, int b, float progress)
{
if (progress != 0 &amp;&amp; b != a) {
int diff = AngleInterpolation.normalizeAngle256(b - a);
float diff = AngleInterpolation.normalizeAngle360(platform, b - a);
if (diff >= 180) {
diff -= 360;

