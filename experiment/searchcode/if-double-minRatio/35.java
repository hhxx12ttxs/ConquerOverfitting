public static Dimension scaleDimension(int width, int height, int maxWidth, int maxHeight) {
if (width < maxWidth &amp;&amp; height < maxHeight) return new Dimension(width, height);
double widthRatio = ((double) maxWidth) / ((double) width);
double heightRatio = ((double) maxHeight) / ((double) height);
double minRatio = widthRatio < heightRatio ? widthRatio : heightRatio;

