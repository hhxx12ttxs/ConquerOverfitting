int length = 0;
int startIndex = 0;
while(length < input.length){
length++;

if (input[length] == &#39; &#39;){
reverse(input, startIndex, length);

