private final static String[] hexDigits = {
private String byteToHexString(byte b) {
int n = b;
if (n < 0)
n = 256 + n;
int d1 = n / 16;
int d2 = n % 16;

