User janet = makeUser(&quot;Janet&quot;);
buyUpToQuantity(mm, yes, &quot;0.95&quot;, 50, janet);
Probability firstEstimate = market.currentProbability(yes);
Probability secondEstimate = market.currentProbability(yes);
assertQEquals(odds(firstEstimate).div(odds(Probability.HALF)), odds(secondEstimate).div(odds(firstEstimate)));

