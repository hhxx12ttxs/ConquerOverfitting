double Ki; // integral gain governs rate of convergence of gyroscope biases
double halfT; // half the sample period
double q0, q1, q2, q3; // quaternion elements representing the estimated orientation
// integrate quaternion rate
double q0i = (-q1 * gx - q2 * gy - q3 * gz) * halfT;
double q1i = (q0 * gx + q2 * gz - q3 * gy) * halfT;

