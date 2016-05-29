package exam1.prob2;

public class Div extends Arith {
@Override
double calculate() {
if (super.b == (double)0) {
return (double)0;
}

double ret = (super.a / super.b);
return ret;
}
}

