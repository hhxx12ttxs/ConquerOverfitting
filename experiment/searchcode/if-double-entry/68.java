import nu.thiele.mllib.data.Data.DataEntry;

public class Statistics {

public static double max(List<Double> t){
double maks = Double.MIN_VALUE;
for(double d : t){
if(d > maks) maks = d;
}
return maks;

