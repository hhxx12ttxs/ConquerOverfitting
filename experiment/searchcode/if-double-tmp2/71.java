vec.add(a);
}

double tmp1, tmp2;
int i = 0;
while(vec.size() > 0) {
if(vec.elementAt(i).charAt(0) == &#39;*&#39;) {
tmp1 = vec.elementAt(i).charAt(i);
tmp2 = vec.elementAt(i).charAt(i+1);
}
else if(sb.charAt(i) == &#39;/&#39;)	{
tmp1 = sb.charAt(i-1);

