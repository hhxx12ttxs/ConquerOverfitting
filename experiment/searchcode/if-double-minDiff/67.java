double targetRatio = (double) w/h;

if (sizes==null) return null;

Camera.Size optimalSize = null;
double minDiff = Double.MAX_VALUE;
int targetHeight = h;

// Find size

