//	kernel:	 2nd order b-spline
public static double kernel(double x_){
if(x_ <-1.5) return 0.0;
else if(x_ <-0.5) return (x_+1.5)*(x_+1.5);
for(int i = index_min; i < index_max; ++i){
double delta_t =  (tnow - xdata.getTi(i))/dt;
double delta_x = vx * (xdata.getTi(i) - tnow) + xbar - xdata.getXi(i);

