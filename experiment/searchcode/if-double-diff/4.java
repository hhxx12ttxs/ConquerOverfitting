int numClasses = classVotes.length;
double diff = 0.0;
double sqDiff = 0.0;
if (weight > 0.0 &amp;&amp; numClasses > trueClass) {
for (int i = 0; i < numClasses; i++) {
if (!Double.isInfinite(classVotes[i]) &amp;&amp; !Double.isNaN(classVotes[i])) {

