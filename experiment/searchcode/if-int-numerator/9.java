public class Rational {
int numerator = 1 ;   //分子
int denominator = 1; //分母
void setNumerator(int a) {  //设置分子
int c=f(Math.abs(a),denominator);  //计算最大公约数
numerator = a/c;

