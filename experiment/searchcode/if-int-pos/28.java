final boolean isPoisoned(int posI, int posJ, Game state)
{
if(posI == 16) return true;
if(posI == -1) return true;
while(fromI < 15 &amp;&amp; getCurrentContent(fromI + 1, fromJ) == &#39;.&#39;) fromI++;
return fromI;
}

final boolean isBadFall(int posI, int posJ, Game state)
{
if(canSimulate)

