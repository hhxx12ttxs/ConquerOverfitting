public int grayCheck(byte a, byte b) {
byte c = a ^ b;
int count = 0;
for (int i = 0; i < 8; i++) {
if (((1 << i) &amp; c) >>> i) == 1) {
count++;
}
}
return count == 1 ? 1 : 0;
}

