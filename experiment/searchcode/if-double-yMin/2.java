protected double[] x;
protected double[] y;
protected double xmin,ymin,xmax,ymax;

public ConstXYDataSource(double[] y) {
if (y[i]<ymin) ymin=y[i];
if (x[i]>xmax) xmax=x[i];
if (y[i]>ymax) ymax=y[i];
}
}

public double[] getXData() {return x;}

