public void process(double time) {
double U = positiveLoad.Uc - negativeLoad.Uc;
if(U > Umax) return;
double UTarget = Math.sqrt(2*E/C);
if(UTarget > Umax) UTarget = Umax;
double Q = (UTarget-U)*C;
double I = Q/time;
if(I > Imax) I = Imax;

