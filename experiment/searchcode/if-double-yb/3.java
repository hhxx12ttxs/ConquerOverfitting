Xa=X[i-1];
Xb=X[i];
double Ya;
double Yb;
Ya=Y[i-1];
Yb=Y[i];
//Calculate slope from p1 to p2
double m = (Xb-Xa)/(Yb-Ya);
double a=Ya*(x-Xb)/(Xa-Xb);
double b=Yb*(x-Xa)/(Xa-Xb);

return a-b;

}
}

