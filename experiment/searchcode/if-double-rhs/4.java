out.m[2][1] = lhs.m[2][1] - rhs.m[2][1];
out.m[2][2] = lhs.m[2][2] - rhs.m[2][2];

return out;
}

public double[][] m = new double[3][3];
public void mul(Matrix3x3 rhs) {
double[][] out = new double[3][3];

