Integer tokenLen = Integer.parseInt(args[1]);
String phase = args[2];

/*
* STATUS: Just report
*/
if (phase.equalsIgnoreCase(PHASES[0])) {
initer.initializeChars(inputFile, ergo);

for (int currentLength = MIN_TOKEN_LENGTH; currentLength <= tokenLen; currentLength++) {

