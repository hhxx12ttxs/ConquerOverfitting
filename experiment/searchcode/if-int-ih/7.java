double[] Ih = new QuadratureMethod().getApproximateSolution();
/*findMaxDifference(Ih, IH);
if (findDifference(Ih, IH) < EntryPoint.EPS) {
return IH;
}*/
if (findMaxDifference(Ih, IH) / D < EntryPoint.EPS) {

