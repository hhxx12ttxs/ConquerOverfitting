for (int i = 0; i < rowNum + 2; i++)
{
if ((i == 0) || (i == rowNum + 1))
{
for (int j = 0; j < columnNum + 2; j++)
int blockRow = Env.getInteger(&quot;blockRow&quot; + i) + 1;
int blockColumn = Env.getInteger(&quot;blockColumn&quot; + i) + 1;
map[blockRow][blockColumn] = PathState.BLOCK;

