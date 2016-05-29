private float treshold;
private float result;

public TresholdFilter(float treshold, float result) {
this.treshold = treshold;
this.result = result;
}

@Override
public float[] apply(float[] data) {

