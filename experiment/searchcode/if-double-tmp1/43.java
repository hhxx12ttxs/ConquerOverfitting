double[] tmp1 = tmp_3[3];
double[] tmp2 = tmp_3[4];
double t = tmp_1[0];
//Schneidet der Strahl die Ebene des Dreiecks ?
MyVector.add(lb, rayPos, rayDir);
MyVector.scale(tmp1,pl,-1);
double d = tmp_1[1];
d = tmp1[0]*pl[3] + tmp1[1]*pl[4] + tmp1[2]*pl[5];

