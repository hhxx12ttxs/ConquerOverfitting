public boolean isNumber(String inputStr){

int compare;

for(int i = 0; i < inputStr.length(); i++){//inputStr에 담겨있는 것들을 모두 돌림
compare = (int)(inputStr.charAt(i));
//아스키구나...!! 48~57 사이에 들어가는지 파악할 것

if(compare != 48&amp;&amp;compare != 49&amp;&amp;compare != 50&amp;&amp;compare != 51&amp;&amp;compare != 52&amp;&amp;compare != 53&amp;&amp;compare != 54&amp;&amp;compare != 55&amp;&amp;compare != 56&amp;&amp;compare != 57){

