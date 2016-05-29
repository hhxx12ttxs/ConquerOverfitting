public double refSignalRamp(BSim sim, double ymax, double ymin, double tstart, double t1, double t2, double tend){

double timepoint=sim.getTime();
double m1=(ymax-ymin)/(t1-tstart);
double m2=(ymin-ymax)/(tend-t2);
double m3=0;
double interceptforuppart=ymin - m1*tstart;
double interceptfordownpart=ymax - m2*t2;

