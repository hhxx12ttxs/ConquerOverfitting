double sqy = q1.y*q1.y;
double sqz = q1.z*q1.z;
double unit = sqx + sqy + sqz + sqw; // if normalised is one, otherwise is correction factor
double test = q1.x*q1.y + q1.z*q1.w;
if (test > 0.499*unit) { // singularity at north pole

