* Returns true if the character is an XML name character.
*/
public static boolean isNameChar(int ch)
{
if (ch < 0x20)
return false;
else if (ch < 128)
return isAsciiNameChar[ch];
else
return (isBaseChar(ch) ||

