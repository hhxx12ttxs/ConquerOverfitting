int pivot = (int)Math.floor(left+(right-left)/2.0);
int newPivot = partition(left,right,pivot,list);
if (newPivot==k) {//do nothing
double scK = ((StatePermutation)list.get(k)).getScore();
//		   for i=left:(right-1)
//		       if(s(i)<pivotVal),
//		           temp=s(storeIndex);
//		           s(storeIndex)=s(i);

