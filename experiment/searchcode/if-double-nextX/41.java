* @param order 多项式拟合的最高次数
* @param x x变量
* @param y y值
* @param nextX 下一个要预测的值的x变量
* @return
* double  拟合值
*/
public static double getNextY(int order ,double[] x,double[] y,double nextX){
public static double getNextY(int order ,List<Double> lsX, List<Double> lsY,double nextX){

if(lsX.size()!=lsY.size())
throw new RuntimeException(&quot;list X 与 list Y 的大小不一样！&quot;);

