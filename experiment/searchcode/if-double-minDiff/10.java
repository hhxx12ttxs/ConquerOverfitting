double targetRatio = (double) w / h;
if (supportedSizes == null) return null;

Size optimalSize = null;
double minDiff = Double.MAX_VALUE;

int targetHeight = h;

// Try to find an size match aspect ratio and size

