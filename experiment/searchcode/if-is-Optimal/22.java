// nothing can be added to Knapsack.
if ( w == 0 || n == weight.length)
{
optimalKnapsack[w][n] = new ListWithBenefits(0);
// this node can not be added to Knapsack.
if(weight[n] > w)
return (optimalKnapsack[w][n+1] == null) ? findOptimalItems(w, n+1, val, weight, optimalKnapsack) : optimalKnapsack[w][n+1];

