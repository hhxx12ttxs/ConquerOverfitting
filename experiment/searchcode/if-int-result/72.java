public String fence(int h, int w) {
String result = &quot;&quot;;
for (int i = h; i > 0; i = i -1){
if(i==h|| i==1){
result = result + &quot;+&quot;;
for (int j = w-1; j > 0; j = j-1){
if (j == 1){
result = result + &quot;+&quot;;

