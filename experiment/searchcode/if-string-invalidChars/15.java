private static String removeIllegalCharacters(String rawString, HashMap<Character,Object> invalidChars)
for(int i=0;i<chars.length;i++)
{
if(!invalidChars.containsKey(chars[i]))
newSB.append(chars[i]);

