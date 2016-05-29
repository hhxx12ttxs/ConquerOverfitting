public static double average_gain(ArrayList<Double> arr){
double ret = 0.0;
int count = 0;
for(double i: arr){
if(i>0.0){ //>0 if new > old
ret += i;
count += 1;
}
}
if(count>0){
ret = ret/((double)count);

