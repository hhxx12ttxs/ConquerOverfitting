int nLin = m.length;
int nCol = m[0].length;
int soma = 0;

for(int i = 0; i < nLin; i++)
for(int i = 0; i < nLin; i++)
{
maiores[i] = m[i][0];
for(int j = 1; j < nCol; j++)
{
if(m[i][j] > maiores[i])

