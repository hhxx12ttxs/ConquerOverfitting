int pivotrow;
double temp;
double temp1;
for(int i=0;i<rows;i++)
{

pivotrow=i;
for (int j=i+1;j<rows;j++)
{
if(matrix[j][i] > matrix[pivotrow][i] || matrix[pivotrow][i]!=0)

