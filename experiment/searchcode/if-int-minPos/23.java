public void sort()
{
for (int i = 0; i < a.length-1; i++)
{
int minPos = minimumPosition(i);
swap(minPos, i);
public int minimumPosition (int from)
{
int minPos = from;

for (int i = from+1; i < a.length; i++)
{
if (a[i] < a[minPos])

