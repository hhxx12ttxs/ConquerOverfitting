// Start with 2 because we skip it in the loop below
int total = 2;
int a = 1;
int b = 2;

int newvalue = a + b;
while (newvalue < 4000000)
{
if (newvalue % 2 == 0)
{
total += newvalue;

