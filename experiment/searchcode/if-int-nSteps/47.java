do	line = br.readLine();
while (line.startsWith(&quot;#&quot;) || line.isEmpty());

for (int t = 0; t < nSteps; ++t) {

if (line.startsWith(&quot;#&quot;) || line.isEmpty()) continue;
for (int t = 0; t < nSteps; ++t) {
for (int g = 0; g < nGenes; ++g) {
if (mad.getData(g,t) == 0) mad.setData(g,t,0.0001);

