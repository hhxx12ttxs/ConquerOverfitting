double var_X = a / 500 + var_Y;
double var_Z = var_Y - b / 200.0;

if (Math.pow(var_Y, 3) > 0.008856) {
double var_G = g / 255.0; // Where G = 0 � 255
double var_B = b / 255.0; // Where B = 0 � 255

if (var_R > 0.04045) {
var_R = Math.pow((var_R + 0.055) / 1.055, 2.4);

