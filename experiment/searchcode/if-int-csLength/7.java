private boolean regionMatches(byte[] otherBytes, int otherOffset, int n) {
for (int i = 0; i < n; ++i) {
if (bytes[offset + i] != otherBytes[otherOffset + i]) {
private boolean regionMatches(int start, CharSequence cs, int n) {
for (int i = 0; i < n; ++i) {
if (bytes[offset + start + i] != cs.charAt(i)) {

