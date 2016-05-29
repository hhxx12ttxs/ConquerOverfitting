double var_X = a / 500 + var_Y;
double var_Z = var_Y - b / 200.0;

if (Math.pow(var_Y, 3) > 0.008856) {
double C = cmy.C;
double M = cmy.M;
double Y = cmy.Y;

double var_K = 1.0;

if (C < var_K) {

