* Created by radu on 07.12.2014.
*/
public class AvgHolder {

private BoundedQueue<Double> samples;
private int sampleSize;
public AvgHolder(int sampleSize) {
this(sampleSize, 0);
}

public AvgHolder(int sampleSize, double silenceLevel) {

