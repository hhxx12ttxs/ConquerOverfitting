for (int i = 0; i < values.length; i++) {
cumulativeSum[i+1] = cumulativeSum[i] + values[i];
}

double randomValue = Math.random() * cumulativeSum[cumulativeSum.length-1];
for (int i = 0; i < cumulativeSum.length -1; i++) {
if(randomValue < cumulativeSum[i+1])
return i;
}

throw new RuntimeException(&quot;Unable to generate index by softMax method&quot;);

