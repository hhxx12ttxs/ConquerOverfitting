int search(String str,String key,int start)
{
int sub=str.substring(start).indexOf(key);
if(sub!=-1)
{
return start+sub;
}
final int keylen=key.length();
final int strlen=str.length();
for(int i=Math.max(0, keylen-strlen+start);i<keylen;i++)

