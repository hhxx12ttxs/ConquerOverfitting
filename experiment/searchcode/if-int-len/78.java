public int hIndex(int[] citations) {
if(citations.length == 0) return 0;
int len = citations.length;
for(int c : citations) {
if(c > len) count[len]++;
else count[c]++;
}

int res = 0;

