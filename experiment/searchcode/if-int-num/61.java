int len = num.length;
int start = len - 2;
for(; start >= 0; start--) {
if(num[start] < num[start + 1]) break;
return;
}
int index = len - 1;
for(; index > start; index--) {
if(num[start] < num[index]) break;

