static Pattern pattern = null;

public static Pattern getPattern()
{
if (pattern == null)
{
String string_pattern = &quot;(deadline|submi|Due|Camera-ready|Camera ready|soume|camera-paper|final version|version final|soumi)&quot;;
/*for (int i = 0; i < keywords.values().length; ++i)
{
if (i > 0)
string_pattern += &quot;|&quot;;

string_pattern += keywords.values()[i].toString();

