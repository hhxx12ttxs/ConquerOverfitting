int x = 26;

int upper = 0;

while (upper * upper < x) {
upper++;
}

int lower = 0;

while (lower < upper) {

int sum = lower * lower + upper * upper;

if (sum == x) {
Log(lower + &quot;,&quot; + upper);

