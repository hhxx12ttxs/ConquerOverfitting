public static double getTopAdvanceForBore(ChamberStyle style, int octane, double compression, double bore) {
int octaneCorrection;
if ( octane <= 90) {
octaneCorrection = -2;
} else if (octane < 94) {

