package cn.asmm.imagezoom.easing;

public class Expo implements Easing {

@Override
public double easeOut( double time, double start, double end, double duration ) {
public double easeInOut( double time, double start, double end, double duration ) {
if ( time == 0 ) return start;

