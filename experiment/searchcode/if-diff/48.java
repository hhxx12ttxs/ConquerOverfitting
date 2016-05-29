int max_diff = diff[0];
for (int i=1; i<n-1; i++)
{
if (diff[i-1] > 0)
diff[i] += diff[i-1];
if (max_diff < diff[i])
max_diff = diff[i];
}
return max_diff;

