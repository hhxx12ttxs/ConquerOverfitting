public static byte[] hexStr2ByteArray(String hexStr) {
if (hexStr.length() < 1)
return null;
byte[] result = new byte[hexStr.length() / 2];
char[] resultCharArray = new char[byteArray.length * 2];
int index = 0;
for (byte b : byteArray) {
resultCharArray[index++] = hexDigits[b >>> 4 &amp; 0xf];

