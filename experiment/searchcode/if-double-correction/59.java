public class ShapeAnchorCalculator {

public int calculateCorrection(int sizeShape, int sizeAnchor, double offset) {
int position = (int) (sizeShape * offset);
int correction = 0;
if (position + sizeAnchor > sizeShape) {
// Correction for right side or bottom

