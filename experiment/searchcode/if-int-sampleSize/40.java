actualSize = 0;
sampleBuffer = new float[bufferSize * sampleSize];
}

int toPos(int i, int index) {
return index * sampleSize + i;
}

public void fetchSample(float[] sample, int off) {

