for(int i = 0; i < a.length; ++i)		// O(n)
{
int minPos = minPos(i, a);			// less than O(n)
if(a[i] > a[minPos])
{
int temp = a[i];
a[i] = a[minPos];
a[minPos] = temp;

