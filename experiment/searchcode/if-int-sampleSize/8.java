private int sampleRateKHz;

/**
* Total number of samples in the clip
*/
private int sampleSize;
public AudioClip(byte[] samples, int sampleRateKHz, int sampleSize){
if(sampleSize < samples.length){
this.samples = Arrays.copyOf(samples, sampleSize);

