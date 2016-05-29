printSorted(workSpace);
}

public void recmergeSort(int[] workspace, int lowerbound, int upperbound){

if (lowerbound == upperbound)
{
return;
}
else
{
int mid = (lowerbound + upperbound) / 2;

