public static String setStringWidth(String str,int i)
{
while(strlen(str) != i)
{
if(strlen(str) < i) str += &quot; &quot;;
if(strlen(str) > i) str.substring(0,-1);
}
return str;
}
public static int strlen(String str)

