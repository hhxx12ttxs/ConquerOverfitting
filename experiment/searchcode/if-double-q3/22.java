double q3q3 = q3*q3;
if(mat==null) {
mat = new double[3][3];
}
mat[0][0] = (q0q0+q1q1-q2q2-q3q3);
double q2q2 = q2*q2, q2q3 = q2*q3;
double q3q3 = q3*q3;
if(mat==null) {
mat = new double[16];
}
mat[0] = (q0q0+q1q1-q2q2-q3q3);

