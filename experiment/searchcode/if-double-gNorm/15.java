double[] aid; double[] oldai;
double oldi; double oldj;
double[] pid;
double Gnorm; double dinorm; int Gpos;
if (Math.abs(G[k])<eps)G[k]=0;
ind=0;
if (alpha_status[k]>0){
Gnorm+=G[k]*G[k];
dinorm+=di[ind]*di[ind];

