public static int decodeType(int key)
{
if(key>=5&amp;&amp;key<=9)
{
return 0;
}
else if(key>=10&amp;&amp;key<=14)
{
return 1;
}
return 2;
}
public static int decodeStrength(int key)
{
if(key>=5&amp;&amp;key<=9)
{
return key-5;

