public List<T> sample;
int sampleSize;
int numSeen;

public ReservoirSampler(int sampleSize) {
sample = new ArrayList<>();
} else if (FastRandom.rand().nextUniform() < sampleSize*1.0 / (numSeen+1)) {
int replaceIndex = FastRandom.rand().nextInt(sampleSize);
sample.set(replaceIndex, x);

