public HitInfo rayHit(Vector o, Vector d, double tmin, double tmax){
double t;
double A = P0.X - P1.X;
double B = P0.Y - P1.Y;
double C = P0.Z - P1.Z;

double D = P0.X - P2.X;
double E = P0.Y - P2.Y;
double F = P0.Z - P2.Z;

