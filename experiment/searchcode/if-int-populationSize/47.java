public void setParameters(int m, int r, int n){
//Correct for invalid parameters
if (m < 1) m = 1;
if (r < 0) r = 0; else if (r > m) r = m;
int k = (int)Math.rint(x);
return comb(type1Size, k) * comb(populationSize - type1Size, sampleSize - k) / c;

