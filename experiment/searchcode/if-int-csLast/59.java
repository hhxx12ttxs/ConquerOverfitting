boolean eq = false;
if (cs == null) {
eq = strs == null;
}

if (strs != null) {
for (int i = 0; i < strs.length; i++) {
int searchLength = searchChars.length;
int csLast = csLength - 1;
int searchLast = searchLength - 1;
for (int i = 0; i < csLength; i++) {

