public class DangerSwitch implements Comparable<DangerSwitch> {
public double factor_;
public double danger_;
public DangerSwitch(double factor, double weight) {
public int compareTo(DangerSwitch other) {
if ( Math.abs(factor_ - other.factor_) < 1e-9 ) return (int)Math.signum(danger_ - other.danger_);

