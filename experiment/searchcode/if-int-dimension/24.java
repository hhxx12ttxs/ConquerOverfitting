for(int j=0; j<dimension; j++)
{
if(matrix[i][j] == 0)
{
row[i]++;
column[j]++;
}

}
}

for(int i=0; i<dimension; i++)
{
if(row[i]>0)
{
for(int j=0; j<dimension; j++)

