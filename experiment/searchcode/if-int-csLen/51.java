public static final <A extends Appendable> A reverseAppend (final A sb, final CharSequence cs) throws IOException
{
final int    csLen=(null == cs) ? 0 : cs.length();
return ((null == sb) || (csLen <= 0)) ? sb : reverseAppend(sb, cs, 0, csLen);

