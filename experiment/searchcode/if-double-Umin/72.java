static double ymax = 599;
static double umin = -2;
static double umax = 1;
static double vmin = -1.2;
static double vmax = 1.2;
double c_re = (x - xmin) / (double) (xmax - xmin) * (umax - umin) + umin;
double c_im = (y - ymin) / (double) (ymax - ymin) * (vmax - vmin) + vmin;

