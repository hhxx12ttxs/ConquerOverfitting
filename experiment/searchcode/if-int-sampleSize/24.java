public class ReservoirSampler<T> {
public List<T> sample;
int sampleSize;
int numSeen;

public ReservoirSampler(int sampleSize) {
public void add(T x) {
if (sample.size() < sampleSize) {
sample.add(x);
} else if (FastRandom.rand().nextUniform() < sampleSize*1.0 / (numSeen+1)) {

