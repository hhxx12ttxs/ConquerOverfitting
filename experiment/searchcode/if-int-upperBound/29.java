public static String createRandomString() {
String s = &quot;&quot;;

int upperBound = RANDOM.nextInt();
if (upperBound > 10) upperBound = 10;
if (upperBound < 1) upperBound = 1;
for (int i = 0; i < upperBound; i++) {

