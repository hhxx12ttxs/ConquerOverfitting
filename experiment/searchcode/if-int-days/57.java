int days=0;

while (beans>=0)
{
days++;
if (days%3==0)
{
beans-=6;
}

if (days%5==0)
{
beans+=8;
}
}
System.out.println (&quot; therefore it will take &quot; + days + &quot; for him to finish the jellybeans&quot;);

