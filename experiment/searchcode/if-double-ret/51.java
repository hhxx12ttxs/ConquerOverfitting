public class SpringDiscountStrategyImpl extends DiscountStrategy {

@Override
public double total(double price, int num) {
// TODO Auto-generated method stub
double ret = price * num * 0.8;
if ( ret > 300 &amp;&amp; ret < 700 ) {
ret = ret-30;
}else if ( ret >= 700 &amp;&amp; ret < 2000 ) {

