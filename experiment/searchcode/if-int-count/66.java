int length = s.length();
int count = 0,temp = 0;
for(int i = 0 ; i < length ; i++){
char c = s.charAt(i);
if(c == &#39; &#39;){
temp = count == 0 ? temp : count;
count = 0;
}else
count++;

