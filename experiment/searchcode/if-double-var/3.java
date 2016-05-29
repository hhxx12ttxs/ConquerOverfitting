double var_G = G / 255.0;        //G from 0 to 255
double var_B = B / 255.0;        //B from 0 to 255

if ( var_R > 0.04045 ) var_R = Math.pow((var_R + 0.055)/1.055, 2.4);
double var_G = var_X * -0.9689 + var_Y *  1.8758 + var_Z *  0.0415;
double var_B = var_X *  0.0557 + var_Y * -0.2040 + var_Z *  1.0570;

if ( var_R > 0.0031308 ) var_R = 1.055 * Math.pow(var_R, 1.0/2.4) - 0.055;

