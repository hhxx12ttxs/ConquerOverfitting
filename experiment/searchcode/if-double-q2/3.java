double q1 = rotationVector.getX();
double q2 = rotationVector.getY();
double q3 = rotationVector.getZ();

q0 = 1 - q1*q1 - q2*q2 - q3*q3;
q0 = (q0 > 0) ? Math.sqrt(q0) : 0;

double sq_q1 = 2 * q1 * q1;

