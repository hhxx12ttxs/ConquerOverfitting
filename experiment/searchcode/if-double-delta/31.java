double a = -10.0;
double b = 10.0;
double delta = 0.01; // precision

System.out.println(find_root(a, b, delta));
double c = f((a+b)/2.0);
double x = (a+b)/2.0;

if (c == 0.0 || (c < delta &amp;&amp; c > -delta))
return x;
else if (f(a)*c < 0)

