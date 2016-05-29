public ColorXYZ getColorXYZ() {
double var_Y = (l + 16) / 116d;
double var_X = a / 500d + var_Y;
double var_Z = var_Y - b / 200d;

if (Math.pow(var_Y, 3d) > 0.008856)
var_Y = Math.pow(var_Y, 3);
else

