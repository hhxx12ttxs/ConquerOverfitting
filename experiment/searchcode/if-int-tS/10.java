while (!ordered)
{
ordered = true;
for (int i = 0; i < ts.length - 1; i++)
{
T a = ts[i];
T b = ts[i + 1];

if(comparator.compare(a, b) > 0)

