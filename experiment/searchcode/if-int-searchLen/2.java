int indexFound;
int searchLen;
int pointer=0;

while (input.length() > 0) {
searchLen = 1;
lookAheadBuffer = input.substring(0, searchLen);
if (windowBuffer.lastIndexOf(lookAheadBuffer) == -1) {

