for (int nRow = 0; nRow <= (lLen-1)/16; nRow++)
{
String pszStr = &quot;&quot;;

// fill in the hex view of the data
int nCol;
switch (lBytes)
{
case 1: // show as bytes
default:
if (nRow*16+nCol < lLen)
pszStr += String.format(&quot;%02x &quot;, pbBuf[nRow*16+nCol]);

