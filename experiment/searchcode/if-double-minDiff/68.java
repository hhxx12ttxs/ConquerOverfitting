double targetRatio = (double) desiredW / desiredH;
if (sizes == null)
return null;

Size optimalSize = null;
// Cannot find the one match the aspect ratio, ignore the requirement
if (optimalSize == null) {
minDiff = Double.MAX_VALUE;

