double c2=Math.cos(joints[1]);
double s2=Math.sin(joints[1]);
double c3=Math.cos(joints[2]);
double s3=Math.sin(joints[2]);
if(c3>1.0)  c3=1.0;
if(c3<-1.0)  c3=-1.0;

double s3=Math.sqrt(1-Math.pow(c3, 2));
double teta3=Math.atan2(s3,c3);

