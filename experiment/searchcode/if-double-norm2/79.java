double[] doc = new double[m_numAttributes+1];

for(int a = 0; a<inst.numValues(); a++)
if(inst.index(a) != inst.classIndex())
//Ce serai pas mal de lancer une exception, si on a le temps
private double calculSimilariteCosinus(double[] v1, double[] v2){
double ps = 0,norm1=0,norm2=0;//On initialise le ps a 1, pour eviter toute valeur nulle

