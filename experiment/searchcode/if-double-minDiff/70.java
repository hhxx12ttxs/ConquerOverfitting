Camera.Size optimalSize = null;
double minDiff = Double.MAX_VALUE;

for (Camera.Size size : sizes) {
Log.e(CameraUtils.class.getSimpleName(), &quot;Couldn&#39;t find any preview size with good ratio&quot;);

minDiff = Double.MAX_VALUE;
for (Camera.Size size : sizes) {
if (Math.abs(size.height - height) < minDiff) {

