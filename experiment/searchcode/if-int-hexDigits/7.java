public static String byteToHexString(byte b) {
int n = b;
if (n < 0) {
n = 256 + n;
}
int d1 = n / 16;
int d2 = n % 16;
return hexDigits[d1] + hexDigits[d2];
}

}

