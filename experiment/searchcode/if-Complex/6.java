public class complex {

double x, y;
ErrorHandler error;
complex z, u;

public complex(float d, float b) {
public complex cdiv(complex u, complex v) {

z = new complex(0, 0);
float d = (float) (v.x * v.x + v.y * v.y), eps = epsilon();
if (d < eps * eps)

