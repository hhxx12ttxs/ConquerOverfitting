int i = free[k];
k++;
double umin = assigncost[i][0] - v[0];
j1 = 0;
for(j = 1; j < dim; j++)
{
double h = assigncost[i][j] - v[j];
if(h >= usubmin)

