public class SwapBits
{
public static long swapBits(long x, int i, int j)
{
if (((x >> i) &amp; 1) != ((x >> j) &amp; 1))
log(&quot;x =&quot; + Long.toBinaryString(x));

long sw = swapBits(x, 61, 60);

log(&quot;sw=&quot; + Long.toBinaryString(sw));

