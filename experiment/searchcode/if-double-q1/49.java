public Quaternions divide(Quaternions q1, Quaternions q2)
throws CommonException {
double s = q2.w*q2.w + q2.x*q2.x + q2.y*q2.y + q2.z*q2.z;
(- q1.w*q2.z + q1.x*q2.y - q1.y*q2.x + q1.z*q2.w) / s);
return q;
}

public Quaternions multiply_scalar(Quaternions q1, double s)

