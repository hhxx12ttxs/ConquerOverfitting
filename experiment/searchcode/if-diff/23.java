currDiff = a[i] + a[j];

if(currDiff < 0) {
if(Math.abs(currDiff) < Math.abs(minDiff)){
minDiff = currDiff;
difference = new Difference(a[i],a[j], minDiff);
}
i++;
}

if(currDiff > 0) {

