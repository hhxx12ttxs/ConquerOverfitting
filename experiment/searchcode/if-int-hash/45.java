package hash.conflicts;

public class AP {
public long aphash(String str){
long hash = 0xAAAAAAAA;
for(int i = 0; i < str.length(); i++)
{
if((i &amp; 1) == 0)
hash ^=((hash << 7) ^ str.charAt(i) ^ (hash >> 3));

