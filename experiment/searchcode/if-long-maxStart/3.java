long maxStart = -1;
for(long i=1; i<1000000; i++)
{
long currLength = collatz(i);
if(currLength>maxLength)
public static long collatz(long num)
{
long steps = 1;
while(num!=1)
{
if(num%2==0)
{
num = num / 2;

