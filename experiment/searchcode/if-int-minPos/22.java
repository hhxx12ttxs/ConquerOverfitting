for (int i = 0; i < data.length - 1; i++) {
int minpos = i;
for (int j = i+1; j < data.length; j++) {
minpos = j;
}
}
if(i!=minpos) {
int tmp = data[i];

