char[] out = new char[((octetString.length - 1) / 3 + 1) * 4];
int outIndex = 0;
int i = 0;

while ((i + 3) <= octetString.length) {
out[outIndex++] = ALPHABET[bits6];
bits6 = (bits24 &amp; 0x0000003F);
out[outIndex++] = ALPHABET[bits6];
}
if (octetString.length - i == 2) {

