public class Cell {

//Boundaries
private double xmin,xmax;
private double ymin,ymax;
private double[] L;
this.ymax = ymax;

L = new double[2];
L[0] = xmax - xmin;
L[1] = ymax - ymin;
if(stdout)
System.out.println(&quot;Created simulation cell &quot;+L[0]+&quot; by &quot;+L[1]);

