List<LZ77Tupel> list = new ArrayList<LZ77Tupel>();
String windowBuffer = &quot;&quot;;
String input = text;

String lookAheadBuffer;
int indexFound2 = 0;
int indexFound;
int searchLen;

while (input.length() > 0) {

