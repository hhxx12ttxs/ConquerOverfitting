int sampleSizeHeight = (int) (options.outHeight / (float) height);

int sampleSize = sampleSizeWidth > sampleSizeHeight ? sampleSizeHeight
: sampleSizeWidth;

if (sampleSize <= 0) {
sampleSize = 1;
}
options.inSampleSize = sampleSize;

