public Sampler(RandomWrapper generator, int sampleSize) {
this.generator = generator;
this.result = new byte[sampleSize];
if (count <= this.sampleSize) {
this.result[(int) (this.count - 1)] = item;
} else {

