for(int j=block.getColumnStart(); j < block.getColumnEnd(); ++j)
{
// if(this.getCase(i, j).caseValue == 0 &amp;&amp; (i != line &amp;&amp; j != column))
int blockLine = (int) Math.floor(line/3) * 3;
int blockColumn = (int) Math.floor(column/3) * 3;

return new Block(blockLine, blockLine+3, blockColumn, blockColumn+3);

