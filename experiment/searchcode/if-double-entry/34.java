double weight = weights.weight(i);
if ((weight==0) || (vec1[i]==0)){
entryFactor[i] = new double[] { Double.MAX_VALUE, vec0[i], vec1[i] };
double weight = weights.weight(i);
double yi = y[i];
if (yi == 0){
if (x[i] == 0){
entryFactor[i] = new double[] {Double.POSITIVE_INFINITY, 0, 0};

