if (i < len) //found decimal
{
int decimals = len - i - 1;

if (decimals > numDecimals)
{
int index = i + 1 + numDecimals;

char[] ca = s.toCharArray();

//check if need to round up
int count = 0;

