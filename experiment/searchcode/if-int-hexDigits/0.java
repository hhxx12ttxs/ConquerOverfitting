public static String txt2Hexa(byte[] bytes) {
if( bytes == null ) return null;
String hexDigits = &quot;0123456789abcdef&quot;;
int j = ((int) bytes[i]) &amp; 0xFF;
sbuffer.append(hexDigits.charAt(j / 16));
sbuffer.append(hexDigits.charAt(j % 16));

