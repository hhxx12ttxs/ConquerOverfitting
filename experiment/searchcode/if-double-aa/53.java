public String solution() {
double[] solution = solve();
return &quot;Observed AA: &quot; + String.format(&quot;%.3f&quot;, solution[0]) +
double totalCount = mAAcount + mAacount + maacount;
double observedAA = mAAcount / totalCount;
double observedAa = mAacount / totalCount;

