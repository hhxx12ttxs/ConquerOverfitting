public boolean lesserThan(Version v) {
final int iv[] = v.intVersion();
for (int i = 0; i < intVersion.length; i++) {
if (intVersion[i] < iv[i]) {
if (intVersion[i] < iv[i]) {
return true;
}

