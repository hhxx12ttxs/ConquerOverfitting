for (int i = 0; i < idx.length; i++)
{
idx[i] = -1;
}

int longest = 0;

for (int i = 0; i < s.length(); i++)
{
if (idx[s.charAt(i)] == -1)
int prev = idx[s.charAt(i)];

for (int j = 0; j < idx.length; j++)
{
if (idx[j] != -1 &amp;&amp; idx[j] <= prev)

