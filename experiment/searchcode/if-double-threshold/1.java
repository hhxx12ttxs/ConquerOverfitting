public class ThresholdFunc implements ActiveFunc {
private double threshold;
public ThresholdFunc( double threshold ){
this.threshold = threshold;
}
public double activate( double input ){
if ( input >= threshold )

