char[] out = new char[((octetString.length - 1) / 3 + 1) * 4];

int outIndex = 0;
int i = 0;

while ((i + 3) <= octetString.length) {
// padding
out[outIndex++] = &#39;=&#39;;
} else if (octetString.length - i == 1) {
// store the octets

