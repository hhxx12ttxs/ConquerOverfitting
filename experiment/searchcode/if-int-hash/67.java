for (int i = 0; i < str.length(); i++) {
hash = (hash << 4) + str.charAt(i);

if ((x = hash &amp; 0xF0000000L) != 0) {
public long APHash(String str) {
long hash = 0xAAAAAAAA;

for (int i = 0; i < str.length(); i++) {
if ((i &amp; 1) == 0) {

