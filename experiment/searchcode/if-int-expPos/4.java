Double d = Double.valueOf(value);

if (!value.matches(&quot;.*\\..*&quot;)) {

try {
Byte valInt = Byte.parseByte(value);
System.out.println(&quot;---------------------Mantisa mayor a 0-------------------------&quot;);

int expPos;
for (expPos = 31; expPos >= 0; expPos--) {

