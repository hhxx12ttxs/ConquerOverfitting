for(int ii = 0; ii< N; ii++)
{
arr.add(stdin.nextInt());
}
Collections.sort(arr);
double minDiff = Double.POSITIVE_INFINITY;
double diff = Math.abs(arr.get(ii)-arr.get(ii+1));
if(diff<minDiff)
minDiff = diff;
}
for(int ii = 0; ii < N-1; ii++)
{
double diff = Math.abs(arr.get(ii)-arr.get(ii+1));

