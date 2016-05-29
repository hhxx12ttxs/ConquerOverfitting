Vector v = r.pos;

double sx2 = sx * sx;
double sy2 = sy * sy;
double sz2 = sz * sz;

double a = r.dir.x * r.dir.x / sx2 + r.dir.y * r.dir.y / sy2 + r.dir.z * r.dir.z / sz2;
double b = 2 * (r.dir.x * r.pos.x / sx2 + r.dir.y * r.pos.y / sy2 + r.dir.z * r.pos.z / sz2);

