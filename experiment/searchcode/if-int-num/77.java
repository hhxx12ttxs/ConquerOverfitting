StringBuffer s = new StringBuffer();
if (num >= 1000) {
int k = num / 1000;
int i = 0;
s.append(&quot;IV&quot;);
num -= 4;
}
if (num >= 1) {
int i = 0;
while (i < num) {

