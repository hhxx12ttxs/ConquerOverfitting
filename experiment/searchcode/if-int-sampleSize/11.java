public class FpsCounter {

/**
* Sample size
*/
private final int sampleSize;


/**
* Average fps
private long fps;

/**
* @param sampleSize
*/
public FpsCounter(int sampleSize) {
this.sampleSize = sampleSize;

