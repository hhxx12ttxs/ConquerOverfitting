public String position(String swaps)
{
char pos = &#39;M&#39;;
for (int i = 0; i < swaps.length(); i++) {
if(swaps.charAt(i)==&#39;L&#39;){
if(pos==&#39;L&#39;){
pos = &#39;M&#39;;
} else if (pos==&#39;M&#39;){
pos = &#39;L&#39;;
}
} else if (swaps.charAt(i)==&#39;R&#39;){

