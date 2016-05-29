public String convert(String s, int nRows) {
if(1 == nRows) {
return s;
}
StringBuilder sb = new StringBuilder();
cur = row - 1;
if(row == 1 || row == nRows) {
while(cur < s.length()) {

