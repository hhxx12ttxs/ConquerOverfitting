package featureSelect;

public class Min {
public double[] Min(double[] d){
double[] result = new double[2];
result[0] = 100000;
result[1] = 0;
for(int i = 0;i<d.length;i++){
if(result[0] > d[i]){

