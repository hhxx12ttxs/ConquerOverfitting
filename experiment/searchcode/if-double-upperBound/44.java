public class Interval implements Serializable {

private double lowerBound = 0;
private double upperBound = 0;

public Interval() {
public void setBounds(double lowerBound, double upperBound) throws Exception {
if (lowerBound > upperBound) {

