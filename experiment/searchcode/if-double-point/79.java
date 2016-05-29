package edu.colorado.phet.signalcircuit.phys2d;

public class DoublePoint {
double x;
double y;

public DoublePoint() //immutable
public double getLength() {
return Math.sqrt( x * x + y * y );
}

public DoublePoint( double x, double y ) {

