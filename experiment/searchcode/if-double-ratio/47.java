int GoldMineRace(int[][] cities, double[] speed, int[] goldmine) {
double [] ratio = new double [speed.length];
ratio[i] = Math.sqrt(Math.pow(citX-Xgold, 2) +  Math.pow(citY-Ygold, 2))/ speed[i];
}
for(int i=0; i<ratio.length; i++)  {
for(int j=0; j<ratio.length; j++) {
if(ratio[i] == ratio[j] &amp;&amp; i!=j){

