package cn.nd.social.privategallery.imageviewer;

public class Cubic implements Easing {
public double easeInOut(double time, double start, double end,
double duration) {
if ((time /= duration / 2.0) < 1.0)
return end / 2.0 * time * time * time + start;

