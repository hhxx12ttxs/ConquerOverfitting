public NormAndLexicographicalOrdering(int[] bandMix) {
super();
this.bandMix = bandMix;
}


private double reducedNorm(double [] o)
{
double res=0.0;
for(int i=0;i<o.length;i++)
if(o[i]>0)

