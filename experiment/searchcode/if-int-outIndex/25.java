final char[] out = new char[((octetString.length - 1) / 3 + 1) * 4];

int outIndex = 0;
int i = 0;

while ((i + 3) <= octetString.length) {
// store the octets
bits6 = (bits24 &amp; 0x0000003F);
out[outIndex++] = alphabet[bits6];
}

if (octetString.length - i == 2) {
// store the octets

