public String diag(int n) {
int i;
String result = &quot;&quot;;
int space;
for (i = 0; i != n; i++){
for (space = 0; space < i; space++) {
result = result + &quot;*\n&quot;;
}
return result;
}
//for loop
public String diagWord(String w) {
int i;
String result = &quot;&quot;;

