int mx;


double cns[]=new double[101];
double lns[]=new double[101];

m=50;

for (n=10;n<91;n++){

k1=k[n]+th[m]*f(k[n],lab(m,n))-con(m,n);
ep=ep+Math.pow(cn[n]-cns[n],2)+Math.pow(ln[n]-lns[n],2);
}

for (n=10;n<91;n++){
cn[n]=cns[n];
ln[n]=lns[n];
}


}

void sec(){

double k1,l1,n1,c1,r1,uc,w1;

