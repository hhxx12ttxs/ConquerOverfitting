public Cube(int num, double spec, double diff, double ref,double[] point, double side){
this.color = new double[]{255, 0, 0};
this.reflection = ref;

points=new double[][] {point, {point[0] + side, point[1], point[2]},
{point[0]+ side, point[1]+ side, point[2]},

