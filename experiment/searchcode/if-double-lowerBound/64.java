public class GrayPaintScale
implements PaintScale, PublicCloneable, Serializable {


private double lowerBound;
this(0.0, 1.0);
}


public GrayPaintScale(double lowerBound, double upperBound) {
if (lowerBound >= upperBound) {

