public class util
{
public static int binlog( int bits ) // returns 0 for bits=0
{
int log = 0;
if( ( bits &amp; 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
if( bits >= 256 ) { bits >>>= 8; log += 8; }

