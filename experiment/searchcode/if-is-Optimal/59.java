// Cannot find the one match the aspect ratio, ignore the requirement
if (optimalSize == null) {
optimalSize = getDefaultSize(camera, targetHeight);
for (Camera.Size size : camera.getParameters().getSupportedPreviewSizes()) {
if (Math.abs(size.height - targetHeight) < minDiff) {
optimalSize = size;

