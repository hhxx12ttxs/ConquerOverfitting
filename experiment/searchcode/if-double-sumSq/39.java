public double distanceSq(Coordinate other){
assert other.coords.length == coords.length;
double sumSq = 0.0;
for(int i = 0; i < coords.length; i++){
return sumSq;
}

public double distanceSq(){
double sumSq = 0.0;
for(int i : coords){
sumSq += Math.pow(i, 2);

