private double rgbToH(double r,double g,double b, double min,double max){
double h = 0;
if(min==max){
return 0;
} else {
double maxMinusMin = (double) (max-min);
h = (double)((60)*gb)/(double)maxMinusMin;
}else
if(max==r &amp;&amp; g<b){
double gb = (double) (g-b);
h = (double)((60)*gb)/(double)maxMinusMin+360;

