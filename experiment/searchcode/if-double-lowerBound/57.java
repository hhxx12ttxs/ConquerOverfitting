public class Range {
public Double lowerBound;
public Double upperBound;

public Range(){
hash = 59 * hash + (int) (Double.doubleToLongBits(this.lowerBound) ^ (Double.doubleToLongBits(this.lowerBound) >>> 32));
hash = 59 * hash + (int) (Double.doubleToLongBits(this.upperBound) ^ (Double.doubleToLongBits(this.upperBound) >>> 32));

