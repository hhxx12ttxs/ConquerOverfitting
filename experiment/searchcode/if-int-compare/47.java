public int value;
public Card(int c,int v)
{
color=c;
value=v;
}
public int compareTo(Card compareCard)
{
if(this.color<compareCard.color)
return 1;
else if(this.color==compareCard.color &amp;&amp; this.value<compareCard.value)

