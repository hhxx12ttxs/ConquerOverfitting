public class Cubic implements Easing {

@Override
public double easeOut( double time, double start, double end, double duration ) {
public double easeIn( double time, double start, double end, double duration ) {
return end * ( time /= duration ) * time * time + start;

