public double entropyBackground (BufferedImage image, int threshold) {
double entropy = 0.0, normalizedSum = 0.0;
int [] histogram = histogram(image);
public double entropyForeground (BufferedImage image, int threshold) {
double entropy = 0.0, normalizedSum = 0.0;
double [] normalizedHistogram = getNormalizedHistogram(histogram(image));

