double growthField[][] = rc.senseCowGrowth();
double maxGrowth = 0;
for (int i = 0; i < rc.getMapWidth(); i++) {
for (int j = 0; j < rc.getMapHeight(); j++) {
double growth = growthField[i][j];
if (growth > maxGrowth &amp;&amp; rand.nextBoolean()) {

