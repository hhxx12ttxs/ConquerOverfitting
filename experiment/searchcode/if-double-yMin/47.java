double[] Xmin, Xmax, Ymin, Ymax, Zmin, Zmax;
double[][] widths;
double[][] XY;

public BoxPlot3D(double[][] _XY, double[][] w, Color c, String n) {
Xmin = new double[XY.length];  		Xmax = new double[XY.length];
Ymin = new double[XY.length]; 		Ymax = new double[XY.length];
Zmin = new double[XY.length]; 		Zmax = new double[XY.length];

