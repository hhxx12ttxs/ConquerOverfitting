public static int FNVHash(byte[] data) {
int hash = (int) 2166136261L;
for (byte b : data)
hash = (hash * 16777619) ^ b;
if (M_SHIFT == 0)
int test = 0;
for (int i = 0; i < str.length(); i++) {
hash = (hash << OneEighth) + str.charAt(i);
if ((test = hash &amp; HighBits) != 0) {

