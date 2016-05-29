package cn.asmm.imagezoom.easing;

public class Cubic implements Easing {

@Override
public double easeOut( double time, double start, double end, double duration ) {
return end * ( ( time = time / duration - 1.0 ) * time * time + 1.0 ) + start;

