Tuple3f tuple3f = new Vector3f();
float heading, attitude, bank;
double test = q1.x * q1.y + q1.z * q1.w;
if (test > 0.499) { // singularity at north pole
bank = 0;
} else {
double sqx = q1.x * q1.x;
double sqy = q1.y * q1.y;
double sqz = q1.z * q1.z;

