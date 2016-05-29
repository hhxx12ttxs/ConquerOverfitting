int end = nums.length;
if (end == 0)
return false;

int step = nums[0];

for(int i = 1; i < end; i++)
if (step > 0)
{
step--;

