int min = Integer.MAX_VALUE, maxDiff = 0;
for(int i = 0; i < prices.length; i++)
{
if(prices[i] < min)
else
{
int diff = prices[i] - min;
if(diff > maxDiff)
maxDiff = diff;
}
}

return maxDiff;
}
}

