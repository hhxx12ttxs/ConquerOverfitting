public double easeOut( double time, double start, double end, double duration ) {
return end * Math.sqrt( 1.0 - ( time = time / duration - 1.0 ) * time ) + start;
}

@Override
public double easeIn( double time, double start, double end, double duration ) {

