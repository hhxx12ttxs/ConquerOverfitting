for (int i = 0; i < str.length(); i++) {
hash = (hash << OneEighth) + str.charAt(i);

if ((test = hash &amp; HighBits) != 0) {
for (int i = 0; i < str.length(); i++) {
hash = (hash << 4) + str.charAt(i);
if ((x = (int) (hash &amp; 0xF0000000L)) != 0) {

