* @see java.io.Reader#read()
*/
@Override
public int read () throws IOException
{
if (!isOpen())
final int            curPos=getCurPos();
final CharSequence    cs=getCharSequence();
final int            csLen=(null == cs) ? 0 : cs.length();

