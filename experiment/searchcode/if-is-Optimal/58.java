package selfPreparation.dp_greedy;

/**
* Given a sequence of n weeks, a plan is specified by a choice of &quot;low-stress&quot;, &quot;high-stress&quot;, or &quot;none&quot; for each of the
//base cases
optimal[0] = 0;
if(low[0] > high[0])
optimal[1] = low[0];
else
optimal[1] = high[0];

