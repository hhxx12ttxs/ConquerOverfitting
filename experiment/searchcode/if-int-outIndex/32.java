int bits24;
int bits6;

char[] out = new char[((octetString.length - 1) / 3 + 1) * 4];

int outIndex = 0;
out[outIndex++] = alphabet[bits6];

// padding
out[outIndex++] = &#39;=&#39;;
} else if (octetString.length - i == 1) {

