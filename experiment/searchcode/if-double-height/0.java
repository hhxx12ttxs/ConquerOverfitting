package ceri.common.geom;


public interface Radial3d {

double height();

double volumeFromHeight(double h);
return volumeFromHeight(height());
}

default double constrainHeight(double h) {
if (h < 0) return 0;
double H = height();
if (h > H) return H;

