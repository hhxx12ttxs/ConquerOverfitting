int[] a = new int[n];
dfs(a, 0);
return count;
}

public void dfs(int[] a, int row) {
return;
}
for (int i = 0; i<a.length; i++)
{
a[row] = i;
if (check(a, row))

