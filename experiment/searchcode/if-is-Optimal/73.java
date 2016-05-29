public boolean isSuccess(double d)
{
return d/optimal*100<200;
}

public void update(double d)
{
count++;
if (evaluations == -1 &amp;&amp; isSuccess(d))
{
evaluations = count;
}
if (d==optimal) optimalfound = true;
}

}

