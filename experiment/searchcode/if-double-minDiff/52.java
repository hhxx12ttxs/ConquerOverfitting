double targetRatio = (double) w / h;
if (sizes == null) return null;

Size optimalSize = null;
if (optimalSize == null) {
minDiff = Double.MAX_VALUE;
for (Size size : sizes) {
if (Math.abs(size.height - targetHeight) < minDiff) {

