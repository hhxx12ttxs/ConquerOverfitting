public void findSmallestDiff(int[] a, int[] b)
{
int[] temp_a = a.clone(),
temp_b = b.clone();

if(a.length > b.length)
{
temp_a = b.clone();
temp_b = a.clone();
}

int[] minPos = new int[3],
result_minPos = new int[3];

