double compute(double sD, double sN, double time, double nB, double nE ) {
double point=0;
double tU=60;
double timeRec=(time/tU)%24;
if(((timeRec>(nB))&amp;&amp;(timeRec<(24)))||((timeRec>=(0))&amp;&amp;(timeRec<(nE)))){

