long purchased = a[0], stock = 1;

long profit = 0;
int startIndex = 1, endIndex = a.length-1;
while(true) {
endIndex = findMax(a, startIndex, endIndex);
for(int i=startIndex;i<endIndex;i++) {

