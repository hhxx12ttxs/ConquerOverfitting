public class QuickUnionPath
{
private int[] id;
private int[] sz;

public QuickUnionPath(int N)
{
id = new int[N];
sz = new int[N];

for (int i=0; i<N; i++)
{
id[i] = i;

