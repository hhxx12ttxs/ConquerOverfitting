public static byte[] decode(String encoded) {
// IMPLEMENTATION NOTE: Special care is taken to permit odd number of hexadecimal digits.
int resultLengthBytes = (encoded.length() + 1) / 2;
byte[] result = new byte[resultLengthBytes];
int resultOffset = 0;
int encodedCharOffset = 0;
if ((encoded.length() % 2) != 0) {

